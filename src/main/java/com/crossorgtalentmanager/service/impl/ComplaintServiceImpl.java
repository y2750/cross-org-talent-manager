package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.ComplaintMapper;
import com.crossorgtalentmanager.mapper.CompanyMapper;
import com.crossorgtalentmanager.mapper.EmployeeMapper;
import com.crossorgtalentmanager.mapper.UserMapper;
import com.crossorgtalentmanager.model.dto.complaint.ComplaintAddRequest;
import com.crossorgtalentmanager.model.dto.complaint.ComplaintHandleRequest;
import com.crossorgtalentmanager.model.dto.complaint.ComplaintQueryRequest;
import com.crossorgtalentmanager.model.entity.*;
import com.crossorgtalentmanager.model.enums.ComplaintStatusEnum;
import com.crossorgtalentmanager.model.enums.ComplaintTypeEnum;
import com.crossorgtalentmanager.model.enums.EvaluationTypeEnum;
import com.crossorgtalentmanager.model.enums.NotificationTypeEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.ComplaintDetailVO;
import com.crossorgtalentmanager.model.vo.ComplaintVO;
import com.crossorgtalentmanager.model.vo.EvaluationDetailVO;
import com.crossorgtalentmanager.service.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投诉服务实现类
 */
@Slf4j
@Service
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint>
        implements ComplaintService {

    @Resource
    private ComplaintMapper complaintMapper;

    @Resource
    private EvaluationService evaluationService;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private UserService userService;

    @Resource
    private CompanyService companyService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private CompanyPointsService companyPointsService;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CompanyMapper companyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComplaint(ComplaintAddRequest addRequest, User loginUser) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "投诉请求不能为空");
        ThrowUtils.throwIf(addRequest.getEvaluationId() == null, ErrorCode.PARAMS_ERROR, "被投诉的评价ID不能为空");
        ThrowUtils.throwIf(addRequest.getType() == null || !ComplaintTypeEnum.isValidValue(addRequest.getType()),
                ErrorCode.PARAMS_ERROR, "投诉类型无效");
        ThrowUtils.throwIf(addRequest.getTitle() == null || addRequest.getTitle().isBlank(),
                ErrorCode.PARAMS_ERROR, "投诉标题不能为空");
        ThrowUtils.throwIf(addRequest.getContent() == null || addRequest.getContent().isBlank(),
                ErrorCode.PARAMS_ERROR, "投诉内容不能为空");

        // 验证投诉人必须是员工
        ThrowUtils.throwIf(!UserRoleEnum.EMPLOYEE.getValue().equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "只有员工可以提交投诉");

        // 获取被投诉的评价
        Evaluation evaluation = evaluationService.getById(addRequest.getEvaluationId());
        ThrowUtils.throwIf(evaluation == null, ErrorCode.NOT_FOUND_ERROR, "被投诉的评价不存在");
        ThrowUtils.throwIf(evaluation.getIsDelete(), ErrorCode.PARAMS_ERROR, "该评价已被删除");

        // 验证评价是否在30天内
        LocalDate evaluationDate = evaluation.getEvaluationDate();
        if (evaluationDate == null) {
            evaluationDate = evaluation.getCreateTime() != null
                    ? evaluation.getCreateTime().toLocalDate()
                    : LocalDate.now();
        }
        long daysBetween = ChronoUnit.DAYS.between(evaluationDate, LocalDate.now());
        ThrowUtils.throwIf(daysBetween > 30, ErrorCode.PARAMS_ERROR,
                String.format("只能对30天以内的评价进行投诉，该评价已超过%d天", daysBetween));

        // 验证投诉人是否为被评价员工
        Employee complainantEmployee = employeeService.list(
                QueryWrapper.create().eq("user_id", loginUser.getId())).stream()
                .findFirst().orElse(null);
        ThrowUtils.throwIf(complainantEmployee == null, ErrorCode.NOT_FOUND_ERROR, "投诉人信息不存在");
        ThrowUtils.throwIf(!evaluation.getEmployeeId().equals(complainantEmployee.getId()),
                ErrorCode.NO_AUTH_ERROR, "只能对自己的评价进行投诉");

        // 检查是否已经投诉过该评价
        long existingComplaintCount = this.count(
                QueryWrapper.create()
                        .eq("complainant_id", loginUser.getId())
                        .eq("evaluation_id", addRequest.getEvaluationId())
                        .eq("status", ComplaintStatusEnum.PENDING.getValue()));
        ThrowUtils.throwIf(existingComplaintCount > 0, ErrorCode.PARAMS_ERROR, "您已经投诉过该评价，请勿重复投诉");

        // 处理证据图片（转换为JSON）
        String evidenceJson = null;
        if (CollUtil.isNotEmpty(addRequest.getEvidenceImages())) {
            evidenceJson = JSONUtil.toJsonStr(addRequest.getEvidenceImages());
        }

        // 创建投诉记录
        Complaint complaint = Complaint.builder()
                .complainantId(loginUser.getId())
                .evaluationId(addRequest.getEvaluationId())
                .companyId(evaluation.getCompanyId())
                .type(addRequest.getType())
                .title(addRequest.getTitle())
                .content(addRequest.getContent())
                .evidence(evidenceJson)
                .status(ComplaintStatusEnum.PENDING.getValue())
                .isDelete(false)
                .build();

        boolean saved = this.save(complaint);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "提交投诉失败");

        // 获取投诉人姓名（之前已经获取过complainantEmployee，这里直接使用）
        String complainantName = complainantEmployee != null ? complainantEmployee.getName() : "未知员工";

        // 获取被投诉员工信息
        Employee evaluatedEmployee = employeeService.getById(evaluation.getEmployeeId());
        String evaluatedEmployeeName = evaluatedEmployee != null ? evaluatedEmployee.getName() : "未知员工";

        // 获取被投诉公司信息
        String companyName = "未知公司";
        if (evaluation.getCompanyId() != null) {
            Company company = companyService.getById(evaluation.getCompanyId());
            if (company != null) {
                companyName = company.getName();
            }
        }

        // 通知系统管理员
        List<User> adminUsers = userService.list(
                QueryWrapper.create().eq("user_role", UserRoleEnum.ADMIN.getValue()));
        if (CollUtil.isNotEmpty(adminUsers)) {
            for (User admin : adminUsers) {
                notificationService.createNotification(
                        admin.getId(),
                        NotificationTypeEnum.COMPLAINT.getValue(),
                        "新的投诉待处理",
                        String.format("投诉人：%s，被投诉公司：%s，被投诉人：%s", complainantName, companyName, evaluatedEmployeeName),
                        complaint.getId());
            }
        }

        log.info("提交投诉成功：complaintId={}, evaluationId={}, complainantId={}",
                complaint.getId(), addRequest.getEvaluationId(), loginUser.getId());
        return complaint.getId();
    }

    @Override
    public Page<ComplaintVO> pageComplaint(ComplaintQueryRequest queryRequest, User loginUser) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        // 权限控制：只有系统管理员可以查看所有投诉，员工只能查看自己的投诉
        QueryWrapper qw = getQueryWrapper(queryRequest);
        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            // 员工只能查看自己的投诉
            qw.eq("complainant_id", loginUser.getId());
        }

        Page<Complaint> page = this.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), qw);
        Page<ComplaintVO> voPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize(),
                page.getTotalRow());
        List<ComplaintVO> voList = getComplaintVOList(page.getRecords());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ComplaintDetailVO getComplaintDetail(Long id, User loginUser) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "投诉ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        Complaint complaint = this.getById(id);
        ThrowUtils.throwIf(complaint == null, ErrorCode.NOT_FOUND_ERROR, "投诉不存在");

        // 权限控制：只有系统管理员可以查看所有投诉详情，员工只能查看自己的投诉详情
        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            ThrowUtils.throwIf(!complaint.getComplainantId().equals(loginUser.getId()),
                    ErrorCode.NO_AUTH_ERROR, "只能查看自己的投诉");
        }

        ComplaintDetailVO detailVO = new ComplaintDetailVO();
        BeanUtil.copyProperties(complaint, detailVO);

        // 填充投诉类型文本
        ComplaintTypeEnum typeEnum = ComplaintTypeEnum.getEnumByValue(complaint.getType());
        if (typeEnum != null) {
            detailVO.setTypeText(typeEnum.getText());
        }

        // 填充状态文本
        ComplaintStatusEnum statusEnum = ComplaintStatusEnum.getEnumByValue(complaint.getStatus());
        if (statusEnum != null) {
            detailVO.setStatusText(statusEnum.getText());
        }

        // 填充投诉人信息
        User complainant = userService.getById(complaint.getComplainantId());
        if (complainant != null) {
            detailVO.setComplainantName(complainant.getNickname());
        }

        // 填充企业信息
        if (complaint.getCompanyId() != null) {
            Company company = companyService.getById(complaint.getCompanyId());
            if (company != null) {
                detailVO.setCompanyName(company.getName());
            }
        }

        // 填充处理人信息
        if (complaint.getHandlerId() != null) {
            User handler = userService.getById(complaint.getHandlerId());
            if (handler != null) {
                detailVO.setHandlerName(handler.getNickname());
            }
        }

        // 填充证据图片列表
        if (complaint.getEvidence() != null && !complaint.getEvidence().isBlank()) {
            try {
                List<String> evidenceImages = JSONUtil.toList(complaint.getEvidence(), String.class);
                detailVO.setEvidenceImages(evidenceImages);
            } catch (Exception e) {
                log.warn("解析证据JSON失败：complaintId={}, evidence={}", id, complaint.getEvidence(), e);
            }
        }

        // 填充被投诉的评价详情（只有管理员可以看到）
        if (UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()) && complaint.getEvaluationId() != null) {
            try {
                EvaluationDetailVO evaluationDetail = evaluationService.getEvaluationDetail(
                        complaint.getEvaluationId(), loginUser);
                detailVO.setEvaluation(evaluationDetail);
            } catch (Exception e) {
                log.warn("获取评价详情失败：evaluationId={}", complaint.getEvaluationId(), e);
            }
        }

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleComplaint(ComplaintHandleRequest handleRequest, User loginUser) {
        ThrowUtils.throwIf(handleRequest == null || handleRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "投诉ID不能为空");
        ThrowUtils.throwIf(handleRequest.getStatus() == null, ErrorCode.PARAMS_ERROR, "处理状态不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户信息不存在");

        // 验证只有系统管理员可以处理投诉
        ThrowUtils.throwIf(!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "只有系统管理员可以处理投诉");

        // 验证状态值
        ThrowUtils.throwIf(!ComplaintStatusEnum.APPROVED.getValue().equals(handleRequest.getStatus())
                && !ComplaintStatusEnum.REJECTED.getValue().equals(handleRequest.getStatus()),
                ErrorCode.PARAMS_ERROR, "处理状态无效，只能设置为已处理(2)或已驳回(3)");

        // 获取投诉记录
        Complaint complaint = this.getById(handleRequest.getId());
        ThrowUtils.throwIf(complaint == null, ErrorCode.NOT_FOUND_ERROR, "投诉不存在");
        ThrowUtils.throwIf(!ComplaintStatusEnum.PENDING.getValue().equals(complaint.getStatus()),
                ErrorCode.PARAMS_ERROR, "只能处理待处理的投诉");

        // 更新投诉状态
        complaint.setStatus(handleRequest.getStatus());
        complaint.setHandlerId(loginUser.getId());
        complaint.setHandleResult(handleRequest.getHandleResult());
        complaint.setHandleTime(LocalDateTime.now());
        boolean updated = this.updateById(complaint);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新投诉状态失败");

        // 如果投诉通过（已处理）
        if (ComplaintStatusEnum.APPROVED.getValue().equals(handleRequest.getStatus())) {
            // 逻辑删除评价
            Evaluation evaluation = evaluationService.getById(complaint.getEvaluationId());
            if (evaluation != null && !evaluation.getIsDelete()) {
                evaluation.setIsDelete(true);
                evaluationService.updateById(evaluation);
                log.info("投诉通过，已逻辑删除评价：evaluationId={}", evaluation.getId());

                // 判断评价是否来自HR或部门主管
                boolean isFromHrOrLeader = EvaluationTypeEnum.HR.getValue().equals(evaluation.getEvaluationType())
                        || EvaluationTypeEnum.LEADER.getValue().equals(evaluation.getEvaluationType());

                // 如果评价来自HR或部门主管，扣除公司积分50分
                boolean pointsDeducted = false;
                if (isFromHrOrLeader && evaluation.getCompanyId() != null) {
                    try {
                        companyPointsService.addPoints(
                                evaluation.getCompanyId(),
                                new BigDecimal("-50"),
                                com.crossorgtalentmanager.model.enums.PointsChangeReasonEnum.EVALUATION_APPEAL
                                        .getValue(),
                                evaluation.getEmployeeId(),
                                "因投诉通过，扣除积分50分");
                        pointsDeducted = true;
                        log.info("投诉通过，扣除公司积分：companyId={}, points=-50", evaluation.getCompanyId());
                    } catch (Exception e) {
                        log.error("扣除公司积分失败：companyId={}", evaluation.getCompanyId(), e);
                    }
                }

                // 获取被投诉员工信息
                Employee evaluatedEmployee = null;
                String evaluatedEmployeeName = "未知员工";
                if (evaluation.getEmployeeId() != null) {
                    evaluatedEmployee = employeeService.getById(evaluation.getEmployeeId());
                    if (evaluatedEmployee != null) {
                        evaluatedEmployeeName = evaluatedEmployee.getName();
                    }
                }

                // 获取投诉人信息
                Employee complainantEmployee = null;
                String complainantEmployeeName = "未知员工";
                if (complaint.getComplainantId() != null) {
                    List<Employee> complainantEmployees = employeeService.list(
                            QueryWrapper.create().eq("user_id", complaint.getComplainantId()));
                    if (CollUtil.isNotEmpty(complainantEmployees)) {
                        complainantEmployee = complainantEmployees.get(0);
                        complainantEmployeeName = complainantEmployee.getName();
                    }
                }

                // 获取被投诉公司信息
                String companyName = "未知公司";
                if (evaluation.getCompanyId() != null) {
                    Company company = companyService.getById(evaluation.getCompanyId());
                    if (company != null) {
                        companyName = company.getName();
                    }
                }

                // 通知投诉员工：以"被投诉公司：被投诉人姓名"的形式反馈
                notificationService.createNotification(
                        complaint.getComplainantId(),
                        NotificationTypeEnum.COMPLAINT.getValue(),
                        "投诉处理结果",
                        String.format("您对%s：%s的投诉已通过处理，该评价已被删除", companyName, evaluatedEmployeeName),
                        null);

                // 通知评价人（被投诉人）：以"被投诉人：投诉人姓名"的形式
                if (evaluation.getEvaluatorId() != null) {
                    notificationService.createNotification(
                            evaluation.getEvaluatorId(),
                            NotificationTypeEnum.COMPLAINT.getValue(),
                            "评价投诉处理通知",
                            String.format("您对%s的评价因投诉已被删除，投诉人：%s",
                                    complainantEmployeeName, complainantEmployeeName),
                            null);
                }

                // 通知公司管理员：以"被投诉人姓名和投诉人姓名"的形式反馈
                // 如果扣除了积分，需要加上积分扣除提醒
                if (evaluation.getCompanyId() != null) {
                    List<User> companyAdmins = userService.list(
                            QueryWrapper.create()
                                    .eq("company_id", evaluation.getCompanyId())
                                    .eq("user_role", UserRoleEnum.COMPANY_ADMIN.getValue()));
                    if (CollUtil.isNotEmpty(companyAdmins)) {
                        String notificationContent;
                        if (pointsDeducted) {
                            // 如果扣除了积分，添加积分扣除提醒
                            notificationContent = String.format(
                                    "您公司的员工评价因投诉已被删除，被投诉人：%s，投诉人：%s。因该评价来自HR或部门主管，已扣除公司积分50分",
                                    evaluatedEmployeeName, complainantEmployeeName);
                        } else {
                            notificationContent = String.format("您公司的员工评价因投诉已被删除，被投诉人：%s，投诉人：%s",
                                    evaluatedEmployeeName, complainantEmployeeName);
                        }
                        for (User admin : companyAdmins) {
                            notificationService.createNotification(
                                    admin.getId(),
                                    NotificationTypeEnum.COMPLAINT.getValue(),
                                    "评价投诉处理通知",
                                    notificationContent,
                                    null);
                        }
                    }
                }
            }
        } else {
            // 投诉驳回，只通知投诉者
            // 获取被投诉员工信息
            Evaluation evaluation = evaluationService.getById(complaint.getEvaluationId());
            String evaluatedEmployeeName = "未知员工";
            String companyName = "未知公司";

            if (evaluation != null) {
                if (evaluation.getEmployeeId() != null) {
                    Employee evaluatedEmployee = employeeService.getById(evaluation.getEmployeeId());
                    if (evaluatedEmployee != null) {
                        evaluatedEmployeeName = evaluatedEmployee.getName();
                    }
                }
                if (evaluation.getCompanyId() != null) {
                    Company company = companyService.getById(evaluation.getCompanyId());
                    if (company != null) {
                        companyName = company.getName();
                    }
                }
            }

            // 通知投诉者：以"被投诉公司：被投诉人姓名"的形式反馈
            notificationService.createNotification(
                    complaint.getComplainantId(),
                    NotificationTypeEnum.COMPLAINT.getValue(),
                    "投诉处理结果",
                    String.format("您对%s：%s的投诉已被驳回。驳回原因：%s",
                            companyName,
                            evaluatedEmployeeName,
                            handleRequest.getHandleResult() != null ? handleRequest.getHandleResult() : "无"),
                    null);
        }

        log.info("处理投诉完成：complaintId={}, status={}, handlerId={}",
                handleRequest.getId(), handleRequest.getStatus(), loginUser.getId());
        return true;
    }

    /**
     * 将 Complaint 列表转换为 ComplaintVO 列表
     */
    private List<ComplaintVO> getComplaintVOList(List<Complaint> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(this::getComplaintVO).collect(Collectors.toList());
    }

    /**
     * 将 Complaint 实体转换为 ComplaintVO
     */
    private ComplaintVO getComplaintVO(Complaint complaint) {
        if (complaint == null) {
            return null;
        }

        ComplaintVO vo = new ComplaintVO();
        BeanUtil.copyProperties(complaint, vo);

        // 填充投诉类型文本
        ComplaintTypeEnum typeEnum = ComplaintTypeEnum.getEnumByValue(complaint.getType());
        if (typeEnum != null) {
            vo.setTypeText(typeEnum.getText());
        }

        // 填充状态文本
        ComplaintStatusEnum statusEnum = ComplaintStatusEnum.getEnumByValue(complaint.getStatus());
        if (statusEnum != null) {
            vo.setStatusText(statusEnum.getText());
        }

        // 填充投诉人信息
        User complainant = userService.getById(complaint.getComplainantId());
        if (complainant != null) {
            vo.setComplainantName(complainant.getNickname());
        }

        // 填充企业信息
        if (complaint.getCompanyId() != null) {
            Company company = companyService.getById(complaint.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getName());
            }
        }

        // 填充处理人信息
        if (complaint.getHandlerId() != null) {
            User handler = userService.getById(complaint.getHandlerId());
            if (handler != null) {
                vo.setHandlerName(handler.getNickname());
            }
        }

        // 填充证据图片列表
        if (complaint.getEvidence() != null && !complaint.getEvidence().isBlank()) {
            try {
                List<String> evidenceImages = JSONUtil.toList(complaint.getEvidence(), String.class);
                vo.setEvidenceImages(evidenceImages);
            } catch (Exception e) {
                log.warn("解析证据JSON失败：complaintId={}, evidence={}", complaint.getId(), complaint.getEvidence(), e);
            }
        }

        return vo;
    }

    /**
     * 根据查询条件构建 QueryWrapper
     */
    private QueryWrapper getQueryWrapper(ComplaintQueryRequest queryRequest) {
        if (queryRequest == null) {
            return QueryWrapper.create();
        }

        QueryWrapper qw = QueryWrapper.create();

        if (queryRequest.getId() != null) {
            qw.eq("id", queryRequest.getId());
        }

        if (queryRequest.getComplainantId() != null) {
            qw.eq("complainant_id", queryRequest.getComplainantId());
        }

        if (queryRequest.getEvaluationId() != null) {
            qw.eq("evaluation_id", queryRequest.getEvaluationId());
        }

        if (queryRequest.getCompanyId() != null) {
            qw.eq("company_id", queryRequest.getCompanyId());
        }

        if (queryRequest.getType() != null) {
            qw.eq("type", queryRequest.getType());
        }

        if (queryRequest.getStatus() != null) {
            qw.eq("status", queryRequest.getStatus());
        }

        if (queryRequest.getHandlerId() != null) {
            qw.eq("handler_id", queryRequest.getHandlerId());
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
