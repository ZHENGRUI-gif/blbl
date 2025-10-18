package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CommentTree;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.message.MsgUnreadService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MsgUnreadController {
    @Autowired
    private MsgUnreadService msgUnreadService;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 获取当前用户全部消息未读数
     * @return
     */
    @GetMapping("/msg-unread/all")
    public CustomResponse getMsgUnread() {
        Integer uid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(msgUnreadService.getUnread(uid));
        return customResponse;
    }

    /**
     * 清除某一列的未读消息提示
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    @PostMapping("/msg-unread/clear")
    public void clearUnread(@RequestParam("column") String column) {
        Integer uid = currentUser.getUserId();
        msgUnreadService.clearUnread(uid, column);
    }

    /**
     * 获取用户收到的评论消息
     * @param offset 分页偏移量
     * @param count 获取数量，默认10条
     * @return 评论消息列表
     */
    @GetMapping("/msg-unread/reply-comments")
    public CustomResponse getUserReceivedComments(@RequestParam(value = "offset", defaultValue = "0") Long offset,
                                                 @RequestParam(value = "count", defaultValue = "10") Integer count) {
        Integer uid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();

        List<CommentTree> comments = msgUnreadService.getUserReceivedComments(uid, offset, count);
        Map<String, Object> data = new HashMap<>();
        data.put("comments", comments);
        data.put("offset", offset);
        data.put("count", count);

        customResponse.setData(data);
        return customResponse;
    }
}
