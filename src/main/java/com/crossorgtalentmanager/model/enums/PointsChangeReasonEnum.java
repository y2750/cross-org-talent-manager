package com.crossorgtalentmanager.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 积分变动原因枚举
 */
@Getter
public enum PointsChangeReasonEnum {
    CREATE_PROFILE(1, "建立档案"),
    EMPLOYEE_EVALUATION(2, "员工评价"),
    RIGHTS_CONSUMPTION(3, "权益消耗"),
    EVALUATION_APPEAL(4, "评价申诉");

    private final Integer value;
    private final String text;

    PointsChangeReasonEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static PointsChangeReasonEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PointsChangeReasonEnum anEnum : PointsChangeReasonEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public static boolean isValidReason(Integer value) {
        return getEnumByValue(value) != null;
    }
}
