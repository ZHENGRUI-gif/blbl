package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.mapper.UserMapper;
import com.teriteri.backend.mapper.UserVideoMapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.IMResponse;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.pojo.UserVideo;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.message.MsgUnreadService;
import com.teriteri.backend.service.video.UserVideoService;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.RedisUtil;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class UserVideoServiceImpl implements UserVideoService {

    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private MsgUnreadService msgUnreadService;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 更新播放次数以及最近播放时间，顺便返回记录信息，没有记录则创建新记录
     * @param uid   用户ID
     * @param vid   视频ID
     * @return 更新后的数据信息
     */
    @Override
    public UserVideo updatePlay(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);
        if (userVideo == null) {
            // 记录不存在，创建新记录
            userVideo = new UserVideo(null, uid, vid, 1, 0, 0, 0, 0, new Date(), null, null);
            userVideoMapper.insert(userVideo);
        } else if (System.currentTimeMillis() - userVideo.getPlayTime().getTime() <= 30000) {
            // 如果最近30秒内播放过则不更新记录，直接返回
            return userVideo;
        } else {
            userVideo.setPlay(userVideo.getPlay() + 1);
            userVideo.setPlayTime(new Date());
            userVideoMapper.updateById(userVideo);
        }
        // 异步线程更新video表和redis
        CompletableFuture.runAsync(() -> {
            redisUtil.zset("user_video_history:" + uid, vid);   // 添加到/更新观看历史记录
            videoStatsService.updateStats(vid, "play", true, 1);
        }, taskExecutor);
        return userVideo;
    }

    /**
     * 点赞或点踩，返回更新后的信息
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isLove    赞还是踩 true赞 false踩
     * @param isSet     设置还是取消  true设置 false取消
     * @return  更新后的信息
     */
    @Override
    public UserVideo setLoveOrUnlove(Integer uid, Integer vid, boolean isLove, boolean isSet) {
        String key = "love_video:" + uid;
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);
        if (isLove && isSet) {
            // 点赞
            if (userVideo.getLove() == 1) {
                // 原本就点了赞就直接返回
                return userVideo;
            }
            // 插入点赞记录
            userVideo.setLove(1);
            UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).eq("vid", vid);
            updateWrapper.setSql("love = 1");
            updateWrapper.set("love_time", new Date());
            if (userVideo.getUnlove() == 1) {
                // 原本点了踩，要取消踩
                userVideo.setUnlove(0);
                updateWrapper.setSql("unlove = 0");
                CompletableFuture.runAsync(() -> {
                    videoStatsService.updateGoodAndBad(vid, true);
                }, taskExecutor);
            } else {
                // 原本没点踩，只需要点赞就行
                CompletableFuture.runAsync(() -> {
                    videoStatsService.updateStats(vid, "good", true, 1);
                }, taskExecutor);
            }
            redisUtil.zset(key, vid);   // 添加点赞记录
            userVideoMapper.update(null, updateWrapper);
            // 通知up主视频被赞了
            CompletableFuture.runAsync(() -> {
                // 查询UP主uid
                Video video = videoMapper.selectById(vid);
                if(!Objects.equals(video.getUid(), uid)) {
                    // 更新最新被点赞的视频
                    redisUtil.zset("be_loved_zset:" + video.getUid(), vid);
                    msgUnreadService.addOneUnread(video.getUid(), "love");
                    // netty 通知未读消息
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", "接收");
                    Set<Channel> channels = IMServer.userChannel.get(video.getUid());
                    if (channels != null) {
                        for (Channel channel: channels) {
                            channel.writeAndFlush(IMResponse.message("love", map));
                        }
                    }
                }
            }, taskExecutor);
        } else if (isLove) {
            // 取消点赞
            if (userVideo.getLove() == 0) {
                // 原本就没有点赞就直接返回
                return userVideo;
            }
            // 取消赞
            userVideo.setLove(0);
            UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).eq("vid", vid);
            updateWrapper.setSql("love = 0");
            userVideoMapper.update(null, updateWrapper);
            redisUtil.zsetDelMember(key, vid);  // 移除点赞记录
            CompletableFuture.runAsync(() -> {
                videoStatsService.updateStats(vid, "good", false, 1);
            }, taskExecutor);
        } else if (isSet) {
            // 点踩
            if (userVideo.getUnlove() == 1) {
                // 原本就点了踩就直接返回
                return userVideo;
            }
            // 更新用户点踩记录
            userVideo.setUnlove(1);
            UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).eq("vid", vid);
            updateWrapper.setSql("unlove = 1");
            if (userVideo.getLove() == 1) {
                // 原本点了赞，要取消赞
                userVideo.setLove(0);
                updateWrapper.setSql("love = 0");
                redisUtil.zsetDelMember(key, vid);  // 移除点赞记录
                CompletableFuture.runAsync(() -> {
                    videoStatsService.updateGoodAndBad(vid, false);
                }, taskExecutor);
            } else {
                // 原本没点赞，只需要点踩就行
                CompletableFuture.runAsync(() -> {
                    videoStatsService.updateStats(vid, "bad", true, 1);
                }, taskExecutor);
            }
            userVideoMapper.update(null, updateWrapper);
        } else {
            // 取消点踩
            if (userVideo.getUnlove() == 0) {
                // 原本就没有点踩就直接返回
                return userVideo;
            }
            // 取消用户点踩记录
            userVideo.setUnlove(0);
            UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).eq("vid", vid);
            updateWrapper.setSql("unlove = 0");
            userVideoMapper.update(null, updateWrapper);
            CompletableFuture.runAsync(() -> {
                videoStatsService.updateStats(vid, "bad", false, 1);
            }, taskExecutor);
        }
        return userVideo;
    }

    /**
     * 收藏或取消收藏
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isCollect 是否收藏 true收藏 false取消
     * @return  返回更新后的信息
     */
    @Override
    public void collectOrCancel(Integer uid, Integer vid, boolean isCollect) {
        UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).eq("vid", vid);
        if (isCollect) {
            updateWrapper.setSql("collect = 1");
        } else {
            updateWrapper.setSql("collect = 0");
        }
        CompletableFuture.runAsync(() -> {
            videoStatsService.updateStats(vid, "collect", isCollect, 1);
        }, taskExecutor);
        userVideoMapper.update(null, updateWrapper);
    }

    /**
     * 获取用户收到的点赞列表（其他用户给该用户视频的点赞）
     * @param uid   用户ID（视频作者）
     * @param offset    偏移量
     * @param quantity  查询数量
     * @return  点赞记录列表，包含点赞用户信息、视频信息、点赞时间
     */
    @Override
    public List<Map<String, Object>> getReceivedLikes(Integer uid, Integer offset, Integer quantity) {
        if (offset == null) offset = 0;
        if (quantity == null) quantity = 10;
        
        // 先查询该用户的所有视频ID
        QueryWrapper<Video> videoQueryWrapper = new QueryWrapper<>();
        videoQueryWrapper.select("vid").eq("uid", uid).ne("status", 3);
        List<Video> userVideoList = videoMapper.selectList(videoQueryWrapper);

        if (userVideoList.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Integer> userVideoIds = userVideoList.stream().map(Video::getVid).collect(Collectors.toList());
        
        // 查询这些视频的点赞记录，按点赞时间倒序
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("love", 1)
                   .isNotNull("love_time")
                   .in("vid", userVideoIds)
                   .orderByDesc("love_time")
                   .last("LIMIT " + offset + ", " + quantity);
        
        List<UserVideo> userVideos = userVideoMapper.selectList(queryWrapper);

        if (userVideos.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取所有相关的视频ID和用户ID
        List<Integer> vidList = userVideos.stream().map(UserVideo::getVid).collect(Collectors.toList());
        List<Integer> uidList = userVideos.stream().map(UserVideo::getUid).collect(Collectors.toList());
        
        // 批量查询视频信息
        QueryWrapper<Video> videoBatchQueryWrapper = new QueryWrapper<>();
        videoBatchQueryWrapper.in("vid", vidList);
        List<Video> videos = videoMapper.selectList(videoBatchQueryWrapper);
        Map<Integer, Video> videoMap = videos.stream().collect(Collectors.toMap(Video::getVid, v -> v));
        
        // 批量查询用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("uid", uidList);
        List<User> users = userMapper.selectList(userQueryWrapper);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUid, u -> u));
        
        // 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserVideo userVideo : userVideos) {
            Map<String, Object> item = new HashMap<>();
            item.put("userVideo", userVideo);
            item.put("video", videoMap.get(userVideo.getVid()));
            
            // 转换用户信息，将avatar字段映射为avatar_url
            User user = userMap.get(userVideo.getUid());
            Map<String, Object> userInfo = new HashMap<>();
            if (user != null) {
                userInfo.put("uid", user.getUid());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar_url", user.getAvatar()); // 关键：将avatar映射为avatar_url
                userInfo.put("bg_url", user.getBackground());
                userInfo.put("gender", user.getGender());
                userInfo.put("description", user.getDescription());
                userInfo.put("exp", user.getExp());
                userInfo.put("coin", user.getCoin());
                userInfo.put("vip", user.getVip());
                userInfo.put("state", user.getState());
                userInfo.put("auth", user.getAuth());
                userInfo.put("authMsg", user.getAuthMsg());
            }
            item.put("user", userInfo);
            
            // 获取视频统计数据
            item.put("stats", videoStatsService.getVideoStatsById(userVideo.getVid()));
            result.add(item);
        }
        
        return result;
    }
}
