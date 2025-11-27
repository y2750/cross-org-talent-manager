package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
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
import java.util.Objects;
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
    public long addCompany(String name, Long contactPersonId, String phone, String email, String industryCategory,
            List<String> industries) {

        ThrowUtils.throwIf(
                name == null || contactPersonId == null || phone == null || email == null || industryCategory == null,
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
        company.setIndustryCategory(industryCategory);
        // 将行业子类列表转换为JSON字符串存储
        if (industries != null && !industries.isEmpty()) {
            company.setIndustry(JSONUtil.toJsonStr(industries));
        }

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

        // 将JSON字符串转换为行业子类列表
        if (company.getIndustry() != null && !company.getIndustry().isEmpty()) {
            try {
                List<String> industries = JSONUtil.toList(company.getIndustry(), String.class);
                companyVO.setIndustries(industries);
            } catch (Exception e) {
                log.error("解析行业子类JSON失败: {}", company.getIndustry(), e);
                companyVO.setIndustries(new ArrayList<>());
            }
        } else {
            companyVO.setIndustries(new ArrayList<>());
        }

        // 填充 contactPersonName
        if (company.getContactPersonId() != null) {
            User contactPerson = userService.getById(company.getContactPersonId());
            if (contactPerson != null) {
                companyVO.setContactPersonName(contactPerson.getNickname());
            }
        }

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
        Objects.requireNonNull(companyQueryRequest, "companyQueryRequest");
        Long id = companyQueryRequest.getId();
        String name = companyQueryRequest.getName();
        String contactPersonName = companyQueryRequest.getContactPersonName();
        String industryCategory = companyQueryRequest.getIndustryCategory();
        List<String> industries = companyQueryRequest.getIndustries();
        String sortField = companyQueryRequest.getSortField();
        String sortOrder = companyQueryRequest.getSortOrder();

        User user = new User();
        if (contactPersonName != null) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("nickname", contactPersonName);
            user = userService.getOne(queryWrapper);
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id)
                .like("name", name)
                .eq("industry_category", industryCategory);

        if (user != null && user.getId() != null) {
            queryWrapper.eq("contact_person_id", user.getId());
        }

        // 如果指定了行业子类筛选，需要匹配任意一个子类
        if (industries != null && !industries.isEmpty()) {
            // 使用OR条件，只要包含任意一个子类就匹配
            StringBuilder sqlCondition = new StringBuilder("(");
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < industries.size(); i++) {
                if (i > 0) {
                    sqlCondition.append(" OR ");
                }
                sqlCondition.append("industry LIKE ?");
                params.add("%" + industries.get(i) + "%");
            }
            sqlCondition.append(")");
            queryWrapper.and(sqlCondition.toString(), params.toArray());
        }

        String dbSortField = mapToDatabaseColumn(sortField);
        if (dbSortField != null && !dbSortField.isEmpty()) {
            queryWrapper.orderBy(dbSortField, "ascend".equals(sortOrder));
        }

        return queryWrapper;
    }

    /**
     * 将 Java 字段名转换为数据库列名，防止排序字段找不到。
     */
    private String mapToDatabaseColumn(String javaFieldName) {
        if (javaFieldName == null) {
            return null;
        }
        switch (javaFieldName) {
            case "createTime":
                return "create_time";
            case "updateTime":
                return "update_time";
            case "contactPersonId":
                return "contact_person_id";
            case "isDelete":
                return "is_delete";
            default:
                return javaFieldName;
        }
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
