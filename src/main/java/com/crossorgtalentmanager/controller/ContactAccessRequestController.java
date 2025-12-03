package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestAddRequest;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestQueryRequest;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestUpdateRequest;
import com.crossorgtalentmanager.model.entity.ContactAccessRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.ContactAccessRequestVO;
import com.crossorgtalentmanager.service.ContactAccessRequestService;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.annotation.AuthCheck;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 联系方式查看请求 控制层
 */
@RestController
@RequestMapping("/contactAccessRequest")
public class ContactAccessRequestController {

    @Resource
    private ContactAccessRequestService contactAccessRequestService;

    @Resource
    private UserService userService;

    @Resource
    private EmployeeService employeeService;

    /**
     * 创建联系方式查看请求（HR/公司管理员）
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> createRequest(@RequestBody ContactAccessRequestAddRequest addRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long result = contactAccessRequestService.createRequest(addRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 审批联系方式查看请求（员工）
     */
    @PutMapping("/approve")
    public BaseResponse<Boolean> approveRequest(@RequestBody ContactAccessRequestUpdateRequest updateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Boolean result = contactAccessRequestService.approveRequest(updateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询联系方式查看请求
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ContactAccessRequestVO>> listRequestByPage(
            @RequestBody ContactAccessRequestQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 根据用户角色设置查询条件
        if ("employee".equals(loginUser.getUserRole())) {
            // 员工：查询自己的请求
            com.crossorgtalentmanager.model.entity.Employee employee = employeeService.list(
                    QueryWrapper.create().eq("user_id", loginUser.getId())).stream()
                    .findFirst().orElse(null);
            if (employee != null) {
                queryRequest.setEmployeeId(employee.getId());
            }
        } else if ("hr".equals(loginUser.getUserRole()) || "company_admin".equals(loginUser.getUserRole())) {
            // HR/公司管理员：查询自己公司的请求
            queryRequest.setRequestCompanyId(loginUser.getCompanyId());
        }

        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        Page<ContactAccessRequest> page = contactAccessRequestService.page(
                Page.of(pageNum, pageSize),
                contactAccessRequestService.getQueryWrapper(queryRequest));
        Page<ContactAccessRequestVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<ContactAccessRequestVO> voList = contactAccessRequestService
                .getContactAccessRequestVOList(page.getRecords());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

    /**
     * 根据ID获取请求详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<ContactAccessRequestVO> getRequestVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ContactAccessRequest requestEntity = contactAccessRequestService.getById(id);
        ThrowUtils.throwIf(requestEntity == null, ErrorCode.NOT_FOUND_ERROR);
        ContactAccessRequestVO vo = contactAccessRequestService.getContactAccessRequestVO(requestEntity);
        return ResultUtils.success(vo);
    }
}
