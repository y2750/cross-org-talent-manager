package com.crossorgtalentmanager.service.impl;

import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.exception.ThrowUtils;
import com.crossorgtalentmanager.mapper.CompanyPointsMapper;
import com.crossorgtalentmanager.model.entity.Company;
import com.crossorgtalentmanager.model.entity.CompanyPoints;
import com.crossorgtalentmanager.model.enums.PointsChangeReasonEnum;
import com.crossorgtalentmanager.service.CompanyPointsService;
import com.crossorgtalentmanager.service.CompanyService;
import com.crossorgtalentmanager.service.EmployeeService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 企业积分服务实现类
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Slf4j
@Service
public class CompanyPointsServiceImpl extends ServiceImpl<CompanyPointsMapper, CompanyPoints>
        implements CompanyPointsService {

    @Resource
    private CompanyService companyService;

    @Resource
    private CompanyPointsMapper companyPointsMapper;

    @Resource
    private EmployeeService employeeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addPoints(Long companyId, BigDecimal points, Integer changeReason, Long withEmployeeId, String changeDescription) {
        ThrowUtils.throwIf(companyId == null || companyId <= 0, ErrorCode.PARAMS_ERROR, "企业ID不能为空");
        ThrowUtils.throwIf(points == null || points.compareTo(BigDecimal.ZERO) == 0,
                ErrorCode.PARAMS_ERROR, "积分不能为0");
        ThrowUtils.throwIf(changeReason == null || !PointsChangeReasonEnum.isValidReason(changeReason),
                ErrorCode.PARAMS_ERROR, "积分变动原因无效");

        // 验证企业是否存在
        Company company = companyService.getById(companyId);
        ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR, "企业不存在");

        // 自动生成变动说明（如果未提供）
        String finalChangeDescription = changeDescription;
        if (finalChangeDescription == null || finalChangeDescription.isEmpty()) {
            finalChangeDescription = generateChangeDescription(changeReason, withEmployeeId, points);
        }

        // 创建积分变动记录
        CompanyPoints companyPoints = CompanyPoints.builder()
                .companyId(companyId)
                .points(points)
                .changeReason(changeReason)
                .withEmployeeId(withEmployeeId)
                .changeDescription(finalChangeDescription)
                .changeDate(LocalDate.now())
                .isDelete(false)
                .build();

        boolean saved = this.save(companyPoints);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "保存积分变动记录失败");

        // 更新企业总积分
        BigDecimal currentTotal = company.getTotalPoints();
        if (currentTotal == null) {
            currentTotal = BigDecimal.ZERO;
        }
        BigDecimal newTotal = currentTotal.add(points);
        company.setTotalPoints(newTotal);
        boolean updated = companyService.updateById(company);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新企业总积分失败");

        log.info("企业积分增加成功：companyId={}, points={}, reason={}, totalPoints={}",
                companyId, points, PointsChangeReasonEnum.getEnumByValue(changeReason) != null
                        ? PointsChangeReasonEnum.getEnumByValue(changeReason).getText()
                        : changeReason, newTotal);

        return companyPoints.getId();
    }

    @Override
    public BigDecimal getTotalPoints(Long companyId) {
        ThrowUtils.throwIf(companyId == null || companyId <= 0, ErrorCode.PARAMS_ERROR, "企业ID不能为空");
        Company company = companyService.getById(companyId);
        ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR, "企业不存在");
        return company.getTotalPoints() != null ? company.getTotalPoints() : BigDecimal.ZERO;
    }

    @Override
    public Page<CompanyPoints> getPointsHistory(Long companyId, long pageNum, long pageSize) {
        ThrowUtils.throwIf(companyId == null || companyId <= 0, ErrorCode.PARAMS_ERROR, "企业ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("company_id", companyId)
                .orderBy("change_date", false)
                .orderBy("create_time", false);
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    @Override
    public List<CompanyPoints> listPointsHistory(Long companyId) {
        ThrowUtils.throwIf(companyId == null || companyId <= 0, ErrorCode.PARAMS_ERROR, "企业ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("company_id", companyId)
                .orderBy("change_date", false)
                .orderBy("create_time", false);
        return this.list(queryWrapper);
    }

    /**
     * 根据变动原因自动生成变动说明
     *
     * @param changeReason   变动原因
     * @param withEmployeeId 关联员工ID
     * @return 变动说明
     */
    private String generateChangeDescription(Integer changeReason, Long withEmployeeId, BigDecimal points) {
        PointsChangeReasonEnum reasonEnum = PointsChangeReasonEnum.getEnumByValue(changeReason);
        if (reasonEnum == null) {
            return "积分变动";
        }

        String employeeName = null;
        if (withEmployeeId != null) {
            try {
                com.crossorgtalentmanager.model.entity.Employee employee = employeeService.getById(withEmployeeId);
                if (employee != null) {
                    employeeName = employee.getName();
                }
            } catch (Exception e) {
                log.warn("获取员工信息失败，employeeId={}", withEmployeeId, e);
            }
        }

        String employeeInfo = employeeName != null ? employeeName : (withEmployeeId != null ? "员工ID:" + withEmployeeId : "");

        switch (reasonEnum) {
            case CREATE_PROFILE:
                return employeeInfo != null && !employeeInfo.isEmpty()
                        ? String.format("为离职员工%s建立完整档案", employeeInfo)
                        : "为离职员工建立完整档案";
            case EMPLOYEE_EVALUATION:
                return employeeInfo != null && !employeeInfo.isEmpty()
                        ? String.format("对员工%s进行了客观评价", employeeInfo)
                        : "对员工进行了客观评价";
            case RIGHTS_CONSUMPTION:
                return "使用权益消耗积分";
            case EVALUATION_APPEAL:
                return employeeInfo != null && !employeeInfo.isEmpty()
                        ? String.format("对员工%s的评价进行申诉", employeeInfo)
                        : "对员工评价进行申诉";
            default:
                return "积分变动";
        }
    }
}

