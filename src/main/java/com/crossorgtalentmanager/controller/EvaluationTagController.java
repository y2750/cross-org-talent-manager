package com.crossorgtalentmanager.controller;

import com.crossorgtalentmanager.common.BaseResponse;
import com.crossorgtalentmanager.common.ResultUtils;
import com.crossorgtalentmanager.model.vo.EvaluationTagVO;
import com.crossorgtalentmanager.service.EvaluationTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 评价标签库 控制器
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
@RestController
@RequestMapping("/evaluationTag")
@Slf4j
public class EvaluationTagController {

    @Resource
    private EvaluationTagService evaluationTagService;

    /**
     * 获取所有启用的评价标签列表
     */
    @GetMapping("/list/active")
    public BaseResponse<List<EvaluationTagVO>> listActiveTags() {
        List<EvaluationTagVO> tags = evaluationTagService.listActiveTags();
        return ResultUtils.success(tags);
    }
}
