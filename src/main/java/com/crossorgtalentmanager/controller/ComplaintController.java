package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.manager.CosManager;
import com.crossorgtalentmanager.model.dto.complaint.ComplaintAddRequest;
import com.crossorgtalentmanager.model.dto.complaint.ComplaintHandleRequest;
import com.crossorgtalentmanager.model.dto.complaint.ComplaintQueryRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.ComplaintDetailVO;
import com.crossorgtalentmanager.model.vo.ComplaintVO;
import com.crossorgtalentmanager.service.ComplaintService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 投诉控制器
 */
@RestController
@RequestMapping("/complaint")
public class ComplaintController {

    @Resource
    private ComplaintService complaintService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 上传投诉证据图片（支持多张）
     */
    @PostMapping("/upload/evidence")
    public BaseResponse<List<String>> uploadEvidenceImages(@RequestPart("files") MultipartFile[] files,
            HttpServletRequest request) {
        ThrowUtils.throwIf(files == null || files.length == 0, ErrorCode.PARAMS_ERROR, "请选择要上传的图片");
        ThrowUtils.throwIf(files.length > 5, ErrorCode.PARAMS_ERROR, "最多只能上传5张图片");

        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能上传图片文件");
            }

            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过5MB");
            }

            try {
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    originalFilename = "image.jpg";
                }
                String key = String.format("complaint/evidence/%s/%s_%s",
                        loginUser.getId(), Instant.now().toEpochMilli(), originalFilename);
                File tmp = File.createTempFile("upload_", originalFilename);
                file.transferTo(tmp);
                String url = cosManager.uploadFile(key, tmp);
                try {
                    Files.deleteIfExists(tmp.toPath());
                } catch (IOException ignored) {
                }
                ThrowUtils.throwIf(url == null, ErrorCode.OPERATION_ERROR, "上传图片失败");
                imageUrls.add(url);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件处理失败: " + e.getMessage());
            }
        }

        return ResultUtils.success(imageUrls);
    }

    /**
     * 提交投诉
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComplaint(@RequestBody ComplaintAddRequest addRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long result = complaintService.addComplaint(addRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询投诉列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ComplaintVO>> pageComplaint(@RequestBody ComplaintQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Page<ComplaintVO> result = complaintService.pageComplaint(queryRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取投诉详情
     */
    @GetMapping("/detail")
    public BaseResponse<ComplaintDetailVO> getComplaintDetail(@RequestParam Long id,
            HttpServletRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ComplaintDetailVO result = complaintService.getComplaintDetail(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 处理投诉（管理员）
     */
    @PostMapping("/handle")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> handleComplaint(@RequestBody ComplaintHandleRequest handleRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(handleRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Boolean result = complaintService.handleComplaint(handleRequest, loginUser);
        return ResultUtils.success(result);
    }
}
