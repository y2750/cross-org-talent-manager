package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.department.DepartmentQueryRequest;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.model.vo.DepartmentVO;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.crossorgtalentmanager.model.entity.Department;
import com.crossorgtalentmanager.mapper.DepartmentMapper;
import com.crossorgtalentmanager.service.DepartmentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 记录公司部门及部门领导 服务层实现。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Service
@Slf4j
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Resource
    private CompanyService companyService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private EmployeeService employeeService;

    @Override
    public Long addDepartment(String name, Long companyId, Long leaderId) {
        ThrowUtils.throwIf(name == null || companyId == null || leaderId == null, ErrorCode.PARAMS_ERROR, "参数不能为空");

        Company company = companyService.getById(companyId);
        ThrowUtils.throwIf(company == null, ErrorCode.PARAMS_ERROR, "公司不存在");
        // todo：后期修改为在emplyee表中查找
        User leader = userService.getById(leaderId);
        ThrowUtils.throwIf(leader == null, ErrorCode.PARAMS_ERROR, "部门领导不存在");

        Department department = new Department();
        department.setName(name);
        department.setCompanyId(companyId);
        department.setLeaderId(leaderId);
        boolean result = this.save(department);
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR, "添加失败");
        return department.getId();
    }

    @Override
    public DepartmentVO getDepartmentVO(Department department) {
        if (department == null) {
            return null;
        }
        DepartmentVO departmentVO = new DepartmentVO();
        BeanUtil.copyProperties(department, departmentVO);

        // 填充企业名称
        if (department.getCompanyId() != null) {
            Company company = companyService.getById(department.getCompanyId());
            if (company != null) {
                departmentVO.setCompanyName(company.getName());
            }
        }

        // 填充领导名称
        if (department.getLeaderId() != null) {
            User leader = userService.getById(department.getLeaderId());
            if (leader != null) {
                departmentVO.setLeaderName(leader.getNickname());
            }
        }

        return departmentVO;
    }

    @Override
    public List<DepartmentVO> getDepartmentVOList(List<Department> departmentList) {
        if (CollUtil.isEmpty(departmentList)) {
            return new ArrayList<>();
        }

        // 先将 Department -> DepartmentVO，然后批量查询企业和用户信息
        List<DepartmentVO> departmentVOList = departmentList.stream()
                .map(this::getDepartmentVO)
                .collect(Collectors.toList());

        // 收集所有 companyId 和 leaderId
        Set<Long> companyIds = departmentList.stream()
                .map(Department::getCompanyId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> leaderIds = departmentList.stream()
                .map(Department::getLeaderId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询企业和员工信息
        java.util.Map<Long, String> companyIdToName = new java.util.HashMap<>();
        java.util.Map<Long, String> employeeIdToName = new java.util.HashMap<>();

        if (!companyIds.isEmpty()) {
            List<Company> companies = companyService.listByIds(companyIds);
            companyIdToName = companies.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(Company::getId, Company::getName, (a, b) -> a));
        }

        // leaderId 是 Employee 的ID，不是 User 的ID
        if (!leaderIds.isEmpty()) {
            List<Employee> employees = employeeService.listByIds(leaderIds);
            employeeIdToName = employees.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(Employee::getId, Employee::getName, (a, b) -> a));
        }

        // 填充企业名称和领导名称
        for (DepartmentVO vo : departmentVOList) {
            if (vo.getCompanyId() != null) {
                vo.setCompanyName(companyIdToName.get(vo.getCompanyId()));
            }
            if (vo.getLeaderId() != null) {
                vo.setLeaderName(employeeIdToName.get(vo.getLeaderId()));
            }
        }

        return departmentVOList;
    }

    @Override
    public QueryWrapper getQueryWrapper(DepartmentQueryRequest departmentQueryRequest) {
        ThrowUtils.throwIf(departmentQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = departmentQueryRequest.getId();
        String name = departmentQueryRequest.getName();
        Long companyId = departmentQueryRequest.getCompanyId();
        Long leaderId = departmentQueryRequest.getLeaderId();
        String sortField = departmentQueryRequest.getSortField();
        String sortOrder = departmentQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("name", name)
                .eq("company_id", companyId)
                .eq("leader_id", leaderId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public Boolean removeById(Long id) {
        // 尝试通过 getById 获取（正常未逻辑删除的部门）并切换 isDelete。
        Department department = null;
        try {
            department = getById(id);
        } catch (Exception ignored) {
        }

        if (department != null) {
            Department deleteDepartment = new Department();
            BeanUtil.copyProperties(department, deleteDepartment);
            deleteDepartment.setIsDelete(!Boolean.TRUE.equals(deleteDepartment.getIsDelete()));
            return updateById(deleteDepartment);
        } else {
            // 记录可能存在但已被逻辑删除，直接调用 mapper 的原生 SQL 恢复
            int affected = this.mapper.restoreById(id);
            ThrowUtils.throwIf(affected <= 0, ErrorCode.NOT_FOUND_ERROR);
            return affected > 0;
        }
    }
}
