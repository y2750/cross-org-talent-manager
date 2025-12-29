package com.crossorgtalentmanager.model.enums;

import lombok.Getter;

/**
 * 企业注册申请状态枚举
 */
@Getter
public enum CompanyRegistrationStatusEnum {

    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 已通过
     */
    APPROVED(1, "已通过"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝");

    private final Integer value;
    private final String text;

    CompanyRegistrationStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static CompanyRegistrationStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CompanyRegistrationStatusEnum anEnum : CompanyRegistrationStatusEnum.values()) {
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









