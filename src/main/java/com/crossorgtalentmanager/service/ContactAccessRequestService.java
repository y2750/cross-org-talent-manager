package com.crossorgtalentmanager.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestAddRequest;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestQueryRequest;
import com.crossorgtalentmanager.model.dto.contactaccess.ContactAccessRequestUpdateRequest;
import com.crossorgtalentmanager.model.entity.ContactAccessRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.ContactAccessRequestVO;

import java.util.List;

/**
 * 联系方式查看请求 服务层
 */
public interface ContactAccessRequestService extends IService<ContactAccessRequest> {

    /**
     * 创建联系方式查看请求
     */
    Long createRequest(ContactAccessRequestAddRequest addRequest, User loginUser);

    /**
     * 审批联系方式查看请求（员工操作）
     */
    Boolean approveRequest(ContactAccessRequestUpdateRequest updateRequest, User loginUser);

    /**
     * 将 ContactAccessRequest 转换为 VO
     */
    ContactAccessRequestVO getContactAccessRequestVO(ContactAccessRequest request);

    /**
     * 批量转换 ContactAccessRequest 列表为 VO 列表
     */
    List<ContactAccessRequestVO> getContactAccessRequestVOList(List<ContactAccessRequest> list);

    /**
     * 根据查询请求构建 QueryWrapper
     */
    QueryWrapper getQueryWrapper(ContactAccessRequestQueryRequest request);

    /**
     * 检查是否可以查看联系方式（考虑权限和请求状态）
     */
    Boolean canAccessContact(Long employeeId, Integer requestType, Long requestCompanyId);

    /**
     * 检查企业是否已获得员工联系方式的查看授权
     * 
     * @param companyId   请求企业ID
     * @param employeeId  员工ID
     * @param requestType 请求类型（1=电话，2=邮箱，3=身份证号，4=所有联系方式）
     * @return 是否已授权
     */
    Boolean hasAuthorizedAccess(Long companyId, Long employeeId, Integer requestType);
}
