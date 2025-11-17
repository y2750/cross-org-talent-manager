package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.mapper.CompanyMapper;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.model.dto.company.CompanyQueryRequest;
import com.crossorgtalentmanager.model.vo.CompanyVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业信息管理 服务层实现。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Service
@Slf4j
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

    @Resource
    private UserService userService;

    @Override
    public long addCompany(String name, Long contactPersonId, String phone, String email, String industry) {

        ThrowUtils.throwIf(
                name == null || contactPersonId == null || phone == null || email == null || industry == null,
                ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userService.getById(contactPersonId) == null, ErrorCode.PARAMS_ERROR, "企业负责人不存在");
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("name", name);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "企业名称已存在");
        Company company = new Company();
        company.setName(name);
        company.setContactPersonId(contactPersonId);
        company.setPhone(phone);
        company.setEmail(email);
        company.setIndustry(industry);
        boolean result = this.save(company);
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR, "企业添加失败");
        User user = new User();
        user.setId(contactPersonId);
        user.setCompanyId(company.getId());
        userService.updateById(user);
        return company.getId();
    }

    @Override
    public CompanyVO getCompanyVO(Company company) {
        if (company == null) {
            return null;
        }
        CompanyVO companyVO = new CompanyVO();
        BeanUtil.copyProperties(company, companyVO);
        return companyVO;
    }

    @Override
    public List<CompanyVO> getCompanyVOList(List<Company> companyList) {
        if (CollUtil.isEmpty(companyList)) {
            return new ArrayList<>();
        }
        // 先将 Company -> CompanyVO（不包含 contactPersonName），然后批量查询 user 表获取 nickname，避免
        // N+1
        List<CompanyVO> companyVOList = companyList.stream().map(this::getCompanyVO).collect(Collectors.toList());

        // 收集所有 contactPersonId
        java.util.Set<Long> contactPersonIds = companyList.stream()
                .map(Company::getContactPersonId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        if (!contactPersonIds.isEmpty()) {
            // 使用 userService 的批量查询接口（IService 提供 listByIds）来获取用户信息
            java.util.List<com.crossorgtalentmanager.model.entity.User> users = userService.listByIds(contactPersonIds);
            java.util.Map<Long, String> idToNickname = users.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toMap(com.crossorgtalentmanager.model.entity.User::getId,
                            com.crossorgtalentmanager.model.entity.User::getNickname, (a, b) -> a));

            // 填充 contactPersonName
            for (int i = 0; i < companyVOList.size(); i++) {
                CompanyVO vo = companyVOList.get(i);
                Long contactId = vo.getContactPersonId();
                if (contactId != null) {
                    vo.setContactPersonName(idToNickname.get(contactId));
                }
            }
        }
        return companyVOList;
    }

    @Override
    public QueryWrapper getQueryWrapper(CompanyQueryRequest companyQueryRequest) {
        ThrowUtils.throwIf(companyQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = companyQueryRequest.getId();
        String name = companyQueryRequest.getName();
        String contactPersonName = companyQueryRequest.getContactPersonName();
        String industry = companyQueryRequest.getIndustry();
        String sortField = companyQueryRequest.getSortField();
        String sortOrder = companyQueryRequest.getSortOrder();

        User user = new User();
        if (contactPersonName != null) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("nickname", contactPersonName);
            user = userService.getOne(queryWrapper);
        }

        return QueryWrapper.create()
                .eq("id", id)
                .like("name", name)
                .eq("contact_person_id", user.getId())
                .like("industry", industry)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public Boolean removeById(Long id) {
        // 说明：ServiceImpl.getById / mapper.selectOneByQuery 都可能受 mybatis-flex 的逻辑删除过滤
        // 导致当 is_delete = 1 时无法读到记录。为兼容“恢复已逻辑删除记录”的场景，先尝试使用
        // getById 读取（能读取到未被逻辑删除的记录并切换），若为 null 则说明记录可能已被逻辑删除，
        // 直接执行 updateById 将 isDelete 置为 false 来恢复记录。
        Company company = null;
        try {
            company = getById(id);
        } catch (Exception ignored) {
            // 有些情况下 getById 可能会抛出异常或返回 null；统一按 null 处理
        }

        if (company != null) {
            Company deleteCompany = new Company();
            BeanUtil.copyProperties(company, deleteCompany);
            // 切换逻辑删除标志
            deleteCompany.setIsDelete(!Boolean.TRUE.equals(deleteCompany.getIsDelete()));
            return updateById(deleteCompany);
        } else {
            // 记录可能存在但已被逻辑删除（is_delete = 1），直接调用 mapper 的原生 SQL 方法恢复。
            int affected = this.mapper.restoreById(id);
            // affected == 0 表示没有行被更新（记录不存在）
            ThrowUtils.throwIf(affected <= 0, ErrorCode.NOT_FOUND_ERROR);
            return affected > 0;
        }
    }
}
