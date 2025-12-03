package com.crossorgtalentmanager.model.dto.employeeprofile;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工更新自己档案公开范围的请求
 */
@Data
public class EmployeeProfileVisibilityUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 档案ID
     */
    private Long id;

    /**
     * 公开范围（0=完全保密，1=对认证企业可见，2=公开）
     */
    private Integer visibility;
}
