package com.teriteri.backend.mapper;

import com.teriteri.backend.pojo.SystemMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SystemMessageMapper {
    
    /**
     * 获取用户的系统消息列表（分页）
     * @param uid 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 系统消息列表
     */
    @Select("SELECT f.id, f.follower_id as fromUserId, f.following_id as toUserId, " +
            "u.nickname as fromUserNickname, u.avatar as fromUserAvatar, " +
            "CASE WHEN f.is_deleted = 0 THEN 'follow' ELSE 'unfollow' END as messageType, " +
            "CASE WHEN f.is_deleted = 0 THEN CONCAT(u.nickname, ' 关注了你') " +
            "ELSE CONCAT(u.nickname, ' 取消关注了你') END as messageContent, " +
            "f.create_date as createTime, f.delete_date as deleteTime, " +
            "0 as isRead " +
            "FROM follow f " +
            "INNER JOIN user u ON f.follower_id = u.uid " +
            "WHERE f.following_id = #{uid} " +
            "ORDER BY f.create_date DESC, f.delete_date DESC " +
            "LIMIT #{offset}, #{limit}")
    List<SystemMessage> getSystemMessages(@Param("uid") Integer uid, 
                                         @Param("offset") Integer offset, 
                                         @Param("limit") Integer limit);
    
    /**
     * 获取用户系统消息总数
     * @param uid 用户ID
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM follow f " +
            "INNER JOIN user u ON f.follower_id = u.uid " +
            "WHERE f.following_id = #{uid}")
    Integer getSystemMessageCount(@Param("uid") Integer uid);
}
