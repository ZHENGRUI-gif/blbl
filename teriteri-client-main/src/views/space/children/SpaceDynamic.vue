<template>
    <div class="space-dynamic">
        <div class="dynamic-container">
            <!-- 左侧筛选栏 -->
            <div class="dynamic-sidebar">
                <div class="sidebar-title">动态</div>
                <ul class="filter-list">
                    <li class="filter-item" :class="{'active': currentFilter === 'all'}" @click="changeFilter('all')">
                        <span>全部</span>
                    </li>
                    <li class="filter-item" :class="{'active': currentFilter === 'video'}" @click="changeFilter('video')">
                        <span>视频</span>
                    </li>
                    <li class="filter-item" :class="{'active': currentFilter === 'article'}" @click="changeFilter('article')">
                        <span>专栏</span>
                    </li>
                    <li class="filter-item" :class="{'active': currentFilter === 'dynamic'}" @click="changeFilter('dynamic')">
                        <span>动态</span>
                    </li>
                </ul>
            </div>

            <!-- 右侧动态内容 -->
            <div class="dynamic-content">
                <div class="dynamic-list">
                    <!-- 空状态提示 -->
                    <div v-if="dynamicList.length === 0" class="empty-state">
                        <p>暂无动态数据</p>
                    </div>
                    <!-- 动态条目 -->
                    <div class="dynamic-item" v-for="item in dynamicList" :key="item.id">
                        <!-- 用户信息 -->
                        <div class="user-info">
                            <VAvatar :img="item.user.avatar_url" :size="40" :auth="item.user.auth || 0"></VAvatar>
                            <div class="user-details">
                                <div class="username">{{ item.user.username }}</div>
                                <div class="action-time">{{ item.actionTime }} · {{ item.action }}</div>
                            </div>
                        </div>

                        <!-- 动态内容 -->
                        <div class="dynamic-body">
                            <!-- 视频动态 -->
                            <div v-if="item.type === 'video'" class="video-dynamic">
                                <a :href="`/video/${item.video.vid}`" target="_blank" class="video-thumbnail">
                                    <img :src="item.video.thumbnail" :alt="item.video.title">
                                    <div class="video-duration">{{ item.video.duration }}</div>
                                    <div class="video-title-overlay">
                                        <h3 class="video-title-text">
                                            {{ item.video.title }}
                                        </h3>
                                    </div>
                                </a>
                                <div class="video-info">
                                    <p class="video-desc" v-if="item.video.description">{{ item.video.description }}</p>
                                    <div class="video-stats">
                                        <span class="stat-item">
                                            <i class="icon-play"></i>
                                            {{ formatNumber(item.video.playCount) }}
                                        </span>
                                        <span class="stat-item">
                                            <i class="icon-comment"></i>
                                            {{ item.video.commentCount }}
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <!-- 动态视频 -->
                            <div v-else-if="item.type === 'dynamic_video'" class="dynamic-video">
                                <div class="dynamic-badge">动态视频</div>
                                <div class="dynamic-thumbnail">
                                    <img :src="item.dynamic.thumbnail" :alt="item.dynamic.title">
                                </div>
                                <div class="dynamic-info">
                                    <h3 class="dynamic-title">{{ item.dynamic.title }}</h3>
                                    <p class="dynamic-desc">{{ item.dynamic.description }}</p>
                                </div>
                            </div>

                            <!-- 普通动态 -->
                            <div v-else class="text-dynamic">
                                <p class="dynamic-text">{{ item.content }}</p>
                                <div v-if="item.images && item.images.length" class="dynamic-images">
                                    <img v-for="(img, index) in item.images" :key="index" :src="img" class="dynamic-image">
                                </div>
                            </div>
                        </div>

                        <!-- 互动区域 -->
                        <div class="dynamic-footer">
                            <div class="interaction-stats">
                                <span class="stat-item">
                                    <i class="iconfont icon-dianzan"></i>
                                    {{ formatNumber(item.likeCount) }}
                                </span>
                                <span class="stat-item">
                                    <i class="iconfont icon-pinglun"></i>
                                    {{ item.commentCount }}
                                </span>
                                <span class="stat-item">
                                    <i class="iconfont icon-zhuanfa"></i>
                                    {{ item.shareCount }}
                                </span>
                            </div>
                            <div class="dynamic-meta">
                                <span class="dynamic-id">#{{ item.id }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- 加载更多 -->
                    <div class="load-more" v-if="hasMore" @click="loadMore">
                        <span>加载更多</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import VAvatar from '@/components/avatar/VAvatar.vue'

export default {
    name: "SpaceDynamic",
    components: {
        VAvatar
    },
    data() {
        return {
            currentFilter: 'all',
            dynamicList: [],
            hasMore: true,
            page: 1,
            pageSize: 10
        }
    },
    computed: {
        uid() {
            return Number(this.$route.params.uid);
        },
        // 当前登录用户信息
        currentUser() {
            return this.$store.state.user;
        }
    },
    mounted() {
        this.loadDynamicList();
    },
    methods: {
        // 切换筛选条件
        changeFilter(filter) {
            this.currentFilter = filter;
            this.page = 1;
            this.dynamicList = [];
            this.loadDynamicList();
        },

        // 加载动态列表
        async loadDynamicList() {
            try {
                // 调用后端API获取用户的视频投稿数据
                const res = await this.$get("/video/user-works", {
                    params: {
                        uid: this.uid,
                        rule: 1, // 按时间排序
                        page: this.page,
                        quantity: this.pageSize
                    }
                });
                
                if (res && res.data && res.data.data && res.data.data.list) {
                    const videoList = res.data.data.list;
                    console.log('API返回的视频数据:', videoList);
                    const dynamicData = this.convertVideosToDynamics(videoList);
                    console.log('转换后的动态数据:', dynamicData);
                    this.dynamicList = [...this.dynamicList, ...dynamicData];
                    this.hasMore = videoList.length === this.pageSize;
                } else {
                    console.log('API返回数据为空');
                    this.dynamicList = [];
                    this.hasMore = false;
                }
            } catch (error) {
                console.error('加载动态列表失败:', error);
                this.dynamicList = [];
                this.hasMore = false;
            }
        },

        // 加载更多
        loadMore() {
            this.page++;
            this.loadDynamicList();
        },

        // 格式化数字
        formatNumber(num) {
            if (num >= 10000) {
                return (num / 10000).toFixed(1) + '万';
            }
            return num.toString();
        },

        // 将视频数据转换为动态数据格式
        convertVideosToDynamics(videoList) {
            return videoList.map(video => {
                // 确保数据结构正确
                const videoData = video.video || video;
                const statsData = video.stats || {};
                const userData = video.user || {};
                
                return {
                    id: `video_${videoData.vid}`,
                    type: 'video',
                    user: {
                        username: userData.nickname || '用户',
                        avatar_url: userData.avatar_url || '',
                        auth: userData.auth || 0
                    },
                    actionTime: this.formatTime(videoData.uploadDate),
                    action: '投稿了视频',
                    video: {
                        title: videoData.title || '无标题',
                        description: videoData.descr || '', // 使用数据库中的descr字段
                        thumbnail: videoData.coverUrl || '',
                        duration: this.formatDuration(videoData.duration),
                        vid: videoData.vid,
                        playCount: statsData.play || 0,
                        commentCount: statsData.comment || 0
                    },
                    likeCount: statsData.good || 0,
                    commentCount: statsData.comment || 0,
                    shareCount: 0 // 分享数暂时设为0
                };
            });
        },

        // 格式化时间
        formatTime(dateString) {
            if (!dateString) return '刚刚';
            
            try {
                const date = new Date(dateString);
                if (isNaN(date.getTime())) return '刚刚';
                
                const now = new Date();
                const diff = now - date;
                
                const days = Math.floor(diff / (1000 * 60 * 60 * 24));
                const hours = Math.floor(diff / (1000 * 60 * 60));
                const minutes = Math.floor(diff / (1000 * 60));
                
                if (days > 0) return days + '天前';
                if (hours > 0) return hours + '小时前';
                if (minutes > 0) return minutes + '分钟前';
                return '刚刚';
            } catch (error) {
                return '刚刚';
            }
        },

        // 格式化视频时长
        formatDuration(duration) {
            if (!duration || duration <= 0) return '00:00';
            
            const totalSeconds = Math.floor(duration);
            const minutes = Math.floor(totalSeconds / 60);
            const seconds = totalSeconds % 60;
            
            return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        },

    }
}
</script>

<style scoped>
.space-dynamic {
    padding: 20px;
    background: #fff;
    min-height: 600px;
}

.dynamic-container {
    display: flex;
    max-width: 1200px;
    margin: 0 auto;
}

/* 左侧筛选栏 */
.dynamic-sidebar {
    width: 200px;
    margin-right: 20px;
}

.sidebar-title {
    font-size: 18px;
    font-weight: 600;
    color: #222;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e5e5e5;
}

.filter-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.filter-item {
    padding: 12px 16px;
    cursor: pointer;
    border-radius: 6px;
    margin-bottom: 4px;
    transition: all 0.3s;
    color: #666;
}

.filter-item:hover {
    background: #f5f5f5;
    color: #00aeec;
}

.filter-item.active {
    background: #00aeec;
    color: #fff;
}

/* 右侧动态内容 */
.dynamic-content {
    flex: 1;
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #999;
    font-size: 16px;
}

.dynamic-list {
    max-width: 800px;
}

/* 动态条目 */
.dynamic-item {
    border-bottom: 1px solid #f0f0f0;
    padding: 20px 0;
}

.dynamic-item:last-child {
    border-bottom: none;
}

/* 用户信息 */
.user-info {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
}

.user-details {
    flex: 1;
    margin-left: 12px;
}

.username {
    font-size: 16px;
    font-weight: 500;
    color: #222;
    margin-bottom: 4px;
}

.action-time {
    font-size: 12px;
    color: #999;
}

/* 动态内容 */
.dynamic-body {
    margin-bottom: 15px;
}

/* 视频动态 */
.video-dynamic {
    display: flex;
    background: #f8f9fa;
    border-radius: 8px;
    padding: 12px;
}

.video-thumbnail {
    position: relative;
    width: 200px;
    height: 120px;
    border-radius: 6px;
    overflow: hidden;
    margin-right: 12px;
    flex-shrink: 0;
    display: block;
    text-decoration: none;
    transition: transform 0.2s;
}

.video-thumbnail:hover {
    transform: scale(1.02);
}

.video-thumbnail img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.video-duration {
    position: absolute;
    bottom: 50px;
    right: 6px;
    background: rgba(0, 0, 0, 0.7);
    color: #fff;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
    z-index: 10;
}

.video-title-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, rgba(0, 0, 0, 0.4) 50%, transparent 100%);
    display: flex;
    align-items: flex-end;
    padding: 8px;
    box-sizing: border-box;
    min-height: 40px;
}

.video-title-text {
    color: #fff;
    font-size: 14px;
    font-weight: 500;
    line-height: 1.3;
    margin: 0;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.video-info {
    flex: 1;
}


.video-desc {
    font-size: 16px;
    font-weight: 500;
    color: #222;
    margin: 0 0 8px 0;
    line-height: 1.4;
}

.video-stats {
    display: flex;
    gap: 16px;
}

.stat-item {
    font-size: 12px;
    color: #999;
    display: flex;
    align-items: center;
    gap: 4px;
}

/* 动态视频 */
.dynamic-video {
    display: flex;
    align-items: center;
    background: #f8f9fa;
    border-radius: 8px;
    padding: 12px;
}

.dynamic-badge {
    background: #ff69b4;
    color: #fff;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    margin-right: 12px;
    flex-shrink: 0;
}

.dynamic-thumbnail {
    width: 100px;
    height: 100px;
    border-radius: 6px;
    overflow: hidden;
    margin-right: 12px;
    flex-shrink: 0;
}

.dynamic-thumbnail img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.dynamic-info {
    flex: 1;
}

.dynamic-title {
    font-size: 16px;
    font-weight: 500;
    color: #222;
    margin: 0 0 8px 0;
}

.dynamic-desc {
    font-size: 14px;
    color: #666;
    margin: 0;
}

/* 普通动态 */
.text-dynamic {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 12px;
}

.dynamic-text {
    font-size: 16px;
    color: #222;
    line-height: 1.6;
    margin: 0 0 12px 0;
}

.dynamic-images {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.dynamic-image {
    width: 120px;
    height: 120px;
    border-radius: 6px;
    object-fit: cover;
}

/* 互动区域 */
.dynamic-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.interaction-stats {
    display: flex;
    gap: 20px;
}

.dynamic-meta {
    display: flex;
    align-items: center;
    gap: 8px;
}

.dynamic-id {
    background: #f0f0f0;
    color: #666;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
}

/* 加载更多 */
.load-more {
    text-align: center;
    padding: 20px;
    color: #00aeec;
    cursor: pointer;
    border: 1px dashed #e5e5e5;
    border-radius: 8px;
    margin-top: 20px;
    transition: all 0.3s;
}

.load-more:hover {
    background: #f8f9fa;
    border-color: #00aeec;
}

/* 图标样式 */
.icon-play::before { content: '▶'; }

/* 互动统计图标样式 */
.interaction-stats .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #666;
    font-size: 13px;
}

.interaction-stats .stat-item .iconfont {
    font-size: 16px;
    color: #999;
}

.interaction-stats .stat-item:hover .iconfont {
    color: var(--brand_pink);
}
</style>