package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.crossorgtalentmanager.model.entity.EvaluationTag;
import com.crossorgtalentmanager.mapper.EvaluationTagMapper;
import com.crossorgtalentmanager.service.EvaluationTagService;
import com.crossorgtalentmanager.model.vo.EvaluationTagVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价标签库 服务层实现。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@Service
@Slf4j
public class EvaluationTagServiceImpl extends ServiceImpl<EvaluationTagMapper, EvaluationTag>
        implements EvaluationTagService {

    @Resource
    private CacheManager cacheManager;

    @Override
    @Cacheable(value = "evaluationTags", key = "'active'")
    public List<EvaluationTagVO> listActiveTags() {
        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("is_active", true)
                    .eq("is_delete", false)
                    .orderBy("type", true)
                    .orderBy("sort_order", true);
            
            List<EvaluationTag> tags = this.list(queryWrapper);
            return getEvaluationTagVOList(tags);
        } catch (ClassCastException e) {
            // 如果缓存数据格式不对（旧格式），清除缓存并重新查询
            log.warn("缓存数据格式错误，清除缓存并重新查询: {}", e.getMessage());
            if (cacheManager != null) {
                Cache cache = cacheManager.getCache("evaluationTags");
                if (cache != null) {
                    cache.evict("active");
                }
            }
            // 重新查询（不使用缓存）
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("is_active", true)
                    .eq("is_delete", false)
                    .orderBy("type", true)
                    .orderBy("sort_order", true);
            
            List<EvaluationTag> tags = this.list(queryWrapper);
            return getEvaluationTagVOList(tags);
        }
    }

    @Override
    public EvaluationTagVO getEvaluationTagVO(EvaluationTag tag) {
        if (tag == null) {
            return null;
        }
        EvaluationTagVO vo = new EvaluationTagVO();
        BeanUtil.copyProperties(tag, vo);
        return vo;
    }

    @Override
    public List<EvaluationTagVO> getEvaluationTagVOList(List<EvaluationTag> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return new ArrayList<>();
        }
        return tagList.stream()
                .map(this::getEvaluationTagVO)
                .collect(Collectors.toList());
    }
}

