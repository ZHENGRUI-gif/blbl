package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.follow.FollowService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class FollowController {
    
    @Autowired
    private FollowService followService;
    
    @Autowired
    private CurrentUser currentUser;
    
    /**
     * 关注或取消关注用户
     * @param followingId 被关注的用户ID
     * @return 操作结果
     */
    @PostMapping("/follow/toggle")
    public CustomResponse toggleFollow(@RequestParam("followingId") Integer followingId) {
        Integer followerId = currentUser.getUserId();
        try {
            return followService.toggleFollow(followerId, followingId);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("操作失败，请稍后重试");
            return customResponse;
        }
    }
    
    /**
     * 检查是否已关注某用户
     * @param followingId 被关注的用户ID
     * @return 关注状态
     */
    @GetMapping("/follow/check")
    public CustomResponse checkFollowStatus(@RequestParam("followingId") Integer followingId) {
        Integer followerId = currentUser.getUserId();
        try {
            return followService.checkFollowStatus(followerId, followingId);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("查询失败，请稍后重试");
            return customResponse;
        }
    }
    
    /**
     * 获取用户的关注列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 关注列表
     */
    @GetMapping("/follow/list")
    public CustomResponse getFollowList(@RequestParam("uid") Integer uid,
                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                       @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            return followService.getFollowList(uid, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("获取关注列表失败");
            return customResponse;
        }
    }
    
    /**
     * 获取用户的粉丝列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 粉丝列表
     */
    @GetMapping("/follow/fans")
    public CustomResponse getFansList(@RequestParam("uid") Integer uid,
                                     @RequestParam(value = "page", defaultValue = "1") Integer page,
                                     @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            return followService.getFansList(uid, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("获取粉丝列表失败");
            return customResponse;
        }
    }
}
