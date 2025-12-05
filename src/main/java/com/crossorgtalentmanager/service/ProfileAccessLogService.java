package com.crossorgtalentmanager.service;

import com.mybatisflex.core.service.IService;
import com.crossorgtalentmanager.model.entity.ProfileAccessLog;

/**
 * 档案查阅记录 服务层
 */
public interface ProfileAccessLogService extends IService<ProfileAccessLog> {

    /**
     * 记录档案查阅日志
     */
    void logAccess(Long requestId, Long accessCompanyId, Long employeeId, Long employeeProfileId, String ipAddress);
}

