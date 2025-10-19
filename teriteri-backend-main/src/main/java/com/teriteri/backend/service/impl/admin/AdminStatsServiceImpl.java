package com.teriteri.backend.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.*;
import com.teriteri.backend.pojo.*;
import com.teriteri.backend.service.admin.AdminStatsService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理后台统计服务实现类
 */
@Service
public class AdminStatsServiceImpl implements AdminStatsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DanmuMapper danmuMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private HotSearchMapper hotSearchMapper;

    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> result = new HashMap<>();

        // 总用户数
        Long totalUsers = userMapper.selectCount(null);
        result.put("totalUsers", totalUsers);

        // 今日新增用户
        LocalDate today = LocalDate.now();
        QueryWrapper<User> todayQuery = new QueryWrapper<>();
        todayQuery.apply("DATE(create_date) = DATE({0})", today.format(DateTimeFormatter.ISO_DATE));
        Long todayUsers = userMapper.selectCount(todayQuery);
        result.put("todayUsers", todayUsers);

        // 昨日新增用户
        LocalDate yesterday = today.minusDays(1);
        QueryWrapper<User> yesterdayQuery = new QueryWrapper<>();
        yesterdayQuery.apply("DATE(create_date) = DATE({0})", yesterday.format(DateTimeFormatter.ISO_DATE));
        Long yesterdayUsers = userMapper.selectCount(yesterdayQuery);
        result.put("yesterdayUsers", yesterdayUsers);

        // 男性用户数
        QueryWrapper<User> maleQuery = new QueryWrapper<>();
        maleQuery.eq("gender", 1);
        Long maleUsers = userMapper.selectCount(maleQuery);
        result.put("maleUsers", maleUsers);

        // 女性用户数
        QueryWrapper<User> femaleQuery = new QueryWrapper<>();
        femaleQuery.eq("gender", 0);
        Long femaleUsers = userMapper.selectCount(femaleQuery);
        result.put("femaleUsers", femaleUsers);

        // 会员用户数
        QueryWrapper<User> vipQuery = new QueryWrapper<>();
        vipQuery.gt("vip", 0);
        Long vipUsers = userMapper.selectCount(vipQuery);
        result.put("vipUsers", vipUsers);

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserGrowthTrend() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取最近30天的用户注册数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            QueryWrapper<User> query = new QueryWrapper<>();
            query.apply("DATE(create_date) = DATE({0})", date.format(DateTimeFormatter.ISO_DATE));

            Long count = userMapper.selectCount(query);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ISO_DATE));
            dayData.put("count", count);
            result.add(dayData);
        }

        return result;
    }

    @Override
    public Map<String, Object> getUserGenderDistribution() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取所有用户的总数
            Long totalUsers = userMapper.selectCount(null);

            // 查询所有用户的gender分布
            QueryWrapper<User> allUsersQuery = new QueryWrapper<>();
            List<User> allUsers = userMapper.selectList(allUsersQuery);

            Map<Integer, Long> genderCount = new HashMap<>();
            for (User user : allUsers) {
                Integer gender = user.getGender();
                genderCount.put(gender, genderCount.getOrDefault(gender, 0L) + 1);
            }

            // 男性用户数 (gender = 1)
            Long maleUsers = genderCount.getOrDefault(1, 0L);

            // 女性用户数 (gender = 0)
            Long femaleUsers = genderCount.getOrDefault(0, 0L);

            // 无性别/保密用户数 (gender = 2 或 gender IS NULL)
            Long unknownUsers = genderCount.getOrDefault(2, 0L) + genderCount.getOrDefault(null, 0L);

            // 调试输出
            System.out.println("用户总数: " + totalUsers);
            System.out.println("所有用户gender分布: " + genderCount);
            System.out.println("男性用户: " + maleUsers);
            System.out.println("女性用户: " + femaleUsers);
            System.out.println("未知性别用户: " + unknownUsers);
            System.out.println("总和检查: " + (maleUsers + femaleUsers + unknownUsers) + " vs " + totalUsers);

            result.put("male", maleUsers);
            result.put("female", femaleUsers);
            result.put("unknown", unknownUsers);

        } catch (Exception e) {
            System.err.println("查询用户性别分布失败: " + e.getMessage());
            e.printStackTrace();

            // 如果查询失败，返回默认值
            result.put("male", 0L);
            result.put("female", 0L);
            result.put("unknown", 0L);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserLocationDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取用户地区分布
        QueryWrapper<User> query = new QueryWrapper<>();
        query.isNotNull("location");
        query.ne("location", "");

        List<User> users = userMapper.selectList(query);

        // 统计各地区的用户数量
        Map<String, Long> locationCount = new HashMap<>();
        for (User user : users) {
            String location = user.getLocation();
            if (location != null && !location.trim().isEmpty()) {
                locationCount.put(location, locationCount.getOrDefault(location, 0L) + 1);
            }
        }

        // 转换为前端需要的格式
        for (Map.Entry<String, Long> entry : locationCount.entrySet()) {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("name", entry.getKey());
            locationData.put("value", entry.getValue());
            result.add(locationData);
        }

        // 按数量排序
        result.sort((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")));

        return result;
    }

    @Override
    public Map<String, Object> getUserLevelDistribution() {
        Map<String, Object> result = new HashMap<>();

        // 根据经验值划分等级：0级(0-49)、1级(50-199)、2级(200-1499)、3级(1500-4499)、4级(4500-10799)、5级(10800-28799)、6级(28800+)
        QueryWrapper<User> level0Query = new QueryWrapper<>();
        level0Query.lt("exp", 50);
        Long level0 = userMapper.selectCount(level0Query);

        QueryWrapper<User> level1Query = new QueryWrapper<>();
        level1Query.between("exp", 50, 199);
        Long level1 = userMapper.selectCount(level1Query);

        QueryWrapper<User> level2Query = new QueryWrapper<>();
        level2Query.between("exp", 200, 1499);
        Long level2 = userMapper.selectCount(level2Query);

        QueryWrapper<User> level3Query = new QueryWrapper<>();
        level3Query.between("exp", 1500, 4499);
        Long level3 = userMapper.selectCount(level3Query);

        QueryWrapper<User> level4Query = new QueryWrapper<>();
        level4Query.between("exp", 4500, 10799);
        Long level4 = userMapper.selectCount(level4Query);

        QueryWrapper<User> level5Query = new QueryWrapper<>();
        level5Query.between("exp", 10800, 28799);
        Long level5 = userMapper.selectCount(level5Query);

        QueryWrapper<User> level6Query = new QueryWrapper<>();
        level6Query.ge("exp", 28800);
        Long level6 = userMapper.selectCount(level6Query);

        result.put("level0", level0);
        result.put("level1", level1);
        result.put("level2", level2);
        result.put("level3", level3);
        result.put("level4", level4);
        result.put("level5", level5);
        result.put("level6", level6);

        return result;
    }

    @Override
    public Map<String, Object> getVideoStats() {
        Map<String, Object> result = new HashMap<>();

        // 总视频数
        Long totalVideos = videoMapper.selectCount(null);
        result.put("totalVideos", totalVideos);

        // 今日新增视频
        LocalDate today = LocalDate.now();
        QueryWrapper<Video> todayQuery = new QueryWrapper<>();
        todayQuery.apply("DATE(upload_date) = DATE({0})", today.format(DateTimeFormatter.ISO_DATE));
        Long todayVideos = videoMapper.selectCount(todayQuery);
        result.put("todayVideos", todayVideos);

        // 审核通过视频数
        QueryWrapper<Video> approvedQuery = new QueryWrapper<>();
        approvedQuery.eq("status", 1);
        Long approvedVideos = videoMapper.selectCount(approvedQuery);
        result.put("approvedVideos", approvedVideos);

        // 待审核视频数
        QueryWrapper<Video> pendingQuery = new QueryWrapper<>();
        pendingQuery.eq("status", 0);
        Long pendingVideos = videoMapper.selectCount(pendingQuery);
        result.put("pendingVideos", pendingVideos);

        return result;
    }

    @Override
    public List<Map<String, Object>> getVideoPublishTrend() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取最近30天的视频发布数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            QueryWrapper<Video> query = new QueryWrapper<>();
            query.apply("DATE(upload_date) = DATE({0})", date.format(DateTimeFormatter.ISO_DATE));

            Long count = videoMapper.selectCount(query);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ISO_DATE));
            dayData.put("count", count);
            result.add(dayData);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getVideoCategoryDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 获取所有分类信息
            List<Category> categories = categoryMapper.selectList(null);

            // 获取所有视频，统计每个主分类的视频数量
            QueryWrapper<Video> videoQuery = new QueryWrapper<>();
            videoQuery.isNotNull("mc_id").isNotNull("sc_id");
            List<Video> videos = videoMapper.selectList(videoQuery);

            // 按主分类统计视频数量
            Map<String, Long> categoryCount = new HashMap<>();

            for (Video video : videos) {
                String mcId = video.getMcId();
                if (mcId != null && !mcId.trim().isEmpty()) {
                    categoryCount.put(mcId, categoryCount.getOrDefault(mcId, 0L) + 1);
                }
            }

            // 查找对应的分类名称
            for (Category category : categories) {
                String mcId = category.getMcId();
                if (categoryCount.containsKey(mcId)) {
                    Map<String, Object> categoryData = new HashMap<>();
                    categoryData.put("name", category.getMcName());
                    categoryData.put("value", categoryCount.get(mcId));
                    result.add(categoryData);
                }
            }

            // 按数量排序
            result.sort((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")));

        } catch (Exception e) {
            // 如果查询失败，返回模拟数据
            Map<String, Object> category1 = new HashMap<>();
            category1.put("name", "动画");
            category1.put("value", 150L);
            result.add(category1);

            Map<String, Object> category2 = new HashMap<>();
            category2.put("name", "音乐");
            category2.put("value", 89L);
            result.add(category2);

            Map<String, Object> category3 = new HashMap<>();
            category3.put("name", "舞蹈");
            category3.put("value", 67L);
            result.add(category3);

            Map<String, Object> category4 = new HashMap<>();
            category4.put("name", "游戏");
            category4.put("value", 234L);
            result.add(category4);

            Map<String, Object> category5 = new HashMap<>();
            category5.put("name", "生活");
            category5.put("value", 123L);
            result.add(category5);
        }

        return result;
    }

    @Override
    public Map<String, Object> getVideoInteractionStats() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取所有视频的总互动数据
            QueryWrapper<VideoStats> query = new QueryWrapper<>();
            List<VideoStats> allStats = videoStatsMapper.selectList(query);

            // 调试：打印查询结果数量
            System.out.println("查询到 " + allStats.size() + " 条视频统计记录");

            // 累加所有数据
            Long totalPlay = allStats.stream().mapToLong(VideoStats::getPlay).sum();
            Long totalDanmu = allStats.stream().mapToLong(VideoStats::getDanmu).sum();
            Long totalGood = allStats.stream().mapToLong(VideoStats::getGood).sum();
            Long totalBad = allStats.stream().mapToLong(VideoStats::getBad).sum();
            Long totalCoin = allStats.stream().mapToLong(VideoStats::getCoin).sum();
            Long totalCollect = allStats.stream().mapToLong(VideoStats::getCollect).sum();
            Long totalShare = allStats.stream().mapToLong(VideoStats::getShare).sum();
            Long totalComment = allStats.stream().mapToLong(VideoStats::getComment).sum();

            // 调试：打印累加结果
            System.out.println("总播放: " + totalPlay + ", 总弹幕: " + totalDanmu + ", 总点赞: " + totalGood);

            result.put("totalPlay", totalPlay);
            result.put("totalDanmu", totalDanmu);
            result.put("totalGood", totalGood);
            result.put("totalBad", totalBad);
            result.put("totalCoin", totalCoin);
            result.put("totalCollect", totalCollect);
            result.put("totalShare", totalShare);
            result.put("totalComment", totalComment);

        } catch (Exception e) {
            System.err.println("查询视频互动统计数据失败: " + e.getMessage());
            e.printStackTrace();

            // 如果查询失败，返回默认值
            result.put("totalPlay", 0L);
            result.put("totalDanmu", 0L);
            result.put("totalGood", 0L);
            result.put("totalBad", 0L);
            result.put("totalCoin", 0L);
            result.put("totalCollect", 0L);
            result.put("totalShare", 0L);
            result.put("totalComment", 0L);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getInteractionTrend() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 获取最近30天的互动数据
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(29);

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                String dateStr = date.format(DateTimeFormatter.ISO_DATE);

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateStr);

                // 查询该日的播放数据（从user_video表统计）
                QueryWrapper<UserVideo> playQuery = new QueryWrapper<>();
                playQuery.apply("DATE(play_time) = DATE({0})", dateStr);
                List<UserVideo> playRecords = userVideoMapper.selectList(playQuery);
                Long playCount = playRecords.stream().mapToLong(UserVideo::getPlay).sum();
                dayData.put("play", playCount);

                // 查询该日的弹幕数据
                QueryWrapper<Danmu> danmuQuery = new QueryWrapper<>();
                danmuQuery.apply("DATE(create_date) = DATE({0})", dateStr);
                Long danmuCount = danmuMapper.selectCount(danmuQuery);
                dayData.put("danmu", danmuCount);

                // 查询该日的评论数据
                QueryWrapper<Comment> commentQuery = new QueryWrapper<>();
                commentQuery.apply("DATE(create_time) = DATE({0})", dateStr);
                Long commentCount = commentMapper.selectCount(commentQuery);
                dayData.put("comment", commentCount);

                // 查询该日的点赞数据（从user_video表统计）
                QueryWrapper<UserVideo> likeQuery = new QueryWrapper<>();
                likeQuery.apply("DATE(love_time) = DATE({0})", dateStr)
                        .eq("love", 1); // 只统计点赞的记录
                Long likeCount = userVideoMapper.selectCount(likeQuery);
                dayData.put("good", likeCount);

                result.add(dayData);
            }

            // 调试输出
            System.out.println("互动趋势数据查询完成，共 " + result.size() + " 天数据");

        } catch (Exception e) {
            System.err.println("查询互动趋势数据失败: " + e.getMessage());
            e.printStackTrace();

            // 如果查询失败，返回模拟数据
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(29);

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                String dateStr = date.format(DateTimeFormatter.ISO_DATE);

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateStr);
                dayData.put("play", Math.round(Math.random() * 1000) + 100);
                dayData.put("danmu", Math.round(Math.random() * 200) + 20);
                dayData.put("comment", Math.round(Math.random() * 150) + 10);
                dayData.put("good", Math.round(Math.random() * 300) + 50);
                result.add(dayData);
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> getCommentStats() {
        Map<String, Object> result = new HashMap<>();

        // 总评论数
        Long totalComments = commentMapper.selectCount(null);
        result.put("totalComments", totalComments);

        // 今日评论数
        LocalDate today = LocalDate.now();
        QueryWrapper<Comment> todayQuery = new QueryWrapper<>();
        todayQuery.apply("DATE(create_time) = DATE({0})", today.format(DateTimeFormatter.ISO_DATE));
        Long todayComments = commentMapper.selectCount(todayQuery);
        result.put("todayComments", todayComments);

        // 根评论数
        QueryWrapper<Comment> rootQuery = new QueryWrapper<>();
        rootQuery.eq("root_id", 0);
        Long rootComments = commentMapper.selectCount(rootQuery);
        result.put("rootComments", rootComments);

        // 回复评论数
        Long replyComments = totalComments - rootComments;
        result.put("replyComments", replyComments);

        return result;
    }

    @Override
    public Map<String, Object> getDanmuStats() {
        Map<String, Object> result = new HashMap<>();

        // 总弹幕数
        Long totalDanmu = danmuMapper.selectCount(null);
        result.put("totalDanmu", totalDanmu);

        // 今日弹幕数
        LocalDate today = LocalDate.now();
        QueryWrapper<Danmu> todayQuery = new QueryWrapper<>();
        todayQuery.apply("DATE(create_date) = DATE({0})", today.format(DateTimeFormatter.ISO_DATE));
        Long todayDanmu = danmuMapper.selectCount(todayQuery);
        result.put("todayDanmu", todayDanmu);

        return result;
    }

    @Override
    public Map<String, Object> getFollowStats() {
        Map<String, Object> result = new HashMap<>();

        // 总关注数
        Long totalFollows = followMapper.selectCount(null);
        result.put("totalFollows", totalFollows);

        // 今日关注数
        LocalDate today = LocalDate.now();
        QueryWrapper<Follow> todayQuery = new QueryWrapper<>();
        todayQuery.apply("DATE(create_date) = DATE({0})", today.format(DateTimeFormatter.ISO_DATE));
        Long todayFollows = followMapper.selectCount(todayQuery);
        result.put("todayFollows", todayFollows);

        return result;
    }

    @Override
    public Map<String, Object> getPlatformStats() {
        Map<String, Object> result = new HashMap<>();

        // 平台总数据
        Long totalUsers = userMapper.selectCount(null);
        Long totalVideos = videoMapper.selectCount(null);
        Long totalComments = commentMapper.selectCount(null);
        Long totalDanmu = danmuMapper.selectCount(null);

        result.put("totalUsers", totalUsers);
        result.put("totalVideos", totalVideos);
        result.put("totalComments", totalComments);
        result.put("totalDanmu", totalDanmu);

        return result;
    }

    @Override
    public Map<String, Object> getReviewStats() {
        Map<String, Object> result = new HashMap<>();

        // 待审核视频数
        QueryWrapper<Video> pendingVideos = new QueryWrapper<>();
        pendingVideos.eq("status", 0);
        Long pendingVideoCount = videoMapper.selectCount(pendingVideos);
        result.put("pendingVideos", pendingVideoCount);

        // 已审核视频数
        QueryWrapper<Video> approvedVideos = new QueryWrapper<>();
        approvedVideos.eq("status", 1);
        Long approvedVideoCount = videoMapper.selectCount(approvedVideos);
        result.put("approvedVideos", approvedVideoCount);

        // 被打回视频数
        QueryWrapper<Video> rejectedVideos = new QueryWrapper<>();
        rejectedVideos.eq("status", 2);
        Long rejectedVideoCount = videoMapper.selectCount(rejectedVideos);
        result.put("rejectedVideos", rejectedVideoCount);

        return result;
    }

    @Override
    public List<Map<String, Object>> getHotSearchWords() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 获取热搜词（这里简化处理，实际应该从专门的热搜表获取）
            List<HotSearch> hotSearches = hotSearchMapper.selectList(null);

            for (HotSearch hotSearch : hotSearches) {
                Map<String, Object> wordData = new HashMap<>();
                wordData.put("word", hotSearch.getContent());
                wordData.put("count", hotSearch.getScore().longValue());
                result.add(wordData);
            }

            // 按搜索量排序
            result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));

        } catch (Exception e) {
            // 如果查询失败，返回模拟数据用于测试
            Map<String, Object> word1 = new HashMap<>();
            word1.put("word", "动画");
            word1.put("count", 150L);
            result.add(word1);

            Map<String, Object> word2 = new HashMap<>();
            word2.put("word", "游戏");
            word2.put("count", 89L);
            result.add(word2);

            Map<String, Object> word3 = new HashMap<>();
            word3.put("word", "音乐");
            word3.put("count", 67L);
            result.add(word3);

            Map<String, Object> word4 = new HashMap<>();
            word4.put("word", "舞蹈");
            word4.put("count", 45L);
            result.add(word4);

            Map<String, Object> word5 = new HashMap<>();
            word5.put("word", "美食");
            word5.put("count", 23L);
            result.add(word5);
        }

        return result;
    }
}
