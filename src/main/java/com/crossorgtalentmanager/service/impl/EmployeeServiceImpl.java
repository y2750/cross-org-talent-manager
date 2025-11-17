package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.employee.EmployeeQueryRequest;
import com.crossorgtalentmanager.model.dto.user.UserQueryRequest;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.model.entity.Department;
import com.crossorgtalentmanager.model.enums.EmployeeStatusEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.EmployeeVO;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.DepartmentService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.mapper.EmployeeMapper;
import com.crossorgtalentmanager.service.EmployeeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 员工基本信息 服务层实现。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Resource
    private UserService userService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private CompanyService companyService;

    final String DEFAULT_PASSWORD = "123456";

    @Override
    public long employeeCreate(Long companyId, String name, String gender, String phone, String email,
            String idCardNumber, Long departmentId) {
        // 检查参数是否为空
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "公司ID不能为空");
        ThrowUtils.throwIf(name == null, ErrorCode.PARAMS_ERROR, "姓名不能为空");
        ThrowUtils.throwIf(gender == null, ErrorCode.PARAMS_ERROR, "性别不能为空");
        ThrowUtils.throwIf(phone == null, ErrorCode.PARAMS_ERROR, "电话不能为空");
        ThrowUtils.throwIf(email == null, ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(idCardNumber == null, ErrorCode.PARAMS_ERROR, "身份证号不能为空");
        ThrowUtils.throwIf(departmentId == null, ErrorCode.PARAMS_ERROR, "部门ID不能为空");
        // 检查手机号是否合法
        ThrowUtils.throwIf(!phone.matches("^1[3456789]\\d{9}$"), ErrorCode.PARAMS_ERROR, "手机号格式不正确");
        // 检查邮箱是否合法
        ThrowUtils.throwIf(!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$"),
                ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        // 检查身份证号是否合法
        ThrowUtils.throwIf(
                !idCardNumber.matches(
                        "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"),
                ErrorCode.PARAMS_ERROR, "身份证号格式不正确");
        // 检查身份证号是否存在
        QueryWrapper queryWrapper = QueryWrapper.create().eq("id_card_number", idCardNumber);
        // 检查部门id是否存在
        Department department = departmentService.getById(departmentId);
        ThrowUtils.throwIf(department == null, ErrorCode.PARAMS_ERROR, "部门不存在");
        Employee employee = this.getOne(queryWrapper);
        if (employee != null) {
            // 若存在同身份证号员工，校验姓名一致
            ThrowUtils.throwIf(name == null || !name.equals(employee.getName()), ErrorCode.PARAMS_ERROR,
                "身份证对应的姓名与请求不一致");
            // 若员工已处于在职状态，则不允许重复添加
            ThrowUtils.throwIf(Boolean.TRUE.equals(employee.getStatus()), ErrorCode.PARAMS_ERROR,
                "员工已在职，无法重复添加");
            // 如果存在且通过校验，则更新员工信息并将状态置为在职（重新入职）
            employee.setName(name);
            employee.setGender(gender);
            employee.setPhone(phone);
            employee.setEmail(email);
            employee.setDepartmentId(departmentId);
            employee.setStatus(EmployeeStatusEnum.NORMAL.getValue());
            this.updateById(employee);
            return employee.getId();
        } else {
            // 如果不存在，则添加新员工
            Employee newEmployee = new Employee();
            newEmployee.setCompanyId(companyId);
            newEmployee.setName(name);
            newEmployee.setGender(gender);
            newEmployee.setPhone(phone);
            newEmployee.setEmail(email);
            newEmployee.setIdCardNumber(idCardNumber);
            newEmployee.setDepartmentId(departmentId);
            newEmployee.setStatus(EmployeeStatusEnum.NORMAL.getValue());
            // 添加员工到 user 表中
            long userId = userService.userRegister(idCardNumber, UserRoleEnum.EMPLOYEE.getValue(), name,
                    DEFAULT_PASSWORD, DEFAULT_PASSWORD);
            newEmployee.setUserId(userId);
            boolean saveResult = this.save(newEmployee);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加员工失败");
            }

            return newEmployee.getId();
        }
    }

    @Override
    public EmployeeVO getEmployeeVO(Employee employee) {
        if (employee == null) {
            return null;
        }
        EmployeeVO employeeVO = new EmployeeVO();
        BeanUtil.copyProperties(employee, employeeVO);

        // 填充公司名称
        if (employee.getCompanyId() != null) {
            Company company = companyService.getById(employee.getCompanyId());
            if (company != null) {
                employeeVO.setCompanyName(company.getName());
            }
        }

        // 填充部门名称
        if (employee.getDepartmentId() != null) {
            Department department = departmentService.getById(employee.getDepartmentId());
            if (department != null) {
                employeeVO.setDepartmentName(department.getName());
            }
        }

        return employeeVO;
    }

    @Override
    public List<EmployeeVO> getEmployeeVOList(List<Employee> employeeList) {
        if (CollUtil.isEmpty(employeeList)) {
            return new ArrayList<>();
        }

        // 先将 Employee -> EmployeeVO
        List<EmployeeVO> employeeVOList = employeeList.stream()
                .map(this::getEmployeeVO)
                .collect(Collectors.toList());

        // 收集所有 companyId 和 departmentId
        Set<Long> companyIds = employeeList.stream()
                .map(Employee::getCompanyId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> departmentIds = employeeList.stream()
                .map(Employee::getDepartmentId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询公司和部门信息
        Map<Long, String> companyIdToName = new HashMap<>();
        Map<Long, String> departmentIdToName = new HashMap<>();

        if (!companyIds.isEmpty()) {
            List<Company> companies = companyService.listByIds(companyIds);
            companyIdToName = companies.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(Company::getId, Company::getName, (a, b) -> a));
        }

        if (!departmentIds.isEmpty()) {
            List<Department> departments = departmentService.listByIds(departmentIds);
            departmentIdToName = departments.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        }

        // 填充公司名称和部门名称
        for (EmployeeVO vo : employeeVOList) {
            if (vo.getCompanyId() != null) {
                vo.setCompanyName(companyIdToName.get(vo.getCompanyId()));
            }
            if (vo.getDepartmentId() != null) {
                vo.setDepartmentName(departmentIdToName.get(vo.getDepartmentId()));
            }
        }

        return employeeVOList;
    }

    @Override
    public QueryWrapper getQueryWrapper(EmployeeQueryRequest employeeQueryRequest) {
        ThrowUtils.throwIf(employeeQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = employeeQueryRequest.getId();
        String name = employeeQueryRequest.getName();
        String idCardNumber = employeeQueryRequest.getIdCardNumber();
        String phone = employeeQueryRequest.getPhone();
        Long companyId = employeeQueryRequest.getCompanyId();
        Long departmentId = employeeQueryRequest.getDepartmentId();
        Boolean status = employeeQueryRequest.getStatus();
        String sortField = employeeQueryRequest.getSortField();
        String sortOrder = employeeQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("name", name)
                .eq("id_card_number", idCardNumber)
                .like("phone", phone)
                .eq("company_id", companyId)
                .eq("department_id", departmentId)
                .eq("status", status)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public com.mybatisflex.core.paginate.Page<EmployeeVO> pageEmployeeVOByPage(
            EmployeeQueryRequest employeeQueryRequest, com.crossorgtalentmanager.model.entity.User loginUser) {
        ThrowUtils.throwIf(employeeQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "未登录");

        long pageNum = employeeQueryRequest.getPageNum();
        long pageSize = employeeQueryRequest.getPageSize();

        // 构建查询条件，调用者的权限决定是否限定 company_id
        QueryWrapper qw = QueryWrapper.create();
        // 基本条件来自请求
        Long id = employeeQueryRequest.getId();
        String name = employeeQueryRequest.getName();
        String phone = employeeQueryRequest.getPhone();
        String idCardNumber = employeeQueryRequest.getIdCardNumber();
        Long departmentId = employeeQueryRequest.getDepartmentId();
        Boolean status = employeeQueryRequest.getStatus();
        String sortField = employeeQueryRequest.getSortField();
        String sortOrder = employeeQueryRequest.getSortOrder();

        qw.eq("id", id)
                .like("name", name)
                .eq("id_card_number", idCardNumber)
                .like("phone", phone)
                .eq("department_id", departmentId)
                .eq("status", status)
                .orderBy(sortField, "ascend".equals(sortOrder));

        // 权限决定 company_id 过滤：管理员不限制，HR 与公司管理员限定为同公司
        com.crossorgtalentmanager.model.enums.UserRoleEnum roleEnum = com.crossorgtalentmanager.model.enums.UserRoleEnum
                .getEnumByValue(loginUser.getUserRole());
        if (roleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        switch (roleEnum) {
            case ADMIN:
                // 管理员查看所有，不添加 company_id 条件
                break;
            case COMPANY_ADMIN:
            case HR:
                Long companyId = loginUser.getCompanyId();
                qw.eq("company_id", companyId);
                break;
            default:
                // 其他角色不允许调用此分页接口
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看员工列表");
        }

        com.mybatisflex.core.paginate.Page<Employee> employeePage = this
                .page(com.mybatisflex.core.paginate.Page.of(pageNum, pageSize), qw);

        // 封装为 VO
        com.mybatisflex.core.paginate.Page<EmployeeVO> employeeVOPage = new com.mybatisflex.core.paginate.Page<>(
                pageNum, pageSize, employeePage.getTotalRow());
        List<EmployeeVO> employeeVOList = getEmployeeVOList(employeePage.getRecords());
        employeeVOPage.setRecords(employeeVOList);
        return employeeVOPage;
    }
}
