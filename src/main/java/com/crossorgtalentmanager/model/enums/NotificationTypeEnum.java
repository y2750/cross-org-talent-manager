package com.crossorgtalentmanager.model.enums;

import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
public enum NotificationTypeEnum {

    /**
     * 评价任务
     */
    EVALUATION_TASK(1, "评价任务"),

    /**
     * 查阅请求（档案/联系方式等）
     */
    ACCESS_REQUEST(2, "查阅请求"),

    /**
     * 系统通知
     */
    SYSTEM(3, "系统通知"),

    /**
     * 投诉处理
     */
    COMPLAINT(4, "投诉处理"),

    /**
     * 企业注册申请
     */
    COMPANY_REGISTRATION(5, "企业注册申请");

    private final Integer value;
    private final String text;

    NotificationTypeEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static NotificationTypeEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (NotificationTypeEnum anEnum : NotificationTypeEnum.values()) {
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

