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
    public List<EvaluationTagVO> listActiveTags() {
        // 尝试从缓存获取
        try {
            if (cacheManager != null) {
                Cache cache = cacheManager.getCache("evaluationTags");
                if (cache != null) {
                    Cache.ValueWrapper wrapper = cache.get("active");
                    if (wrapper != null) {
                        Object cached = wrapper.get();
                        if (cached instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<EvaluationTagVO> cachedList = (List<EvaluationTagVO>) cached;
                            // 验证列表中的元素类型
                            if (!cachedList.isEmpty() && cachedList.get(0) instanceof EvaluationTagVO) {
                                return cachedList;
                            }
                        }
                        // 缓存数据格式错误，清除缓存
                        log.warn("缓存数据格式错误，清除缓存并重新查询: evaluationTags");
                        cache.evict("active");
                    }
                }
            }
        } catch (org.springframework.data.redis.serializer.SerializationException e) {
            // 反序列化失败，清除缓存
            log.warn("缓存反序列化失败，清除缓存并重新查询: evaluationTags, error={}", e.getMessage());
            try {
                if (cacheManager != null) {
                    Cache cache = cacheManager.getCache("evaluationTags");
                    if (cache != null) {
                        cache.evict("active");
                    }
                }
            } catch (Exception evictEx) {
                log.warn("清除缓存失败: evaluationTags, error={}", evictEx.getMessage());
            }
        } catch (Exception e) {
            // 捕获所有其他异常
            log.warn("从缓存获取数据时发生异常，将重新查询: evaluationTags, error={}", e.getMessage());
            try {
                if (cacheManager != null) {
                    Cache cache = cacheManager.getCache("evaluationTags");
                    if (cache != null) {
                        cache.evict("active");
                    }
                }
            } catch (Exception evictEx) {
                log.warn("清除缓存失败: evaluationTags, error={}", evictEx.getMessage());
            }
        }

        // 从数据库查询
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("is_active", true)
                .eq("is_delete", false)
                .orderBy("type", true)
                .orderBy("sort_order", true);
        
        List<EvaluationTag> tags = this.list(queryWrapper);
        List<EvaluationTagVO> result = getEvaluationTagVOList(tags);

        // 将结果存入缓存
        try {
            if (cacheManager != null) {
                Cache cache = cacheManager.getCache("evaluationTags");
                if (cache != null) {
                    cache.put("active", result);
                }
            }
        } catch (Exception e) {
            log.warn("缓存数据失败: evaluationTags, error={}", e.getMessage());
        }

        return result;
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

