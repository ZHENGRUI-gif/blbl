package com.teriteri.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teriteri.backend.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 更新用户关注数（由于user表中没有follows_count字段，这个方法暂时不实现）
     * @param uid 用户ID
     * @param delta 变化量
     */
    void updateFollowCount(@Param("uid") Integer uid, @Param("delta") int delta);
    
    /**
     * 更新用户粉丝数（由于user表中没有fans_count字段，这个方法暂时不实现）
     * @param uid 用户ID
     * @param delta 变化量
     */
    void updateFansCount(@Param("uid") Integer uid, @Param("delta") int delta);
}
