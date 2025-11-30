package com.crossorgtalentmanager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.crossorgtalentmanager.model.entity.EvaluationTag;
import com.crossorgtalentmanager.mapper.EvaluationTagMapper;
import com.crossorgtalentmanager.service.EvaluationTagService;
import com.crossorgtalentmanager.model.vo.EvaluationTagVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Override
    public List<EvaluationTagVO> listActiveTags() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("is_active", true)
                .eq("is_delete", false)
                .orderBy("type", true)
                .orderBy("sort_order", true);
        
        List<EvaluationTag> tags = this.list(queryWrapper);
        return getEvaluationTagVOList(tags);
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

