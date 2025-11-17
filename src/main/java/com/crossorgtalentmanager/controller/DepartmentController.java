package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.department.DepartmentAddRequest;
import com.crossorgtalentmanager.model.dto.department.DepartmentQueryRequest;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.DepartmentVO;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.crossorgtalentmanager.model.entity.Department;
import com.crossorgtalentmanager.service.DepartmentService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.model.entity.Employee;

import java.util.List;

/**
 * 记录公司部门及部门领导 控制层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @Resource
    private UserService userService;

    @Resource
    private CompanyService companyService;

    @Resource
    private EmployeeService employeeService;

    /**
     * 添加部门（仅 HR 以及 公司管理）
     *
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> addDepartment(@RequestBody DepartmentAddRequest departmentAddRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(departmentAddRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(
                !(loginUser.getUserRole().equals(UserRoleEnum.HR.getValue()))
                        && !(loginUser.getUserRole().equals(UserRoleEnum.COMPANY_ADMIN.getValue())),
                ErrorCode.NO_AUTH_ERROR, "无权限");
        String name = departmentAddRequest.getName();
        Long companyId = loginUser.getCompanyId();
        Company company = companyService.getById(companyId);
        Long contactPersonId = company.getContactPersonId();

        Long result = departmentService.addDepartment(name, companyId, contactPersonId);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取部门（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Department> getDepartmentById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Department department = departmentService.getById(id);
        ThrowUtils.throwIf(department == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(department);
    }

    /**
     * 根据 id 获取部门包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<DepartmentVO> getDepartmentVOById(long id) {
        BaseResponse<Department> response = getDepartmentById(id);
        Department department = response.getData();
        return ResultUtils.success(departmentService.getDepartmentVO(department));
    }

    /**
     * 切换部门的删除状态（仅管理员）
     */
    @PostMapping("/toggle")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> toggleDepartmentStatus(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = departmentService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新部门信息（仅管理员）
     */
    @PutMapping("update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDepartment(@RequestBody Department department) {
        ThrowUtils.throwIf(department == null || department.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean result = departmentService.updateById(department);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取部门封装列表（仅管理员）
     *
     * @param departmentQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<DepartmentVO>> listDepartmentVOByPage(
            @RequestBody DepartmentQueryRequest departmentQueryRequest) {
        ThrowUtils.throwIf(departmentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = departmentQueryRequest.getPageNum();
        long pageSize = departmentQueryRequest.getPageSize();
        Page<Department> departmentPage = departmentService.page(
                Page.of(pageNum, pageSize),
                departmentService.getQueryWrapper(departmentQueryRequest));
        // 数据脱敏/包装
        Page<DepartmentVO> departmentVOPage = new Page<>(pageNum, pageSize, departmentPage.getTotalRow());
        List<DepartmentVO> departmentVOList = departmentService.getDepartmentVOList(departmentPage.getRecords());
        departmentVOPage.setRecords(departmentVOList);
        return ResultUtils.success(departmentVOPage);
    }

    @PostMapping("addSupervisor")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> addSupervisor(
            @RequestBody com.crossorgtalentmanager.model.dto.department.DepartmentSupervisorAddRequest request,
            HttpServletRequest httpRequest) {
        // 校验请求
        ThrowUtils.throwIf(request == null || request.getDepartmentId() == null || request.getDepartmentId() <= 0
                || request.getEmployeeId() == null || request.getEmployeeId() <= 0 || request.getIsSupervisor() == null,
                ErrorCode.PARAMS_ERROR);

        // 验证登录用户
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 只有 HR 或 COMPANY_ADMIN 可以操作
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "无权限");

        Long departmentId = request.getDepartmentId();
        Long employeeId = request.getEmployeeId();
        boolean isSupervisor = Boolean.TRUE.equals(request.getIsSupervisor());

        // 验证部门存在且属于当前用户公司
        Department department = departmentService.getById(departmentId);
        ThrowUtils.throwIf(department == null, ErrorCode.NOT_FOUND_ERROR, "部门不存在");
        Long loginCompanyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(loginCompanyId == null || !loginCompanyId.equals(department.getCompanyId()),
                ErrorCode.NO_AUTH_ERROR, "无法为非本公司部门指定主管");

        if (isSupervisor) {
            // 设置主管：验证员工存在且属于当前用户公司
            Employee employee = employeeService.getById(employeeId);
            ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");
            ThrowUtils.throwIf(employee.getCompanyId() == null || !employee.getCompanyId().equals(loginCompanyId),
                    ErrorCode.NO_AUTH_ERROR, "员工不属于本公司");

            // 将部门主管设置为该员工的 ID
            department.setLeaderId(employeeId);
        } else {
            // 取消主管：校验当前部门是否存在该主管
            ThrowUtils.throwIf(department.getLeaderId() == null || !department.getLeaderId().equals(employeeId),
                    ErrorCode.PARAMS_ERROR, "该员工不是当前部门的主管，无法取消");

            // 取消主管，将 leaderId 设置为 null
            department.setLeaderId(null);
        }

        boolean update = departmentService.updateById(department);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "操作失败");
        return ResultUtils.success(true);
    }

}
