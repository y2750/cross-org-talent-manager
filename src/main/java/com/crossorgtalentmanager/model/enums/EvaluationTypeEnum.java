package com.crossorgtalentmanager.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 评价类型枚举
 */
@Getter
public enum EvaluationTypeEnum {
    LEADER(1, "领导评价"),
    COLLEAGUE(2, "同事评价"),
    HR(3, "HR评价"),
    SELF(4, "自评");

    private final Integer value;
    private final String text;

    EvaluationTypeEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static EvaluationTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (EvaluationTypeEnum anEnum : EvaluationTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
