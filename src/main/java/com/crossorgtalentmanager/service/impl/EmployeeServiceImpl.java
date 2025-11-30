package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.employee.EmployeeQueryRequest;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.model.entity.Department;
import com.crossorgtalentmanager.model.enums.EmployeeStatusEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.EmployeeVO;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.DepartmentService;
import com.crossorgtalentmanager.service.EvaluationService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.mapper.EmployeeMapper;
import com.crossorgtalentmanager.mapper.UserMapper;
import com.crossorgtalentmanager.service.EmployeeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private com.crossorgtalentmanager.mapper.DepartmentMapper departmentMapper;

    @Resource
    private CompanyService companyService;

    @Resource
    @Lazy
    private EvaluationService evaluationService;

    final String DEFAULT_PASSWORD = "123456";

    @Override
    public long employeeCreate(Long companyId, String name, String gender, String phone, String email,
            String idCardNumber, Long departmentId, boolean isAdmin) {
        // 检查参数是否为空
        // 管理员可以创建没有公司和部门的员工，非管理员必须提供公司，但部门可选
        if (!isAdmin) {
            ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "公司ID不能为空");
            // 部门ID为可选，允许为空
        }
        ThrowUtils.throwIf(name == null, ErrorCode.PARAMS_ERROR, "姓名不能为空");
        ThrowUtils.throwIf(phone == null, ErrorCode.PARAMS_ERROR, "电话不能为空");
        ThrowUtils.throwIf(email == null, ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(idCardNumber == null, ErrorCode.PARAMS_ERROR, "身份证号不能为空");

        // 如果性别为空，根据身份证号自动推断
        if (gender == null || gender.trim().isEmpty()) {
            gender = inferGenderFromIdCard(idCardNumber);
            ThrowUtils.throwIf(gender == null, ErrorCode.PARAMS_ERROR, "无法从身份证号推断性别，请检查身份证号格式");
        }
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
        // 检查部门id是否存在（如果提供了部门ID）
        if (departmentId != null) {
            Department department = departmentService.getById(departmentId);
            ThrowUtils.throwIf(department == null, ErrorCode.PARAMS_ERROR, "部门不存在");
        }
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
            employee.setCompanyId(companyId); // 设置公司ID（办理入职时必须设置）
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

            // 尝试保存员工信息，如果失败则物理删除已创建的 user 记录
            try {
                boolean saveResult = this.save(newEmployee);
                if (!saveResult) {
                    // 保存失败，物理删除已创建的 user 记录
                    userMapper.deleteByIdPhysically(userId);
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加失败，请重试");
                }
            } catch (Exception e) {
                // 捕获任何异常（包括数据库约束异常等），物理删除已创建的 user 记录
                try {
                    userMapper.deleteByIdPhysically(userId);
                    log.info("已物理删除用户记录，userId: {}", userId);
                } catch (Exception deleteException) {
                    // 记录删除失败日志，但不影响主异常抛出
                    log.error("删除用户记录失败，userId: {}", userId, deleteException);
                }
                // 如果是业务异常，直接抛出；否则包装为业务异常
                if (e instanceof BusinessException) {
                    throw e;
                } else {
                    // 记录原始异常信息
                    log.error("添加员工失败，已回滚用户记录，userId: {}", userId, e);
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加失败，请重试");
                }
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
        Long requestCompanyId = employeeQueryRequest.getCompanyId();
        String sortField = employeeQueryRequest.getSortField();
        String sortOrder = employeeQueryRequest.getSortOrder();

        qw.eq("id", id)
                .like("name", name)
                .eq("id_card_number", idCardNumber)
                .like("phone", phone)
                .eq("department_id", departmentId)
                .eq("status", status)
                .orderBy(sortField, "ascend".equals(sortOrder));

        // 权限决定 company_id 过滤：管理员可以指定查询某公司，HR 与公司管理员限定为同公司
        com.crossorgtalentmanager.model.enums.UserRoleEnum roleEnum = com.crossorgtalentmanager.model.enums.UserRoleEnum
                .getEnumByValue(loginUser.getUserRole());
        if (roleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        switch (roleEnum) {
            case ADMIN:
                // 管理员可以查看所有或指定公司的员工
                if (requestCompanyId != null) {
                    qw.eq("company_id", requestCompanyId);
                }
                break;
            case COMPANY_ADMIN:
            case HR:
                // HR和公司管理员只能查看自己公司的员工
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

    @Override
    public String inferGenderFromIdCard(String idCardNumber) {
        if (idCardNumber == null || idCardNumber.isEmpty() || idCardNumber.length() != 18) {
            return null;
        }

        // 18位身份证：第17位（索引16）是性别位
        char genderDigit = idCardNumber.charAt(16);

        // 将字符转换为数字
        int digit;
        try {
            digit = Character.getNumericValue(genderDigit);
        } catch (NumberFormatException e) {
            return null;
        }

        // 奇数为男性，偶数为女性
        return (digit % 2 == 1) ? "男" : "女";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmFireEmployee(Long employeeId, com.crossorgtalentmanager.model.entity.User loginUser) {
        ThrowUtils.throwIf(employeeId == null || employeeId <= 0, ErrorCode.PARAMS_ERROR, "员工ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        // 查询员工信息
        Employee employee = this.getById(employeeId);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 权限校验：非系统管理员只能解雇本公司员工
        boolean isAdmin = com.crossorgtalentmanager.model.enums.UserRoleEnum.ADMIN.getValue()
                .equals(loginUser.getUserRole());
        if (!isAdmin) {
            Long loginCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");
            ThrowUtils.throwIf(!loginCompanyId.equals(employee.getCompanyId()),
                    ErrorCode.NO_AUTH_ERROR, "只能解雇本公司员工");
        }

        // ⚠️ 关键：在解雇操作之前创建离职评价任务
        // 因为解雇后员工可能就没有部门信息了，无法获取同部门同事
        try {
            evaluationService.createResignationEvaluationTasks(employeeId, loginUser);
            log.info("为离职员工 {} 创建了评价任务", employeeId);
        } catch (Exception e) {
            log.error("创建离职评价任务失败，员工ID：{}", employeeId, e);
            // 可以选择是否继续执行解雇操作，或者抛出异常
            // 这里选择记录日志但继续执行，避免影响解雇流程
        }

        // 检查该员工是否是某个部门的主管，如果是，需要清除部门的主管设置
        // 使用 departmentMapper 来清除所有以该员工为主管的部门的 leaderId
        int clearedDepartments = departmentMapper.clearLeaderIdByEmployeeId(employeeId);
        if (clearedDepartments > 0) {
            log.info("解雇员工 {} 时，清除了 {} 个部门的主管设置", employeeId, clearedDepartments);
        }

        // 执行解雇操作：将companyId和departmentId置为null，status置为false
        // 使用 mapper 的原生 SQL 方法来确保 null 值能够被正确更新
        int affectedRows = this.mapper.fireEmployee(employeeId);
        ThrowUtils.throwIf(affectedRows <= 0, ErrorCode.OPERATION_ERROR, "解雇操作失败");

        return true;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public int batchRemoveFromDepartment(java.util.List<Long> employeeIds,
            com.crossorgtalentmanager.model.entity.User loginUser) {
        ThrowUtils.throwIf(employeeIds == null || employeeIds.isEmpty(), ErrorCode.PARAMS_ERROR, "员工ID列表不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        // 权限校验：非系统管理员只能移出本公司员工
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        if (!isAdmin) {
            Long loginCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

            // 验证所有员工都属于当前用户公司
            for (Long employeeId : employeeIds) {
                Employee employee = this.getById(employeeId);
                ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在，ID: " + employeeId);
                ThrowUtils.throwIf(!loginCompanyId.equals(employee.getCompanyId()),
                        ErrorCode.NO_AUTH_ERROR, "只能移出本公司员工，员工ID: " + employeeId);
            }
        }

        // 使用原生SQL批量移出部门
        int affectedRows = this.mapper.batchRemoveFromDepartment(employeeIds);
        ThrowUtils.throwIf(affectedRows <= 0, ErrorCode.OPERATION_ERROR, "批量移出部门失败");

        log.info("批量移出部门成功，移出员工数量: {}, 受影响行数: {}", employeeIds.size(), affectedRows);
        return affectedRows;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public int batchAddToDepartment(java.util.List<Long> employeeIds, Long departmentId,
            com.crossorgtalentmanager.model.entity.User loginUser) {
        ThrowUtils.throwIf(employeeIds == null || employeeIds.isEmpty(), ErrorCode.PARAMS_ERROR, "员工ID列表不能为空");
        ThrowUtils.throwIf(departmentId == null || departmentId <= 0, ErrorCode.PARAMS_ERROR, "部门ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        // 验证部门存在
        Department department = departmentService.getById(departmentId);
        ThrowUtils.throwIf(department == null, ErrorCode.NOT_FOUND_ERROR, "部门不存在");

        // 查询出所有需要更新的员工实体（只查询一次，避免重复查询）
        List<Employee> employeesToUpdate = new ArrayList<>();
        for (Long employeeId : employeeIds) {
            Employee employee = this.getById(employeeId);
            ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在，ID: " + employeeId);
            employeesToUpdate.add(employee);
        }

        // 权限校验：非系统管理员只能添加本公司员工到本公司部门
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        if (!isAdmin) {
            Long loginCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

            // 验证部门属于当前用户公司
            ThrowUtils.throwIf(!loginCompanyId.equals(department.getCompanyId()),
                    ErrorCode.NO_AUTH_ERROR, "只能将员工添加到本公司部门");

            // 验证所有员工都属于当前用户公司
            for (Employee employee : employeesToUpdate) {
                ThrowUtils.throwIf(!loginCompanyId.equals(employee.getCompanyId()),
                        ErrorCode.NO_AUTH_ERROR, "只能添加本公司员工，员工ID: " + employee.getId());
            }
        }

        // 设置所有员工的部门ID
        for (Employee employee : employeesToUpdate) {
            employee.setDepartmentId(departmentId);
        }

        // 使用 MyBatis-Flex 的批量更新方法
        // 根据 MyBatis-Flex
        // 文档：https://mybatis-flex.com/zh/base/batch.html#db-updatebatch-%E6%96%B9%E6%B3%95
        // 使用 UpdateChain 进行批量更新，根据员工ID列表批量设置部门ID
        List<Long> employeeIdList = employeesToUpdate.stream()
                .map(Employee::getId)
                .collect(java.util.stream.Collectors.toList());

        boolean updateResult = UpdateChain.of(Employee.class)
                .set(Employee::getDepartmentId, departmentId)
                .where(Employee::getId).in(employeeIdList)
                .update();

        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "批量添加到部门失败");

        // UpdateChain.update() 返回 boolean，实际受影响的行数等于更新的员工数量
        int affectedRows = employeesToUpdate.size();

        log.info("批量添加到部门成功，添加员工数量: {}, 部门ID: {}, 受影响行数: {}", employeeIds.size(), departmentId, affectedRows);
        return affectedRows;
    }
}
