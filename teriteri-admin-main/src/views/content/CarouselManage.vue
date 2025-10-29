<template>
    <div class="flex-fill">
        <div class="v-container">
            <div class="carousel-layout">
                <!-- 左侧：轮播图列表 -->
                <div class="carousel-left" :style="{width: leftWidth + '%'}">
                    <div class="v-card">
                        <div class="carousel-table-card">
                            <div class="v-table" v-loading="loading">
                                <div class="top">
                                    <div class="navbar">
                                        <div class="bar-item active">轮播图列表</div>
                                    </div>
                                    <div class="top-right">
                                        <div class="add-btn" @click="showAddDialog">添加视频</div>
                                        <div class="init-btn" @click="initCarousels" v-if="carousels.length === 0">初始化</div>
                                        <div class="refresh" @click="loadCarousels">刷新</div>
                                        <div class="total">共 {{ carousels.length }} 条</div>
                                    </div>
                                </div>
                                <div class="v-table__wrapper">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th style="min-width: 80px;">视频ID</th>
                                                <th style="min-width: 176px;">封面</th>
                                                <th style="min-width: 200px;">标题</th>
                                                <th style="min-width: 100px;">底色</th>
                                                <th style="min-width: 100px;">排序</th>
                                                <th style="min-width: 80px;">状态</th>
                                                <th style="min-width: 160px;">操作</th>
                                            </tr>
                                        </thead>
                                        <tbody v-if="carousels.length != 0">
                                            <tr v-for="(item, index) in carousels" :key="index">
                                                <td style="min-width: 80px;"># {{ item.vid }}</td>
                                                <td style="width: 176px;">
                                                    <img :src="item.url" class="carousel-img" alt="">
                                                </td>
                                                <td style="min-width: 200px;">
                                                    {{ item.title }}
                                                    <span v-if="item.videoStatus !== 1" style="color: #f56c6c; font-size: 12px;">
                                                        (视频{{ item.videoStatus === -1 ? '已删除' : '未过审' }})
                                                    </span>
                                                </td>
                                                <td style="min-width: 100px;">
                                                    <div class="color-display">
                                                        <div class="color-box" :style="{backgroundColor: item.color}"></div>
                                                        <span>{{ item.color }}</span>
                                                    </div>
                                                </td>
                                                <td style="min-width: 100px;">
                                                    <el-input-number 
                                                        v-model="item.sort" 
                                                        :min="0" 
                                                        :max="999"
                                                        size="small"
                                                        @change="updateCarousel(item)"
                                                    ></el-input-number>
                                                </td>
                                                <td style="min-width: 80px;">
                                                    <el-switch
                                                        v-model="item.status"
                                                        :active-value="1"
                                                        :inactive-value="0"
                                                        @change="updateCarousel(item)"
                                                    ></el-switch>
                                                </td>
                                                <td style="min-width: 160px;">
                                                    <span class="action-btn edit-btn" @click="showEditDialog(item)">编辑</span>
                                                    <span class="action-btn delete-btn" @click="deleteCarousel(item.vid)">删除</span>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                    <div class="no-more" v-if="!loading && carousels.length == 0">
                                        <img src="~assets/img/silly.png" alt="">
                                        <span>暂无轮播图数据，点击"初始化"按钮添加默认轮播图</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 可拖动的分隔条 -->
                <div class="resizer" @mousedown="startDrag"></div>

                <!-- 右侧：热门视频列表 -->
                <div class="carousel-right" :style="{width: (100 - leftWidth) + '%'}">
                    <div class="v-card">
                        <div class="hot-videos-card">
                            <div class="hot-videos-header">
                                <div class="header-title">
                                    <i class="iconfont icon-hot" style="color: #ff6b6b;"></i>
                                    <h3>热门视频</h3>
                                </div>
                                <span class="subtitle">按播放量排序，点击按钮快速替换轮播图</span>
                            </div>
                            <div class="hot-videos-list" v-loading="loadingHotVideos">
                                <div 
                                    class="hot-video-item" 
                                    v-for="(video, index) in hotVideos" 
                                    :key="video.vid"
                                >
                                    <div class="hot-rank" :class="'rank-' + ((currentPage - 1) * 10 + index + 1)">
                                        {{ (currentPage - 1) * 10 + index + 1 }}
                                    </div>
                                    <img :src="video.coverUrl" class="hot-video-cover" alt="">
                                    <div class="hot-video-info">
                                        <div class="hot-video-title">{{ video.title }}</div>
                                        <div class="hot-video-stats">
                                            <span><i class="iconfont icon-bofangshu"></i> {{ formatNumber(video.play) }}</span>
                                            <span><i class="iconfont icon-dianzan"></i> {{ formatNumber(video.good) }}</span>
                                        </div>
                                        <div class="hot-video-author">UP: {{ video.author }}</div>
                                    </div>
                                    <div class="hot-video-actions">
                                        <el-button 
                                            v-if="!isVideoInCarousel(video.vid)"
                                            type="primary" 
                                            size="small"
                                            @click="replaceCarouselWithHot(video)"
                                        >
                                            替换轮播图
                                        </el-button>
                                        <el-tag v-else type="success" size="small">
                                            已在轮播图中
                                        </el-tag>
                                    </div>
                                </div>
                                <div class="no-data" v-if="!loadingHotVideos && hotVideos.length === 0">
                                    <span>暂无热门视频数据</span>
                                </div>
                            </div>
                            <div class="pagination-wrapper" v-if="totalHotVideos > 0">
                                <el-pagination
                                    v-model:current-page="currentPage"
                                    :page-size="10"
                                    layout="prev, pager, next, jumper, total"
                                    :total="totalHotVideos"
                                    @current-change="handlePageChange"
                                    background
                                    small
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 添加/编辑对话框 -->
        <el-dialog 
            v-model="dialogVisible" 
            width="680px"
            :close-on-click-modal="false"
            @close="resetForm"
            class="carousel-dialog"
        >
            <template #header>
                <div class="dialog-header">
                    <div class="dialog-title">
                        <i class="iconfont" :class="isEdit ? 'icon-bianji' : 'icon-tianjia'" style="margin-right: 8px;"></i>
                        {{ dialogTitle }}
                    </div>
                    <div class="dialog-subtitle">{{ isEdit ? '修改轮播图配置' : '添加视频到轮播图' }}</div>
                </div>
            </template>

            <div class="dialog-content">
                <!-- 视频ID输入 -->
                <div class="section" v-if="!isEdit">
                    <div class="section-title">
                        <i class="iconfont icon-shipin"></i>
                        <span>视频ID</span>
                        <span class="required">*</span>
                    </div>
                    <el-input 
                        v-model.number="form.vid" 
                        type="number" 
                        size="large"
                        placeholder="请输入视频ID后按回车或点击查询"
                        @blur="loadVideoInfo"
                        @keyup.enter="loadVideoInfo"
                        class="vid-input"
                    >
                        <template #append>
                            <el-button @click="loadVideoInfo" :icon="'Search'">查询</el-button>
                        </template>
                    </el-input>
                </div>

                <!-- 视频ID显示（编辑模式） -->
                <div class="section" v-else>
                    <div class="section-title">
                        <i class="iconfont icon-shipin"></i>
                        <span>视频ID</span>
                    </div>
                    <el-input v-model="form.vid" size="large" disabled class="vid-display"></el-input>
                </div>

                <!-- 视频信息卡片 -->
                <div class="section" v-if="videoInfo.title">
                    <div class="section-title">
                        <i class="iconfont icon-xinxi"></i>
                        <span>视频信息</span>
                    </div>
                    <div class="video-info-card">
                        <img :src="videoInfo.coverUrl" class="video-info-cover" alt="">
                        <div class="video-info-detail">
                            <div class="video-info-title">{{ videoInfo.title }}</div>
                            <div class="video-info-status">
                                <span class="status-label">状态:</span>
                                <span class="status-value" :class="'status-' + videoInfo.status">
                                    {{ getVideoStatusText(videoInfo.status) }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 底色选择 -->
                <div class="section">
                    <div class="section-title">
                        <i class="iconfont icon-yanse"></i>
                        <span>轮播图底色</span>
                        <span class="required">*</span>
                    </div>
                    <div class="color-picker-wrapper">
                        <div class="color-preview" :style="{backgroundColor: form.color}"></div>
                        <el-input 
                            v-model="form.color" 
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
                            :class="{ active: form.color === color }"
                            :style="{backgroundColor: color}"
                            @click="form.color = color"
                            :title="color"
                        ></div>
                    </div>
                </div>

                <!-- 排序 -->
                <div class="section">
                    <div class="section-title">
                        <i class="iconfont icon-paixu"></i>
                        <span>显示排序</span>
                        <span class="tip">(数字越小越靠前)</span>
                    </div>
                    <el-input-number 
                        v-model="form.sort" 
                        :min="0" 
                        :max="999"
                        size="large"
                        class="sort-input"
                        controls-position="right"
                    ></el-input-number>
                </div>

                <!-- 状态开关（仅编辑时显示） -->
                <div class="section" v-if="isEdit">
                    <div class="section-title">
                        <i class="iconfont icon-zhuangtai"></i>
                        <span>启用状态</span>
                    </div>
                    <el-switch
                        v-model="form.status"
                        :active-value="1"
                        :inactive-value="0"
                        active-text="启用"
                        inactive-text="禁用"
                        size="large"
                        class="status-switch"
                    ></el-switch>
                </div>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button size="large" @click="dialogVisible = false">
                        <i class="iconfont icon-guanbi" style="margin-right: 4px;"></i>
                        取消
                    </el-button>
                    <el-button 
                        type="primary" 
                        size="large"
                        @click="submitForm" 
                        :disabled="!videoInfo.title"
                    >
                        <i class="iconfont icon-duihao" style="margin-right: 4px;"></i>
                        确定
                    </el-button>
                </div>
            </template>
        </el-dialog>

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
                        <span class="badge new-badge">HOT</span>
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
                            :disabled="carousel.vid === replaceForm.newVideo?.vid"
                        >
                            <div class="carousel-option" :class="{ 'is-current': carousel.vid === replaceForm.newVideo?.vid }">
                                <img :src="carousel.url" class="carousel-option-cover" alt="">
                                <div class="carousel-option-info">
                                    <div class="carousel-option-title">
                                        {{ carousel.title }}
                                        <span v-if="carousel.vid === replaceForm.newVideo?.vid" class="current-tag">当前视频</span>
                                    </div>
                                    <div class="carousel-option-meta">
                                        <span class="carousel-option-id">#{{ carousel.vid }}</span>
                                        <span class="carousel-option-sort">排序: {{ carousel.sort }}</span>
                                    </div>
                                </div>
                            </div>
                        </el-option>
                    </el-select>
                    <div class="select-tip" v-if="carousels.some(c => c.vid === replaceForm.newVideo?.vid)">
                        <i class="iconfont icon-info"></i>
                        提示：当前视频已在轮播图中（灰色不可选）
                    </div>
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
    name: "CarouselManage",
    data() {
        return {
            carousels: [],
            loading: true,
            dialogVisible: false,
            dialogTitle: '添加轮播图',
            isEdit: false,
            form: {
                vid: null,
                color: '#ca8d6b',
                sort: 0,
                status: 1
            },
            videoInfo: {
                title: '',
                coverUrl: '',
                status: 0
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
            ],
            // 热门视频相关
            hotVideos: [],
            loadingHotVideos: false,
            currentPage: 1,
            totalHotVideos: 0,
            replaceDialogVisible: false,
            replaceForm: {
                newVideo: null,
                targetVid: null,
                color: '#ca8d6b'
            },
            // 分隔条拖动相关
            isDragging: false,
            leftWidth: 60, // 左侧宽度百分比
            startX: 0,
            startWidth: 60
        }
    },
    methods: {
        // 加载轮播图列表
        async loadCarousels() {
            this.loading = true;
            try {
                const res = await this.$get('/carousel/admin/list', {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });
                if (res.data.code === 200) {
                    this.carousels = res.data.data || [];
                } else {
                    ElMessage.error(res.data.message || '加载失败');
                }
            } catch (error) {
                ElMessage.error('加载失败');
                console.error(error);
            }
            this.loading = false;
        },

        // 初始化默认轮播图
        async initCarousels() {
            try {
                await ElMessageBox.confirm('将使用默认的6个视频初始化轮播图，确定继续吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'info',
                });

                const res = await this.$post('/carousel/admin/init', {}, {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });

                if (res.data.code === 200) {
                    ElMessage.success(res.data.message || '初始化成功');
                    await this.loadCarousels();
                } else {
                    ElMessage.error(res.data.message || '初始化失败');
                }
            } catch (error) {
                if (error !== 'cancel') {
                    ElMessage.error('初始化失败');
                    console.error(error);
                }
            }
        },

        // 显示添加对话框
        showAddDialog() {
            this.dialogTitle = '添加轮播图';
            this.isEdit = false;
            this.resetForm();
            this.dialogVisible = true;
        },

        // 显示编辑对话框
        showEditDialog(item) {
            this.dialogTitle = '编辑轮播图';
            this.isEdit = true;
            this.form = {
                vid: item.vid,
                color: item.color,
                sort: item.sort,
                status: item.status
            };
            this.videoInfo = {
                title: item.title,
                coverUrl: item.url,
                status: item.videoStatus
            };
            this.dialogVisible = true;
        },

        // 加载视频信息
        async loadVideoInfo() {
            if (!this.form.vid) {
                ElMessage.warning('请输入视频ID');
                return;
            }

            try {
                const res = await this.$get('/review/video/getone', {
                    params: { vid: this.form.vid },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    }
                });
                
                if (res && res.data && res.data.code === 200 && res.data.data) {
                    const video = res.data.data.video;
                    this.videoInfo = {
                        title: video.title,
                        coverUrl: video.coverUrl,
                        status: video.status
                    };
                    
                    if (video.status !== 1) {
                        ElMessage.warning('该视频未过审，添加后可能无法正常展示');
                    }
                } else {
                    ElMessage.error('视频不存在');
                    this.videoInfo = {
                        title: '',
                        coverUrl: '',
                        status: 0
                    };
                }
            } catch (error) {
                ElMessage.error('查询视频信息失败');
                console.error(error);
                this.videoInfo = {
                    title: '',
                    coverUrl: '',
                    status: 0
                };
            }
        },

        // 提交表单
        async submitForm() {
            if (!this.form.vid) {
                ElMessage.warning('请输入视频ID');
                return;
            }

            if (!this.videoInfo.title) {
                ElMessage.warning('请先查询视频信息');
                return;
            }

            try {
                let res;
                if (this.isEdit) {
                    res = await this.$put('/carousel/admin/update', this.form, {
                        headers: {
                            Authorization: "Bearer " + localStorage.getItem("teri_token"),
                        },
                    });
                } else {
                    res = await this.$post('/carousel/admin/add', this.form, {
                        headers: {
                            Authorization: "Bearer " + localStorage.getItem("teri_token"),
                        },
                    });
                }

                if (res.data.code === 200) {
                    ElMessage.success(this.isEdit ? '更新成功' : '添加成功');
                    this.dialogVisible = false;
                    await this.loadCarousels();
                } else {
                    ElMessage.error(res.data.message || '操作失败');
                }
            } catch (error) {
                ElMessage.error('操作失败');
                console.error(error);
            }
        },

        // 更新轮播图
        async updateCarousel(item) {
            try {
                const res = await this.$put('/carousel/admin/update', {
                    vid: item.vid,
                    color: item.color,
                    sort: item.sort,
                    status: item.status
                }, {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });

                if (res.data.code === 200) {
                    ElMessage.success('更新成功');
                    await this.loadCarousels();
                } else {
                    ElMessage.error(res.data.message || '更新失败');
                    await this.loadCarousels();
                }
            } catch (error) {
                ElMessage.error('更新失败');
                await this.loadCarousels();
                console.error(error);
            }
        },

        // 删除轮播图
        async deleteCarousel(vid) {
            try {
                await ElMessageBox.confirm('确定要删除这个轮播图吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                });

                const res = await this.$delete(`/carousel/admin/delete/${vid}`, {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });

                if (res.data.code === 200) {
                    ElMessage.success('删除成功');
                    await this.loadCarousels();
                } else {
                    ElMessage.error(res.data.message || '删除失败');
                }
            } catch (error) {
                if (error !== 'cancel') {
                    ElMessage.error('删除失败');
                    console.error(error);
                }
            }
        },

        // 重置表单
        resetForm() {
            this.form = {
                vid: null,
                color: '#ca8d6b',
                sort: this.carousels.length,
                status: 1
            };
            this.videoInfo = {
                title: '',
                coverUrl: '',
                status: 0
            };
        },

        // 获取视频状态文本
        getVideoStatusText(status) {
            const statusMap = {
                0: '审核中',
                1: '已过审',
                2: '未通过',
                3: '已删除',
                '-1': '已删除'
            };
            return statusMap[status] || '未知';
        },

        // 加载热门视频列表
        async loadHotVideos(page = 1) {
            this.loadingHotVideos = true;
            try {
                const res = await this.$get('/video/hot/get', {
                    params: {
                        page: page,
                        quantity: 10
                    },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token"),
                    },
                });

                if (res && res.data && res.data.data) {
                    const data = res.data.data;
                    this.totalHotVideos = data.count || 0;
                    
                    if (data.list && data.list.length > 0) {
                        this.hotVideos = data.list.map(item => ({
                            vid: item.video.vid,
                            title: item.video.title,
                            coverUrl: item.video.coverUrl,
                            play: item.stats?.play || 0,
                            good: item.stats?.good || 0,
                            author: item.user?.nickname || '未知'
                        }));
                    } else {
                        this.hotVideos = [];
                    }
                }
            } catch (error) {
                console.error('加载热门视频失败', error);
                this.hotVideos = [];
                this.totalHotVideos = 0;
            }
            this.loadingHotVideos = false;
        },

        // 分页切换
        handlePageChange(page) {
            this.currentPage = page;
            this.loadHotVideos(page);
        },

        // 格式化数字
        formatNumber(num) {
            if (num >= 10000) {
                return (num / 10000).toFixed(1) + '万';
            }
            return num;
        },

        // 检查视频是否已在轮播图中
        isVideoInCarousel(vid) {
            return this.carousels.some(carousel => carousel.vid === vid);
        },

        // 显示替换对话框（热门视频）
        async replaceCarouselWithHot(video) {
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

            // 检查是否选择了同一个视频（自我替换）
            if (this.replaceForm.targetVid === this.replaceForm.newVideo.vid) {
                ElMessage.warning('不能用同一个视频替换自己哦~');
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

        // 开始拖动分隔条
        startDrag(e) {
            this.isDragging = true;
            this.startX = e.clientX;
            this.startWidth = this.leftWidth;
            document.addEventListener('mousemove', this.onDrag);
            document.addEventListener('mouseup', this.stopDrag);
            document.body.style.cursor = 'col-resize';
            document.body.style.userSelect = 'none';
        },

        // 拖动中
        onDrag(e) {
            if (!this.isDragging) return;
            
            const containerWidth = document.querySelector('.carousel-layout').offsetWidth;
            const deltaX = e.clientX - this.startX;
            const deltaPercent = (deltaX / containerWidth) * 100;
            
            let newWidth = this.startWidth + deltaPercent;
            
            // 限制宽度范围：30% - 80%
            if (newWidth < 30) newWidth = 30;
            if (newWidth > 80) newWidth = 80;
            
            this.leftWidth = newWidth;
        },

        // 停止拖动
        stopDrag() {
            this.isDragging = false;
            document.removeEventListener('mousemove', this.onDrag);
            document.removeEventListener('mouseup', this.stopDrag);
            document.body.style.cursor = '';
            document.body.style.userSelect = '';
        }
    },
    async created() {
        await this.loadCarousels();
        await this.loadHotVideos(1);
    },
    beforeUnmount() {
        // 清理事件监听
        document.removeEventListener('mousemove', this.onDrag);
        document.removeEventListener('mouseup', this.stopDrag);
    }
}
</script>

<style scoped>
.carousel-table-card {
    height: calc(100vh - 96px);
    position: relative;
    overflow: hidden !important;
}

.v-table {
    --v-table-row-height: 120px;
}

.top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 64px;
    border-bottom: 1px solid #e7e7e7;
}

.navbar,
.top-right {
    display: flex;
    flex: 0 0 auto;
}

.top-right {
    margin-left: 100px;
}

.bar-item {
    flex: 0 0 auto;
    height: 64px;
    padding-bottom: 18px;
    padding-top: 26px;
    margin-left: 40px;
    font-size: 16px;
    color: #505050;
}

.active {
    color: var(--brand_pink);
    font-weight: 600;
    border-bottom: 3px solid var(--brand_pink);
}

.top-right>div {
    flex: 0 0 auto;
    line-height: 54px;
    margin-right: 30px;
    padding-top: 10px;
}

.add-btn, .init-btn {
    cursor: pointer;
    color: var(--success_green);
    font-weight: 500;
}

.add-btn:hover, .init-btn:hover {
    color: #67c23a;
}

.refresh {
    cursor: pointer;
    color: var(--brand_blue);
}

.refresh:hover {
    color: var(--Lb6);
}

.v-table__wrapper {
    height: calc(100% - 64px);
}

.v-table__wrapper table {
    padding: 0 4px 8px;
}

.carousel-img {
    height: 81px;
    width: 144px;
    object-fit: cover;
    box-shadow: 2px 2px 8px #0000001f;
}

.color-display {
    display: flex;
    align-items: center;
    gap: 8px;
}

.color-box {
    width: 30px;
    height: 30px;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
}

.action-btn {
    cursor: pointer;
    margin-right: 12px;
    padding: 4px 8px;
    display: inline-block;
}

.edit-btn {
    color: var(--brand_blue);
}

.edit-btn:hover {
    color: var(--Lb6);
    text-decoration: underline;
}

.delete-btn {
    color: var(--stress_red);
}

.delete-btn:hover {
    color: #f56c6c;
    text-decoration: underline;
}

.no-more {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    width: 100%;
}

.no-more img {
    height: 80px;
}

.no-more span {
    font-size: 16px;
    color: var(--text3);
    line-height: 40px;
}

/* ========== 轮播图编辑弹窗样式 ========== */
.carousel-dialog :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
}

.carousel-dialog :deep(.el-dialog__body) {
    padding: 0;
}

.carousel-dialog :deep(.el-dialog__footer) {
    padding: 0;
}

.dialog-header {
    padding: 24px 24px 20px;
    border-bottom: 1px solid #e7e7e7;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
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
    color: var(--brand_pink);
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

/* 视频ID输入框 */
.vid-input {
    width: 100%;
}

.vid-display {
    width: 100%;
}

/* 视频信息卡片 */
.video-info-card {
    display: flex;
    gap: 16px;
    padding: 16px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(240, 147, 251, 0.08) 0%, rgba(245, 87, 108, 0.08) 100%);
    border: 2px solid rgba(240, 147, 251, 0.2);
    transition: all 0.3s;
}

.video-info-card:hover {
    border-color: rgba(240, 147, 251, 0.4);
    box-shadow: 0 4px 12px rgba(240, 147, 251, 0.15);
}

.video-info-cover {
    width: 160px;
    height: 90px;
    object-fit: cover;
    border-radius: 8px;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.video-info-detail {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
}

.video-info-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text1);
    margin-bottom: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.5;
}

.video-info-status {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
}

.status-label {
    color: var(--text2);
}

.status-value {
    padding: 2px 10px;
    border-radius: 12px;
    font-weight: 500;
    font-size: 12px;
}

.status-value.status-0 {
    background: #fdf6ec;
    color: #e6a23c;
}

.status-value.status-1 {
    background: #f0f9ff;
    color: #67c23a;
}

.status-value.status-2 {
    background: #fef0f0;
    color: #f56c6c;
}

.status-value.status-3 {
    background: #f4f4f5;
    color: #909399;
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

/* 排序输入框 */
.sort-input {
    width: 200px;
}

/* 状态开关 */
.status-switch {
    display: block;
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

/* ========== 左右布局样式 ========== */
.carousel-layout {
    display: flex;
    height: calc(100vh - 96px);
    overflow: hidden;
    position: relative;
}

.carousel-left {
    height: 100%;
    overflow: hidden;
    min-width: 30%;
    max-width: 80%;
}

.carousel-right {
    height: 100%;
    overflow: hidden;
    flex: 1;
    min-width: 20%;
}

.carousel-left .v-card,
.carousel-right .v-card {
    height: 100%;
    overflow: hidden;
}

/* 可拖动的分隔条 */
.resizer {
    width: 4px;
    background: transparent;
    cursor: col-resize;
    flex-shrink: 0;
    position: relative;
    transition: background-color 0.2s;
}

.resizer:hover {
    background: var(--brand_blue);
}

.resizer::before {
    content: '';
    position: absolute;
    left: -4px;
    right: -4px;
    top: 0;
    bottom: 0;
}

/* ========== 热门视频列表样式 ========== */
.hot-videos-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.hot-videos-header {
    padding: 20px;
    border-bottom: 1px solid #e7e7e7;
    flex-shrink: 0;
}

.header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}

.header-title h3 {
    margin: 0;
    font-size: 18px;
    color: var(--text1);
}

.header-title .iconfont {
    font-size: 20px;
}

.hot-videos-header .subtitle {
    font-size: 12px;
    color: var(--text3);
    padding-left: 28px;
}

.hot-videos-list {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 10px;
    min-height: 0;
}

.hot-video-item {
    display: flex;
    align-items: center;
    padding: 12px;
    margin-bottom: 10px;
    border-radius: 8px;
    background: var(--bg1);
    border: 1px solid #e7e7e7;
    transition: all 0.2s;
}

.hot-video-item:hover {
    border-color: var(--brand_pink);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.hot-rank {
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

.hot-rank.rank-1 {
    background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
    color: #fff;
}

.hot-rank.rank-2 {
    background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
    color: #fff;
}

.hot-rank.rank-3 {
    background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
    color: #fff;
}

.hot-video-cover {
    width: 100px;
    height: 63px;
    object-fit: cover;
    border-radius: 6px;
    margin-right: 12px;
    flex-shrink: 0;
}

.hot-video-info {
    flex: 1;
    min-width: 0;
    margin-right: 10px;
}

.hot-video-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--text1);
    margin-bottom: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
}

.hot-video-stats {
    display: flex;
    gap: 12px;
    font-size: 11px;
    color: var(--text3);
    margin-bottom: 4px;
}

.hot-video-stats span {
    display: flex;
    align-items: center;
    gap: 3px;
}

.hot-video-stats .iconfont {
    font-size: 12px;
}

.hot-video-author {
    font-size: 11px;
    color: var(--text2);
}

.hot-video-actions {
    flex-shrink: 0;
}

.hot-video-actions .el-tag {
    font-size: 12px;
    padding: 4px 10px;
}

.pagination-wrapper {
    padding: 16px;
    border-top: 1px solid #e7e7e7;
    display: flex;
    justify-content: center;
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

.replace-dialog .dialog-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.badge {
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 500;
}

.new-badge {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
    color: white;
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

/* 当前视频标识 */
.carousel-option.is-current {
    opacity: 0.5;
}

.current-tag {
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 11px;
    background: #909399;
    color: #fff;
    margin-left: 6px;
}

.select-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 8px;
    padding: 8px 12px;
    background: #fff3cd;
    border: 1px solid #ffc107;
    border-radius: 4px;
    font-size: 12px;
    color: #856404;
}

.select-tip .iconfont {
    font-size: 14px;
}
</style>