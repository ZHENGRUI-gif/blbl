<template>
    <div class="flex-fill">
        <div class="v-container">
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

        <!-- 添加/编辑对话框 -->
        <el-dialog 
            :title="dialogTitle" 
            v-model="dialogVisible" 
            width="600px"
            @close="resetForm"
        >
            <el-form :model="form" label-width="100px">
                <el-form-item label="视频ID" v-if="!isEdit">
                    <el-input 
                        v-model.number="form.vid" 
                        type="number" 
                        placeholder="请输入视频ID"
                        @blur="loadVideoInfo"
                    >
                        <template #append>
                            <el-button @click="loadVideoInfo">查询</el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="视频ID" v-else>
                    <el-input v-model="form.vid" disabled></el-input>
                </el-form-item>
                <el-form-item label="视频信息" v-if="videoInfo.title">
                    <div class="video-info">
                        <img :src="videoInfo.coverUrl" class="video-preview" alt="">
                        <div class="video-detail">
                            <div class="video-title">{{ videoInfo.title }}</div>
                            <div class="video-status">
                                状态: 
                                <span :style="{color: videoInfo.status === 1 ? '#67c23a' : '#f56c6c'}">
                                    {{ getVideoStatusText(videoInfo.status) }}
                                </span>
                            </div>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="底色">
                    <el-input v-model="form.color" placeholder="请输入底色，如 #ca8d6b">
                        <template #prepend>
                            <div :style="{backgroundColor: form.color, width: '30px', height: '30px'}"></div>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="排序">
                    <el-input-number v-model="form.sort" :min="0" :max="999"></el-input-number>
                    <span style="color: #909399; font-size: 12px; margin-left: 10px;">数字越小越靠前</span>
                </el-form-item>
                <el-form-item label="状态" v-if="isEdit">
                    <el-switch
                        v-model="form.status"
                        :active-value="1"
                        :inactive-value="0"
                        active-text="启用"
                        inactive-text="禁用"
                    ></el-switch>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitForm" :disabled="!videoInfo.title">确定</el-button>
                </span>
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
            }
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
        }
    },
    async created() {
        await this.loadCarousels();
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

.video-info {
    display: flex;
    gap: 15px;
    padding: 10px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background-color: #f5f7fa;
}

.video-preview {
    width: 120px;
    height: 68px;
    object-fit: cover;
    border-radius: 4px;
}

.video-detail {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.video-title {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 8px;
    color: #303133;
}

.video-status {
    font-size: 12px;
    color: #606266;
}
</style>