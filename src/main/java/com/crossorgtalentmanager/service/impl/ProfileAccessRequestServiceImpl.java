package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.ProfileAccessRequestMapper;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestAddRequest;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestQueryRequest;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestUpdateRequest;
import com.crossorgtalentmanager.model.entity.*;
import com.crossorgtalentmanager.model.enums.NotificationTypeEnum;
import com.crossorgtalentmanager.model.enums.ProfileAccessRequestStatusEnum;
import com.crossorgtalentmanager.model.enums.ProfileVisibilityEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.ProfileAccessRequestVO;
import com.crossorgtalentmanager.service.*;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 档案查阅请求 服务层实现
 */
@Service("ProfileAccessRequestService")
@Slf4j
public class ProfileAccessRequestServiceImpl extends ServiceImpl<ProfileAccessRequestMapper, ProfileAccessRequest>
        implements ProfileAccessRequestService {

    @Resource
    private EmployeeService employeeService;

    @Resource
    private EmployeeProfileService employeeProfileService;

    @Resource
    private CompanyService companyService;

    @Resource
    private UserService userService;

    @Resource
    private ProfileAccessLogService profileAccessLogService;

    @Resource
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRequest(ProfileAccessRequestAddRequest addRequest, User loginUser) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");
        ThrowUtils.throwIf(addRequest.getEmployeeId() == null || addRequest.getEmployeeId() <= 0,
                ErrorCode.PARAMS_ERROR, "员工 ID 不能为空");

        // 权限校验：只有HR或公司管理员可以创建请求
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "只有HR或公司管理员可以创建查阅请求");

        Long requestCompanyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(requestCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

        // 查询员工信息
        Employee employee = employeeService.getById(addRequest.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 不能请求查看自己公司的员工档案
        ThrowUtils.throwIf(requestCompanyId.equals(employee.getCompanyId()),
                ErrorCode.PARAMS_ERROR, "不能请求查看自己公司的员工档案");

        // 如果指定了档案ID，验证档案的可见性
        if (addRequest.getEmployeeProfileId() != null) {
            EmployeeProfile profile = employeeProfileService.getById(addRequest.getEmployeeProfileId());
            ThrowUtils.throwIf(profile == null, ErrorCode.NOT_FOUND_ERROR, "档案不存在");
            ThrowUtils.throwIf(!profile.getEmployeeId().equals(addRequest.getEmployeeId()),
                    ErrorCode.PARAMS_ERROR, "档案与员工不匹配");

            // 只能请求查看"对认证企业可见"的档案
            Integer visibility = profile.getVisibility();
            if (visibility == null) {
                visibility = ProfileVisibilityEnum.PUBLIC.getValue(); // 默认为公开
            }
            ThrowUtils.throwIf(!visibility.equals(ProfileVisibilityEnum.COMPANY_VISIBLE.getValue()),
                    ErrorCode.PARAMS_ERROR, "只能请求查看对认证企业可见的档案");
        }

        // 创建请求
        ProfileAccessRequest request = new ProfileAccessRequest();
        request.setRequestCompanyId(requestCompanyId);
        request.setRequestUserId(loginUser.getId()); // 保存请求用户ID
        request.setEmployeeId(addRequest.getEmployeeId());
        request.setEmployeeProfileId(addRequest.getEmployeeProfileId());
        request.setRequestReason(addRequest.getRequestReason());
        request.setStatus(ProfileAccessRequestStatusEnum.PENDING.getValue());
        request.setRequestTime(LocalDateTime.now());
        request.setExpireTime(addRequest.getExpireTime());

        boolean save = this.save(request);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建请求失败");

        log.info("创建档案查阅请求：requestId={}, requestCompanyId={}, employeeId={}, profileId={}",
                request.getId(), requestCompanyId, addRequest.getEmployeeId(), addRequest.getEmployeeProfileId());

        // 向员工本人发送查阅请求通知
        if (employee.getUserId() != null) {
            Company requestCompany = companyService.getById(requestCompanyId);
            String companyName = requestCompany != null ? requestCompany.getName() : "某企业";
            String title = "档案查阅请求通知";
            String reason = addRequest.getRequestReason() != null ? addRequest.getRequestReason() : "无";
            String content = String.format("企业【%s】申请查阅你的工作档案，原因：%s", companyName, reason);
            notificationService.createNotification(
                    employee.getUserId(),
                    NotificationTypeEnum.ACCESS_REQUEST.getValue(),
                    title,
                    content,
                    request.getId());
        }

        return request.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveRequest(ProfileAccessRequestUpdateRequest updateRequest, User loginUser) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "请求 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");
        ThrowUtils.throwIf(updateRequest.getStatus() == null,
                ErrorCode.PARAMS_ERROR, "状态不能为空");

        // 权限校验：只有员工本人可以审批
        ThrowUtils.throwIf(!UserRoleEnum.EMPLOYEE.getValue().equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "只有员工本人可以审批查阅请求");

        // 查询请求
        ProfileAccessRequest request = this.getById(updateRequest.getId());
        ThrowUtils.throwIf(request == null, ErrorCode.NOT_FOUND_ERROR, "请求不存在");
        ThrowUtils.throwIf(!ProfileAccessRequestStatusEnum.PENDING.getValue().equals(request.getStatus()),
                ErrorCode.PARAMS_ERROR, "只能处理待处理的请求");

        // 验证员工身份
        Employee employee = employeeService.getById(request.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");
        ThrowUtils.throwIf(!employee.getUserId().equals(loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR, "只能审批自己的查阅请求");

        // 验证状态值
        Integer status = updateRequest.getStatus();
        ThrowUtils.throwIf(!status.equals(ProfileAccessRequestStatusEnum.APPROVED.getValue()) &&
                !status.equals(ProfileAccessRequestStatusEnum.REJECTED.getValue()),
                ErrorCode.PARAMS_ERROR, "状态值无效");

        // 如果授权，必须提供过期时间
        if (status.equals(ProfileAccessRequestStatusEnum.APPROVED.getValue())) {
            ThrowUtils.throwIf(updateRequest.getExpireTime() == null,
                    ErrorCode.PARAMS_ERROR, "授权时必须提供过期时间");
            ThrowUtils.throwIf(updateRequest.getExpireTime().isBefore(LocalDateTime.now()),
                    ErrorCode.PARAMS_ERROR, "过期时间不能早于当前时间");
        }

        // 更新请求
        request.setStatus(status);
        request.setResponseTime(LocalDateTime.now());
        if (status.equals(ProfileAccessRequestStatusEnum.APPROVED.getValue())) {
            request.setExpireTime(updateRequest.getExpireTime());
        }

        boolean update = this.updateById(request);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新请求失败");

        log.info("审批档案查阅请求：requestId={}, status={}, employeeId={}",
                request.getId(), status, request.getEmployeeId());

        // 向请求用户发送审批结果通知（仅通知创建请求的用户）
        if (request.getRequestUserId() != null) {
            String statusText = status.equals(ProfileAccessRequestStatusEnum.APPROVED.getValue())
                    ? "已同意"
                    : "已拒绝";
            String title = "档案查阅请求结果通知";
            String employeeName = employee.getName();
            String content = String.format("你发起的对员工【%s】的档案查阅请求%s。", employeeName, statusText);
            notificationService.createNotification(
                    request.getRequestUserId(),
                    NotificationTypeEnum.ACCESS_REQUEST.getValue(),
                    title,
                    content,
                    request.getId());
        }

        return true;
    }

    @Override
    public ProfileAccessRequestVO getProfileAccessRequestVO(ProfileAccessRequest request) {
        if (request == null) {
            return null;
        }
        ProfileAccessRequestVO vo = new ProfileAccessRequestVO();
        BeanUtil.copyProperties(request, vo);

        // 填充企业名称
        if (request.getRequestCompanyId() != null) {
            Company company = companyService.getById(request.getRequestCompanyId());
            if (company != null) {
                vo.setRequestCompanyName(company.getName());
            }
        }

        // 填充员工姓名
        if (request.getEmployeeId() != null) {
            Employee employee = employeeService.getById(request.getEmployeeId());
            if (employee != null) {
                vo.setEmployeeName(employee.getName());
            }
        }

        // 填充状态文本
        ProfileAccessRequestStatusEnum statusEnum = ProfileAccessRequestStatusEnum.getEnumByValue(request.getStatus());
        if (statusEnum != null) {
            vo.setStatusText(statusEnum.getText());
        }

        return vo;
    }

    @Override
    public List<ProfileAccessRequestVO> getProfileAccessRequestVOList(List<ProfileAccessRequest> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 批量查询企业和员工信息
        Set<Long> companyIds = list.stream()
                .map(ProfileAccessRequest::getRequestCompanyId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> employeeIds = list.stream()
                .map(ProfileAccessRequest::getEmployeeId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> companyIdToName = new java.util.HashMap<>();
        if (!companyIds.isEmpty()) {
            List<Company> companies = companyService.listByIds(companyIds);
            companyIdToName = companies.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(Company::getId, Company::getName, (a, b) -> a));
        }

        Map<Long, String> employeeIdToName = new java.util.HashMap<>();
        if (!employeeIds.isEmpty()) {
            List<Employee> employees = employeeService.listByIds(employeeIds);
            employeeIdToName = employees.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(Employee::getId, Employee::getName, (a, b) -> a));
        }

        // 转换VO
        final Map<Long, String> finalCompanyMap = companyIdToName;
        final Map<Long, String> finalEmployeeMap = employeeIdToName;
        return list.stream().map(request -> {
            ProfileAccessRequestVO vo = getProfileAccessRequestVO(request);
            if (vo != null) {
                vo.setRequestCompanyName(finalCompanyMap.get(request.getRequestCompanyId()));
                vo.setEmployeeName(finalEmployeeMap.get(request.getEmployeeId()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(ProfileAccessRequestQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        QueryWrapper qw = QueryWrapper.create()
                .eq("id", request.getId())
                .eq("request_company_id", request.getRequestCompanyId())
                .eq("employee_id", request.getEmployeeId())
                .eq("employee_profile_id", request.getEmployeeProfileId())
                .eq("status", request.getStatus())
                .orderBy(request.getSortField(), "ascend".equals(request.getSortOrder()));

        return qw;
    }

    @Override
    public Boolean canAccessProfile(Long profileId, Long requestCompanyId, Long employeeId) {
        if (profileId == null || requestCompanyId == null || employeeId == null) {
            return false;
        }

        // 查询档案
        EmployeeProfile profile = employeeProfileService.getById(profileId);
        if (profile == null || !profile.getEmployeeId().equals(employeeId)) {
            return false;
        }

        // 查询员工
        Employee employee = employeeService.getById(employeeId);
        if (employee == null) {
            return false;
        }

        // 注意：此方法只在查看者公司不是员工所属公司时被调用
        // 如果员工属于请求公司，应该在调用此方法之前就被处理了（同一公司可以查看所有档案）
        // 但为了安全起见，这里仍然检查：如果员工属于请求公司，直接返回true
        if (requestCompanyId.equals(employee.getCompanyId())) {
            // 同一公司，可以查看所有档案
            return true;
        }

        // 如果员工不属于请求公司，需要检查档案的公开范围和授权状态
        Integer visibility = profile.getVisibility();
        if (visibility == null) {
            visibility = ProfileVisibilityEnum.PUBLIC.getValue();
        }

        // 完全保密的档案，不能查看
        if (visibility.equals(ProfileVisibilityEnum.PRIVATE.getValue())) {
            return false;
        }

        // 公开的档案可以直接查看
        if (visibility.equals(ProfileVisibilityEnum.PUBLIC.getValue())) {
            return true;
        }

        // 对认证企业可见的档案，需要检查是否有已授权的请求
        if (visibility.equals(ProfileVisibilityEnum.COMPANY_VISIBLE.getValue())) {
            QueryWrapper qw = QueryWrapper.create()
                    .eq("request_company_id", requestCompanyId)
                    .eq("employee_id", employeeId)
                    .eq("employee_profile_id", profileId)
                    .eq("status", ProfileAccessRequestStatusEnum.APPROVED.getValue())
                    .ge("expire_time", LocalDateTime.now());
            long count = this.count(qw);
            return count > 0;
        }

        return false;
    }
}
