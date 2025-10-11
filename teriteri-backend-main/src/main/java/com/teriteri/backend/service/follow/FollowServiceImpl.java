package com.teriteri.backend.service.follow;

import com.teriteri.backend.mapper.FollowMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Follow;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.pojo.dto.UserDTO;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.message.MsgUnreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FollowServiceImpl implements FollowService {
    
    @Autowired
    private FollowMapper followMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MsgUnreadService msgUnreadService;
    
    @Override
    @Transactional
    public CustomResponse toggleFollow(Integer followerId, Integer followingId) {
        CustomResponse response = new CustomResponse();
        
        // 不能关注自己
        if (followerId.equals(followingId)) {
            response.setCode(400);
            response.setMessage("不能关注自己");
            return response;
        }
        
        // 检查被关注用户是否存在
        UserDTO followingUser = userService.getUserById(followingId);
        if (followingUser == null) {
            response.setCode(404);
            response.setMessage("用户不存在");
            return response;
        }
        
        // 获取关注者信息用于通知
        UserDTO followerUser = userService.getUserById(followerId);
        
        // 检查是否已有关注关系
        Follow existingFollow = followMapper.findByFollowerAndFollowing(followerId, followingId);
        
        if (existingFollow == null) {
            // 创建新的关注关系
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFollowingId(followingId);
            follow.setCreateDate(LocalDateTime.now());
            follow.setIsDeleted(0);
            
            followMapper.insert(follow);
            
            // 更新用户关注数和粉丝数
            userService.updateFollowCount(followerId, 1);
            userService.updateFansCount(followingId, 1);
            
            // 发送系统通知给被关注者
            msgUnreadService.addOneUnread(followingId, "system");
            
            response.setCode(200);
            response.setMessage("关注成功");
            response.setData(true); // true表示已关注
        } else {
            // 取消关注
            if (existingFollow.getIsDeleted() == 0) {
                // 当前是关注状态，取消关注
                existingFollow.setIsDeleted(1);
                existingFollow.setDeleteDate(LocalDateTime.now());
                followMapper.updateById(existingFollow);
                
                // 更新用户关注数和粉丝数
                userService.updateFollowCount(followerId, -1);
                userService.updateFansCount(followingId, -1);
                
                // 发送系统通知给被关注者
                msgUnreadService.addOneUnread(followingId, "system");
                
                response.setCode(200);
                response.setMessage("取消关注成功");
                response.setData(false); // false表示未关注
            } else {
                // 当前是取消关注状态，重新关注
                existingFollow.setIsDeleted(0);
                existingFollow.setDeleteDate(null);
                followMapper.updateById(existingFollow);
                
                // 更新用户关注数和粉丝数
                userService.updateFollowCount(followerId, 1);
                userService.updateFansCount(followingId, 1);
                
                // 发送系统通知给被关注者
                msgUnreadService.addOneUnread(followingId, "system");
                
                response.setCode(200);
                response.setMessage("关注成功");
                response.setData(true); // true表示已关注
            }
        }
        
        return response;
    }
    
    @Override
    public CustomResponse checkFollowStatus(Integer followerId, Integer followingId) {
        CustomResponse response = new CustomResponse();
        
        Follow follow = followMapper.findByFollowerAndFollowing(followerId, followingId);
        boolean isFollowing = follow != null && follow.getIsDeleted() == 0;
        
        response.setCode(200);
        response.setMessage("查询成功");
        response.setData(isFollowing);
        
        return response;
    }
    
    @Override
    public CustomResponse getFollowList(Integer uid, Integer page, Integer pageSize) {
        CustomResponse response = new CustomResponse();
        
        int offset = (page - 1) * pageSize;
        List<UserDTO> followList = followMapper.getFollowList(uid, offset, pageSize);
        int total = followMapper.getFollowCount(uid);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", followList);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        
        response.setCode(200);
        response.setMessage("获取关注列表成功");
        response.setData(data);
        
        return response;
    }
    
    @Override
    public CustomResponse getFansList(Integer uid, Integer page, Integer pageSize) {
        CustomResponse response = new CustomResponse();
        
        int offset = (page - 1) * pageSize;
        List<UserDTO> fansList = followMapper.getFansList(uid, offset, pageSize);
        int total = followMapper.getFansCount(uid);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", fansList);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        
        response.setCode(200);
        response.setMessage("获取粉丝列表成功");
        response.setData(data);
        
        return response;
    }
}
