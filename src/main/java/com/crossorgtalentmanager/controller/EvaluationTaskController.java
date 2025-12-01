package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationTaskCreateRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationTaskQueryRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.EvaluationTaskVO;
import com.crossorgtalentmanager.service.EvaluationTaskService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 评价任务控制器
 */
@RestController
@RequestMapping("/evaluation/task")
public class EvaluationTaskController {

    @Resource
    private EvaluationTaskService evaluationTaskService;

    @Resource
    private UserService userService;

    /**
     * 创建评价任务
     */
    @PostMapping("/create")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Integer> createEvaluationTasks(@RequestBody EvaluationTaskCreateRequest createRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(createRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Integer count = evaluationTaskService.createEvaluationTasks(createRequest, loginUser);
        return ResultUtils.success(count);
    }

    /**
     * 创建季度评价任务（部门领导使用）
     */
    @PostMapping("/create/quarterly")
    public BaseResponse<Integer> createQuarterlyEvaluationTasks(
            @RequestParam Long departmentId,
            @RequestParam Integer periodYear,
            @RequestParam Integer periodQuarter,
            HttpServletRequest request) {
        ThrowUtils.throwIf(departmentId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(periodYear == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(periodQuarter == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Integer count = evaluationTaskService.createQuarterlyEvaluationTasks(
                departmentId, periodYear, periodQuarter, loginUser);
        return ResultUtils.success(count);
    }

    /**
     * 分页查询评价任务列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<EvaluationTaskVO>> pageEvaluationTasks(
            @RequestBody EvaluationTaskQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 系统管理员不能查看评价任务
        ThrowUtils.throwIf("admin".equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "系统管理员不能查看评价任务");
        Page<EvaluationTaskVO> result = evaluationTaskService.pageEvaluationTasks(queryRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取待评价任务数量
     */
    @GetMapping("/pending/count")
    public BaseResponse<Long> getPendingTaskCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 系统管理员不能查看评价任务
        ThrowUtils.throwIf("admin".equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR, "系统管理员不能查看评价任务");
        Long count = evaluationTaskService.getPendingTaskCount(loginUser.getId());
        return ResultUtils.success(count);
    }
}
