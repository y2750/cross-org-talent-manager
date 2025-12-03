package com.crossorgtalentmanager.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 投诉状态枚举
 */
@Getter
public enum ComplaintStatusEnum {
    PENDING(0, "待处理"),
    APPROVED(2, "通过"),
    REJECTED(3, "已驳回");

    private final Integer value;
    private final String text;

    ComplaintStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static ComplaintStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ComplaintStatusEnum anEnum : ComplaintStatusEnum.values()) {
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
