package com.crossorgtalentmanager.schedule;

import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.service.EmployeeService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工定时任务调度类。
 * 每天 00:00 执行：检索离职超过 24 小时的员工，清空其 companyId 与 departmentId。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Component
public class EmployeeScheduleTask {

    @Resource
    private EmployeeService employeeService;

    /**
     * 每天 00:00 执行：清空已离职超过 24 小时的员工的 companyId 与 departmentId。
     * 查询条件：
     * - status = 0（离职状态）
     * - companyId 不为空
     * - updateTime <= 24 小时前
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void clearCompanyAndDepartmentForFiredEmployees() {
        // 计算 24 小时前的时间
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        // 先查询符合条件的员工
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Employee::getStatus, false)
                .isNotNull(Employee::getCompanyId)
                .le(Employee::getUpdateTime, twentyFourHoursAgo);
        List<Employee> employees = employeeService.list(queryWrapper);

        // 批量更新符合条件的员工：将 companyId 和 departmentId 置空
        if (!employees.isEmpty()) {
            for (Employee employee : employees) {
                employee.setCompanyId(null);
                employee.setDepartmentId(null);
                employeeService.updateById(employee);
            }
        }
    }
}
