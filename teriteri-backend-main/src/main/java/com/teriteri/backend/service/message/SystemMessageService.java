package com.teriteri.backend.service.message;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.SystemMessage;

import java.util.List;

public interface SystemMessageService {
    
    /**
     * 获取用户的系统消息列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 系统消息列表
     */
    CustomResponse getSystemMessages(Integer uid, Integer page, Integer pageSize);
    
    /**
     * 标记系统消息为已读
     * @param messageId 消息ID
     * @param uid 用户ID
     * @return 操作结果
     */
    CustomResponse markAsRead(Integer messageId, Integer uid);
    
    /**
     * 获取用户未读系统消息数量
     * @param uid 用户ID
     * @return 未读数量
     */
    Integer getUnreadCount(Integer uid);
}
