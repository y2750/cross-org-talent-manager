package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationAddRequest;
import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationApproveRequest;
import com.crossorgtalentmanager.model.dto.company.CompanyRegistrationQueryRequest;
import com.crossorgtalentmanager.model.entity.CompanyRegistrationRequest;
import com.crossorgtalentmanager.model.vo.CompanyRegistrationRequestVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

/**
 * 企业注册申请 服务层
 */
public interface CompanyRegistrationRequestService extends IService<CompanyRegistrationRequest> {

    /**
     * 创建企业注册申请（前端公开入口）
     */
    Long createRegistration(CompanyRegistrationAddRequest addRequest);

    /**
     * 管理员审批企业注册申请
     */
    Boolean approveRegistration(CompanyRegistrationApproveRequest approveRequest, Long adminUserId);

    /**
     * 构建查询条件
     */
    QueryWrapper getQueryWrapper(CompanyRegistrationQueryRequest queryRequest);

    /**
     * 分页查询申请列表（管理员查看）
     */
    Page<CompanyRegistrationRequestVO> pageRegistrationVO(CompanyRegistrationQueryRequest queryRequest);
}












