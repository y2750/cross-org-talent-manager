package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestAddRequest;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestQueryRequest;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestUpdateRequest;
import com.crossorgtalentmanager.model.entity.ProfileAccessRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.ProfileAccessRequestVO;
import com.crossorgtalentmanager.service.ProfileAccessRequestService;
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
 * 档案查阅请求 控制层
 */
@RestController
@RequestMapping("/profileAccessRequest")
public class ProfileAccessRequestController {

    @Resource
    private ProfileAccessRequestService profileAccessRequestService;

    @Resource
    private UserService userService;

    @Resource
    private EmployeeService employeeService;

    /**
     * 创建档案查阅请求（HR/公司管理员）
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> createRequest(@RequestBody ProfileAccessRequestAddRequest addRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long result = profileAccessRequestService.createRequest(addRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 审批档案查阅请求（员工）
     */
    @PutMapping("/approve")
    public BaseResponse<Boolean> approveRequest(@RequestBody ProfileAccessRequestUpdateRequest updateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Boolean result = profileAccessRequestService.approveRequest(updateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询档案查阅请求
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ProfileAccessRequestVO>> listRequestByPage(
            @RequestBody ProfileAccessRequestQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 根据用户角色设置查询条件
        // 员工只能查看自己的请求，HR/公司管理员可以查看自己公司的请求
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
        Page<ProfileAccessRequest> page = profileAccessRequestService.page(
                Page.of(pageNum, pageSize),
                profileAccessRequestService.getQueryWrapper(queryRequest));
        Page<ProfileAccessRequestVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<ProfileAccessRequestVO> voList = profileAccessRequestService
                .getProfileAccessRequestVOList(page.getRecords());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

    /**
     * 根据ID获取请求详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<ProfileAccessRequestVO> getRequestVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ProfileAccessRequest requestEntity = profileAccessRequestService.getById(id);
        ThrowUtils.throwIf(requestEntity == null, ErrorCode.NOT_FOUND_ERROR);
        ProfileAccessRequestVO vo = profileAccessRequestService.getProfileAccessRequestVO(requestEntity);
        return ResultUtils.success(vo);
    }
}
