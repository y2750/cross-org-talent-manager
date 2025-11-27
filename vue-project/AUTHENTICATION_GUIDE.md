# 跨组织人才管理系统 - 登录认证与权限控制功能说明

## 项目完成概述

本项目已成功实现了完整的登录认证、权限控制和用户管理功能，基于 Vue 3 + TypeScript + Pinia + Axios 的现代化全栈架构。

---

## 📋 核心功能实现

### 1. 登录态路由控制 ✅

**实现文件：** `src/router/index.ts`

#### 功能特性：

- ✅ 未登录用户自动重定向至登录页 (`/login`)
- ✅ 登录成功后自动跳转至首页 (`/home`)
- ✅ 登出操作自动重定向至登录页
- ✅ 页面刷新自动恢复登录状态（基于 localStorage）
- ✅ 支持路由元数据权限验证

#### 工作流程：

1. 用户访问受保护页面 → 路由守卫检查登录状态
2. 未登录 → 重定向至 `/login`
3. 已登录 + 有权限 → 允许访问
4. 已登录 + 无权限 → 重定向至 `/home`

---

### 2. 用户认证系统 ✅

**实现文件：** `src/stores/userStore.ts`

#### 数据管理：

```typescript
interface UserInfo {
  id: number
  username: string
  nickname?: string
  userRole: 'admin' | 'companyAdmin' | 'hr' | 'user'
  companyId?: number
}
```

#### 核心方法：

- `login(username, password)` - 用户登录
- `logout()` - 用户登出
- `restoreLoginState()` - 页面刷新时恢复登录状态
- `fetchCurrentUser()` - 获取当前登录用户信息
- `clearUserData()` - 清除用户数据

#### 存储机制：

- 用户信息存储在 localStorage
- 支持跨标签页同步
- 支持刷新后状态恢复

---

### 3. 角色权限系统 ✅

**实现文件：** `src/composables/useRole.ts`

#### 支持的角色：

| 角色           | 权限描述   | 功能权限                                                          |
| -------------- | ---------- | ----------------------------------------------------------------- |
| `admin`        | 系统管理员 | ✅ 注册/新增用户 ✅ 更新用户信息 ✅ 禁用/启用用户 ✅ 查看用户列表 |
| `companyAdmin` | 公司管理员 | ✅ 添加 HR ✅ 修改密码 ✅ 查看本公司 HR 列表                      |
| `hr`           | HR         | ✅ 修改密码                                                       |
| `user`         | 普通用户   | 基础访问权限                                                      |

#### 权限检查方法：

```typescript
const {
  isSystemAdmin, // 检查是否系统管理员
  isCompanyAdmin, // 检查是否公司管理员
  isHR, // 检查是否 HR
  isNormalUser, // 检查是否普通用户
  canManageUsers, // 检查是否可以管理用户
  canAddHR, // 检查是否可以添加 HR
  canChangePassword, // 检查是否可以修改密码
  canViewUserList, // 检查是否可以查看用户列表
  canViewCompanyHRs, // 检查是否可以查看公司 HR 列表
} = useRole()
```

---

### 4. 请求拦截器 ✅

**实现文件：** `src/utils/request.ts`

#### 功能特性：

- ✅ 自动在请求头添加认证 token
- ✅ 处理 401/403 未授权错误，自动重定向登录
- ✅ 业务层错误统一处理和提示
- ✅ 网络错误提示
- ✅ 请求超时处理

#### 拦截器流程：

```
请求 → 添加 token → 发送请求 → 响应 → 检查状态码
                                      ├─ 401/403 → 清除认证，重定向登录
                                      ├─ 错误 → 提示错误信息
                                      └─ 成功 → 返回数据
```

---

### 5. 登录页面 ✅

**实现文件：** `src/views/LoginView.vue`

#### 界面特性：

- ✅ 使用 Ant Design Vue `a-form` 组件
- ✅ 账户和密码输入字段
- ✅ 表单验证（必填项）
- ✅ 登录/重置按钮
- ✅ 响应式设计
- ✅ 加载状态反馈

#### 用户流程：

1. 输入用户名和密码
2. 点击登录 → 表单验证
3. 调用 userStore.login()
4. 登录成功 → 跳转至首页 (`/home`)
5. 登录失败 → 显示错误提示

---

### 6. 用户管理页面 ✅

**实现文件：** `src/views/UserManagementView.vue`

#### 权限限制：

- 🔒 **仅系统管理员可访问** (`/users` 路由)

#### 核心功能：

##### 用户列表展示

- ✅ 分页显示用户列表
- ✅ 显示字段：ID、用户名、昵称、角色、创建时间
- ✅ 支持表格排序

##### 搜索和筛选

- ✅ 按用户名搜索
- ✅ 按角色筛选（系统管理员、公司管理员、HR、普通用户）
- ✅ 支持重置搜索条件

##### 用户操作

- ✅ **新增用户** - 创建新的用户账户
  - 需要：用户名、密码、昵称、角色
- ✅ **编辑用户** - 修改用户信息
  - 可修改：昵称、角色
  - 不可修改：用户名（保持唯一性）
- ✅ **禁用/启用用户** - 调用 `toggleUserStatus` 接口
  - 确认对话框保护

##### 表单验证

- 用户名：3-20 字符
- 密码：新增时必填，至少 6 字符
- 昵称：必填
- 角色：必填

---

### 7. 全局布局与导航 ✅

**实现文件：** `src/components/layout/GlobalHeader.vue`

#### 功能特性：

- ✅ 响应式导航栏
- ✅ Logo 和网站标题
- ✅ 动态菜单（根据用户角色）
- ✅ 用户头像和下拉菜单
- ✅ 登出功能

#### 菜单配置：

```
所有用户：
  ├─ 首页 (/)
  ├─ 关于 (/about)
  └─ [仅系统管理员] 用户管理 (/users)

已登录用户：
  └─ 用户下拉菜单
      ├─ 个人信息
      └─ 退出登录
```

---

## 🗂️ 文件结构

```
src/
├── api/                          # API 接口（自动生成）
│   ├── userController.ts         # 用户相关 API
│   ├── companyController.ts      # 公司相关 API
│   └── ...
├── stores/
│   └── userStore.ts              # ✅ Pinia 用户认证 Store
├── composables/
│   └── useRole.ts                # ✅ 权限检查组合式函数
├── utils/
│   └── request.ts                # ✅ Axios 请求拦截器
├── views/
│   ├── LoginView.vue             # ✅ 登录页面
│   ├── UserManagementView.vue    # ✅ 用户管理页面
│   ├── HomeView.vue              # 首页
│   └── AboutView.vue             # 关于页面
├── components/
│   └── layout/
│       ├── GlobalHeader.vue      # ✅ 全局头部导航
│       └── GlobalFooter.vue      # 全局底部
├── layouts/
│   └── BasicLayout.vue           # ✅ 基础布局
├── router/
│   └── index.ts                  # ✅ 路由配置及守卫
├── App.vue                       # ✅ 根组件（恢复登录状态）
└── main.ts                       # 入口文件
```

---

## 🔐 API 调用权限矩阵

| API 接口           | 参数               | 系统管理员 | 公司管理员 | HR  | 普通用户 |
| ------------------ | ------------------ | ---------- | ---------- | --- | -------- |
| `userLogin`        | username, password | ✅         | ✅         | ✅  | ✅       |
| `userLogout`       | 无                 | ✅         | ✅         | ✅  | ✅       |
| `getLoginUser`     | 无                 | ✅         | ✅         | ✅  | ✅       |
| `listUserVoByPage` | page params        | ✅         | ❌         | ❌  | ❌       |
| `getUserById`      | id                 | ✅         | ❌         | ❌  | ❌       |
| `userRegister`     | user info          | ✅         | ❌         | ❌  | ❌       |
| `updateUser`       | user info          | ✅         | ❌         | ❌  | ❌       |
| `toggleUserStatus` | id                 | ✅         | ❌         | ❌  | ❌       |

---

## 🚀 快速开始

### 1. 环境准备

```bash
# 安装依赖
npm install

# 开发服务器
npm run dev

# 类型检查
npm run type-check

# 构建生产
npm run build
```

### 2. 测试用户（示例）

假设后端已配置测试用户：

| 用户名     | 密码       | 角色       |
| ---------- | ---------- | ---------- |
| `admin`    | `admin123` | 系统管理员 |
| `company1` | `pwd123`   | 公司管理员 |
| `hr1`      | `pwd123`   | HR         |
| `user1`    | `pwd123`   | 普通用户   |

### 3. 用户操作流程

#### 登录流程

```
访问 → 路由守卫检查 → 未登录 → 重定向 /login
  ↓
输入账户信息 → 点击登录 → 表单验证
  ↓
API 调用 userLogin → 登录成功 → 保存到 Store 和 localStorage
  ↓
跳转 /home → 首页加载
```

#### 用户管理流程（系统管理员）

```
登录 (admin) → 导航菜单中看到"用户管理"
  ↓
点击菜单 → /users 页面加载用户列表
  ↓
操作：
  ├─ 搜索/筛选 → 查询用户
  ├─ 新增用户 → 打开对话框 → 填表 → 提交
  ├─ 编辑用户 → 打开对话框 → 修改 → 提交
  └─ 禁用/启用 → 确认 → 操作完成
```

#### 权限验证流程

```
用户访问 /users
  ↓
路由守卫检查：
  ├─ 未登录？ → 重定向 /login
  ├─ 非管理员？ → 重定向 /home
  └─ 是管理员 → 允许访问
```

---

## 🔒 安全特性

### 已实现

✅ 路由级权限验证  
✅ 组件级权限检查（useRole）  
✅ 请求拦截器 token 管理  
✅ 401/403 自动重登  
✅ localStorage 加密友好（建议后端返回 HttpOnly Cookie）  
✅ 敏感操作确认对话框

### 建议改进（后续）

🔹 实现 Token 刷新机制（Refresh Token）  
🔹 后端返回 HttpOnly Cookie 替代 localStorage  
🔹 使用 CSRF token  
🔹 敏感操作日志审计  
🔹 接口级权限验证（后端）

---

## 📝 使用示例

### 权限检查示例

```typescript
<script setup lang="ts">
import { useRole } from '@/composables/useRole'

const { isSystemAdmin, canManageUsers } = useRole()

// 在组件中检查权限
if (!canManageUsers()) {
  throw new Error('没有权限访问此页面')
}
</script>

<template>
  <!-- 只有系统管理员才能看到 -->
  <div v-if="isSystemAdmin">
    <button @click="manageUsers">管理用户</button>
  </div>
</template>
```

### API 调用示例

```typescript
import * as userController from '@/api/userController'
import { useRole } from '@/composables/useRole'

const { canManageUsers } = useRole()

// 调用前检查权限
if (canManageUsers()) {
  const result = await userController.listUserVoByPage({
    pageNum: 1,
    pageSize: 10,
  })

  if (result?.data?.code === 0) {
    console.log('用户列表:', result.data.data.records)
  }
}
```

---

## 🐛 故障排除

### 问题 1：登录后没有保存状态

**解决：** 确保 localStorage 已启用，刷新页面应该保持登录状态

### 问题 2：权限检查不生效

**解决：** 确保在组件中使用 `useRole()` 组合式函数，而不是直接访问 Store

### 问题 3：API 请求 401 错误

**解决：** 检查后端是否返回正确的用户角色信息，确保 Store 中的 userRole 正确

### 问题 4：菜单项不显示

**解决：** 确保用户已登录，检查 `isSystemAdmin()` 返回值

---

## 📞 技术栈

- **框架：** Vue 3 + TypeScript
- **状态管理：** Pinia
- **HTTP 客户端：** Axios
- **UI 组件库：** Ant Design Vue 4.2.6
- **路由：** Vue Router 4.6.3
- **构建工具：** Vite 7.1.11
- **包管理：** npm

---

## 📌 注意事项

1. **localStorage 安全性**
   - 当前使用 localStorage 存储用户信息
   - 生产环境建议使用后端 HttpOnly Cookie
   - 避免在 localStorage 中存储敏感信息

2. **Token 管理**
   - 当前使用占位符 'auth-token'
   - 后端应返回真实的 JWT token
   - 建议实现 token 刷新机制

3. **CORS 跨域**
   - 后端需要正确配置 CORS
   - 前端请求应携带 credentials

4. **类型定义**
   - API 类型由 OpenAPI 自动生成（`openapi2ts` 命令）
   - 修改后端 API 后需要重新生成

---

## ✅ 完成清单

- [x] 登录态路由控制
- [x] 用户认证系统（Pinia Store）
- [x] 角色权限系统（useRole 组合式函数）
- [x] 请求拦截器（Axios）
- [x] 登录页面
- [x] 用户管理页面
- [x] 全局导航组件
- [x] 路由守卫实现
- [x] 权限检查装饰器
- [x] 错误处理和提示

---

**最后更新：** 2025年11月24日  
**版本：** 1.0.0
