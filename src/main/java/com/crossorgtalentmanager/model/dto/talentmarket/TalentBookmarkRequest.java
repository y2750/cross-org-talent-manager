package com.crossorgtalentmanager.model.dto.talentmarket;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人才收藏请求
 *
 * @author y
 */
@Data
public class TalentBookmarkRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 收藏备注
     */
    private String remark;
}

