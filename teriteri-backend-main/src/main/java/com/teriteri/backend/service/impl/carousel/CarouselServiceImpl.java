package com.teriteri.backend.service.impl.carousel;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.CarouselConfig;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.carousel.CarouselService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarouselServiceImpl implements CarouselService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private VideoMapper videoMapper;
    
    private static final String CAROUSEL_KEY = "carousel:config";
    
    /**
     * 客户端接口：获取启用状态的轮播图列表
     */
    @Override
    public CustomResponse getEnabledCarousels() {
        CustomResponse response = new CustomResponse();
        
        try {
            // 从Redis获取所有轮播图配置
            List<CarouselConfig> configs = redisUtil.getAllList(CAROUSEL_KEY, CarouselConfig.class);
            
            if (configs == null || configs.isEmpty()) {
                response.setData(new ArrayList<>());
                return response;
            }
            
            // 过滤启用状态并按排序
            List<CarouselConfig> enabledConfigs = configs.stream()
                    .filter(config -> config.getStatus() == 1)
                    .sorted(Comparator.comparing(CarouselConfig::getSort))
                    .collect(Collectors.toList());
            
            // 构建返回数据（包含视频信息）
            List<Map<String, Object>> result = new ArrayList<>();
            for (CarouselConfig config : enabledConfigs) {
                Video video = videoMapper.selectById(config.getVid());
                if (video != null && video.getStatus() == 1) { // 只返回已过审的视频
                    Map<String, Object> item = new HashMap<>();
                    item.put("url", video.getCoverUrl());
                    item.put("title", video.getTitle());
                    item.put("color", config.getColor());
                    item.put("target", "/video/" + video.getVid());
                    result.add(item);
                }
            }
            
            response.setData(result);
        } catch (Exception e) {
            log.error("获取轮播图失败", e);
            response.setCode(500);
            response.setMessage("获取轮播图失败");
        }
        
        return response;
    }
    
    /**
     * 管理端接口：获取所有轮播图配置
     */
    @Override
    public CustomResponse getAllCarousels() {
        CustomResponse response = new CustomResponse();
        
        try {
            List<CarouselConfig> configs = redisUtil.getAllList(CAROUSEL_KEY, CarouselConfig.class);
            
            if (configs == null || configs.isEmpty()) {
                response.setData(new ArrayList<>());
                return response;
            }
            
            // 排序
            configs.sort(Comparator.comparing(CarouselConfig::getSort));
            
            // 补充视频信息
            List<Map<String, Object>> result = new ArrayList<>();
            for (CarouselConfig config : configs) {
                Video video = videoMapper.selectById(config.getVid());
                Map<String, Object> item = new HashMap<>();
                item.put("vid", config.getVid());
                item.put("color", config.getColor());
                item.put("sort", config.getSort());
                item.put("status", config.getStatus());
                if (video != null) {
                    item.put("url", video.getCoverUrl());
                    item.put("title", video.getTitle());
                    item.put("videoStatus", video.getStatus());
                } else {
                    item.put("url", "");
                    item.put("title", "视频已删除");
                    item.put("videoStatus", -1);
                }
                result.add(item);
            }
            
            response.setData(result);
        } catch (Exception e) {
            log.error("获取轮播图配置失败", e);
            response.setCode(500);
            response.setMessage("获取轮播图配置失败");
        }
        
        return response;
    }
    
    /**
     * 管理端接口：添加轮播图
     */
    @Override
    public CustomResponse addCarousel(Integer vid, String color, Integer sort) {
        CustomResponse response = new CustomResponse();
        
        try {
            // 检查视频是否存在
            Video video = videoMapper.selectById(vid);
            if (video == null) {
                response.setCode(404);
                response.setMessage("视频不存在");
                return response;
            }
            
            // 获取现有配置
            List<CarouselConfig> configs = redisUtil.getAllList(CAROUSEL_KEY, CarouselConfig.class);
            if (configs == null) {
                configs = new ArrayList<>();
            }
            
            // 检查是否已存在
            boolean exists = configs.stream().anyMatch(c -> c.getVid().equals(vid));
            if (exists) {
                response.setCode(400);
                response.setMessage("该视频已在轮播图中");
                return response;
            }
            
            // 创建新配置
            CarouselConfig config = new CarouselConfig();
            config.setVid(vid);
            config.setUrl(video.getCoverUrl());
            config.setTitle(video.getTitle());
            config.setColor(color != null ? color : "#ca8d6b");
            config.setTarget("/video/" + vid);
            config.setSort(sort != null ? sort : configs.size());
            config.setStatus(1);
            
            configs.add(config);
            
            // 保存到Redis
            redisUtil.delValue(CAROUSEL_KEY);
            redisUtil.setAllList(CAROUSEL_KEY, configs);
            
            response.setMessage("添加成功");
            response.setData(config);
        } catch (Exception e) {
            log.error("添加轮播图失败", e);
            response.setCode(500);
            response.setMessage("添加失败");
        }
        
        return response;
    }
    
    /**
     * 管理端接口：更新轮播图配置
     */
    @Override
    public CustomResponse updateCarousel(Integer vid, String color, Integer sort, Integer status) {
        CustomResponse response = new CustomResponse();
        
        try {
            List<CarouselConfig> configs = redisUtil.getAllList(CAROUSEL_KEY, CarouselConfig.class);
            if (configs == null || configs.isEmpty()) {
                response.setCode(404);
                response.setMessage("轮播图不存在");
                return response;
            }
            
            // 查找并更新
            boolean found = false;
            for (CarouselConfig config : configs) {
                if (config.getVid().equals(vid)) {
                    if (color != null) config.setColor(color);
                    if (sort != null) config.setSort(sort);
                    if (status != null) config.setStatus(status);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                response.setCode(404);
                response.setMessage("轮播图不存在");
                return response;
            }
            
            // 保存到Redis
            redisUtil.delValue(CAROUSEL_KEY);
            redisUtil.setAllList(CAROUSEL_KEY, configs);
            
            response.setMessage("更新成功");
        } catch (Exception e) {
            log.error("更新轮播图失败", e);
            response.setCode(500);
            response.setMessage("更新失败");
        }
        
        return response;
    }
    
    /**
     * 管理端接口：删除轮播图
     */
    @Override
    public CustomResponse deleteCarousel(Integer vid) {
        CustomResponse response = new CustomResponse();
        
        try {
            List<CarouselConfig> configs = redisUtil.getAllList(CAROUSEL_KEY, CarouselConfig.class);
            if (configs == null || configs.isEmpty()) {
                response.setCode(404);
                response.setMessage("轮播图不存在");
                return response;
            }
            
            // 删除指定配置
            boolean removed = configs.removeIf(config -> config.getVid().equals(vid));
            
            if (!removed) {
                response.setCode(404);
                response.setMessage("轮播图不存在");
                return response;
            }
            
            // 保存到Redis
            redisUtil.delValue(CAROUSEL_KEY);
            if (!configs.isEmpty()) {
                redisUtil.setAllList(CAROUSEL_KEY, configs);
            }
            
            response.setMessage("删除成功");
        } catch (Exception e) {
            log.error("删除轮播图失败", e);
            response.setCode(500);
            response.setMessage("删除失败");
        }
        
        return response;
    }
    
    /**
     * 管理端接口：初始化默认轮播图
     */
    @Override
    public CustomResponse initDefaultCarousels() {
        CustomResponse response = new CustomResponse();
        
        try {
            // 检查是否已有配置
            List<CarouselConfig> existingConfigs = redisUtil.getAllList(CAROUSEL_KEY, CarouselConfig.class);
            if (existingConfigs != null && !existingConfigs.isEmpty()) {
                response.setMessage("已存在轮播图配置，无需初始化");
                return response;
            }
            
            // 根据原JSON文件的视频ID初始化
            List<CarouselConfig> configs = new ArrayList<>();
            
            // 原JSON中的视频ID: 15, 2, 20, 4, 10, 28
            int[][] defaultData = {
                {15, 0xca8d6b, 1},
                {2, 0x5894d4, 2},
                {20, 0x836e61, 3},
                {4, 0x728cb4, 4},
                {10, 0x564e3e, 5},
                {28, 0x724b50, 6}
            };
            
            for (int[] data : defaultData) {
                Integer vid = data[0];
                String color = String.format("#%06x", data[1]);
                Integer sort = data[2];
                
                Video video = videoMapper.selectById(vid);
                if (video != null) {
                    CarouselConfig config = new CarouselConfig();
                    config.setVid(vid);
                    config.setUrl(video.getCoverUrl());
                    config.setTitle(video.getTitle());
                    config.setColor(color);
                    config.setTarget("/video/" + vid);
                    config.setSort(sort);
                    config.setStatus(1);
                    configs.add(config);
                }
            }
            
            if (!configs.isEmpty()) {
                redisUtil.delValue(CAROUSEL_KEY);
                redisUtil.setAllList(CAROUSEL_KEY, configs);
                response.setMessage("初始化成功，共添加" + configs.size() + "个轮播图");
            } else {
                response.setMessage("未找到对应的视频，初始化失败");
            }
            
        } catch (Exception e) {
            log.error("初始化轮播图失败", e);
            response.setCode(500);
            response.setMessage("初始化失败");
        }
        
        return response;
    }
}

