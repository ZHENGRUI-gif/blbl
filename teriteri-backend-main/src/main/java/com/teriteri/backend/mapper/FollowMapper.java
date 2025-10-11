package com.teriteri.backend.mapper;

import com.teriteri.backend.pojo.Follow;
import com.teriteri.backend.pojo.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {
    
    /**
     * 插入关注关系
     * @param follow 关注关系对象
     * @return 影响行数
     */
    int insert(Follow follow);
    
    /**
     * 根据ID更新关注关系
     * @param follow 关注关系对象
     * @return 影响行数
     */
    int updateById(Follow follow);
    
    /**
     * 根据关注者和被关注者查找关注关系
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 关注关系对象
     */
    Follow findByFollowerAndFollowing(@Param("followerId") Integer followerId, 
                                     @Param("followingId") Integer followingId);
    
    /**
     * 获取用户的关注列表
     * @param uid 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 关注用户列表
     */
    List<UserDTO> getFollowList(@Param("uid") Integer uid, 
                               @Param("offset") Integer offset, 
                               @Param("limit") Integer limit);
    
    /**
     * 获取用户的粉丝列表
     * @param uid 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 粉丝用户列表
     */
    List<UserDTO> getFansList(@Param("uid") Integer uid, 
                             @Param("offset") Integer offset, 
                             @Param("limit") Integer limit);
    
    /**
     * 获取用户关注数量
     * @param uid 用户ID
     * @return 关注数量
     */
    int getFollowCount(@Param("uid") Integer uid);
    
    /**
     * 获取用户粉丝数量
     * @param uid 用户ID
     * @return 粉丝数量
     */
    int getFansCount(@Param("uid") Integer uid);
}
