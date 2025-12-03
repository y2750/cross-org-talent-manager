package com.crossorgtalentmanager.model.enums;

import lombok.Getter;

/**
 * 档案查阅请求状态枚举
 */
@Getter
public enum ProfileAccessRequestStatusEnum {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 已授权
     */
    APPROVED(1, "已授权"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),

    /**
     * 已过期
     */
    EXPIRED(3, "已过期");

    private final Integer value;
    private final String text;

    ProfileAccessRequestStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static ProfileAccessRequestStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ProfileAccessRequestStatusEnum anEnum : ProfileAccessRequestStatusEnum.values()) {
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
