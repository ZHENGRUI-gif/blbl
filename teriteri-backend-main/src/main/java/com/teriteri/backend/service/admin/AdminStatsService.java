package com.teriteri.backend.service.admin;

import java.util.List;
import java.util.Map;

/**
 * 管理后台统计服务接口
 */
public interface AdminStatsService {

    /**
     * 获取用户统计数据
     * @return 用户统计信息
     */
    Map<String, Object> getUserStats();

    /**
     * 获取用户增长趋势数据（最近30天）
     * @return 每日用户注册数量
     */
    List<Map<String, Object>> getUserGrowthTrend();

    /**
     * 获取用户性别分布统计
     * @return 性别分布数据
     */
    Map<String, Object> getUserGenderDistribution();

    /**
     * 获取用户地区分布统计
     * @return 地区分布数据
     */
    List<Map<String, Object>> getUserLocationDistribution();

    /**
     * 获取用户等级分布统计
     * @return 等级分布数据
     */
    Map<String, Object> getUserLevelDistribution();

    /**
     * 获取视频统计数据
     * @return 视频统计信息
     */
    Map<String, Object> getVideoStats();

    /**
     * 获取视频发布趋势数据（最近30天）
     * @return 每日视频发布数量
     */
    List<Map<String, Object>> getVideoPublishTrend();

    /**
     * 获取视频分类分布统计
     * @return 分类分布数据
     */
    List<Map<String, Object>> getVideoCategoryDistribution();

    /**
     * 获取视频互动统计数据
     * @return 互动统计信息
     */
    Map<String, Object> getVideoInteractionStats();

    /**
     * 获取互动趋势数据（最近30天）
     * @return 每日互动数据
     */
    List<Map<String, Object>> getInteractionTrend();

    /**
     * 获取评论统计数据
     * @return 评论统计信息
     */
    Map<String, Object> getCommentStats();

    /**
     * 获取弹幕统计数据
     * @return 弹幕统计信息
     */
    Map<String, Object> getDanmuStats();

    /**
     * 获取关注关系统计数据
     * @return 关注统计信息
     */
    Map<String, Object> getFollowStats();

    /**
     * 获取平台运营统计数据
     * @return 运营统计信息
     */
    Map<String, Object> getPlatformStats();

    /**
     * 获取内容审核统计数据
     * @return 审核统计信息
     */
    Map<String, Object> getReviewStats();

    /**
     * 获取热搜词数据
     * @return 热搜词列表
     */
    List<Map<String, Object>> getHotSearchWords();
}
