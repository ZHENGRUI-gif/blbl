package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.message.SystemMessageService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message/system")
public class SystemMessageController {
    
    @Autowired
    private SystemMessageService systemMessageService;
    
    @Autowired
    private CurrentUser currentUser;
    
    /**
     * 获取系统消息列表
     * @param page 页码
     * @param pageSize 每页数量
     * @return 系统消息列表
     */
    @GetMapping("/list")
    public CustomResponse getSystemMessages(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                           @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Integer uid = currentUser.getUserId();
        return systemMessageService.getSystemMessages(uid, page, pageSize);
    }
    
    /**
     * 标记系统消息为已读
     * @param messageId 消息ID
     * @return 操作结果
     */
    @PostMapping("/mark-read")
    public CustomResponse markAsRead(@RequestParam("messageId") Integer messageId) {
        Integer uid = currentUser.getUserId();
        return systemMessageService.markAsRead(messageId, uid);
    }
    
    /**
     * 获取未读系统消息数量
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    public CustomResponse getUnreadCount() {
        Integer uid = currentUser.getUserId();
        Integer count = systemMessageService.getUnreadCount(uid);
        
        CustomResponse response = new CustomResponse();
        response.setCode(200);
        response.setMessage("获取未读数量成功");
        response.setData(count);
        return response;
    }
}
