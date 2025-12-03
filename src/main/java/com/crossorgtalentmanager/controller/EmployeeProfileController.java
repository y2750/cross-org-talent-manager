package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileAddRequest;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileQueryRequest;
import com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileUpdateRequest;
import com.crossorgtalentmanager.model.entity.EmployeeProfile;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.EmployeeProfileVO;
import com.crossorgtalentmanager.service.EmployeeProfileService;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.service.RewardPunishmentService;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.model.entity.RewardPunishment;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.model.vo.RewardPunishmentVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 员工档案信息 控制层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@RestController
@RequestMapping("/employeeProfile")
public class EmployeeProfileController {

    @Resource
    private EmployeeProfileService employeeProfileService;

    @Resource
    private UserService userService;

    @Resource
    private com.crossorgtalentmanager.service.EmployeeService employeeService;

    @Resource
    private RewardPunishmentService rewardPunishmentService;

    @Resource
    private CompanyService companyService;

    /**
     * 添加员工档案（HR）。
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> addEmployeeProfile(@RequestBody EmployeeProfileAddRequest addRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long result = employeeProfileService.addEmployeeProfile(addRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 更新员工档案（HR）。
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> updateEmployeeProfile(@RequestBody EmployeeProfileUpdateRequest updateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        employeeProfileService.updateEmployeeProfile(updateRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 逻辑删除档案（管理员）
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> deleteEmployeeProfile(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new com.crossorgtalentmanager.exception.BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = employeeProfileService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取员工档案
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<EmployeeProfile> getEmployeeProfileById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        EmployeeProfile profile = employeeProfileService.getById(id);
        ThrowUtils.throwIf(profile == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(profile);
    }

    /**
     * 根据 id 获取员工档案包装类（VO）
     * 应用权限控制和脱敏逻辑
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<EmployeeProfileVO> getEmployeeProfileVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        EmployeeProfileVO vo = employeeProfileService.getEmployeeProfileVOByIdWithPermission(id, loginUser);
        return ResultUtils.success(vo);
    }

    /**
     * 分页查询员工档案（返回 VO 列表）
     * 应用权限控制和脱敏逻辑
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<EmployeeProfileVO>> listEmployeeProfileVOByPage(
            @RequestBody EmployeeProfileQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Page<EmployeeProfileVO> voPage = employeeProfileService.listEmployeeProfileVOByPageWithPermission(
                queryRequest, loginUser);
        return ResultUtils.success(voPage);
    }

    /**
     * 员工更新自己档案的公开范围
     */
    @PutMapping("/update/visibility/me")
    public BaseResponse<Boolean> updateMyProfileVisibility(
            @RequestBody com.crossorgtalentmanager.model.dto.employeeprofile.EmployeeProfileVisibilityUpdateRequest updateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "档案 ID 不能为空");
        ThrowUtils.throwIf(updateRequest.getVisibility() == null,
                ErrorCode.PARAMS_ERROR, "公开范围不能为空");

        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 验证公开范围值
        if (updateRequest.getVisibility() < 0 || updateRequest.getVisibility() > 2) {
            throw new com.crossorgtalentmanager.exception.BusinessException(ErrorCode.PARAMS_ERROR, "公开范围值无效");
        }

        // 查询档案
        EmployeeProfile profile = employeeProfileService.getById(updateRequest.getId());
        ThrowUtils.throwIf(profile == null, ErrorCode.NOT_FOUND_ERROR, "档案不存在");

        // 验证员工身份：只能更新自己的档案
        com.crossorgtalentmanager.model.entity.Employee employee = employeeService.list(
                com.mybatisflex.core.query.QueryWrapper.create().eq("user_id", loginUser.getId())).stream()
                .findFirst().orElse(null);
        ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "未找到员工信息");
        ThrowUtils.throwIf(!profile.getEmployeeId().equals(employee.getId()),
                ErrorCode.NO_AUTH_ERROR, "只能更新自己的档案");

        // 只更新公开范围
        EmployeeProfile profileToUpdate = new EmployeeProfile();
        profileToUpdate.setId(updateRequest.getId());
        profileToUpdate.setVisibility(updateRequest.getVisibility());

        boolean update = employeeProfileService.updateById(profileToUpdate);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 获取当前登录员工的档案列表（按公司分类，仅员工角色可访问）
     * 同时拼接该员工的奖惩记录信息，按公司分组
     */
    @PostMapping("/list/page/vo/me")
    public BaseResponse<Page<EmployeeProfileVO>> listMyEmployeeProfileVOByPage(
            @RequestBody EmployeeProfileQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 通过 userId 查找当前员工的 employee 信息
        com.mybatisflex.core.query.QueryWrapper qw = com.mybatisflex.core.query.QueryWrapper.create()
                .eq("user_id", loginUser.getId());
        com.crossorgtalentmanager.model.entity.Employee currentEmployee = employeeService.getOne(qw);
        ThrowUtils.throwIf(currentEmployee == null, ErrorCode.NOT_FOUND_ERROR, "未找到员工信息");

        // 设置查询条件：只查询当前员工的档案
        queryRequest.setEmployeeId(currentEmployee.getId());

        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        Page<EmployeeProfile> page = employeeProfileService.page(Page.of(pageNum, pageSize),
                employeeProfileService.getQueryWrapper(queryRequest));
        Page<EmployeeProfileVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<EmployeeProfileVO> voList = employeeProfileService.getEmployeeProfileVOList(page.getRecords());

        // 查询当前员工的所有奖惩记录（不调用接口，直接查询数据库，避免权限问题）
        QueryWrapper rewardPunishmentQuery = QueryWrapper.create()
                .eq("employee_id", currentEmployee.getId())
                .eq("is_delete", false);
        List<RewardPunishment> allRewardPunishments = rewardPunishmentService.list(rewardPunishmentQuery);

        // 将奖惩记录转换为VO并按公司分组
        List<RewardPunishmentVO> rewardPunishmentVOList = rewardPunishmentService
                .getRewardPunishmentVOList(allRewardPunishments);
        Map<Long, List<RewardPunishmentVO>> rewardPunishmentsByCompany = rewardPunishmentVOList.stream()
                .filter(vo -> vo.getCompanyId() != null)
                .collect(Collectors.groupingBy(RewardPunishmentVO::getCompanyId));

        // 将奖惩记录按公司填充到对应的档案VO中
        // 对于每个公司，只在一个档案VO中填充奖惩记录（避免重复）
        Map<Long, Boolean> companyRewardPunishmentsFilled = new HashMap<>();
        for (EmployeeProfileVO vo : voList) {
            if (vo.getCompanyId() != null && !companyRewardPunishmentsFilled.containsKey(vo.getCompanyId())) {
                List<RewardPunishmentVO> companyRewardPunishments = rewardPunishmentsByCompany.getOrDefault(
                        vo.getCompanyId(), new ArrayList<>());
                vo.setRewardPunishments(companyRewardPunishments);
                companyRewardPunishmentsFilled.put(vo.getCompanyId(), true);
            }
        }

        // 对于有奖惩记录但没有档案的公司，创建虚拟档案VO来存储奖惩记录
        // 这样前端就可以显示这些公司的奖惩记录
        Set<Long> companyIdsToQuery = new java.util.HashSet<>();
        for (Map.Entry<Long, List<RewardPunishmentVO>> entry : rewardPunishmentsByCompany.entrySet()) {
            Long companyId = entry.getKey();
            // 如果这个公司没有档案但有奖惩记录，需要查询公司名称
            if (!companyRewardPunishmentsFilled.containsKey(companyId)) {
                companyIdsToQuery.add(companyId);
            }
        }

        // 批量查询公司名称
        Map<Long, String> companyIdToName = new HashMap<>();
        if (!companyIdsToQuery.isEmpty()) {
            List<Company> companies = companyService.listByIds(new ArrayList<>(companyIdsToQuery));
            companyIdToName = companies.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toMap(Company::getId, Company::getName, (a, b) -> a));
        }

        // 创建虚拟档案VO
        for (Map.Entry<Long, List<RewardPunishmentVO>> entry : rewardPunishmentsByCompany.entrySet()) {
            Long companyId = entry.getKey();
            List<RewardPunishmentVO> companyRewardPunishments = entry.getValue();

            // 如果这个公司没有档案但有奖惩记录，创建一个虚拟档案VO
            if (!companyRewardPunishmentsFilled.containsKey(companyId) && !companyRewardPunishments.isEmpty()) {
                String companyName = companyIdToName.getOrDefault(companyId, "公司" + companyId);

                // 创建虚拟档案VO（id为null，表示没有真实档案）
                EmployeeProfileVO virtualProfile = new EmployeeProfileVO();
                virtualProfile.setId(null); // 虚拟档案，没有真实ID
                virtualProfile.setEmployeeId(currentEmployee.getId());
                virtualProfile.setCompanyId(companyId);
                virtualProfile.setCompanyName(companyName);
                virtualProfile.setRewardPunishments(companyRewardPunishments);

                voList.add(virtualProfile);
            }
        }

        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

}
