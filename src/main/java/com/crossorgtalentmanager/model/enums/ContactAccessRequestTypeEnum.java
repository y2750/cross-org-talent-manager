package com.crossorgtalentmanager.model.enums;

import lombok.Getter;

/**
 * 联系方式查看请求类型枚举
 */
@Getter
public enum ContactAccessRequestTypeEnum {
    /**
     * 查看电话
     */
    PHONE(1, "查看电话"),

    /**
     * 查看邮箱
     */
    EMAIL(2, "查看邮箱"),

    /**
     * 查看身份证号
     */
    ID_CARD(3, "查看身份证号"),

    /**
     * 查看所有联系方式
     */
    ALL(4, "查看所有联系方式"),

    /**
     * 查看电话和邮箱
     */
    PHONE_AND_EMAIL(5, "查看电话和邮箱");

    private final Integer value;
    private final String text;

    ContactAccessRequestTypeEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static ContactAccessRequestTypeEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ContactAccessRequestTypeEnum anEnum : ContactAccessRequestTypeEnum.values()) {
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
