package com.crossorgtalentmanager.model.dto.talentmarket;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 评价解锁请求
 *
 * @author y
 */
@Data
public class EvaluationUnlockRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 要解锁的评价ID
     */
    private Long evaluationId;

    /**
     * 批量解锁的评价ID列表
     */
    private List<Long> evaluationIds;
}

