package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 企业积分变动记录VO
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
public class CompanyPointsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 所属企业ID
     */
    private Long companyId;

    /**
     * 积分变动（正/负）
     */
    private BigDecimal points;

    /**
     * 变动原因（1=建立档案，2=员工评价，3=权益消耗，4=评价申诉）
     */
    private Integer changeReason;

    /**
     * 变动原因文本
     */
    private String changeReasonText;

    /**
     * 关联员工ID
     */
    private Long withEmployeeId;

    /**
     * 关联员工姓名
     */
    private String withEmployeeName;

    /**
     * 变动说明（后端自动生成）
     */
    private String changeDescription;

    /**
     * 变动日期
     */
    private LocalDate changeDate;

    private LocalDateTime createTime;

}
