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
import com.crossorgtalentmanager.model.enums.EmployeeStatusEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.EmployeeVO;
import com.crossorgtalentmanager.service.DepartmentService;
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
import java.util.List;
import java.util.Map;

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

    @Resource
    private DepartmentService departmentService;

    /**
     * 员工创建
     * 当数据库中存在相同身份证号员工时执行更新，否则执行添加
     * 管理员可以创建没有公司和部门的员工（为未加入公司的员工创建账户）
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

        // 权限判断：管理员可以指定公司（可选），非管理员必须使用当前公司
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        Long companyId;

        if (isAdmin) {
            // 管理员：可以使用请求中的 companyId（可选），也可以不指定（为未加入公司的员工创建账户）
            companyId = employeeCreateRequest.getCompanyId();
        } else {
            // 非管理员：必须使用当前登录用户的公司
            companyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(companyId == null, ErrorCode.NO_AUTH_ERROR, "无权限");
        }

        String name = employeeCreateRequest.getName();
        String gender = employeeCreateRequest.getGender();
        String phone = employeeCreateRequest.getPhone();
        String email = employeeCreateRequest.getEmail();
        String idCardNumber = employeeCreateRequest.getIdCardNumber();
        Long departmentId = employeeCreateRequest.getDepartmentId();
        long result = employeeService.employeeCreate(companyId, name, gender, phone, email, idCardNumber, departmentId,
                isAdmin);
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
     * 管理员可以额外更新：姓名、身份证号（更新身份证号时会自动推断性别）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> updateEmployee(@RequestParam Long id,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String idCardNumber,
            @RequestPart(required = false) MultipartFile photo,
            HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Employee employee = employeeService.getById(id);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR);

        // 检查是否为管理员（只有管理员可以修改姓名和身份证号）
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());

        // 管理员可以更新姓名和身份证号
        if (isAdmin) {
            if (name != null && !name.trim().isEmpty()) {
                employee.setName(name.trim());
            }
            if (idCardNumber != null && !idCardNumber.trim().isEmpty()) {
                // 更新身份证号并自动推断性别
                String newIdCardNumber = idCardNumber.trim();
                employee.setIdCardNumber(newIdCardNumber);
                // 根据身份证号推断性别（第17位，奇数为男性，偶数为女性）
                String inferredGender = employeeService.inferGenderFromIdCard(newIdCardNumber);
                if (inferredGender != null) {
                    employee.setGender(inferredGender);
                }
            }
        }

        // 处理 departmentId：只支持设置部门，不支持清空（清空操作请使用批量移出部门接口）
        if (departmentId != null && !departmentId.trim().isEmpty() && !"null".equalsIgnoreCase(departmentId)) {
            try {
                Long departmentIdForLong = Long.parseLong(departmentId);
                employee.setDepartmentId(departmentIdForLong);
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "部门ID格式错误");
            }
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
     * 批量移出部门：将指定员工的departmentId置为null
     * 使用原生SQL确保null值能够被正确设置
     * 
     * @param employeeIds 员工ID列表
     * @param request     HTTP请求
     * @return 成功移出的员工数量
     */
    @PostMapping("/batch/remove-from-department")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Integer> batchRemoveFromDepartment(@RequestBody java.util.List<Long> employeeIds,
            HttpServletRequest request) {
        ThrowUtils.throwIf(employeeIds == null || employeeIds.isEmpty(), ErrorCode.PARAMS_ERROR, "员工ID列表不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        int affectedRows = employeeService.batchRemoveFromDepartment(employeeIds, loginUser);
        return ResultUtils.success(affectedRows);
    }

    /**
     * 批量添加到部门：将指定员工的departmentId设置为指定部门ID
     * 使用原生SQL确保批量更新能够正确执行
     * 
     * @param requestBody 包含员工ID列表和部门ID的请求体
     * @param request     HTTP请求
     * @return 成功添加的员工数量
     */
    @PostMapping("/batch/add-to-department")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Integer> batchAddToDepartment(@RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        ThrowUtils.throwIf(requestBody == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        // 处理 employeeIds：前端可能发送字符串数组，需要转换为 Long 列表
        @SuppressWarnings("unchecked")
        java.util.List<Object> employeeIdsObj = (java.util.List<Object>) requestBody.get("employeeIds");
        Object departmentIdObj = requestBody.get("departmentId");

        ThrowUtils.throwIf(employeeIdsObj == null || employeeIdsObj.isEmpty(), ErrorCode.PARAMS_ERROR, "员工ID列表不能为空");
        ThrowUtils.throwIf(departmentIdObj == null, ErrorCode.PARAMS_ERROR, "部门ID不能为空");

        // 将 employeeIds 转换为 Long 列表（支持字符串和数字类型）
        java.util.List<Long> employeeIds = new java.util.ArrayList<>();
        for (Object idObj : employeeIdsObj) {
            Long employeeId;
            if (idObj instanceof Long) {
                employeeId = (Long) idObj;
            } else if (idObj instanceof Number) {
                employeeId = ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                try {
                    employeeId = Long.parseLong((String) idObj);
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "员工ID格式错误: " + idObj);
                }
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "员工ID格式错误: " + idObj);
            }
            employeeIds.add(employeeId);
        }

        Long departmentId;
        if (departmentIdObj instanceof Long) {
            departmentId = (Long) departmentIdObj;
        } else if (departmentIdObj instanceof Number) {
            departmentId = ((Number) departmentIdObj).longValue();
        } else if (departmentIdObj instanceof String) {
            try {
                departmentId = Long.parseLong((String) departmentIdObj);
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "部门ID格式错误");
            }
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "部门ID格式错误");
        }

        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        int affectedRows = employeeService.batchAddToDepartment(employeeIds, departmentId, loginUser);
        return ResultUtils.success(affectedRows);
    }

    /**
     * 开始解雇流程（HR）: 不立即解雇，只是进入解雇流程，等待档案编辑完成后确认解雇
     * 系统管理员不能执行解雇操作
     */
    @PostMapping("/fire")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> fireEmployee(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查是否为系统管理员，系统管理员不能执行解雇操作
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ThrowUtils.throwIf(isAdmin, ErrorCode.NO_AUTH_ERROR, "系统管理员不能执行解雇操作");

        Long id = deleteRequest.getId();
        Employee employee = employeeService.getById(id);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");
        // 开始解雇流程，不立即解雇，只是返回成功，让前端进入解雇模式
        return ResultUtils.success(true);
    }

    /**
     * 确认解雇员工（HR）: 完成解雇操作，将companyId和departmentId置为null，status置为false
     * 如果未完成档案编辑工作，所有操作将回滚
     * 系统管理员不能执行解雇操作
     */
    @PostMapping("/fire/confirm")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> confirmFireEmployee(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 检查是否为系统管理员，系统管理员不能执行解雇操作
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ThrowUtils.throwIf(isAdmin, ErrorCode.NO_AUTH_ERROR, "系统管理员不能执行解雇操作");

        // 调用服务层方法，使用事务处理
        boolean result = employeeService.confirmFireEmployee(id, loginUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "解雇操作失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取员工（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
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
     * 获取当前登录员工的员工信息（通过 userId）
     */
    @GetMapping("/get/me/vo")
    public BaseResponse<EmployeeVO> getMyEmployeeVO(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 通过 userId 查找 employee
        QueryWrapper qw = QueryWrapper.create().eq("user_id", loginUser.getId());
        Employee employee = employeeService.getOne(qw);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "未找到员工信息");

        EmployeeVO employeeVO = employeeService.getEmployeeVO(employee);
        return ResultUtils.success(employeeVO);
    }

    /**
     * 获取部门同事列表（脱敏版本，仅员工角色可访问）
     * 脱敏规则：电话、邮箱、身份证号脱敏
     * 当登录的员工为部门主管时，同事列表信息不进行脱敏处理
     */
    @GetMapping("/colleagues/department")
    public BaseResponse<List<EmployeeVO>> getDepartmentColleagues(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 通过 userId 查找当前员工的 employee 信息
        QueryWrapper qw = QueryWrapper.create().eq("user_id", loginUser.getId());
        Employee currentEmployee = employeeService.getOne(qw);
        ThrowUtils.throwIf(currentEmployee == null, ErrorCode.NOT_FOUND_ERROR, "未找到员工信息");
        ThrowUtils.throwIf(currentEmployee.getDepartmentId() == null, ErrorCode.PARAMS_ERROR, "员工未分配部门");

        // 获取部门信息，确定主管ID
        com.crossorgtalentmanager.model.entity.Department department = departmentService
                .getById(currentEmployee.getDepartmentId());
        Long leaderId = department != null ? department.getLeaderId() : null;

        // 判断当前登录员工是否为部门主管
        boolean isCurrentEmployeeLeader = leaderId != null && currentEmployee.getId() != null
                && currentEmployee.getId().equals(leaderId);

        // 查询同部门的所有员工
        QueryWrapper deptQw = QueryWrapper.create()
                .eq("department_id", currentEmployee.getDepartmentId())
                .eq("company_id", currentEmployee.getCompanyId())
                .eq("status", EmployeeStatusEnum.NORMAL.getValue());
        List<Employee> colleagues = employeeService.list(deptQw);

        // 转换为 VO 并脱敏
        List<EmployeeVO> colleagueVOs = employeeService.getEmployeeVOList(colleagues);

        // 脱敏处理：电话、邮箱、身份证号
        // 如果当前登录员工是部门主管，则不对同事信息进行脱敏处理
        if (!isCurrentEmployeeLeader) {
            for (EmployeeVO vo : colleagueVOs) {
                // 电话脱敏：保留前3位和后4位，中间用*代替
                if (vo.getPhone() != null && vo.getPhone().length() > 7) {
                    String phone = vo.getPhone();
                    vo.setPhone(phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4));
                }

                // 邮箱脱敏：保留@前1位和@后部分
                if (vo.getEmail() != null && vo.getEmail().contains("@")) {
                    String email = vo.getEmail();
                    int atIndex = email.indexOf("@");
                    if (atIndex > 0) {
                        vo.setEmail(email.substring(0, 1) + "***" + email.substring(atIndex));
                    }
                }

                // 身份证号脱敏：保留前6位和后4位
                if (vo.getIdCardNumber() != null && vo.getIdCardNumber().length() > 10) {
                    String idCard = vo.getIdCardNumber();
                    vo.setIdCardNumber(idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4));
                }
            }
        }

        return ResultUtils.success(colleagueVOs);
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

    /**
     * 统计指定公司的员工数量
     */
    @GetMapping("/count")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> countEmployees(@RequestParam Long companyId,
            @RequestParam(required = false) Boolean status) {
        ThrowUtils.throwIf(companyId == null || companyId <= 0, ErrorCode.PARAMS_ERROR);
        QueryWrapper qw = QueryWrapper.create()
                .eq("company_id", companyId);
        if (status != null) {
            qw.eq("status", status);
        }
        long count = employeeService.count(qw);
        return ResultUtils.success(count);
    }

    /**
     * 批量导入员工（从Excel文件）
     * Excel格式：姓名、身份证号、部门名、手机号、邮箱
     *
     * @param file    Excel文件（.xlsx格式）
     * @param request HTTP请求
     * @return 导入结果
     */
    @PostMapping("/batch/import")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<com.crossorgtalentmanager.model.dto.employee.EmployeeBatchImportResult> batchImportEmployees(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(request);

        // 权限判断：管理员可以指定公司（可选），非管理员必须使用当前公司
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        Long companyId;

        if (isAdmin) {
            // 管理员：可以使用请求中的 companyId（可选），也可以不指定（为未加入公司的员工创建账户）
            // 这里暂时不支持管理员指定公司，统一使用当前登录用户的公司
            companyId = loginUser.getCompanyId();
        } else {
            // 非管理员：必须使用当前登录用户的公司
            companyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(companyId == null, ErrorCode.NO_AUTH_ERROR, "无权限");
        }

        com.crossorgtalentmanager.model.dto.employee.EmployeeBatchImportResult result = employeeService
                .batchImportEmployees(file, companyId, loginUser);
        return ResultUtils.success(result);
    }
}
