package com.teriteri.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teriteri.backend.pojo.Follow;
import com.teriteri.backend.pojo.dto.UserDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    
    /**
     * 根据关注者和被关注者查找关注关系
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 关注关系对象
     */
    @Select("SELECT id, follower_id, following_id, create_date, is_deleted, delete_date " +
            "FROM follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    Follow findByFollowerAndFollowing(@Param("followerId") Integer followerId, 
                                     @Param("followingId") Integer followingId);
    
    /**
     * 获取用户的关注列表
     * @param uid 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 关注用户列表
     */
    @Select("SELECT u.uid, u.username, u.nickname, u.avatar as avatar_url, u.background as bg_url, " +
            "u.gender, u.description, u.exp, u.coin, u.vip, u.state, u.auth, u.auth_msg as authMsg, " +
            "u.create_date, f.create_date as follow_date " +
            "FROM follow f " +
            "INNER JOIN user u ON f.following_id = u.uid " +
            "WHERE f.follower_id = #{uid} AND f.is_deleted = 0 AND u.state != 2 " +
            "ORDER BY f.create_date DESC " +
            "LIMIT #{offset}, #{limit}")
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
    @Select("SELECT u.uid, u.username, u.nickname, u.avatar as avatar_url, u.background as bg_url, " +
            "u.gender, u.description, u.exp, u.coin, u.vip, u.state, u.auth, u.auth_msg as authMsg, " +
            "u.create_date, f.create_date as follow_date " +
            "FROM follow f " +
            "INNER JOIN user u ON f.follower_id = u.uid " +
            "WHERE f.following_id = #{uid} AND f.is_deleted = 0 AND u.state != 2 " +
            "ORDER BY f.create_date DESC " +
            "LIMIT #{offset}, #{limit}")
    List<UserDTO> getFansList(@Param("uid") Integer uid, 
                             @Param("offset") Integer offset, 
                             @Param("limit") Integer limit);
    
    /**
     * 获取用户关注数量
     * @param uid 用户ID
     * @return 关注数量
     */
    @Select("SELECT COUNT(*) FROM follow f " +
            "INNER JOIN user u ON f.following_id = u.uid " +
            "WHERE f.follower_id = #{uid} AND f.is_deleted = 0 AND u.state != 2")
    int getFollowCount(@Param("uid") Integer uid);
    
    /**
     * 获取用户粉丝数量
     * @param uid 用户ID
     * @return 粉丝数量
     */
    @Select("SELECT COUNT(*) FROM follow f " +
            "INNER JOIN user u ON f.follower_id = u.uid " +
            "WHERE f.following_id = #{uid} AND f.is_deleted = 0 AND u.state != 2")
    int getFansCount(@Param("uid") Integer uid);
}
