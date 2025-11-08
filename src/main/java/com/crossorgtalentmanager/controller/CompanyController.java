package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.common.DeleteRequest;
import com.crossorgtalentmanager.constant.UserConstant;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.model.dto.company.CompanyQueryRequest;
import com.crossorgtalentmanager.model.vo.CompanyVO;
import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.dto.company.CompanyAddRequest;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.service.CompanyService;


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

    @PostMapping("/add")
    public BaseResponse<Long> addCompany(@RequestBody CompanyAddRequest companyAddRequest) {
        ThrowUtils.throwIf(companyAddRequest == null, ErrorCode.PARAMS_ERROR);
        String name = companyAddRequest.getName();
        Long contactPersonId = companyAddRequest.getContactPersonId();
        String phone = companyAddRequest.getPhone();
        String email = companyAddRequest.getEmail();
        String industry = companyAddRequest.getIndustry();
        long result = companyService.addCompany(name, contactPersonId, phone, email, industry);
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
        BaseResponse<Company> response = getCompanyById(id);
        Company company = response.getData();
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
     * @param company 企业信息管理
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody Company company) {
        return companyService.updateById(company);
    }


}
