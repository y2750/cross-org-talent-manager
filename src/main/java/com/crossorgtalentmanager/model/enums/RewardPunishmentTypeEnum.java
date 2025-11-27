package com.crossorgtalentmanager.model.enums;

import lombok.Getter;

/**
 * 奖惩类型枚举。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Getter
public enum RewardPunishmentTypeEnum {

    /**
     * 奖励
     */
    REWARD(1, "奖励"),

    /**
     * 惩罚
     */
    PUNISHMENT(2, "惩罚");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型描述
     */
    private final String desc;

    RewardPunishmentTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据类型值获取枚举。
     *
     * @param value 类型值
     * @return 对应的枚举，若不存在则返回 null
     */
    public static RewardPunishmentTypeEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (RewardPunishmentTypeEnum type : RewardPunishmentTypeEnum.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查指定的类型值是否有效。
     *
     * @param value 类型值
     * @return 若有效则返回 true，否则返回 false
     */
    public static boolean isValidType(Integer value) {
        return getByValue(value) != null;
    }
}
