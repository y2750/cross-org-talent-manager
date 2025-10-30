package model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    private String username;

    /**
     * 用户类型
     */
    private String userRole;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 公司id
     */
    private String companyId;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String checkPassword;
}
