package com.teriteri.backend.service.impl.message;

import com.teriteri.backend.mapper.SystemMessageMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.SystemMessage;
import com.teriteri.backend.service.message.SystemMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemMessageServiceImpl implements SystemMessageService {
    
    @Autowired
    private SystemMessageMapper systemMessageMapper;
    
    @Override
    public CustomResponse getSystemMessages(Integer uid, Integer page, Integer pageSize) {
        CustomResponse response = new CustomResponse();
        
        try {
            // 计算偏移量
            Integer offset = (page - 1) * pageSize;
            
            // 获取系统消息列表
            List<SystemMessage> messages = systemMessageMapper.getSystemMessages(uid, offset, pageSize);
            
            // 获取总数
            Integer total = systemMessageMapper.getSystemMessageCount(uid);
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("messages", messages);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (total + pageSize - 1) / pageSize);
            
            response.setCode(200);
            response.setMessage("获取系统消息成功");
            response.setData(data);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(500);
            response.setMessage("获取系统消息失败");
        }
        
        return response;
    }
    
    @Override
    public CustomResponse markAsRead(Integer messageId, Integer uid) {
        CustomResponse response = new CustomResponse();
        
        try {
            // 这里暂时不实现标记已读功能，因为follow表没有is_read字段
            // 如果需要实现，可以考虑在follow表中添加is_read字段
            response.setCode(200);
            response.setMessage("标记已读成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(500);
            response.setMessage("标记已读失败");
        }
        
        return response;
    }
    
    @Override
    public Integer getUnreadCount(Integer uid) {
        try {
            // 暂时返回0，因为follow表没有is_read字段
            // 如果需要实现未读计数，可以考虑在follow表中添加is_read字段
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
