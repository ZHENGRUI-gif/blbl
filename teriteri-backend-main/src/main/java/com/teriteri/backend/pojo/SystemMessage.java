package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统消息实体类
 * 使用follow表存储系统消息，通过is_deleted字段区分消息类型
 * is_deleted = 0: 关注消息
 * is_deleted = 1: 取消关注消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemMessage {
    private Integer id;                    // 消息ID (对应follow表的id)
    private Integer fromUserId;            // 操作者ID (对应follow表的follower_id)
    private Integer toUserId;              // 接收者ID (对应follow表的following_id)
    private String fromUserNickname;       // 操作者昵称
    private String fromUserAvatar;         // 操作者头像
    private String messageType;            // 消息类型: "follow" 或 "unfollow"
    private String messageContent;         // 消息内容
    private LocalDateTime createTime;      // 创建时间 (对应follow表的create_date)
    private LocalDateTime deleteTime;      // 删除时间 (对应follow表的delete_date)
    private Integer isRead;                // 是否已读 (新增字段，暂时用0表示未读，1表示已读)
}
