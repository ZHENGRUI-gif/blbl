<template>
    <div class="flex-fill">
        <div class="v-container">
            <div class="hot-search-layout">
                <!-- 左侧：热搜词列表 -->
                <div class="hot-search-left">
                    <div class="v-card">
                        <div class="hot-search-list-card">
                            <div class="hot-search-header">
                                <h3>热搜榜单</h3>
                                <div class="header-actions">
                                    <span class="refresh" @click="loadHotSearches">刷新</span>
                                    <span class="total">共 {{ hotSearches.length }} 条</span>
                                </div>
                            </div>
                            <div class="hot-search-list" v-loading="loading">
                                <div 
                                    class="hot-search-item" 
                                    v-for="(item, index) in hotSearches" 
                                    :key="index"
                                    :class="{ active: selectedSearch === item.content }"
                                    @click="selectHotSearch(item)"
                                >
                                    <div class="rank-badge" :class="'rank-' + (index + 1)">
                                        {{ index + 1 }}
                                    </div>
                                    <div class="search-info">
                                        <div class="search-content">
                                            <span class="search-text">{{ item.content }}</span>
                                            <span class="search-tag" v-if="item.type === 1">新</span>
                                            <span class="search-tag hot" v-if="item.type === 2">热</span>
                                        </div>
                                        <div class="search-stats">
                                            <span>热度: {{ formatNumber(item.score) }}</span>
                                        </div>
                                    </div>
                                    <i class="iconfont icon-youjiantou"></i>
                                </div>
                                <div class="no-data" v-if="!loading && hotSearches.length === 0">
                                    <img src="~assets/img/silly.png" alt="">
                                    <span>暂无热搜数据</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 右侧：热搜相关视频 -->
                <div class="hot-search-right">
                    <div class="v-card">
                        <div class="related-videos-card">
                            <div class="related-videos-header">
                                <h3>{{ selectedSearch ? `"${selectedSearch}" 相关视频` : '请选择热搜词' }}</h3>
                                <span class="subtitle" v-if="selectedSearch">点击按钮将视频设为轮播图</span>
                            </div>
                            <div class="related-videos-list" v-loading="loadingVideos">
                                <div 
                                    class="video-item" 
                                    v-for="video in relatedVideos" 
                                    :key="video.vid"
                                >
                                    <img :src="video.coverUrl" class="video-cover" alt="">
                                    <div class="video-info">
                                        <div class="video-title">{{ video.title }}</div>
                                        <div class="video-stats">
                                            <span><i class="iconfont icon-bofangshu"></i> {{ formatNumber(video.play) }}</span>
                                            <span><i class="iconfont icon-dianzan"></i> {{ formatNumber(video.good) }}</span>
                                        </div>
                                        <div class="video-author">UP: {{ video.author }}</div>
                                    </div>
                                    <div class="video-actions">
                                        <el-button 
                                            type="primary" 
                                            size="small"
                                            @click="replaceCarousel(video)"
                                        >
                                            替换轮播图
                                        </el-button>
                                    </div>
                                </div>
                                <div class="no-data" v-if="!loadingVideos && selectedSearch && relatedVideos.length === 0">
                                    <span>未找到相关视频</span>
                                </div>
                                <div class="no-data" v-if="!loadingVideos && !selectedSearch">
                                    <span>← 请从左侧选择热搜词</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 替换轮播图对话框 -->
        <el-dialog 
            v-model="replaceDialogVisible" 
            width="680px"
            :close-on-click-modal="false"
            class="replace-dialog"
        >
            <template #header>
                <div class="dialog-header">
                    <div class="dialog-title">
                        <i class="iconfont icon-bianji" style="margin-right: 8px;"></i>
                        替换轮播图
                    </div>
                    <div class="dialog-subtitle">将热门视频快速设置为轮播图</div>
                </div>
            </template>

            <div class="dialog-content">
                <!-- 新视频信息卡片 -->
                <div class="section">
                    <div class="section-title">
                        <i class="iconfont icon-shipin"></i>
                        <span>新视频</span>
                        <span class="badge new-badge">NEW</span>
                    </div>
                    <div class="new-video-card">
                        <img :src="replaceForm.newVideo?.coverUrl" class="new-video-cover" alt="">
                        <div class="new-video-info">
                            <div class="new-video-title">{{ replaceForm.newVideo?.title }}</div>
                            <div class="new-video-stats">
                                <span><i class="iconfont icon-bofangshu"></i> {{ formatNumber(replaceForm.newVideo?.play || 0) }}</span>
                                <span><i class="iconfont icon-dianzan"></i> {{ formatNumber(replaceForm.newVideo?.good || 0) }}</span>
                            </div>
                            <div class="new-video-author">UP主: {{ replaceForm.newVideo?.author }}</div>
                        </div>
                    </div>
                </div>

                <!-- 选择要替换的轮播图 -->
                <div class="section">
                    <div class="section-title">
                        <i class="iconfont icon-liebiao"></i>
                        <span>选择要替换的轮播图</span>
                        <span class="required">*</span>
                    </div>
                    <el-select 
                        v-model="replaceForm.targetVid" 
                        placeholder="请选择要替换的轮播图位置"
                        size="large"
                        class="target-select"
                    >
                        <el-option
                            v-for="carousel in carousels"
                            :key="carousel.vid"
                            :label="`#${carousel.vid} - ${carousel.title}`"
                            :value="carousel.vid"
                        >
                            <div class="carousel-option">
                                <img :src="carousel.url" class="carousel-option-cover" alt="">
                                <div class="carousel-option-info">
                                    <div class="carousel-option-title">{{ carousel.title }}</div>
                                    <div class="carousel-option-meta">
                                        <span class="carousel-option-id">#{{ carousel.vid }}</span>
                                        <span class="carousel-option-sort">排序: {{ carousel.sort }}</span>
                                    </div>
                                </div>
                            </div>
                        </el-option>
                    </el-select>
                </div>

                <!-- 底色选择 -->
                <div class="section">
                    <div class="section-title">
                        <i class="iconfont icon-yanse"></i>
                        <span>自定义底色</span>
                        <span class="tip">(可选)</span>
                    </div>
                    <div class="color-picker-wrapper">
                        <div class="color-preview" :style="{backgroundColor: replaceForm.color}"></div>
                        <el-input 
                            v-model="replaceForm.color" 
                            placeholder="输入颜色值，如 #ca8d6b"
                            size="large"
                            class="color-input"
                        >
                            <template #prepend>
                                <span style="color: var(--text2);">HEX</span>
                            </template>
                        </el-input>
                    </div>
                    <div class="color-presets">
                        <span class="color-preset-label">推荐配色:</span>
                        <div 
                            v-for="color in presetColors" 
                            :key="color"
                            class="color-preset-item"
                            :class="{ active: replaceForm.color === color }"
                            :style="{backgroundColor: color}"
                            @click="replaceForm.color = color"
                            :title="color"
                        ></div>
                    </div>
                </div>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button size="large" @click="replaceDialogVisible = false">
                        <i class="iconfont icon-guanbi" style="margin-right: 4px;"></i>
                        取消
                    </el-button>
                    <el-button 
                        type="primary" 
                        size="large"
                        @click="confirmReplace" 
                        :disabled="!replaceForm.targetVid"
                    >
                        <i class="iconfont icon-duihao" style="margin-right: 4px;"></i>
                        确定替换
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
    name: "HotSearchManage",
    data() {
        return {
            hotSearches: [],
            loading: true,
            selectedSearch: '',
            relatedVideos: [],
            loadingVideos: false,
            carousels: [],
            replaceDialogVisible: false,
            replaceForm: {
                newVideo: null,
                targetVid: null,
                color: '#ca8d6b'
            },
            presetColors: [
                '#ca8d6b',
                '#5894d4',
                '#836e61',
                '#728cb4',
                '#f56c6c',
                '#67c23a',
                '#e6a23c',
                '#909399',
                '#ff6b9d',
                '#c084fc'
            ]
        }
    },
    methods: {
        // 加载热搜列表
        async loadHotSearches() {
            this.loading = true;
            try {
                const res = await this.$get('/search/hot/get');
                if (res && res.data && res.data.data) {
                    this.hotSearches = res.data.data;
                }
            } catch (error) {
                ElMessage.error('加载热搜失败');
                console.error(error);
            }
            this.loading = false;
        },

        // 选择热搜词
        async selectHotSearch(item) {
            this.selectedSearch = item.content;
            await this.loadRelatedVideos(item.content);
        },

        // 加载相关视频
        async loadRelatedVideos(keyword) {
            this.loadingVideos = true;
            this.relatedVideos = [];
            try {
                // 使用搜索接口查找相关视频（仅已过审）
                const res = await this.$get('/search/video/only-pass', {
                    params: {
                        keyword: keyword,
                        page: 1
                    }
                });

                if (res && res.data && res.data.data) {
                    this.relatedVideos = res.data.data.map(item => ({
                        vid: item.video.vid,
                        title: item.video.title,
                        coverUrl: item.video.coverUrl,
                        play: item.stats?.play || 0,
                        good: item.stats?.good || 0,
                        author: item.user?.nickname || '未知'
                    }));
                }
            } catch (error) {
                console.error('加载相关视频失败', error);
            }
            this.loadingVideos = false;
        },

        // 加载当前轮播图列表
        async loadCarousels() {
            try {
                const res = await this.$get('/carousel/admin/list', {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });
                if (res && res.data && res.data.code === 200) {
                    this.carousels = res.data.data || [];
                }
            } catch (error) {
                console.error('加载轮播图列表失败', error);
            }
        },

        // 显示替换对话框
        async replaceCarousel(video) {
            // 先加载轮播图列表
            await this.loadCarousels();
            
            if (this.carousels.length === 0) {
                ElMessage.warning('当前没有轮播图，请先添加轮播图');
                return;
            }

            this.replaceForm = {
                newVideo: video,
                targetVid: null,
                color: '#ca8d6b'
            };
            this.replaceDialogVisible = true;
        },

        // 确认替换
        async confirmReplace() {
            if (!this.replaceForm.targetVid) {
                ElMessage.warning('请选择要替换的轮播图');
                return;
            }

            try {
                await ElMessageBox.confirm(
                    `确定要用"${this.replaceForm.newVideo.title}"替换选中的轮播图吗？`, 
                    '确认替换', 
                    {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning',
                    }
                );

                // 先删除旧的
                const deleteRes = await this.$delete(`/carousel/admin/delete/${this.replaceForm.targetVid}`, {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });

                if (deleteRes.data.code !== 200) {
                    ElMessage.error('删除旧轮播图失败');
                    return;
                }

                // 再添加新的
                const targetCarousel = this.carousels.find(c => c.vid === this.replaceForm.targetVid);
                const addRes = await this.$post('/carousel/admin/add', {
                    vid: this.replaceForm.newVideo.vid,
                    color: this.replaceForm.color,
                    sort: targetCarousel?.sort || 0
                }, {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    }
                });

                if (addRes.data.code === 200) {
                    ElMessage.success('替换成功');
                    this.replaceDialogVisible = false;
                    await this.loadCarousels();
                } else {
                    ElMessage.error(addRes.data.message || '添加新轮播图失败');
                }
            } catch (error) {
                if (error !== 'cancel') {
                    ElMessage.error('替换失败');
                    console.error(error);
                }
            }
        },

        // 格式化数字
        formatNumber(num) {
            if (num >= 10000) {
                return (num / 10000).toFixed(1) + '万';
            }
            return num;
        }
    },
    async created() {
        await this.loadHotSearches();
    }
}
</script>

<style scoped>
.hot-search-layout {
    display: flex;
    gap: 20px;
    height: calc(100vh - 96px);
    overflow: hidden;
}

.hot-search-left {
    width: 360px;
    flex-shrink: 0;
    height: 100%;
    overflow: hidden;
}

.hot-search-right {
    flex: 1;
    min-width: 0;
    height: 100%;
    overflow: hidden;
}

.v-card {
    height: 100%;
    overflow: hidden;
}

.hot-search-list-card,
.related-videos-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.hot-search-header,
.related-videos-header {
    padding: 20px;
    border-bottom: 1px solid #e7e7e7;
    flex-shrink: 0;
}

.hot-search-header h3,
.related-videos-header h3 {
    margin: 0 0 5px 0;
    font-size: 18px;
    color: var(--text1);
}

.header-actions {
    display: flex;
    gap: 15px;
    margin-top: 10px;
}

.refresh {
    cursor: pointer;
    color: var(--brand_blue);
    font-size: 14px;
}

.refresh:hover {
    color: var(--Lb6);
}

.total {
    font-size: 14px;
    color: var(--text3);
}

.related-videos-header .subtitle {
    font-size: 12px;
    color: var(--text3);
}

.hot-search-list,
.related-videos-list {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 10px;
    min-height: 0;
}

.hot-search-item {
    display: flex;
    align-items: center;
    padding: 15px;
    margin-bottom: 10px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    background: var(--bg1);
    border: 2px solid transparent;
}

.hot-search-item:hover {
    background: var(--graph_bg_thick);
    border-color: var(--brand_blue);
}

.hot-search-item.active {
    background: var(--graph_bg_thick);
    border-color: var(--brand_pink);
}

.rank-badge {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    font-size: 16px;
    font-weight: bold;
    background: #e7e7e7;
    color: #666;
    flex-shrink: 0;
    margin-right: 12px;
}

.rank-badge.rank-1 {
    background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
    color: #fff;
}

.rank-badge.rank-2 {
    background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
    color: #fff;
}

.rank-badge.rank-3 {
    background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
    color: #fff;
}

.search-info {
    flex: 1;
    min-width: 0;
}

.search-content {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
}

.search-text {
    font-size: 15px;
    font-weight: 500;
    color: var(--text1);
}

.search-tag {
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 12px;
    background: #67c23a;
    color: #fff;
}

.search-tag.hot {
    background: #f56c6c;
}

.search-stats {
    font-size: 12px;
    color: var(--text3);
}

.hot-search-item .iconfont {
    font-size: 16px;
    color: var(--text3);
    margin-left: 10px;
}

.video-item {
    display: flex;
    align-items: center;
    padding: 15px;
    margin-bottom: 12px;
    border-radius: 8px;
    background: var(--bg1);
    border: 1px solid #e7e7e7;
    transition: all 0.2s;
}

.video-item:hover {
    border-color: var(--brand_blue);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.video-cover {
    width: 120px;
    height: 75px;
    object-fit: cover;
    border-radius: 6px;
    margin-right: 15px;
    flex-shrink: 0;
}

.video-info {
    flex: 1;
    min-width: 0;
    margin-right: 15px;
}

.video-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--text1);
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
}

.video-stats {
    display: flex;
    gap: 15px;
    font-size: 12px;
    color: var(--text3);
    margin-bottom: 6px;
}

.video-stats span {
    display: flex;
    align-items: center;
    gap: 4px;
}

.video-stats .iconfont {
    font-size: 14px;
}

.video-author {
    font-size: 12px;
    color: var(--text2);
}

.video-actions {
    flex-shrink: 0;
}

.no-data {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    color: var(--text3);
}

.no-data img {
    height: 80px;
    margin-bottom: 10px;
}

.no-data span {
    font-size: 16px;
}

/* ========== 替换轮播图弹窗样式 ========== */
.replace-dialog :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
}

.replace-dialog :deep(.el-dialog__body) {
    padding: 0;
}

.replace-dialog :deep(.el-dialog__footer) {
    padding: 0;
}

.dialog-header {
    padding: 24px 24px 20px;
    border-bottom: 1px solid #e7e7e7;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.dialog-title {
    font-size: 20px;
    font-weight: 600;
    display: flex;
    align-items: center;
    margin-bottom: 6px;
}

.dialog-subtitle {
    font-size: 13px;
    opacity: 0.9;
    padding-left: 28px;
}

.dialog-content {
    padding: 24px;
    max-height: 65vh;
    overflow-y: auto;
}

.section {
    margin-bottom: 24px;
}

.section:last-child {
    margin-bottom: 0;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--text1);
    margin-bottom: 12px;
}

.section-title .iconfont {
    font-size: 18px;
    color: var(--brand_blue);
}

.badge {
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 500;
}

.new-badge {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.required {
    color: #f56c6c;
    font-size: 16px;
    margin-left: auto;
}

.tip {
    color: var(--text3);
    font-size: 12px;
    font-weight: 400;
}

/* 新视频卡片 */
.new-video-card {
    display: flex;
    gap: 16px;
    padding: 16px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
    border: 2px solid rgba(102, 126, 234, 0.2);
    transition: all 0.3s;
}

.new-video-card:hover {
    border-color: rgba(102, 126, 234, 0.4);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.new-video-cover {
    width: 160px;
    height: 90px;
    object-fit: cover;
    border-radius: 8px;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.new-video-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
}

.new-video-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text1);
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.5;
}

.new-video-stats {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: var(--text2);
    margin-bottom: 6px;
}

.new-video-stats span {
    display: flex;
    align-items: center;
    gap: 4px;
}

.new-video-stats .iconfont {
    font-size: 14px;
}

.new-video-author {
    font-size: 12px;
    color: var(--text3);
}

/* 目标选择器 */
.target-select {
    width: 100%;
}

.carousel-option {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 4px 0;
}

.carousel-option-cover {
    width: 80px;
    height: 45px;
    object-fit: cover;
    border-radius: 6px;
    flex-shrink: 0;
    border: 1px solid #e7e7e7;
}

.carousel-option-info {
    flex: 1;
    min-width: 0;
}

.carousel-option-title {
    font-size: 14px;
    color: var(--text1);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: 4px;
}

.carousel-option-meta {
    display: flex;
    gap: 12px;
    font-size: 12px;
}

.carousel-option-id {
    color: var(--brand_blue);
    font-weight: 500;
}

.carousel-option-sort {
    color: var(--text3);
}

/* 颜色选择器 */
.color-picker-wrapper {
    display: flex;
    gap: 12px;
    align-items: stretch;
}

.color-preview {
    width: 60px;
    height: 40px;
    border-radius: 8px;
    border: 2px solid #e7e7e7;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s;
}

.color-preview:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.color-input {
    flex: 1;
}

.color-presets {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 12px;
    padding: 12px;
    background: var(--graph_bg_thick);
    border-radius: 8px;
}

.color-preset-label {
    font-size: 13px;
    color: var(--text2);
    margin-right: 4px;
}

.color-preset-item {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.color-preset-item:hover {
    transform: scale(1.1);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.color-preset-item.active {
    border-color: var(--text1);
    transform: scale(1.15);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* 弹窗底部 */
.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 24px;
    border-top: 1px solid #e7e7e7;
    background: var(--graph_bg_thick);
}

.dialog-footer .el-button {
    min-width: 100px;
}
</style>