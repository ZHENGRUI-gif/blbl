<template>
    <div class="message-love">
        <!-- 操作栏 -->
        <div class="action-bar" v-if="likeList.length > 0">
            <div class="action-info">
                <span class="total-count">共收到 {{ likeList.length }} 个点赞</span>
            </div>
            <div class="action-buttons">
                <button 
                    class="organize-btn" 
                    :class="{ 'active': isOrganized }"
                    @click="toggleOrganize"
                >
                    <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M3 6h18v2H3V6zm0 5h18v2H3v-2zm0 5h18v2H3v-2z"/>
                    </svg>
                    {{ isOrganized ? '取消整理' : '一键整理' }}
                </button>
            </div>
        </div>
        
        <div class="love-list" v-if="likeList.length > 0">
            <!-- 未整理状态：显示所有点赞记录 -->
            <template v-if="!isOrganized">
                <div class="love-item" v-for="(item, index) in likeList" :key="index">
                    <div class="love-card">
                        <!-- 用户信息和点赞操作 -->
                        <div class="love-header">
                            <div class="user-section">
                                <VAvatar :img="item.user.avatar" :size="48" :auth="item.user.auth"></VAvatar>
                                <div class="user-info">
                                    <div class="user-name" :class="item.user.vip !== 0 ? 'vip-name' : ''">
                                        {{ item.user.nickname }}
                                    </div>
                                    <div class="love-time">{{ handleDateTime(item.userVideo.loveTime) }}</div>
                                </div>
                            </div>
                            <div class="love-action">
                                <div class="love-icon">👍</div>
                                <span class="love-text">赞了你的视频</span>
                            </div>
                        </div>
                        
                        <!-- 视频信息 -->
                        <div class="video-section" @click="openVideo(item.video.vid)">
                            <div class="video-cover">
                                <img :src="item.video.coverUrl" :alt="item.video.title" />
                                <div class="video-duration">{{ handleDuration(item.video.duration) }}</div>
                                <div class="play-overlay">
                                    <svg class="play-icon" viewBox="0 0 24 24" fill="white">
                                        <path d="M8 5v14l11-7z"/>
                                    </svg>
                                </div>
                            </div>
                            <div class="video-info">
                                <h3 class="video-title">{{ item.video.title }}</h3>
                                <div class="video-stats">
                                    <span class="stat-item">
                                        <svg class="stat-icon" viewBox="0 0 24 24">
                                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                                        </svg>
                                        {{ handleNum(item.stats ? item.stats.play : 0) }}播放
                                    </span>
                                    <span class="stat-item">
                                        <svg class="stat-icon" viewBox="0 0 24 24">
                                            <path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"/>
                                        </svg>
                                        {{ handleNum(item.stats ? item.stats.good : 0) }}点赞
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
            
            <!-- 整理状态：按视频分组显示 -->
            <template v-else>
                <div class="grouped-item" v-for="(group, videoId) in groupedLikes" :key="videoId">
                    <div class="grouped-card">
                        <!-- 视频信息头部 -->
                        <div class="video-header" @click="openVideo(group.video.vid)">
                            <div class="video-cover">
                                <img :src="group.video.coverUrl" :alt="group.video.title" />
                                <div class="video-duration">{{ handleDuration(group.video.duration) }}</div>
                                <div class="play-overlay">
                                    <svg class="play-icon" viewBox="0 0 24 24" fill="white">
                                        <path d="M8 5v14l11-7z"/>
                                    </svg>
                                </div>
                            </div>
                            <div class="video-info">
                                <h3 class="video-title">{{ group.video.title }}</h3>
                                <div class="video-stats">
                                    <span class="stat-item">
                                        <svg class="stat-icon" viewBox="0 0 24 24">
                                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                                        </svg>
                                        {{ handleNum(group.stats ? group.stats.play : 0) }}播放
                                    </span>
                                    <span class="stat-item">
                                        <svg class="stat-icon" viewBox="0 0 24 24">
                                            <path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"/>
                                        </svg>
                                        {{ handleNum(group.stats ? group.stats.good : 0) }}点赞
                                    </span>
                                </div>
                            </div>
                        </div>
                        
                        <!-- 点赞用户列表 -->
                        <div class="likers-section">
                            <div class="likers-header">
                                <span class="likers-count">{{ group.likes.length }} 人点赞</span>
                                <button 
                                    class="toggle-btn" 
                                    @click="toggleGroup(videoId)"
                                    :class="{ 'expanded': expandedGroups[videoId] }"
                                >
                                    <svg class="toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M7 10l5 5 5-5z"/>
                                    </svg>
                                </button>
                            </div>
                            
                            <div class="likers-list" v-show="expandedGroups[videoId]">
                                <div class="liker-item" v-for="(like, index) in group.likes" :key="index">
                                    <VAvatar :img="like.user.avatar" :size="32" :auth="like.user.auth"></VAvatar>
                                    <div class="liker-info">
                                        <div class="liker-name" :class="like.user.vip !== 0 ? 'vip-name' : ''">
                                            {{ like.user.nickname }}
                                        </div>
                                        <div class="liker-time">{{ handleDateTime(like.userVideo.loveTime) }}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </div>
        
        <!-- 空状态 -->
        <div class="empty-state" v-else-if="!loading">
            <div class="empty-img"></div>
            <div class="empty-text">还没有收到点赞</div>
            <div class="empty-tip">发布优质内容，获得更多点赞吧！</div>
        </div>
        
        <!-- 加载中 -->
        <div class="loading" v-if="loading">
            <div class="loading-text">加载中...</div>
        </div>
        
        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore && !loading && likeList.length > 0">
            <button @click="loadMore" class="load-more-btn">加载更多</button>
        </div>
    </div>
</template>

<script>
import VAvatar from '@/components/avatar/VAvatar.vue';
import { handleNum, handleTime, handleDateTime } from '@/utils/utils.js';

export default {
    name: "MessageLove",
    components: {
        VAvatar,
    },
    data() {
        return {
            likeList: [],
            loading: false,
            hasMore: true,
            offset: 0,
            quantity: 10,
            isOrganized: false, // 是否已整理
            expandedGroups: {}, // 展开的分组
        }
    },
    computed: {
        // 按视频分组的点赞数据
        groupedLikes() {
            const groups = {};
            this.likeList.forEach(item => {
                const videoId = item.video.vid;
                if (!groups[videoId]) {
                    groups[videoId] = {
                        video: item.video,
                        stats: item.stats,
                        likes: []
                    };
                }
                groups[videoId].likes.push(item);
            });
            
            // 按点赞时间排序
            Object.keys(groups).forEach(videoId => {
                groups[videoId].likes.sort((a, b) => 
                    new Date(b.userVideo.loveTime) - new Date(a.userVideo.loveTime)
                );
            });
            
            return groups;
        }
    },
    methods: {
        // 清除未读消息
        async clearUnread() {
            const formData = new FormData();
            formData.append("column", "love");
            await this.$post("/msg-unread/clear", formData, {
                headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
            })
        },
        
        // 获取收到的点赞列表
        async getReceivedLikes() {
            if (this.loading || !this.hasMore) return;
            
            this.loading = true;
            console.log('开始获取收到的点赞，offset:', this.offset, 'quantity:', this.quantity);
            try {
                const res = await this.$get("/video/received-likes", {
                    params: {
                        offset: this.offset,
                        quantity: this.quantity
                    },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token")
                    }
                });
                
                if (res.data && res.data.data) {
                    const newLikes = res.data.data;
                    if (newLikes.length < this.quantity) {
                        this.hasMore = false;
                    }
                    this.likeList = this.likeList.concat(newLikes);
                    this.offset += newLikes.length;
                }
            } catch (error) {
                console.error('获取收到的点赞失败:', error);
            } finally {
                this.loading = false;
            }
        },
        
        // 加载更多
        async loadMore() {
            await this.getReceivedLikes();
        },
        
        // 打开视频
        openVideo(vid) {
            window.open(`/video/${vid}`, '_blank');
        },
        
        // 切换整理状态
        toggleOrganize() {
            this.isOrganized = !this.isOrganized;
            if (this.isOrganized) {
                // 整理时，默认展开所有分组
                this.expandedGroups = {};
                Object.keys(this.groupedLikes).forEach(videoId => {
                    this.expandedGroups[videoId] = true;
                });
            } else {
                // 取消整理时，清空展开状态
                this.expandedGroups = {};
            }
        },
        
        // 切换分组展开/收起
        toggleGroup(videoId) {
            this.expandedGroups[videoId] = !this.expandedGroups[videoId];
        },
        
        // 工具函数
        handleNum: handleNum,
        handleTime: handleTime,
        handleDateTime: handleDateTime,
        
        // 处理时长显示
        handleDuration(time) {
            return handleTime(time);
        },
    },
    async created() {
        await this.clearUnread();
        await this.getReceivedLikes();
    }
}
</script>

<style scoped>
.message-love {
    height: calc(100vh - 138px);
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 4px 0 rgba(121,146,185,0.54);
    overflow-y: auto;
}

/* 操作栏样式 */
.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 1px solid #e9ecef;
    border-radius: 4px 4px 0 0;
}

.action-info {
    display: flex;
    align-items: center;
}

.total-count {
    font-size: 14px;
    color: #666;
    font-weight: 500;
}

.action-buttons {
    display: flex;
    gap: 12px;
}

.organize-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    background: linear-gradient(135deg, #6c757d 0%, #495057 100%);
    color: white;
    border: none;
    border-radius: 20px;
    padding: 8px 16px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(108, 117, 125, 0.3);
}

.organize-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(108, 117, 125, 0.4);
}

.organize-btn.active {
    background: linear-gradient(135deg, var(--brand_pink) 0%, #ff4d7a 100%);
    box-shadow: 0 2px 8px rgba(255, 107, 157, 0.3);
}

.organize-btn.active:hover {
    box-shadow: 0 4px 12px rgba(255, 107, 157, 0.4);
}

.btn-icon {
    width: 14px;
    height: 14px;
}

.love-list {
    padding: 24px;
}

.love-item {
    margin-bottom: 20px;
}

.love-item:last-child {
    margin-bottom: 0;
}

.love-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    transition: all 0.3s ease;
    border: 1px solid #f0f0f0;
}

.love-card:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
}

.love-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 1px solid #e9ecef;
}

.user-section {
    display: flex;
    align-items: center;
}

.user-info {
    margin-left: 16px;
}

.user-name {
    font-size: 16px;
    color: #333;
    font-weight: 600;
    margin-bottom: 4px;
    line-height: 1.2;
}

.user-name.vip-name {
    color: var(--brand_pink);
    position: relative;
}

.user-name.vip-name::after {
    content: "👑";
    margin-left: 4px;
    font-size: 12px;
}

.love-time {
    font-size: 13px;
    color: #666;
    font-weight: 400;
}

.love-action {
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
    color: white;
    padding: 8px 16px;
    border-radius: 20px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(255, 107, 157, 0.3);
}

.love-icon {
    font-size: 16px;
    margin-right: 6px;
}

.love-text {
    font-size: 14px;
}

.video-section {
    display: flex;
    align-items: flex-start;
    padding: 20px 24px;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

.video-section:hover {
    background-color: #f8f9fa;
}

.video-cover {
    position: relative;
    width: 160px;
    height: 100px;
    border-radius: 8px;
    overflow: hidden;
    margin-right: 16px;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.video-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.video-section:hover .video-cover img {
    transform: scale(1.05);
}

.video-duration {
    position: absolute;
    bottom: 6px;
    right: 6px;
    background-color: rgba(0, 0, 0, 0.8);
    color: #fff;
    font-size: 12px;
    padding: 2px 6px;
    border-radius: 4px;
    font-weight: 500;
}

.play-overlay {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 40px;
    height: 40px;
    background-color: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.video-section:hover .play-overlay {
    opacity: 1;
}

.play-icon {
    width: 20px;
    height: 20px;
    margin-left: 2px;
}

.video-info {
    flex: 1;
    min-width: 0;
}

.video-title {
    font-size: 16px;
    color: #333;
    font-weight: 600;
    margin: 0 0 12px 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: color 0.2s ease;
}

.video-section:hover .video-title {
    color: var(--brand_pink);
}

.video-stats {
    display: flex;
    align-items: center;
    gap: 16px;
}

.stat-item {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #666;
    font-weight: 500;
}

.stat-icon {
    width: 14px;
    height: 14px;
    margin-right: 4px;
    fill: #999;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 400px;
    color: #999;
    padding: 40px;
}

.empty-img {
    width: 200px;
    height: 150px;
    background-image: url('~assets/img/bilibili/no_message.png');
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    margin-bottom: 24px;
    opacity: 0.6;
}

.empty-text {
    font-size: 18px;
    color: #666;
    margin-bottom: 8px;
    font-weight: 500;
}

.empty-tip {
    font-size: 14px;
    color: #999;
    text-align: center;
    line-height: 1.5;
}

.loading {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
}

.loading-text {
    color: #999;
    font-size: 14px;
    display: flex;
    align-items: center;
}

.loading-text::before {
    content: "";
    width: 16px;
    height: 16px;
    border: 2px solid #f3f3f3;
    border-top: 2px solid var(--brand_pink);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-right: 8px;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.load-more {
    display: flex;
    justify-content: center;
    padding: 24px;
}

.load-more-btn {
    background: linear-gradient(135deg, var(--brand_pink) 0%, #ff4d7a 100%);
    color: #fff;
    border: none;
    border-radius: 24px;
    padding: 12px 32px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(255, 107, 157, 0.3);
}

.load-more-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(255, 107, 157, 0.4);
}

.load-more-btn:disabled {
    background: #ccc;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
}

/* 分组显示样式 */
.grouped-item {
    margin-bottom: 20px;
}

.grouped-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    transition: all 0.3s ease;
    border: 1px solid #f0f0f0;
}

.grouped-card:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
}

.video-header {
    display: flex;
    align-items: flex-start;
    padding: 20px 24px;
    cursor: pointer;
    transition: background-color 0.2s ease;
    border-bottom: 1px solid #f8f9fa;
}

.video-header:hover {
    background-color: #f8f9fa;
}

.likers-section {
    background-color: #fafbfc;
    border-top: 1px solid #e9ecef;
}

.likers-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 24px;
    background-color: #f8f9fa;
    border-bottom: 1px solid #e9ecef;
}

.likers-count {
    font-size: 14px;
    color: #666;
    font-weight: 500;
}

.toggle-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    background: none;
    border: none;
    border-radius: 50%;
    cursor: pointer;
    transition: all 0.2s ease;
    color: #666;
}

.toggle-btn:hover {
    background-color: #e9ecef;
    color: #333;
}

.toggle-btn.expanded {
    transform: rotate(180deg);
    color: var(--brand_pink);
}

.toggle-icon {
    width: 16px;
    height: 16px;
    transition: transform 0.2s ease;
}

.likers-list {
    padding: 0 24px 16px;
}

.liker-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
}

.liker-item:last-child {
    border-bottom: none;
}

.liker-info {
    margin-left: 12px;
    flex: 1;
}

.liker-name {
    font-size: 14px;
    color: #333;
    font-weight: 500;
    margin-bottom: 2px;
}

.liker-name.vip-name {
    color: var(--brand_pink);
}

.liker-time {
    font-size: 12px;
    color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .action-bar {
        padding: 12px 16px;
        flex-direction: column;
        gap: 12px;
        align-items: stretch;
    }
    
    .action-buttons {
        justify-content: center;
    }
    
    .love-list {
        padding: 16px;
    }
    
    .love-header {
        padding: 16px 20px;
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
    }
    
    .video-section {
        padding: 16px 20px;
        flex-direction: column;
    }
    
    .video-cover {
        width: 100%;
        height: 180px;
        margin-right: 0;
        margin-bottom: 12px;
    }
    
    .video-stats {
        flex-wrap: wrap;
        gap: 12px;
    }
    
    .video-header {
        padding: 16px 20px;
        flex-direction: column;
    }
    
    .likers-header {
        padding: 12px 20px;
    }
    
    .likers-list {
        padding: 0 20px 16px;
    }
}
</style>