package com.crossorgtalentmanager.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * ClassName: EmployeeStatusEnum
 * Package: com.crossorgtalentmanager.model.enums
 * Description:
 *
 * @Author
 * @Create 2025/11/21 12:37
 * @Version 1.0
 */
@Getter
public enum EmployeeStatusEnum {
    NORMAL("正常", true),
    LEAVE("离职", false);

    private final String text;

    private final Boolean value;

    EmployeeStatusEnum(String text, Boolean value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static EmployeeStatusEnum getEnumByValue(Boolean value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (EmployeeStatusEnum anEnum : EmployeeStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
