package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationAddRequest;
import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationApproveRequest;
import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationQueryRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.CompanyRegistrationRequestVO;
import com.crossorgtalentmanager.service.CompanyRegistrationRequestService;
import com.crossorgtalentmanager.service.UploadRateLimitService;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.manager.CosManager;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业注册申请 控制层
 */
@RestController
@RequestMapping("/company/registration")
public class CompanyRegistrationController {

    @Resource
    private CompanyRegistrationRequestService companyRegistrationRequestService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private UploadRateLimitService uploadRateLimitService;

    /**
     * 上传企业注册证明材料图片（支持多张）
     */
    @PostMapping("/upload/proof")
    public BaseResponse<List<String>> uploadProofImages(
            @RequestPart("files") MultipartFile[] files,
            HttpServletRequest request) {
        if (files == null || files.length == 0) {
            throw new com.crossorgtalentmanager.exception.BusinessException(ErrorCode.PARAMS_ERROR, "请选择要上传的图片");
        }
        ThrowUtils.throwIf(files.length > 10, ErrorCode.PARAMS_ERROR, "最多只能上传10张图片");

        // 限流检查：基于IP地址（因为这是公开接口，用户可能未登录）
        String clientIP = uploadRateLimitService.getClientIP(request);
        uploadRateLimitService.checkUploadLimit(clientIP, files.length);

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new com.crossorgtalentmanager.exception.BusinessException(ErrorCode.PARAMS_ERROR, "只能上传图片文件");
            }
            // 单张 5MB 限制
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new com.crossorgtalentmanager.exception.BusinessException(ErrorCode.PARAMS_ERROR, "单张图片大小不能超过5MB");
            }
            try {
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    originalFilename = "image.jpg";
                }
                String key = String.format("company/registration/proof/%s_%s",
                        Instant.now().toEpochMilli(), originalFilename);
                File tmp = File.createTempFile("company_reg_proof_", originalFilename);
                file.transferTo(tmp);
                String url = cosManager.uploadFile(key, tmp);
                try {
                    Files.deleteIfExists(tmp.toPath());
                } catch (IOException ignored) {
                }
                ThrowUtils.throwIf(url == null, ErrorCode.OPERATION_ERROR, "上传图片失败");
                imageUrls.add(url);
            } catch (IOException e) {
                throw new com.crossorgtalentmanager.exception.BusinessException(ErrorCode.OPERATION_ERROR,
                        "文件处理失败: " + e.getMessage());
            }
        }
        return ResultUtils.success(imageUrls);
    }

    /**
     * 前端公开入口：提交企业注册申请（无需登录）
     */
    @PostMapping("/apply")
    public BaseResponse<Long> apply(@Valid @RequestBody CompanyRegistrationAddRequest addRequest) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        // 校验证明材料不能为空
        ThrowUtils.throwIf(addRequest.getProofImages() == null || addRequest.getProofImages().isEmpty(),
                ErrorCode.PARAMS_ERROR, "证明材料不能为空，请至少上传一张图片");
        Long id = companyRegistrationRequestService.createRegistration(addRequest);
        return ResultUtils.success(id);
    }

    /**
     * 管理员分页查看企业注册申请列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CompanyRegistrationRequestVO>> listPage(
            @RequestBody CompanyRegistrationQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        Page<CompanyRegistrationRequestVO> page =
                companyRegistrationRequestService.pageRegistrationVO(queryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 管理员审批企业注册申请（同意 / 拒绝）
     */
    @PutMapping("/approve")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> approve(@RequestBody CompanyRegistrationApproveRequest approveRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(approveRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "管理员未登录");
        Boolean result = companyRegistrationRequestService.approveRegistration(approveRequest, loginUser.getId());
        return ResultUtils.success(result);
    }
}


