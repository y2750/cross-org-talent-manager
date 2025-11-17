package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.manager.CosManager;
import com.crossorgtalentmanager.model.dto.employee.EmployeeCreateRequest;
import com.crossorgtalentmanager.model.dto.employee.EmployeeQueryRequest;
import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.EmployeeVO;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;

/**
 * 员工基本信息 控制层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 员工创建
     * 当数据库中存在相同身份证号员工时执行更新，否则执行添加
     *
     * @param employeeCreateRequest 员工添加请求
     * @return 注册结果
     */
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    @PostMapping("/create")
    public BaseResponse<Long> employeeCreate(@RequestBody EmployeeCreateRequest employeeCreateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(employeeCreateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long companyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.NO_AUTH_ERROR, "无权限");
        String name = employeeCreateRequest.getName();
        String gender = employeeCreateRequest.getGender();
        String phone = employeeCreateRequest.getPhone();
        String email = employeeCreateRequest.getEmail();
        String idCardNumber = employeeCreateRequest.getIdCardNumber();
        Long departmentId = employeeCreateRequest.getDepartmentId();
        long result = employeeService.employeeCreate(companyId, name, gender, phone, email, idCardNumber, departmentId);
        return ResultUtils.success(result);
    }

    /**
     * 员工更新自己的个人资料：电话、邮箱、照片
     */
    @PostMapping("/update/me")
    public BaseResponse<Boolean> updateMyProfile(@RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestPart(required = false) MultipartFile photo,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 通过 userId 查找 employee
        QueryWrapper qw = QueryWrapper.create().eq("user_id", loginUser.getId());
        Employee employee = employeeService.getOne(qw);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "未找到员工信息");
        if (phone != null) {
            employee.setPhone(phone);
        }
        if (email != null) {
            employee.setEmail(email);
        }
        if (photo != null && !photo.isEmpty()) {
            try {
                String original = photo.getOriginalFilename();
                String key = String.format("employee/%s/%s_%s", loginUser.getId(), Instant.now().toEpochMilli(),
                        original);
                File tmp = File.createTempFile("upload_", original);
                photo.transferTo(tmp);
                String url = cosManager.uploadFile(key, tmp);
                try {
                    Files.deleteIfExists(tmp.toPath());
                } catch (IOException ignored) {
                }
                ThrowUtils.throwIf(url == null, ErrorCode.OPERATION_ERROR, "上传图片失败");
                employee.setPhotoUrl(url);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件处理失败");
            }
        }
        boolean update = employeeService.updateById(employee);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * HR 及以上可以更新员工信息：部门、电话、邮箱、照片
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> updateEmployee(@RequestParam Long id,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestPart(required = false) MultipartFile photo) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Employee employee = employeeService.getById(id);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR);
        if (departmentId != null) {
            employee.setDepartmentId(departmentId);
        }
        if (phone != null) {
            employee.setPhone(phone);
        }
        if (email != null) {
            employee.setEmail(email);
        }
        if (photo != null && !photo.isEmpty()) {
            try {
                String original = photo.getOriginalFilename();
                String key = String.format("employee/%s/%s_%s", id, Instant.now().toEpochMilli(), original);
                File tmp = File.createTempFile("upload_", original);
                photo.transferTo(tmp);
                String url = cosManager.uploadFile(key, tmp);
                try {
                    Files.deleteIfExists(tmp.toPath());
                } catch (IOException ignored) {
                }
                ThrowUtils.throwIf(url == null, ErrorCode.OPERATION_ERROR, "上传图片失败");
                employee.setPhotoUrl(url);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件处理失败");
            }
        }
        boolean update = employeeService.updateById(employee);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 解雇员工（HR）: 将状态置为 false，公司ID 和 部门ID 置为 null
     */
    @PostMapping("/fire")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> fireEmployee(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        Employee employee = employeeService.getById(id);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR);
        employee.setStatus(false);
        employee.setCompanyId(null);
        employee.setDepartmentId(null);
        boolean update = employeeService.updateById(employee);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "解雇操作失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取员工（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Employee> getEmployeeById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Employee employee = employeeService.getById(id);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(employee);
    }

    /**
     * 根据 id 获取员工包装类（含公司名、部门名等关联信息）
     */
    @GetMapping("/get/vo")
    public BaseResponse<EmployeeVO> getEmployeeVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Employee employee = employeeService.getById(id);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR);
        EmployeeVO employeeVO = employeeService.getEmployeeVO(employee);
        return ResultUtils.success(employeeVO);
    }

    /**
     * 分页获取员工列表（仅管理员），返回 VO 对象
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<EmployeeVO>> listEmployeeVOByPage(@RequestBody EmployeeQueryRequest employeeQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(employeeQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<EmployeeVO> page = employeeService.pageEmployeeVOByPage(employeeQueryRequest, loginUser);
        return ResultUtils.success(page);
    }
}
