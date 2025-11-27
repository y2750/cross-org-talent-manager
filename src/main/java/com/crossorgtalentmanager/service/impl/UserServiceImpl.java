package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import com.crossorgtalentmanager.model.dto.user.UserQueryRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.mapper.UserMapper;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.LoginUserVO;
import com.crossorgtalentmanager.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.crossorgtalentmanager.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 系统用户管理 服务层实现。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Service("userService")
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public long userRegister(String username, String userRole, String nickname, String password, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(username, password, checkPassword, nickname)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (UserRoleEnum.getEnumByValue(userRole) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户角色错误");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(password);
        // 4. 插入数据
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword);
        user.setNickname(nickname);
        user.setUserRole(UserRoleEnum.getEnumByValue(userRole).getValue());

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "ycrossorg";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String username, String password, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(password);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", encryptPassword);
        queryWrapper.eq("is_delete", 0);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4. 获得脱敏后的用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String username = userQueryRequest.getUsername();
        String nickname = userQueryRequest.getNickname();
        Long companyId = userQueryRequest.getCompanyId();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 字段名映射：Java字段名 -> 数据库列名
        String dbSortField = mapToDatabaseColumn(sortField);

        return QueryWrapper.create()
                .eq("id", id)
                .eq("user_role", userRole)
                .like("username", username)
                .like("nickname", nickname)
                .eq("company_id", companyId)
                .orderBy(dbSortField, "ascend".equals(sortOrder));
    }

    /**
     * 将Java字段名映射为数据库列名
     */
    private String mapToDatabaseColumn(String javaFieldName) {
        if (javaFieldName == null) {
            return null;
        }
        switch (javaFieldName) {
            case "createTime":
                return "create_time";
            case "updateTime":
                return "update_time";
            case "userRole":
                return "user_role";
            case "companyId":
                return "company_id";
            case "isDelete":
                return "is_delete";
            default:
                return javaFieldName; // 对于其他字段，直接使用原名
        }
    }

    @Override
    public Boolean removeById(Long id) {
        // 尝试通过 getById 获取（正常未逻辑删除的用户）并切换 isDelete。
        User user = null;
        try {
            user = getById(id);
        } catch (Exception ignored) {
        }

        if (user != null) {
            User deleteUser = new User();
            BeanUtil.copyProperties(user, deleteUser);
            deleteUser.setIsDelete(!Boolean.TRUE.equals(deleteUser.getIsDelete()));
            return updateById(deleteUser);
        } else {
            // 记录可能存在但已被逻辑删除，直接调用 mapper 的原生 SQL 恢复
            int affected = this.mapper.restoreById(id);
            ThrowUtils.throwIf(affected <= 0, ErrorCode.NOT_FOUND_ERROR);
            return affected > 0;
        }
    }

}
