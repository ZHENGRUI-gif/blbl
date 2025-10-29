package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 轮播图配置类（不对应数据库表，存储在Redis中）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarouselConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer vid;        // 视频ID
    private String url;         // 轮播图图片地址（使用视频封面）
    private String title;       // 轮播图标题（使用视频标题）
    private String color;       // 轮播图底色
    private String target;      // 跳转链接（/video/{vid}）
    private Integer sort;       // 排序顺序，数字越小越靠前
    private Integer status;     // 状态 0-禁用 1-启用
}

