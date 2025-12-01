package com.crossorgtalentmanager.controller;

import cn.hutool.json.JSONUtil;
import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.model.dto.company.CompanyQueryRequest;
import com.crossorgtalentmanager.model.dto.company.CompanyUpdateRequest;
import com.crossorgtalentmanager.model.vo.CompanyVO;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.company.CompanyAddRequest;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.model.entity.CompanyPoints;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.CompanyPointsVO;
import com.crossorgtalentmanager.service.CompanyPointsService;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.crossorgtalentmanager.service.UserService;
import com.crossorgtalentmanager.model.enums.PointsChangeReasonEnum;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 企业信息管理 控制层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@RestController
@RequestMapping("/company")
public class CompanyController {

    @Resource
    private CompanyService companyService;

    @Resource
    private CompanyPointsService companyPointsService;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.COMPANY_ADMIN_ROLE)
    public BaseResponse<Long> addCompany(@RequestBody CompanyAddRequest companyAddRequest) {
        ThrowUtils.throwIf(companyAddRequest == null, ErrorCode.PARAMS_ERROR);
        String name = companyAddRequest.getName();
        Long contactPersonId = companyAddRequest.getContactPersonId();
        String phone = companyAddRequest.getPhone();
        String email = companyAddRequest.getEmail();
        String industryCategory = companyAddRequest.getIndustryCategory();
        java.util.List<String> industries = companyAddRequest.getIndustries();
        long result = companyService.addCompany(name, contactPersonId, phone, email, industryCategory, industries);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取企业（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Company> getCompanyById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Company company = companyService.getById(id);
        ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(company);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<CompanyVO> getCompanyVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Company company = companyService.getById(id);
        ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(companyService.getCompanyVO(company));
    }

    /**
     * 启用或禁用企业
     */
    @PostMapping("/toggle")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> toggleCompanyStatus(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = companyService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 分页获取企业封装列表
     *
     * @param companyQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CompanyVO>> listCompanyVOByPage(@RequestBody CompanyQueryRequest companyQueryRequest) {
        ThrowUtils.throwIf(companyQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = companyQueryRequest.getPageNum();
        long pageSize = companyQueryRequest.getPageSize();
        Page<Company> companyPage = companyService.page(Page.of(pageNum, pageSize),
                companyService.getQueryWrapper(companyQueryRequest));
        // 数据脱敏/包装
        Page<CompanyVO> companyVOPage = new Page<>(pageNum, pageSize, companyPage.getTotalRow());
        java.util.List<CompanyVO> companyVOList = companyService.getCompanyVOList(companyPage.getRecords());
        companyVOPage.setRecords(companyVOList);
        return ResultUtils.success(companyVOPage);
    }

    /**
     * 根据主键更新企业信息管理。
     *
     * @param companyUpdateRequest 企业更新请求
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Boolean> update(@RequestBody CompanyUpdateRequest companyUpdateRequest) {
        ThrowUtils.throwIf(companyUpdateRequest == null || companyUpdateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);

        Company company = companyService.getById(companyUpdateRequest.getId());
        ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR, "企业不存在");

        // 更新基本信息
        if (companyUpdateRequest.getName() != null) {
            company.setName(companyUpdateRequest.getName());
        }
        if (companyUpdateRequest.getContactPersonId() != null) {
            company.setContactPersonId(companyUpdateRequest.getContactPersonId());
        }
        if (companyUpdateRequest.getPhone() != null) {
            company.setPhone(companyUpdateRequest.getPhone());
        }
        if (companyUpdateRequest.getEmail() != null) {
            company.setEmail(companyUpdateRequest.getEmail());
        }
        if (companyUpdateRequest.getIndustryCategory() != null) {
            company.setIndustryCategory(companyUpdateRequest.getIndustryCategory());
        }
        if (companyUpdateRequest.getIndustries() != null) {
            // 将行业子类列表转换为JSON字符串存储
            company.setIndustry(JSONUtil.toJsonStr(companyUpdateRequest.getIndustries()));
        }

        boolean result = companyService.updateById(company);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 获取公司的积分（支持admin查看任意公司，hr和company_admin查看自己的公司）
     */
    @GetMapping("/points")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<java.math.BigDecimal> getCompanyPoints(Long companyId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户未登录");

        // 确定要查询的公司ID
        Long targetCompanyId = null;
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());

        if (isAdmin) {
            // 管理员可以查看任意公司，使用传入的companyId参数
            ThrowUtils.throwIf(companyId == null || companyId <= 0,
                    ErrorCode.PARAMS_ERROR, "管理员查询时需要指定公司ID");
            targetCompanyId = companyId;
        } else {
            // HR或公司管理员只能查看自己公司的积分
            String userRole = loginUser.getUserRole();
            ThrowUtils.throwIf(!UserConstant.HR_ROLE.equals(userRole)
                    && !UserConstant.COMPANY_ADMIN_ROLE.equals(userRole),
                    ErrorCode.NO_AUTH_ERROR, "无权限访问");
            ThrowUtils.throwIf(loginUser.getCompanyId() == null,
                    ErrorCode.NO_AUTH_ERROR, "用户无公司信息");
            targetCompanyId = loginUser.getCompanyId();
        }

        java.math.BigDecimal totalPoints = companyPointsService.getTotalPoints(targetCompanyId);
        return ResultUtils.success(totalPoints);
    }

    /**
     * 分页查询公司的积分变动记录（支持admin查看任意公司，hr和company_admin查看自己的公司）
     */
    @GetMapping("/points/history")
    @AuthCheck(mustRole = UserConstant.HR_ROLE)
    public BaseResponse<Page<CompanyPointsVO>> getCompanyPointsHistory(long pageNum, long pageSize,
            Long companyId, HttpServletRequest request) {
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页参数错误");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户未登录");

        // 确定要查询的公司ID
        Long targetCompanyId = null;
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());

        if (isAdmin) {
            // 管理员可以查看任意公司，使用传入的companyId参数
            ThrowUtils.throwIf(companyId == null || companyId <= 0,
                    ErrorCode.PARAMS_ERROR, "管理员查询时需要指定公司ID");
            targetCompanyId = companyId;
        } else {
            // HR或公司管理员只能查看自己公司的积分
            String userRole = loginUser.getUserRole();
            ThrowUtils.throwIf(!UserConstant.HR_ROLE.equals(userRole)
                    && !UserConstant.COMPANY_ADMIN_ROLE.equals(userRole),
                    ErrorCode.NO_AUTH_ERROR, "无权限访问");
            ThrowUtils.throwIf(loginUser.getCompanyId() == null,
                    ErrorCode.NO_AUTH_ERROR, "用户无公司信息");
            targetCompanyId = loginUser.getCompanyId();
        }

        Page<CompanyPoints> pointsPage = companyPointsService.getPointsHistory(
                targetCompanyId, pageNum, pageSize);

        // 转换为VO
        Page<CompanyPointsVO> voPage = new Page<>(pageNum, pageSize, pointsPage.getTotalRow());
        java.util.List<CompanyPointsVO> voList = pointsPage.getRecords().stream().map(point -> {
            CompanyPointsVO vo = new CompanyPointsVO();
            cn.hutool.core.bean.BeanUtil.copyProperties(point, vo);

            // 设置变动原因文本
            PointsChangeReasonEnum reasonEnum = PointsChangeReasonEnum.getEnumByValue(point.getChangeReason());
            if (reasonEnum != null) {
                vo.setChangeReasonText(reasonEnum.getText());
            }

            // 设置员工姓名
            if (point.getWithEmployeeId() != null) {
                try {
                    com.crossorgtalentmanager.model.entity.Employee employee = employeeService
                            .getById(point.getWithEmployeeId());
                    if (employee != null) {
                        vo.setWithEmployeeName(employee.getName());
                    }
                } catch (Exception e) {
                    // 忽略错误，员工可能已删除
                }
            }

            return vo;
        }).collect(java.util.stream.Collectors.toList());

        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

}
