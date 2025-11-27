package com.crossorgtalentmanager.controller;

import cn.hutool.core.bean.BeanUtil;
import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import com.crossorgtalentmanager.model.dto.user.UserLoginRequest;
import com.crossorgtalentmanager.model.dto.user.UserQueryRequest;
import com.crossorgtalentmanager.model.dto.user.UserRegisterRequest;
import com.crossorgtalentmanager.model.dto.user.UserUpdateRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.LoginUserVO;
import com.crossorgtalentmanager.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.crossorgtalentmanager.service.UserService;

import java.util.List;

/**
 * 系统用户管理 控制层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUsername();
        String userRole = userRegisterRequest.getUserRole();
        String nickname = userRegisterRequest.getNickname();
        String userPassword = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        long result = userService.userRegister(userAccount, userRole, nickname, userPassword, checkPassword);
        if (result > 0 && !loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            // 非系统管理员注册的用户，设置所属公司
            User newUser = new User();
            newUser.setId(result);
            newUser.setCompanyId(loginUser.getCompanyId());
            userService.updateById(newUser);
        }
        if (userRegisterRequest.getCompanyId() != null) {
            User newUser = new User();
            newUser.setId(result);
            newUser.setCompanyId(userRegisterRequest.getCompanyId());
            userService.updateById(newUser);
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String username = userLoginRequest.getUsername();
        String password = userLoginRequest.getPassword();
        LoginUserVO loginUserVO = userService.userLogin(username, password, request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 启用或禁用用户
     */
    @PostMapping("/toggle")
    public BaseResponse<Boolean> toggleUserStatus(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        log.info("toggleUserStatus: {}", deleteRequest);
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        boolean isSystemAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserConstant.COMPANY_ADMIN_ROLE.equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isSystemAdmin && !isCompanyAdmin, ErrorCode.NO_AUTH_ERROR);

        User targetUser = userService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR);

        if (isCompanyAdmin) {
            ThrowUtils.throwIf(!UserConstant.HR_ROLE.equals(targetUser.getUserRole()),
                    ErrorCode.NO_AUTH_ERROR, "仅可管理 HR 用户");
            ThrowUtils.throwIf(targetUser.getCompanyId() == null
                    || !targetUser.getCompanyId().equals(loginUser.getCompanyId()),
                    ErrorCode.NO_AUTH_ERROR, "仅可管理本公司 HR");
        }

        User updateUser = new User();
        updateUser.setId(targetUser.getId());
        Boolean currentDeleteStatus = Boolean.TRUE.equals(targetUser.getIsDelete());
        updateUser.setIsDelete(!currentDeleteStatus);
        boolean result = userService.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.COMPANY_ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        log.info("userQueryRequest: {}", userQueryRequest);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        User loginUser = userService.getLoginUser(request);
        if (loginUser.getUserRole().equals(UserConstant.COMPANY_ADMIN_ROLE)) {
            log.info("公司管理员只能查看本公司用户，companyId={}", loginUser.getCompanyId());
            // 公司管理员只能查看本公司用户
            userQueryRequest.setCompanyId(loginUser.getCompanyId());
        }
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        log.info("userVOPage: {}", userVOPage);
        return ResultUtils.success(userVOPage);
    }

}
