package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.carousel.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/carousel")
public class CarouselController {
    
    @Autowired
    private CarouselService carouselService;
    
    /**
     * 客户端接口：获取启用状态的轮播图列表
     */
    @GetMapping("/list")
    public CustomResponse getEnabledCarousels() {
        return carouselService.getEnabledCarousels();
    }
    
    /**
     * 管理端接口：获取所有轮播图配置
     */
    @GetMapping("/admin/list")
    public CustomResponse getAllCarousels() {
        return carouselService.getAllCarousels();
    }
    
    /**
     * 管理端接口：添加轮播图
     */
    @PostMapping("/admin/add")
    public CustomResponse addCarousel(@RequestBody Map<String, Object> params) {
        Integer vid = (Integer) params.get("vid");
        String color = (String) params.get("color");
        Integer sort = (Integer) params.get("sort");
        return carouselService.addCarousel(vid, color, sort);
    }
    
    /**
     * 管理端接口：更新轮播图配置
     */
    @PutMapping("/admin/update")
    public CustomResponse updateCarousel(@RequestBody Map<String, Object> params) {
        Integer vid = (Integer) params.get("vid");
        String color = (String) params.get("color");
        Integer sort = (Integer) params.get("sort");
        Integer status = (Integer) params.get("status");
        return carouselService.updateCarousel(vid, color, sort, status);
    }
    
    /**
     * 管理端接口：删除轮播图
     */
    @DeleteMapping("/admin/delete/{vid}")
    public CustomResponse deleteCarousel(@PathVariable Integer vid) {
        return carouselService.deleteCarousel(vid);
    }
    
    /**
     * 管理端接口：初始化默认轮播图
     */
    @PostMapping("/admin/init")
    public CustomResponse initDefaultCarousels() {
        return carouselService.initDefaultCarousels();
    }
}

