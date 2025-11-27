/**
 * Mock API 服务
 * 用于开发测试，模拟后端 API 响应
 * 可根据需要切换真实 API 和 Mock API
 */

// 模拟用户数据库
const mockUsers = [
  {
    id: 1,
    username: 'admin',
    password: 'admin123',
    nickname: '系统管理员',
    userRole: 'admin',
    createTime: '2025-01-01 10:00:00',
  },
  {
    id: 2,
    username: 'company1',
    password: 'pwd123',
    nickname: '公司管理员',
    userRole: 'companyAdmin',
    companyId: 1,
    createTime: '2025-01-01 11:00:00',
  },
  {
    id: 3,
    username: 'hr1',
    password: 'pwd123',
    nickname: 'HR员工',
    userRole: 'hr',
    companyId: 1,
    createTime: '2025-01-01 12:00:00',
  },
  {
    id: 4,
    username: 'user1',
    password: 'pwd123',
    nickname: '普通用户',
    userRole: 'user',
    companyId: 1,
    createTime: '2025-01-01 13:00:00',
  },
]

/**
 * 模拟 API 响应格式
 */
interface MockResponse<T = any> {
  code: number // 0 = 成功, 非0 = 失败
  message: string
  data?: T
}

/**
 * 模拟登录接口
 */
export async function mockUserLogin(username: string, password: string): Promise<MockResponse> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const user = mockUsers.find((u) => u.username === username)

      if (!user) {
        resolve({
          code: 1,
          message: '用户不存在',
        })
        return
      }

      if (user.password !== password) {
        resolve({
          code: 1,
          message: '密码错误',
        })
        return
      }

      const { password: _, ...userInfo } = user
      resolve({
        code: 0,
        message: '登录成功',
        data: userInfo,
      })
    }, 500) // 模拟网络延迟
  })
}

/**
 * 模拟登出接口
 */
export async function mockUserLogout(): Promise<MockResponse> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 0,
        message: '登出成功',
        data: true,
      })
    }, 300)
  })
}

/**
 * 模拟获取当前登录用户接口
 */
export async function mockGetLoginUser(): Promise<MockResponse> {
  return new Promise((resolve) => {
    setTimeout(() => {
      // 从 localStorage 获取用户信息
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        resolve({
          code: 0,
          message: '获取成功',
          data: JSON.parse(userInfo),
        })
      } else {
        resolve({
          code: 401,
          message: '未登录',
        })
      }
    }, 200)
  })
}

/**
 * 模拟用户列表查询接口
 */
export async function mockListUserVoByPage(params: any): Promise<MockResponse> {
  return new Promise((resolve) => {
    setTimeout(() => {
      let filteredUsers = [...mockUsers]

      // 按用户名搜索
      if (params.username) {
        filteredUsers = filteredUsers.filter((u) =>
          u.username.toLowerCase().includes(params.username.toLowerCase()),
        )
      }

      // 按角色筛选
      if (params.userRole) {
        filteredUsers = filteredUsers.filter((u) => u.userRole === params.userRole)
      }

      // 分页
      const pageNum = params.pageNum || 1
      const pageSize = params.pageSize || 10
      const startIdx = (pageNum - 1) * pageSize
      const endIdx = startIdx + pageSize

      resolve({
        code: 0,
        message: '查询成功',
        data: {
          records: filteredUsers.slice(startIdx, endIdx),
          pageNumber: pageNum,
          pageSize: pageSize,
          totalRow: filteredUsers.length,
          totalPage: Math.ceil(filteredUsers.length / pageSize),
        },
      })
    }, 400)
  })
}

/**
 * 模拟注册新用户接口
 */
export async function mockUserRegister(userData: any): Promise<MockResponse> {
  return new Promise((resolve) => {
    setTimeout(() => {
      // 检查用户名是否存在
      if (mockUsers.some((u) => u.username === userData.username)) {
        resolve({
          code: 1,
          message: '用户名已存在',
        })
        return
      }

      const newUser = {
        id: mockUsers.length + 1,
        username: userData.username,
        password: Math.random().toString(36).slice(-8), // 随机密码
        nickname: userData.nickname,
        userRole: userData.userRole,
        companyId: userData.companyId,
        createTime: new Date().toLocaleString('zh-CN'),
      }

      mockUsers.push(newUser)

      resolve({
        code: 0,
        message: '用户注册成功',
        data: newUser.id,
      })
    }, 300)
  })
}

/**
 * 模拟更新用户接口
 */
export async function mockUpdateUser(userData: any): Promise<MockResponse> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const user = mockUsers.find((u) => u.id === userData.id)

      if (!user) {
        resolve({
          code: 1,
          message: '用户不存在',
        })
        return
      }

      if (userData.nickname) {
        user.nickname = userData.nickname
      }
      if (userData.userRole) {
        user.userRole = userData.userRole
      }

      resolve({
        code: 0,
        message: '用户更新成功',
        data: true,
      })
    }, 300)
  })
}

/**
 * 模拟禁用/启用用户接口
 */
export async function mockToggleUserStatus(id: number): Promise<MockResponse> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const user = mockUsers.find((u) => u.id === id)

      if (!user) {
        resolve({
          code: 1,
          message: '用户不存在',
        })
        return
      }

      // 在实际实现中，应该有一个 status 字段
      resolve({
        code: 0,
        message: '用户状态更新成功',
        data: true,
      })
    }, 300)
  })
}

/**
 * 所有 Mock API 的导出对象
 */
export const mockAPI = {
  userLogin: mockUserLogin,
  userLogout: mockUserLogout,
  getLoginUser: mockGetLoginUser,
  listUserVoByPage: mockListUserVoByPage,
  userRegister: mockUserRegister,
  updateUser: mockUpdateUser,
  toggleUserStatus: mockToggleUserStatus,
}

export default mockAPI
