# 快速参考指南 - 登录认证与权限控制

## 📚 核心概念速查

### 用户角色

```typescript
type UserRole = 'admin' | 'companyAdmin' | 'hr' | 'user'
```

| 角色         | 权限     | 特殊菜单 |
| ------------ | -------- | -------- |
| admin        | 完全访问 | 用户管理 |
| companyAdmin | 管理权限 | -        |
| hr           | 基础权限 | -        |
| user         | 访问权限 | -        |

---

## 🔐 权限检查速查表

### 在 Vue 组件中检查权限

```vue
<script setup lang="ts">
import { useRole } from '@/composables/useRole'

const {
  isSystemAdmin, // 是否系统管理员
  isCompanyAdmin, // 是否公司管理员
  isHR, // 是否HR
  isNormalUser, // 是否普通用户
  canManageUsers, // 是否可以管理用户
  canAddHR, // 是否可以添加HR
  canChangePassword, // 是否可以修改密码
  getCurrentRole, // 获取当前角色
  getCurrentUserId, // 获取当前用户ID
} = useRole()
</script>

<template>
  <!-- 示例：只有系统管理员才能看到 -->
  <div v-if="isSystemAdmin()">
    <button>管理用户</button>
  </div>

  <!-- 示例：根据角色显示不同内容 -->
  <div v-if="canManageUsers()">用户管理功能</div>
</template>
```

---

## 📡 API 调用权限检查

```typescript
// ❌ 错误做法：直接调用
import * as userController from '@/api/userController'

const users = await userController.listUserVoByPage({
  pageNum: 1,
})

// ✅ 正确做法：先检查权限
import { useRole } from '@/composables/useRole'
import * as userController from '@/api/userController'

const { canViewUserList } = useRole()

if (canViewUserList()) {
  const users = await userController.listUserVoByPage({
    pageNum: 1,
  })
} else {
  message.error('没有权限')
}
```

---

## 🛣️ 路由配置参考

### 添加新的受保护路由

```typescript
// src/router/index.ts

const routes: RouteRecordRaw[] = [
  // ... 其他路由

  {
    path: '/new-page',
    name: 'newPage',
    component: () => import('../views/NewPageView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['admin', 'companyAdmin'], // 可选：限制角色
    },
  },
]
```

### 路由元数据说明

```typescript
meta: {
  requiresAuth: true,     // 是否需要登录
  roles: ['admin'],       // 允许的角色（可选）
}
```

---

## 🔑 登录/登出实现

### 从 Store 进行登录

```typescript
import { useUserStore } from '@/stores/userStore'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// 登录
const handleLogin = async (username: string, password: string) => {
  const success = await userStore.login(username, password)

  if (success) {
    router.push('/home')
  } else {
    message.error('登录失败')
  }
}

// 登出
const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
```

### 在组件中访问用户信息

```typescript
import { useUserStore } from '@/stores/userStore'
import { computed } from 'vue'

const userStore = useUserStore()

const username = computed(() => userStore.username)
const userRole = computed(() => userStore.userRole)
const isLoggedIn = computed(() => userStore.isLoggedIn)
```

---

## 🎯 页面路由映射

| 路径     | 页面     | 权限                   | 状态 |
| -------- | -------- | ---------------------- | ---- |
| `/login` | 登录页面 | 公开（已登录跳转首页） | ✅   |
| `/home`  | 首页     | 需要登录               | ✅   |
| `/about` | 关于     | 需要登录               | ✅   |
| `/users` | 用户管理 | admin 角色             | ✅   |

---

## 📝 常见任务

### 任务 1：添加权限检查的页面

```vue
<script setup lang="ts">
import { useRole } from '@/composables/useRole'
import { message } from 'ant-design-vue'

const { isSystemAdmin } = useRole()

// 检查权限
if (!isSystemAdmin()) {
  message.error('您没有权限访问此页面')
}
</script>

<template>
  <div>
    <!-- 页面内容 -->
  </div>
</template>
```

### 任务 2：调用受保护的 API

```typescript
import { useRole } from '@/composables/useRole'
import * as userController from '@/api/userController'

const { canManageUsers } = useRole()

const loadUsers = async () => {
  // 权限检查
  if (!canManageUsers()) {
    message.error('没有权限')
    return
  }

  try {
    // 调用 API
    const result = await userController.listUserVoByPage({
      pageNum: 1,
      pageSize: 10,
    })

    if (result?.data?.code === 0) {
      console.log('用户列表:', result.data.data)
    }
  } catch (error) {
    message.error('加载失败')
  }
}
```

### 任务 3：根据角色显示菜单

```vue
<script setup lang="ts">
import { useRole } from '@/composables/useRole'
import { computed } from 'vue'

const { isSystemAdmin } = useRole()

const menuItems = computed(() => {
  const items = [
    { key: 'home', label: '首页' },
    { key: 'about', label: '关于' },
  ]

  // 系统管理员的菜单
  if (isSystemAdmin()) {
    items.push({ key: 'users', label: '用户管理' })
  }

  return items
})
</script>

<template>
  <a-menu :items="menuItems" />
</template>
```

### 任务 4：实现登录页面表单

```vue
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const handleLogin = async () => {
  loading.value = true

  try {
    const success = await userStore.login(form.username, form.password)

    if (success) {
      message.success('登录成功')
      router.push('/home')
    }
  } finally {
    loading.value = false
  }
}
</script>
```

---

## 🧪 测试权限检查

### 1. 测试未登录重定向

```
访问 /home → 自动重定向 /login ✅
```

### 2. 测试权限拦截

```
登录为普通用户 → 访问 /users → 重定向 /home ✅
登录为管理员 → 访问 /users → 加载页面 ✅
```

### 3. 测试登出

```
点击登出 → 清除用户信息 → 重定向 /login ✅
刷新页面 → 保持登出状态 ✅
```

### 4. 测试页面刷新恢复

```
登录 → 刷新页面 → 保持登录状态 ✅
```

---

## ⚠️ 常见错误

### 错误 1：忘记检查权限

```typescript
// ❌ 错误：直接调用管理员 API
const users = await userController.listUserVoByPage(...)

// ✅ 正确：先检查权限
if (canManageUsers()) {
  const users = await userController.listUserVoByPage(...)
}
```

### 错误 2：使用错误的导入路径

```typescript
// ❌ 错误
import request from '@/request'

// ✅ 正确
import request from '@/utils/request'
```

### 错误 3：在模板中使用 Store 而不是计算属性

```vue
<!-- ❌ 错误：响应性不佳 -->
<template>
  <div>{{ userStore.username }}</div>
</template>

<!-- ✅ 正确：使用计算属性 -->
<script setup>
const username = computed(() => userStore.username)
</script>

<template>
  <div>{{ username }}</div>
</template>
```

---

## 🔍 调试技巧

### 查看当前用户信息

```typescript
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()

console.log('用户信息:', userStore.userInfo)
console.log('当前角色:', userStore.userRole)
console.log('是否登录:', userStore.isLoggedIn)
```

### 查看 localStorage 中的数据

```javascript
// 浏览器控制台
console.log(JSON.parse(localStorage.getItem('userInfo')))
console.log(localStorage.getItem('isLoggedIn'))
```

### 检查路由守卫

```typescript
// 在 router/index.ts 中添加日志
router.beforeEach((to, from, next) => {
  console.log('导航到:', to.path)
  console.log('用户角色:', userStore.userRole)
  console.log('是否登录:', userStore.isLoggedIn)
  // ...
})
```

---

## 📞 常见问题 Q&A

**Q: 如何修改用户角色？**  
A: 在用户管理页面编辑用户，修改角色字段后保存。

**Q: 刷新后登录状态丢失？**  
A: 检查浏览器 localStorage 是否启用，确保 isLoggedIn 标志正确保存。

**Q: API 请求 401 错误？**  
A: 检查后端是否返回正确的响应格式，确保状态码为 0 表示成功。

**Q: 权限检查不生效？**  
A: 确保使用的是 `useRole()` 组合式函数返回的方法。

**Q: 如何添加新角色？**  
A: 修改 `src/stores/userStore.ts` 中的角色类型定义和 `src/composables/useRole.ts` 中的权限检查。

---

## 📚 相关文件

| 文件                                     | 用途           |
| ---------------------------------------- | -------------- |
| `src/stores/userStore.ts`                | 用户状态管理   |
| `src/composables/useRole.ts`             | 权限检查函数   |
| `src/utils/request.ts`                   | API 请求拦截   |
| `src/router/index.ts`                    | 路由和守卫配置 |
| `src/views/LoginView.vue`                | 登录页面       |
| `src/views/UserManagementView.vue`       | 用户管理页面   |
| `src/components/layout/GlobalHeader.vue` | 导航栏         |
| `AUTHENTICATION_GUIDE.md`                | 详细文档       |

---

## 🎨 Ant Design Vue 常用组件

```vue
<!-- 表单 -->
<a-form :model="form" :rules="rules">
  <a-form-item label="用户名" name="username">
    <a-input v-model:value="form.username" />
  </a-form-item>
</a-form>

<!-- 按钮 -->
<a-button type="primary" :loading="loading">提交</a-button>

<!-- 表格 -->
<a-table :columns="columns" :data-source="data" :pagination="pagination" />

<!-- 模态框 -->
<a-modal v-model:visible="visible" @ok="handleOk">
  <p>内容</p>
</a-modal>

<!-- 提示 -->
<a-button @click="message.success('成功')">Success</a-button>

<!-- 下拉菜单 -->
<a-dropdown>
  <template #overlay>
    <a-menu @click="handleMenuClick">
      <a-menu-item key="1">选项1</a-menu-item>
    </a-menu>
  </template>
</a-dropdown>
```

---

**最后更新：** 2025年11月24日  
**版本：** 1.0.0
