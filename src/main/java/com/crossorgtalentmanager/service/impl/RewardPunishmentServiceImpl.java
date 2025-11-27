package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.RewardPunishmentMapper;
import com.crossorgtalentmanager.model.enums.RewardPunishmentTypeEnum;
import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentAddRequest;
import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentQueryRequest;
import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentUpdateRequest;
import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.model.entity.RewardPunishment;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.RewardPunishmentVO;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.service.RewardPunishmentService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 奖惩记录 服务层实现。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Service
@Slf4j
public class RewardPunishmentServiceImpl extends ServiceImpl<RewardPunishmentMapper, RewardPunishment>
        implements RewardPunishmentService {

    @Resource
    private EmployeeService employeeService;

    @Resource
    private com.crossorgtalentmanager.service.UserService userService;

    @Override
    public Long addRewardPunishment(RewardPunishmentAddRequest addRequest, User loginUser) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");
        ThrowUtils.throwIf(addRequest.getEmployeeId() == null || addRequest.getEmployeeId() <= 0,
                ErrorCode.PARAMS_ERROR, "员工 ID 不能为空");
        ThrowUtils.throwIf(addRequest.getType() == null,
                ErrorCode.PARAMS_ERROR, "奖惩类型不能为空");
        ThrowUtils.throwIf(!RewardPunishmentTypeEnum.isValidType(addRequest.getType()),
                ErrorCode.PARAMS_ERROR, "奖惩类型无效");

        // 查询员工信息并校验其公司 ID 与操作人员的公司 ID 是否一致
        Employee employee = employeeService.getById(addRequest.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        Long loginUserCompanyId = loginUser.getCompanyId();
        ThrowUtils.throwIf(loginUserCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");
        ThrowUtils.throwIf(!loginUserCompanyId.equals(employee.getCompanyId()), ErrorCode.NO_AUTH_ERROR,
                "员工所属公司与操作人员不一致");

        RewardPunishment record = new RewardPunishment();
        record.setEmployeeId(addRequest.getEmployeeId());
        record.setCompanyId(employee.getCompanyId());
        record.setType(addRequest.getType());
        record.setDescription(addRequest.getDescription());
        record.setAmount(addRequest.getAmount());
        record.setDate(addRequest.getDate());
        record.setOperatorId(loginUser.getId());
        boolean save = this.save(record);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "新增失败");
        return record.getId();
    }

    @Override
    public Boolean updateRewardPunishment(RewardPunishmentUpdateRequest updateRequest, User loginUser) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "奖惩记录 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        // 如果提供了类型，需要验证其有效性
        if (updateRequest.getType() != null) {
            ThrowUtils.throwIf(!RewardPunishmentTypeEnum.isValidType(updateRequest.getType()),
                    ErrorCode.PARAMS_ERROR, "奖惩类型无效，请选择：1（奖励）或 2（惩罚）");
        }

        // 查询现有奖惩记录
        RewardPunishment existingRecord = this.getById(updateRequest.getId());
        ThrowUtils.throwIf(existingRecord == null, ErrorCode.NOT_FOUND_ERROR, "奖惩记录不存在");

        // 检查用户角色，非系统管理员需要校验权限
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            // 非系统管理员只能修改本公司产生的奖惩记录
            Long loginUserCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginUserCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

            // 检查奖惩记录的公司ID是否等于登录用户的公司ID
            Long recordCompanyId = existingRecord.getCompanyId();
            ThrowUtils.throwIf(recordCompanyId == null || !loginUserCompanyId.equals(recordCompanyId),
                    ErrorCode.NO_AUTH_ERROR, "只能修改本公司产生的奖惩记录");
        }

        // 查询员工信息（用于更新时设置公司ID）
        Employee employee = employeeService.getById(existingRecord.getEmployeeId());
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

        // 使用 DTO 的字段覆盖现有记录
        RewardPunishment recordToUpdate = new RewardPunishment();
        recordToUpdate.setId(updateRequest.getId());
        recordToUpdate.setCompanyId(employee.getCompanyId());
        recordToUpdate.setType(updateRequest.getType());
        recordToUpdate.setDescription(updateRequest.getDescription());
        recordToUpdate.setAmount(updateRequest.getAmount());
        recordToUpdate.setDate(updateRequest.getDate());
        recordToUpdate.setOperatorId(loginUser.getId());

        boolean update = this.updateById(recordToUpdate);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新失败");
        return true;
    }

    @Override
    public RewardPunishmentVO getRewardPunishmentVO(RewardPunishment rewardPunishment) {
        if (rewardPunishment == null) {
            return null;
        }
        RewardPunishmentVO vo = new RewardPunishmentVO();
        BeanUtil.copyProperties(rewardPunishment, vo);

        // 重要：companyId 应该使用奖惩记录本身的 companyId（记录产生时的公司ID）
        // 而不是员工当前的公司ID，这样才能正确判断记录是否属于当前用户公司
        vo.setCompanyId(rewardPunishment.getCompanyId());

        // 填充员工信息
        if (rewardPunishment.getEmployeeId() != null) {
            Employee employee = employeeService.getById(rewardPunishment.getEmployeeId());
            if (employee != null) {
                vo.setEmployeeName(employee.getName());
                // 不再覆盖 companyId，保持使用奖惩记录本身的 companyId
            }
        }

        // 填充操作人员名称
        if (rewardPunishment.getOperatorId() != null) {
            User operator = userService.getById(rewardPunishment.getOperatorId());
            if (operator != null) {
                vo.setOperatorName(operator.getNickname());
            }
        }

        return vo;
    }

    @Override
    public List<RewardPunishmentVO> getRewardPunishmentVOList(List<RewardPunishment> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 先转换为 VO
        List<RewardPunishmentVO> voList = list.stream().map(this::getRewardPunishmentVO).toList();

        // 批量填充员工信息以减少查询
        Set<Long> employeeIds = list.stream()
                .map(RewardPunishment::getEmployeeId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        if (!employeeIds.isEmpty()) {
            List<Employee> employees = employeeService.listByIds(employeeIds);
            Map<Long, Employee> employeeIdToEmployee = employees.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(Employee::getId, e -> e, (a, b) -> a));

            // 创建奖惩记录ID到奖惩记录的映射，用于获取原始记录的companyId
            Map<Long, RewardPunishment> recordIdToRecord = list.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(RewardPunishment::getId, r -> r, (a, b) -> a));

            for (RewardPunishmentVO vo : voList) {
                if (vo.getEmployeeId() != null) {
                    Employee employee = employeeIdToEmployee.get(vo.getEmployeeId());
                    if (employee != null) {
                        vo.setEmployeeName(employee.getName());
                        // 重要：保持使用奖惩记录本身的 companyId（记录产生时的公司ID）
                        // 而不是员工当前的公司ID，这样才能正确判断记录是否属于当前用户公司
                        RewardPunishment originalRecord = recordIdToRecord.get(vo.getId());
                        if (originalRecord != null && originalRecord.getCompanyId() != null) {
                            vo.setCompanyId(originalRecord.getCompanyId());
                        }
                    }
                }
            }
        }

        // 批量填充操作人员名称以减少查询
        Set<Long> operatorIds = list.stream()
                .map(RewardPunishment::getOperatorId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        if (!operatorIds.isEmpty()) {
            List<User> operators = userService.listByIds(operatorIds);
            Map<Long, String> operatorIdToName = operators.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

            for (RewardPunishmentVO vo : voList) {
                if (vo.getOperatorId() != null) {
                    vo.setOperatorName(operatorIdToName.get(vo.getOperatorId()));
                }
            }
        }

        return voList;
    }

    @Override
    public QueryWrapper getQueryWrapper(RewardPunishmentQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Long companyId = request.getCompanyId();
        Long employeeId = request.getEmployeeId();
        String employeeName = request.getEmployeeName();
        String idCardNumber = request.getIdCardNumber();
        Long departmentId = request.getDepartmentId();
        String gender = request.getGender();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        QueryWrapper qw = QueryWrapper.create();

        // 如果同时指定了员工ID和公司ID，直接通过奖惩记录的companyId和employeeId过滤
        // 这样可以查询该员工在该公司产生的所有奖惩记录
        if (employeeId != null && employeeId > 0 && companyId != null && companyId > 0) {
            qw.eq("employee_id", employeeId);
            qw.eq("company_id", companyId);
            if (sortField != null && !sortField.isEmpty()) {
                qw.orderBy(sortField, "ascend".equals(sortOrder));
            }
            return qw;
        }

        // 如果指定了公司 ID，先获取该公司的所有员工，然后通过 employee_id 过滤
        // 这样可以查询本公司员工的所有奖惩记录（包括在其他公司产生的记录）
        Set<Long> companyEmployeeIds = null;
        if (companyId != null && companyId > 0) {
            List<Employee> companyEmployees = employeeService
                    .list(QueryWrapper.create().eq("company_id", companyId));
            if (companyEmployees == null || companyEmployees.isEmpty()) {
                return QueryWrapper.create().eq("id", -1);
            }
            companyEmployeeIds = companyEmployees.stream().map(Employee::getId).collect(Collectors.toSet());
        }

        // 收集所有符合条件的员工ID
        Set<Long> finalEmployeeIds = null;

        // 如果指定了员工ID，直接使用
        if (employeeId != null && employeeId > 0) {
            // 如果指定了公司ID，需要验证该员工是否属于该公司
            if (companyEmployeeIds != null && !companyEmployeeIds.contains(employeeId)) {
                return QueryWrapper.create().eq("id", -1);
            }
            finalEmployeeIds = new java.util.HashSet<>();
            finalEmployeeIds.add(employeeId);
        } else {
            // 根据员工信息进行精确或模糊查询，收集符合条件的员工ID
            List<Set<Long>> employeeIdSets = new ArrayList<>();

            if (employeeName != null && !employeeName.isEmpty()) {
                QueryWrapper employeeQw = QueryWrapper.create().like("name", employeeName);
                if (companyId != null && companyId > 0) {
                    employeeQw.eq("company_id", companyId);
                }
                List<Employee> employees = employeeService.list(employeeQw);
                if (employees != null && !employees.isEmpty()) {
                    Set<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toSet());
                    employeeIdSets.add(employeeIds);
                } else {
                    return QueryWrapper.create().eq("id", -1);
                }
            }

            if (idCardNumber != null && !idCardNumber.isEmpty()) {
                QueryWrapper employeeQw = QueryWrapper.create().eq("id_card_number", idCardNumber);
                if (companyId != null && companyId > 0) {
                    employeeQw.eq("company_id", companyId);
                }
                List<Employee> employees = employeeService.list(employeeQw);
                if (employees != null && !employees.isEmpty()) {
                    Set<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toSet());
                    employeeIdSets.add(employeeIds);
                } else {
                    return QueryWrapper.create().eq("id", -1);
                }
            }

            if (departmentId != null && departmentId > 0) {
                QueryWrapper employeeQw = QueryWrapper.create().eq("department_id", departmentId);
                if (companyId != null && companyId > 0) {
                    employeeQw.eq("company_id", companyId);
                }
                List<Employee> employees = employeeService.list(employeeQw);
                if (employees != null && !employees.isEmpty()) {
                    Set<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toSet());
                    employeeIdSets.add(employeeIds);
                } else {
                    return QueryWrapper.create().eq("id", -1);
                }
            }

            if (gender != null && !gender.isEmpty()) {
                QueryWrapper employeeQw = QueryWrapper.create().eq("gender", gender);
                if (companyId != null && companyId > 0) {
                    employeeQw.eq("company_id", companyId);
                }
                List<Employee> employees = employeeService.list(employeeQw);
                if (employees != null && !employees.isEmpty()) {
                    Set<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toSet());
                    employeeIdSets.add(employeeIds);
                } else {
                    return QueryWrapper.create().eq("id", -1);
                }
            }

            // 如果有多个条件，取交集；如果只有一个条件，直接使用
            if (!employeeIdSets.isEmpty()) {
                finalEmployeeIds = new java.util.HashSet<>(employeeIdSets.get(0));
                for (int i = 1; i < employeeIdSets.size(); i++) {
                    finalEmployeeIds.retainAll(employeeIdSets.get(i));
                }
                if (finalEmployeeIds.isEmpty()) {
                    return QueryWrapper.create().eq("id", -1);
                }
            } else if (companyEmployeeIds != null) {
                // 如果没有指定其他条件，但指定了公司ID，则使用该公司的所有员工ID
                finalEmployeeIds = companyEmployeeIds;
            }
        }

        // 如果指定了公司ID，需要与公司员工ID取交集
        if (companyEmployeeIds != null && finalEmployeeIds != null) {
            finalEmployeeIds.retainAll(companyEmployeeIds);
            if (finalEmployeeIds.isEmpty()) {
                return QueryWrapper.create().eq("id", -1);
            }
        }

        // 使用最终确定的员工ID列表来过滤奖惩记录
        if (finalEmployeeIds != null && !finalEmployeeIds.isEmpty()) {
            if (finalEmployeeIds.size() == 1) {
                qw.eq("employee_id", finalEmployeeIds.iterator().next());
            } else {
                qw.in("employee_id", finalEmployeeIds);
            }
        }

        if (sortField != null && !sortField.isEmpty()) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        }

        return qw;
    }

    @Override
    public Boolean removeById(Long id) {
        RewardPunishment record = getById(id);
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR);
        RewardPunishment copy = new RewardPunishment();
        BeanUtil.copyProperties(record, copy);
        copy.setIsDelete(!Boolean.TRUE.equals(copy.getIsDelete()));
        return updateById(copy);
    }
}
