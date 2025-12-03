package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.complaint.ComplaintAddRequest;
import com.crossorgtalentmanager.model.dto.complaint.ComplaintHandleRequest;
import com.crossorgtalentmanager.model.dto.complaint.ComplaintQueryRequest;
import com.crossorgtalentmanager.model.entity.Complaint;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.ComplaintDetailVO;
import com.crossorgtalentmanager.model.vo.ComplaintVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 投诉服务接口
 */
public interface ComplaintService extends IService<Complaint> {

    /**
     * 提交投诉
     *
     * @param addRequest 投诉请求
     * @param loginUser  登录用户（投诉人）
     * @return 投诉ID
     */
    Long addComplaint(ComplaintAddRequest addRequest, User loginUser);

    /**
     * 分页查询投诉列表
     *
     * @param queryRequest 查询请求
     * @param loginUser    登录用户
     * @return 分页结果
     */
    Page<ComplaintVO> pageComplaint(ComplaintQueryRequest queryRequest, User loginUser);

    /**
     * 根据ID获取投诉详情
     *
     * @param id        投诉ID
     * @param loginUser 登录用户
     * @return 投诉详情
     */
    ComplaintDetailVO getComplaintDetail(Long id, User loginUser);

    /**
     * 处理投诉（管理员）
     *
     * @param handleRequest 处理请求
     * @param loginUser     登录用户（管理员）
     * @return 是否成功
     */
    Boolean handleComplaint(ComplaintHandleRequest handleRequest, User loginUser);
}
