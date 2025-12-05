package com.crossorgtalentmanager.service.impl;

import com.crossorgtalentmanager.mapper.ProfileAccessLogMapper;
import com.crossorgtalentmanager.model.entity.ProfileAccessLog;
import com.crossorgtalentmanager.service.ProfileAccessLogService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 档案查阅记录 服务层实现
 */
@Service
@Slf4j
public class ProfileAccessLogServiceImpl extends ServiceImpl<ProfileAccessLogMapper, ProfileAccessLog>
        implements ProfileAccessLogService {

    @Override
    public void logAccess(Long requestId, Long accessCompanyId, Long employeeId, Long employeeProfileId,
            String ipAddress) {
        ProfileAccessLog log = new ProfileAccessLog();
        log.setRequestId(requestId);
        log.setAccessCompanyId(accessCompanyId);
        log.setEmployeeId(employeeId);
        log.setEmployeeProfileId(employeeProfileId);
        log.setAccessTime(LocalDateTime.now());
        log.setIpAddress(ipAddress);
        this.save(log);
    }
}

