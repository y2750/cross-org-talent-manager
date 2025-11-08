package com.crossorgtalentmanager.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CompanyVO implements Serializable {

    private Long id;

    private String name;

    private Long contactPersonId;

    private String contactPersonName;

    private String phone;

    private String email;

    private String industry;

    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}
