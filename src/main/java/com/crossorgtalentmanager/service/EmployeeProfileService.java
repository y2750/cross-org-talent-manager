package com.crossorgtalentmanager.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileAddRequest;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileQueryRequest;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileUpdateRequest;
import com.crossorgtalentmanager.model.entity.EmployeeProfile;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.EmployeeProfileVO;

import java.util.List;

/**
 * 员工档案信息 服务层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface EmployeeProfileService extends IService<EmployeeProfile> {
    /**
     * 添加员工档案（包括校验公司 ID 一致性与设置操作人员 ID）
     */
    Long addEmployeeProfile(EmployeeProfileAddRequest addRequest, User loginUser);

    /**
     * 更新员工档案（包括校验公司 ID 一致性、字段覆盖与设置操作人员 ID）
     */
    Boolean updateEmployeeProfile(EmployeeProfileUpdateRequest updateRequest, User loginUser);

    /**
     * 将 EmployeeProfile 转换为包装类 VO
     */
    EmployeeProfileVO getEmployeeProfileVO(EmployeeProfile employeeProfile);

    /**
     * 批量转换 EmployeeProfile 列表为 VO 列表
     */
    List<EmployeeProfileVO> getEmployeeProfileVOList(List<EmployeeProfile> list);

    /**
     * 根据查询请求构建 QueryWrapper（用于分页查询）
     */
    QueryWrapper getQueryWrapper(EmployeeProfileQueryRequest request);

    /**
     * 切换逻辑删除状态（如果存在则反转 isDelete 字段）
     */
    Boolean removeById(Long id);

    /**
     * 根据权限获取员工档案VO（带权限控制和脱敏）
     * 
     * @param employeeProfile  档案实体
     * @param viewerCompanyId  查看者公司ID（null表示员工本人）
     * @param viewerEmployeeId 查看者员工ID（null表示非员工）
     * @return 脱敏后的VO
     */
    EmployeeProfileVO getEmployeeProfileVOWithPermission(EmployeeProfile employeeProfile, Long viewerCompanyId,
            Long viewerEmployeeId);

    /**
     * 检查是否可以查看档案详情
     * 
     * @param profileId       档案ID
     * @param viewerCompanyId 查看者公司ID
     * @param employeeId      员工ID
     * @return 是否可以查看
     */
    Boolean canViewProfileDetail(Long profileId, Long viewerCompanyId, Long employeeId);

    /**
     * 分页查询员工档案（带权限控制和脱敏）
     * 
     * @param queryRequest 查询请求
     * @param loginUser    登录用户
     * @return 分页结果
     */
    com.mybatisflex.core.paginate.Page<EmployeeProfileVO> listEmployeeProfileVOByPageWithPermission(
            EmployeeProfileQueryRequest queryRequest, User loginUser);

    /**
     * 根据ID获取员工档案VO（带权限控制和脱敏）
     * 
     * @param id        档案ID
     * @param loginUser 登录用户
     * @return 档案VO
     */
    EmployeeProfileVO getEmployeeProfileVOByIdWithPermission(Long id, User loginUser);

}
