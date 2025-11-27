package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.mybatisflex.core.paginate.Page;

import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.crossorgtalentmanager.model.entity.RewardPunishment;
import com.crossorgtalentmanager.service.RewardPunishmentService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentAddRequest;
import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentQueryRequest;
import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentUpdateRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.model.vo.RewardPunishmentVO;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 奖惩记录 控制层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@RestController
@RequestMapping("/rewardPunishment")
public class RewardPunishmentController {

    @Resource
    private RewardPunishmentService rewardPunishmentService;

    @Resource
    private UserService userService;

    @Resource
    private EmployeeService employeeService;

    /**
     * 新增奖惩记录（HR/公司管理员）。
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Long> addRewardPunishment(@RequestBody RewardPunishmentAddRequest addRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Long result = rewardPunishmentService.addRewardPunishment(addRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 更新奖惩记录（HR/公司管理员）。
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> updateRewardPunishment(@RequestBody RewardPunishmentUpdateRequest updateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        rewardPunishmentService.updateRewardPunishment(updateRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 根据 employee_id 查询奖惩记录。
     * HR 和公司管理员只能查询本公司员工，系统管理员可以查询所有人。
     */
    @GetMapping("/query")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<List<RewardPunishment>> queryByEmployeeId(Long employeeId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(employeeId == null || employeeId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 验证登录用户并根据用户角色进行公司校验
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            // 非系统管理员仅允许 HR 或 公司管理员 查询本公司数据
            ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "无权限");

            // 查询员工信息并校验其公司 ID 与登录用户的公司 ID 是否一致
            Employee employee = employeeService.getById(employeeId);
            ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");

            Long loginUserCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginUserCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");
            ThrowUtils.throwIf(!loginUserCompanyId.equals(employee.getCompanyId()), ErrorCode.NO_AUTH_ERROR,
                    "员工所属公司与登录用户不一致");
        }

        List<RewardPunishment> records = rewardPunishmentService
                .list(QueryWrapper.create().eq("employee_id", employeeId));
        return ResultUtils.success(records);
    }

    /**
     * 分页查询奖惩记录（返回 VO 列表）。
     * 系统管理员可以查看所有记录，HR 和公司管理员只能查看本公司员工的所有数据。
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<RewardPunishmentVO>> listRewardPunishmentVOByPage(
            @RequestBody RewardPunishmentQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);

        // 验证登录用户并根据用户角色拼接公司过滤条件
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        if (!isAdmin) {
            // 非系统管理员仅允许 HR 或 公司管理员 查询本公司数据
            ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "无权限");

            Long loginCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

            // 如果指定了 employeeId，验证该员工是否属于当前用户的公司
            // 如果属于，则允许查询该员工在所有公司的奖惩记录（不限制 companyId）
            Long employeeId = queryRequest.getEmployeeId();
            if (employeeId != null && employeeId > 0) {
                Employee employee = employeeService.getById(employeeId);
                ThrowUtils.throwIf(employee == null, ErrorCode.NOT_FOUND_ERROR, "员工不存在");
                ThrowUtils.throwIf(!loginCompanyId.equals(employee.getCompanyId()),
                        ErrorCode.NO_AUTH_ERROR, "只能查询本公司员工的奖惩记录");
                // 如果员工属于当前用户的公司，则不限制 companyId，允许查看该员工在所有公司的奖惩记录
                // 如果请求中指定了 companyId，则使用指定的 companyId（用于查看特定公司的记录）
                // 如果没有指定 companyId，则不设置（查询所有公司的记录）
                // 这里不强制设置 companyId，让查询可以返回该员工在所有公司的奖惩记录
            } else {
                // 如果没有指定 employeeId，则只查询当前公司的数据
                queryRequest.setCompanyId(loginCompanyId);
            }
        }
        // 系统管理员可以查看所有记录，如果queryRequest中指定了companyId则使用，否则查看所有

        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();

        // 获取查询条件构建的 QueryWrapper
        QueryWrapper qw = rewardPunishmentService.getQueryWrapper(queryRequest);

        Page<RewardPunishment> page = rewardPunishmentService.page(Page.of(pageNum, pageSize), qw);
        Page<RewardPunishmentVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<RewardPunishmentVO> voList = rewardPunishmentService.getRewardPunishmentVOList(page.getRecords());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

    /**
     * 逻辑删除奖惩记录。
     * 系统管理员可以删除任意记录，HR和公司管理员只能删除本公司奖惩记录。
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> deleteRewardPunishment(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new com.crossorgtalentmanager.exception.BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        boolean isHr = UserRoleEnum.HR.getValue().equals(loginUser.getUserRole());
        boolean isCompanyAdmin = UserRoleEnum.COMPANY_ADMIN.getValue().equals(loginUser.getUserRole());

        // 非系统管理员需要校验权限和公司
        if (!isAdmin) {
            // 非系统管理员仅允许 HR 或 公司管理员 删除本公司数据
            ThrowUtils.throwIf(!(isHr || isCompanyAdmin), ErrorCode.NO_AUTH_ERROR, "无权限");

            // 查询奖惩记录并校验其公司 ID 与登录用户的公司 ID 是否一致
            RewardPunishment record = rewardPunishmentService.getById(deleteRequest.getId());
            ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "奖惩记录不存在");

            Long loginUserCompanyId = loginUser.getCompanyId();
            ThrowUtils.throwIf(loginUserCompanyId == null, ErrorCode.NO_AUTH_ERROR, "操作人员无所属公司");

            // 校验：只能删除本公司产生的奖惩记录（检查奖惩记录的公司ID）
            Long recordCompanyId = record.getCompanyId();
            ThrowUtils.throwIf(recordCompanyId == null || !loginUserCompanyId.equals(recordCompanyId),
                    ErrorCode.NO_AUTH_ERROR, "只能删除本公司产生的奖惩记录");
        }

        boolean b = rewardPunishmentService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }
}
