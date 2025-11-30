package com.crossorgtalentmanager.schedule;

import com.crossorgtalentmanager.model.entity.Department;
import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.enums.EvaluationPeriodEnum;
import com.crossorgtalentmanager.model.enums.EvaluationTypeEnum;
import com.crossorgtalentmanager.service.DepartmentService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.service.EvaluationTaskService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价定时任务
 */
@Slf4j
@Component
public class EvaluationScheduleTask {

    @Resource
    private EvaluationTaskService evaluationTaskService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private EmployeeService employeeService;

    /**
     * 季度评价任务自动创建
     * 每季度第一天执行：1月1日、4月1日、7月1日、10月1日
     * cron表达式：秒 分 时 日 月 周
     * 0 0 0 1 1,4,7,10 ? - 每年1月、4月、7月、10月的第1天0点0分0秒执行
     */
    @Scheduled(cron = "0 0 0 1 1,4,7,10 ?")
    public void generateQuarterlyEvaluationTasks() {
        log.info("开始自动创建季度评价任务");
        try {
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            int quarter = (month - 1) / 3 + 1;

            log.info("当前年份：{}，季度：{}", year, quarter);

            // 获取所有有主管的部门
            List<Department> departments = departmentService.list(
                QueryWrapper.create()
                    .isNotNull("leader_id")
            );

            if (departments == null || departments.isEmpty()) {
                log.info("没有找到有主管的部门");
                return;
            }

            int totalTasks = 0;

            // 为每个部门创建季度评价任务
            for (Department department : departments) {
                if (department.getLeaderId() == null) {
                    continue;
                }

                // 获取部门主管
                Employee leader = employeeService.getById(department.getLeaderId());
                if (leader == null || leader.getUserId() == null) {
                    log.warn("部门 {} 的主管不存在或没有关联用户", department.getId());
                    continue;
                }

                // 获取部门所有员工（排除主管）
                List<Employee> employees = employeeService.list(
                    QueryWrapper.create()
                        .eq("department_id", department.getId())
                        .eq("status", true)
                        .ne("id", department.getLeaderId())
                );

                if (employees == null || employees.isEmpty()) {
                    log.info("部门 {} 没有需要评价的员工", department.getId());
                    continue;
                }

                // 为部门主管创建评价任务（评价部门所有员工）
                // 创建评价任务请求
                com.crossorgtalentmanager.model.dto.evaluation.EvaluationTaskCreateRequest createRequest = 
                    new com.crossorgtalentmanager.model.dto.evaluation.EvaluationTaskCreateRequest();
                createRequest.setEmployeeIds(employees.stream().map(Employee::getId).toList());
                createRequest.setEvaluationType(EvaluationTypeEnum.LEADER.getValue());
                createRequest.setEvaluationPeriod(EvaluationPeriodEnum.QUARTERLY.getValue());
                createRequest.setPeriodYear(year);
                createRequest.setPeriodQuarter(quarter);
                
                // 季度评价默认截止时间为季度结束后的15天
                LocalDateTime quarterEnd = LocalDateTime.of(year, (quarter - 1) * 3 + 3, 1, 0, 0)
                    .plusMonths(1).minusDays(1).plusDays(15);
                createRequest.setDeadline(quarterEnd);

                // 创建一个虚拟的User对象用于创建任务（实际上不需要loginUser，但接口需要）
                User systemUser = new User();
                systemUser.setId(leader.getUserId());
                systemUser.setCompanyId(leader.getCompanyId());

                int taskCount = evaluationTaskService.createEvaluationTasks(createRequest, systemUser);
                totalTasks += taskCount;
                log.info("为部门 {} 的主管创建了 {} 个季度评价任务", department.getId(), taskCount);
            }

            log.info("季度评价任务创建完成，共创建 {} 个任务，年份：{}，季度：{}", totalTasks, year, quarter);
        } catch (Exception e) {
            log.error("自动创建季度评价任务失败", e);
        }
    }
}

