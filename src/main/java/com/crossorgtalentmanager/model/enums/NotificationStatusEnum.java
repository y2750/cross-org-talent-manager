package com.crossorgtalentmanager.model.enums;

import lombok.Getter;

/**
 * 通知状态枚举
 */
@Getter
public enum NotificationStatusEnum {

    /**
     * 未读
     */
    UNREAD(0, "未读"),

    /**
     * 已读
     */
    READ(1, "已读"),

    /**
     * 已处理
     */
    PROCESSED(2, "已处理");

    private final Integer value;
    private final String text;

    NotificationStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static NotificationStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (NotificationStatusEnum anEnum : NotificationStatusEnum.values()) {
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
