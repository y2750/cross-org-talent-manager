package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileAddRequest;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileQueryRequest;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileUpdateRequest;
import com.crossorgtalentmanager.model.entity.EmployeeProfile;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.EmployeeProfileVO;
import com.crossorgtalentmanager.service.EmployeeProfileService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;

/**
 * 员工档案信息 控制层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@RestController
@RequestMapping("/employeeProfile")
public class EmployeeProfileController {

    @Resource
    private EmployeeProfileService employeeProfileService;

    @Resource
    private UserService userService;

    /**
     * 添加员工档案（HR）。
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> addEmployeeProfile(@RequestBody EmployeeProfileAddRequest addRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long result = employeeProfileService.addEmployeeProfile(addRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 更新员工档案（HR）。
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> updateEmployeeProfile(@RequestBody EmployeeProfileUpdateRequest updateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        employeeProfileService.updateEmployeeProfile(updateRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 逻辑删除档案（管理员）
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteEmployeeProfile(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new com.crossorgtalentmanager.exception.BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = employeeProfileService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取员工档案
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<EmployeeProfile> getEmployeeProfileById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        EmployeeProfile profile = employeeProfileService.getById(id);
        ThrowUtils.throwIf(profile == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(profile);
    }

    /**
     * 根据 id 获取员工档案包装类（VO）
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<EmployeeProfileVO> getEmployeeProfileVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        EmployeeProfile profile = employeeProfileService.getById(id);
        ThrowUtils.throwIf(profile == null, ErrorCode.NOT_FOUND_ERROR);
        EmployeeProfileVO vo = employeeProfileService.getEmployeeProfileVO(profile);
        return ResultUtils.success(vo);
    }

    /**
     * 分页查询员工档案（返回 VO 列表）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<EmployeeProfileVO>> listEmployeeProfileVOByPage(
            @RequestBody EmployeeProfileQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);

        // 验证登录用户并根据用户角色拼接公司过滤条件
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            // 非系统管理员仅允许 HR 或 公司管理员 查询本公司数据
            ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "无权限");
            queryRequest.setCompanyId(loginUser.getCompanyId());
        }

        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        Page<EmployeeProfile> page = employeeProfileService.page(Page.of(pageNum, pageSize),
                employeeProfileService.getQueryWrapper(queryRequest));
        Page<EmployeeProfileVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<EmployeeProfileVO> voList = employeeProfileService.getEmployeeProfileVOList(page.getRecords());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

}
