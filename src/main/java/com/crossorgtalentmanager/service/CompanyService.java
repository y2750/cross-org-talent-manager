package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.company.CompanyQueryRequest;
import com.crossorgtalentmanager.model.vo.CompanyVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.crossorgtalentmanager.model.entity.Company;

import java.util.List;

/**
 * 企业信息管理 服务层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface CompanyService extends IService<Company> {

    long addCompany(String name, Long contactPersonId, String phone, String email, String industry);

    CompanyVO getCompanyVO(Company company);

    List<CompanyVO> getCompanyVOList(java.util.List<Company> companyList);

    QueryWrapper getQueryWrapper(
            CompanyQueryRequest companyQueryRequest);

    Boolean removeById(Long id);
}
