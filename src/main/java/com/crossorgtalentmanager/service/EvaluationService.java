package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.evaluation.EvaluationAddRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationColleagueRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationQueryRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationUpdateRequest;
import com.crossorgtalentmanager.model.entity.Evaluation;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.EvaluationDetailVO;
import com.crossorgtalentmanager.model.vo.EvaluationDimensionScoreVO;
import com.crossorgtalentmanager.model.vo.EvaluationStatisticsVO;
import com.crossorgtalentmanager.model.vo.EvaluationTagStatisticsVO;
import com.crossorgtalentmanager.model.vo.EvaluationVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 评价服务接口
 */
public interface EvaluationService extends IService<Evaluation> {

    /**
     * 新增评价
     *
     * @param addRequest 新增请求
     * @param loginUser  登录用户
     * @return 评价ID
     */
    Long addEvaluation(EvaluationAddRequest addRequest, User loginUser);

    /**
     * 更新评价
     *
     * @param updateRequest 更新请求
     * @param loginUser     登录用户
     * @return 是否成功
     */
    Boolean updateEvaluation(EvaluationUpdateRequest updateRequest, User loginUser);

    /**
     * 删除评价（逻辑删除）
     *
     * @param id        评价ID
     * @param loginUser 登录用户
     * @return 是否成功
     */
    Boolean deleteEvaluation(Long id, User loginUser);

    /**
     * 根据ID获取评价详情
     *
     * @param id        评价ID
     * @param loginUser 登录用户
     * @return 评价详情
     */
    EvaluationDetailVO getEvaluationDetail(Long id, User loginUser);

    /**
     * 分页查询评价列表
     *
     * @param queryRequest 查询请求
     * @param loginUser    登录用户
     * @return 分页结果
     */
    Page<EvaluationVO> pageEvaluation(EvaluationQueryRequest queryRequest, User loginUser);

    /**
     * 同事评价（离职时触发）
     *
     * @param colleagueRequest 同事评价请求
     * @param loginUser        登录用户（评价人）
     * @return 评价ID
     */
    Long addColleagueEvaluation(EvaluationColleagueRequest colleagueRequest, User loginUser);

    /**
     * 创建离职评价任务（触发同事评价）
     *
     * @param employeeId 离职员工ID
     * @param loginUser  登录用户（HR）
     * @return 是否成功
     */
    Boolean createResignationEvaluationTasks(Long employeeId, User loginUser);

    /**
     * 获取待评价的同事列表（离职评价）
     *
     * @param employeeId 离职员工ID
     * @param loginUser  登录用户
     * @return 待评价同事列表
     */
    List<Long> getColleaguesToEvaluate(Long employeeId, User loginUser);

    /**
     * 获取评价统计信息
     *
     * @param employeeId 员工ID
     * @param loginUser  登录用户
     * @return 统计信息
     */
    EvaluationStatisticsVO getEvaluationStatistics(Long employeeId, User loginUser);

    /**
     * 转换Evaluation为EvaluationVO
     *
     * @param evaluation 评价实体
     * @return VO对象
     */
    EvaluationVO getEvaluationVO(Evaluation evaluation);

    /**
     * 批量转换Evaluation列表为EvaluationVO列表
     *
     * @param evaluations 评价列表
     * @return VO列表
     */
    List<EvaluationVO> getEvaluationVOList(List<Evaluation> evaluations);

    /**
     * 构建查询条件
     *
     * @param queryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(EvaluationQueryRequest queryRequest);

    /**
     * 验证评价权限
     *
     * @param employeeId     被评价员工ID
     * @param evaluatorId    评价人ID
     * @param evaluationType 评价类型
     * @return 是否有权限
     */
    Boolean validateEvaluationPermission(Long employeeId, Long evaluatorId, Integer evaluationType);

    /**
     * 根据查询条件计算五维评价数据（加权平均，考虑评价类型权重）
     *
     * @param queryRequest 查询请求（包含筛选条件）
     * @param loginUser    登录用户
     * @return 五维评价数据列表
     */
    List<EvaluationDimensionScoreVO> calculateDimensionScores(EvaluationQueryRequest queryRequest, User loginUser);

    /**
     * 根据查询条件统计标签出现次数
     *
     * @param queryRequest 查询请求（包含筛选条件）
     * @param loginUser    登录用户
     * @return 标签统计列表
     */
    List<EvaluationTagStatisticsVO> countTagStatistics(EvaluationQueryRequest queryRequest, User loginUser);
}
