package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.evaluation.EvaluationTaskCreateRequest;
import com.crossorgtalentmanager.model.dto.evaluation.EvaluationTaskQueryRequest;
import com.crossorgtalentmanager.model.entity.EvaluationTask;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.EvaluationTaskVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 评价任务服务接口
 */
public interface EvaluationTaskService extends IService<EvaluationTask> {

    /**
     * 创建评价任务
     *
     * @param createRequest 创建请求
     * @param loginUser     登录用户
     * @return 创建的任务数量
     */
    Integer createEvaluationTasks(EvaluationTaskCreateRequest createRequest, User loginUser);

    /**
     * 创建季度评价任务（部门领导使用）
     *
     * @param departmentId 部门ID
     * @param periodYear    评价年份
     * @param periodQuarter 评价季度
     * @param loginUser     登录用户（部门领导）
     * @return 创建的任务数量
     */
    Integer createQuarterlyEvaluationTasks(Long departmentId, Integer periodYear, Integer periodQuarter, User loginUser);

    /**
     * 分页查询评价任务列表
     *
     * @param queryRequest 查询请求
     * @param loginUser    登录用户
     * @return 分页结果
     */
    Page<EvaluationTaskVO> pageEvaluationTasks(EvaluationTaskQueryRequest queryRequest, User loginUser);

    /**
     * 完成任务（在创建评价时调用）
     *
     * @param evaluationId 评价ID
     * @param evaluatorId 评价人ID
     * @param employeeId  被评价员工ID
     * @param evaluationPeriod 评价周期
     * @param periodYear  评价年份
     * @param periodQuarter 评价季度
     * @return 是否成功
     */
    Boolean completeTask(Long evaluationId, Long evaluatorId, Long employeeId, 
                        Integer evaluationPeriod, Integer periodYear, Integer periodQuarter);

    /**
     * 获取待评价任务数量
     *
     * @param evaluatorId 评价人ID
     * @return 待评价任务数量
     */
    Long getPendingTaskCount(Long evaluatorId);

    /**
     * 转换EvaluationTask为EvaluationTaskVO
     *
     * @param task 任务实体
     * @return VO对象
     */
    EvaluationTaskVO getEvaluationTaskVO(EvaluationTask task);

    /**
     * 批量转换EvaluationTask列表为EvaluationTaskVO列表
     *
     * @param tasks 任务列表
     * @return VO列表
     */
    List<EvaluationTaskVO> getEvaluationTaskVOList(List<EvaluationTask> tasks);

    /**
     * 构建查询条件
     *
     * @param queryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(EvaluationTaskQueryRequest queryRequest);
}


