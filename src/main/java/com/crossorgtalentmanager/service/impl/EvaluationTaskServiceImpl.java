package com.crossorgtalentmanager.service.impl;

import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.*;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationTaskCreateRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationTaskQueryRequest;
import com.crossorgtalentmanager.model.entity.*;
import com.crossorgtalentmanager.model.enums.EvaluationPeriodEnum;
import com.crossorgtalentmanager.model.enums.EvaluationTypeEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.EvaluationTaskVO;
import com.crossorgtalentmanager.service.DepartmentService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.service.EvaluationTaskService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价任务服务实现类
 */
@Slf4j
@Service
public class EvaluationTaskServiceImpl extends ServiceImpl<EvaluationTaskMapper, EvaluationTask>
        implements EvaluationTaskService {

    @Resource
    private EvaluationTaskMapper evaluationTaskMapper;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createEvaluationTasks(EvaluationTaskCreateRequest createRequest, User loginUser) {
        ThrowUtils.throwIf(createRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(createRequest.getEvaluationType() == null, ErrorCode.PARAMS_ERROR, "评价类型不能为空");
        ThrowUtils.throwIf(createRequest.getEvaluationPeriod() == null, ErrorCode.PARAMS_ERROR, "评价周期不能为空");

        List<Long> employeeIds = createRequest.getEmployeeIds();
        if (employeeIds == null || employeeIds.isEmpty()) {
            if (createRequest.getEmployeeId() != null) {
                employeeIds = List.of(createRequest.getEmployeeId());
            } else {
                ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "员工ID列表不能为空");
            }
        }

        List<Long> evaluatorIds = new ArrayList<>();

        // 根据评价类型确定评价人
        EvaluationTypeEnum typeEnum = EvaluationTypeEnum.getEnumByValue(createRequest.getEvaluationType());
        ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "无效的评价类型");

        switch (typeEnum) {
            case LEADER:
                // 领导评价：为每个员工找到其部门主管
                for (Long employeeId : employeeIds) {
                    Employee employee = employeeService.getById(employeeId);
                    if (employee != null && employee.getDepartmentId() != null) {
                        List<Department> departments = departmentService.list(
                                QueryWrapper.create().eq("id", employee.getDepartmentId()));
                        if (departments != null && !departments.isEmpty()) {
                            Department department = departments.get(0);
                            if (department.getLeaderId() != null) {
                                Employee leader = employeeService.getById(department.getLeaderId());
                                if (leader != null && leader.getUserId() != null) {
                                    evaluatorIds.add(leader.getUserId());
                                }
                            }
                        }
                    }
                }
                break;
            case HR:
                // HR评价：找到该公司的所有HR
                if (loginUser.getCompanyId() != null) {
                    List<User> hrUsers = userService.list(
                            QueryWrapper.create()
                                    .eq("company_id", loginUser.getCompanyId())
                                    .eq("user_role", UserRoleEnum.HR.getValue()));
                    evaluatorIds = hrUsers.stream().map(User::getId).collect(Collectors.toList());
                }
                break;
            case COLLEAGUE:
                // 同事评价：为每个被评价员工找到同部门同事作为评价人（排除部门主管）
                for (Long employeeId : employeeIds) {
                    Employee employee = employeeService.getById(employeeId);
                    if (employee != null && employee.getDepartmentId() != null) {
                        // 获取部门信息，找到部门主管ID
                        List<Department> departments = departmentService.list(
                                QueryWrapper.create().eq("id", employee.getDepartmentId()));
                        Long leaderId = null;
                        if (departments != null && !departments.isEmpty()) {
                            leaderId = departments.get(0).getLeaderId();
                        }

                        // 获取同部门同事（排除被评价员工和部门主管）
                        List<Employee> colleagues = employeeService.list(
                                QueryWrapper.create()
                                        .eq("department_id", employee.getDepartmentId())
                                        .eq("status", true)
                                        .ne("id", employeeId));
                        for (Employee colleague : colleagues) {
                            // 排除部门主管
                            if (leaderId != null && colleague.getId().equals(leaderId)) {
                                continue;
                            }
                            if (colleague.getUserId() != null && !evaluatorIds.contains(colleague.getUserId())) {
                                evaluatorIds.add(colleague.getUserId());
                            }
                        }
                    }
                }
                break;
            case SELF:
                // 自评：员工自己
                for (Long employeeId : employeeIds) {
                    Employee employee = employeeService.getById(employeeId);
                    if (employee != null && employee.getUserId() != null) {
                        evaluatorIds.add(employee.getUserId());
                    }
                }
                break;
        }

        if (evaluatorIds.isEmpty()) {
            log.warn("未找到评价人，无法创建评价任务");
            return 0;
        }

        // 创建任务
        int taskCount = 0;
        LocalDateTime deadline = createRequest.getDeadline();
        if (deadline == null) {
            // 默认截止时间为30天后
            deadline = LocalDateTime.now().plusDays(30);
        }

        for (Long employeeId : employeeIds) {
            // 获取员工信息，保存部门信息到任务中（避免员工离职后无法查询）
            Employee employee = employeeService.getById(employeeId);
            Long departmentId = null;
            String departmentName = null;

            if (employee != null && employee.getDepartmentId() != null) {
                departmentId = employee.getDepartmentId();
                List<Department> departments = departmentService.list(
                        QueryWrapper.create().eq("id", employee.getDepartmentId()));
                if (departments != null && !departments.isEmpty()) {
                    departmentName = departments.get(0).getName();
                }
            }

            for (Long evaluatorId : evaluatorIds) {
                // 检查是否已存在相同任务
                long existingCount = this.count(
                        QueryWrapper.create()
                                .eq("employee_id", employeeId)
                                .eq("evaluator_id", evaluatorId)
                                .eq("evaluation_type", createRequest.getEvaluationType())
                                .eq("evaluation_period", createRequest.getEvaluationPeriod())
                                .eq("period_year", createRequest.getPeriodYear())
                                .eq("period_quarter", createRequest.getPeriodQuarter())
                                .eq("status", 0) // 只检查待评价的任务
                );

                if (existingCount == 0) {
                    EvaluationTask task = EvaluationTask.builder()
                            .employeeId(employeeId)
                            .departmentId(departmentId)
                            .departmentName(departmentName)
                            .evaluatorId(evaluatorId)
                            .evaluationType(createRequest.getEvaluationType())
                            .evaluationPeriod(createRequest.getEvaluationPeriod())
                            .periodYear(createRequest.getPeriodYear())
                            .periodQuarter(createRequest.getPeriodQuarter())
                            .status(0) // 待评价
                            .deadline(deadline)
                            .isDelete(false)
                            .build();
                    evaluationTaskMapper.insert(task);
                    taskCount++;

                    // 创建通知（如果notification表已实现）
                    // createNotification(task, evaluatorId);
                }
            }
        }

        log.info("创建了 {} 个评价任务", taskCount);
        return taskCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createQuarterlyEvaluationTasks(Long departmentId, Integer periodYear, Integer periodQuarter,
            User loginUser) {
        ThrowUtils.throwIf(departmentId == null, ErrorCode.PARAMS_ERROR, "部门ID不能为空");
        ThrowUtils.throwIf(periodYear == null, ErrorCode.PARAMS_ERROR, "评价年份不能为空");
        ThrowUtils.throwIf(periodQuarter == null || periodQuarter < 1 || periodQuarter > 4,
                ErrorCode.PARAMS_ERROR, "评价季度必须在1-4之间");

        // 验证是否为部门主管
        List<Department> departments = departmentService.list(
                QueryWrapper.create().eq("id", departmentId));
        ThrowUtils.throwIf(departments == null || departments.isEmpty(),
                ErrorCode.NOT_FOUND_ERROR, "部门不存在");
        Department department = departments.get(0);

        // 获取部门主管的员工ID
        ThrowUtils.throwIf(department.getLeaderId() == null,
                ErrorCode.NO_AUTH_ERROR, "该部门没有主管");

        Employee leader = employeeService.getById(department.getLeaderId());
        ThrowUtils.throwIf(leader == null, ErrorCode.NOT_FOUND_ERROR, "部门主管不存在");
        ThrowUtils.throwIf(leader.getUserId() == null || !leader.getUserId().equals(loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR, "只有部门主管可以创建季度评价任务");

        // 获取部门所有员工（排除主管）
        List<Employee> employees = employeeService.list(
                QueryWrapper.create()
                        .eq("department_id", departmentId)
                        .eq("status", true)
                        .ne("id", department.getLeaderId()));

        if (employees.isEmpty()) {
            log.info("部门 {} 没有需要评价的员工", departmentId);
            return 0;
        }

        // 创建评价任务请求
        // 注意：季度评价任务只推送给部门主管（作为评价人）
        EvaluationTaskCreateRequest createRequest = new EvaluationTaskCreateRequest();
        createRequest.setEmployeeIds(employees.stream().map(Employee::getId).collect(Collectors.toList()));
        createRequest.setEvaluationType(EvaluationTypeEnum.LEADER.getValue());
        createRequest.setEvaluationPeriod(EvaluationPeriodEnum.QUARTERLY.getValue());
        createRequest.setPeriodYear(periodYear);
        createRequest.setPeriodQuarter(periodQuarter);
        // 季度评价默认截止时间为季度结束后的15天
        LocalDateTime quarterEnd = LocalDateTime.of(periodYear, (periodQuarter - 1) * 3 + 3, 1, 0, 0)
                .plusMonths(1).minusDays(1).plusDays(15);
        createRequest.setDeadline(quarterEnd);

        // 创建任务，任务会自动推送给部门主管（因为evaluationType是LEADER，createEvaluationTasks会自动找到部门主管）
        return createEvaluationTasks(createRequest, loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeTask(Long evaluationId, Long evaluatorId, Long employeeId,
            Integer evaluationPeriod, Integer periodYear, Integer periodQuarter) {
        ThrowUtils.throwIf(evaluationId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(evaluatorId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(employeeId == null, ErrorCode.PARAMS_ERROR);

        // 查找对应的任务
        QueryWrapper qw = QueryWrapper.create()
                .eq("employee_id", employeeId)
                .eq("evaluator_id", evaluatorId)
                .eq("evaluation_period", evaluationPeriod)
                .eq("status", 0); // 待评价状态

        if (periodYear != null) {
            qw.eq("period_year", periodYear);
        }
        if (periodQuarter != null) {
            qw.eq("period_quarter", periodQuarter);
        }

        List<EvaluationTask> tasks = this.list(qw);
        if (tasks.isEmpty()) {
            log.warn("未找到对应的评价任务，evaluationId: {}, evaluatorId: {}, employeeId: {}",
                    evaluationId, evaluatorId, employeeId);
            return false;
        }

        // 更新任务状态为已完成
        for (EvaluationTask task : tasks) {
            task.setStatus(1); // 已完成
            task.setEvaluationId(evaluationId);
            evaluationTaskMapper.update(task);
        }

        return true;
    }

    @Override
    public Long getPendingTaskCount(Long evaluatorId) {
        return this.count(
                QueryWrapper.create()
                        .eq("evaluator_id", evaluatorId)
                        .eq("status", 0) // 待评价
        );
    }

    @Override
    public Page<EvaluationTaskVO> pageEvaluationTasks(EvaluationTaskQueryRequest queryRequest, User loginUser) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);

        QueryWrapper qw = getQueryWrapper(queryRequest);

        // 权限控制：非系统管理员只能查看自己的任务
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        if (!isAdmin) {
            qw.eq("evaluator_id", loginUser.getId());
        }

        Page<EvaluationTask> page = this.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), qw);
        Page<EvaluationTaskVO> voPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(),
                page.getTotalRow());
        List<EvaluationTaskVO> voList = getEvaluationTaskVOList(page.getRecords());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public EvaluationTaskVO getEvaluationTaskVO(EvaluationTask task) {
        if (task == null) {
            return null;
        }

        EvaluationTaskVO vo = new EvaluationTaskVO();
        cn.hutool.core.bean.BeanUtil.copyProperties(task, vo);

        // 填充员工姓名
        Employee employee = employeeService.getById(task.getEmployeeId());
        if (employee != null) {
            vo.setEmployeeName(employee.getName());
        }

        // 填充部门信息（优先使用任务中保存的部门信息，避免员工离职后无法查询）
        if (task.getDepartmentName() != null) {
            // 使用任务创建时保存的部门信息
            vo.setDepartmentName(task.getDepartmentName());
        } else if (employee != null && employee.getDepartmentId() != null) {
            // 如果任务中没有保存部门信息，尝试从员工信息中获取（适用于未离职的员工）
            List<Department> departments = departmentService.list(
                    QueryWrapper.create().eq("id", employee.getDepartmentId()));
            if (departments != null && !departments.isEmpty()) {
                vo.setDepartmentName(departments.get(0).getName());
            }
        }

        // 填充评价人姓名
        User evaluator = userService.getById(task.getEvaluatorId());
        if (evaluator != null) {
            vo.setEvaluatorName(evaluator.getNickname());
        }

        // 填充评价类型文本
        EvaluationTypeEnum typeEnum = EvaluationTypeEnum.getEnumByValue(task.getEvaluationType());
        if (typeEnum != null) {
            vo.setEvaluationTypeText(typeEnum.getText());
        }

        // 填充评价周期文本
        if (task.getEvaluationPeriod() != null) {
            EvaluationPeriodEnum periodEnum = EvaluationPeriodEnum.getEnumByValue(task.getEvaluationPeriod());
            if (periodEnum != null) {
                vo.setEvaluationPeriodText(periodEnum.getText());
            }
        }

        // 填充任务状态文本
        if (task.getStatus() != null) {
            String[] statusTexts = { "待评价", "已完成", "已过期" };
            if (task.getStatus() >= 0 && task.getStatus() < statusTexts.length) {
                vo.setStatusText(statusTexts[task.getStatus()]);
            }
        }

        return vo;
    }

    @Override
    public List<EvaluationTaskVO> getEvaluationTaskVOList(List<EvaluationTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }
        return tasks.stream()
                .map(this::getEvaluationTaskVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(EvaluationTaskQueryRequest queryRequest) {
        if (queryRequest == null) {
            return QueryWrapper.create();
        }

        QueryWrapper qw = QueryWrapper.create();

        if (queryRequest.getEmployeeId() != null) {
            qw.eq("employee_id", queryRequest.getEmployeeId());
        }

        if (queryRequest.getEvaluatorId() != null) {
            qw.eq("evaluator_id", queryRequest.getEvaluatorId());
        }

        if (queryRequest.getEvaluationType() != null) {
            qw.eq("evaluation_type", queryRequest.getEvaluationType());
        }

        if (queryRequest.getEvaluationPeriod() != null) {
            qw.eq("evaluation_period", queryRequest.getEvaluationPeriod());
        }

        if (queryRequest.getStatus() != null) {
            qw.eq("status", queryRequest.getStatus());
        }

        if (queryRequest.getPeriodYear() != null) {
            qw.eq("period_year", queryRequest.getPeriodYear());
        }

        if (queryRequest.getPeriodQuarter() != null) {
            qw.eq("period_quarter", queryRequest.getPeriodQuarter());
        }

        // 排序
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        if (sortField != null && !sortField.isEmpty()) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            qw.orderBy("create_time", false);
        }

        return qw;
    }
}
