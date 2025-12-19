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

  type EmployeeBatchImportResult = {
    totalRows?: number
    successCount?: number
    failCount?: number
    warningCount?: number
    successItems?: ImportSuccessItem[]
    failItems?: ImportFailItem[]
    warningItems?: ImportWarningItem[]
  }

  type ImportSuccessItem = {
    rowNumber?: number
    name?: string
    idCardNumber?: string
    operationType?: string
  }

  type ImportFailItem = {
    rowNumber?: number
    name?: string
    idCardNumber?: string
    reason?: string
  }

  type ImportWarningItem = {
    rowNumber?: number
    name?: string
    idCardNumber?: string
    existingName?: string
    message?: string
  }

  type BaseResponseEmployeeBatchImportResult = {
    code?: number
    data?: EmployeeBatchImportResult
    message?: string
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

  // 投诉相关类型
  type ComplaintAddRequest = {
    evaluationId?: string
    type?: number
    title?: string
    content?: string
    evidenceImages?: string[]
  }

  type ComplaintQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    complainantId?: number
    evaluationId?: number
    companyId?: number
    type?: number
    status?: number
    handlerId?: number
  }

  type ComplaintHandleRequest = {
    id?: string
    status?: number
    handleResult?: string
  }

  type ComplaintVO = {
    id?: number
    complainantId?: number
    complainantName?: string
    evaluationId?: number
    companyId?: number
    companyName?: string
    type?: number
    typeText?: string
    title?: string
    content?: string
    evidenceImages?: string[]
    status?: number
    statusText?: string
    handlerId?: number
    handlerName?: string
    handleResult?: string
    handleTime?: string
    createTime?: string
    updateTime?: string
  }

  type ComplaintDetailVO = {
    id?: number
    complainantId?: number
    complainantName?: string
    evaluationId?: number
    companyId?: number
    companyName?: string
    type?: number
    typeText?: string
    title?: string
    content?: string
    evidenceImages?: string[]
    status?: number
    statusText?: string
    handlerId?: number
    handlerName?: string
    handleResult?: string
    handleTime?: string
    createTime?: string
    updateTime?: string
    evaluation?: EvaluationDetailVO
  }

  type PageComplaintVO = {
    records?: ComplaintVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  type BaseResponseComplaintDetailVO = {
    code?: number
    data?: ComplaintDetailVO
    message?: string
  }

  type BaseResponsePageComplaintVO = {
    code?: number
    data?: PageComplaintVO
    message?: string
  }

  // 企业注册申请相关类型
  type CompanyRegistrationAddRequest = {
    companyName?: string
    address?: string
    companyEmail?: string
    adminName?: string
    adminPhone?: string
    adminEmail?: string
    adminIdNumber?: string
    industryCategory?: string
    industries?: string[]
    proofImages?: string[]
  }

  type CompanyRegistrationApproveRequest = {
    id?: number
    approved?: boolean
    rejectReason?: string
  }

  type CompanyRegistrationQueryRequest = {
    id?: number
    companyName?: string
    status?: number
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
  }

  type CompanyRegistrationRequestVO = {
    id?: number
    companyName?: string
    address?: string
    companyEmail?: string
    adminName?: string
    adminPhone?: string
    adminEmail?: string
    adminIdNumber?: string
    industryCategory?: string
    industries?: string[]
    proofImages?: string[]
    status?: number
    statusText?: string
    rejectReason?: string
    createTime?: string
    updateTime?: string
  }

  type PageCompanyRegistrationRequestVO = {
    records?: CompanyRegistrationRequestVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  type BaseResponsePageCompanyRegistrationRequestVO = {
    code?: number
    data?: PageCompanyRegistrationRequestVO
    message?: string
  }

  type BaseResponseListString = {
    code?: number
    data?: string[]
    message?: string
  }

  // 通知相关类型
  type NotificationQueryRequest = {
    id?: number
    userId?: number
    type?: number
    status?: number
    relatedId?: number
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
  }

  type NotificationUpdateRequest = {
    id?: number
    status?: number
  }

  type NotificationVO = {
    id?: number
    userId?: number
    type?: number
    typeText?: string
    title?: string
    content?: string
    relatedId?: number
    status?: number
    statusText?: string
    createTime?: string
    updateTime?: string
    deadline?: string
  }

  type PageNotificationVO = {
    records?: NotificationVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  type BaseResponsePageNotificationVO = {
    code?: number
    data?: PageNotificationVO
    message?: string
  }

  type BaseResponseNotificationVO = {
    code?: number
    data?: NotificationVO
    message?: string
  }

  type NotificationListItemVO = {
    id?: number
    title?: string
    status?: number
    statusText?: string
    createTime?: string
  }

  type PageNotificationListItemVO = {
    records?: NotificationListItemVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  type BaseResponsePageNotificationListItemVO = {
    code?: number
    data?: PageNotificationListItemVO
    message?: string
  }

  type NotificationSendRequest = {
    title?: string
    content?: string
    sendType?: number
    username?: string
    userRole?: string
  }

  type BaseResponseInteger = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseInt = {
    code?: number
    data?: number
    message?: string
  }

  // 人才市场相关类型
  type TalentSearchRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    keyword?: string
    occupation?: string
    occupations?: string[]
    minAverageScore?: number
    maxAverageScore?: number
    includeTagIds?: number[]
    excludeTagIds?: number[]
    evaluationKeyword?: string
    excludeReasonKeywords?: string[]
    gender?: string
    onlyLeft?: boolean
    onlyWorking?: boolean
    excludeMajorIncident?: boolean
    minAttendanceRate?: number
  }

  type TalentVO = {
    id?: number
    name?: string
    gender?: string
    phone?: string
    email?: string
    photoUrl?: string
    status?: boolean
    currentCompanyId?: number
    currentCompanyName?: string
    latestOccupation?: string
    averageScore?: number
    evaluationCount?: number
    profileCount?: number
    positiveTags?: TalentTagStatVO[]
    neutralTags?: TalentTagStatVO[]
    bookmarked?: boolean
  }

  type TalentTagStatVO = {
    tagId?: number
    tagName?: string
    count?: number
  }

  type TalentDetailVO = {
    id?: number
    name?: string
    gender?: string
    phone?: string
    email?: string
    photoUrl?: string
    status?: boolean
    currentCompanyId?: number
    currentCompanyName?: string
    latestOccupation?: string
    averageScore?: number
    evaluationCount?: number
    profileCount?: number
    positiveTags?: TalentTagStatVO[]
    neutralTags?: TalentTagStatVO[]
    dimensionScores?: Record<string, number>
    profiles?: TalentProfileVO[]
    evaluations?: TalentEvaluationVO[]
    freeEvaluationCount?: number
    unlockedEvaluationCount?: number
    lockedEvaluationCount?: number
    bookmarked?: boolean
    companyPoints?: number
    isOwnCompany?: boolean
  }

  type TalentProfileVO = {
    profileId?: number
    companyId?: number
    companyName?: string
    startDate?: string
    endDate?: string
    occupation?: string
    attendanceRate?: number
    hasMajorIncident?: boolean
    reasonForLeaving?: string
    performanceSummary?: string
    annualSalary?: number
  }

  type TalentEvaluationVO = {
    id?: number
    companyId?: number
    companyName?: string
    evaluatorId?: number
    evaluatorName?: string
    evaluationType?: number
    evaluationTypeName?: string
    evaluationDate?: string
    comment?: string
    averageScore?: number
    dimensionScores?: Record<string, number>
    tags?: EvaluationTagVO[]
    unlocked?: boolean
    unlockCost?: number
  }

  type PageTalentVO = {
    records?: TalentVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  type BaseResponsePageTalentVO = {
    code?: number
    data?: PageTalentVO
    message?: string
  }

  type BaseResponseTalentDetailVO = {
    code?: number
    data?: TalentDetailVO
    message?: string
  }

  type EvaluationUnlockRequest = {
    employeeId?: string
    evaluationId?: string
    evaluationIds?: string[]
  }

  type BaseResponseBigDecimal = {
    code?: number
    data?: number
    message?: string
  }

  type TalentBookmarkRequest = {
    employeeId?: string
    remark?: string
  }

  type UnlockPriceConfigVO = {
    id?: number
    evaluationType?: number
    evaluationTypeName?: string
    pointsCost?: number
    description?: string
    isActive?: boolean
  }

  type BaseResponseListUnlockPriceConfigVO = {
    code?: number
    data?: UnlockPriceConfigVO[]
    message?: string
  }

  type TalentViewLogRequest = {
    employeeId?: string
    viewSource?: string
    searchKeyword?: string
  }

  type TalentViewLogVO = {
    id?: number
    companyId?: number
    employeeId?: number
    employeeName?: string
    employeePhotoUrl?: string
    latestOccupation?: string
    viewTime?: string
    viewSource?: string
    searchKeyword?: string
    viewDuration?: number
  }

  type PageTalentViewLogVO = {
    records?: TalentViewLogVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  type BaseResponsePageTalentViewLogVO = {
    code?: number
    data?: PageTalentViewLogVO
    message?: string
  }

  type ViewStatisticsVO = {
    totalViews?: number
    todayViews?: number
    weekViews?: number
    monthViews?: number
    uniqueTalentCount?: number
    topViewedOccupations?: Record<string, number>
    topViewedTags?: Record<string, number>
  }

  type BaseResponseViewStatisticsVO = {
    code?: number
    data?: ViewStatisticsVO
    message?: string
  }

  type CompanyPreferenceRequest = {
    preferredOccupations?: string[]
    preferredTagIds?: number[]
    excludedTagIds?: number[]
    minScore?: number
    excludeMajorIncident?: boolean
    minAttendanceRate?: number
    requirementDescription?: string
  }

  type CompanyPreferenceVO = {
    id?: number
    companyId?: number
    preferredOccupations?: string[]
    preferredTags?: EvaluationTagVO[]
    excludedTags?: EvaluationTagVO[]
    minScore?: number
    excludeMajorIncident?: boolean
    minAttendanceRate?: number
    requirementDescription?: string
  }

  type BaseResponseCompanyPreferenceVO = {
    code?: number
    data?: CompanyPreferenceVO
    message?: string
  }

  type TalentRecommendVO = {
    id?: number
    name?: string
    gender?: string
    phone?: string
    email?: string
    photoUrl?: string
    status?: boolean
    currentCompanyId?: number
    currentCompanyName?: string
    latestOccupation?: string
    averageScore?: number
    evaluationCount?: number
    profileCount?: number
    positiveTags?: TalentTagStatVO[]
    neutralTags?: TalentTagStatVO[]
    bookmarked?: boolean
    recommendScore?: number
    recommendReasons?: string[]
    matchPercentage?: number
    matchedOccupations?: string[]
    matchedTags?: EvaluationTagVO[]
  }

  type PageTalentRecommendVO = {
    records?: TalentRecommendVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
    totalPage?: number
  }

  type BaseResponsePageTalentRecommendVO = {
    code?: number
    data?: PageTalentRecommendVO
    message?: string
  }

  type BaseResponseListTalentRecommendVO = {
    code?: number
    data?: TalentRecommendVO[]
    message?: string
  }

  type TalentCompareRequest = {
    employeeIds?: string[]
  }

  type TalentCompareVO = {
    talents?: TalentDetailVO[]
    dimensions?: string[]
    comparisonSummary?: Record<string, any>
  }

  type BaseResponseTalentCompareVO = {
    code?: number
    data?: TalentCompareVO
    message?: string
  }
}
