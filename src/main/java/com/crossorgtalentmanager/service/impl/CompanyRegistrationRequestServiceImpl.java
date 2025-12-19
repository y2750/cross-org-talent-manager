package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.CompanyRegistrationRequestMapper;
import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationAddRequest;
import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationApproveRequest;
import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationQueryRequest;
import com.crossorgtalentmanager.model.entity.CompanyRegistrationRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.enums.CompanyRegistrationStatusEnum;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import com.crossorgtalentmanager.model.vo.CompanyRegistrationRequestVO;
import com.crossorgtalentmanager.service.CompanyRegistrationRequestService;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.NotificationService;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.service.mail.QqMailService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业注册申请 服务实现
 */
@Service
@Slf4j
public class CompanyRegistrationRequestServiceImpl
        extends ServiceImpl<CompanyRegistrationRequestMapper, CompanyRegistrationRequest>
        implements CompanyRegistrationRequestService {

    @Resource
    private UserService userService;

    @Resource
    private CompanyService companyService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private QqMailService qqMailService;

    /**
     * 默认初始密码
     */
    private static final String DEFAULT_PASSWORD = "12345678";

    @Override
    public Long createRegistration(CompanyRegistrationAddRequest addRequest) {
        if (addRequest == null) {
            throw new BusinessException(com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        ThrowUtils.throwIf(addRequest.getCompanyName() == null
                || addRequest.getAddress() == null
                || addRequest.getCompanyEmail() == null
                || addRequest.getAdminName() == null
                || addRequest.getAdminPhone() == null
                || addRequest.getAdminEmail() == null
                || addRequest.getAdminIdNumber() == null
                || addRequest.getAdminUsername() == null
                || addRequest.getIndustryCategory() == null,
                com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "必填项不能为空");

        // 校验账号名格式（4-20个字符，只能包含字母、数字、下划线）
        String adminUsername = addRequest.getAdminUsername();
        if (adminUsername.length() < 4 || adminUsername.length() > 20) {
            throw new BusinessException(com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR,
                    "账号名长度必须在4-20个字符之间");
        }
        if (!adminUsername.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR,
                    "账号名只能包含字母、数字和下划线");
        }

        // 检查账号名是否已存在
        Long usernameCount = userService.count(
                QueryWrapper.create().eq("username", adminUsername));
        ThrowUtils.throwIf(usernameCount != null && usernameCount > 0,
                com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR,
                "该账号名已被占用，请更换其他账号名");

        // 校验企业名称是否重复（仅在申请表中做简单检查，最终创建企业时再由 CompanyService 校验）
        Long pendingCount = this.count(
                QueryWrapper.create()
                        .eq("company_name", addRequest.getCompanyName())
                        .eq("status", CompanyRegistrationStatusEnum.PENDING.getValue()));
        ThrowUtils.throwIf(pendingCount != null && pendingCount > 0,
                com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR,
                "该企业已提交待处理申请，请耐心等待管理员审核");

        CompanyRegistrationRequest request = new CompanyRegistrationRequest();
        request.setCompanyName(addRequest.getCompanyName());
        request.setAddress(addRequest.getAddress());
        request.setCompanyEmail(addRequest.getCompanyEmail());
        request.setAdminName(addRequest.getAdminName());
        request.setAdminPhone(addRequest.getAdminPhone());
        request.setAdminEmail(addRequest.getAdminEmail());
        request.setAdminIdNumber(addRequest.getAdminIdNumber());
        request.setAdminUsername(addRequest.getAdminUsername());
        request.setIndustryCategory(addRequest.getIndustryCategory());
        if (CollUtil.isNotEmpty(addRequest.getIndustries())) {
            request.setIndustries(JSONUtil.toJsonStr(addRequest.getIndustries()));
        }
        if (CollUtil.isNotEmpty(addRequest.getProofImages())) {
            request.setProofMaterials(JSONUtil.toJsonStr(addRequest.getProofImages()));
        }
        request.setStatus(CompanyRegistrationStatusEnum.PENDING.getValue());
        request.setIsDelete(false);

        boolean save = this.save(request);
        ThrowUtils.throwIf(!save, com.crossorgtalentmanager.exception.ErrorCode.OPERATION_ERROR, "保存企业注册申请失败");

        // 向系统管理员发送站内通知，提醒有新的企业注册申请
        try {
            String title = "新的企业注册申请待处理";
            String content = String.format("企业名称：%s，联系人：%s（电话：%s，邮箱：%s）",
                    request.getCompanyName(),
                    request.getAdminName(),
                    request.getAdminPhone(),
                    request.getAdminEmail());
            // 获取所有管理员用户ID，为每个管理员创建通知
            List<User> adminUsers = userService.list(
                    QueryWrapper.create().eq("user_role", UserConstant.ADMIN_ROLE));
            for (User admin : adminUsers) {
                notificationService.createNotification(
                        admin.getId(),
                        5, // 企业注册申请通知类型
                        title,
                        content,
                        request.getId());
            }
        } catch (Exception e) {
            log.error("创建企业注册申请时发送管理员通知失败", e);
        }

        return request.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveRegistration(CompanyRegistrationApproveRequest approveRequest, Long adminUserId) {
        if (approveRequest == null || approveRequest.getId() == null) {
            throw new BusinessException(com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "申请 ID 不能为空");
        }
        ThrowUtils.throwIf(adminUserId == null, com.crossorgtalentmanager.exception.ErrorCode.NO_AUTH_ERROR,
                "管理员信息不存在");
        ThrowUtils.throwIf(approveRequest.getApproved() == null,
                com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "审批结果不能为空");

        CompanyRegistrationRequest request = this.getById(approveRequest.getId());
        ThrowUtils.throwIf(request == null, com.crossorgtalentmanager.exception.ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        ThrowUtils.throwIf(!CompanyRegistrationStatusEnum.PENDING.getValue().equals(request.getStatus()),
                com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "仅允许处理待审批的申请");

        boolean approved = Boolean.TRUE.equals(approveRequest.getApproved());

        if (!approved) {
            // 拒绝申请
            ThrowUtils.throwIf(approveRequest.getRejectReason() == null
                    || approveRequest.getRejectReason().isBlank(),
                    com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "拒绝原因不能为空");
            request.setStatus(CompanyRegistrationStatusEnum.REJECTED.getValue());
            request.setRejectReason(approveRequest.getRejectReason());
            boolean update = this.updateById(request);
            ThrowUtils.throwIf(!update, com.crossorgtalentmanager.exception.ErrorCode.OPERATION_ERROR, "更新申请状态失败");

            // 发送拒绝邮件
            try {
                qqMailService.sendCompanyRegistrationFailEmail(
                        request.getAdminEmail(),
                        request.getCompanyName(),
                        approveRequest.getRejectReason());
            } catch (Exception e) {
                log.error("发送企业注册失败邮件异常，requestId={}", request.getId(), e);
            }
            return true;
        }

        // 通过申请：创建公司管理员账号与企业信息
        // 使用申请时填写的账号名作为登录用户名
        String username = request.getAdminUsername();
        ThrowUtils.throwIf(username == null || username.isBlank(),
                com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "管理人账号名为空，无法创建管理员账号");

        // 再次检查账号是否已存在（防止审批时账号已被占用）
        Long existCount = userService.count(
                QueryWrapper.create().eq("username", username));
        ThrowUtils.throwIf(existCount != null && existCount > 0,
                com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "该账号名已被占用，请联系管理员处理");

        // 创建公司管理员用户
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword);
        user.setNickname(request.getAdminName());
        user.setUserRole(UserRoleEnum.COMPANY_ADMIN.getValue());

        boolean userSaved = userService.save(user);
        ThrowUtils.throwIf(!userSaved, com.crossorgtalentmanager.exception.ErrorCode.OPERATION_ERROR, "创建企业管理员账号失败");

        // 创建企业信息并关联管理员为联系人
        List<String> industries = new ArrayList<>();
        if (request.getIndustries() != null && !request.getIndustries().isBlank()) {
            try {
                industries = JSONUtil.toList(request.getIndustries(), String.class);
            } catch (Exception e) {
                log.error("解析企业注册申请行业子类失败，requestId={}, industries={}",
                        request.getId(), request.getIndustries(), e);
            }
        }

        long companyId = companyService.addCompany(
                request.getCompanyName(),
                user.getId(),
                request.getAdminPhone(),
                request.getCompanyEmail(),
                request.getIndustryCategory(),
                industries);

        // 更新申请状态
        request.setStatus(CompanyRegistrationStatusEnum.APPROVED.getValue());
        boolean update = this.updateById(request);
        ThrowUtils.throwIf(!update, com.crossorgtalentmanager.exception.ErrorCode.OPERATION_ERROR, "更新申请状态失败");

        // 发送成功邮件，告知账号与默认密码
        try {
            qqMailService.sendCompanyRegistrationSuccessEmail(
                    request.getAdminEmail(),
                    request.getCompanyName(),
                    request.getAdminUsername(),
                    DEFAULT_PASSWORD);
        } catch (Exception e) {
            log.error("发送企业注册成功邮件异常，requestId={}", request.getId(), e);
        }

        log.info("管理员 {} 审批通过企业注册申请，requestId={}, companyId={}, adminUserId={}",
                adminUserId, request.getId(), companyId, user.getId());
        return true;
    }

    @Override
    public QueryWrapper getQueryWrapper(CompanyRegistrationQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(com.crossorgtalentmanager.exception.ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        }
        QueryWrapper qw = QueryWrapper.create()
                .eq("id", queryRequest.getId())
                .eq("status", queryRequest.getStatus())
                .like("company_name", queryRequest.getCompanyName());

        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        String dbSortField = mapToDatabaseColumn(sortField);
        if (dbSortField != null && !dbSortField.isEmpty()) {
            qw.orderBy(dbSortField, "ascend".equals(sortOrder));
        }
        return qw;
    }

    /**
     * Java 字段名到数据库列名的简单映射
     */
    private String mapToDatabaseColumn(String javaFieldName) {
        if (javaFieldName == null) {
            return null;
        }
        return switch (javaFieldName) {
            case "createTime" -> "create_time";
            case "updateTime" -> "update_time";
            default -> javaFieldName;
        };
    }

    @Override
    public Page<CompanyRegistrationRequestVO> pageRegistrationVO(CompanyRegistrationQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        Page<CompanyRegistrationRequest> page = this.page(Page.of(pageNum, pageSize), getQueryWrapper(queryRequest));

        Page<CompanyRegistrationRequestVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<CompanyRegistrationRequestVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    private CompanyRegistrationRequestVO toVO(CompanyRegistrationRequest request) {
        if (request == null) {
            return null;
        }
        CompanyRegistrationRequestVO vo = new CompanyRegistrationRequestVO();
        BeanUtil.copyProperties(request, vo);
        // 行业子类 JSON -> List
        if (request.getIndustries() != null && !request.getIndustries().isEmpty()) {
            try {
                List<String> industries = JSONUtil.toList(request.getIndustries(), String.class);
                vo.setIndustries(industries);
            } catch (Exception e) {
                log.error("解析企业注册申请行业子类失败: {}", request.getIndustries(), e);
                vo.setIndustries(new ArrayList<>());
            }
        } else {
            vo.setIndustries(new ArrayList<>());
        }
        // 证明材料 JSON -> List
        if (request.getProofMaterials() != null && !request.getProofMaterials().isEmpty()) {
            try {
                List<String> proofImages = JSONUtil.toList(request.getProofMaterials(), String.class);
                vo.setProofImages(proofImages);
            } catch (Exception e) {
                log.error("解析企业注册申请证明材料失败: {}", request.getProofMaterials(), e);
                vo.setProofImages(new ArrayList<>());
            }
        } else {
            vo.setProofImages(new ArrayList<>());
        }
        CompanyRegistrationStatusEnum statusEnum =
                CompanyRegistrationStatusEnum.getEnumByValue(request.getStatus());
        if (statusEnum != null) {
            vo.setStatusText(statusEnum.getText());
        }
        return vo;
    }
}


