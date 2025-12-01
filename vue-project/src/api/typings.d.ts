declare namespace API {
  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseInteger = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageEvaluationTaskVO = {
    code?: number
    data?: PageEvaluationTaskVO
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

  type BaseResponseNumber = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageCompanyPointsVO = {
    code?: number
    data?: PageCompanyPointsVO
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

  type BaseResponseListEvaluationTagVO = {
    code?: number
    data?: EvaluationTagVO[]
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
    totalPoints?: number
    createTime?: string
  }

  type CompanyPointsVO = {
    id?: number
    companyId?: number
    points?: number
    changeReason?: number
    changeReasonText?: string
    withEmployeeId?: number
    withEmployeeName?: string
    changeDescription?: string
    changeDate?: string
    createTime?: string
  }

  type PageCompanyPointsVO = {
    records?: CompanyPointsVO[]
    totalRow?: number
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

  type getCompanyPointsParams = {
    companyId?: number
  }

  type getCompanyPointsHistoryParams = {
    pageNum?: number
    pageSize?: number
    companyId?: number
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

  // 评价相关类型
  type BaseResponseEvaluationDetailVO = {
    code?: number
    data?: EvaluationDetailVO
    message?: string
  }

  type BaseResponseEvaluationStatisticsVO = {
    code?: number
    data?: EvaluationStatisticsVO
    message?: string
  }

  type BaseResponseEvaluationVO = {
    code?: number
    data?: EvaluationVO
    message?: string
  }

  type BaseResponseListLong = {
    code?: number
    data?: number[]
    message?: string
  }

  type BaseResponsePageEvaluationVO = {
    code?: number
    data?: PageEvaluationVO
    message?: string
  }

  type DimensionScoreRequest = {
    dimensionId?: number
    score?: number
  }

  type EvaluationAddRequest = {
    employeeId?: number
    comment?: string
    evaluationDate?: string
    evaluationType?: number
    evaluationPeriod?: number
    periodYear?: number
    periodQuarter?: number
    dimensionScores?: DimensionScoreRequest[]
    tagIds?: number[]
  }

  type EvaluationColleagueRequest = {
    employeeId?: number
    comment?: string
    dimensionScores?: DimensionScoreRequest[]
    tagIds?: number[]
  }

  type EvaluationDetailVO = {
    id?: number
    employeeId?: number
    employeeName?: string
    evaluatorId?: number
    evaluatorName?: string
    comment?: string
    evaluationDate?: string
    evaluationType?: number
    evaluationTypeText?: string
    evaluationPeriod?: number
    evaluationPeriodText?: string
    periodYear?: number
    periodQuarter?: number
    dimensionScores?: DimensionScoreVO[]
    tags?: TagVO[]
    averageDimensionScore?: number
  }

  type EvaluationDimensionScore = {
    id?: number
    evaluationId?: number
    dimensionId?: number
    score?: number
    createTime?: string
  }

  type EvaluationQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    employeeId?: number
    evaluatorId?: number
    evaluationType?: number
    evaluationPeriod?: number
    periodYear?: number
    periodQuarter?: number
  }

  type EvaluationStatisticsVO = {
    employeeId?: number
    employeeName?: string
    totalCount?: number
    countByType?: Record<string, number>
    countByPeriod?: Record<string, number>
    averageDimensionScores?: Record<string, number>
    quarterlyTrends?: QuarterlyTrend[]
  }

  type EvaluationUpdateRequest = {
    id?: number
    comment?: string
    dimensionScores?: DimensionScoreRequest[]
    tagIds?: number[]
  }

  type EvaluationTagVO = {
    id?: number
    name?: string
    type?: number
    description?: string
    sortOrder?: number
    isActive?: boolean
  }

  type EvaluationVO = {
    id?: number
    employeeId?: number
    companyId?: number
    companyName?: string
    employeeName?: string
    evaluatorId?: number
    evaluatorName?: string
    evaluatorCompanyId?: number
    evaluatorCompanyName?: string
    comment?: string
    evaluationDate?: string
    evaluationType?: number
    evaluationTypeText?: string
    evaluationPeriod?: number
    evaluationPeriodText?: string
    periodYear?: number
    periodQuarter?: number
    createTime?: string
    updateTime?: string
    dimensionScores?: DimensionScoreVO[]
    tags?: TagVO[]
  }

  type EvaluationDimensionScoreVO = {
    dimensionId?: number
    dimensionName?: string
    averageScore?: number
  }

  type EvaluationTagStatisticsVO = {
    tagId?: number
    tagName?: string
    tagType?: number
    count?: number
  }

  type BaseResponseListEvaluationDimensionScoreVO = {
    code?: number
    data?: EvaluationDimensionScoreVO[]
    message?: string
  }

  type DimensionScoreVO = {
    dimensionId?: number
    dimensionName?: string
    score?: number
  }

  type TagVO = {
    tagId?: number
    tagName?: string
    tagType?: number
  }

  type PageEvaluationVO = {
    records?: EvaluationVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  // 评价任务相关类型
  type EvaluationTaskCreateRequest = {
    employeeIds?: number[]
    employeeId?: number
    evaluationType?: number
    evaluationPeriod?: number
    periodYear?: number
    periodQuarter?: number
    deadline?: string
  }

  type EvaluationTaskQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    employeeId?: number
    evaluatorId?: number
    evaluationType?: number
    evaluationPeriod?: number
    status?: number
    periodYear?: number
    periodQuarter?: number
  }

  type EvaluationTaskVO = {
    id?: number
    employeeId?: number
    employeeName?: string
    departmentName?: string
    isLeader?: boolean
    evaluatorId?: number
    evaluatorName?: string
    evaluationType?: number
    evaluationTypeText?: string
    evaluationPeriod?: number
    evaluationPeriodText?: string
    periodYear?: number
    periodQuarter?: number
    status?: number
    statusText?: string
    deadline?: string
    evaluationId?: number
    createTime?: string
  }

  type PageEvaluationTaskVO = {
    records?: EvaluationTaskVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  type QuarterlyTrend = {
    year?: number
    quarter?: number
    averageScore?: number
  }
}
