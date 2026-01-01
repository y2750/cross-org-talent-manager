package com.crossorgtalentmanager.model.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入员工结果
 *
 * @author y
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBatchImportResult {

    /**
     * 总行数（不包括表头）
     */
    private Integer totalRows;

    /**
     * 成功导入数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 警告数量（身份证号相同但姓名不同）
     */
    private Integer warningCount;

    /**
     * 成功导入的员工信息
     */
    private List<ImportSuccessItem> successItems;

    /**
     * 失败的行信息
     */
    private List<ImportFailItem> failItems;

    /**
     * 警告信息（身份证号相同但姓名不同）
     */
    private List<ImportWarningItem> warningItems;

    /**
     * 成功导入项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportSuccessItem {
        /**
         * 行号（从1开始，不包括表头）
         */
        private Integer rowNumber;

        /**
         * 姓名
         */
        private String name;

        /**
         * 身份证号
         */
        private String idCardNumber;

        /**
         * 操作类型：NEW（新建）、REHIRE（重新入职）
         */
        private String operationType;
    }

    /**
     * 失败项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportFailItem {
        /**
         * 行号（从1开始，不包括表头）
         */
        private Integer rowNumber;

        /**
         * 姓名
         */
        private String name;

        /**
         * 身份证号
         */
        private String idCardNumber;

        /**
         * 失败原因
         */
        private String reason;
    }

    /**
     * 警告项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportWarningItem {
        /**
         * 行号（从1开始，不包括表头）
         */
        private Integer rowNumber;

        /**
         * 姓名（导入的姓名）
         */
        private String name;

        /**
         * 身份证号
         */
        private String idCardNumber;

        /**
         * 数据库中已存在的姓名
         */
        private String existingName;

        /**
         * 警告信息
         */
        private String message;
    }

    /**
     * 创建空结果对象
     */
    public static EmployeeBatchImportResult createEmpty() {
        EmployeeBatchImportResult result = new EmployeeBatchImportResult();
        result.setTotalRows(0);
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setWarningCount(0);
        result.setSuccessItems(new ArrayList<>());
        result.setFailItems(new ArrayList<>());
        result.setWarningItems(new ArrayList<>());
        return result;
    }
}















