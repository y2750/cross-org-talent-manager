package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import cn.hutool.json.JSONUtil;
import com.crossorgtalentmanager.mapper.*;
import com.crossorgtalentmanager.model.dto.talentmarket.*;
import com.crossorgtalentmanager.model.entity.*;
import com.crossorgtalentmanager.model.enums.EvaluationPeriodEnum;
import com.crossorgtalentmanager.model.enums.EvaluationTypeEnum;
import com.crossorgtalentmanager.model.enums.PointsChangeReasonEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.*;
import com.crossorgtalentmanager.service.*;
import com.crossorgtalentmanager.ai.AiTalentComparisonService;
import com.crossorgtalentmanager.model.entity.TalentCompareRecord;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人才市场服务实现类
 *
 * @author y
 */
@Slf4j
@Service
public class TalentMarketServiceImpl implements TalentMarketService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private EmployeeProfileMapper employeeProfileMapper;

    @Resource
    private EvaluationMapper evaluationMapper;

    @Resource
    private EvaluationDimensionScoreMapper dimensionScoreMapper;

    @Resource
    private EvaluationTagRelationMapper tagRelationMapper;

    @Resource
    private EvaluationTagMapper tagMapper;

    @Resource
    private EvaluationUnlockMapper unlockMapper;

    @Resource
    private TalentBookmarkMapper bookmarkMapper;

    @Resource
    private UnlockPriceConfigMapper priceConfigMapper;

    @Resource
    private CompanyMapper companyMapper;

    @Resource
    private CompanyPointsService companyPointsService;

    @Resource
    private EvaluationDimensionMapper dimensionMapper;

    @Resource
    private TalentViewLogMapper viewLogMapper;

    @Resource
    private CompanyPreferenceMapper preferenceMapper;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private UserService userService;

    @Resource
    private ProfileAccessRequestService profileAccessRequestService;

    @Resource
    private ContactAccessRequestService contactAccessRequestService;

    @Resource
    private AiTalentComparisonService aiTalentComparisonService;

    @Resource
    private com.crossorgtalentmanager.service.AiAnalysisTaskService aiAnalysisTaskService;

    @Resource
    private TalentCompareRecordService talentCompareRecordService;

    @Override
    public String getAiAnalysisTaskStatus(String taskId) {
        if (aiAnalysisTaskService == null) {
            return "not_found";
        }
        return aiAnalysisTaskService.getTaskStatus(taskId);
    }

    @Override
    public String getAiAnalysisTaskResult(String taskId) {
        if (aiAnalysisTaskService == null) {
            return null;
        }
        return aiAnalysisTaskService.getTaskResult(taskId);
    }

    @Override
    public String getAiAnalysisTaskError(String taskId) {
        if (aiAnalysisTaskService == null) {
            return null;
        }
        return aiAnalysisTaskService.getTaskError(taskId);
    }

    // 高级搜索功能积分消耗配置
    private static final BigDecimal ADVANCED_SEARCH_TAG_INCLUDE_COST = new BigDecimal("0.5"); // 包含标签筛选
    private static final BigDecimal ADVANCED_SEARCH_TAG_EXCLUDE_COST = new BigDecimal("0.5"); // 排除标签筛选
    private static final BigDecimal ADVANCED_SEARCH_EVAL_KEYWORD_COST = new BigDecimal("1.0"); // 评价内容搜索
    private static final BigDecimal ADVANCED_SEARCH_EXCLUDE_REASON_COST = new BigDecimal("0.5"); // 离职原因排除
    private static final BigDecimal ADVANCED_SEARCH_MAJOR_INCIDENT_COST = new BigDecimal("0.2"); // 重大违纪排除
    private static final BigDecimal ADVANCED_SEARCH_ATTENDANCE_COST = new BigDecimal("0.3"); // 出勤率筛选

    /**
     * 内部搜索方法，不扣除积分（供相似人才推荐等系统功能使用）
     */
    private Page<TalentVO> searchTalentsInternal(TalentSearchRequest request, User loginUser) {
        Long companyId = loginUser.getCompanyId();
        long pageNum = request.getPageNum();
        long pageSize = request.getPageSize();

        // 构建员工基础查询
        QueryWrapper employeeQuery = QueryWrapper.create()
                .select("id", "name", "gender", "photo_url", "status", "company_id");

        // 关键词搜索（姓名）
        if (StrUtil.isNotBlank(request.getKeyword())) {
            employeeQuery.like("name", request.getKeyword());
        }

        // 性别筛选
        if (StrUtil.isNotBlank(request.getGender())) {
            employeeQuery.eq("gender", request.getGender());
        }

        // 在职状态筛选
        if (Boolean.TRUE.equals(request.getOnlyLeft())) {
            employeeQuery.eq("status", false);
        } else if (Boolean.TRUE.equals(request.getOnlyWorking())) {
            employeeQuery.eq("status", true);
        }

        // 先查询所有符合基础条件的员工
        List<Employee> employees = employeeMapper.selectListByQuery(employeeQuery);

        if (CollUtil.isEmpty(employees)) {
            return new Page<>(Collections.emptyList(), pageNum, pageSize, 0);
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());

        // 排除本公司员工（包括当前和曾经在本公司的员工）
        if (Boolean.TRUE.equals(request.getExcludeOwnCompany()) && companyId != null) {
            Set<Long> ownCompanyEmployeeIds = getOwnCompanyEmployeeIds(companyId);
            employeeIds.removeAll(ownCompanyEmployeeIds);
        }

        // 职位筛选（从档案中查询）
        if (StrUtil.isNotBlank(request.getOccupation()) || CollUtil.isNotEmpty(request.getOccupations())) {
            Set<Long> occupationEmployeeIds = filterByOccupation(request.getOccupation(), request.getOccupations());
            employeeIds.retainAll(occupationEmployeeIds);
        }

        // 排除有重大违纪的
        if (Boolean.TRUE.equals(request.getExcludeMajorIncident())) {
            Set<Long> incidentEmployeeIds = getEmployeesWithMajorIncident();
            employeeIds.removeAll(incidentEmployeeIds);
        }

        // 最低出勤率筛选
        if (request.getMinAttendanceRate() != null) {
            Set<Long> attendanceEmployeeIds = filterByMinAttendanceRate(request.getMinAttendanceRate());
            employeeIds.retainAll(attendanceEmployeeIds);
        }

        // 离职原因排除
        if (CollUtil.isNotEmpty(request.getExcludeReasonKeywords())) {
            Set<Long> excludeEmployeeIds = filterByExcludeReasons(request.getExcludeReasonKeywords());
            employeeIds.removeAll(excludeEmployeeIds);
        }

        // 标签筛选
        if (CollUtil.isNotEmpty(request.getIncludeTagIds()) || CollUtil.isNotEmpty(request.getExcludeTagIds())) {
            employeeIds = filterByTags(employeeIds, request.getIncludeTagIds(), request.getExcludeTagIds());
        }

        // 评价内容模糊搜索
        if (StrUtil.isNotBlank(request.getEvaluationKeyword())) {
            Set<Long> keywordEmployeeIds = filterByEvaluationKeyword(request.getEvaluationKeyword());
            employeeIds.retainAll(keywordEmployeeIds);
        }

        if (CollUtil.isEmpty(employeeIds)) {
            return new Page<>(Collections.emptyList(), pageNum, pageSize, 0);
        }

        // 计算每个员工的平均评分
        Map<Long, BigDecimal> employeeScores = calculateEmployeeAverageScores(employeeIds);

        // 评分筛选
        if (request.getMinAverageScore() != null || request.getMaxAverageScore() != null) {
            employeeIds = filterByScore(employeeIds, employeeScores,
                    request.getMinAverageScore(), request.getMaxAverageScore());
        }

        // 排序
        List<Long> sortedEmployeeIds = sortEmployees(employeeIds, employeeScores,
                request.getSortField(), request.getSortOrder());

        // 分页
        int total = sortedEmployeeIds.size();
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + (int) pageSize, total);

        if (fromIndex >= total) {
            return new Page<>(Collections.emptyList(), pageNum, pageSize, total);
        }

        List<Long> pagedIds = sortedEmployeeIds.subList(fromIndex, toIndex);

        // 构建TalentVO列表
        List<TalentVO> talentVOs = buildTalentVOList(pagedIds, companyId, employeeScores);

        return new Page<>(talentVOs, pageNum, pageSize, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Page<TalentVO> searchTalents(TalentSearchRequest request, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");

        Long companyId = loginUser.getCompanyId();
        long pageNum = request.getPageNum();

        // 计算高级搜索积分消耗（仅在第一页时扣除，避免翻页重复扣除）
        // 如果 skipPointDeduction 为 true，则跳过积分扣除（用于恢复搜索条件时不重复扣除）
        if (pageNum == 1 && companyId != null && !Boolean.TRUE.equals(request.getSkipPointDeduction())) {
            BigDecimal advancedSearchCost = calculateAdvancedSearchCost(request);
            if (advancedSearchCost.compareTo(BigDecimal.ZERO) > 0) {
                // 检查积分是否充足
                BigDecimal currentPoints = companyPointsService.getTotalPoints(companyId);
                ThrowUtils.throwIf(currentPoints.compareTo(advancedSearchCost) < 0,
                        ErrorCode.OPERATION_ERROR,
                        "积分不足，高级搜索需消耗 " + advancedSearchCost + " 积分，当前积分 " + currentPoints);

                // 扣除积分（使用负数）
                companyPointsService.addPoints(
                        companyId,
                        advancedSearchCost.negate(),
                        PointsChangeReasonEnum.RIGHTS_CONSUMPTION.getValue(),
                        null,
                        "高级搜索消耗积分");
                log.info("高级搜索扣除积分：companyId={}, cost={}", companyId, advancedSearchCost);
            }
        }

        // 调用内部搜索方法（不扣除积分，积分已在上面扣除）
        return searchTalentsInternal(request, loginUser);
    }

    /**
     * 计算高级搜索消耗的积分
     */
    private BigDecimal calculateAdvancedSearchCost(TalentSearchRequest request) {
        BigDecimal totalCost = BigDecimal.ZERO;

        // 包含标签筛选
        if (CollUtil.isNotEmpty(request.getIncludeTagIds())) {
            totalCost = totalCost.add(ADVANCED_SEARCH_TAG_INCLUDE_COST);
        }

        // 排除标签筛选
        if (CollUtil.isNotEmpty(request.getExcludeTagIds())) {
            totalCost = totalCost.add(ADVANCED_SEARCH_TAG_EXCLUDE_COST);
        }

        // 评价内容模糊搜索
        if (StrUtil.isNotBlank(request.getEvaluationKeyword())) {
            totalCost = totalCost.add(ADVANCED_SEARCH_EVAL_KEYWORD_COST);
        }

        // 离职原因排除
        if (CollUtil.isNotEmpty(request.getExcludeReasonKeywords())) {
            totalCost = totalCost.add(ADVANCED_SEARCH_EXCLUDE_REASON_COST);
        }

        // 重大违纪排除
        if (Boolean.TRUE.equals(request.getExcludeMajorIncident())) {
            totalCost = totalCost.add(ADVANCED_SEARCH_MAJOR_INCIDENT_COST);
        }

        // 出勤率筛选
        if (request.getMinAttendanceRate() != null) {
            totalCost = totalCost.add(ADVANCED_SEARCH_ATTENDANCE_COST);
        }

        return totalCost;
    }

    @Override
    public BigDecimal calculateAdvancedSearchCostPreview(TalentSearchRequest request) {
        return calculateAdvancedSearchCost(request);
    }

    @Override
    public TalentDetailVO getTalentDetail(Long employeeId, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");
        ThrowUtils.throwIf(employeeId == null || employeeId <= 0, ErrorCode.PARAMS_ERROR, "员工ID无效");

        Employee employee = employeeMapper.selectOneById(employeeId);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        Long companyId = loginUser.getCompanyId();
        TalentDetailVO detailVO = new TalentDetailVO();

        // 基础信息
        detailVO.setId(employee.getId());
        detailVO.setName(employee.getName());
        detailVO.setGender(employee.getGender());
        detailVO.setPhotoUrl(employee.getPhotoUrl());
        detailVO.setStatus(employee.getStatus());

        // 判断是否为本公司员工（当前或曾经）
        boolean isOwnEmployee = isOwnCompanyEmployee(employeeId, companyId);
        detailVO.setIsOwnEmployee(isOwnEmployee);

        // 联系方式处理
        if (isOwnEmployee) {
            // 本公司员工，直接显示联系方式
            detailVO.setPhone(employee.getPhone());
            detailVO.setEmail(employee.getEmail());
            detailVO.setIdCardNumber(employee.getIdCardNumber());
            detailVO.setCanRequestContact(false);
            detailVO.setContactAuthorized(true);
        } else {
            // 非本公司员工，检查是否已获得电话和邮箱查看授权
            // 优先检查 type 5（查看电话和邮箱）的授权
            boolean phoneAndEmailAuthorized = contactAccessRequestService.hasAuthorizedAccess(
                    companyId, employeeId, 5); // 5=查看电话和邮箱
            boolean phoneAuthorized = phoneAndEmailAuthorized || contactAccessRequestService.hasAuthorizedAccess(
                    companyId, employeeId, 1); // 1=查看电话
            boolean emailAuthorized = phoneAndEmailAuthorized || contactAccessRequestService.hasAuthorizedAccess(
                    companyId, employeeId, 2); // 2=查看邮箱
            // 只有当电话和邮箱都授权时，才显示完整信息
            boolean contactAuthorized = phoneAuthorized && emailAuthorized;
            detailVO.setContactAuthorized(contactAuthorized);

            if (phoneAuthorized) {
                detailVO.setPhone(employee.getPhone());
            } else {
                detailVO.setPhone(maskPhone(employee.getPhone()));
            }

            if (emailAuthorized) {
                detailVO.setEmail(employee.getEmail());
            } else {
                detailVO.setEmail(maskEmail(employee.getEmail()));
            }

            // 检查是否已获得身份证号查看授权
            boolean idCardAuthorized = contactAccessRequestService.hasAuthorizedAccess(
                    companyId, employeeId, 3); // 3=查看身份证号
            if (idCardAuthorized) {
                detailVO.setIdCardNumber(employee.getIdCardNumber());
            } else {
                detailVO.setIdCardNumber(maskIdCardNumber(employee.getIdCardNumber()));
            }

            // 如果电话或邮箱任意一个未授权，可以申请
            detailVO.setCanRequestContact(!contactAuthorized);
        }

        // 当前公司名称
        if (employee.getCompanyId() != null) {
            Company currentCompany = companyMapper.selectOneById(employee.getCompanyId());
            if (currentCompany != null) {
                detailVO.setCurrentCompanyName(currentCompany.getName());
            }
        }

        // 工作经历（考虑权限）
        List<TalentDetailVO.ProfileSummaryVO> profiles = getProfileSummaries(employeeId, companyId,
                employee.getCompanyId());
        detailVO.setProfiles(profiles);

        // 职位历史
        Set<String> occupations = new LinkedHashSet<>();
        for (TalentDetailVO.ProfileSummaryVO profile : profiles) {
            if (StrUtil.isNotBlank(profile.getOccupation())) {
                occupations.add(profile.getOccupation());
            }
        }
        detailVO.setOccupationHistory(new ArrayList<>(occupations));
        detailVO.setLatestOccupation(CollUtil.isNotEmpty(profiles) ? profiles.get(0).getOccupation() : null);
        detailVO.setProfileCount(profiles.size());

        // 评价信息
        List<Evaluation> evaluations = getEmployeeEvaluations(employeeId);
        int totalEvaluations = evaluations.size();
        detailVO.setEvaluationCount(totalEvaluations);

        // 计算评分
        Map<Long, BigDecimal> scoreMap = calculateEmployeeAverageScores(Collections.singletonList(employeeId));
        detailVO.setAverageScore(scoreMap.getOrDefault(employeeId, BigDecimal.ZERO));

        // 各维度评分
        detailVO.setDimensionScores(calculateDimensionAverages(employeeId));

        // 标签统计
        Map<String, List<TalentVO.TagStatVO>> tagStats = getTagStatistics(employeeId);
        detailVO.setPositiveTags(tagStats.get("positive"));
        detailVO.setNeutralTags(tagStats.get("neutral"));

        // 处理评价列表（带解锁状态）
        List<TalentEvaluationVO> evaluationVOs = buildEvaluationVOList(evaluations, companyId, isOwnEmployee);
        detailVO.setEvaluations(evaluationVOs);

        // 统计解锁信息
        if (isOwnEmployee) {
            detailVO.setFreeEvaluationCount(totalEvaluations);
            detailVO.setUnlockedEvaluationCount(totalEvaluations);
            detailVO.setLockedEvaluationCount(0);
        } else {
            int freeCount = Math.min(FREE_EVALUATION_COUNT, totalEvaluations);
            List<Long> unlockedIds = getUnlockedEvaluationIds(companyId, employeeId);
            int unlockedCount = freeCount + unlockedIds.size();
            detailVO.setFreeEvaluationCount(freeCount);
            detailVO.setUnlockedEvaluationCount(unlockedCount);
            detailVO.setLockedEvaluationCount(Math.max(0, totalEvaluations - unlockedCount));
        }

        // 企业积分
        if (companyId != null) {
            detailVO.setCompanyPoints(companyPointsService.getTotalPoints(companyId));
        }

        // 收藏状态
        detailVO.setBookmarked(isBookmarked(companyId, employeeId));

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal unlockEvaluation(EvaluationUnlockRequest request, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");
        ThrowUtils.throwIf(request.getEvaluationId() == null, ErrorCode.PARAMS_ERROR, "评价ID不能为空");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        Long evaluationId = request.getEvaluationId();

        // 检查是否已解锁
        if (isEvaluationUnlocked(companyId, evaluationId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该评价已解锁");
        }

        // 获取评价信息
        Evaluation evaluation = evaluationMapper.selectOneById(evaluationId);
        ThrowUtils.throwIf(evaluation == null, ErrorCode.NOT_FOUND_ERROR, "评价不存在");

        // 检查是否为本公司员工
        if (isOwnCompanyEmployee(evaluation.getEmployeeId(), companyId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "本公司员工评价无需解锁");
        }

        // 获取解锁价格
        BigDecimal cost = getUnlockPrice(evaluation.getEvaluationType());
        ThrowUtils.throwIf(cost == null, ErrorCode.SYSTEM_ERROR, "未配置解锁价格");

        // 检查积分是否足够
        BigDecimal currentPoints = companyPointsService.getTotalPoints(companyId);
        if (currentPoints.compareTo(cost) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分不足，需要" + cost + "积分，当前" + currentPoints + "积分");
        }

        // 扣除积分
        companyPointsService.addPoints(companyId, cost.negate(),
                PointsChangeReasonEnum.RIGHTS_CONSUMPTION.getValue(),
                evaluation.getEmployeeId(),
                "解锁评价（" + EvaluationTypeEnum.getEnumByValue(evaluation.getEvaluationType()).getText() + "）");

        // 记录解锁
        EvaluationUnlock unlock = EvaluationUnlock.builder()
                .companyId(companyId)
                .employeeId(evaluation.getEmployeeId())
                .evaluationId(evaluationId)
                .evaluationType(evaluation.getEvaluationType())
                .pointsCost(cost)
                .unlockTime(LocalDateTime.now())
                .isDelete(false)
                .build();
        unlockMapper.insert(unlock);

        log.info("评价解锁成功：companyId={}, evaluationId={}, cost={}", companyId, evaluationId, cost);
        return cost;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal batchUnlockEvaluations(EvaluationUnlockRequest request, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");
        ThrowUtils.throwIf(CollUtil.isEmpty(request.getEvaluationIds()), ErrorCode.PARAMS_ERROR, "评价ID列表不能为空");

        BigDecimal totalCost = BigDecimal.ZERO;
        for (Long evaluationId : request.getEvaluationIds()) {
            EvaluationUnlockRequest singleRequest = new EvaluationUnlockRequest();
            singleRequest.setEvaluationId(evaluationId);
            try {
                BigDecimal cost = unlockEvaluation(singleRequest, loginUser);
                totalCost = totalCost.add(cost);
            } catch (BusinessException e) {
                // 已解锁或本公司员工，跳过
                log.debug("跳过评价解锁：evaluationId={}, reason={}", evaluationId, e.getMessage());
            }
        }
        return totalCost;
    }

    @Override
    public boolean isEvaluationUnlocked(Long companyId, Long evaluationId) {
        if (companyId == null || evaluationId == null) {
            return false;
        }
        QueryWrapper query = QueryWrapper.create()
                .eq("company_id", companyId)
                .eq("evaluation_id", evaluationId);
        return unlockMapper.selectCountByQuery(query) > 0;
    }

    @Override
    public List<Long> getUnlockedEvaluationIds(Long companyId, Long employeeId) {
        if (companyId == null || employeeId == null) {
            return Collections.emptyList();
        }
        QueryWrapper query = QueryWrapper.create()
                .select("evaluation_id")
                .eq("company_id", companyId)
                .eq("employee_id", employeeId);
        List<EvaluationUnlock> unlocks = unlockMapper.selectListByQuery(query);
        return unlocks.stream().map(EvaluationUnlock::getEvaluationId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bookmarkTalent(TalentBookmarkRequest request, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");
        ThrowUtils.throwIf(request.getEmployeeId() == null, ErrorCode.PARAMS_ERROR, "员工ID不能为空");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        // 检查员工是否存在
        Employee employee = employeeMapper.selectOneById(request.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 检查是否已收藏（包括已删除的记录，因为唯一约束包含所有记录）
        TalentBookmark existingBookmark = bookmarkMapper.selectOneIncludingDeleted(companyId, request.getEmployeeId());

        if (existingBookmark != null) {
            Boolean isDelete = existingBookmark.getIsDelete();
            log.info("收藏检查：companyId={}, employeeId={}, existingBookmark.id={}, isDelete={}, isDelete类型={}",
                    companyId, request.getEmployeeId(), existingBookmark.getId(), isDelete,
                    isDelete != null ? isDelete.getClass().getName() : "null");

            // 判断记录是否已删除：isDelete 为 true 或 Boolean.TRUE 表示已删除
            // 注意：MyBatis 可能将 tinyint(1) 映射为 Boolean，也可能映射为 Integer
            boolean isDeleted = Boolean.TRUE.equals(isDelete) || (isDelete != null && isDelete);

            if (!isDeleted) {
                // 如果记录存在且未删除，说明已收藏
                log.warn("尝试重复收藏：companyId={}, employeeId={}, bookmarkId={}, isDelete={}",
                        companyId, request.getEmployeeId(), existingBookmark.getId(), isDelete);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "已收藏该人才");
            }
            // 如果记录存在但已删除，则恢复并更新备注
            log.info("恢复已删除的收藏记录：companyId={}, employeeId={}, bookmarkId={}",
                    companyId, request.getEmployeeId(), existingBookmark.getId());
            int updated = bookmarkMapper.restoreBookmark(companyId, request.getEmployeeId(), request.getRemark());
            ThrowUtils.throwIf(updated <= 0, ErrorCode.OPERATION_ERROR, "恢复收藏记录失败");
            return existingBookmark.getId();
        }

        // 如果记录不存在，则插入新记录
        TalentBookmark bookmark = TalentBookmark.builder()
                .companyId(companyId)
                .employeeId(request.getEmployeeId())
                .remark(request.getRemark())
                .isDelete(false)
                .build();
        bookmarkMapper.insert(bookmark);

        return bookmark.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbookmarkTalent(Long employeeId, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");
        ThrowUtils.throwIf(employeeId == null, ErrorCode.PARAMS_ERROR, "员工ID不能为空");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        QueryWrapper query = QueryWrapper.create()
                .eq("company_id", companyId)
                .eq("employee_id", employeeId);
        return bookmarkMapper.deleteByQuery(query) > 0;
    }

    @Override
    public Page<TalentVO> getBookmarkedTalents(long pageNum, long pageSize, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        QueryWrapper query = QueryWrapper.create()
                .eq("company_id", companyId)
                .orderBy("create_time", false);

        Page<TalentBookmark> bookmarkPage = bookmarkMapper.paginate(Page.of(pageNum, pageSize), query);

        if (CollUtil.isEmpty(bookmarkPage.getRecords())) {
            return new Page<>(Collections.emptyList(), pageNum, pageSize, 0);
        }

        List<Long> employeeIds = bookmarkPage.getRecords().stream()
                .map(TalentBookmark::getEmployeeId)
                .collect(Collectors.toList());

        Map<Long, BigDecimal> scoreMap = calculateEmployeeAverageScores(employeeIds);
        List<TalentVO> talentVOs = buildTalentVOList(employeeIds, companyId, scoreMap);

        return new Page<>(talentVOs, pageNum, pageSize, bookmarkPage.getTotalRow());
    }

    @Override
    public boolean isBookmarked(Long companyId, Long employeeId) {
        if (companyId == null || employeeId == null) {
            return false;
        }
        QueryWrapper query = QueryWrapper.create()
                .eq("company_id", companyId)
                .eq("employee_id", employeeId);
        return bookmarkMapper.selectCountByQuery(query) > 0;
    }

    @Override
    public List<UnlockPriceConfigVO> getUnlockPriceConfigs() {
        // 尝试从缓存获取
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache("unlockPriceConfigs");
                if (cache != null) {
                    org.springframework.cache.Cache.ValueWrapper wrapper = cache.get("all");
                    if (wrapper != null) {
                        Object cached = wrapper.get();
                        if (cached instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<UnlockPriceConfigVO> cachedList = (List<UnlockPriceConfigVO>) cached;
                            // 验证列表中的元素类型
                            if (!cachedList.isEmpty() && cachedList.get(0) instanceof UnlockPriceConfigVO) {
                                return cachedList;
                            }
                        }
                        // 缓存数据格式错误，清除缓存
                        log.warn("缓存数据格式错误，清除缓存并重新查询: unlockPriceConfigs");
                        cache.evict("all");
                    }
                }
            }
        } catch (org.springframework.data.redis.serializer.SerializationException e) {
            // 反序列化失败，清除缓存
            log.warn("缓存反序列化失败，清除缓存并重新查询: unlockPriceConfigs, error={}", e.getMessage());
            try {
                if (cacheManager != null) {
                    org.springframework.cache.Cache cache = cacheManager.getCache("unlockPriceConfigs");
                    if (cache != null) {
                        cache.evict("all");
                    }
                }
            } catch (Exception evictEx) {
                log.warn("清除缓存失败: unlockPriceConfigs, error={}", evictEx.getMessage());
            }
        } catch (Exception e) {
            // 捕获所有其他异常
            log.warn("从缓存获取数据时发生异常，将重新查询: unlockPriceConfigs, error={}", e.getMessage());
            try {
                if (cacheManager != null) {
                    org.springframework.cache.Cache cache = cacheManager.getCache("unlockPriceConfigs");
                    if (cache != null) {
                        cache.evict("all");
                    }
                }
            } catch (Exception evictEx) {
                log.warn("清除缓存失败: unlockPriceConfigs, error={}", evictEx.getMessage());
            }
        }

        // 从数据库查询
        QueryWrapper query = QueryWrapper.create()
                .eq("is_active", true)
                .orderBy("evaluation_type", true);
        List<UnlockPriceConfig> configs = priceConfigMapper.selectListByQuery(query);

        List<UnlockPriceConfigVO> result = configs.stream().map(config -> {
            UnlockPriceConfigVO vo = new UnlockPriceConfigVO();
            vo.setEvaluationType(config.getEvaluationType());
            vo.setEvaluationTypeName(EvaluationTypeEnum.getEnumByValue(config.getEvaluationType()) != null
                    ? EvaluationTypeEnum.getEnumByValue(config.getEvaluationType()).getText()
                    : "未知");
            vo.setPointsCost(config.getPointsCost());
            vo.setDescription(config.getDescription());
            return vo;
        }).collect(Collectors.toList());

        // 将结果存入缓存
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache("unlockPriceConfigs");
                if (cache != null) {
                    cache.put("all", result);
                }
            }
        } catch (Exception e) {
            log.warn("缓存数据失败: unlockPriceConfigs, error={}", e.getMessage());
        }

        return result;
    }

    @Override
    public BigDecimal getUnlockPrice(Integer evaluationType) {
        if (evaluationType == null) {
            return null;
        }
        QueryWrapper query = QueryWrapper.create()
                .eq("evaluation_type", evaluationType)
                .eq("is_active", true);
        UnlockPriceConfig config = priceConfigMapper.selectOneByQuery(query);
        return config != null ? config.getPointsCost() : null;
    }

    @Override
    public List<EvaluationTagVO> getAllTags() {
        // 尝试从缓存获取
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache("evaluationTags");
                if (cache != null) {
                    org.springframework.cache.Cache.ValueWrapper wrapper = cache.get("all");
                    if (wrapper != null) {
                        Object cached = wrapper.get();
                        if (cached instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<EvaluationTagVO> cachedList = (List<EvaluationTagVO>) cached;
                            // 验证列表中的元素类型
                            if (!cachedList.isEmpty() && cachedList.get(0) instanceof EvaluationTagVO) {
                                return cachedList;
                            }
                        }
                        // 缓存数据格式错误，清除缓存
                        log.warn("缓存数据格式错误，清除缓存并重新查询: evaluationTags");
                        cache.evict("all");
                    }
                }
            }
        } catch (org.springframework.data.redis.serializer.SerializationException e) {
            // 反序列化失败，清除缓存
            log.warn("缓存反序列化失败，清除缓存并重新查询: evaluationTags, error={}", e.getMessage());
            try {
                if (cacheManager != null) {
                    org.springframework.cache.Cache cache = cacheManager.getCache("evaluationTags");
                    if (cache != null) {
                        cache.evict("all");
                    }
                }
            } catch (Exception evictEx) {
                log.warn("清除缓存失败: evaluationTags, error={}", evictEx.getMessage());
            }
        } catch (Exception e) {
            // 捕获所有其他异常
            log.warn("从缓存获取数据时发生异常，将重新查询: evaluationTags, error={}", e.getMessage());
            try {
                if (cacheManager != null) {
                    org.springframework.cache.Cache cache = cacheManager.getCache("evaluationTags");
                    if (cache != null) {
                        cache.evict("all");
                    }
                }
            } catch (Exception evictEx) {
                log.warn("清除缓存失败: evaluationTags, error={}", evictEx.getMessage());
            }
        }

        // 从数据库查询
        QueryWrapper query = QueryWrapper.create()
                .eq("is_active", true)
                .orderBy("type", true)
                .orderBy("sort_order", true);
        List<EvaluationTag> tags = tagMapper.selectListByQuery(query);

        List<EvaluationTagVO> result = tags.stream().map(tag -> {
            EvaluationTagVO vo = new EvaluationTagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setType(tag.getType());
            vo.setDescription(tag.getDescription());
            vo.setSortOrder(tag.getSortOrder());
            vo.setIsActive(tag.getIsActive());
            return vo;
        }).collect(Collectors.toList());

        // 将结果存入缓存
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache("evaluationTags");
                if (cache != null) {
                    cache.put("all", result);
                }
            }
        } catch (Exception e) {
            log.warn("缓存数据失败: evaluationTags, error={}", e.getMessage());
        }

        return result;
    }

    @Override
    public boolean hasAccessPermission(User loginUser) {
        if (loginUser == null) {
            return false;
        }
        String role = loginUser.getUserRole();
        return UserRoleEnum.ADMIN.getValue().equals(role)
                || UserRoleEnum.COMPANY_ADMIN.getValue().equals(role)
                || UserRoleEnum.HR.getValue().equals(role);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据职位筛选员工
     */
    private Set<Long> filterByOccupation(String occupation, List<String> occupations) {
        Set<Long> resultIds = new HashSet<>();

        // 单个职位关键词搜索
        if (StrUtil.isNotBlank(occupation)) {
            QueryWrapper query = QueryWrapper.create()
                    .select("DISTINCT employee_id")
                    .like("occupation", occupation);
            List<EmployeeProfile> profiles = employeeProfileMapper.selectListByQuery(query);
            resultIds.addAll(profiles.stream().map(EmployeeProfile::getEmployeeId).collect(Collectors.toSet()));
        }

        // 多个职位关键词搜索（满足其一即可）
        if (CollUtil.isNotEmpty(occupations)) {
            for (String occ : occupations) {
                QueryWrapper query = QueryWrapper.create()
                        .select("DISTINCT employee_id")
                        .like("occupation", occ);
                List<EmployeeProfile> profiles = employeeProfileMapper.selectListByQuery(query);
                resultIds.addAll(profiles.stream().map(EmployeeProfile::getEmployeeId).collect(Collectors.toSet()));
            }
        }

        return resultIds;
    }

    /**
     * 获取有重大违纪的员工ID集合
     */
    private Set<Long> getEmployeesWithMajorIncident() {
        QueryWrapper query = QueryWrapper.create()
                .select("DISTINCT employee_id")
                .eq("has_major_incident", true);
        List<EmployeeProfile> profiles = employeeProfileMapper.selectListByQuery(query);
        return profiles.stream().map(EmployeeProfile::getEmployeeId).collect(Collectors.toSet());
    }

    /**
     * 根据最低出勤率筛选员工
     */
    private Set<Long> filterByMinAttendanceRate(BigDecimal minRate) {
        QueryWrapper query = QueryWrapper.create()
                .select("DISTINCT employee_id")
                .ge("attendance_rate", minRate);
        List<EmployeeProfile> profiles = employeeProfileMapper.selectListByQuery(query);
        return profiles.stream().map(EmployeeProfile::getEmployeeId).collect(Collectors.toSet());
    }

    /**
     * 根据离职原因排除关键词筛选
     */
    private Set<Long> filterByExcludeReasons(List<String> excludeKeywords) {
        Set<Long> excludeIds = new HashSet<>();
        for (String keyword : excludeKeywords) {
            QueryWrapper query = QueryWrapper.create()
                    .select("DISTINCT employee_id")
                    .like("reason_for_leaving", keyword);
            List<EmployeeProfile> profiles = employeeProfileMapper.selectListByQuery(query);
            excludeIds.addAll(profiles.stream().map(EmployeeProfile::getEmployeeId).collect(Collectors.toSet()));
        }
        return excludeIds;
    }

    /**
     * 根据标签筛选员工
     */
    private List<Long> filterByTags(List<Long> employeeIds, List<Long> includeTagIds, List<Long> excludeTagIds) {
        Set<Long> resultIds = new HashSet<>(employeeIds);

        // 包含标签筛选：找出拥有指定标签的员工
        if (CollUtil.isNotEmpty(includeTagIds)) {
            // 先找出包含指定标签的评价ID
            QueryWrapper tagQuery = QueryWrapper.create()
                    .select("DISTINCT evaluation_id")
                    .in("tag_id", includeTagIds)
                    .eq("is_delete", false);
            List<EvaluationTagRelation> tagRelations = tagRelationMapper.selectListByQuery(tagQuery);

            if (CollUtil.isEmpty(tagRelations)) {
                return Collections.emptyList();
            }

            List<Long> evaluationIds = tagRelations.stream()
                    .map(EvaluationTagRelation::getEvaluationId)
                    .collect(Collectors.toList());

            // 再根据评价ID找出员工ID
            QueryWrapper evalQuery = QueryWrapper.create()
                    .select("DISTINCT employee_id")
                    .in("id", evaluationIds);
            List<Evaluation> evaluations = evaluationMapper.selectListByQuery(evalQuery);
            Set<Long> includeEmployeeIds = evaluations.stream()
                    .map(Evaluation::getEmployeeId)
                    .collect(Collectors.toSet());
            resultIds.retainAll(includeEmployeeIds);
        }

        // 排除标签筛选：排除拥有指定标签的员工
        if (CollUtil.isNotEmpty(excludeTagIds)) {
            // 先找出包含指定标签的评价ID
            QueryWrapper tagQuery = QueryWrapper.create()
                    .select("DISTINCT evaluation_id")
                    .in("tag_id", excludeTagIds)
                    .eq("is_delete", false);
            List<EvaluationTagRelation> tagRelations = tagRelationMapper.selectListByQuery(tagQuery);

            if (CollUtil.isNotEmpty(tagRelations)) {
                List<Long> evaluationIds = tagRelations.stream()
                        .map(EvaluationTagRelation::getEvaluationId)
                        .collect(Collectors.toList());

                // 再根据评价ID找出员工ID
                QueryWrapper evalQuery = QueryWrapper.create()
                        .select("DISTINCT employee_id")
                        .in("id", evaluationIds);
                List<Evaluation> evaluations = evaluationMapper.selectListByQuery(evalQuery);
                Set<Long> excludeEmployeeIds = evaluations.stream()
                        .map(Evaluation::getEmployeeId)
                        .collect(Collectors.toSet());
                resultIds.removeAll(excludeEmployeeIds);
            }
        }

        return new ArrayList<>(resultIds);
    }

    /**
     * 根据评价内容关键词筛选员工
     */
    private Set<Long> filterByEvaluationKeyword(String keyword) {
        QueryWrapper query = QueryWrapper.create()
                .select("DISTINCT employee_id")
                .like("comment", keyword);
        List<Evaluation> evaluations = evaluationMapper.selectListByQuery(query);
        return evaluations.stream().map(Evaluation::getEmployeeId).collect(Collectors.toSet());
    }

    /**
     * 计算员工平均评分
     */
    private Map<Long, BigDecimal> calculateEmployeeAverageScores(List<Long> employeeIds) {
        Map<Long, BigDecimal> scoreMap = new HashMap<>();

        for (Long employeeId : employeeIds) {
            QueryWrapper evalQuery = QueryWrapper.create()
                    .select("id")
                    .eq("employee_id", employeeId);
            List<Evaluation> evaluations = evaluationMapper.selectListByQuery(evalQuery);

            if (CollUtil.isEmpty(evaluations)) {
                scoreMap.put(employeeId, BigDecimal.ZERO);
                continue;
            }

            List<Long> evaluationIds = evaluations.stream().map(Evaluation::getId).collect(Collectors.toList());

            // 获取所有维度评分并计算平均值
            QueryWrapper scoreQuery = QueryWrapper.create()
                    .in("evaluation_id", evaluationIds);
            List<EvaluationDimensionScore> scores = dimensionScoreMapper.selectListByQuery(scoreQuery);

            if (CollUtil.isNotEmpty(scores)) {
                double avg = scores.stream()
                        .mapToInt(EvaluationDimensionScore::getScore)
                        .average()
                        .orElse(0.0);
                BigDecimal avgScore = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
                scoreMap.put(employeeId, avgScore);
            } else {
                scoreMap.put(employeeId, BigDecimal.ZERO);
            }
        }

        return scoreMap;
    }

    /**
     * 根据评分筛选员工
     */
    private List<Long> filterByScore(List<Long> employeeIds, Map<Long, BigDecimal> scoreMap,
            BigDecimal minScore, BigDecimal maxScore) {
        return employeeIds.stream()
                .filter(id -> {
                    BigDecimal score = scoreMap.getOrDefault(id, BigDecimal.ZERO);
                    if (minScore != null && score.compareTo(minScore) < 0) {
                        return false;
                    }
                    if (maxScore != null && score.compareTo(maxScore) > 0) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 员工排序
     */
    private List<Long> sortEmployees(List<Long> employeeIds, Map<Long, BigDecimal> scoreMap,
            String sortField, String sortOrder) {
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);

        if ("averageScore".equals(sortField)) {
            return employeeIds.stream()
                    .sorted((a, b) -> {
                        BigDecimal scoreA = scoreMap.getOrDefault(a, BigDecimal.ZERO);
                        BigDecimal scoreB = scoreMap.getOrDefault(b, BigDecimal.ZERO);
                        return ascending ? scoreA.compareTo(scoreB) : scoreB.compareTo(scoreA);
                    })
                    .collect(Collectors.toList());
        }

        // 默认按ID倒序
        return employeeIds.stream()
                .sorted((a, b) -> ascending ? a.compareTo(b) : b.compareTo(a))
                .collect(Collectors.toList());
    }

    /**
     * 构建TalentVO列表
     */
    private List<TalentVO> buildTalentVOList(List<Long> employeeIds, Long companyId, Map<Long, BigDecimal> scoreMap) {
        List<TalentVO> result = new ArrayList<>();

        for (Long employeeId : employeeIds) {
            Employee employee = employeeMapper.selectOneById(employeeId);
            if (employee == null) {
                continue;
            }

            TalentVO vo = new TalentVO();
            vo.setId(employee.getId());
            vo.setName(employee.getName());
            vo.setGender(employee.getGender());
            vo.setPhotoUrl(employee.getPhotoUrl());
            vo.setStatus(employee.getStatus());

            // 当前公司名称
            if (employee.getCompanyId() != null) {
                Company company = companyMapper.selectOneById(employee.getCompanyId());
                if (company != null) {
                    vo.setCurrentCompanyName(company.getName());
                }
            }

            // 职位历史
            List<TalentDetailVO.ProfileSummaryVO> profiles = getProfileSummaries(employeeId);
            Set<String> occupations = new LinkedHashSet<>();
            for (TalentDetailVO.ProfileSummaryVO profile : profiles) {
                if (StrUtil.isNotBlank(profile.getOccupation())) {
                    occupations.add(profile.getOccupation());
                }
            }
            vo.setOccupationHistory(new ArrayList<>(occupations));
            vo.setLatestOccupation(CollUtil.isNotEmpty(profiles) ? profiles.get(0).getOccupation() : null);
            vo.setProfileCount(profiles.size());

            // 评分
            vo.setAverageScore(scoreMap.getOrDefault(employeeId, BigDecimal.ZERO));

            // 评价数量
            QueryWrapper evalQuery = QueryWrapper.create().eq("employee_id", employeeId);
            vo.setEvaluationCount((int) evaluationMapper.selectCountByQuery(evalQuery));

            // 标签统计
            Map<String, List<TalentVO.TagStatVO>> tagStats = getTagStatistics(employeeId);
            vo.setPositiveTags(tagStats.get("positive"));
            vo.setNeutralTags(tagStats.get("neutral"));

            // 收藏状态
            vo.setBookmarked(isBookmarked(companyId, employeeId));

            // 是否为本公司员工
            vo.setIsOwnEmployee(isOwnCompanyEmployee(employeeId, companyId));

            result.add(vo);
        }

        return result;
    }

    /**
     * 获取员工工作经历摘要（不考虑权限，内部方法）
     */
    private List<TalentDetailVO.ProfileSummaryVO> getProfileSummaries(Long employeeId) {
        return getProfileSummaries(employeeId, null, null);
    }

    /**
     * 获取员工工作经历摘要（考虑权限）
     * 
     * @param employeeId              员工ID
     * @param viewerCompanyId         查看者公司ID
     * @param targetEmployeeCompanyId 目标员工所属公司ID
     */
    private List<TalentDetailVO.ProfileSummaryVO> getProfileSummaries(Long employeeId, Long viewerCompanyId,
            Long targetEmployeeCompanyId) {
        QueryWrapper query = QueryWrapper.create()
                .eq("employee_id", employeeId)
                .orderBy("start_date", false);
        List<EmployeeProfile> profiles = employeeProfileMapper.selectListByQuery(query);

        return profiles.stream().map(profile -> {
            TalentDetailVO.ProfileSummaryVO vo = new TalentDetailVO.ProfileSummaryVO();
            vo.setProfileId(profile.getId());
            vo.setCompanyId(profile.getCompanyId());

            Company company = companyMapper.selectOneById(profile.getCompanyId());
            vo.setCompanyName(company != null ? company.getName() : null);

            Integer visibility = profile.getVisibility() != null ? profile.getVisibility() : 2;
            vo.setVisibility(visibility);

            // 入职和离职日期始终可见
            vo.setStartDate(profile.getStartDate() != null ? profile.getStartDate().toString() : null);
            vo.setEndDate(profile.getEndDate() != null ? profile.getEndDate().toString() : null);

            // 判断是否为同一公司（查看者公司 = 档案所属公司 或 查看者公司 = 员工当前公司）
            boolean isSameCompany = viewerCompanyId != null &&
                    (viewerCompanyId.equals(profile.getCompanyId()) || viewerCompanyId.equals(targetEmployeeCompanyId));

            // 根据可见性判断是否可查看详情
            boolean canViewDetail = false;
            boolean needRequest = false;
            boolean authorized = false;

            if (visibility == 2) {
                // 公开档案，任何人都可查看
                canViewDetail = true;
            } else if (visibility == 1) {
                // 对认证企业可见，需要申请
                if (isSameCompany) {
                    canViewDetail = true;
                } else {
                    // 检查是否已有授权
                    authorized = profileAccessRequestService.canAccessProfile(
                            profile.getId(), viewerCompanyId, employeeId);
                    canViewDetail = authorized;
                    needRequest = !authorized;
                }
            } else {
                // visibility == 0，完全保密
                canViewDetail = false;
            }

            vo.setCanViewDetail(canViewDetail);
            vo.setNeedRequest(needRequest);
            vo.setAuthorized(authorized);

            // 根据权限设置字段值（入职和离职日期始终可见，其他字段需要权限）
            if (canViewDetail) {
                vo.setOccupation(profile.getOccupation());
                vo.setAttendanceRate(profile.getAttendanceRate());
                vo.setHasMajorIncident(profile.getHasMajorIncident());
                vo.setReasonForLeaving(profile.getReasonForLeaving());
                vo.setPerformanceSummary(profile.getPerformanceSummary());
            } else {
                // 保密档案用 ** 展示
                vo.setOccupation("**");
                vo.setAttendanceRate(null);
                vo.setHasMajorIncident(null);
                vo.setReasonForLeaving("**");
                vo.setPerformanceSummary("**");
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取员工评价列表
     */
    private List<Evaluation> getEmployeeEvaluations(Long employeeId) {
        QueryWrapper query = QueryWrapper.create()
                .eq("employee_id", employeeId)
                .orderBy("evaluation_date", false)
                .orderBy("create_time", false);
        return evaluationMapper.selectListByQuery(query);
    }

    /**
     * 计算各维度平均分
     */
    private Map<String, BigDecimal> calculateDimensionAverages(Long employeeId) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();

        // 获取所有维度
        List<EvaluationDimension> dimensions = dimensionMapper.selectListByQuery(
                QueryWrapper.create().eq("is_active", true).orderBy("sort_order", true));

        // 获取员工所有评价ID
        List<Evaluation> evaluations = getEmployeeEvaluations(employeeId);
        if (CollUtil.isEmpty(evaluations)) {
            for (EvaluationDimension dim : dimensions) {
                result.put(dim.getName(), BigDecimal.ZERO);
            }
            return result;
        }

        List<Long> evaluationIds = evaluations.stream().map(Evaluation::getId).collect(Collectors.toList());

        for (EvaluationDimension dimension : dimensions) {
            QueryWrapper query = QueryWrapper.create()
                    .in("evaluation_id", evaluationIds)
                    .eq("dimension_id", dimension.getId());
            List<EvaluationDimensionScore> scores = dimensionScoreMapper.selectListByQuery(query);

            if (CollUtil.isNotEmpty(scores)) {
                double avg = scores.stream()
                        .mapToInt(EvaluationDimensionScore::getScore)
                        .average()
                        .orElse(0.0);
                BigDecimal avgScore = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
                result.put(dimension.getName(), avgScore);
            } else {
                result.put(dimension.getName(), BigDecimal.ZERO);
            }
        }

        return result;
    }

    /**
     * 获取标签统计
     */
    private Map<String, List<TalentVO.TagStatVO>> getTagStatistics(Long employeeId) {
        Map<String, List<TalentVO.TagStatVO>> result = new HashMap<>();
        result.put("positive", new ArrayList<>());
        result.put("neutral", new ArrayList<>());

        // 获取员工所有评价
        List<Evaluation> evaluations = getEmployeeEvaluations(employeeId);
        if (CollUtil.isEmpty(evaluations)) {
            return result;
        }

        List<Long> evaluationIds = evaluations.stream().map(Evaluation::getId).collect(Collectors.toList());

        // 获取所有标签关联记录
        QueryWrapper query = QueryWrapper.create()
                .in("evaluation_id", evaluationIds)
                .eq("is_delete", false);
        List<EvaluationTagRelation> relations = tagRelationMapper.selectListByQuery(query);

        // 使用Java进行标签统计
        Map<Long, Integer> tagCountMap = new HashMap<>();
        for (EvaluationTagRelation relation : relations) {
            tagCountMap.merge(relation.getTagId(), 1, Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : tagCountMap.entrySet()) {
            Long tagId = entry.getKey();
            Integer count = entry.getValue();

            EvaluationTag tag = tagMapper.selectOneById(tagId);
            if (tag == null) {
                continue;
            }

            TalentVO.TagStatVO statVO = new TalentVO.TagStatVO();
            statVO.setTagId(tagId);
            statVO.setTagName(tag.getName());
            statVO.setTagType(tag.getType());
            statVO.setCount(count);

            if (tag.getType() == 1) {
                result.get("positive").add(statVO);
            } else {
                result.get("neutral").add(statVO);
            }
        }

        // 按次数排序
        result.get("positive").sort((a, b) -> b.getCount().compareTo(a.getCount()));
        result.get("neutral").sort((a, b) -> b.getCount().compareTo(a.getCount()));

        return result;
    }

    /**
     * 构建评价VO列表（带解锁状态）
     */
    private List<TalentEvaluationVO> buildEvaluationVOList(List<Evaluation> evaluations,
            Long companyId, boolean isOwnEmployee) {
        List<TalentEvaluationVO> result = new ArrayList<>();
        List<Long> unlockedIds = isOwnEmployee ? Collections.emptyList()
                : getUnlockedEvaluationIds(companyId,
                        evaluations.isEmpty() ? null : evaluations.get(0).getEmployeeId());

        int freeCount = 0;
        for (Evaluation evaluation : evaluations) {
            TalentEvaluationVO vo = new TalentEvaluationVO();
            vo.setId(evaluation.getId());
            vo.setCompanyId(evaluation.getCompanyId());

            Company company = companyMapper.selectOneById(evaluation.getCompanyId());
            vo.setCompanyName(company != null ? company.getName() : null);

            vo.setEvaluationType(evaluation.getEvaluationType());
            EvaluationTypeEnum typeEnum = EvaluationTypeEnum.getEnumByValue(evaluation.getEvaluationType());
            vo.setEvaluationTypeName(typeEnum != null ? typeEnum.getText() : "未知");

            vo.setEvaluationPeriod(evaluation.getEvaluationPeriod());
            EvaluationPeriodEnum periodEnum = EvaluationPeriodEnum.getEnumByValue(evaluation.getEvaluationPeriod());
            vo.setEvaluationPeriodName(periodEnum != null ? periodEnum.getText() : "未知");

            vo.setEvaluationDate(evaluation.getEvaluationDate());
            vo.setPeriodYear(evaluation.getPeriodYear());
            vo.setPeriodQuarter(evaluation.getPeriodQuarter());

            // 判断是否解锁
            boolean unlocked;
            if (isOwnEmployee) {
                unlocked = true;
            } else if (freeCount < FREE_EVALUATION_COUNT) {
                unlocked = true;
                freeCount++;
            } else {
                unlocked = unlockedIds.contains(evaluation.getId());
            }

            vo.setUnlocked(unlocked);

            if (unlocked) {
                // 填充详细信息
                vo.setComment(evaluation.getComment());
                vo.setDimensionScores(getDimensionScoresForEvaluation(evaluation.getId()));
                vo.setAverageScore(calculateEvaluationAverageScore(evaluation.getId()));
                vo.setTags(getTagsForEvaluation(evaluation.getId()));
                vo.setEvaluatorInfo("评价人（隐私保护）");
            } else {
                // 未解锁时显示解锁价格
                vo.setUnlockCost(getUnlockPrice(evaluation.getEvaluationType()));
                vo.setComment(null);
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 获取评价的维度评分
     */
    private Map<String, Integer> getDimensionScoresForEvaluation(Long evaluationId) {
        Map<String, Integer> result = new LinkedHashMap<>();

        QueryWrapper query = QueryWrapper.create().eq("evaluation_id", evaluationId);
        List<EvaluationDimensionScore> scores = dimensionScoreMapper.selectListByQuery(query);

        for (EvaluationDimensionScore score : scores) {
            EvaluationDimension dimension = dimensionMapper.selectOneById(score.getDimensionId());
            if (dimension != null) {
                result.put(dimension.getName(), score.getScore());
            }
        }

        return result;
    }

    /**
     * 计算单个评价的平均分
     */
    private BigDecimal calculateEvaluationAverageScore(Long evaluationId) {
        QueryWrapper query = QueryWrapper.create()
                .eq("evaluation_id", evaluationId);
        List<EvaluationDimensionScore> scores = dimensionScoreMapper.selectListByQuery(query);

        if (CollUtil.isNotEmpty(scores)) {
            double avg = scores.stream()
                    .mapToInt(EvaluationDimensionScore::getScore)
                    .average()
                    .orElse(0.0);
            return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取评价的标签列表
     */
    private List<EvaluationTagVO> getTagsForEvaluation(Long evaluationId) {
        QueryWrapper query = QueryWrapper.create()
                .eq("evaluation_id", evaluationId)
                .eq("is_delete", false);
        List<EvaluationTagRelation> relations = tagRelationMapper.selectListByQuery(query);

        return relations.stream().map(relation -> {
            EvaluationTag tag = tagMapper.selectOneById(relation.getTagId());
            if (tag == null) {
                return null;
            }
            EvaluationTagVO vo = new EvaluationTagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setType(tag.getType());
            vo.setDescription(tag.getDescription());
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 判断是否为本公司员工（当前或曾经）
     */
    private boolean isOwnCompanyEmployee(Long employeeId, Long companyId) {
        if (companyId == null || employeeId == null) {
            return false;
        }

        // 只检查当前是否在该公司（不包括曾经在本公司工作过的员工）
        Employee employee = employeeMapper.selectOneById(employeeId);
        return employee != null && companyId.equals(employee.getCompanyId());
    }

    /**
     * 获取本公司所有员工ID集合（包括当前和曾经在本公司的员工）
     */
    private Set<Long> getOwnCompanyEmployeeIds(Long companyId) {
        Set<Long> employeeIds = new HashSet<>();

        if (companyId == null) {
            return employeeIds;
        }

        // 只获取当前在本公司的员工（不包括曾经在本公司工作过但现在已经离开的员工）
        QueryWrapper currentQuery = QueryWrapper.create()
                .select("id")
                .eq("company_id", companyId);
        List<Employee> currentEmployees = employeeMapper.selectListByQuery(currentQuery);
        if (CollUtil.isNotEmpty(currentEmployees)) {
            employeeIds.addAll(currentEmployees.stream().map(Employee::getId).collect(Collectors.toSet()));
        }

        return employeeIds;
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        return email.substring(0, 1) + "***" + email.substring(atIndex);
    }

    /**
     * 身份证号脱敏（显示前6位和后4位，中间用*代替）
     */
    private String maskIdCardNumber(String idCardNumber) {
        if (StrUtil.isBlank(idCardNumber) || idCardNumber.length() < 10) {
            return idCardNumber;
        }
        if (idCardNumber.length() == 18) {
            // 18位身份证：显示前6位和后4位
            return idCardNumber.substring(0, 6) + "********" + idCardNumber.substring(idCardNumber.length() - 4);
        } else if (idCardNumber.length() == 15) {
            // 15位身份证：显示前6位和后3位
            return idCardNumber.substring(0, 6) + "******" + idCardNumber.substring(idCardNumber.length() - 3);
        }
        return idCardNumber;
    }

    // ==================== 人才浏览记录功能实现 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordView(TalentViewLogRequest request, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");
        ThrowUtils.throwIf(request.getEmployeeId() == null, ErrorCode.PARAMS_ERROR, "员工ID不能为空");

        // 检查员工是否存在
        Employee employee = employeeMapper.selectOneById(request.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 确定公司ID：系统管理员使用被浏览员工所属的公司ID，其他角色使用登录用户所属的公司ID
        Long companyId = loginUser.getCompanyId();
        if (companyId == null && UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            // 系统管理员：使用被浏览员工所属的公司ID
            companyId = employee.getCompanyId();
            ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "被浏览员工未分配公司，无法记录浏览记录");
        } else {
            // 非系统管理员：必须有所属公司
            ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");
        }

        // 查找是否已存在该员工的浏览记录（同一公司同一员工只保留一条记录）
        // 首先查询包括已删除的记录（因为唯一索引不包含is_delete字段）
        TalentViewLog existingLog = viewLogMapper.selectOneIncludingDeleted(companyId, request.getEmployeeId());

        TalentViewLog viewLog;
        if (existingLog != null) {
            // 已存在记录（包括可能被逻辑删除的），恢复并更新浏览时间和相关信息
            existingLog.setViewTime(LocalDateTime.now());
            existingLog.setUserId(loginUser.getId());
            existingLog.setViewDuration(request.getViewDuration() != null ? request.getViewDuration() : 0);
            if (StrUtil.isNotBlank(request.getViewSource())) {
                existingLog.setViewSource(request.getViewSource());
            }
            if (StrUtil.isNotBlank(request.getSearchKeyword())) {
                existingLog.setSearchKeyword(request.getSearchKeyword());
            }
            // 增加浏览次数
            existingLog.setViewCount(existingLog.getViewCount() != null ? existingLog.getViewCount() + 1 : 1);
            existingLog.setIsDelete(false); // 恢复记录
            viewLogMapper.update(existingLog);
            viewLog = existingLog;
            log.info("更新人才浏览记录：companyId={}, userId={}, employeeId={}, viewCount={}",
                    companyId, loginUser.getId(), request.getEmployeeId(), viewLog.getViewCount());
        } else {
            // 不存在记录，创建新记录
            viewLog = TalentViewLog.builder()
                    .companyId(companyId)
                    .userId(loginUser.getId())
                    .employeeId(request.getEmployeeId())
                    .viewTime(LocalDateTime.now())
                    .viewDuration(request.getViewDuration() != null ? request.getViewDuration() : 0)
                    .viewSource(request.getViewSource())
                    .searchKeyword(request.getSearchKeyword())
                    .viewCount(1)
                    .isDelete(false)
                    .build();
            try {
                viewLogMapper.insert(viewLog);
                log.info("创建人才浏览记录：companyId={}, userId={}, employeeId={}",
                        companyId, loginUser.getId(), request.getEmployeeId());
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 如果因为唯一键冲突插入失败（可能是并发插入），则再次查询并更新
                log.warn("插入浏览记录时发生唯一键冲突（可能是并发插入），尝试更新现有记录：companyId={}, employeeId={}",
                        companyId, request.getEmployeeId());
                TalentViewLog conflictLog = viewLogMapper.selectOneIncludingDeleted(companyId, request.getEmployeeId());
                if (conflictLog != null) {
                    conflictLog.setViewTime(LocalDateTime.now());
                    conflictLog.setUserId(loginUser.getId());
                    conflictLog.setViewDuration(request.getViewDuration() != null ? request.getViewDuration() : 0);
                    if (StrUtil.isNotBlank(request.getViewSource())) {
                        conflictLog.setViewSource(request.getViewSource());
                    }
                    if (StrUtil.isNotBlank(request.getSearchKeyword())) {
                        conflictLog.setSearchKeyword(request.getSearchKeyword());
                    }
                    conflictLog.setViewCount(conflictLog.getViewCount() != null ? conflictLog.getViewCount() + 1 : 1);
                    conflictLog.setIsDelete(false);
                    viewLogMapper.update(conflictLog);
                    viewLog = conflictLog;
                    log.info("更新冲突的浏览记录：companyId={}, userId={}, employeeId={}, viewCount={}",
                            companyId, loginUser.getId(), request.getEmployeeId(), viewLog.getViewCount());
                } else {
                    // 理论上不应该到这里，如果到这里说明有异常情况
                    throw new RuntimeException("无法创建或恢复浏览记录", e);
                }
            }
        }

        return viewLog.getId();
    }

    @Override
    public boolean updateViewDuration(Long viewLogId, Integer viewDuration, User loginUser) {
        ThrowUtils.throwIf(viewLogId == null || viewDuration == null, ErrorCode.PARAMS_ERROR);

        TalentViewLog viewLog = viewLogMapper.selectOneById(viewLogId);
        if (viewLog == null || !viewLog.getUserId().equals(loginUser.getId())) {
            return false;
        }

        viewLog.setViewDuration(viewDuration);
        return viewLogMapper.update(viewLog) > 0;
    }

    @Override
    public Page<TalentViewLogVO> getViewHistory(long pageNum, long pageSize, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        QueryWrapper query = QueryWrapper.create()
                .eq("company_id", companyId)
                .orderBy("view_time", false);

        Page<TalentViewLog> logPage = viewLogMapper.paginate(Page.of(pageNum, pageSize), query);

        if (CollUtil.isEmpty(logPage.getRecords())) {
            return new Page<>(Collections.emptyList(), pageNum, pageSize, 0);
        }

        List<TalentViewLogVO> voList = logPage.getRecords().stream().map(this::convertToViewLogVO)
                .collect(Collectors.toList());

        return new Page<>(voList, pageNum, pageSize, logPage.getTotalRow());
    }

    @Override
    public ViewStatisticsVO getViewStatistics(User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        ViewStatisticsVO stats = new ViewStatisticsVO();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusDays(30);

        // 总浏览次数
        QueryWrapper totalQuery = QueryWrapper.create().eq("company_id", companyId);
        stats.setTotalViews(viewLogMapper.selectCountByQuery(totalQuery));

        // 今日浏览
        QueryWrapper todayQuery = QueryWrapper.create()
                .eq("company_id", companyId)
                .ge("view_time", todayStart);
        stats.setTodayViews(viewLogMapper.selectCountByQuery(todayQuery));

        // 本周浏览
        QueryWrapper weekQuery = QueryWrapper.create()
                .eq("company_id", companyId)
                .ge("view_time", weekStart);
        stats.setWeekViews(viewLogMapper.selectCountByQuery(weekQuery));

        // 本月浏览
        QueryWrapper monthQuery = QueryWrapper.create()
                .eq("company_id", companyId)
                .ge("view_time", monthStart);
        stats.setMonthViews(viewLogMapper.selectCountByQuery(monthQuery));

        // 浏览人才数（去重）
        QueryWrapper distinctQuery = QueryWrapper.create()
                .select("DISTINCT employee_id")
                .eq("company_id", companyId);
        List<TalentViewLog> distinctLogs = viewLogMapper.selectListByQuery(distinctQuery);
        stats.setUniqueTalentCount((long) distinctLogs.size());

        // 最常浏览的人才（Top 10）
        stats.setMostViewedTalents(getMostViewedTalents(companyId, 10));

        // 最近浏览的人才（Top 10）
        QueryWrapper recentQuery = QueryWrapper.create()
                .eq("company_id", companyId)
                .orderBy("view_time", false);
        Page<TalentViewLog> recentPage = viewLogMapper.paginate(Page.of(1, 10), recentQuery);
        stats.setRecentViews(
                recentPage.getRecords().stream().map(this::convertToViewLogVO).collect(Collectors.toList()));

        // 按来源统计
        stats.setViewSourceDistribution(getViewSourceDistribution(companyId));

        // 最常搜索的关键词（Top 10）
        stats.setTopKeywords(getTopKeywords(companyId, 10));

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int clearViewHistory(Long employeeId, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        QueryWrapper query = QueryWrapper.create().eq("company_id", companyId);
        if (employeeId != null) {
            query.eq("employee_id", employeeId);
        }

        return viewLogMapper.deleteByQuery(query);
    }

    // ==================== 企业招聘偏好功能实现 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @org.springframework.cache.annotation.CacheEvict(value = "companyPreference", key = "#loginUser.companyId")
    public Long savePreference(CompanyPreferenceRequest request, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        // 查找现有偏好
        QueryWrapper query = QueryWrapper.create().eq("company_id", companyId);
        CompanyPreference existing = preferenceMapper.selectOneByQuery(query);

        CompanyPreference preference;
        if (existing != null) {
            preference = existing;
        } else {
            preference = new CompanyPreference();
            preference.setCompanyId(companyId);
        }

        // 更新偏好设置
        if (request.getPreferredOccupations() != null) {
            preference.setPreferredOccupations(JSONUtil.toJsonStr(request.getPreferredOccupations()));
        }
        if (request.getPreferredTagIds() != null) {
            preference.setPreferredTagIds(JSONUtil.toJsonStr(request.getPreferredTagIds()));
        }
        if (request.getExcludedTagIds() != null) {
            preference.setExcludedTagIds(JSONUtil.toJsonStr(request.getExcludedTagIds()));
        }
        preference.setMinScore(request.getMinScore());
        preference.setExcludeMajorIncident(request.getExcludeMajorIncident());
        preference.setMinAttendanceRate(request.getMinAttendanceRate());
        preference.setRequirementDescription(request.getRequirementDescription());
        preference.setIsDelete(false);

        if (existing != null) {
            preferenceMapper.update(preference);
        } else {
            preferenceMapper.insert(preference);
        }

        return preference.getId();
    }

    @Override
    public CompanyPreferenceVO getPreference(User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");

        Long companyId = loginUser.getCompanyId();
        if (companyId == null) {
            return new CompanyPreferenceVO();
        }

        // 尝试从缓存获取
        boolean cacheError = false;
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache("companyPreference");
                if (cache != null) {
                    org.springframework.cache.Cache.ValueWrapper wrapper = cache.get(companyId);
                    if (wrapper != null) {
                        Object cached = wrapper.get();
                        if (cached instanceof CompanyPreferenceVO) {
                            return (CompanyPreferenceVO) cached;
                        } else if (cached != null) {
                            // 缓存数据格式错误，清除缓存
                            log.warn("缓存数据格式错误，清除缓存并重新查询: companyId={}, cached类型={}",
                                    companyId, cached.getClass().getName());
                            cacheError = true;
                            try {
                                cache.evict(companyId);
                            } catch (Exception evictEx) {
                                log.warn("清除缓存失败: companyId={}, error={}", companyId, evictEx.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            // 如果缓存数据格式不对（旧格式），清除缓存并重新查询
            log.warn("缓存数据格式错误，清除缓存并重新查询: companyId={}, error={}", companyId, e.getMessage());
            cacheError = true;
            try {
                if (cacheManager != null) {
                    org.springframework.cache.Cache cache = cacheManager.getCache("companyPreference");
                    if (cache != null) {
                        cache.evict(companyId);
                    }
                }
            } catch (Exception evictEx) {
                log.warn("清除缓存失败: companyId={}, error={}", companyId, evictEx.getMessage());
            }
        } catch (Exception e) {
            // 捕获所有其他异常，确保不会影响正常查询
            log.warn("从缓存获取数据时发生异常，将重新查询: companyId={}, error={}", companyId, e.getMessage());
            cacheError = true;
            try {
                if (cacheManager != null) {
                    org.springframework.cache.Cache cache = cacheManager.getCache("companyPreference");
                    if (cache != null) {
                        cache.evict(companyId);
                    }
                }
            } catch (Exception evictEx) {
                log.warn("清除缓存失败: companyId={}, error={}", companyId, evictEx.getMessage());
            }
        }

        // 如果缓存出错，确保清除缓存后再查询
        if (cacheError && cacheManager != null) {
            try {
                org.springframework.cache.Cache cache = cacheManager.getCache("companyPreference");
                if (cache != null) {
                    cache.evict(companyId);
                }
            } catch (Exception e) {
                log.warn("最终清除缓存失败: companyId={}, error={}", companyId, e.getMessage());
            }
        }

        // 从数据库查询
        QueryWrapper query = QueryWrapper.create().eq("company_id", companyId);
        CompanyPreference preference = preferenceMapper.selectOneByQuery(query);

        if (preference == null) {
            return new CompanyPreferenceVO();
        }

        CompanyPreferenceVO result = convertToPreferenceVO(preference);

        // 将结果存入缓存
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache("companyPreference");
                if (cache != null) {
                    cache.put(companyId, result);
                }
            }
        } catch (Exception e) {
            log.warn("缓存数据失败: companyId={}, error={}", companyId, e.getMessage());
        }

        return result;
    }

    // ==================== 人才推荐功能实现 ====================

    @Override
    public Page<TalentRecommendVO> getRecommendedTalents(long pageNum, long pageSize, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");

        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "企业ID不能为空");

        // 获取企业偏好
        CompanyPreference preference = getCompanyPreference(companyId);

        // 获取浏览历史中的职位倾向
        List<String> viewedOccupations = getViewedOccupations(companyId);

        // 构建推荐查询
        TalentSearchRequest searchRequest = buildRecommendSearchRequest(preference, viewedOccupations);
        searchRequest.setPageNum((int) pageNum);
        searchRequest.setPageSize((int) pageSize);

        // 搜索符合条件的人才（使用内部方法，不扣除积分，智能推荐是系统功能）
        Page<TalentVO> talentPage = searchTalentsInternal(searchRequest, loginUser);

        // 转换为推荐VO并计算推荐评分
        List<TalentRecommendVO> recommendList = talentPage.getRecords().stream()
                .map(talent -> convertToRecommendVO(talent, preference, viewedOccupations))
                .sorted((a, b) -> b.getRecommendScore().compareTo(a.getRecommendScore()))
                .collect(Collectors.toList());

        return new Page<>(recommendList, pageNum, pageSize, talentPage.getTotalRow());
    }

    @Override
    public List<TalentRecommendVO> getSimilarTalents(Long employeeId, int limit, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");
        ThrowUtils.throwIf(employeeId == null, ErrorCode.PARAMS_ERROR, "员工ID不能为空");

        Employee referenceEmployee = employeeMapper.selectOneById(employeeId);
        ThrowUtils.throwIf(referenceEmployee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 获取参考员工的职位和标签
        List<String> referenceOccupations = getEmployeeOccupations(employeeId);
        List<Long> referenceTagIds = getEmployeeTagIds(employeeId);

        // 构建搜索请求
        TalentSearchRequest searchRequest = new TalentSearchRequest();
        if (CollUtil.isNotEmpty(referenceOccupations)) {
            searchRequest.setOccupations(referenceOccupations);
        }
        if (CollUtil.isNotEmpty(referenceTagIds)) {
            searchRequest.setIncludeTagIds(referenceTagIds);
        }
        searchRequest.setPageNum(1);
        searchRequest.setPageSize(limit + 1); // 多查一个，排除自己

        // 使用内部搜索方法，不扣除积分（相似人才推荐是系统功能，不应消耗积分）
        Page<TalentVO> talentPage = searchTalentsInternal(searchRequest, loginUser);

        // 过滤掉自己，转换为推荐VO
        return talentPage.getRecords().stream()
                .filter(talent -> !talent.getId().equals(employeeId))
                .limit(limit)
                .map(talent -> {
                    TalentRecommendVO vo = new TalentRecommendVO();
                    copyTalentVOProperties(talent, vo);
                    vo.setRecommendScore(calculateSimilarityScore(talent, referenceOccupations, referenceTagIds));
                    vo.setRecommendReasons(generateSimilarityReasons(talent, referenceOccupations, referenceTagIds));
                    vo.setMatchedOccupations(
                            findMatchedOccupations(talent.getOccupationHistory(), referenceOccupations));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // ==================== 人才对比功能实现 ====================

    @Override
    public TalentCompareVO compareTalents(TalentCompareRequest request, User loginUser) {
        ThrowUtils.throwIf(!hasAccessPermission(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问人才市场");
        ThrowUtils.throwIf(request == null || CollUtil.isEmpty(request.getEmployeeIds()), ErrorCode.PARAMS_ERROR,
                "员工ID列表不能为空");
        ThrowUtils.throwIf(request.getEmployeeIds().size() < 2, ErrorCode.PARAMS_ERROR, "至少需要2个人才进行对比");
        ThrowUtils.throwIf(request.getEmployeeIds().size() > 5, ErrorCode.PARAMS_ERROR, "最多支持5个人才对比");

        Long companyId = loginUser.getCompanyId();
        List<Long> employeeIds = request.getEmployeeIds();

        TalentCompareVO compareVO = new TalentCompareVO();
        List<TalentCompareVO.CompareItemVO> items = new ArrayList<>();

        // 获取所有维度名称
        List<EvaluationDimension> dimensions = dimensionMapper.selectListByQuery(
                QueryWrapper.create().eq("is_active", true).orderBy("sort_order", true));
        List<String> dimensionNames = dimensions.stream().map(EvaluationDimension::getName)
                .collect(Collectors.toList());
        compareVO.setDimensionNames(dimensionNames);

        // 收集所有人的维度评分用于雷达图
        Map<String, List<BigDecimal>> dimensionRadarData = new LinkedHashMap<>();
        for (String dimName : dimensionNames) {
            dimensionRadarData.put(dimName, new ArrayList<>());
        }

        // 计算所有人的平均值，用于对比
        Map<Long, BigDecimal> avgScores = calculateEmployeeAverageScores(employeeIds);
        Map<Long, BigDecimal> avgAttendanceRates = calculateAverageAttendanceRates(employeeIds);

        BigDecimal overallAvgScore = avgScores.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(avgScores.size()), 2, RoundingMode.HALF_UP);

        for (Long employeeId : employeeIds) {
            Employee employee = employeeMapper.selectOneById(employeeId);
            if (employee == null) {
                continue;
            }

            TalentCompareVO.CompareItemVO item = buildCompareItem(employee, companyId, dimensions, dimensionRadarData,
                    overallAvgScore, avgScores, avgAttendanceRates);
            items.add(item);
        }

        // 计算每个人的优势和劣势
        calculateAdvantagesAndDisadvantages(items, dimensionNames);

        compareVO.setItems(items);
        compareVO.setDimensionRadarData(dimensionRadarData);

        // 深度智能分析需要扣除积分：10x，x为对比人数（测试阶段设为0）
        BigDecimal compareCost = BigDecimal.valueOf(0); // 测试阶段设为0，正式环境改为：10 * employeeIds.size()
        Long pointsRecordId = null; // 保存积分扣除记录ID，以便失败时返还

        // 检查积分是否足够（测试阶段跳过检查）
        if (compareCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentPoints = companyPointsService.getTotalPoints(companyId);
            ThrowUtils.throwIf(currentPoints.compareTo(compareCost) < 0,
                    ErrorCode.OPERATION_ERROR,
                    "积分不足，深度智能分析需要" + compareCost + "积分（10×" + employeeIds.size() + "），当前" + currentPoints + "积分");

            // 扣除积分
            try {
                pointsRecordId = companyPointsService.addPoints(
                        companyId,
                        compareCost.negate(),
                        PointsChangeReasonEnum.RIGHTS_CONSUMPTION.getValue(),
                        null,
                        "深度智能分析（" + employeeIds.size() + "人对比）");
                log.info("深度智能分析积分扣除成功，公司ID={}, 扣除积分={}, 记录ID={}", companyId, compareCost, pointsRecordId);
            } catch (Exception e) {
                log.error("扣除深度智能分析积分失败，公司ID={}, 错误：{}", companyId, e.getMessage(), e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "扣除积分失败：" + e.getMessage());
            }
        } else {
            log.info("深度智能分析测试模式，不扣除积分，公司ID={}, 对比人数={}", companyId, employeeIds.size());
        }

        // 先返回基础数据，AI分析异步执行
        // 如果AI服务可用，准备异步分析
        if (aiTalentComparisonService != null) {
            try {
                log.info("准备启动AI分析任务，公司ID={}, 员工数量={}", companyId, employeeIds.size());

                // 收集公司偏好信息
                String companyPreferenceInfo = formatCompanyPreferenceInfo(companyId);
                log.info("公司偏好信息格式化完成，长度={}字符", companyPreferenceInfo.length());

                // 收集所有人才的信息
                StringBuilder talentInfosBuilder = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TalentCompareVO.CompareItemVO item = items.get(i);
                    Long employeeId = item.getEmployeeId();
                    String talentInfo = formatTalentInfoForAI(employeeId, companyId, item);
                    talentInfosBuilder.append("=".repeat(80)).append("\n");
                    talentInfosBuilder.append("候选人 ").append(i + 1).append("：").append(item.getName()).append("\n");
                    talentInfosBuilder.append("=".repeat(80)).append("\n");
                    talentInfosBuilder.append(talentInfo).append("\n\n");
                    log.info("格式化候选人{}信息完成，姓名={}, 信息长度={}字符", i + 1, item.getName(), talentInfo.length());
                }
                String talentInfos = talentInfosBuilder.toString();
                log.info("所有候选人信息格式化完成，总长度={}字符", talentInfos.length());

                // 查找历史对比记录作为参考（最多5条）
                List<TalentCompareRecord> historyRecords = talentCompareRecordService
                        .findRelatedHistoryRecords(companyId, employeeIds);
                StringBuilder historyContext = new StringBuilder();
                if (CollUtil.isNotEmpty(historyRecords)) {
                    log.info("找到{}条相关历史对比记录，将作为AI分析参考", historyRecords.size());
                    historyContext.append("\n\n## 历史对比记录参考\n");
                    historyContext.append("以下是与当前对比候选人相关的历史对比记录，可作为参考，帮助保持推荐指数的稳定性：\n\n");
                    for (int i = 0; i < historyRecords.size(); i++) {
                        TalentCompareRecord historyRecord = historyRecords.get(i);
                        historyContext.append("### 历史记录 ").append(i + 1).append("\n");
                        if (StrUtil.isNotBlank(historyRecord.getAiAnalysisResult())) {
                            // 只提取历史记录的AI分析结果作为参考，不包含完整对比数据
                            historyContext.append(historyRecord.getAiAnalysisResult()).append("\n\n");
                        }
                    }
                    historyContext.append("请参考以上历史记录，在保持推荐指数稳定性的基础上，对当前候选人进行对比分析。\n");
                } else {
                    log.info("未找到相关历史对比记录");
                }

                // 准备AI输入数据
                String inputData = "公司招聘偏好设置：\n" + companyPreferenceInfo + "\n\n候选人信息：\n" + talentInfos
                        + historyContext.toString();
                log.debug("完整AI输入数据长度={}字符", inputData.length());

                // 提交AI分析任务（异步执行）
                String taskId = aiAnalysisTaskService.submitTask(companyId, employeeIds, inputData);
                compareVO.setAiAnalysisTaskId(taskId);
                log.info("AI分析任务已提交，任务ID={}，基础数据将立即返回", taskId);

                // 不等待AI分析完成，立即返回基础数据
                // AI分析结果将通过任务ID查询接口获取
            } catch (Exception e) {
                log.error("提交AI分析任务失败，公司ID={}, 员工数量={}, 错误信息：{}",
                        companyId, employeeIds.size(), e.getMessage(), e);

                // AI分析任务提交失败，返还积分
                if (pointsRecordId != null) {
                    try {
                        companyPointsService.addPoints(
                                companyId,
                                compareCost,
                                PointsChangeReasonEnum.RIGHTS_CONSUMPTION.getValue(),
                                null,
                                "深度智能分析失败，返还积分（" + employeeIds.size() + "人对比）");
                        log.info("AI分析任务提交失败，已返还积分，公司ID={}, 返还积分={}", companyId, compareCost);
                    } catch (Exception refundException) {
                        log.error("返还积分失败，公司ID={}, 错误：{}", companyId, refundException.getMessage(), refundException);
                    }
                }

                // AI分析任务提交失败不影响基础对比功能，继续返回基础对比结果
            }
        } else {
            log.warn("AI人才对比服务未配置，跳过AI分析，仅返回基础对比结果");

            // AI服务不可用，返还积分
            if (pointsRecordId != null) {
                try {
                    companyPointsService.addPoints(
                            companyId,
                            compareCost,
                            PointsChangeReasonEnum.RIGHTS_CONSUMPTION.getValue(),
                            null,
                            "深度智能分析服务不可用，返还积分（" + employeeIds.size() + "人对比）");
                    log.info("AI服务不可用，已返还积分，公司ID={}, 返还积分={}", companyId, compareCost);
                } catch (Exception refundException) {
                    log.error("返还积分失败，公司ID={}, 错误：{}", companyId, refundException.getMessage(), refundException);
                }
            }
        }

        // 保存对比记录到数据库（每次对比都创建新记录）
        try {
            String compareResultJson = JSONUtil.toJsonStr(compareVO);

            // 每次对比都创建新记录，不检查是否已存在
            // 排序员工ID列表，确保格式一致
            List<Long> sortedEmployeeIds = new ArrayList<>(employeeIds);
            Collections.sort(sortedEmployeeIds);
            String employeeIdsJson = JSONUtil.toJsonStr(sortedEmployeeIds);

            TalentCompareRecord newRecord = TalentCompareRecord.builder()
                    .companyId(companyId)
                    .employeeIds(employeeIdsJson)
                    .compareResult(compareResultJson)
                    .aiAnalysisResult(null) // AI分析结果稍后通过任务ID更新
                    .isDelete(false)
                    .build();

            boolean saved = talentCompareRecordService.save(newRecord);
            if (saved) {
                log.info("对比记录已保存到数据库，记录ID={}, 企业ID={}, 员工数量={}", newRecord.getId(), companyId, employeeIds.size());
            } else {
                log.error("保存对比记录失败，企业ID={}, 员工数量={}", companyId, employeeIds.size());
            }
        } catch (Exception e) {
            log.error("保存对比记录失败，企业ID={}, 员工数量={}, 错误：{}", companyId, employeeIds.size(), e.getMessage(), e);
            // 保存失败不影响对比功能，继续返回结果
        }

        return compareVO;
    }

    // ==================== 私有辅助方法（新增功能） ====================

    private TalentViewLogVO convertToViewLogVO(TalentViewLog log) {
        TalentViewLogVO vo = new TalentViewLogVO();
        vo.setId(log.getId());
        vo.setEmployeeId(log.getEmployeeId());
        vo.setViewTime(log.getViewTime());
        vo.setViewDuration(log.getViewDuration());
        vo.setViewSource(log.getViewSource());
        vo.setSearchKeyword(log.getSearchKeyword());
        vo.setUserId(log.getUserId());
        vo.setViewCount(log.getViewCount() != null ? log.getViewCount() : 1);

        Employee employee = employeeMapper.selectOneById(log.getEmployeeId());
        if (employee != null) {
            vo.setEmployeeName(employee.getName());
            vo.setEmployeePhotoUrl(employee.getPhotoUrl());
        }

        // 获取最新职位
        List<TalentDetailVO.ProfileSummaryVO> profiles = getProfileSummaries(log.getEmployeeId());
        if (CollUtil.isNotEmpty(profiles)) {
            vo.setLatestOccupation(profiles.get(0).getOccupation());
        }

        // 获取用户姓名
        User user = userService.getById(log.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }

        return vo;
    }

    private List<ViewStatisticsVO.TalentViewCountVO> getMostViewedTalents(Long companyId, int limit) {
        // 统计每个员工的浏览次数
        QueryWrapper query = QueryWrapper.create().eq("company_id", companyId);
        List<TalentViewLog> allLogs = viewLogMapper.selectListByQuery(query);

        Map<Long, Long> viewCounts = allLogs.stream()
                .collect(Collectors.groupingBy(TalentViewLog::getEmployeeId, Collectors.counting()));

        return viewCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> {
                    ViewStatisticsVO.TalentViewCountVO vo = new ViewStatisticsVO.TalentViewCountVO();
                    vo.setEmployeeId(entry.getKey());
                    vo.setViewCount(entry.getValue());

                    Employee employee = employeeMapper.selectOneById(entry.getKey());
                    if (employee != null) {
                        vo.setEmployeeName(employee.getName());
                        vo.setEmployeePhotoUrl(employee.getPhotoUrl());
                    }

                    List<TalentDetailVO.ProfileSummaryVO> profiles = getProfileSummaries(entry.getKey());
                    if (CollUtil.isNotEmpty(profiles)) {
                        vo.setLatestOccupation(profiles.get(0).getOccupation());
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Long> getViewSourceDistribution(Long companyId) {
        QueryWrapper query = QueryWrapper.create().eq("company_id", companyId);
        List<TalentViewLog> allLogs = viewLogMapper.selectListByQuery(query);

        return allLogs.stream()
                .filter(log -> log.getViewSource() != null)
                .collect(Collectors.groupingBy(TalentViewLog::getViewSource, Collectors.counting()));
    }

    private List<ViewStatisticsVO.KeywordStatVO> getTopKeywords(Long companyId, int limit) {
        QueryWrapper query = QueryWrapper.create()
                .eq("company_id", companyId)
                .isNotNull("search_keyword");
        List<TalentViewLog> logs = viewLogMapper.selectListByQuery(query);

        Map<String, Long> keywordCounts = logs.stream()
                .filter(log -> StrUtil.isNotBlank(log.getSearchKeyword()))
                .collect(Collectors.groupingBy(TalentViewLog::getSearchKeyword, Collectors.counting()));

        return keywordCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> {
                    ViewStatisticsVO.KeywordStatVO vo = new ViewStatisticsVO.KeywordStatVO();
                    vo.setKeyword(entry.getKey());
                    vo.setCount(entry.getValue());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private CompanyPreferenceVO convertToPreferenceVO(CompanyPreference preference) {
        CompanyPreferenceVO vo = new CompanyPreferenceVO();

        if (StrUtil.isNotBlank(preference.getPreferredOccupations())) {
            vo.setPreferredOccupations(JSONUtil.toList(preference.getPreferredOccupations(), String.class));
        }

        if (StrUtil.isNotBlank(preference.getPreferredTagIds())) {
            List<Long> tagIds = JSONUtil.toList(preference.getPreferredTagIds(), Long.class);
            vo.setPreferredTags(tagIds.stream()
                    .map(tagMapper::selectOneById)
                    .filter(Objects::nonNull)
                    .map(this::convertTagToVO)
                    .collect(Collectors.toList()));
        }

        if (StrUtil.isNotBlank(preference.getExcludedTagIds())) {
            List<Long> tagIds = JSONUtil.toList(preference.getExcludedTagIds(), Long.class);
            vo.setExcludedTags(tagIds.stream()
                    .map(tagMapper::selectOneById)
                    .filter(Objects::nonNull)
                    .map(this::convertTagToVO)
                    .collect(Collectors.toList()));
        }

        vo.setMinScore(preference.getMinScore());
        vo.setExcludeMajorIncident(preference.getExcludeMajorIncident());
        vo.setMinAttendanceRate(preference.getMinAttendanceRate());
        vo.setRequirementDescription(preference.getRequirementDescription());

        return vo;
    }

    private EvaluationTagVO convertTagToVO(EvaluationTag tag) {
        EvaluationTagVO vo = new EvaluationTagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setType(tag.getType());
        vo.setDescription(tag.getDescription());
        return vo;
    }

    private CompanyPreference getCompanyPreference(Long companyId) {
        QueryWrapper query = QueryWrapper.create().eq("company_id", companyId);
        return preferenceMapper.selectOneByQuery(query);
    }

    private List<String> getViewedOccupations(Long companyId) {
        // 获取最近浏览的人才的职位
        QueryWrapper query = QueryWrapper.create()
                .eq("company_id", companyId)
                .orderBy("view_time", false);
        Page<TalentViewLog> logs = viewLogMapper.paginate(Page.of(1, 50), query);

        Set<String> occupations = new LinkedHashSet<>();
        for (TalentViewLog log : logs.getRecords()) {
            List<TalentDetailVO.ProfileSummaryVO> profiles = getProfileSummaries(log.getEmployeeId());
            for (TalentDetailVO.ProfileSummaryVO profile : profiles) {
                if (StrUtil.isNotBlank(profile.getOccupation())) {
                    occupations.add(profile.getOccupation());
                }
            }
        }
        return new ArrayList<>(occupations);
    }

    private TalentSearchRequest buildRecommendSearchRequest(CompanyPreference preference,
            List<String> viewedOccupations) {
        TalentSearchRequest request = new TalentSearchRequest();

        // 合并偏好职位和浏览历史职位
        Set<String> allOccupations = new LinkedHashSet<>();
        if (preference != null && StrUtil.isNotBlank(preference.getPreferredOccupations())) {
            allOccupations.addAll(JSONUtil.toList(preference.getPreferredOccupations(), String.class));
        }
        if (CollUtil.isNotEmpty(viewedOccupations)) {
            allOccupations.addAll(viewedOccupations.subList(0, Math.min(10, viewedOccupations.size())));
        }
        if (CollUtil.isNotEmpty(allOccupations)) {
            request.setOccupations(new ArrayList<>(allOccupations));
        }

        if (preference != null) {
            // 偏好标签
            if (StrUtil.isNotBlank(preference.getPreferredTagIds())) {
                request.setIncludeTagIds(JSONUtil.toList(preference.getPreferredTagIds(), Long.class));
            }
            // 排除标签
            if (StrUtil.isNotBlank(preference.getExcludedTagIds())) {
                request.setExcludeTagIds(JSONUtil.toList(preference.getExcludedTagIds(), Long.class));
            }
            // 最低评分（如果设置了才应用，避免过滤掉所有人才）
            if (preference.getMinScore() != null && preference.getMinScore().compareTo(BigDecimal.ZERO) > 0) {
                request.setMinAverageScore(preference.getMinScore());
            }
            // 排除重大违纪（如果设置了才应用）
            if (preference.getExcludeMajorIncident() != null) {
                request.setExcludeMajorIncident(preference.getExcludeMajorIncident());
            }
            // 最低出勤率（如果设置了才应用）
            if (preference.getMinAttendanceRate() != null
                    && preference.getMinAttendanceRate().compareTo(BigDecimal.ZERO) > 0) {
                request.setMinAttendanceRate(preference.getMinAttendanceRate());
            }
        }

        // 智能推荐默认排除本公司员工
        request.setExcludeOwnCompany(true);

        // 如果没有设置任何筛选条件，默认按评分降序排序
        request.setSortField("averageScore");
        request.setSortOrder("desc");

        return request;
    }

    private TalentRecommendVO convertToRecommendVO(TalentVO talent, CompanyPreference preference,
            List<String> viewedOccupations) {
        TalentRecommendVO vo = new TalentRecommendVO();
        copyTalentVOProperties(talent, vo);

        // 计算推荐评分
        vo.setRecommendScore(calculateRecommendScore(talent, preference, viewedOccupations));

        // 计算匹配度
        vo.setMatchPercentage(calculateMatchPercentage(talent, preference, viewedOccupations));

        // 生成推荐理由
        vo.setRecommendReasons(generateRecommendReasons(talent, preference, viewedOccupations));

        // 匹配的职位
        Set<String> preferredOccupations = new HashSet<>();
        if (preference != null && StrUtil.isNotBlank(preference.getPreferredOccupations())) {
            preferredOccupations.addAll(JSONUtil.toList(preference.getPreferredOccupations(), String.class));
        }
        vo.setMatchedOccupations(
                findMatchedOccupations(talent.getOccupationHistory(), new ArrayList<>(preferredOccupations)));

        // 匹配的标签
        if (preference != null && StrUtil.isNotBlank(preference.getPreferredTagIds())) {
            List<Long> preferredTagIds = JSONUtil.toList(preference.getPreferredTagIds(), Long.class);
            List<EvaluationTagVO> matchedTags = new ArrayList<>();
            if (talent.getPositiveTags() != null) {
                for (TalentVO.TagStatVO tagStat : talent.getPositiveTags()) {
                    if (preferredTagIds.contains(tagStat.getTagId())) {
                        EvaluationTag tag = tagMapper.selectOneById(tagStat.getTagId());
                        if (tag != null) {
                            matchedTags.add(convertTagToVO(tag));
                        }
                    }
                }
            }
            vo.setMatchedTags(matchedTags);
        }

        return vo;
    }

    private void copyTalentVOProperties(TalentVO source, TalentVO target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setGender(source.getGender());
        target.setPhotoUrl(source.getPhotoUrl());
        target.setStatus(source.getStatus());
        target.setCurrentCompanyName(source.getCurrentCompanyName());
        target.setLatestOccupation(source.getLatestOccupation());
        target.setOccupationHistory(source.getOccupationHistory());
        target.setAverageScore(source.getAverageScore());
        target.setEvaluationCount(source.getEvaluationCount());
        target.setPositiveTags(source.getPositiveTags());
        target.setNeutralTags(source.getNeutralTags());
        target.setProfileCount(source.getProfileCount());
        target.setBookmarked(source.getBookmarked());
        target.setIsOwnEmployee(source.getIsOwnEmployee());
    }

    private BigDecimal calculateRecommendScore(TalentVO talent, CompanyPreference preference,
            List<String> viewedOccupations) {
        BigDecimal score = BigDecimal.ZERO;

        // 评分权重 (最高40分)
        if (talent.getAverageScore() != null) {
            score = score.add(talent.getAverageScore().multiply(BigDecimal.valueOf(8)));
        }

        // 职位匹配权重 (最高30分)
        int occupationMatches = 0;
        Set<String> preferredOccupations = new HashSet<>();
        if (preference != null && StrUtil.isNotBlank(preference.getPreferredOccupations())) {
            preferredOccupations.addAll(JSONUtil.toList(preference.getPreferredOccupations(), String.class));
        }
        preferredOccupations.addAll(viewedOccupations);

        if (talent.getOccupationHistory() != null) {
            for (String occ : talent.getOccupationHistory()) {
                for (String preferred : preferredOccupations) {
                    if (occ.contains(preferred) || preferred.contains(occ)) {
                        occupationMatches++;
                        break;
                    }
                }
            }
        }
        score = score.add(BigDecimal.valueOf(Math.min(occupationMatches * 10, 30)));

        // 标签匹配权重 (最高20分)
        if (preference != null && StrUtil.isNotBlank(preference.getPreferredTagIds())
                && talent.getPositiveTags() != null) {
            List<Long> preferredTagIds = JSONUtil.toList(preference.getPreferredTagIds(), Long.class);
            int tagMatches = 0;
            for (TalentVO.TagStatVO tagStat : talent.getPositiveTags()) {
                if (preferredTagIds.contains(tagStat.getTagId())) {
                    tagMatches++;
                }
            }
            score = score.add(BigDecimal.valueOf(Math.min(tagMatches * 5, 20)));
        }

        // 评价数量权重 (最高10分)
        if (talent.getEvaluationCount() != null) {
            score = score.add(BigDecimal.valueOf(Math.min(talent.getEvaluationCount(), 10)));
        }

        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private int calculateMatchPercentage(TalentVO talent, CompanyPreference preference,
            List<String> viewedOccupations) {
        if (preference == null) {
            return 50; // 无偏好时默认50%
        }

        int totalCriteria = 0;
        int matchedCriteria = 0;

        // 职位匹配
        if (StrUtil.isNotBlank(preference.getPreferredOccupations())) {
            totalCriteria++;
            List<String> preferredOccupations = JSONUtil.toList(preference.getPreferredOccupations(), String.class);
            if (talent.getOccupationHistory() != null) {
                for (String occ : talent.getOccupationHistory()) {
                    for (String preferred : preferredOccupations) {
                        if (occ.contains(preferred) || preferred.contains(occ)) {
                            matchedCriteria++;
                            break;
                        }
                    }
                    if (matchedCriteria > 0)
                        break;
                }
            }
        }

        // 评分匹配
        if (preference.getMinScore() != null) {
            totalCriteria++;
            if (talent.getAverageScore() != null && talent.getAverageScore().compareTo(preference.getMinScore()) >= 0) {
                matchedCriteria++;
            }
        }

        // 标签匹配
        if (StrUtil.isNotBlank(preference.getPreferredTagIds()) && talent.getPositiveTags() != null) {
            totalCriteria++;
            List<Long> preferredTagIds = JSONUtil.toList(preference.getPreferredTagIds(), Long.class);
            for (TalentVO.TagStatVO tagStat : talent.getPositiveTags()) {
                if (preferredTagIds.contains(tagStat.getTagId())) {
                    matchedCriteria++;
                    break;
                }
            }
        }

        if (totalCriteria == 0) {
            return 50;
        }

        return (int) ((matchedCriteria * 100.0) / totalCriteria);
    }

    private List<String> generateRecommendReasons(TalentVO talent, CompanyPreference preference,
            List<String> viewedOccupations) {
        List<String> reasons = new ArrayList<>();

        // 评分高
        if (talent.getAverageScore() != null && talent.getAverageScore().compareTo(BigDecimal.valueOf(4)) >= 0) {
            reasons.add("综合评分优秀（" + talent.getAverageScore() + "分）");
        }

        // 职位匹配
        Set<String> preferredOccupations = new HashSet<>();
        if (preference != null && StrUtil.isNotBlank(preference.getPreferredOccupations())) {
            preferredOccupations.addAll(JSONUtil.toList(preference.getPreferredOccupations(), String.class));
        }
        if (talent.getOccupationHistory() != null) {
            for (String occ : talent.getOccupationHistory()) {
                for (String preferred : preferredOccupations) {
                    if (occ.contains(preferred) || preferred.contains(occ)) {
                        reasons.add("符合职位偏好：" + occ);
                        break;
                    }
                }
            }
        }

        // 标签匹配
        if (preference != null && StrUtil.isNotBlank(preference.getPreferredTagIds())
                && talent.getPositiveTags() != null) {
            List<Long> preferredTagIds = JSONUtil.toList(preference.getPreferredTagIds(), Long.class);
            for (TalentVO.TagStatVO tagStat : talent.getPositiveTags()) {
                if (preferredTagIds.contains(tagStat.getTagId())) {
                    reasons.add("具备偏好标签：" + tagStat.getTagName());
                }
            }
        }

        // 评价数量
        if (talent.getEvaluationCount() != null && talent.getEvaluationCount() >= 5) {
            reasons.add("评价丰富（" + talent.getEvaluationCount() + "条评价）");
        }

        // 工作经历
        if (talent.getProfileCount() != null && talent.getProfileCount() >= 2) {
            reasons.add("工作经验丰富（" + talent.getProfileCount() + "段工作经历）");
        }

        if (reasons.isEmpty()) {
            reasons.add("综合条件符合");
        }

        return reasons.subList(0, Math.min(5, reasons.size()));
    }

    private List<String> getEmployeeOccupations(Long employeeId) {
        List<TalentDetailVO.ProfileSummaryVO> profiles = getProfileSummaries(employeeId);
        return profiles.stream()
                .map(TalentDetailVO.ProfileSummaryVO::getOccupation)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> getEmployeeTagIds(Long employeeId) {
        List<Evaluation> evaluations = getEmployeeEvaluations(employeeId);
        if (CollUtil.isEmpty(evaluations)) {
            return Collections.emptyList();
        }

        List<Long> evaluationIds = evaluations.stream().map(Evaluation::getId).collect(Collectors.toList());

        QueryWrapper query = QueryWrapper.create()
                .select("DISTINCT tag_id")
                .in("evaluation_id", evaluationIds)
                .eq("is_delete", false);
        List<EvaluationTagRelation> relations = tagRelationMapper.selectListByQuery(query);

        return relations.stream().map(EvaluationTagRelation::getTagId).collect(Collectors.toList());
    }

    private BigDecimal calculateSimilarityScore(TalentVO talent, List<String> referenceOccupations,
            List<Long> referenceTagIds) {
        BigDecimal score = BigDecimal.ZERO;

        // 职位相似度
        if (talent.getOccupationHistory() != null && CollUtil.isNotEmpty(referenceOccupations)) {
            int matches = 0;
            for (String occ : talent.getOccupationHistory()) {
                for (String refOcc : referenceOccupations) {
                    if (occ.contains(refOcc) || refOcc.contains(occ)) {
                        matches++;
                        break;
                    }
                }
            }
            score = score.add(BigDecimal.valueOf(matches * 20));
        }

        // 标签相似度
        if (talent.getPositiveTags() != null && CollUtil.isNotEmpty(referenceTagIds)) {
            for (TalentVO.TagStatVO tagStat : talent.getPositiveTags()) {
                if (referenceTagIds.contains(tagStat.getTagId())) {
                    score = score.add(BigDecimal.valueOf(10));
                }
            }
        }

        // 评分相似度
        score = score.add(talent.getAverageScore().multiply(BigDecimal.valueOf(5)));

        return score.min(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private List<String> generateSimilarityReasons(TalentVO talent, List<String> referenceOccupations,
            List<Long> referenceTagIds) {
        List<String> reasons = new ArrayList<>();

        // 职位相似
        if (talent.getOccupationHistory() != null) {
            for (String occ : talent.getOccupationHistory()) {
                for (String refOcc : referenceOccupations) {
                    if (occ.contains(refOcc) || refOcc.contains(occ)) {
                        reasons.add("相似职位：" + occ);
                        break;
                    }
                }
            }
        }

        // 标签相似
        if (talent.getPositiveTags() != null) {
            for (TalentVO.TagStatVO tagStat : talent.getPositiveTags()) {
                if (referenceTagIds.contains(tagStat.getTagId())) {
                    reasons.add("相同标签：" + tagStat.getTagName());
                }
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("综合特征相似");
        }

        return reasons.subList(0, Math.min(3, reasons.size()));
    }

    private List<String> findMatchedOccupations(List<String> occupationHistory, List<String> preferredOccupations) {
        if (CollUtil.isEmpty(occupationHistory) || CollUtil.isEmpty(preferredOccupations)) {
            return Collections.emptyList();
        }

        List<String> matched = new ArrayList<>();
        for (String occ : occupationHistory) {
            for (String preferred : preferredOccupations) {
                if (occ.contains(preferred) || preferred.contains(occ)) {
                    matched.add(occ);
                    break;
                }
            }
        }
        return matched;
    }

    private Map<Long, BigDecimal> calculateAverageAttendanceRates(List<Long> employeeIds) {
        Map<Long, BigDecimal> result = new HashMap<>();
        for (Long employeeId : employeeIds) {
            QueryWrapper query = QueryWrapper.create()
                    .eq("employee_id", employeeId)
                    .isNotNull("attendance_rate");
            List<EmployeeProfile> profiles = employeeProfileMapper.selectListByQuery(query);

            if (CollUtil.isNotEmpty(profiles)) {
                double avg = profiles.stream()
                        .mapToDouble(p -> p.getAttendanceRate().doubleValue())
                        .average()
                        .orElse(0.0);
                result.put(employeeId, BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            } else {
                result.put(employeeId, BigDecimal.ZERO);
            }
        }
        return result;
    }

    private TalentCompareVO.CompareItemVO buildCompareItem(Employee employee, Long companyId,
            List<EvaluationDimension> dimensions,
            Map<String, List<BigDecimal>> dimensionRadarData,
            BigDecimal overallAvgScore,
            Map<Long, BigDecimal> avgScores,
            Map<Long, BigDecimal> avgAttendanceRates) {
        TalentCompareVO.CompareItemVO item = new TalentCompareVO.CompareItemVO();

        item.setEmployeeId(employee.getId());
        item.setName(employee.getName());
        item.setGender(employee.getGender());
        item.setPhotoUrl(employee.getPhotoUrl());
        item.setStatus(employee.getStatus());

        if (employee.getCompanyId() != null) {
            Company company = companyMapper.selectOneById(employee.getCompanyId());
            if (company != null) {
                item.setCurrentCompanyName(company.getName());
            }
        }

        // 工作经历
        List<TalentDetailVO.ProfileSummaryVO> profiles = getProfileSummaries(employee.getId());
        item.setProfileCount(profiles.size());

        // 工作年限（简单计算）
        int workYears = calculateWorkYears(profiles);
        item.setWorkYears(workYears);

        // 职位历史
        Set<String> occupations = new LinkedHashSet<>();
        for (TalentDetailVO.ProfileSummaryVO profile : profiles) {
            if (StrUtil.isNotBlank(profile.getOccupation())) {
                occupations.add(profile.getOccupation());
            }
        }
        item.setOccupationHistory(new ArrayList<>(occupations));
        item.setLatestOccupation(CollUtil.isNotEmpty(profiles) ? profiles.get(0).getOccupation() : null);

        // 评分
        item.setAverageScore(avgScores.getOrDefault(employee.getId(), BigDecimal.ZERO));

        // 各维度评分
        Map<String, BigDecimal> dimScores = calculateDimensionAverages(employee.getId());
        item.setDimensionScores(dimScores);

        // 添加到雷达图数据
        for (EvaluationDimension dim : dimensions) {
            BigDecimal score = dimScores.getOrDefault(dim.getName(), BigDecimal.ZERO);
            dimensionRadarData.get(dim.getName()).add(score);
        }

        // 评价数量
        List<Evaluation> evaluations = getEmployeeEvaluations(employee.getId());
        item.setEvaluationCount(evaluations.size());

        // 平均出勤率
        item.setAvgAttendanceRate(avgAttendanceRates.getOrDefault(employee.getId(), BigDecimal.ZERO));

        // 是否有重大违纪
        item.setHasMajorIncident(hasAnyMajorIncident(employee.getId()));

        // 标签统计
        Map<String, List<TalentVO.TagStatVO>> tagStats = getTagStatistics(employee.getId());
        List<TalentVO.TagStatVO> positiveTags = tagStats.get("positive");
        List<TalentVO.TagStatVO> neutralTags = tagStats.get("neutral");
        item.setTopPositiveTags(positiveTags.subList(0, Math.min(5, positiveTags.size())));
        item.setTopNeutralTags(neutralTags.subList(0, Math.min(3, neutralTags.size())));

        return item;
    }

    private int calculateWorkYears(List<TalentDetailVO.ProfileSummaryVO> profiles) {
        if (CollUtil.isEmpty(profiles)) {
            return 0;
        }

        // 简单计算：累加所有工作经历的年数
        int totalMonths = 0;
        for (TalentDetailVO.ProfileSummaryVO profile : profiles) {
            if (profile.getStartDate() != null) {
                java.time.LocalDate start = java.time.LocalDate.parse(profile.getStartDate());
                java.time.LocalDate end = profile.getEndDate() != null
                        ? java.time.LocalDate.parse(profile.getEndDate())
                        : java.time.LocalDate.now();
                totalMonths += java.time.Period.between(start, end).toTotalMonths();
            }
        }
        return totalMonths / 12;
    }

    private boolean hasAnyMajorIncident(Long employeeId) {
        QueryWrapper query = QueryWrapper.create()
                .eq("employee_id", employeeId)
                .eq("has_major_incident", true);
        return employeeProfileMapper.selectCountByQuery(query) > 0;
    }

    private void calculateAdvantagesAndDisadvantages(List<TalentCompareVO.CompareItemVO> items,
            List<String> dimensionNames) {
        if (items.size() < 2) {
            return;
        }

        // 计算每个维度的平均值
        Map<String, BigDecimal> dimAvg = new HashMap<>();
        for (String dimName : dimensionNames) {
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            for (TalentCompareVO.CompareItemVO item : items) {
                BigDecimal score = item.getDimensionScores().get(dimName);
                if (score != null && score.compareTo(BigDecimal.ZERO) > 0) {
                    sum = sum.add(score);
                    count++;
                }
            }
            if (count > 0) {
                dimAvg.put(dimName, sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            }
        }

        // 计算每个人的优势和劣势
        for (TalentCompareVO.CompareItemVO item : items) {
            List<String> advantages = new ArrayList<>();
            List<String> disadvantages = new ArrayList<>();

            for (String dimName : dimensionNames) {
                BigDecimal score = item.getDimensionScores().get(dimName);
                BigDecimal avg = dimAvg.get(dimName);
                if (score != null && avg != null) {
                    if (score.compareTo(avg) > 0) {
                        advantages.add(dimName + "表现突出");
                    } else if (score.compareTo(avg) < 0) {
                        disadvantages.add(dimName + "有待提升");
                    }
                }
            }

            // 其他维度对比
            BigDecimal avgScore = items.stream()
                    .map(TalentCompareVO.CompareItemVO::getAverageScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);

            if (item.getAverageScore().compareTo(avgScore) > 0) {
                advantages.add("综合评分高于平均");
            }

            if (item.getProfileCount() != null && item.getProfileCount() >= 3) {
                advantages.add("工作经验丰富");
            }

            if (Boolean.TRUE.equals(item.getHasMajorIncident())) {
                disadvantages.add("存在重大违纪记录");
            }

            item.setAdvantages(advantages.subList(0, Math.min(3, advantages.size())));
            item.setDisadvantages(disadvantages.subList(0, Math.min(3, disadvantages.size())));
        }
    }

    // ==================== AI人才对比相关方法 ====================

    /**
     * 格式化公司招聘偏好信息为文本
     */
    private String formatCompanyPreferenceInfo(Long companyId) {
        log.info("开始格式化公司偏好信息，公司ID={}", companyId);
        CompanyPreference preference = getCompanyPreference(companyId);
        if (preference == null) {
            log.info("公司未设置招聘偏好，使用默认提示");
            return "公司未设置招聘偏好，请根据通用标准进行评估。";
        }
        log.info("找到公司偏好设置，开始格式化");

        StringBuilder sb = new StringBuilder();
        sb.append("## 公司招聘偏好设置\n\n");

        // 偏好职位
        if (StrUtil.isNotBlank(preference.getPreferredOccupations())) {
            List<String> occupations = JSONUtil.toList(preference.getPreferredOccupations(), String.class);
            if (CollUtil.isNotEmpty(occupations)) {
                sb.append("偏好职位：").append(String.join("、", occupations)).append("\n");
            }
        }

        // 偏好标签
        if (StrUtil.isNotBlank(preference.getPreferredTagIds())) {
            List<Long> tagIds = JSONUtil.toList(preference.getPreferredTagIds(), Long.class);
            if (CollUtil.isNotEmpty(tagIds)) {
                List<String> tagNames = new ArrayList<>();
                for (Long tagId : tagIds) {
                    EvaluationTag tag = tagMapper.selectOneById(tagId);
                    if (tag != null) {
                        tagNames.add(tag.getName());
                    }
                }
                if (CollUtil.isNotEmpty(tagNames)) {
                    sb.append("偏好标签：").append(String.join("、", tagNames)).append("\n");
                }
            }
        }

        // 排除标签
        if (StrUtil.isNotBlank(preference.getExcludedTagIds())) {
            List<Long> tagIds = JSONUtil.toList(preference.getExcludedTagIds(), Long.class);
            if (CollUtil.isNotEmpty(tagIds)) {
                List<String> tagNames = new ArrayList<>();
                for (Long tagId : tagIds) {
                    EvaluationTag tag = tagMapper.selectOneById(tagId);
                    if (tag != null) {
                        tagNames.add(tag.getName());
                    }
                }
                if (CollUtil.isNotEmpty(tagNames)) {
                    sb.append("排除标签：").append(String.join("、", tagNames)).append("\n");
                }
            }
        }

        // 最低评分要求
        if (preference.getMinScore() != null) {
            sb.append("最低评分要求：").append(preference.getMinScore()).append("分\n");
        }

        // 是否排除重大违纪
        if (Boolean.TRUE.equals(preference.getExcludeMajorIncident())) {
            sb.append("排除有重大违纪记录的人员\n");
        }

        // 最低出勤率要求
        if (preference.getMinAttendanceRate() != null) {
            sb.append("最低出勤率要求：").append(preference.getMinAttendanceRate()).append("%\n");
        }

        // 具体要求描述
        if (StrUtil.isNotBlank(preference.getRequirementDescription())) {
            sb.append("\n具体要求描述：\n").append(preference.getRequirementDescription()).append("\n");
        }

        sb.append("\n");
        String result = sb.toString();
        log.info("公司偏好信息格式化完成，长度={}字符", result.length());
        return result;
    }

    /**
     * 格式化单个人才的信息为文本（供AI分析使用）
     */
    private String formatTalentInfoForAI(Long employeeId, Long companyId, TalentCompareVO.CompareItemVO item) {
        log.info("开始格式化人才信息，员工ID={}, 姓名={}, 查看公司ID={}", employeeId, item.getName(), companyId);
        StringBuilder sb = new StringBuilder();

        // 基本信息
        sb.append("## 基本信息\n");
        sb.append("姓名：").append(item.getName()).append("\n");
        sb.append("性别：").append(item.getGender() != null ? item.getGender() : "未知").append("\n");
        sb.append("当前状态：").append(Boolean.TRUE.equals(item.getStatus()) ? "在职" : "离职").append("\n");
        if (StrUtil.isNotBlank(item.getCurrentCompanyName())) {
            sb.append("当前公司：").append(item.getCurrentCompanyName()).append("\n");
        }
        if (item.getWorkYears() != null) {
            sb.append("工作年限：").append(item.getWorkYears()).append("年\n");
        }
        if (item.getAverageScore() != null) {
            sb.append("综合评分：").append(item.getAverageScore()).append("分\n");
        }
        sb.append("\n");

        // 工作经历
        sb.append("## 工作经历\n");
        // 获取员工信息以获取当前公司ID
        Employee employee = employeeMapper.selectOneById(employeeId);
        Long targetEmployeeCompanyId = employee != null ? employee.getCompanyId() : null;
        log.debug("员工{}当前公司ID={}, 查看公司ID={}", employeeId, targetEmployeeCompanyId, companyId);

        List<TalentDetailVO.ProfileSummaryVO> profiles = getProfileSummaries(employeeId, companyId,
                targetEmployeeCompanyId);
        log.debug("员工{}共有{}条工作经历", employeeId, profiles != null ? profiles.size() : 0);

        if (CollUtil.isNotEmpty(profiles)) {
            for (TalentDetailVO.ProfileSummaryVO profile : profiles) {
                sb.append("### 工作经历 ").append(profiles.indexOf(profile) + 1).append("\n");
                sb.append("公司：").append(profile.getCompanyName() != null ? profile.getCompanyName() : "未知")
                        .append("\n");

                // 检查档案的可见性和授权状态
                Integer visibility = profile.getVisibility() != null ? profile.getVisibility() : 2;
                boolean canViewDetail = Boolean.TRUE.equals(profile.getCanViewDetail());

                // 如果visibility=0或visibility=1且未授权，只提供入职和离职时间
                if (visibility == 0 || (visibility == 1 && !canViewDetail)) {
                    sb.append("入职日期：").append(profile.getStartDate() != null ? profile.getStartDate() : "未知")
                            .append("\n");
                    sb.append("离职日期：").append(profile.getEndDate() != null ? profile.getEndDate() : "在职").append("\n");
                    sb.append("（该档案信息保密，不可查看详细信息）\n");
                } else {
                    // 可以查看完整信息
                    sb.append("职位：").append(profile.getOccupation() != null && !"**".equals(profile.getOccupation())
                            ? profile.getOccupation()
                            : "未知").append("\n");
                    sb.append("入职日期：").append(profile.getStartDate() != null ? profile.getStartDate() : "未知")
                            .append("\n");
                    sb.append("离职日期：").append(profile.getEndDate() != null ? profile.getEndDate() : "在职").append("\n");

                    if (profile.getAttendanceRate() != null) {
                        sb.append("出勤率：").append(profile.getAttendanceRate()).append("%\n");
                    }
                    if (profile.getPerformanceSummary() != null && !"**".equals(profile.getPerformanceSummary())) {
                        sb.append("绩效摘要：").append(profile.getPerformanceSummary()).append("\n");
                    }
                    if (Boolean.TRUE.equals(profile.getHasMajorIncident())) {
                        sb.append("重大违纪：是\n");
                    }
                    if (StrUtil.isNotBlank(profile.getReasonForLeaving())
                            && !"**".equals(profile.getReasonForLeaving())) {
                        sb.append("离职原因：").append(profile.getReasonForLeaving()).append("\n");
                    }
                }
                sb.append("\n");
            }
        } else {
            sb.append("无工作经历记录\n\n");
        }

        // 评价信息
        sb.append("## 评价信息\n");
        List<Evaluation> allEvaluations = getEmployeeEvaluations(employeeId);
        log.debug("员工{}共有{}条评价", employeeId, allEvaluations != null ? allEvaluations.size() : 0);

        List<Long> unlockedEvaluationIds = getUnlockedEvaluationIds(companyId, employeeId);
        log.debug("公司{}已解锁员工{}的{}条评价", companyId, employeeId, unlockedEvaluationIds.size());

        List<Evaluation> visibleEvaluations = new ArrayList<>();

        // 获取可查看的评价（前3条免费 + 已解锁的），但排除自评（evaluation_type=4）
        if (CollUtil.isNotEmpty(allEvaluations)) {
            int freeCount = 0;
            for (Evaluation evaluation : allEvaluations) {
                // 排除自评（evaluation_type=4）
                if (evaluation.getEvaluationType() != null && evaluation.getEvaluationType() == 4) {
                    continue;
                }

                // 前3条免费可见，或者已解锁的可见
                if (freeCount < 3 || unlockedEvaluationIds.contains(evaluation.getId())) {
                    visibleEvaluations.add(evaluation);
                    if (freeCount < 3) {
                        freeCount++;
                    }
                }
            }
            log.debug("员工{}可查看的评价数量：{}条（前3条免费 + {}条已解锁，已排除自评）",
                    employeeId, visibleEvaluations.size(), unlockedEvaluationIds.size());
        }

        if (CollUtil.isNotEmpty(visibleEvaluations)) {
            // 计算排除自评后的总评价数
            long totalNonSelfEvaluations = allEvaluations.stream()
                    .filter(e -> e.getEvaluationType() == null || e.getEvaluationType() != 4)
                    .count();

            sb.append("评价总数：").append(totalNonSelfEvaluations).append("条（已排除自评），可见：").append(visibleEvaluations.size())
                    .append("条\n\n");

            // 按evaluation_type分组，每个类型最多30条，按日期降序（日期越新的优先级越高）
            // 注意：自评已经被过滤，不会出现在这里
            Map<Integer, List<Evaluation>> evaluationsByType = visibleEvaluations.stream()
                    .collect(Collectors.groupingBy(Evaluation::getEvaluationType));

            // 对每个类型的评价按日期降序排序，并限制最多30条
            for (Map.Entry<Integer, List<Evaluation>> entry : evaluationsByType.entrySet()) {
                Integer evaluationType = entry.getKey();
                List<Evaluation> typeEvaluations = entry.getValue();

                // 按日期降序排序（日期越新的优先级越高）
                typeEvaluations.sort((a, b) -> {
                    if (a.getEvaluationDate() == null && b.getEvaluationDate() == null) {
                        return 0;
                    }
                    if (a.getEvaluationDate() == null) {
                        return 1;
                    }
                    if (b.getEvaluationDate() == null) {
                        return -1;
                    }
                    return b.getEvaluationDate().compareTo(a.getEvaluationDate());
                });

                // 每个类型最多30条
                List<Evaluation> limitedEvaluations = typeEvaluations.stream()
                        .limit(30)
                        .collect(Collectors.toList());

                EvaluationTypeEnum typeEnum = EvaluationTypeEnum.getEnumByValue(evaluationType);
                sb.append("### ").append(typeEnum != null ? typeEnum.getText() : "评价类型" + evaluationType)
                        .append("（共").append(typeEvaluations.size()).append("条，显示最新").append(limitedEvaluations.size())
                        .append("条）\n");

                for (Evaluation evaluation : limitedEvaluations) {
                    sb.append("#### 评价日期：").append(evaluation.getEvaluationDate()).append("\n");
                    if (StrUtil.isNotBlank(evaluation.getComment())) {
                        sb.append("评价内容：").append(evaluation.getComment()).append("\n");
                    }

                    // 维度评分
                    QueryWrapper dimQuery = QueryWrapper.create()
                            .eq("evaluation_id", evaluation.getId());
                    List<EvaluationDimensionScore> dimScores = dimensionScoreMapper.selectListByQuery(dimQuery);
                    if (CollUtil.isNotEmpty(dimScores)) {
                        sb.append("维度评分：");
                        List<String> dimScoreStrs = new ArrayList<>();
                        for (EvaluationDimensionScore dimScore : dimScores) {
                            EvaluationDimension dim = dimensionMapper.selectOneById(dimScore.getDimensionId());
                            if (dim != null) {
                                dimScoreStrs.add(dim.getName() + "：" + dimScore.getScore() + "分");
                            }
                        }
                        sb.append(String.join("，", dimScoreStrs)).append("\n");
                    }

                    // 标签
                    QueryWrapper tagQuery = QueryWrapper.create()
                            .eq("evaluation_id", evaluation.getId())
                            .eq("is_delete", false);
                    List<EvaluationTagRelation> tagRelations = tagRelationMapper.selectListByQuery(tagQuery);
                    if (CollUtil.isNotEmpty(tagRelations)) {
                        List<String> tagNames = new ArrayList<>();
                        for (EvaluationTagRelation relation : tagRelations) {
                            EvaluationTag tag = tagMapper.selectOneById(relation.getTagId());
                            if (tag != null) {
                                tagNames.add(tag.getName());
                            }
                        }
                        if (CollUtil.isNotEmpty(tagNames)) {
                            sb.append("标签：").append(String.join("、", tagNames)).append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
        } else {
            sb.append("暂无评价信息\n\n");
        }

        // 标签统计
        sb.append("## 标签统计\n");
        Map<String, List<TalentVO.TagStatVO>> tagStats = getTagStatistics(employeeId);
        List<TalentVO.TagStatVO> positiveTags = tagStats.get("positive");
        List<TalentVO.TagStatVO> neutralTags = tagStats.get("neutral");

        if (CollUtil.isNotEmpty(positiveTags)) {
            sb.append("正面标签：");
            List<String> positiveTagNames = positiveTags.stream()
                    .map(TalentVO.TagStatVO::getTagName)
                    .collect(Collectors.toList());
            sb.append(String.join("、", positiveTagNames)).append("\n");
        }

        if (CollUtil.isNotEmpty(neutralTags)) {
            sb.append("中性标签：");
            List<String> neutralTagNames = neutralTags.stream()
                    .map(TalentVO.TagStatVO::getTagName)
                    .collect(Collectors.toList());
            sb.append(String.join("、", neutralTagNames)).append("\n");
        }

        // 各维度评分
        if (item.getDimensionScores() != null && !item.getDimensionScores().isEmpty()) {
            sb.append("\n## 各维度评分\n");
            for (Map.Entry<String, BigDecimal> entry : item.getDimensionScores().entrySet()) {
                sb.append(entry.getKey()).append("：").append(entry.getValue()).append("分\n");
            }
        }

        // 其他信息
        sb.append("\n## 其他信息\n");
        if (item.getEvaluationCount() != null) {
            sb.append("评价数量：").append(item.getEvaluationCount()).append("条\n");
        }
        if (item.getAvgAttendanceRate() != null && item.getAvgAttendanceRate().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("平均出勤率：").append(item.getAvgAttendanceRate()).append("%\n");
        }
        if (Boolean.TRUE.equals(item.getHasMajorIncident())) {
            sb.append("重大违纪：是\n");
        }
        if (CollUtil.isNotEmpty(item.getOccupationHistory())) {
            sb.append("职位历史：").append(String.join("、", item.getOccupationHistory())).append("\n");
        }

        String result = sb.toString();
        log.info("人才信息格式化完成，员工ID={}, 姓名={}, 信息长度={}字符",
                employeeId, item.getName(), result.length());
        return result;
    }

    @Override
    public void updateCompareRecordAiResult(String taskId, String aiResult, User loginUser) {
        if (taskId == null || StrUtil.isBlank(aiResult) || loginUser == null || loginUser.getCompanyId() == null) {
            return;
        }

        try {
            Long companyId = loginUser.getCompanyId();

            // 查询该企业最近创建的对比记录（AI分析结果为空或未完成的）
            // 由于任务ID格式为 ai_task_{companyId}_{timestamp}，我们可以通过时间戳匹配
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("company_id", companyId)
                    .eq("is_delete", false)
                    .isNull("ai_analysis_result")
                    .orderBy("create_time", false)
                    .limit(20); // 查询最近20条，提高匹配成功率

            List<TalentCompareRecord> recentRecords = talentCompareRecordService.list(queryWrapper);

            if (CollUtil.isEmpty(recentRecords)) {
                log.warn("未找到需要更新AI分析结果的对比记录，任务ID={}, 企业ID={}", taskId, companyId);
                return;
            }

            // 尝试通过任务ID中的时间戳匹配（任务ID格式：ai_task_{companyId}_{timestamp}）
            TalentCompareRecord matchedRecord = null;
            if (taskId.contains("_")) {
                String[] parts = taskId.split("_");
                if (parts.length >= 3) {
                    try {
                        long taskTimestamp = Long.parseLong(parts[parts.length - 1]);
                        // 查找创建时间最接近任务时间戳的记录（误差在10分钟内）
                        long minTimeDiff = Long.MAX_VALUE;
                        for (TalentCompareRecord record : recentRecords) {
                            if (record.getCreateTime() != null) {
                                long recordTimestamp = record.getCreateTime()
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli();
                                long timeDiff = Math.abs(recordTimestamp - taskTimestamp);
                                // 如果时间差在10分钟内，且是最接近的，认为是同一条记录
                                if (timeDiff < 10 * 60 * 1000 && timeDiff < minTimeDiff) {
                                    minTimeDiff = timeDiff;
                                    matchedRecord = record;
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析任务ID时间戳失败，任务ID={}", taskId);
                    }
                }
            }

            // 如果无法通过时间戳匹配，更新最近一条未完成的记录
            if (matchedRecord == null && CollUtil.isNotEmpty(recentRecords)) {
                matchedRecord = recentRecords.get(0);
                log.info("通过时间戳无法匹配，使用最近一条记录，记录ID={}, 任务ID={}", matchedRecord.getId(), taskId);
            }

            if (matchedRecord != null) {
                // 检查AI分析结果是否有效（如果为空或包含错误信息，视为失败）
                boolean isAiAnalysisFailed = StrUtil.isBlank(aiResult) ||
                        aiResult.toLowerCase().contains("error") ||
                        aiResult.toLowerCase().contains("失败") ||
                        aiResult.toLowerCase().contains("exception");

                if (isAiAnalysisFailed) {
                    log.warn("AI分析失败，任务ID={}, 记录ID={}, 结果：{}", taskId, matchedRecord.getId(), aiResult);

                    // 返还积分：深度智能分析费用为 10x（x为对比人数）
                    // 从employeeIds JSON中解析人数
                    int employeeCount = 2; // 默认值
                    try {
                        if (StrUtil.isNotBlank(matchedRecord.getEmployeeIds())) {
                            List<Long> employeeIdsList = JSONUtil.toList(matchedRecord.getEmployeeIds(), Long.class);
                            if (CollUtil.isNotEmpty(employeeIdsList)) {
                                employeeCount = employeeIdsList.size();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析员工ID列表失败，使用默认值，记录ID={}", matchedRecord.getId());
                    }

                    BigDecimal refundAmount = BigDecimal.valueOf(10 * employeeCount);
                    try {
                        companyPointsService.addPoints(
                                companyId,
                                refundAmount,
                                PointsChangeReasonEnum.RIGHTS_CONSUMPTION.getValue(),
                                null,
                                "深度智能分析失败，返还积分（" + employeeCount + "人对比）");
                        log.info("AI分析失败，已返还积分，公司ID={}, 返还积分={}, 记录ID={}", companyId, refundAmount,
                                matchedRecord.getId());
                    } catch (Exception refundException) {
                        log.error("返还积分失败，公司ID={}, 记录ID={}, 错误：{}", companyId, matchedRecord.getId(),
                                refundException.getMessage(), refundException);
                    }
                } else {
                    // AI分析成功，更新结果
                    talentCompareRecordService.updateAiAnalysisResult(matchedRecord.getId(), aiResult);
                    log.info("更新对比记录AI分析结果成功，记录ID={}, 任务ID={}", matchedRecord.getId(), taskId);
                }
            } else {
                log.warn("未找到匹配的对比记录，任务ID={}, 企业ID={}", taskId, companyId);
            }
        } catch (Exception e) {
            log.error("更新对比记录AI分析结果失败，任务ID={}, 错误：{}", taskId, e.getMessage(), e);
        }
    }

    @Override
    public Page<TalentCompareRecordVO> getCompareHistory(long pageNum, long pageSize, User loginUser) {
        ThrowUtils.throwIf(loginUser == null || loginUser.getCompanyId() == null,
                ErrorCode.NO_AUTH_ERROR, "用户信息不完整");

        Long companyId = loginUser.getCompanyId();
        List<TalentCompareRecord> records = talentCompareRecordService.getLatestRecords(companyId,
                (int) (pageNum * pageSize));

        // 转换为VO
        List<TalentCompareRecordVO> voList = new ArrayList<>();
        for (TalentCompareRecord record : records) {
            TalentCompareRecordVO vo = convertToCompareRecordVO(record);
            if (vo != null) {
                voList.add(vo);
            }
        }

        // 手动分页
        int start = (int) ((pageNum - 1) * pageSize);
        int end = (int) Math.min(start + pageSize, voList.size());
        List<TalentCompareRecordVO> pageList = start < voList.size()
                ? voList.subList(start, end)
                : new ArrayList<>();

        Page<TalentCompareRecordVO> page = new Page<>(pageNum, pageSize, voList.size());
        page.setRecords(pageList);
        return page;
    }

    @Override
    public TalentCompareRecordVO getCompareHistoryById(Long recordId, User loginUser) {
        ThrowUtils.throwIf(recordId == null, ErrorCode.PARAMS_ERROR, "记录ID不能为空");
        ThrowUtils.throwIf(loginUser == null || loginUser.getCompanyId() == null,
                ErrorCode.NO_AUTH_ERROR, "用户信息不完整");

        TalentCompareRecord record = talentCompareRecordService.getById(recordId);
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "历史记录不存在");

        // 验证记录是否属于当前企业
        ThrowUtils.throwIf(!record.getCompanyId().equals(loginUser.getCompanyId()),
                ErrorCode.NO_AUTH_ERROR, "无权访问该历史记录");

        return convertToCompareRecordVO(record);
    }

    @Override
    public TalentCompareRecordVO checkExistingCompare(TalentCompareRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null || CollUtil.isEmpty(request.getEmployeeIds()),
                ErrorCode.PARAMS_ERROR, "员工ID列表不能为空");
        ThrowUtils.throwIf(loginUser == null || loginUser.getCompanyId() == null,
                ErrorCode.NO_AUTH_ERROR, "用户信息不完整");

        Long companyId = loginUser.getCompanyId();
        TalentCompareRecord existingRecord = talentCompareRecordService.findExistingRecord(
                companyId, request.getEmployeeIds());

        if (existingRecord != null) {
            return convertToCompareRecordVO(existingRecord);
        }
        return null;
    }

    /**
     * 转换对比记录实体为VO
     */
    private TalentCompareRecordVO convertToCompareRecordVO(TalentCompareRecord record) {
        if (record == null) {
            return null;
        }

        TalentCompareRecordVO vo = new TalentCompareRecordVO();
        vo.setId(record.getId());
        vo.setCompanyId(record.getCompanyId());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        vo.setAiAnalysisResult(record.getAiAnalysisResult());

        // 解析员工ID列表
        try {
            List<Long> employeeIds = JSONUtil.toList(record.getEmployeeIds(), Long.class);
            vo.setEmployeeIds(employeeIds);

            // 获取员工姓名列表
            List<String> employeeNames = new ArrayList<>();
            for (Long employeeId : employeeIds) {
                Employee employee = employeeMapper.selectOneById(employeeId);
                if (employee != null) {
                    employeeNames.add(employee.getName());
                } else {
                    employeeNames.add("未知");
                }
            }
            vo.setEmployeeNames(employeeNames);

            // 解析对比结果
            if (StrUtil.isNotBlank(record.getCompareResult())) {
                try {
                    TalentCompareVO compareVO = JSONUtil.toBean(record.getCompareResult(), TalentCompareVO.class);
                    vo.setCompareResult(compareVO);
                } catch (Exception e) {
                    log.warn("解析对比结果失败，记录ID={}, 错误：{}", record.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("解析员工ID列表失败，记录ID={}, 错误：{}", record.getId(), e.getMessage());
        }

        return vo;
    }
}
