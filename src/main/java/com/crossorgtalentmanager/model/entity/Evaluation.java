package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工评价记录 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("evaluation")
public class Evaluation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 被评价员工ID
     */
    private Long employeeId;

    /**
     * 评价时员工所属公司ID
     */
    private Long companyId;

    /**
     * 评价人ID
     */
    private Long evaluatorId;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评价日期
     */
    private LocalDate evaluationDate;

    /**
     * 评价类型（1=领导评价，2=同事评价，3=HR评价，4=自评）
     */
    private Integer evaluationType;

    /**
     * 评价周期（1=季度评价，2=年度评价，3=离职评价，4=临时评价）
     */
    private Integer evaluationPeriod;

    /**
     * 评价年份（如2024）
     */
    private Integer periodYear;

    /**
     * 评价季度（1-4，仅季度评价时有效）
     */
    private Integer periodQuarter;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Boolean isDelete;
}
