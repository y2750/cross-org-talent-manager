package com.crossorgtalentmanager.model.vo;

import java.io.Serializable;

/**
 * 部门信息包装类（VO）。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public class DepartmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门 ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 所属企业 ID
     */
    private Long companyId;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 部门领导 ID
     */
    private Long leaderId;

    /**
     * 部门领导名称（昵称）
     */
    private String leaderName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

}
