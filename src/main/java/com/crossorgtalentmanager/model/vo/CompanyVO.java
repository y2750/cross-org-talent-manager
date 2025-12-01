package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompanyVO implements Serializable {

    private Long id;

    private String name;

    private Long contactPersonId;

    private String contactPersonName;

    private String phone;

    private String email;

    /**
     * 行业大类
     */
    private String industryCategory;

    /**
     * 行业子类列表
     */
    private List<String> industries;

    /**
     * 当前总积分
     */
    private java.math.BigDecimal totalPoints;

    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}
