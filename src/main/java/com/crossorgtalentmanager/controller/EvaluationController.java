package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationAddRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationColleagueRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationQueryRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationUpdateRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.EvaluationDetailVO;
import com.crossorgtalentmanager.model.vo.EvaluationDimensionScoreVO;
import com.crossorgtalentmanager.model.vo.EvaluationStatisticsVO;
import com.crossorgtalentmanager.model.vo.EvaluationTagStatisticsVO;
import com.crossorgtalentmanager.model.vo.EvaluationVO;
import com.crossorgtalentmanager.service.EvaluationService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    @Resource
    private EvaluationService evaluationService;

    @Resource
    private UserService userService;

    /**
     * 新增评价
     */
    @PostMapping("/add")
    public BaseResponse<Long> addEvaluation(@RequestBody EvaluationAddRequest addRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long result = evaluationService.addEvaluation(addRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 更新评价
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> updateEvaluation(@RequestBody EvaluationUpdateRequest updateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Boolean result = evaluationService.updateEvaluation(updateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除评价
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> deleteEvaluation(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Boolean result = evaluationService.deleteEvaluation(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/detail")
    public BaseResponse<EvaluationDetailVO> getEvaluationDetail(@RequestParam Long id,
            HttpServletRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        EvaluationDetailVO result = evaluationService.getEvaluationDetail(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询评价列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<EvaluationVO>> pageEvaluation(@RequestBody EvaluationQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Page<EvaluationVO> result = evaluationService.pageEvaluation(queryRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 同事评价（离职时触发）
     */
    @PostMapping("/colleague/add")
    public BaseResponse<Long> addColleagueEvaluation(@RequestBody EvaluationColleagueRequest colleagueRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(colleagueRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long result = evaluationService.addColleagueEvaluation(colleagueRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 创建离职评价任务（触发同事评价）
     */
    @PostMapping("/resignation/create")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> createResignationEvaluationTasks(@RequestParam Long employeeId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(employeeId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Boolean result = evaluationService.createResignationEvaluationTasks(employeeId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取待评价的同事列表（离职评价）
     */
    @GetMapping("/colleague/pending")
    public BaseResponse<List<Long>> getColleaguesToEvaluate(@RequestParam Long employeeId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(employeeId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        List<Long> result = evaluationService.getColleaguesToEvaluate(employeeId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取评价统计信息
     */
    @GetMapping("/statistics")
    public BaseResponse<EvaluationStatisticsVO> getEvaluationStatistics(@RequestParam Long employeeId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(employeeId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        EvaluationStatisticsVO result = evaluationService.getEvaluationStatistics(employeeId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据查询条件计算五维评价数据（加权平均，考虑评价类型权重）
     */
    @PostMapping("/dimension-scores")
    public BaseResponse<List<EvaluationDimensionScoreVO>> calculateDimensionScores(
            @RequestBody EvaluationQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        List<EvaluationDimensionScoreVO> result = evaluationService.calculateDimensionScores(queryRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据查询条件统计标签出现次数
     */
    @PostMapping("/tag-statistics")
    public BaseResponse<List<EvaluationTagStatisticsVO>> countTagStatistics(
            @RequestBody EvaluationQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        List<EvaluationTagStatisticsVO> result = evaluationService.countTagStatistics(queryRequest, loginUser);
        return ResultUtils.success(result);
    }
}
