package com.teriteri.backend.service.carousel;

import com.teriteri.backend.pojo.CarouselConfig;
import com.teriteri.backend.pojo.CustomResponse;

import java.util.List;

public interface CarouselService {
    // 客户端接口：获取启用状态的轮播图列表
    CustomResponse getEnabledCarousels();
    
    // 管理端接口：获取所有轮播图配置
    CustomResponse getAllCarousels();
    
    // 管理端接口：添加轮播图（基于视频ID）
    CustomResponse addCarousel(Integer vid, String color, Integer sort);
    
    // 管理端接口：更新轮播图配置
    CustomResponse updateCarousel(Integer vid, String color, Integer sort, Integer status);
    
    // 管理端接口：删除轮播图
    CustomResponse deleteCarousel(Integer vid);
    
    // 管理端接口：初始化默认轮播图（从现有JSON数据）
    CustomResponse initDefaultCarousels();
}

