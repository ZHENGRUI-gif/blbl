<template>
    <div class="category-view">
        <!-- 分类导航 -->
        <div class="category-nav">
            <div class="nav-container">
                <div class="nav-item" 
                     :class="{ active: currentCategory && currentCategory.mcId === item.mcId }"
                     v-for="item in channels" 
                     :key="item.mcId"
                     @click="changeMainCategory(item)">
                    {{ item.mcName }}
                </div>
            </div>
        </div>

        <!-- 子分类导航 -->
        <div class="sub-category-nav" v-if="currentCategory && currentCategory.scList && currentCategory.scList.length > 0">
            <div class="sub-nav-container">
                <div class="sub-nav-item" 
                     :class="{ active: currentSubCategory && currentSubCategory.scId === item.scId }"
                     v-for="item in currentCategory.scList" 
                     :key="item.scId"
                     @click="changeSubCategory(item)">
                    {{ item.scName }}
                </div>
            </div>
        </div>

        <!-- 排序选项 -->
        <div class="sort-options">
            <div class="sort-container">
                <div class="sort-item" 
                     :class="{ active: sortRule === 1 }"
                     @click="changeSortRule(1)">
                    最新投稿
                </div>
                <div class="sort-item" 
                     :class="{ active: sortRule === 2 }"
                     @click="changeSortRule(2)">
                    最多播放
                </div>
                <div class="sort-item" 
                     :class="{ active: sortRule === 3 }"
                     @click="changeSortRule(3)">
                    最多点赞
                </div>
            </div>
        </div>

        <!-- 视频列表 -->
        <div class="video-list">
            <div class="video-grid" v-if="videoList.length > 0">
                <div class="video-item" 
                     v-for="item in videoList" 
                     :key="item.video.vid"
                     @click="openVideo(item.video.vid)">
                    <div class="video-cover">
                        <img :src="item.video.coverUrl" :alt="item.video.title" />
                        <div class="video-duration">{{ handleDuration(item.video.duration) }}</div>
                    </div>
                    <div class="video-info">
                        <h3 class="video-title">{{ item.video.title }}</h3>
                        <div class="video-meta">
                            <span class="video-author">{{ item.user.nickname }}</span>
                            <span class="video-stats">
                                {{ handleNum(item.stats.play) }}播放 · {{ handleNum(item.stats.good) }}点赞
                            </span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="empty-state" v-else-if="!loading">
                <p>该分类下暂无视频</p>
            </div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore && !loading">
            <button @click="loadMore">加载更多</button>
        </div>

        <!-- 加载中 -->
        <div class="loading" v-if="loading">
            <p>加载中...</p>
        </div>
    </div>
</template>

<script>
import { handleNum, handleTime } from '@/utils/utils.js';

export default {
    name: "CategoryView",
    data() {
        return {
            currentCategory: null,
            currentSubCategory: null,
            sortRule: 1, // 1: 最新投稿, 2: 最多播放, 3: 最多点赞
            videoList: [],
            loading: false,
            hasMore: true,
            currentPage: 1,
            pageSize: 20
        }
    },
    computed: {
        channels() {
            return this.$store.state.channels;
        }
    },
    watch: {
        '$route'() {
            this.initCategory();
        }
    },
    async created() {
        // 确保channels数据已加载
        if (this.channels.length === 0) {
            await this.getChannels();
        }
        this.initCategory();
    },
    methods: {
        // 获取频道列表
        async getChannels() {
            try {
                let res = await this.$get("/category/getall");
                this.$store.commit("updateChannels", res.data.data);
            } catch (error) {
                console.error('获取频道列表失败:', error);
            }
        },

        // 初始化分类
        initCategory() {
            const mcId = this.$route.params.mcId;
            const scId = this.$route.params.scId;
            
            // 查找主分类
            this.currentCategory = this.channels.find(item => item.mcId === mcId);
            if (!this.currentCategory) {
                this.$router.push('/');
                return;
            }
            
            // 查找子分类
            if (scId && this.currentCategory.scList) {
                this.currentSubCategory = this.currentCategory.scList.find(item => item.scId === scId);
            } else {
                this.currentSubCategory = null;
            }
            
            this.loadVideos();
        },

        // 切换主分类
        changeMainCategory(category) {
            this.currentCategory = category;
            this.currentSubCategory = null;
            this.$router.push(`/v/${category.mcId}`);
        },

        // 切换子分类
        changeSubCategory(subCategory) {
            this.currentSubCategory = subCategory;
            this.$router.push(`/v/${this.currentCategory.mcId}/${subCategory.scId}`);
        },

        // 切换排序规则
        changeSortRule(rule) {
            this.sortRule = rule;
            this.currentPage = 1;
            this.videoList = [];
            this.loadVideos();
        },

        // 加载视频列表
        async loadVideos() {
            if (this.loading) return;
            
            this.loading = true;
            try {
                const params = {
                    mcId: this.currentCategory.mcId,
                    rule: this.sortRule,
                    page: this.currentPage,
                    quantity: this.pageSize
                };
                
                if (this.currentSubCategory) {
                    params.scId = this.currentSubCategory.scId;
                }
                
                const res = await this.$get("/video/category", { params });
                if (res && res.data && res.data.data) {
                    const newVideos = res.data.data.list || [];
                    if (this.currentPage === 1) {
                        this.videoList = newVideos;
                    } else {
                        this.videoList.push(...newVideos);
                    }
                    this.hasMore = newVideos.length === this.pageSize;
                } else {
                    // 如果没有数据，显示空状态
                    if (this.currentPage === 1) {
                        this.videoList = [];
                    }
                    this.hasMore = false;
                }
            } catch (error) {
                console.error('加载视频列表失败:', error);
                // 如果是第一页，显示空状态
                if (this.currentPage === 1) {
                    this.videoList = [];
                }
                this.hasMore = false;
            } finally {
                this.loading = false;
            }
        },

        // 加载更多
        loadMore() {
            this.currentPage++;
            this.loadVideos();
        },

        // 打开视频详情
        openVideo(vid) {
            this.$router.push(`/video/${vid}`);
        },

        // 处理数字显示
        handleNum(number) {
            return handleNum(number);
        },

        // 处理时长显示
        handleDuration(time) {
            return handleTime(time);
        }
    }
}
</script>

<style scoped>
.category-view {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.category-nav {
    margin-bottom: 20px;
}

.nav-container {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.nav-item {
    padding: 8px 16px;
    border-radius: 4px;
    background-color: transparent;
    color: var(--text2);
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid transparent;
}

.nav-item:hover {
    background-color: var(--graph_bg_thin);
    color: var(--text1);
}

.nav-item.active {
    background-color: #00aeec;
    color: white;
    border-color: #00aeec;
}

.sub-category-nav {
    margin-bottom: 20px;
}

.sub-nav-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.sub-nav-item {
    padding: 6px 12px;
    border-radius: 4px;
    background-color: transparent;
    color: var(--text2);
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;
    border: 1px solid transparent;
}

.sub-nav-item:hover {
    background-color: var(--graph_bg_thin);
    color: var(--text1);
}

.sub-nav-item.active {
    background-color: #00aeec;
    color: white;
    border-color: #00aeec;
}

.sort-options {
    margin-bottom: 20px;
}

.sort-container {
    display: flex;
    gap: 10px;
}

.sort-item {
    padding: 6px 12px;
    border-radius: 4px;
    background-color: var(--graph_bg_thin);
    color: var(--text2);
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;
}

.sort-item:hover {
    background-color: var(--graph_bg_thick);
    color: var(--text1);
}

.sort-item.active {
    background-color: var(--brand_pink);
    color: white;
}

.video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
}

.video-item {
    cursor: pointer;
    transition: transform 0.3s;
}

.video-item:hover {
    transform: translateY(-2px);
}

.video-cover {
    position: relative;
    width: 100%;
    height: 180px;
    border-radius: 8px;
    overflow: hidden;
    background-color: var(--graph_bg_thin);
}

.video-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.video-duration {
    position: absolute;
    bottom: 8px;
    right: 8px;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
}

.video-info {
    padding: 12px 0;
}

.video-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--text1);
    margin: 0 0 8px 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.video-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: var(--text2);
}

.video-author {
    flex: 1;
}

.video-stats {
    flex-shrink: 0;
}

.empty-state {
    text-align: center;
    padding: 60px 20px;
    color: var(--text2);
}

.load-more {
    text-align: center;
    margin-top: 30px;
}

.load-more button {
    padding: 10px 20px;
    background-color: var(--brand_pink);
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.load-more button:hover {
    background-color: #ff6b9d;
}

.loading {
    text-align: center;
    padding: 40px 20px;
    color: var(--text2);
}
</style>
