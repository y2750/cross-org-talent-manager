package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.talentmarket.*;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.*;
import com.crossorgtalentmanager.ratelimiter.annotation.RateLimit;
import com.crossorgtalentmanager.ratelimiter.enums.RateLimitType;
import com.crossorgtalentmanager.service.TalentMarketService;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人才市场 控制层
 *
 * @author y
 */
@RestController
@RequestMapping("/talent-market")
public class TalentMarketController {

    @Resource
    private TalentMarketService talentMarketService;

    @Resource
    private UserService userService;

    /**
     * 搜索人才（分页）
     * 支持多条件筛选：职位、评分、标签、评价内容关键词等
     *
     * @param request     搜索请求
     * @param httpRequest HTTP请求
     * @return 人才列表分页结果
     */
    @PostMapping("/search")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<TalentVO>> searchTalents(@RequestBody TalentSearchRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Page<TalentVO> page = talentMarketService.searchTalents(request, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 预估高级搜索消耗的积分
     * 用于在搜索前提示用户将消耗多少积分
     *
     * @param request 搜索请求
     * @return 预估消耗的积分
     */
    @PostMapping("/search/cost-preview")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<BigDecimal> getAdvancedSearchCostPreview(@RequestBody TalentSearchRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        BigDecimal cost = talentMarketService.calculateAdvancedSearchCostPreview(request);
        return ResultUtils.success(cost);
    }

    /**
     * 获取人才详情
     * 包含工作经历、评价（带解锁状态）、标签统计等
     *
     * @param employeeId  员工ID
     * @param httpRequest HTTP请求
     * @return 人才详情
     */
    @GetMapping("/detail/{employeeId}")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<TalentDetailVO> getTalentDetail(@PathVariable Long employeeId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(employeeId == null || employeeId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        TalentDetailVO detail = talentMarketService.getTalentDetail(employeeId, loginUser);
        return ResultUtils.success(detail);
    }

    /**
     * 解锁评价（消耗积分）
     *
     * @param request     解锁请求
     * @param httpRequest HTTP请求
     * @return 消耗的积分
     */
    @PostMapping("/unlock")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<BigDecimal> unlockEvaluation(@RequestBody EvaluationUnlockRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        BigDecimal cost = talentMarketService.unlockEvaluation(request, loginUser);
        return ResultUtils.success(cost);
    }

    /**
     * 批量解锁评价（消耗积分）
     *
     * @param request     解锁请求（包含评价ID列表）
     * @param httpRequest HTTP请求
     * @return 消耗的总积分
     */
    @PostMapping("/unlock/batch")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<BigDecimal> batchUnlockEvaluations(@RequestBody EvaluationUnlockRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        BigDecimal cost = talentMarketService.batchUnlockEvaluations(request, loginUser);
        return ResultUtils.success(cost);
    }

    /**
     * 收藏人才
     *
     * @param request     收藏请求
     * @param httpRequest HTTP请求
     * @return 收藏ID
     */
    @PostMapping("/bookmark")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> bookmarkTalent(@RequestBody TalentBookmarkRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Long bookmarkId = talentMarketService.bookmarkTalent(request, loginUser);
        return ResultUtils.success(bookmarkId);
    }

    /**
     * 取消收藏人才
     *
     * @param employeeId  员工ID
     * @param httpRequest HTTP请求
     * @return 是否成功
     */
    @DeleteMapping("/bookmark/{employeeId}")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> unbookmarkTalent(@PathVariable Long employeeId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(employeeId == null || employeeId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = talentMarketService.unbookmarkTalent(employeeId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取收藏的人才列表（分页）
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param httpRequest HTTP请求
     * @return 收藏的人才列表
     */
    @GetMapping("/bookmarks")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<TalentVO>> getBookmarkedTalents(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<TalentVO> page = talentMarketService.getBookmarkedTalents(pageNum, pageSize, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 获取解锁价格配置
     *
     * @return 价格配置列表
     */
    @GetMapping("/unlock-prices")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<List<UnlockPriceConfigVO>> getUnlockPriceConfigs() {
        List<UnlockPriceConfigVO> configs = talentMarketService.getUnlockPriceConfigs();
        return ResultUtils.success(configs);
    }

    /**
     * 获取所有评价标签（用于筛选）
     *
     * @return 标签列表
     */
    @GetMapping("/tags")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<List<EvaluationTagVO>> getAllTags() {
        List<EvaluationTagVO> tags = talentMarketService.getAllTags();
        return ResultUtils.success(tags);
    }

    /**
     * 检查当前用户是否有权限访问人才市场
     *
     * @param httpRequest HTTP请求
     * @return 是否有权限
     */
    @GetMapping("/check-permission")
    public BaseResponse<Boolean> checkPermission(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        boolean hasPermission = talentMarketService.hasAccessPermission(loginUser);
        return ResultUtils.success(hasPermission);
    }

    // ==================== 人才浏览记录相关 ====================

    /**
     * 记录人才浏览
     *
     * @param request     浏览记录请求
     * @param httpRequest HTTP请求
     * @return 浏览记录ID
     */
    @PostMapping("/view/record")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> recordView(@RequestBody TalentViewLogRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Long viewLogId = talentMarketService.recordView(request, loginUser);
        return ResultUtils.success(viewLogId);
    }

    /**
     * 更新浏览时长
     *
     * @param viewLogId    浏览记录ID
     * @param viewDuration 浏览时长（秒）
     * @param httpRequest  HTTP请求
     * @return 是否成功
     */
    @PostMapping("/view/duration")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> updateViewDuration(@RequestParam Long viewLogId,
            @RequestParam Integer viewDuration,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = talentMarketService.updateViewDuration(viewLogId, viewDuration, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取浏览历史（分页）
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param httpRequest HTTP请求
     * @return 浏览历史列表
     */
    @GetMapping("/view/history")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<TalentViewLogVO>> getViewHistory(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<TalentViewLogVO> page = talentMarketService.getViewHistory(pageNum, pageSize, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 获取浏览统计数据
     *
     * @param httpRequest HTTP请求
     * @return 统计数据
     */
    @GetMapping("/view/statistics")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<ViewStatisticsVO> getViewStatistics(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ViewStatisticsVO stats = talentMarketService.getViewStatistics(loginUser);
        return ResultUtils.success(stats);
    }

    /**
     * 清除浏览记录
     *
     * @param employeeId  员工ID（可选，为空则清除全部）
     * @param httpRequest HTTP请求
     * @return 清除的记录数
     */
    @DeleteMapping("/view/history")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Integer> clearViewHistory(
            @RequestParam(required = false) Long employeeId,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        int count = talentMarketService.clearViewHistory(employeeId, loginUser);
        return ResultUtils.success(count);
    }

    // ==================== 企业招聘偏好相关 ====================

    /**
     * 保存或更新企业招聘偏好
     *
     * @param request     偏好设置请求
     * @param httpRequest HTTP请求
     * @return 偏好ID
     */
    @PostMapping("/preference")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> savePreference(@RequestBody CompanyPreferenceRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Long preferenceId = talentMarketService.savePreference(request, loginUser);
        return ResultUtils.success(preferenceId);
    }

    /**
     * 获取企业招聘偏好
     *
     * @param httpRequest HTTP请求
     * @return 偏好设置
     */
    @GetMapping("/preference")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<CompanyPreferenceVO> getPreference(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        CompanyPreferenceVO preference = talentMarketService.getPreference(loginUser);
        return ResultUtils.success(preference);
    }

    // ==================== 人才推荐相关 ====================

    /**
     * 获取推荐人才列表（基于企业偏好和浏览历史）
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param httpRequest HTTP请求
     * @return 推荐人才列表
     */
    @GetMapping("/recommend")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<TalentRecommendVO>> getRecommendedTalents(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<TalentRecommendVO> page = talentMarketService.getRecommendedTalents(pageNum, pageSize, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 获取相似人才推荐（基于指定员工）
     *
     * @param employeeId  参考员工ID
     * @param limit       推荐数量
     * @param httpRequest HTTP请求
     * @return 相似人才列表
     */
    @GetMapping("/recommend/similar/{employeeId}")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<List<TalentRecommendVO>> getSimilarTalents(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(employeeId == null || employeeId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        List<TalentRecommendVO> talents = talentMarketService.getSimilarTalents(employeeId, limit, loginUser);
        return ResultUtils.success(talents);
    }

    // ==================== 人才对比相关 ====================

    /**
     * 对比多个人才
     *
     * @param request     对比请求（包含员工ID列表，2-5个）
     * @param httpRequest HTTP请求
     * @return 对比结果
     */
    @PostMapping("/compare")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    @RateLimit(limitType = RateLimitType.USER, rate = 2, rateInterval = 60, message = "提交分析任务过于频繁，请稍后再试")
    public BaseResponse<TalentCompareVO> compareTalents(@RequestBody TalentCompareRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        TalentCompareVO compareResult = talentMarketService.compareTalents(request, loginUser);
        return ResultUtils.success(compareResult);
    }

    /**
     * 查询AI分析结果
     *
     * @param taskId      任务ID
     * @param httpRequest HTTP请求
     * @return AI分析结果
     */
    @GetMapping("/compare/ai-result")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    @RateLimit(limitType = RateLimitType.USER, rate = 120, rateInterval = 60, message = "请求过于频繁，请稍后再试")
    public BaseResponse<Map<String, Object>> getAiAnalysisResult(@RequestParam String taskId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        User loginUser = userService.getLoginUser(httpRequest);

        Map<String, Object> result = new HashMap<>();
        String status = talentMarketService.getAiAnalysisTaskStatus(taskId);
        String aiResult = talentMarketService.getAiAnalysisTaskResult(taskId);
        String error = talentMarketService.getAiAnalysisTaskError(taskId);

        result.put("status", status);
        result.put("result", aiResult);
        result.put("error", error);

        // 如果AI分析完成，更新对比记录中的AI分析结果
        if ("completed".equals(status) && aiResult != null && !aiResult.trim().isEmpty()) {
            talentMarketService.updateCompareRecordAiResult(taskId, aiResult, loginUser);
        }

        return ResultUtils.success(result);
    }

    /**
     * 查询历史对比记录
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param httpRequest HTTP请求
     * @return 历史对比记录列表
     */
    @GetMapping("/compare/history")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<TalentCompareRecordVO>> getCompareHistory(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<TalentCompareRecordVO> page = talentMarketService.getCompareHistory(pageNum, pageSize, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 根据记录ID获取历史对比记录详情
     *
     * @param recordId    记录ID
     * @param httpRequest HTTP请求
     * @return 历史对比记录详情
     */
    @GetMapping("/compare/history/{recordId}")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<TalentCompareRecordVO> getCompareHistoryById(
            @PathVariable Long recordId,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        TalentCompareRecordVO record = talentMarketService.getCompareHistoryById(recordId, loginUser);
        return ResultUtils.success(record);
    }

    /**
     * 检查是否存在相同的对比记录
     *
     * @param request     对比请求（包含员工ID列表）
     * @param httpRequest HTTP请求
     * @return 如果存在返回记录信息，否则返回null
     */
    @PostMapping("/compare/check-existing")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<TalentCompareRecordVO> checkExistingCompare(@RequestBody TalentCompareRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        TalentCompareRecordVO record = talentMarketService.checkExistingCompare(request, loginUser);
        return ResultUtils.success(record);
    }
}
