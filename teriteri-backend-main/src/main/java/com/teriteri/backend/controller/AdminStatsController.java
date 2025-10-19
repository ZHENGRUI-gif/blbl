package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.admin.AdminStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理后台统计控制器
 */
@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    @Autowired
    private AdminStatsService adminStatsService;

    /**
     * 获取用户统计数据
     */
    @GetMapping("/users")
    public CustomResponse getUserStats() {
        Map<String, Object> data = adminStatsService.getUserStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取用户增长趋势数据
     */
    @GetMapping("/user-growth-trend")
    public CustomResponse getUserGrowthTrend() {
        List<Map<String, Object>> data = adminStatsService.getUserGrowthTrend();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取用户性别分布统计
     */
    @GetMapping("/user-gender-distribution")
    public CustomResponse getUserGenderDistribution() {
        Map<String, Object> data = adminStatsService.getUserGenderDistribution();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取用户地区分布统计
     */
    @GetMapping("/user-location-distribution")
    public CustomResponse getUserLocationDistribution() {
        List<Map<String, Object>> data = adminStatsService.getUserLocationDistribution();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取用户等级分布统计
     */
    @GetMapping("/user-level-distribution")
    public CustomResponse getUserLevelDistribution() {
        Map<String, Object> data = adminStatsService.getUserLevelDistribution();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取视频统计数据
     */
    @GetMapping("/videos")
    public CustomResponse getVideoStats() {
        Map<String, Object> data = adminStatsService.getVideoStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取视频发布趋势数据
     */
    @GetMapping("/video-publish-trend")
    public CustomResponse getVideoPublishTrend() {
        List<Map<String, Object>> data = adminStatsService.getVideoPublishTrend();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取视频分类分布统计
     */
    @GetMapping("/video-category-distribution")
    public CustomResponse getVideoCategoryDistribution() {
        List<Map<String, Object>> data = adminStatsService.getVideoCategoryDistribution();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取视频互动统计数据
     */
    @GetMapping("/video-interaction")
    public CustomResponse getVideoInteractionStats() {
        Map<String, Object> data = adminStatsService.getVideoInteractionStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取互动趋势数据
     */
    @GetMapping("/interaction-trend")
    public CustomResponse getInteractionTrend() {
        List<Map<String, Object>> data = adminStatsService.getInteractionTrend();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取评论统计数据
     */
    @GetMapping("/comments")
    public CustomResponse getCommentStats() {
        Map<String, Object> data = adminStatsService.getCommentStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取弹幕统计数据
     */
    @GetMapping("/danmu")
    public CustomResponse getDanmuStats() {
        Map<String, Object> data = adminStatsService.getDanmuStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取关注关系统计数据
     */
    @GetMapping("/follows")
    public CustomResponse getFollowStats() {
        Map<String, Object> data = adminStatsService.getFollowStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取平台运营统计数据
     */
    @GetMapping("/platform")
    public CustomResponse getPlatformStats() {
        Map<String, Object> data = adminStatsService.getPlatformStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取内容审核统计数据
     */
    @GetMapping("/review")
    public CustomResponse getReviewStats() {
        Map<String, Object> data = adminStatsService.getReviewStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取热搜词数据
     */
    @GetMapping("/hot-search")
    public CustomResponse getHotSearchWords() {
        List<Map<String, Object>> data = adminStatsService.getHotSearchWords();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }

    /**
     * 获取数据中心概览数据
     */
    @GetMapping("/overview")
    public CustomResponse getOverviewStats() {
        Map<String, Object> data = adminStatsService.getPlatformStats();
        CustomResponse response = new CustomResponse();
        response.setData(data);
        return response;
    }
}
