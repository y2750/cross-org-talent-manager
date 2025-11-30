package com.crossorgtalentmanager.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 评价周期枚举
 */
@Getter
public enum EvaluationPeriodEnum {
    QUARTERLY(1, "季度评价"),
    ANNUAL(2, "年度评价"),
    RESIGNATION(3, "离职评价"),
    TEMPORARY(4, "临时评价");

    private final Integer value;
    private final String text;

    EvaluationPeriodEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static EvaluationPeriodEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (EvaluationPeriodEnum anEnum : EvaluationPeriodEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
