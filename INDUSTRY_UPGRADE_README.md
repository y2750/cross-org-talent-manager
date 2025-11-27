# 企业行业分类优化说明

## 概述

本次优化将企业行业字段从单一字段改为"大类+子类"的二级分类结构，支持企业选择一个大类和多个子类。

## 修改内容

### 1. 数据库层面

**修改文件**: `sql.sql`, `sql_upgrade.sql`

- 在 `company` 表中新增 `industry_category` 字段，用于存储行业大类
- 修改 `industry` 字段长度为 500，用于存储行业子类的 JSON 数组（格式：`["子类1","子类2"]`）

**升级现有数据库**:

```sql
-- 执行升级脚本
source sql_upgrade.sql;
```

### 2. 后端层面

#### 2.1 实体类修改

**修改文件**: `src/main/java/com/crossorgtalentmanager/model/entity/Company.java`

- 新增 `industryCategory` 字段（行业大类）
- 保留 `industry` 字段（存储 JSON 格式的行业子类数组）

#### 2.2 VO 类修改

**修改文件**: `src/main/java/com/crossorgtalentmanager/model/vo/CompanyVO.java`

- 新增 `industryCategory` 字段
- 将 `industry` 字段改为 `industries`，类型为 `List<String>`

#### 2.3 DTO 类修改

**新增文件**: `src/main/java/com/crossorgtalentmanager/model/dto/company/CompanyUpdateRequest.java`

**修改文件**:

- `CompanyAddRequest.java` - 新增 `industryCategory` 和 `industries` 字段
- `CompanyQueryRequest.java` - 新增 `industryCategory` 和 `industries` 字段用于筛选

#### 2.4 Service 层修改

**修改文件**:

- `src/main/java/com/crossorgtalentmanager/service/CompanyService.java`
- `src/main/java/com/crossorgtalentmanager/service/impl/CompanyServiceImpl.java`

核心改动：

- `addCompany` 方法：接收 `industryCategory` 和 `List<String> industries`，并将 industries 转换为 JSON 字符串存储
- `getCompanyVO` 方法：将数据库中的 JSON 字符串解析为 List<String> 返回给前端
- `getQueryWrapper` 方法：支持按行业大类和子类进行筛选
  - 行业大类使用精确匹配（`eq`）
  - 行业子类使用 LIKE 查询，支持多个子类的 OR 条件匹配
  - 生成的 SQL 类似：`AND (industry LIKE '%子类1%' OR industry LIKE '%子类2%')`

#### 2.5 Controller 层修改

**修改文件**: `src/main/java/com/crossorgtalentmanager/controller/CompanyController.java`

- 修改 `addCompany` 方法：处理新的行业字段
- 修改 `update` 方法：使用 `CompanyUpdateRequest`，支持更新行业信息

### 3. 前端层面

#### 3.1 行业分类常量

**新增文件**: `vue-project/src/constants/industry.ts`

提供了完整的行业分类数据结构，包含 10 个大类和对应的子类：

1. 制造业（9 个子类）
2. 信息技术/互联网（8 个子类）
3. 金融/保险（5 个子类）
4. 房地产/建筑（5 个子类）
5. 专业服务（6 个子类）
6. 消费零售（5 个子类）
7. 医疗健康（5 个子类）
8. 能源/环保（5 个子类）
9. 教育/文化（5 个子类）
10. 其他（5 个子类）

提供的工具方法：

- `getIndustryCategories()` - 获取所有大类
- `getIndustrySubCategories(category)` - 根据大类获取子类列表

#### 3.2 API 类型定义

**修改文件**: `vue-project/src/api/typings.d.ts`

修改的类型：

- `Company` - 添加 `industryCategory` 字段
- `CompanyVO` - 添加 `industryCategory` 和 `industries: string[]` 字段
- `CompanyAddRequest` - 添加 `industryCategory` 和 `industries: string[]` 字段
- `CompanyQueryRequest` - 添加 `industryCategory` 和 `industries: string[]` 字段用于筛选
- 新增 `CompanyUpdateRequest` 类型

**修改文件**: `vue-project/src/api/companyController.ts`

- 修改 `update` 方法的参数类型为 `CompanyUpdateRequest`

## 使用示例

### 后端使用

#### 添加企业

```java
CompanyAddRequest request = new CompanyAddRequest();
request.setName("示例公司");
request.setIndustryCategory("信息技术/互联网");
request.setIndustries(Arrays.asList("软件开发", "人工智能", "大数据/云计算"));
```

#### 查询企业

```java
CompanyQueryRequest queryRequest = new CompanyQueryRequest();
queryRequest.setIndustryCategory("信息技术/互联网");  // 按大类筛选
queryRequest.setIndustries(Arrays.asList("人工智能"));  // 按子类筛选（可选多个）
```

### 前端使用

#### 导入行业分类常量

```typescript
import {
  INDUSTRY_CATEGORIES,
  getIndustryCategories,
  getIndustrySubCategories,
} from "@/constants/industry";

// 获取所有大类
const categories = getIndustryCategories();

// 根据大类获取子类
const subCategories = getIndustrySubCategories("信息技术/互联网");
```

#### 添加/更新企业

```typescript
import { addCompany, update } from "@/api/companyController";

// 添加企业
const addRequest: API.CompanyAddRequest = {
  name: "示例公司",
  industryCategory: "信息技术/互联网",
  industries: ["软件开发", "人工智能"],
};
await addCompany(addRequest);

// 更新企业
const updateRequest: API.CompanyUpdateRequest = {
  id: 123,
  industryCategory: "信息技术/互联网",
  industries: ["软件开发", "人工智能", "大数据/云计算"],
};
await update(updateRequest);
```

#### 查询和筛选

```typescript
import { listCompanyVoByPage } from "@/api/companyController";

const queryRequest: API.CompanyQueryRequest = {
  pageNum: 1,
  pageSize: 10,
  industryCategory: "信息技术/互联网", // 按大类筛选
  industries: ["人工智能", "软件开发"], // 按子类筛选（包含任意一个即匹配）
};
const response = await listCompanyVoByPage(queryRequest);
```

#### 展示企业行业信息

```vue
<template>
  <div>
    <div>行业大类：{{ company.industryCategory }}</div>
    <div>
      行业子类：
      <span v-for="(industry, index) in company.industries" :key="index">
        {{ industry }}
        <span v-if="index < company.industries.length - 1">, </span>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { getCompanyVoById } from "@/api/companyController";

const company = ref<API.CompanyVO>();

const loadCompany = async (id: number) => {
  const res = await getCompanyVoById({ id });
  if (res.code === 0) {
    company.value = res.data;
  }
};
</script>
```

## 注意事项

1. **数据迁移**: 如果已有数据，需要手动将旧的 `industry` 字段数据迁移到新的结构
2. **JSON 格式**: 后端存储的行业子类格式为标准 JSON 数组：`["子类1","子类2"]`
3. **筛选逻辑**: 在查询时，如果指定了多个子类，只要企业包含其中任意一个子类就会被匹配
4. **必填字段**: 添加企业时，`industryCategory` 为必填，`industries` 可选
5. **前端展示**: 前端展示企业时，应同时显示行业大类和所有选中的子类

## 测试建议

1. 测试添加企业：选择一个大类和多个子类
2. 测试更新企业：修改行业大类和子类
3. 测试查询：
   - 按大类筛选
   - 按单个子类筛选
   - 按多个子类筛选
4. 测试展示：确保前端正确显示所有行业信息

## 回滚方案

如需回滚到旧版本：

```sql
-- 删除新增的字段
ALTER TABLE `company` DROP COLUMN `industry_category`;

-- 将industry字段改回原长度
ALTER TABLE `company` MODIFY COLUMN `industry` varchar(50);
```

并恢复相关代码文件的修改。
