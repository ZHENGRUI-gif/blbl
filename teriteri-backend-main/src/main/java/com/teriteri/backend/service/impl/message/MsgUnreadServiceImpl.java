package com.teriteri.backend.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.mapper.ChatMapper;
import com.teriteri.backend.mapper.CommentMapper;
import com.teriteri.backend.mapper.MsgUnreadMapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.Chat;
import com.teriteri.backend.pojo.Comment;
import com.teriteri.backend.pojo.CommentTree;
import com.teriteri.backend.pojo.IMResponse;
import com.teriteri.backend.pojo.MsgUnread;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.comment.CommentService;
import com.teriteri.backend.service.message.MsgUnreadService;
import com.teriteri.backend.utils.RedisUtil;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class MsgUnreadServiceImpl implements MsgUnreadService {

    @Autowired
    private MsgUnreadMapper msgUnreadMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    @Lazy
    private CommentService commentService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 给指定用户的某一列未读消息加一
     * @param uid   用户ID
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    @Override
    public void addOneUnread(Integer uid, String column) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).setSql("`" + column + "` = `" + column + "` + 1");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
    }

    /**
     * 清除指定用户的某一列未读消息
     * @param uid   用户ID
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    @Override
    public void clearUnread(Integer uid, String column) {
        QueryWrapper<MsgUnread> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        // 使用apply方法处理保留关键字
        queryWrapper.apply("`" + column + "` <> 0");
        MsgUnread msgUnread = msgUnreadMapper.selectOne(queryWrapper);
        // 如果本身就是0条未读就没必要执行下面的操作了 不过如果有未读的话做这个查询就会带来额外的开销
        if (msgUnread == null) return;

        // 通知用户的全部channel 更新该消息类型未读数为0
        Map<String, Object> map = new HashMap<>();
        map.put("type", "全部已读");
        Set<Channel> myChannels = IMServer.userChannel.get(uid);
        if (myChannels != null) {
            for (Channel channel : myChannels) {
                channel.writeAndFlush(IMResponse.message(column, map));
            }
        }

        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid);
        // 使用setSql方法处理保留关键字
        updateWrapper.setSql("`" + column + "` = 0");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
        if (Objects.equals(column, "whisper")) {
            // 如果是清除私聊消息还需要去把chat表的全部未读清掉
            UpdateWrapper<Chat> updateWrapper1 = new UpdateWrapper<>();
            updateWrapper1.eq("another_id", uid).set("unread", 0);
            chatMapper.update(null, updateWrapper1);
        }
    }

    /**
     * 私聊消息特有的减除一定数量的未读数
     * @param uid   用户ID
     * @param count 要减多少
     */
    @Override
    public void subtractWhisper(Integer uid, Integer count) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid)
                // 更新后的未读数不能小于0
                .setSql("whisper = CASE WHEN whisper - " + count + " < 0 THEN 0 ELSE whisper - " + count + " END");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
    }

    /**
     * 获取某人的全部消息未读数
     * @param uid   用户ID
     * @return  MsgUnread对象
     */
    @Override
    public MsgUnread getUnread(Integer uid) {
        MsgUnread msgUnread = redisUtil.getObject("msg_unread:" + uid, MsgUnread.class);
        if (msgUnread == null) {
            msgUnread = msgUnreadMapper.selectById(uid);
            if (msgUnread != null) {
                MsgUnread finalMsgUnread = msgUnread;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("msg_unread:" + uid, finalMsgUnread);    // 异步更新到redis
                }, taskExecutor);
            } else {
                return new MsgUnread(uid,0,0,0,0,0,0);
            }
        }
        return msgUnread;
    }

    /**
     * 获取用户收到的评论消息
     * @param uid 用户ID
     * @param offset 分页偏移量
     * @param count 获取数量
     * @return 评论树列表
     */
    @Override
    public List<CommentTree> getUserReceivedComments(Integer uid, Long offset, Integer count) {
        // 方法1：先尝试从Redis获取（推荐）
        Set<Object> commentIdsObj = redisUtil.zReverange("comment_video_owner:" + uid, offset, offset + count - 1);
        System.out.println("用户 " + uid + " 的Redis评论ID: " + commentIdsObj);
        Set<Integer> commentIds = commentIdsObj.stream()
                .filter(obj -> obj instanceof Integer)
                .map(obj -> (Integer) obj)
                .collect(Collectors.toSet());

        System.out.println("Redis中评论ID数量: " + commentIds.size());

        // 方法2：如果Redis为空，尝试从数据库查询（兜底方案）
        if (commentIds.isEmpty()) {
            System.out.println("Redis为空，尝试从数据库查询用户 " + uid + " 的评论");
            // 查询该用户所有视频的根评论
            QueryWrapper<Video> videoQueryWrapper = new QueryWrapper<>();
            videoQueryWrapper.eq("uid", uid).eq("status", 1); // 只查询审核通过的视频
            List<Video> userVideos = videoMapper.selectList(videoQueryWrapper);

            if (!userVideos.isEmpty()) {
                List<Integer> videoIds = userVideos.stream()
                        .map(Video::getVid)
                        .collect(Collectors.toList());

                QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
                commentQueryWrapper.in("vid", videoIds)
                        .eq("root_id", 0)
                        .eq("is_deleted", 0)
                        .orderByDesc("create_time")
                        .last("LIMIT " + count + " OFFSET " + offset);

                List<Comment> commentsFromDb = commentMapper.selectList(commentQueryWrapper);
                commentIds = commentsFromDb.stream()
                        .map(Comment::getId)
                        .collect(Collectors.toSet());

                System.out.println("从数据库找到 " + commentIds.size() + " 条评论ID");
            }
        }

        if (commentIds.isEmpty()) {
            System.out.println("用户 " + uid + " 没有收到任何评论");
            return Collections.emptyList();
        }

        // 根据评论ID获取评论详情
        List<Comment> comments = commentIds.stream()
                .map(commentMapper::selectById)
                .filter(comment -> comment != null && comment.getIsDeleted().equals(0))
                .collect(Collectors.toList());

        System.out.println("找到 " + comments.size() + " 条评论详情");
        for (Comment comment : comments) {
            System.out.println("评论ID: " + comment.getId() + ", 视频ID: " + comment.getVid() + ", 用户ID: " + comment.getUid() + ", 根评论ID: " + comment.getRootId());
        }

        // 为每个根评论构建评论树
        List<CommentTree> commentTrees = new ArrayList<>();
        for (Comment comment : comments) {
            CommentTree commentTree = commentService.getMoreCommentsById(comment.getId());
            commentTrees.add(commentTree);
        }

        return commentTrees;
    }

}
