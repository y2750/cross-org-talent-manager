package com.crossorgtalentmanager.service.impl;

import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.*;
import com.crossorgtalentmanager.model.dto.evaluation.*;
import com.crossorgtalentmanager.model.entity.*;
import com.crossorgtalentmanager.model.enums.EvaluationPeriodEnum;
import com.crossorgtalentmanager.model.enums.EvaluationTypeEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.EvaluationDetailVO;
import com.crossorgtalentmanager.model.vo.EvaluationDimensionScoreVO;
import com.crossorgtalentmanager.model.vo.EvaluationStatisticsVO;
import com.crossorgtalentmanager.model.vo.EvaluationTagStatisticsVO;
import com.crossorgtalentmanager.model.vo.EvaluationVO;
import com.crossorgtalentmanager.service.CompanyPointsService;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.DepartmentService;
import com.crossorgtalentmanager.service.EmployeeProfileService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.service.EvaluationService;
import com.crossorgtalentmanager.service.EvaluationTaskService;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.model.enums.PointsChangeReasonEnum;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价服务实现类
 */
@Slf4j
@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Resource
    private EvaluationMapper evaluationMapper;

    @Resource
    private EvaluationDimensionScoreMapper dimensionScoreMapper;

    @Resource
    private EvaluationTagRelationMapper tagRelationMapper;

    @Resource
    private EvaluationDimensionMapper dimensionMapper;

    @Resource
    private EvaluationTagMapper tagMapper;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private UserService userService;

    @Resource
    private EvaluationTaskService evaluationTaskService;

    @Resource
    private CompanyPointsService companyPointsService;

    @Resource
    private EmployeeProfileService employeeProfileService;

    @Resource
    private CompanyService companyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addEvaluation(EvaluationAddRequest addRequest, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(addRequest.getEmployeeId() == null, ErrorCode.PARAMS_ERROR, "被评价员工ID不能为空");
        ThrowUtils.throwIf(addRequest.getEvaluationType() == null, ErrorCode.PARAMS_ERROR, "评价类型不能为空");

        // 2. 权限验证
        // 对于离职评价，如果存在对应的评价任务，就允许评价（因为任务创建时已经验证过权限）
        boolean hasPermission = false;
        if (addRequest.getEvaluationPeriod() != null
                && EvaluationPeriodEnum.RESIGNATION.getValue().equals(addRequest.getEvaluationPeriod())) {
            // 离职评价：检查是否存在对应的评价任务
            long taskCount = evaluationTaskService.count(
                    QueryWrapper.create()
                            .eq("employee_id", addRequest.getEmployeeId())
                            .eq("evaluator_id", loginUser.getId())
                            .eq("evaluation_type", addRequest.getEvaluationType())
                            .eq("evaluation_period", EvaluationPeriodEnum.RESIGNATION.getValue())
                            .eq("status", 0) // 待评价状态
            );
            if (taskCount > 0) {
                // 存在对应的评价任务，允许评价（任务创建时已经验证过权限）
                hasPermission = true;
                log.info("离职评价权限验证通过：存在对应的评价任务，employeeId={}, evaluatorId={}",
                        addRequest.getEmployeeId(), loginUser.getId());
            } else {
                // 不存在任务，使用原来的权限验证逻辑
                hasPermission = validateEvaluationPermission(
                        addRequest.getEmployeeId(),
                        loginUser.getId(),
                        addRequest.getEvaluationType());
            }
        } else {
            // 非离职评价，使用原来的权限验证逻辑
            hasPermission = validateEvaluationPermission(
                    addRequest.getEmployeeId(),
                    loginUser.getId(),
                    addRequest.getEvaluationType());
        }
        ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR, "无评价权限");

        // 3. 获取被评价员工的companyId
        Employee employee = employeeService.getById(addRequest.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "被评价员工不存在");
        Long companyId = employee.getCompanyId(); // 可能为null（已离职员工）

        // 如果员工companyId为null（离职员工），尝试从员工的档案中获取公司ID
        if (companyId == null) {
            List<com.crossorgtalentmanager.model.entity.EmployeeProfile> profiles = employeeProfileService.list(
                    QueryWrapper.create()
                            .eq("employee_id", addRequest.getEmployeeId())
                            .orderBy("create_time", false)
                            .limit(1));
            if (profiles != null && !profiles.isEmpty()) {
                companyId = profiles.get(0).getCompanyId();
                log.info("从员工档案获取公司ID：employeeId={}, companyId={}", addRequest.getEmployeeId(), companyId);
            }
        }

        // 如果还是null，尝试从评价人的公司ID获取（对于hr/部门主管评价自己公司的员工）
        if (companyId == null && loginUser.getCompanyId() != null) {
            companyId = loginUser.getCompanyId();
            log.info("从评价人公司获取公司ID：evaluatorId={}, companyId={}", loginUser.getId(), companyId);
        }

        // 对于HR评价(3)或领导评价(1)，确保companyId不为null（使用评价人的companyId）
        Integer evaluationTypeForCompanyId = addRequest.getEvaluationType();
        if ((EvaluationTypeEnum.HR.getValue().equals(evaluationTypeForCompanyId)
                || EvaluationTypeEnum.LEADER.getValue().equals(evaluationTypeForCompanyId))
                && companyId == null && loginUser.getCompanyId() != null) {
            companyId = loginUser.getCompanyId();
            log.info("HR/领导评价时，使用评价人公司ID：evaluatorId={}, companyId={}", loginUser.getId(), companyId);
        }

        // 4. 创建评价记录
        Evaluation evaluation = Evaluation.builder()
                .employeeId(addRequest.getEmployeeId())
                .companyId(companyId)
                .evaluatorId(loginUser.getId())
                .comment(addRequest.getComment())
                .evaluationDate(addRequest.getEvaluationDate() != null
                        ? addRequest.getEvaluationDate()
                        : LocalDate.now())
                .evaluationType(addRequest.getEvaluationType())
                .evaluationPeriod(addRequest.getEvaluationPeriod())
                .periodYear(addRequest.getPeriodYear())
                .periodQuarter(addRequest.getPeriodQuarter())
                .isDelete(false)
                .build();

        evaluationMapper.insert(evaluation);

        // 3.5. 完成任务（如果存在对应的任务）
        evaluationTaskService.completeTask(
                evaluation.getId(),
                loginUser.getId(),
                addRequest.getEmployeeId(),
                addRequest.getEvaluationPeriod(),
                addRequest.getPeriodYear(),
                addRequest.getPeriodQuarter());

        // 4. 保存维度评分
        if (addRequest.getDimensionScores() != null && !addRequest.getDimensionScores().isEmpty()) {
            for (EvaluationAddRequest.DimensionScoreRequest ds : addRequest.getDimensionScores()) {
                ThrowUtils.throwIf(ds.getScore() == null || ds.getScore() < 1 || ds.getScore() > 5,
                        ErrorCode.PARAMS_ERROR, "维度评分必须在1-5分之间");
                EvaluationDimensionScore dimensionScore = EvaluationDimensionScore.builder()
                        .evaluationId(evaluation.getId())
                        .dimensionId(ds.getDimensionId())
                        .score(ds.getScore())
                        .build();
                dimensionScoreMapper.insert(dimensionScore);
            }
        }

        // 5. 保存标签关联
        if (addRequest.getTagIds() != null && !addRequest.getTagIds().isEmpty()) {
            for (Long tagId : addRequest.getTagIds()) {
                EvaluationTagRelation tagRelation = EvaluationTagRelation.builder()
                        .evaluationId(evaluation.getId())
                        .tagId(tagId)
                        .build();
                tagRelationMapper.insert(tagRelation);
            }
        }

        // 6. 如果评价类型为HR评价(3)或领导评价(1)，则增加企业积分+5分
        Integer evaluationType = addRequest.getEvaluationType();
        if (companyId != null && (EvaluationTypeEnum.HR.getValue().equals(evaluationType)
                || EvaluationTypeEnum.LEADER.getValue().equals(evaluationType))) {
            try {
                companyPointsService.addPoints(
                        companyId,
                        BigDecimal.valueOf(5),
                        PointsChangeReasonEnum.EMPLOYEE_EVALUATION.getValue(),
                        addRequest.getEmployeeId(),
                        null); // changeDescription will be auto-generated
                log.info("HR/领导评价增加积分：companyId={}, employeeId={}, evaluationType={}, points=5",
                        companyId, addRequest.getEmployeeId(), evaluationType);
            } catch (Exception e) {
                log.error("添加积分失败：companyId={}, employeeId={}, evaluationType={}",
                        companyId, addRequest.getEmployeeId(), evaluationType, e);
                // 积分添加失败不影响评价创建，只记录日志
            }
        }

        return evaluation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addColleagueEvaluation(EvaluationColleagueRequest colleagueRequest, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(colleagueRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(colleagueRequest.getEmployeeId() == null, ErrorCode.PARAMS_ERROR, "被评价员工ID不能为空");

        // 2. 验证是否为同事评价（离职时）
        Employee employee = employeeService.getById(colleagueRequest.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 验证是否为同部门
        List<Employee> evaluatorEmployees = employeeService.list(
                QueryWrapper.create().eq("user_id", loginUser.getId()));
        ThrowUtils.throwIf(evaluatorEmployees == null || evaluatorEmployees.isEmpty(),
                ErrorCode.NOT_FOUND_ERROR, "评价人信息不存在");
        Employee evaluatorEmployee = evaluatorEmployees.get(0);
        ThrowUtils.throwIf(evaluatorEmployee.getDepartmentId() == null || employee.getDepartmentId() == null,
                ErrorCode.NO_AUTH_ERROR, "员工或评价人没有部门信息");
        ThrowUtils.throwIf(!evaluatorEmployee.getDepartmentId().equals(employee.getDepartmentId()),
                ErrorCode.NO_AUTH_ERROR, "只能评价同部门同事");

        // 验证是否已经评价过（离职评价只能评价一次）
        long existingCount = this.count(
                QueryWrapper.create()
                        .eq("employee_id", colleagueRequest.getEmployeeId())
                        .eq("evaluator_id", loginUser.getId())
                        .eq("evaluation_type", EvaluationTypeEnum.COLLEAGUE.getValue())
                        .eq("evaluation_period", EvaluationPeriodEnum.RESIGNATION.getValue()));
        ThrowUtils.throwIf(existingCount > 0, ErrorCode.OPERATION_ERROR, "已经评价过该同事");

        // 3. 创建同事评价
        EvaluationAddRequest addRequest = new EvaluationAddRequest();
        addRequest.setEmployeeId(colleagueRequest.getEmployeeId());
        addRequest.setComment(colleagueRequest.getComment());
        addRequest.setEvaluationType(EvaluationTypeEnum.COLLEAGUE.getValue());
        addRequest.setEvaluationPeriod(EvaluationPeriodEnum.RESIGNATION.getValue());
        addRequest.setPeriodYear(LocalDate.now().getYear());
        // 转换DimensionScoreRequest类型
        if (colleagueRequest.getDimensionScores() != null) {
            List<EvaluationAddRequest.DimensionScoreRequest> dimensionScores = new ArrayList<>();
            for (EvaluationColleagueRequest.DimensionScoreRequest ds : colleagueRequest.getDimensionScores()) {
                EvaluationAddRequest.DimensionScoreRequest addDs = new EvaluationAddRequest.DimensionScoreRequest();
                addDs.setDimensionId(ds.getDimensionId());
                addDs.setScore(ds.getScore());
                dimensionScores.add(addDs);
            }
            addRequest.setDimensionScores(dimensionScores);
        }
        addRequest.setTagIds(colleagueRequest.getTagIds());

        return addEvaluation(addRequest, loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createResignationEvaluationTasks(Long employeeId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(employeeId == null, ErrorCode.PARAMS_ERROR);

        // 2. 权限校验（HR权限）
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "无权限创建离职评价任务");

        // 3. 获取离职员工信息
        Employee employee = employeeService.getById(employeeId);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 4. 创建离职评价任务
        // 4.1 创建HR评价任务
        EvaluationTaskCreateRequest hrTaskRequest = new EvaluationTaskCreateRequest();
        hrTaskRequest.setEmployeeId(employeeId);
        hrTaskRequest.setEvaluationType(EvaluationTypeEnum.HR.getValue());
        hrTaskRequest.setEvaluationPeriod(EvaluationPeriodEnum.RESIGNATION.getValue());
        hrTaskRequest.setPeriodYear(LocalDate.now().getYear());
        hrTaskRequest.setDeadline(LocalDateTime.now().plusDays(7)); // 离职评价7天内完成
        evaluationTaskService.createEvaluationTasks(hrTaskRequest, loginUser);

        // 4.2 创建部门主管评价任务（如果有部门）
        if (employee.getDepartmentId() != null) {
            List<Department> departments = departmentService.list(
                    QueryWrapper.create().eq("id", employee.getDepartmentId()));
            if (departments != null && !departments.isEmpty()) {
                Department department = departments.get(0);
                if (department.getLeaderId() != null) {
                    Employee leader = employeeService.getById(department.getLeaderId());
                    if (leader != null && leader.getUserId() != null) {
                        EvaluationTaskCreateRequest leaderTaskRequest = new EvaluationTaskCreateRequest();
                        leaderTaskRequest.setEmployeeId(employeeId);
                        leaderTaskRequest.setEvaluationType(EvaluationTypeEnum.LEADER.getValue());
                        leaderTaskRequest.setEvaluationPeriod(EvaluationPeriodEnum.RESIGNATION.getValue());
                        leaderTaskRequest.setPeriodYear(LocalDate.now().getYear());
                        leaderTaskRequest.setDeadline(java.time.LocalDateTime.now().plusDays(7));
                        evaluationTaskService.createEvaluationTasks(leaderTaskRequest, loginUser);
                    }
                }
            }
        }

        // 4.3 创建同事评价任务
        if (employee.getDepartmentId() != null) {
            // 创建同事评价任务（所有同事评价离职员工）
            EvaluationTaskCreateRequest colleagueTaskRequest = new EvaluationTaskCreateRequest();
            colleagueTaskRequest.setEmployeeId(employeeId);
            colleagueTaskRequest.setEvaluationType(EvaluationTypeEnum.COLLEAGUE.getValue());
            colleagueTaskRequest.setEvaluationPeriod(EvaluationPeriodEnum.RESIGNATION.getValue());
            colleagueTaskRequest.setPeriodYear(LocalDate.now().getYear());
            colleagueTaskRequest.setDeadline(LocalDateTime.now().plusDays(7));
            evaluationTaskService.createEvaluationTasks(colleagueTaskRequest, loginUser);

            log.info("为离职员工 {} 创建了评价任务", employeeId);
        }

        return true;
    }

    @Override
    public List<Long> getColleaguesToEvaluate(Long employeeId, User loginUser) {
        // 1. 获取离职员工信息
        Employee employee = employeeService.getById(employeeId);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 2. 获取当前登录用户的员工信息
        List<Employee> evaluatorEmployees = employeeService.list(
                QueryWrapper.create().eq("user_id", loginUser.getId()));
        ThrowUtils.throwIf(evaluatorEmployees == null || evaluatorEmployees.isEmpty(),
                ErrorCode.NOT_FOUND_ERROR, "评价人信息不存在");
        Employee evaluatorEmployee = evaluatorEmployees.get(0);

        // 3. 验证是否为同部门
        ThrowUtils.throwIf(evaluatorEmployee.getDepartmentId() == null || employee.getDepartmentId() == null,
                ErrorCode.NO_AUTH_ERROR, "员工或评价人没有部门信息");
        ThrowUtils.throwIf(!evaluatorEmployee.getDepartmentId().equals(employee.getDepartmentId()),
                ErrorCode.NO_AUTH_ERROR, "只能评价同部门同事");

        // 4. 检查是否已经评价过
        long existingCount = this.count(
                QueryWrapper.create()
                        .eq("employee_id", employeeId)
                        .eq("evaluator_id", loginUser.getId())
                        .eq("evaluation_type", EvaluationTypeEnum.COLLEAGUE.getValue())
                        .eq("evaluation_period", EvaluationPeriodEnum.RESIGNATION.getValue()));

        // 如果已经评价过，返回空列表
        if (existingCount > 0) {
            return Collections.emptyList();
        }

        // 返回待评价的员工ID列表（这里只返回一个，因为同事评价是一对一的）
        return Collections.singletonList(employeeId);
    }

    @Override
    public Boolean validateEvaluationPermission(Long employeeId, Long evaluatorId, Integer evaluationType) {
        Employee employee = employeeService.getById(employeeId);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        EvaluationTypeEnum typeEnum = EvaluationTypeEnum.getEnumByValue(evaluationType);
        ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "无效的评价类型");

        User evaluatorUser = userService.getById(evaluatorId);
        ThrowUtils.throwIf(evaluatorUser == null, ErrorCode.NOT_FOUND_ERROR, "评价人用户信息不存在");

        // 获取评价人的员工信息（HR可能不是employee，需要特殊处理）
        List<Employee> evaluatorEmployees = employeeService.list(
                QueryWrapper.create().eq("user_id", evaluatorId));
        Employee evaluatorEmployee = null;
        if (evaluatorEmployees != null && !evaluatorEmployees.isEmpty()) {
            evaluatorEmployee = evaluatorEmployees.get(0);
        }

        switch (typeEnum) {
            case LEADER:
                // 领导评价：验证是否为部门主管
                // 如果员工没有部门信息（可能是离职员工），尝试从评价任务中获取部门信息
                Long departmentId = employee.getDepartmentId();
                if (departmentId == null) {
                    // 尝试从评价任务中获取部门信息（离职评价场景）
                    List<EvaluationTask> tasks = evaluationTaskService.list(
                            QueryWrapper.create()
                                    .eq("employee_id", employeeId)
                                    .eq("evaluator_id", evaluatorId)
                                    .eq("evaluation_type", EvaluationTypeEnum.LEADER.getValue())
                                    .eq("status", 0) // 待评价状态
                                    .limit(1));
                    if (tasks != null && !tasks.isEmpty() && tasks.get(0).getDepartmentId() != null) {
                        departmentId = tasks.get(0).getDepartmentId();
                        log.info("从评价任务中获取部门信息，employeeId={}, departmentId={}", employeeId, departmentId);
                    } else {
                        log.warn("领导评价权限验证失败：被评价员工没有部门信息，且无法从评价任务中获取，employeeId={}", employeeId);
                        return false;
                    }
                }
                if (evaluatorEmployee == null) {
                    log.warn("领导评价权限验证失败：评价人不是员工，evaluatorId={}", evaluatorId);
                    return false;
                }
                // 获取被评价员工所在部门的信息
                List<Department> departments = departmentService.list(
                        QueryWrapper.create().eq("id", departmentId));
                if (departments == null || departments.isEmpty()) {
                    log.warn("领导评价权限验证失败：部门不存在，departmentId={}", departmentId);
                    return false;
                }
                Department department = departments.get(0);
                // 验证部门主管ID是否与评价人的员工ID匹配
                if (department.getLeaderId() == null) {
                    log.warn("领导评价权限验证失败：部门没有主管，departmentId={}", departmentId);
                    return false;
                }
                // 比较部门主管ID（employee ID）和评价人的员工ID
                boolean isLeader = department.getLeaderId().equals(evaluatorEmployee.getId());
                if (!isLeader) {
                    log.warn("领导评价权限验证失败：评价人不是部门主管，departmentId={}, departmentLeaderId={}, evaluatorEmployeeId={}",
                            departmentId, department.getLeaderId(), evaluatorEmployee.getId());
                }
                return isLeader;
            case COLLEAGUE:
                // 同事评价：验证是否为同部门且不是自己
                if (evaluatorEmployee == null) {
                    return false;
                }
                if (evaluatorEmployee.getId().equals(employeeId)) {
                    return false;
                }
                return evaluatorEmployee.getDepartmentId() != null
                        && employee.getDepartmentId() != null
                        && evaluatorEmployee.getDepartmentId().equals(employee.getDepartmentId());
            case HR:
                // HR评价：验证是否为HR角色（HR可能不是employee，所以不需要employee信息）
                return UserRoleEnum.HR.getValue().equals(evaluatorUser.getUserRole())
                        || UserRoleEnum.COMPANY_ADMIN.getValue().equals(evaluatorUser.getUserRole())
                        || UserRoleEnum.ADMIN.getValue().equals(evaluatorUser.getUserRole());
            case SELF:
                // 自评：验证是否为本人
                if (evaluatorEmployee == null) {
                    return false;
                }
                return evaluatorEmployee.getId().equals(employeeId);
            default:
                return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEvaluation(EvaluationUpdateRequest updateRequest, User loginUser) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null, ErrorCode.PARAMS_ERROR);

        Evaluation evaluation = this.getById(updateRequest.getId());
        ThrowUtils.throwIf(evaluation == null, ErrorCode.NOT_FOUND_ERROR, "评价不存在");

        // 权限校验：只能更新自己创建的评价
        if (evaluation.getEvaluatorId() != null) {
            ThrowUtils.throwIf(!evaluation.getEvaluatorId().equals(loginUser.getId()),
                    ErrorCode.NO_AUTH_ERROR, "只能更新自己创建的评价");
        }

        // 更新评价内容
        if (updateRequest.getComment() != null) {
            evaluation.setComment(updateRequest.getComment());
        }
        evaluationMapper.update(evaluation);

        // 更新维度评分（先删除旧的，再插入新的）
        if (updateRequest.getDimensionScores() != null) {
            List<EvaluationDimensionScore> oldScores = dimensionScoreMapper.selectListByQuery(
                    QueryWrapper.create().eq("evaluation_id", evaluation.getId()));
            if (oldScores != null && !oldScores.isEmpty()) {
                for (EvaluationDimensionScore oldScore : oldScores) {
                    dimensionScoreMapper.deleteById(oldScore.getId());
                }
            }
            for (EvaluationUpdateRequest.DimensionScoreRequest ds : updateRequest.getDimensionScores()) {
                ThrowUtils.throwIf(ds.getScore() == null || ds.getScore() < 1 || ds.getScore() > 5,
                        ErrorCode.PARAMS_ERROR, "维度评分必须在1-5分之间");
                EvaluationDimensionScore dimensionScore = EvaluationDimensionScore.builder()
                        .evaluationId(evaluation.getId())
                        .dimensionId(ds.getDimensionId())
                        .score(ds.getScore())
                        .build();
                dimensionScoreMapper.insert(dimensionScore);
            }
        }

        // 更新标签关联（先删除旧的，再插入新的）
        if (updateRequest.getTagIds() != null) {
            List<EvaluationTagRelation> oldRelations = tagRelationMapper.selectListByQuery(
                    QueryWrapper.create().eq("evaluation_id", evaluation.getId()));
            if (oldRelations != null && !oldRelations.isEmpty()) {
                for (EvaluationTagRelation oldRelation : oldRelations) {
                    tagRelationMapper.deleteById(oldRelation.getId());
                }
            }
            for (Long tagId : updateRequest.getTagIds()) {
                EvaluationTagRelation tagRelation = EvaluationTagRelation.builder()
                        .evaluationId(evaluation.getId())
                        .tagId(tagId)
                        .build();
                tagRelationMapper.insert(tagRelation);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteEvaluation(Long id, User loginUser) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);

        Evaluation evaluation = this.getById(id);
        ThrowUtils.throwIf(evaluation == null, ErrorCode.NOT_FOUND_ERROR, "评价不存在");

        // 权限校验：只有系统管理员可以删除
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isAdmin, ErrorCode.NO_AUTH_ERROR, "只有系统管理员可以删除评价");

        // 逻辑删除评价（评价表）
        boolean deleted = this.removeById(id);

        // 逻辑删除关联的标签记录（evaluation_tag_relation表）
        if (deleted) {
            List<EvaluationTagRelation> tagRelations = tagRelationMapper.selectListByQuery(
                    QueryWrapper.create().eq("evaluation_id", id));
            if (tagRelations != null && !tagRelations.isEmpty()) {
                for (EvaluationTagRelation tagRelation : tagRelations) {
                    tagRelation.setIsDelete(true);
                    tagRelationMapper.update(tagRelation);
                }
                log.info("删除评价时，同时逻辑删除了 {} 条标签关联记录，evaluationId={}",
                        tagRelations.size(), id);
            }
        }

        return deleted;
    }

    @Override
    public EvaluationDetailVO getEvaluationDetail(Long id, User loginUser) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);

        Evaluation evaluation = this.getById(id);
        ThrowUtils.throwIf(evaluation == null, ErrorCode.NOT_FOUND_ERROR, "评价不存在");

        // 权限验证
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            if (isHr || isCompanyAdmin) {
                // HR和公司管理员只能查看本公司员工的评价
                Employee employee = employeeService.getById(evaluation.getEmployeeId());
                if (employee != null && employee.getCompanyId() != null) {
                    ThrowUtils.throwIf(!employee.getCompanyId().equals(loginUser.getCompanyId()),
                            ErrorCode.NO_AUTH_ERROR, "只能查看本公司员工的评价详情");
                }
            } else {
                // 普通员工只能查看自己的评价（作为被评价人或评价人）
                List<Employee> employees = employeeService.list(
                        QueryWrapper.create().eq("user_id", loginUser.getId()));
                if (employees != null && !employees.isEmpty()) {
                    Long currentEmployeeId = employees.get(0).getId();
                    // 验证：要么是被评价人，要么是评价人
                    boolean isEvaluatedEmployee = evaluation.getEmployeeId().equals(currentEmployeeId);
                    boolean isEvaluator = evaluation.getEvaluatorId().equals(loginUser.getId());
                    ThrowUtils.throwIf(!isEvaluatedEmployee && !isEvaluator,
                            ErrorCode.NO_AUTH_ERROR, "只能查看与自己相关的评价详情");
                } else {
                    // 如果当前用户不是员工，只能查看自己作为评价人的评价
                    ThrowUtils.throwIf(!evaluation.getEvaluatorId().equals(loginUser.getId()),
                            ErrorCode.NO_AUTH_ERROR, "只能查看自己作为评价人的评价详情");
                }
            }
        }

        EvaluationDetailVO detailVO = new EvaluationDetailVO();
        EvaluationVO vo = getEvaluationVO(evaluation);
        cn.hutool.core.bean.BeanUtil.copyProperties(vo, detailVO);

        // 计算平均维度评分
        List<EvaluationDimensionScore> dimensionScores = dimensionScoreMapper.selectListByQuery(
                QueryWrapper.create().eq("evaluation_id", id));
        if (dimensionScores != null && !dimensionScores.isEmpty()) {
            double avgScore = dimensionScores.stream()
                    .mapToInt(EvaluationDimensionScore::getScore)
                    .average()
                    .orElse(0.0);
            detailVO.setAverageDimensionScore(avgScore);
        }

        return detailVO;
    }

    @Override
    public Page<EvaluationVO> pageEvaluation(EvaluationQueryRequest queryRequest, User loginUser) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);

        // 权限过滤
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        QueryWrapper qw = getQueryWrapper(queryRequest);

        // 权限控制：非系统管理员只能查看本公司或本部门的评价
        if (!isAdmin) {
            if (isHr || isCompanyAdmin) {
                // HR和公司管理员可以查看本公司员工的评价
                Long companyId = loginUser.getCompanyId();
                if (companyId != null) {
                    // 如果查询条件中指定了evaluatorId，说明要查询某个评价人的评价
                    if (queryRequest.getEvaluatorId() != null) {
                        // 验证evaluatorId是否是当前登录用户
                        if (!queryRequest.getEvaluatorId().equals(loginUser.getId())) {
                            log.info("HR/公司管理员权限控制：查询的评价人不是当前用户，evaluatorId={}, loginUserId={}",
                                    queryRequest.getEvaluatorId(), loginUser.getId());
                            return new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                        }
                        // evaluatorId验证通过，说明是查询"我作为评价人"的记录
                        // 由于离职员工的companyId可能已清空，所以不限制employee_id，只限制evaluatorId
                        log.debug("HR/公司管理员查询：查询我作为评价人的评价记录，只限制evaluatorId，不限制employee_id");
                    } else if (queryRequest.getEmployeeId() != null) {
                        // 如果指定了employeeId，验证该员工是否属于当前公司
                        // 但需要考虑员工可能已离职（companyId可能为null）
                        Employee employee = employeeService.getById(queryRequest.getEmployeeId());
                        if (employee == null) {
                            log.warn("被查询的员工不存在: employeeId={}", queryRequest.getEmployeeId());
                            return new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                        }
                        // 如果员工有companyId，验证是否属于当前公司
                        if (employee.getCompanyId() != null && !companyId.equals(employee.getCompanyId())) {
                            log.warn("被查询的员工不属于当前公司: employeeId={}, employeeCompanyId={}, loginUserCompanyId={}",
                                    queryRequest.getEmployeeId(), employee.getCompanyId(), companyId);
                            return new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                        }
                        log.debug("被查询的员工属于当前公司或已离职: employeeId={}", queryRequest.getEmployeeId());
                    } else {
                        // 如果两者都没指定，限制为本公司员工的评价
                        List<Employee> companyEmployees = employeeService.list(
                                QueryWrapper.create().eq("company_id", companyId));
                        if (companyEmployees == null || companyEmployees.isEmpty()) {
                            return new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                        }
                        List<Long> employeeIds = companyEmployees.stream()
                                .map(Employee::getId)
                                .collect(Collectors.toList());
                        qw.in("employee_id", employeeIds);
                        log.debug("HR/公司管理员查询：限制employee_id为本公司员工: employeeIds={}", employeeIds);
                    }
                }
            } else {
                // 普通员工只能查看自己的评价
                // 获取当前登录用户的员工信息
                List<Employee> employees = employeeService.list(
                        QueryWrapper.create().eq("user_id", loginUser.getId()));
                if (employees == null || employees.isEmpty()) {
                    log.warn("普通员工权限控制：未找到当前用户的员工信息，userId={}", loginUser.getId());
                    return new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                }
                Employee currentEmployee = employees.get(0);
                Long currentEmployeeId = currentEmployee.getId();

                // 如果查询条件中指定了evaluatorId，说明要查询"我作为评价人"的评价记录
                // 此时需要验证evaluatorId是否是当前登录用户，验证通过后不需要限制employee_id
                if (queryRequest.getEvaluatorId() != null) {
                    if (!queryRequest.getEvaluatorId().equals(loginUser.getId())) {
                        log.info("普通员工权限控制：查询的评价人不是当前用户，evaluatorId={}, loginUserId={}",
                                queryRequest.getEvaluatorId(), loginUser.getId());
                        // 查询的不是当前用户的评价，返回空结果
                        return new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                    }
                    // evaluatorId验证通过，说明是查询"我作为评价人"的记录
                    // 此时不需要限制employee_id，因为员工可能评价了多个不同的员工
                    log.debug("普通员工查询：查询我作为评价人的评价记录，evaluatorId={}", queryRequest.getEvaluatorId());
                } else if (queryRequest.getEmployeeId() != null) {
                    // 如果指定了employeeId，说明要查询"我被评价"的记录
                    // 验证employeeId是否是当前员工
                    if (!queryRequest.getEmployeeId().equals(currentEmployeeId)) {
                        log.info("普通员工权限控制：查询的被评价员工不是当前员工，employeeId={}, currentEmployeeId={}",
                                queryRequest.getEmployeeId(), currentEmployeeId);
                        // 查询的不是自己的评价，返回空结果
                        return new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(), 0);
                    }
                    // employeeId验证通过，说明是查询"我被评价"的记录
                    // 此时不需要额外限制，因为employeeId已经在查询条件中了
                    log.debug("普通员工查询：查询我被评价的记录，employeeId={}", queryRequest.getEmployeeId());
                } else {
                    // 如果两者都没指定，默认查询"我被评价"的记录
                    // 限制employee_id为当前员工的employeeId
                    qw.eq("employee_id", currentEmployeeId);
                    log.debug("普通员工查询：默认查询我被评价的记录，employeeId={}", currentEmployeeId);
                }
            }
        }

        // 记录查询条件用于调试
        log.debug("查询评价列表，查询条件：evaluatorId={}, employeeId={}, 登录用户角色={}, 登录用户ID={}",
                queryRequest.getEvaluatorId(), queryRequest.getEmployeeId(),
                loginUser.getUserRole(), loginUser.getId());

        Page<Evaluation> page = this.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), qw);
        log.debug("查询结果：总数={}, 当前页记录数={}", page.getTotalRow(),
                page.getRecords() != null ? page.getRecords().size() : 0);

        Page<EvaluationVO> voPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(),
                page.getTotalRow());
        List<EvaluationVO> voList = getEvaluationVOList(page.getRecords());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public EvaluationStatisticsVO getEvaluationStatistics(Long employeeId, User loginUser) {
        ThrowUtils.throwIf(employeeId == null, ErrorCode.PARAMS_ERROR);

        Employee employee = employeeService.getById(employeeId);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 权限校验
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            if (isHr || isCompanyAdmin) {
                ThrowUtils.throwIf(!loginUser.getCompanyId().equals(employee.getCompanyId()),
                        ErrorCode.NO_AUTH_ERROR, "只能查看本公司员工的评价统计");
            } else {
                List<Employee> currentEmployees = employeeService.list(
                        QueryWrapper.create().eq("user_id", loginUser.getId()));
                ThrowUtils.throwIf(currentEmployees == null || currentEmployees.isEmpty()
                        || !currentEmployees.get(0).getId().equals(employeeId),
                        ErrorCode.NO_AUTH_ERROR, "只能查看自己的评价统计");
            }
        }

        EvaluationStatisticsVO statistics = new EvaluationStatisticsVO();
        statistics.setEmployeeId(employeeId);
        statistics.setEmployeeName(employee.getName());

        // 查询所有评价
        List<Evaluation> evaluations = this.list(
                QueryWrapper.create().eq("employee_id", employeeId));

        statistics.setTotalCount(evaluations.size());

        // 按类型统计
        Map<Integer, Integer> countByType = evaluations.stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getEvaluationType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        statistics.setCountByType(countByType);

        // 按周期统计
        Map<Integer, Integer> countByPeriod = evaluations.stream()
                .filter(e -> e.getEvaluationPeriod() != null)
                .collect(Collectors.groupingBy(
                        Evaluation::getEvaluationPeriod,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        statistics.setCountByPeriod(countByPeriod);

        // 计算平均维度评分
        List<EvaluationDimensionScore> allDimensionScores = dimensionScoreMapper.selectListByQuery(
                QueryWrapper.create().in("evaluation_id",
                        evaluations.stream().map(Evaluation::getId).collect(Collectors.toList())));

        Map<String, Double> averageDimensionScores = new HashMap<>();
        if (allDimensionScores != null && !allDimensionScores.isEmpty()) {
            Map<Long, List<EvaluationDimensionScore>> scoresByDimension = allDimensionScores.stream()
                    .collect(Collectors.groupingBy(EvaluationDimensionScore::getDimensionId));

            for (Map.Entry<Long, List<EvaluationDimensionScore>> entry : scoresByDimension.entrySet()) {
                EvaluationDimension dimension = dimensionMapper.selectOneById(entry.getKey());
                if (dimension != null) {
                    double avgScore = entry.getValue().stream()
                            .mapToInt(EvaluationDimensionScore::getScore)
                            .average()
                            .orElse(0.0);
                    averageDimensionScores.put(dimension.getName(), avgScore);
                }
            }
        }
        statistics.setAverageDimensionScores(averageDimensionScores);

        // 季度评价趋势（简化实现）
        List<EvaluationStatisticsVO.QuarterlyTrend> quarterlyTrends = new ArrayList<>();
        Map<String, List<Evaluation>> quarterlyEvaluations = evaluations.stream()
                .filter(e -> e.getEvaluationPeriod() != null
                        && e.getEvaluationPeriod().equals(EvaluationPeriodEnum.QUARTERLY.getValue())
                        && e.getPeriodYear() != null && e.getPeriodQuarter() != null)
                .collect(Collectors.groupingBy(e -> e.getPeriodYear() + "-Q" + e.getPeriodQuarter()));

        for (Map.Entry<String, List<Evaluation>> entry : quarterlyEvaluations.entrySet()) {
            List<Long> evalIds = entry.getValue().stream()
                    .map(Evaluation::getId)
                    .collect(Collectors.toList());

            List<EvaluationDimensionScore> scores = dimensionScoreMapper.selectListByQuery(
                    QueryWrapper.create().in("evaluation_id", evalIds));

            if (!scores.isEmpty()) {
                double avgScore = scores.stream()
                        .mapToInt(EvaluationDimensionScore::getScore)
                        .average()
                        .orElse(0.0);

                String[] parts = entry.getKey().split("-Q");
                EvaluationStatisticsVO.QuarterlyTrend trend = new EvaluationStatisticsVO.QuarterlyTrend();
                trend.setYear(Integer.parseInt(parts[0]));
                trend.setQuarter(Integer.parseInt(parts[1]));
                trend.setAverageScore(avgScore);
                quarterlyTrends.add(trend);
            }
        }
        statistics.setQuarterlyTrends(quarterlyTrends);

        return statistics;
    }

    @Override
    public EvaluationVO getEvaluationVO(Evaluation evaluation) {
        if (evaluation == null) {
            return null;
        }

        EvaluationVO vo = new EvaluationVO();
        cn.hutool.core.bean.BeanUtil.copyProperties(evaluation, vo);

        // 填充员工姓名
        Employee employee = employeeService.getById(evaluation.getEmployeeId());
        if (employee != null) {
            vo.setEmployeeName(employee.getName());
        }

        // 填充评价人姓名和公司信息
        User evaluator = userService.getById(evaluation.getEvaluatorId());
        if (evaluator != null) {
            vo.setEvaluatorName(evaluator.getNickname());

            // 获取评价人所属公司ID
            Long evaluatorCompanyId = evaluator.getCompanyId();

            // 如果评价人是employee角色且user表中的companyId为null，则从employee表获取
            if (evaluatorCompanyId == null && UserRoleEnum.EMPLOYEE.getValue().equals(evaluator.getUserRole())) {
                List<Employee> evaluatorEmployees = employeeService.list(
                        QueryWrapper.create().eq("user_id", evaluator.getId()).limit(1));
                if (evaluatorEmployees != null && !evaluatorEmployees.isEmpty()) {
                    Employee evaluatorEmployee = evaluatorEmployees.get(0);
                    evaluatorCompanyId = evaluatorEmployee.getCompanyId();
                }
            }

            vo.setEvaluatorCompanyId(evaluatorCompanyId);

            // 获取评价人所属公司名称
            if (evaluatorCompanyId != null) {
                Company evaluatorCompany = companyService.getById(evaluatorCompanyId);
                if (evaluatorCompany != null) {
                    vo.setEvaluatorCompanyName(evaluatorCompany.getName());
                }
            }
        }

        // 填充被评价员工所属公司名称
        if (evaluation.getCompanyId() != null) {
            Company company = companyService.getById(evaluation.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getName());
            }
        }

        // 填充评价类型文本
        EvaluationTypeEnum typeEnum = EvaluationTypeEnum.getEnumByValue(evaluation.getEvaluationType());
        if (typeEnum != null) {
            vo.setEvaluationTypeText(typeEnum.getText());
        }

        // 填充评价周期文本
        if (evaluation.getEvaluationPeriod() != null) {
            EvaluationPeriodEnum periodEnum = EvaluationPeriodEnum.getEnumByValue(evaluation.getEvaluationPeriod());
            if (periodEnum != null) {
                vo.setEvaluationPeriodText(periodEnum.getText());
            }
        }

        // 填充维度评分
        List<EvaluationDimensionScore> dimensionScores = dimensionScoreMapper.selectListByQuery(
                QueryWrapper.create().eq("evaluation_id", evaluation.getId()));
        if (dimensionScores != null && !dimensionScores.isEmpty()) {
            List<EvaluationVO.DimensionScoreVO> scoreVOs = new ArrayList<>();
            for (EvaluationDimensionScore ds : dimensionScores) {
                EvaluationDimension dimension = dimensionMapper.selectOneById(ds.getDimensionId());
                if (dimension != null) {
                    EvaluationVO.DimensionScoreVO scoreVO = new EvaluationVO.DimensionScoreVO();
                    scoreVO.setDimensionId(ds.getDimensionId());
                    scoreVO.setDimensionName(dimension.getName());
                    scoreVO.setScore(ds.getScore());
                    scoreVOs.add(scoreVO);
                }
            }
            vo.setDimensionScores(scoreVOs);
        }

        // 填充标签
        List<EvaluationTagRelation> tagRelations = tagRelationMapper.selectListByQuery(
                QueryWrapper.create().eq("evaluation_id", evaluation.getId()));
        if (tagRelations != null && !tagRelations.isEmpty()) {
            List<EvaluationVO.TagVO> tagVOs = new ArrayList<>();
            for (EvaluationTagRelation tr : tagRelations) {
                EvaluationTag tag = tagMapper.selectOneById(tr.getTagId());
                if (tag != null) {
                    EvaluationVO.TagVO tagVO = new EvaluationVO.TagVO();
                    tagVO.setTagId(tag.getId());
                    tagVO.setTagName(tag.getName());
                    tagVO.setTagType(tag.getType());
                    tagVOs.add(tagVO);
                }
            }
            vo.setTags(tagVOs);
        }

        return vo;
    }

    @Override
    public List<EvaluationVO> getEvaluationVOList(List<Evaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return new ArrayList<>();
        }
        return evaluations.stream()
                .map(this::getEvaluationVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(EvaluationQueryRequest queryRequest) {
        if (queryRequest == null) {
            return QueryWrapper.create();
        }

        QueryWrapper qw = QueryWrapper.create();

        if (queryRequest.getEmployeeId() != null) {
            qw.eq("employee_id", queryRequest.getEmployeeId());
        }

        if (queryRequest.getEvaluatorId() != null) {
            qw.eq("evaluator_id", queryRequest.getEvaluatorId());
            log.debug("查询条件：evaluatorId={}", queryRequest.getEvaluatorId());
        }

        if (queryRequest.getEvaluationType() != null) {
            qw.eq("evaluation_type", queryRequest.getEvaluationType());
        }

        if (queryRequest.getEvaluationPeriod() != null) {
            qw.eq("evaluation_period", queryRequest.getEvaluationPeriod());
        }

        if (queryRequest.getPeriodYear() != null) {
            qw.eq("period_year", queryRequest.getPeriodYear());
        }

        if (queryRequest.getPeriodQuarter() != null) {
            qw.eq("period_quarter", queryRequest.getPeriodQuarter());
        }

        if (queryRequest.getCompanyId() != null) {
            qw.eq("company_id", queryRequest.getCompanyId());
        }

        // 排序
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        if (sortField != null && !sortField.isEmpty()) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            qw.orderBy("create_time", false);
        }

        return qw;
    }

    @Override
    public List<EvaluationDimensionScoreVO> calculateDimensionScores(EvaluationQueryRequest queryRequest,
            User loginUser) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(queryRequest.getEmployeeId() == null, ErrorCode.PARAMS_ERROR, "员工ID不能为空");

        // 权限校验
        Employee employee = employeeService.getById(queryRequest.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            if (isHr || isCompanyAdmin) {
                ThrowUtils.throwIf(!loginUser.getCompanyId().equals(employee.getCompanyId()),
                        ErrorCode.NO_AUTH_ERROR, "只能查看本公司员工的评价数据");
            } else {
                List<Employee> currentEmployees = employeeService.list(
                        QueryWrapper.create().eq("user_id", loginUser.getId()));
                ThrowUtils.throwIf(currentEmployees == null || currentEmployees.isEmpty()
                        || !currentEmployees.get(0).getId().equals(queryRequest.getEmployeeId()),
                        ErrorCode.NO_AUTH_ERROR, "只能查看自己的评价数据");
            }
        }

        // 构建查询条件（不分页，获取所有符合条件的评价）
        QueryWrapper qw = getQueryWrapper(queryRequest);

        // 查询所有符合条件的评价
        List<Evaluation> evaluations = this.list(qw);

        if (evaluations == null || evaluations.isEmpty()) {
            return new ArrayList<>();
        }

        // 权重配置：领导评价40%，同事评价30%，HR评价30%，自评0%
        Map<Integer, Double> weights = new HashMap<>();
        weights.put(1, 0.4); // 领导评价
        weights.put(2, 0.3); // 同事评价
        weights.put(3, 0.3); // HR评价
        weights.put(4, 0.0); // 自评不计入

        // 获取所有评价的维度评分
        List<Long> evaluationIds = evaluations.stream()
                .map(Evaluation::getId)
                .collect(Collectors.toList());

        List<EvaluationDimensionScore> allDimensionScores = dimensionScoreMapper.selectListByQuery(
                QueryWrapper.create().in("evaluation_id", evaluationIds));

        if (allDimensionScores == null || allDimensionScores.isEmpty()) {
            return new ArrayList<>();
        }

        // 按维度ID分组，计算加权平均分
        Map<Long, Map<String, Object>> dimensionDataMap = new HashMap<>();

        for (Evaluation evaluation : evaluations) {
            Double weight = weights.get(evaluation.getEvaluationType());
            if (weight == null || weight == 0.0) {
                continue; // 跳过自评或无效类型
            }

            // 获取该评价的维度评分
            List<EvaluationDimensionScore> evalScores = allDimensionScores.stream()
                    .filter(ds -> ds.getEvaluationId().equals(evaluation.getId()))
                    .collect(Collectors.toList());

            for (EvaluationDimensionScore ds : evalScores) {
                Long dimensionId = ds.getDimensionId();
                if (dimensionId == null) {
                    continue;
                }

                dimensionDataMap.putIfAbsent(dimensionId, new HashMap<>());
                Map<String, Object> data = dimensionDataMap.get(dimensionId);

                // 累加加权分数
                double weightedScore = (ds.getScore() != null ? ds.getScore() : 0) * weight;
                data.put("total", (Double) data.getOrDefault("total", 0.0) + weightedScore);
                data.put("count", (Double) data.getOrDefault("count", 0.0) + weight);

                // 从维度表查询维度名称
                EvaluationDimension dimension = dimensionMapper.selectOneById(dimensionId);
                if (dimension != null && dimension.getName() != null && !dimension.getName().trim().isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Set<String> names = (Set<String>) data.getOrDefault("names", new HashSet<String>());
                    names.add(dimension.getName().trim());
                    data.put("names", names);
                }
            }
        }

        // 构建结果列表
        List<EvaluationDimensionScoreVO> result = new ArrayList<>();

        for (Map.Entry<Long, Map<String, Object>> entry : dimensionDataMap.entrySet()) {
            Long dimensionId = entry.getKey();
            Map<String, Object> data = entry.getValue();

            Double total = (Double) data.get("total");
            Double count = (Double) data.get("count");

            if (count == null || count == 0.0) {
                continue;
            }

            double averageScore = total / count;

            // 获取维度名称
            String dimensionName = "维度" + dimensionId;
            @SuppressWarnings("unchecked")
            Set<String> names = (Set<String>) data.get("names");
            if (names != null && !names.isEmpty()) {
                dimensionName = names.iterator().next(); // 使用第一个名称
            } else {
                // 如果维度评分中没有名称，从维度表中查询
                EvaluationDimension dimension = dimensionMapper.selectOneById(dimensionId);
                if (dimension != null && dimension.getName() != null) {
                    dimensionName = dimension.getName();
                }
            }

            EvaluationDimensionScoreVO vo = new EvaluationDimensionScoreVO();
            vo.setDimensionId(dimensionId);
            vo.setDimensionName(dimensionName);
            vo.setAverageScore(Math.round(averageScore * 100.0) / 100.0); // 保留两位小数

            result.add(vo);
        }

        // 按维度ID排序
        result.sort(Comparator.comparing(EvaluationDimensionScoreVO::getDimensionId));

        return result;
    }

    @Override
    public List<EvaluationTagStatisticsVO> countTagStatistics(EvaluationQueryRequest queryRequest, User loginUser) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(queryRequest.getEmployeeId() == null, ErrorCode.PARAMS_ERROR, "员工ID不能为空");

        // 权限校验（与calculateDimensionScores相同的逻辑）
        Employee employee = employeeService.getById(queryRequest.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            if (isHr || isCompanyAdmin) {
                ThrowUtils.throwIf(!loginUser.getCompanyId().equals(employee.getCompanyId()),
                        ErrorCode.NO_AUTH_ERROR, "只能查看本公司员工的评价数据");
            } else {
                List<Employee> currentEmployees = employeeService.list(
                        QueryWrapper.create().eq("user_id", loginUser.getId()));
                ThrowUtils.throwIf(currentEmployees == null || currentEmployees.isEmpty()
                        || !currentEmployees.get(0).getId().equals(queryRequest.getEmployeeId()),
                        ErrorCode.NO_AUTH_ERROR, "只能查看自己的评价数据");
            }
        }

        // 构建查询条件（不分页，获取所有符合条件的评价）
        QueryWrapper qw = getQueryWrapper(queryRequest);

        // 查询所有符合条件的评价
        List<Evaluation> evaluations = this.list(qw);

        if (evaluations == null || evaluations.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有评价ID
        List<Long> evaluationIds = evaluations.stream()
                .map(Evaluation::getId)
                .collect(Collectors.toList());

        // 查询所有标签关联
        List<EvaluationTagRelation> tagRelations = tagRelationMapper.selectListByQuery(
                QueryWrapper.create().in("evaluation_id", evaluationIds));

        if (tagRelations == null || tagRelations.isEmpty()) {
            return new ArrayList<>();
        }

        // 按标签ID分组统计
        Map<Long, Long> tagCountMap = tagRelations.stream()
                .filter(tr -> tr.getTagId() != null)
                .collect(Collectors.groupingBy(
                        EvaluationTagRelation::getTagId,
                        Collectors.counting()));

        // 获取所有标签信息
        List<Long> tagIds = new ArrayList<>(tagCountMap.keySet());
        List<EvaluationTag> tags = tagMapper.selectListByQuery(
                QueryWrapper.create().in("id", tagIds));

        // 构建结果列表
        List<EvaluationTagStatisticsVO> result = new ArrayList<>();
        Map<Long, EvaluationTag> tagMap = tags.stream()
                .collect(Collectors.toMap(EvaluationTag::getId, tag -> tag));

        for (Map.Entry<Long, Long> entry : tagCountMap.entrySet()) {
            Long tagId = entry.getKey();
            Long count = entry.getValue();

            EvaluationTag tag = tagMap.get(tagId);
            if (tag == null) {
                continue;
            }

            EvaluationTagStatisticsVO vo = new EvaluationTagStatisticsVO();
            vo.setTagId(tagId);
            vo.setTagName(tag.getName());
            vo.setTagType(tag.getType());
            vo.setCount(count);

            result.add(vo);
        }

        // 按出现次数降序排序，次数相同则按标签名称排序
        result.sort((a, b) -> {
            int countCompare = Long.compare(b.getCount(), a.getCount());
            if (countCompare != 0) {
                return countCompare;
            }
            String nameA = a.getTagName() != null ? a.getTagName() : "";
            String nameB = b.getTagName() != null ? b.getTagName() : "";
            return nameA.compareTo(nameB);
        });

        return result;
    }
}
