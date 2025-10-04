package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.UserVideo;
import java.util.List;
import java.util.Map;

public interface UserVideoService {
    /**
     * 更新播放次数以及最近播放时间，顺便返回记录信息，没有记录则创建新记录
     * @param uid   用户ID
     * @param vid   视频ID
     * @return 更新后的数据信息
     */
    UserVideo updatePlay(Integer uid, Integer vid);

    /**
     * 点赞或点踩，返回更新后的信息
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isLove    赞还是踩 true赞 false踩
     * @param isSet     设置还是取消  true设置 false取消
     * @return  更新后的信息
     */
    UserVideo setLoveOrUnlove(Integer uid, Integer vid, boolean isLove, boolean isSet);

    /**
     * 收藏或取消收藏
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isCollect 是否收藏 true收藏 false取消
     */
    void collectOrCancel(Integer uid, Integer vid, boolean isCollect);

    /**
     * 获取用户收到的点赞列表（其他用户给该用户视频的点赞）
     * @param uid   用户ID（视频作者）
     * @param offset    偏移量
     * @param quantity  查询数量
     * @return  点赞记录列表，包含点赞用户信息、视频信息、点赞时间
     */
    List<Map<String, Object>> getReceivedLikes(Integer uid, Integer offset, Integer quantity);
}
