package com.crossorgtalentmanager.model.enums;

import lombok.Getter;

/**
 * 档案公开范围枚举
 */
@Getter
public enum ProfileVisibilityEnum {
    /**
     * 完全保密
     */
    PRIVATE(0, "完全保密"),

    /**
     * 对认证企业可见
     */
    COMPANY_VISIBLE(1, "对认证企业可见"),

    /**
     * 公开
     */
    PUBLIC(2, "公开");

    private final Integer value;
    private final String text;

    ProfileVisibilityEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static ProfileVisibilityEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ProfileVisibilityEnum anEnum : ProfileVisibilityEnum.values()) {
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
