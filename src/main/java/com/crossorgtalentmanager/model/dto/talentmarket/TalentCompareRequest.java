package com.crossorgtalentmanager.model.dto.talentmarket;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 人才对比请求
 *
 * @author y
 */
@Data
public class TalentCompareRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 要对比的员工ID列表（2-5个）
     */
    private List<Long> employeeIds;
}
