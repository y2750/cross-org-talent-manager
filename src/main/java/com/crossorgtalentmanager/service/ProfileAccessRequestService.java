package com.crossorgtalentmanager.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestAddRequest;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestQueryRequest;
import com.crossorgtalentmanager.model.dto.profileaccess.ProfileAccessRequestUpdateRequest;
import com.crossorgtalentmanager.model.entity.ProfileAccessRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.ProfileAccessRequestVO;

import java.util.List;

/**
 * 档案查阅请求 服务层
 */
public interface ProfileAccessRequestService extends IService<ProfileAccessRequest> {

    /**
     * 创建档案查阅请求
     */
    Long createRequest(ProfileAccessRequestAddRequest addRequest, User loginUser);

    /**
     * 审批档案查阅请求（员工操作）
     */
    Boolean approveRequest(ProfileAccessRequestUpdateRequest updateRequest, User loginUser);

    /**
     * 将 ProfileAccessRequest 转换为 VO
     */
    ProfileAccessRequestVO getProfileAccessRequestVO(ProfileAccessRequest request);

    /**
     * 批量转换 ProfileAccessRequest 列表为 VO 列表
     */
    List<ProfileAccessRequestVO> getProfileAccessRequestVOList(List<ProfileAccessRequest> list);

    /**
     * 根据查询请求构建 QueryWrapper
     */
    QueryWrapper getQueryWrapper(ProfileAccessRequestQueryRequest request);

    /**
     * 检查是否可以查看档案（考虑权限和请求状态）
     */
    Boolean canAccessProfile(Long profileId, Long requestCompanyId, Long employeeId);
}

