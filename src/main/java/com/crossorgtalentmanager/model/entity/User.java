package com.crossorgtalentmanager.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统用户管理 实体类。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 所属企业
     */
    private Long companyId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean isDelete;

}
