package com.crossorgtalentmanager.model.dto.rewardpunishment;

import com.crossorgtalentmanager.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 奖惩记录查询请求（分页）。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RewardPunishmentQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 员工 ID
     */
    private Long employeeId;

    /**
     * 员工姓名（支持模糊查询）
     */
    private String employeeName;

    /**
     * 身份证号（精确匹配）
     */
    private String idCardNumber;

    /**
     * 部门 ID
     */
    private Long departmentId;

    /**
     * 员工性别
     */
    private String gender;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（ascend/descend）
     */
    private String sortOrder;
}
