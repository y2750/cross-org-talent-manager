package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.entity.CompanyPoints;
import com.mybatisflex.core.service.IService;
import com.mybatisflex.core.paginate.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 企业积分服务接口
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface CompanyPointsService extends IService<CompanyPoints> {

    /**
     * 增加企业积分
     *
     * @param companyId         企业ID
     * @param points            积分数量（正数）
     * @param changeReason      变动原因（1=建立档案，2=员工评价，3=权益消耗，4=评价申诉）
     * @param withEmployeeId    关联员工ID（可选）
     * @param changeDescription 变动说明（可选，为空时后端自动生成）
     * @return 积分记录ID
     */
    Long addPoints(Long companyId, BigDecimal points, Integer changeReason, Long withEmployeeId,
            String changeDescription);

    /**
     * 获取企业当前总积分
     *
     * @param companyId 企业ID
     * @return 总积分
     */
    BigDecimal getTotalPoints(Long companyId);

    /**
     * 分页查询企业积分变动记录
     *
     * @param companyId 企业ID
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页结果
     */
    Page<CompanyPoints> getPointsHistory(Long companyId, long pageNum, long pageSize);

    /**
     * 查询企业积分变动记录列表
     *
     * @param companyId 企业ID
     * @return 积分变动记录列表
     */
    List<CompanyPoints> listPointsHistory(Long companyId);
}
