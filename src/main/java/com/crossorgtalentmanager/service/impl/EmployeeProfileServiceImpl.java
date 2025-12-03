package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileAddRequest;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileQueryRequest;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileUpdateRequest;
import com.crossorgtalentmanager.model.entity.EmployeeProfile;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.EmployeeProfileVO;
import com.crossorgtalentmanager.service.EmployeeProfileService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.crossorgtalentmanager.mapper.EmployeeProfileMapper;
import com.crossorgtalentmanager.model.enums.PointsChangeReasonEnum;
import com.crossorgtalentmanager.service.CompanyPointsService;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.service.ProfileAccessRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 员工档案信息 服务层实现。
 *
 * 提供 VO 包装、查询构造器与逻辑删除切换等方法，风格与其他服务一致。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Service("employeeProfileService")
@Slf4j
public class EmployeeProfileServiceImpl extends ServiceImpl<EmployeeProfileMapper, EmployeeProfile>
        implements EmployeeProfileService {

    @Resource
    private CompanyService companyService;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private CompanyPointsService companyPointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addEmployeeProfile(EmployeeProfileAddRequest addRequest, User loginUser) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");
        ThrowUtils.throwIf(addRequest.getEmployeeId() == null || addRequest.getEmployeeId() <= 0,
                ErrorCode.PARAMS_ERROR, "员工 ID 不能为空");

        // 查询员工信息
        com.crossorgtalentmanager.model.entity.Employee employee = employeeService.getById(addRequest.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 权限校验：管理员可以添加任何人的任何档案（可指定公司），公司管理员/HR只能添加本公司员工在本公司产生的档案
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        Long profileCompanyId;

        if (isAdmin) {
            // 管理员：必须指定公司ID（因为离职员工可能没有companyId）
            ThrowUtils.throwIf(addRequest.getCompanyId() == null || addRequest.getCompanyId() <= 0,
                    ErrorCode.PARAMS_ERROR, "管理员添加档案时必须指定所属公司");
            // 验证指定的公司是否存在
            com.crossorgtalentmanager.model.entity.Company company = companyService
                    .getById(addRequest.getCompanyId());
            ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR, "指定的公司不存在");
            profileCompanyId = addRequest.getCompanyId();
        } else {
            // 非管理员：必须使用操作人员的公司，且员工必须属于该公司
            Long loginUserCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginUserCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");
            ThrowUtils.throwIf(!loginUserCompanyId.equals(employee.getCompanyId()),
                    ErrorCode.NO_AUTH_ERROR, "员工所属公司与操作人员不一致");
            profileCompanyId = loginUserCompanyId;
        }

        // 创建档案
        EmployeeProfile profile = new EmployeeProfile();
        profile.setEmployeeId(addRequest.getEmployeeId());
        profile.setCompanyId(profileCompanyId);
        profile.setStartDate(addRequest.getStartDate());
        profile.setEndDate(addRequest.getEndDate());
        profile.setPerformanceSummary(addRequest.getPerformanceSummary());
        profile.setAttendanceRate(addRequest.getAttendanceRate());
        profile.setHasMajorIncident(addRequest.getHasMajorIncident());
        profile.setReasonForLeaving(addRequest.getReasonForLeaving());
        profile.setOccupation(addRequest.getOccupation());
        profile.setAnnualSalary(addRequest.getAnnualSalary());
        profile.setOperatorId(loginUser.getId());
        // 设置公开范围，默认为公开
        profile.setVisibility(addRequest.getVisibility() != null ? addRequest.getVisibility() : 2);

        boolean save = this.save(profile);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "添加失败");

        // 如果为离职员工建立档案（endDate不为null），则增加积分+10分
        if (addRequest.getEndDate() != null) {
            try {
                companyPointsService.addPoints(
                        profileCompanyId,
                        BigDecimal.valueOf(10),
                        PointsChangeReasonEnum.CREATE_PROFILE.getValue(),
                        addRequest.getEmployeeId(),
                        null); // changeDescription will be auto-generated
                log.info("为离职员工建立档案，增加积分：companyId={}, employeeId={}, points=10",
                        profileCompanyId, addRequest.getEmployeeId());
            } catch (Exception e) {
                log.error("添加积分失败：companyId={}, employeeId={}", profileCompanyId, addRequest.getEmployeeId(), e);
                // 积分添加失败不影响档案创建，只记录日志
            }
        }

        return profile.getId();
    }

    @Override
    public Boolean updateEmployeeProfile(EmployeeProfileUpdateRequest updateRequest, User loginUser) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "档案 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        // 查询现有档案信息
        EmployeeProfile existingProfile = this.getById(updateRequest.getId());
        ThrowUtils.throwIf(existingProfile == null, ErrorCode.NOT_FOUND_ERROR, "档案不存在");

        // 权限校验：管理员可以更新任何档案，公司管理员/HR只能更新本公司员工在本公司产生的档案
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        if (!isAdmin) {
            // 非管理员需要校验公司权限
            Long loginUserCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginUserCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

            // 校验档案所属公司必须与操作人员一致
            ThrowUtils.throwIf(!loginUserCompanyId.equals(existingProfile.getCompanyId()),
                    ErrorCode.NO_AUTH_ERROR, "档案所属公司与操作人员不一致");

            // 查询员工信息，校验员工所属公司必须与操作人员一致
            com.crossorgtalentmanager.model.entity.Employee employee = employeeService
                    .getById(existingProfile.getEmployeeId());
            ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工信息不存在");
            ThrowUtils.throwIf(!loginUserCompanyId.equals(employee.getCompanyId()),
                    ErrorCode.NO_AUTH_ERROR, "员工所属公司与操作人员不一致");
        }

        // 使用 DTO 的字段覆盖现有档案（null 值也会被覆盖）
        EmployeeProfile profileToUpdate = new EmployeeProfile();
        profileToUpdate.setId(updateRequest.getId());
        profileToUpdate.setStartDate(updateRequest.getStartDate());
        profileToUpdate.setEndDate(updateRequest.getEndDate());
        profileToUpdate.setPerformanceSummary(updateRequest.getPerformanceSummary());
        profileToUpdate.setAttendanceRate(updateRequest.getAttendanceRate());
        profileToUpdate.setHasMajorIncident(updateRequest.getHasMajorIncident());
        profileToUpdate.setReasonForLeaving(updateRequest.getReasonForLeaving());
        profileToUpdate.setOccupation(updateRequest.getOccupation());
        profileToUpdate.setAnnualSalary(updateRequest.getAnnualSalary());
        // 设置操作人员 ID（系统维护，不接受前端修改）
        profileToUpdate.setOperatorId(loginUser.getId());
        // 更新公开范围
        if (updateRequest.getVisibility() != null) {
            profileToUpdate.setVisibility(updateRequest.getVisibility());
        }

        boolean update = this.updateById(profileToUpdate);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新失败");
        return true;
    }

    @Override
    public EmployeeProfileVO getEmployeeProfileVO(EmployeeProfile employeeProfile) {
        if (employeeProfile == null) {
            return null;
        }
        EmployeeProfileVO vo = new EmployeeProfileVO();
        BeanUtil.copyProperties(employeeProfile, vo);

        // 填充公司名称
        if (employeeProfile.getCompanyId() != null) {
            com.crossorgtalentmanager.model.entity.Company company = companyService
                    .getById(employeeProfile.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getName());
            }
        }
        return vo;
    }

    @Override
    public List<EmployeeProfileVO> getEmployeeProfileVOList(List<EmployeeProfile> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 先转换为 VO
        List<EmployeeProfileVO> voList = list.stream().map(this::getEmployeeProfileVO).toList();

        // 批量填充公司名称以减少查询
        Set<Long> companyIds = list.stream()
                .map(EmployeeProfile::getCompanyId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        if (!companyIds.isEmpty()) {
            List<com.crossorgtalentmanager.model.entity.Company> companies = companyService.listByIds(companyIds);
            Map<Long, String> companyIdToName = companies.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(com.crossorgtalentmanager.model.entity.Company::getId,
                            com.crossorgtalentmanager.model.entity.Company::getName, (a, b) -> a));

            for (EmployeeProfileVO vo : voList) {
                if (vo.getCompanyId() != null) {
                    vo.setCompanyName(companyIdToName.get(vo.getCompanyId()));
                }
            }
        }

        return voList;
    }

    @Override
    public QueryWrapper getQueryWrapper(EmployeeProfileQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Long id = request.getId();
        Long employeeId = request.getEmployeeId();
        Long companyId = request.getCompanyId();
        String companyName = request.getCompanyName();
        String occupation = request.getOccupation();
        java.math.BigDecimal minAnnual = request.getMinAnnualSalary();
        java.math.BigDecimal maxAnnual = request.getMaxAnnualSalary();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        QueryWrapper qw = QueryWrapper.create()
                .eq("id", id)
                .eq("employee_id", employeeId)
                .eq("company_id", companyId)
                .like("occupation", occupation)
                .orderBy(sortField, "ascend".equals(sortOrder));

        // 如果传入 companyName，则先查公司表获得 companyId 列表，再用 in 过滤
        if (companyName != null && !companyName.isEmpty()) {
            List<com.crossorgtalentmanager.model.entity.Company> companies = companyService
                    .list(QueryWrapper.create().like("name", companyName));
            if (companies == null || companies.isEmpty()) {
                // 无匹配公司，返回一个恒为空的条件
                return QueryWrapper.create().eq("id", -1);
            } else {
                Set<Long> ids = companies.stream().map(com.crossorgtalentmanager.model.entity.Company::getId)
                        .collect(Collectors.toSet());
                qw.in("company_id", ids);
            }
        }

        if (minAnnual != null) {
            qw.ge("annual_salary", minAnnual);
        }
        if (maxAnnual != null) {
            qw.le("annual_salary", maxAnnual);
        }

        return qw;
    }

    @Override
    public Boolean removeById(Long id) {
        EmployeeProfile profile = getById(id);
        ThrowUtils.throwIf(profile == null, ErrorCode.NOT_FOUND_ERROR);
        EmployeeProfile copy = new EmployeeProfile();
        BeanUtil.copyProperties(profile, copy);
        copy.setIsDelete(!Boolean.TRUE.equals(copy.getIsDelete()));
        return updateById(copy);
    }

    @Resource
    @Lazy
    private ProfileAccessRequestService profileAccessRequestService;

    @Override
    public EmployeeProfileVO getEmployeeProfileVOWithPermission(EmployeeProfile employeeProfile, Long viewerCompanyId,
            Long viewerEmployeeId) {
        if (employeeProfile == null) {
            return null;
        }

        EmployeeProfileVO vo = getEmployeeProfileVO(employeeProfile);
        if (vo == null) {
            return null;
        }

        // 如果查看者是员工本人，返回完整信息
        if (viewerEmployeeId != null && viewerEmployeeId.equals(employeeProfile.getEmployeeId())) {
            vo.setCanViewDetail(true);
            return vo;
        }

        // 查询员工信息
        com.crossorgtalentmanager.model.entity.Employee employee = employeeService
                .getById(employeeProfile.getEmployeeId());
        if (employee == null) {
            vo.setCanViewDetail(false);
            return vo;
        }

        // 如果查看者公司是员工所属公司，可以查看认证企业可见和公开的档案，但不能查看完全保密的档案
        boolean isSameCompany = viewerCompanyId != null && viewerCompanyId.equals(employee.getCompanyId());
        Integer visibility = employeeProfile.getVisibility();
        if (visibility == null) {
            visibility = 2; // 默认为公开
        }

        // 如果查看者公司是员工所属公司
        if (isSameCompany) {
            // 完全保密的档案，即使是同一公司也不能查看
            if (visibility == 0) {
                vo.setPerformanceSummary(null);
                vo.setAttendanceRate(null);
                vo.setHasMajorIncident(null);
                vo.setReasonForLeaving(null);
                vo.setOccupation(null);
                vo.setAnnualSalary(null);
                vo.setStartDate(null);
                vo.setEndDate(null);
                vo.setCanViewDetail(false);
                return vo;
            }
            // 认证企业可见和公开的档案可以查看
            vo.setCanViewDetail(true);
            return vo;
        }

        // 如果查看者公司不是员工所属公司，需要检查权限
        // 完全保密的档案，只显示有档案存在和入职离职日期，其他信息不显示
        if (visibility == 0) {
            vo.setPerformanceSummary(null);
            vo.setAttendanceRate(null);
            vo.setHasMajorIncident(null);
            vo.setReasonForLeaving(null);
            vo.setOccupation(null);
            vo.setAnnualSalary(null);
            // 入职和离职日期始终可见
            // vo.setStartDate(null);
            // vo.setEndDate(null);
            vo.setCanViewDetail(false);
            return vo;
        }

        // 对认证企业可见的档案，需要检查是否有已授权的请求
        if (visibility == 1) {
            boolean canAccess = profileAccessRequestService.canAccessProfile(
                    employeeProfile.getId(), viewerCompanyId, employeeProfile.getEmployeeId());
            if (!canAccess) {
                // 只显示有档案存在和入职离职日期，其他信息不显示
                vo.setPerformanceSummary(null);
                vo.setAttendanceRate(null);
                vo.setHasMajorIncident(null);
                vo.setReasonForLeaving(null);
                vo.setOccupation(null);
                vo.setAnnualSalary(null);
                // 入职和离职日期始终可见
                // vo.setStartDate(null);
                // vo.setEndDate(null);
                vo.setCanViewDetail(false);
                return vo;
            }
            vo.setCanViewDetail(true);
        } else {
            // 公开的档案可以查看
            vo.setCanViewDetail(true);
        }

        return vo;
    }

    @Override
    public Boolean canViewProfileDetail(Long profileId, Long viewerCompanyId, Long employeeId) {
        if (profileId == null || employeeId == null) {
            return false;
        }

        EmployeeProfile profile = this.getById(profileId);
        if (profile == null || !profile.getEmployeeId().equals(employeeId)) {
            return false;
        }

        // 如果查看者是员工本人，可以查看
        // 这里需要从viewerCompanyId推断viewerEmployeeId，暂时通过查询员工来判断
        com.crossorgtalentmanager.model.entity.Employee employee = employeeService.getById(employeeId);
        if (employee == null) {
            return false;
        }

        Integer visibility = profile.getVisibility();
        if (visibility == null) {
            visibility = 2; // 默认为公开
        }

        // 如果查看者公司是员工所属公司，可以查看认证企业可见和公开的档案，但不能查看完全保密的档案
        if (viewerCompanyId != null && viewerCompanyId.equals(employee.getCompanyId())) {
            // 完全保密的档案，即使是同一公司也不能查看
            if (visibility == 0) {
                return false;
            }
            // 认证企业可见和公开的档案可以查看
            return true;
        }

        // 如果查看者公司不是员工所属公司
        if (visibility == 0) {
            // 完全保密，不能查看
            return false;
        }
        if (visibility == 2) {
            // 公开，可以查看
            return true;
        }
        if (visibility == 1) {
            // 对认证企业可见，需要检查是否有已授权的请求
            return profileAccessRequestService.canAccessProfile(profileId, viewerCompanyId, employeeId);
        }

        return false;
    }

    @Override
    public com.mybatisflex.core.paginate.Page<EmployeeProfileVO> listEmployeeProfileVOByPageWithPermission(
            EmployeeProfileQueryRequest queryRequest, User loginUser) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            // 非系统管理员仅允许 HR 或 公司管理员 查询
            ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "无权限");

            Long loginCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

            // 如果指定了 employeeId，允许查询该员工在所有公司的档案（不限制 companyId）
            // 权限控制将在返回VO时根据档案公开范围和查看者公司进行判断
            Long employeeId = queryRequest.getEmployeeId();
            if (employeeId != null && employeeId > 0) {
                com.crossorgtalentmanager.model.entity.Employee employee = employeeService.getById(employeeId);
                ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");
                // 允许查询该员工在所有公司的档案，不限制 companyId
                // 权限控制将在返回VO时根据档案公开范围和查看者公司进行判断
                // 保持请求中的 companyId（如果指定了）或设置为 null（查看所有公司）
            } else {
                // 如果没有指定 employeeId，则只查询当前公司的档案
                Long requestCompanyId = queryRequest.getCompanyId();
                if (requestCompanyId != null && requestCompanyId.equals(loginCompanyId)) {
                    // 使用请求中的companyId（例如解雇流程中需要查询特定公司的档案）
                    queryRequest.setCompanyId(requestCompanyId);
                } else {
                    // 使用登录用户的companyId
                    queryRequest.setCompanyId(loginCompanyId);
                }
            }
        }

        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        com.mybatisflex.core.paginate.Page<EmployeeProfile> page = this.page(
                com.mybatisflex.core.paginate.Page.of(pageNum, pageSize),
                this.getQueryWrapper(queryRequest));
        com.mybatisflex.core.paginate.Page<EmployeeProfileVO> voPage = new com.mybatisflex.core.paginate.Page<>(
                pageNum, pageSize, page.getTotalRow());

        // 获取查看者信息
        Long viewerCompanyId = loginUser.getCompanyId();
        Long viewerEmployeeId = null;

        // 如果指定了employeeId，判断查看者是否是员工本人
        Long queryEmployeeId = queryRequest.getEmployeeId();
        if (queryEmployeeId != null && queryEmployeeId > 0) {
            // 查询员工信息，判断是否是查看者本人
            com.crossorgtalentmanager.model.entity.Employee queryEmployee = employeeService.getById(queryEmployeeId);
            if (queryEmployee != null && queryEmployee.getUserId() != null) {
                // 如果查询的员工是查看者本人，设置viewerEmployeeId
                if (queryEmployee.getUserId().equals(loginUser.getId())) {
                    viewerEmployeeId = queryEmployeeId;
                }
            }
        }

        // 应用权限控制和脱敏逻辑
        List<EmployeeProfileVO> voList = new ArrayList<>();
        for (EmployeeProfile profile : page.getRecords()) {
            EmployeeProfileVO vo = this.getEmployeeProfileVOWithPermission(
                    profile, viewerCompanyId, viewerEmployeeId);
            if (vo != null) {
                voList.add(vo);
            }
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public EmployeeProfileVO getEmployeeProfileVOByIdWithPermission(Long id, User loginUser) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "档案 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        EmployeeProfile profile = this.getById(id);
        ThrowUtils.throwIf(profile == null, ErrorCode.NOT_FOUND_ERROR, "档案不存在");

        // 获取查看者信息
        Long viewerCompanyId = loginUser.getCompanyId();

        // 判断查看者是否是员工本人
        Long viewerEmployeeId = null;
        com.crossorgtalentmanager.model.entity.Employee employee = employeeService.getById(profile.getEmployeeId());
        if (employee != null && employee.getUserId() != null && employee.getUserId().equals(loginUser.getId())) {
            viewerEmployeeId = employee.getId();
        }

        // 应用权限控制和脱敏逻辑
        return this.getEmployeeProfileVOWithPermission(profile, viewerCompanyId, viewerEmployeeId);
    }

}
