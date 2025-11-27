declare namespace API {
  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseCompany = {
    code?: number
    data?: Company
    message?: string
  }

  type BaseResponseCompanyVO = {
    code?: number
    data?: CompanyVO
    message?: string
  }

  type BaseResponseDepartment = {
    code?: number
    data?: Department
    message?: string
  }

  type BaseResponseDepartmentVO = {
    code?: number
    data?: DepartmentVO
    message?: string
  }

  type BaseResponseEmployee = {
    code?: number
    data?: Employee
    message?: string
  }

  type BaseResponseEmployeeProfile = {
    code?: number
    data?: EmployeeProfile
    message?: string
  }

  type BaseResponseEmployeeProfileVO = {
    code?: number
    data?: EmployeeProfileVO
    message?: string
  }

  type BaseResponseEmployeeVO = {
    code?: number
    data?: EmployeeVO
    message?: string
  }

  type BaseResponseListEmployeeVO = {
    code?: number
    data?: EmployeeVO[]
    message?: string
  }

  type BaseResponseListRewardPunishment = {
    code?: number
    data?: RewardPunishment[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseInteger = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageCompanyVO = {
    code?: number
    data?: PageCompanyVO
    message?: string
  }

  type BaseResponsePageDepartmentVO = {
    code?: number
    data?: PageDepartmentVO
    message?: string
  }

  type BaseResponsePageEmployeeProfileVO = {
    code?: number
    data?: PageEmployeeProfileVO
    message?: string
  }

  type BaseResponsePageEmployeeVO = {
    code?: number
    data?: PageEmployeeVO
    message?: string
  }

  type BaseResponsePageRewardPunishmentVO = {
    code?: number
    data?: PageRewardPunishmentVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type Company = {
    id?: number
    name?: string
    contactPersonId?: number
    phone?: string
    email?: string
    industryCategory?: string
    industry?: string
    createTime?: string
    updateTime?: string
    isDelete?: boolean
  }

  type CompanyAddRequest = {
    name?: string
    contactPersonId?: number
    phone?: string
    email?: string
    industryCategory?: string
    industries?: string[]
  }

  type CompanyUpdateRequest = {
    id?: number
    name?: string
    contactPersonId?: number
    phone?: string
    email?: string
    industryCategory?: string
    industries?: string[]
  }

  type CompanyQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    name?: string
    contactPersonName?: string
    industryCategory?: string
    industries?: string[]
  }

  type CompanyVO = {
    id?: number
    name?: string
    contactPersonId?: number
    contactPersonName?: string
    phone?: string
    email?: string
    industryCategory?: string
    industries?: string[]
    createTime?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type Department = {
    id?: number
    name?: string
    companyId?: number
    leaderId?: number
    createTime?: string
    updateTime?: string
    isDelete?: boolean
  }

  type DepartmentAddRequest = {
    name?: string
  }

  type DepartmentQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    name?: string
    companyId?: number
    leaderId?: number
  }

  type DepartmentSupervisorAddRequest = {
    departmentId?: number
    employeeId?: number
    isSupervisor?: boolean
  }

  type DepartmentVO = {
    id?: number
    name?: string
    companyId?: number
    companyName?: string
    leaderId?: number
    leaderName?: string
  }

  type Employee = {
    id?: number
    name?: string
    gender?: string
    phone?: string
    email?: string
    idCardNumber?: string
    companyId?: number
    departmentId?: number
    status?: boolean
    photoUrl?: string
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: boolean
  }

  type EmployeeCreateRequest = {
    name?: string
    gender?: string
    phone?: string
    email?: string
    idCardNumber?: string
    departmentId?: number
    companyId?: number
  }

  type EmployeeProfile = {
    id?: number
    employeeId?: number
    companyId?: number
    startDate?: string
    endDate?: string
    performanceSummary?: string
    attendanceRate?: number
    hasMajorIncident?: boolean
    reasonForLeaving?: string
    occupation?: string
    annualSalary?: number
    createTime?: string
    updateTime?: string
    isDelete?: boolean
    operatorId?: number
  }

  type EmployeeProfileAddRequest = {
    employeeId?: number
    companyId?: number
    startDate?: string
    endDate?: string
    performanceSummary?: string
    attendanceRate?: number
    hasMajorIncident?: boolean
    reasonForLeaving?: string
    occupation?: string
    annualSalary?: number
  }

  type EmployeeProfileQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    employeeId?: number
    companyId?: number
    companyName?: string
    startDate?: string
    endDate?: string
    occupation?: string
    minAnnualSalary?: number
    maxAnnualSalary?: number
  }

  type EmployeeProfileUpdateRequest = {
    id?: number
    startDate?: string
    endDate?: string
    performanceSummary?: string
    attendanceRate?: number
    hasMajorIncident?: boolean
    reasonForLeaving?: string
    occupation?: string
    annualSalary?: number
  }

  type EmployeeProfileVO = {
    id?: number
    employeeId?: number
    companyId?: number
    companyName?: string
    startDate?: string
    endDate?: string
    performanceSummary?: string
    attendanceRate?: number
    hasMajorIncident?: boolean
    reasonForLeaving?: string
    occupation?: string
    annualSalary?: number
    createTime?: string
    updateTime?: string
    isDelete?: boolean
    operatorId?: number
    rewardPunishments?: RewardPunishmentVO[] // 该公司的奖惩记录列表（仅用于员工查看自己的档案时）
  }

  type EmployeeQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    name?: string
    phone?: string
    idCardNumber?: string
    companyId?: number
    departmentId?: number
    status?: boolean
  }

  type EmployeeVO = {
    id?: number
    name?: string
    gender?: string
    phone?: string
    email?: string
    idCardNumber?: string
    companyId?: number
    companyName?: string
    departmentId?: number
    departmentName?: string
    photoUrl?: string
  }

  type getCompanyByIdParams = {
    id: number
  }

  type getCompanyVOByIdParams = {
    id: number
  }

  type getDepartmentByIdParams = {
    id: number
  }

  type getDepartmentVOByIdParams = {
    id: number
  }

  type getEmployeeByIdParams = {
    id: number
  }

  type getEmployeeProfileByIdParams = {
    id: number
  }

  type getEmployeeProfileVOByIdParams = {
    id: number
  }

  type getEmployeeVOByIdParams = {
    id: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type LoginUserVO = {
    id?: number
    username?: string
    nickname?: string
    companyId?: number
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type PageCompanyVO = {
    records?: CompanyVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageDepartmentVO = {
    records?: DepartmentVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageEmployeeProfileVO = {
    records?: EmployeeProfileVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageEmployeeVO = {
    records?: EmployeeVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageRewardPunishmentVO = {
    records?: RewardPunishmentVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type queryByEmployeeIdParams = {
    employeeId: number
  }

  type RewardPunishment = {
    id?: number
    employeeId?: number
    type?: number
    description?: string
    amount?: number
    operatorId?: number
    date?: string
    createTime?: string
    updateTime?: string
    isDelete?: boolean
  }

  type RewardPunishmentAddRequest = {
    employeeId?: number
    type?: number
    description?: string
    amount?: number
    date?: string
  }

  type RewardPunishmentQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    companyId?: number
    employeeId?: number
    employeeName?: string
    idCardNumber?: string
    departmentId?: number
    gender?: string
  }

  type RewardPunishmentUpdateRequest = {
    id?: number
    type?: number
    description?: string
    amount?: number
    date?: string
  }

  type RewardPunishmentVO = {
    id?: number
    profileId?: number
    employeeId?: number
    employeeName?: string
    idCardNumber?: string
    departmentId?: number
    departmentName?: string
    gender?: string
    companyId?: number
    type?: number
    description?: string
    amount?: number
    date?: string
    operatorId?: number
    operatorName?: string
    createTime?: string
    updateTime?: string
  }

  type updateEmployeeParams = {
    id: number
    departmentId?: number
    phone?: string
    email?: string
  }

  type updateMyProfileParams = {
    phone?: string
    email?: string
  }

  type User = {
    id?: number
    username?: string
    password?: string
    userRole?: string
    nickname?: string
    companyId?: number
    createTime?: string
    updateTime?: string
    isDelete?: boolean
  }

  type UserLoginRequest = {
    username?: string
    password?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    nickname?: string
    username?: string
    companyId?: number
    userRole?: string
  }

  type UserRegisterRequest = {
    username?: string
    userRole?: string
    nickname?: string
    password?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    nickname?: string
    companyId?: number
    userRole?: string
  }

  type UserVO = {
    id?: number
    username?: string
    nickname?: string
    companyId?: number
    userRole?: string
    isDelete?: boolean
    createTime?: string
  }
}
