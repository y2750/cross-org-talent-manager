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
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
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

    @Override
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

        boolean save = this.save(profile);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "添加失败");
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

}
