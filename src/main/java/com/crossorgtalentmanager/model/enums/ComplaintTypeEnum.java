package com.crossorgtalentmanager.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 投诉类型枚举
 */
@Getter
public enum ComplaintTypeEnum {
    MALICIOUS_EVALUATION(1, "恶意评价"),
    FALSE_INFORMATION(2, "虚假信息"),
    OTHER(3, "其他");

    private final Integer value;
    private final String text;

    ComplaintTypeEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static ComplaintTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ComplaintTypeEnum anEnum : ComplaintTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public static boolean isValidValue(Integer value) {
        return getEnumByValue(value) != null;
    }
}
