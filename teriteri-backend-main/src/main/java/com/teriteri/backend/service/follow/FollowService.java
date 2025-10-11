package com.teriteri.backend.service.follow;

import com.teriteri.backend.pojo.CustomResponse;

public interface FollowService {
    
    /**
     * 关注或取消关注用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 操作结果
     */
    CustomResponse toggleFollow(Integer followerId, Integer followingId);
    
    /**
     * 检查关注状态
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 关注状态
     */
    CustomResponse checkFollowStatus(Integer followerId, Integer followingId);
    
    /**
     * 获取关注列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 关注列表
     */
    CustomResponse getFollowList(Integer uid, Integer page, Integer pageSize);
    
    /**
     * 获取粉丝列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 粉丝列表
     */
    CustomResponse getFansList(Integer uid, Integer page, Integer pageSize);
}
