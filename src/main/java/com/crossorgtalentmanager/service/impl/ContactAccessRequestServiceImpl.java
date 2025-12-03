package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.ContactAccessRequestMapper;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestAddRequest;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestQueryRequest;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestUpdateRequest;
import com.crossorgtalentmanager.model.entity.*;
import com.crossorgtalentmanager.model.enums.ContactAccessRequestStatusEnum;
import com.crossorgtalentmanager.model.enums.ContactAccessRequestTypeEnum;
import com.crossorgtalentmanager.model.enums.NotificationTypeEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.ContactAccessRequestVO;
import com.crossorgtalentmanager.service.*;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 联系方式查看请求 服务层实现
 */
@Service
@Slf4j
public class ContactAccessRequestServiceImpl extends ServiceImpl<ContactAccessRequestMapper, ContactAccessRequest>
        implements ContactAccessRequestService {

    @Resource
    private EmployeeService employeeService;

    @Resource
    private CompanyService companyService;

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRequest(ContactAccessRequestAddRequest addRequest, User loginUser) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");
        ThrowUtils.throwIf(addRequest.getEmployeeId() == null || addRequest.getEmployeeId() <= 0,
                ErrorCode.PARAMS_ERROR, "员工 ID 不能为空");
        ThrowUtils.throwIf(addRequest.getRequestType() == null,
                ErrorCode.PARAMS_ERROR, "请求类型不能为空");
        ThrowUtils.throwIf(!ContactAccessRequestTypeEnum.isValidValue(addRequest.getRequestType()),
                ErrorCode.PARAMS_ERROR, "请求类型无效");

        // 权限校验：只有HR或公司管理员可以创建请求
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "只有HR或公司管理员可以创建查看请求");

        Long requestCompanyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(requestCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

        // 查询员工信息
        Employee employee = employeeService.getById(addRequest.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 不能请求查看自己公司的员工联系方式
        ThrowUtils.throwIf(requestCompanyId.equals(employee.getCompanyId()),
                ErrorCode.PARAMS_ERROR, "不能请求查看自己公司的员工联系方式");

        // 检查是否有未处理的请求（同一公司和员工，状态为待处理）
        QueryWrapper pendingQuery = QueryWrapper.create()
                .eq("request_company_id", requestCompanyId)
                .eq("employee_id", addRequest.getEmployeeId())
                .eq("status", ContactAccessRequestStatusEnum.PENDING.getValue());
        long pendingCount = this.count(pendingQuery);
        ThrowUtils.throwIf(pendingCount > 0,
                ErrorCode.OPERATION_ERROR, "已有未处理的联系方式申请请求，请等待处理完成后再申请");

        // 设置过期时间：如果没有指定，默认为7天后
        LocalDateTime expireTime = addRequest.getExpireTime();
        if (expireTime == null) {
            expireTime = LocalDateTime.now().plusDays(7);
        }

        // 创建请求
        ContactAccessRequest request = new ContactAccessRequest();
        request.setRequestCompanyId(requestCompanyId);
        request.setEmployeeId(addRequest.getEmployeeId());
        request.setRequestType(addRequest.getRequestType());
        request.setRequestReason(addRequest.getRequestReason());
        request.setStatus(ContactAccessRequestStatusEnum.PENDING.getValue());
        request.setRequestTime(LocalDateTime.now());
        request.setExpireTime(expireTime);

        boolean save = this.save(request);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建请求失败");

        log.info("创建联系方式查看请求：requestId={}, requestCompanyId={}, employeeId={}, requestType={}",
                request.getId(), requestCompanyId, addRequest.getEmployeeId(), addRequest.getRequestType());

        // 向员工本人发送联系方式查阅请求通知
        if (employee.getUserId() != null) {
            Company requestCompany = companyService.getById(requestCompanyId);
            String companyName = requestCompany != null ? requestCompany.getName() : "某企业";
            ContactAccessRequestTypeEnum typeEnum = ContactAccessRequestTypeEnum
                    .getEnumByValue(addRequest.getRequestType());
            String typeText = typeEnum != null ? typeEnum.getText() : "联系方式";
            String title = "联系方式查看请求通知";
            String reason = addRequest.getRequestReason() != null ? addRequest.getRequestReason() : "无";
            // 计算授权天数（根据过期时间计算，默认7天）
            long authDays = 7;
            if (request.getExpireTime() != null) {
                long daysBetween = ChronoUnit.DAYS.between(
                        LocalDateTime.now(), request.getExpireTime());
                if (daysBetween > 0) {
                    authDays = daysBetween;
                }
            }
            String content = String.format("企业【%s】申请查看你的【%s】，原因：%s，授权天数：%d天",
                    companyName, typeText, reason, authDays);
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
    public Boolean approveRequest(ContactAccessRequestUpdateRequest updateRequest, User loginUser) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "请求 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");
        ThrowUtils.throwIf(updateRequest.getStatus() == null,
                ErrorCode.PARAMS_ERROR, "状态不能为空");

        // 权限校验：只有员工本人可以审批
        ThrowUtils.throwIf(!UserRoleEnum.EMPLOYEE.getValue().equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "只有员工本人可以审批查看请求");

        // 查询请求
        ContactAccessRequest request = this.getById(updateRequest.getId());
        ThrowUtils.throwIf(request == null, ErrorCode.NOT_FOUND_ERROR, "请求不存在");
        ThrowUtils.throwIf(!ContactAccessRequestStatusEnum.PENDING.getValue().equals(request.getStatus()),
                ErrorCode.PARAMS_ERROR, "只能处理待处理的请求");

        // 验证员工身份
        Employee employee = employeeService.getById(request.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");
        ThrowUtils.throwIf(!employee.getUserId().equals(loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR, "只能审批自己的查看请求");

        // 验证状态值
        Integer status = updateRequest.getStatus();
        ThrowUtils.throwIf(!status.equals(ContactAccessRequestStatusEnum.APPROVED.getValue()) &&
                !status.equals(ContactAccessRequestStatusEnum.REJECTED.getValue()),
                ErrorCode.PARAMS_ERROR, "状态值无效");

        // 更新请求状态和响应时间
        request.setStatus(status);
        LocalDateTime responseTime = LocalDateTime.now();
        request.setResponseTime(responseTime);

        // 如果授权，设置授权过期时间为响应时间加7天
        if (status.equals(ContactAccessRequestStatusEnum.APPROVED.getValue())) {
            LocalDateTime expireTime;
            // 如果前端提供了过期时间，使用前端提供的（但要校验有效性）
            if (updateRequest.getExpireTime() != null && updateRequest.getExpireTime().isAfter(responseTime)) {
                expireTime = updateRequest.getExpireTime();
            } else {
                // 默认使用响应时间加7天
                expireTime = responseTime.plusDays(7);
            }
            request.setExpireTime(expireTime);
        }

        boolean update = this.updateById(request);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新请求失败");

        log.info("审批联系方式查看请求：requestId={}, status={}, employeeId={}",
                request.getId(), status, request.getEmployeeId());

        // 向请求企业的相关用户发送审批结果通知（HR、企业管理员）
        if (request.getRequestCompanyId() != null) {
            List<User> receivers = userService.list(
                    QueryWrapper.create()
                            .eq("company_id", request.getRequestCompanyId())
                            .in("user_role",
                                    UserRoleEnum.HR.getValue(),
                                    UserRoleEnum.COMPANY_ADMIN.getValue()));
            if (receivers != null && !receivers.isEmpty()) {
                String statusText = status.equals(ContactAccessRequestStatusEnum.APPROVED.getValue())
                        ? "已同意"
                        : "已拒绝";
                ContactAccessRequestTypeEnum typeEnum = ContactAccessRequestTypeEnum
                        .getEnumByValue(request.getRequestType());
                String typeText = typeEnum != null ? typeEnum.getText() : "联系方式";
                String title = "联系方式查看请求结果通知";
                String employeeName = employee.getName();
                String content = String.format("你发起的查看员工【%s】【%s】的请求%s。", employeeName, typeText, statusText);
                // 如果同意，在通知内容中包含员工ID，方便前端跳转
                if (status.equals(ContactAccessRequestStatusEnum.APPROVED.getValue())) {
                    content += String.format("\n员工ID：%s", request.getEmployeeId());
                }
                for (User receiver : receivers) {
                    // 如果同意，relatedId 存储员工ID，方便前端跳转到人才详情页面
                    Long relatedId = status.equals(ContactAccessRequestStatusEnum.APPROVED.getValue())
                            ? request.getEmployeeId()
                            : request.getId();
                    notificationService.createNotification(
                            receiver.getId(),
                            NotificationTypeEnum.ACCESS_REQUEST.getValue(),
                            title,
                            content,
                            relatedId);
                }
            }
        }

        return true;
    }

    @Override
    public ContactAccessRequestVO getContactAccessRequestVO(ContactAccessRequest request) {
        if (request == null) {
            return null;
        }
        ContactAccessRequestVO vo = new ContactAccessRequestVO();
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

        // 填充请求类型文本
        ContactAccessRequestTypeEnum typeEnum = ContactAccessRequestTypeEnum.getEnumByValue(request.getRequestType());
        if (typeEnum != null) {
            vo.setRequestTypeText(typeEnum.getText());
        }

        // 填充状态文本
        ContactAccessRequestStatusEnum statusEnum = ContactAccessRequestStatusEnum.getEnumByValue(request.getStatus());
        if (statusEnum != null) {
            vo.setStatusText(statusEnum.getText());
        }

        return vo;
    }

    @Override
    public List<ContactAccessRequestVO> getContactAccessRequestVOList(List<ContactAccessRequest> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 批量查询企业和员工信息
        Set<Long> companyIds = list.stream()
                .map(ContactAccessRequest::getRequestCompanyId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> employeeIds = list.stream()
                .map(ContactAccessRequest::getEmployeeId)
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
            ContactAccessRequestVO vo = getContactAccessRequestVO(request);
            if (vo != null) {
                vo.setRequestCompanyName(finalCompanyMap.get(request.getRequestCompanyId()));
                vo.setEmployeeName(finalEmployeeMap.get(request.getEmployeeId()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(ContactAccessRequestQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        QueryWrapper qw = QueryWrapper.create()
                .eq("id", request.getId())
                .eq("request_company_id", request.getRequestCompanyId())
                .eq("employee_id", request.getEmployeeId())
                .eq("request_type", request.getRequestType())
                .eq("status", request.getStatus())
                .orderBy(request.getSortField(), "ascend".equals(request.getSortOrder()));

        return qw;
    }

    @Override
    public Boolean canAccessContact(Long employeeId, Integer requestType, Long requestCompanyId) {
        if (employeeId == null || requestType == null || requestCompanyId == null) {
            return false;
        }

        // 查询员工
        Employee employee = employeeService.getById(employeeId);
        if (employee == null) {
            return false;
        }

        // 如果员工属于请求公司，可以直接查看
        if (requestCompanyId.equals(employee.getCompanyId())) {
            return true;
        }

        // 如果员工不属于请求公司，需要检查是否有已授权的请求
        return hasAuthorizedAccess(requestCompanyId, employeeId, requestType);
    }

    @Override
    public Boolean hasAuthorizedAccess(Long companyId, Long employeeId, Integer requestType) {
        if (companyId == null || employeeId == null || requestType == null) {
            return false;
        }

        // 检查是否有已授权且未过期的请求
        // 如果请求类型为4（查看所有联系方式），则也检查是否有其他类型的授权
        QueryWrapper qw = QueryWrapper.create()
                .eq("request_company_id", companyId)
                .eq("employee_id", employeeId)
                .eq("status", ContactAccessRequestStatusEnum.APPROVED.getValue())
                .ge("expire_time", LocalDateTime.now());

        if (requestType.equals(4)) {
            // 查看所有联系方式，只要有任意类型的授权就可以
            qw.in("request_type", 1, 2, 3, 4, 5);
        } else if (requestType.equals(5)) {
            // 查看电话和邮箱，需要检查 type 5，或者同时有 type 1 和 type 2，或者 type 4
            qw.in("request_type", 4, 5);
            long count = this.count(qw);
            if (count > 0) {
                return true;
            }
            // 检查是否同时有 type 1 和 type 2 的授权
            QueryWrapper phoneQuery = QueryWrapper.create()
                    .eq("request_company_id", companyId)
                    .eq("employee_id", employeeId)
                    .eq("status", ContactAccessRequestStatusEnum.APPROVED.getValue())
                    .ge("expire_time", LocalDateTime.now())
                    .eq("request_type", 1);
            QueryWrapper emailQuery = QueryWrapper.create()
                    .eq("request_company_id", companyId)
                    .eq("employee_id", employeeId)
                    .eq("status", ContactAccessRequestStatusEnum.APPROVED.getValue())
                    .ge("expire_time", LocalDateTime.now())
                    .eq("request_type", 2);
            return this.count(phoneQuery) > 0 && this.count(emailQuery) > 0;
        } else if (requestType.equals(1) || requestType.equals(2)) {
            // 查看电话或邮箱，需要检查该类型，或者 type 4，或者 type 5
            qw.in("request_type", requestType, 4, 5);
        } else {
            // 其他类型（如身份证号），需要检查该类型或 type 4
            qw.in("request_type", requestType, 4);
        }

        long count = this.count(qw);
        return count > 0;
    }
}
