package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.talentmarket.*;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.*;
import com.mybatisflex.core.paginate.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人才市场服务接口
 *
 * @author y
 */
public interface TalentMarketService {

    /**
     * 分页搜索人才
     *
     * @param request   搜索请求
     * @param loginUser 当前登录用户
     * @return 人才列表分页结果
     */
    Page<TalentVO> searchTalents(TalentSearchRequest request, User loginUser);

    /**
     * 计算高级搜索消耗的积分预估
     *
     * @param request 搜索请求
     * @return 预估消耗积分
     */
    BigDecimal calculateAdvancedSearchCostPreview(TalentSearchRequest request);

    /**
     * 获取人才详情
     *
     * @param employeeId 员工ID
     * @param loginUser  当前登录用户
     * @return 人才详情
     */
    TalentDetailVO getTalentDetail(Long employeeId, User loginUser);

    /**
     * 解锁评价（消耗积分）
     *
     * @param request   解锁请求
     * @param loginUser 当前登录用户
     * @return 解锁结果（消耗的积分）
     */
    BigDecimal unlockEvaluation(EvaluationUnlockRequest request, User loginUser);

    /**
     * 批量解锁评价
     *
     * @param request   解锁请求
     * @param loginUser 当前登录用户
     * @return 解锁结果（消耗的总积分）
     */
    BigDecimal batchUnlockEvaluations(EvaluationUnlockRequest request, User loginUser);

    /**
     * 检查评价是否已解锁
     *
     * @param companyId    企业ID
     * @param evaluationId 评价ID
     * @return 是否已解锁
     */
    boolean isEvaluationUnlocked(Long companyId, Long evaluationId);

    /**
     * 获取企业对某员工已解锁的评价ID列表
     *
     * @param companyId  企业ID
     * @param employeeId 员工ID
     * @return 已解锁的评价ID列表
     */
    List<Long> getUnlockedEvaluationIds(Long companyId, Long employeeId);

    /**
     * 收藏人才
     *
     * @param request   收藏请求
     * @param loginUser 当前登录用户
     * @return 收藏ID
     */
    Long bookmarkTalent(TalentBookmarkRequest request, User loginUser);

    /**
     * 取消收藏人才
     *
     * @param employeeId 员工ID
     * @param loginUser  当前登录用户
     * @return 是否成功
     */
    boolean unbookmarkTalent(Long employeeId, User loginUser);

    /**
     * 获取收藏的人才列表
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param loginUser 当前登录用户
     * @return 收藏的人才列表
     */
    Page<TalentVO> getBookmarkedTalents(long pageNum, long pageSize, User loginUser);

    /**
     * 检查是否已收藏
     *
     * @param companyId  企业ID
     * @param employeeId 员工ID
     * @return 是否已收藏
     */
    boolean isBookmarked(Long companyId, Long employeeId);

    /**
     * 获取解锁价格配置
     *
     * @return 价格配置列表
     */
    List<UnlockPriceConfigVO> getUnlockPriceConfigs();

    /**
     * 获取指定评价类型的解锁价格
     *
     * @param evaluationType 评价类型
     * @return 解锁价格
     */
    BigDecimal getUnlockPrice(Integer evaluationType);

    /**
     * 获取所有评价标签（用于筛选）
     *
     * @return 标签列表
     */
    List<com.crossorgtalentmanager.model.vo.EvaluationTagVO> getAllTags();

    /**
     * 检查用户是否有权限访问人才市场
     *
     * @param loginUser 当前登录用户
     * @return 是否有权限
     */
    boolean hasAccessPermission(User loginUser);

    /**
     * 免费可见评价数量常量
     */
    int FREE_EVALUATION_COUNT = 3;

    // ==================== 人才浏览记录相关 ====================

    /**
     * 记录人才浏览
     *
     * @param request   浏览记录请求
     * @param loginUser 当前登录用户
     * @return 记录ID
     */
    Long recordView(TalentViewLogRequest request, User loginUser);

    /**
     * 更新浏览时长
     *
     * @param viewLogId    浏览记录ID
     * @param viewDuration 浏览时长（秒）
     * @param loginUser    当前登录用户
     * @return 是否成功
     */
    boolean updateViewDuration(Long viewLogId, Integer viewDuration, User loginUser);

    /**
     * 获取企业浏览历史（分页）
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param loginUser 当前登录用户
     * @return 浏览历史列表
     */
    Page<TalentViewLogVO> getViewHistory(long pageNum, long pageSize, User loginUser);

    /**
     * 获取浏览统计数据
     *
     * @param loginUser 当前登录用户
     * @return 统计数据
     */
    ViewStatisticsVO getViewStatistics(User loginUser);

    /**
     * 清除浏览记录
     *
     * @param employeeId 员工ID（可选，为空则清除全部）
     * @param loginUser  当前登录用户
     * @return 清除的记录数
     */
    int clearViewHistory(Long employeeId, User loginUser);

    // ==================== 企业招聘偏好相关 ====================

    /**
     * 保存或更新企业招聘偏好
     *
     * @param request   偏好设置请求
     * @param loginUser 当前登录用户
     * @return 偏好ID
     */
    Long savePreference(CompanyPreferenceRequest request, User loginUser);

    /**
     * 获取企业招聘偏好
     *
     * @param loginUser 当前登录用户
     * @return 偏好设置
     */
    CompanyPreferenceVO getPreference(User loginUser);

    // ==================== 人才推荐相关 ====================

    /**
     * 获取推荐人才列表（基于企业偏好和浏览历史）
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param loginUser 当前登录用户
     * @return 推荐人才列表
     */
    Page<TalentRecommendVO> getRecommendedTalents(long pageNum, long pageSize, User loginUser);

    /**
     * 获取相似人才推荐（基于指定员工）
     *
     * @param employeeId 参考员工ID
     * @param limit      推荐数量
     * @param loginUser  当前登录用户
     * @return 相似人才列表
     */
    List<TalentRecommendVO> getSimilarTalents(Long employeeId, int limit, User loginUser);

    // ==================== 人才对比相关 ====================

    /**
     * 对比多个人才
     *
     * @param request   对比请求（包含员工ID列表）
     * @param loginUser 当前登录用户
     * @return 对比结果
     */
    TalentCompareVO compareTalents(TalentCompareRequest request, User loginUser);
}
