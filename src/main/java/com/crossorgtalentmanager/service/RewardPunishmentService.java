package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentAddRequest;
import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentQueryRequest;
import com.crossorgtalentmanager.model.dto.rewardpunishment.RewardPunishmentUpdateRequest;
import com.crossorgtalentmanager.model.entity.RewardPunishment;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.vo.RewardPunishmentVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 奖惩记录 服务层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface RewardPunishmentService extends IService<RewardPunishment> {

    /**
     * 新增奖惩记录。
     *
     * @param addRequest 新增请求
     * @param loginUser  操作人
     * @return 奖惩记录 ID
     */
    Long addRewardPunishment(RewardPunishmentAddRequest addRequest, User loginUser);

    /**
     * 更新奖惩记录。
     *
     * @param updateRequest 更新请求
     * @param loginUser     操作人
     * @return 是否成功
     */
    Boolean updateRewardPunishment(RewardPunishmentUpdateRequest updateRequest, User loginUser);

    /**
     * 转换 RewardPunishment 实体为 RewardPunishmentVO。
     *
     * @param rewardPunishment 奖惩记录实体
     * @return VO 对象
     */
    RewardPunishmentVO getRewardPunishmentVO(RewardPunishment rewardPunishment);

    /**
     * 批量转换 RewardPunishment 列表为 RewardPunishmentVO 列表。
     *
     * @param list 奖惩记录列表
     * @return VO 列表
     */
    List<RewardPunishmentVO> getRewardPunishmentVOList(List<RewardPunishment> list);

    /**
     * 根据查询条件构建 QueryWrapper。
     *
     * @param queryRequest 查询请求
     * @return QueryWrapper 对象
     */
    QueryWrapper getQueryWrapper(RewardPunishmentQueryRequest queryRequest);

    /**
     * 逻辑删除奖惩记录。
     *
     * @param id 奖惩记录 ID
     * @return 是否成功
     */
    Boolean removeById(Long id);
}
