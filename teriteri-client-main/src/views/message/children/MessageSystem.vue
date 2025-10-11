<template>
    <div class="message-system">
        <div class="system-message-list" v-if="messageList.length > 0">
            <div class="message-item" v-for="message in messageList" :key="message.id">
                <div class="message-avatar" @click="goToUserSpace(message.fromUserId)">
                    <img :src="message.fromUserAvatar || '/img/default-avatar.png'" 
                         :alt="message.fromUserNickname"
                         @error="handleAvatarError">
                </div>
                <div class="message-content">
                    <div class="message-text">
                        <span class="user-name" @click="goToUserSpace(message.fromUserId)">
                            {{ message.fromUserNickname }}
                        </span>
                        {{ message.messageType === 'follow' ? ' 关注了你' : ' 取消关注了你' }}
                    </div>
                    <div class="message-time">
                        {{ formatTime(message.createTime) }}
                    </div>
                </div>
                <div class="message-actions">
                    <!-- 关注按钮 - 只对关注消息显示，且不是自己 -->
                    <div class="follow-action" v-if="message.messageType === 'follow' && message.fromUserId !== currentUserId">
                        <button 
                            class="follow-btn" 
                            :class="{ 'following': message.isFollowing }"
                            @click="toggleFollow(message)"
                            :disabled="message.followLoading"
                        >
                            <span v-if="message.followLoading">处理中...</span>
                            <span v-else>{{ message.isFollowing ? '已关注' : '关注' }}</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="empty-state" v-else-if="!loading">
            <div class="empty-icon">📭</div>
            <div class="empty-text">暂无系统消息</div>
        </div>
        
        <div class="loading-state" v-if="loading">
            <div class="loading-text">加载中...</div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination" v-if="totalPages > 1">
            <button @click="loadPage(currentPage - 1)" :disabled="currentPage <= 1">上一页</button>
            <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
            <button @click="loadPage(currentPage + 1)" :disabled="currentPage >= totalPages">下一页</button>
        </div>
    </div>
</template>

<script>
export default {
    name: "MessageSystem",
    data() {
        return {
            messageList: [],
            loading: false,
            currentPage: 1,
            pageSize: 10,
            totalPages: 0,
            total: 0
        }
    },
    computed: {
        currentUserId() {
            return this.$store.state.user.uid;
        }
    },
    async mounted() {
        await this.clearUnread();
        await this.loadSystemMessages();
    },
    methods: {
        async clearUnread() {
            try {
                const formData = new FormData();
                formData.append("column", "system");
                await this.$post("/msg-unread/clear", formData, {
                    headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
                });
            } catch (error) {
                console.error('清除系统消息未读数失败:', error);
            }
        },
        
        async loadSystemMessages() {
            this.loading = true;
            try {
                const res = await this.$get('/message/system/list', {
                    params: {
                        page: this.currentPage,
                        pageSize: this.pageSize
                    },
                    headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
                });
                
                if (res && res.data && res.data.code === 200) {
                    const data = res.data.data;
                    this.messageList = data.messages || [];
                    this.total = data.total || 0;
                    this.totalPages = data.totalPages || 0;
                    
                    // 为每个消息添加关注状态和加载状态
                    this.messageList.forEach(message => {
                        message.isFollowing = false;
                        message.followLoading = false;
                    });
                    
                    // 检查关注状态
                    this.checkFollowStatuses();
                } else {
                    const errorMsg = res?.data?.message || '获取系统消息失败';
                    this.$message.error(errorMsg);
                }
            } catch (error) {
                console.error('获取系统消息失败:', error);
                let errorMsg = '获取系统消息失败';
                if (error.response) {
                    // 服务器响应了错误状态码
                    if (error.response.status === 403) {
                        errorMsg = '权限不足，请重新登录';
                    } else if (error.response.status === 401) {
                        errorMsg = '未登录，请先登录';
                    } else {
                        errorMsg = error.response.data?.message || `请求失败 (${error.response.status})`;
                    }
                } else if (error.request) {
                    // 请求已发出但没有收到响应
                    errorMsg = '网络连接失败，请检查网络';
                }
                this.$message.error(errorMsg);
            } finally {
                this.loading = false;
            }
        },
        
        loadPage(page) {
            if (page >= 1 && page <= this.totalPages) {
                this.currentPage = page;
                this.loadSystemMessages();
            }
        },
        
        formatTime(timeString) {
            if (!timeString) return '';
            
            const time = new Date(timeString);
            const now = new Date();
            const diff = now - time;
            
            // 小于1分钟
            if (diff < 60000) {
                return '刚刚';
            }
            // 小于1小时
            if (diff < 3600000) {
                return Math.floor(diff / 60000) + '分钟前';
            }
            // 小于1天
            if (diff < 86400000) {
                return Math.floor(diff / 3600000) + '小时前';
            }
            // 小于1周
            if (diff < 604800000) {
                return Math.floor(diff / 86400000) + '天前';
            }
            // 超过1周，显示具体日期
            return time.toLocaleDateString();
        },
        
        handleAvatarError(event) {
            event.target.src = '/img/default-avatar.png';
        },
        
        // 检查所有消息的关注状态
        async checkFollowStatuses() {
            if (!this.currentUserId) return;
            
            const followPromises = this.messageList
                .filter(message => message.messageType === 'follow' && message.fromUserId !== this.currentUserId)
                .map(message => this.checkFollowStatus(message));
            
            await Promise.all(followPromises);
        },
        
        // 检查单个用户的关注状态
        async checkFollowStatus(message) {
            try {
                const res = await this.$get('/follow/check', {
                    params: { followingId: message.fromUserId },
                    headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
                });
                
                if (res && res.data && res.data.code === 200) {
                    message.isFollowing = res.data.data;
                }
            } catch (error) {
                console.error('检查关注状态失败:', error);
            }
        },
        
        // 切换关注状态
        async toggleFollow(message) {
            if (!this.currentUserId) {
                this.$message.warning('请先登录');
                return;
            }
            
            if (message.followLoading) return;
            
            message.followLoading = true;
            
            try {
                const formData = new FormData();
                formData.append('followingId', message.fromUserId);
                
                const res = await this.$post("/follow/toggle", 
                    formData,
                    { headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") } }
                );
                
                if (res.data && res.data.code === 200) {
                    message.isFollowing = res.data.data;
                    this.$message.success(res.data.message);
                } else {
                    this.$message.error(res.data?.message || "操作失败");
                }
            } catch (error) {
                console.error("关注操作失败:", error);
                this.$message.error("操作失败，请稍后重试");
            } finally {
                message.followLoading = false;
            }
        },
        
        // 跳转到用户空间
        goToUserSpace(uid) {
            if (uid && uid !== this.currentUserId) {
                this.$router.push(`/space/${uid}`);
            }
        }
    }
}
</script>

<style scoped>
.message-system {
    padding: 20px;
    min-height: 400px;
}

.system-message-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.message-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    gap: 16px;
}

.message-item:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
}

.message-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;
    cursor: pointer;
    transition: transform 0.2s ease;
}

.message-avatar:hover {
    transform: scale(1.05);
}

.message-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.message-content {
    flex: 1;
    min-width: 0;
}

.message-text {
    font-size: 14px;
    color: #333;
    margin-bottom: 4px;
    line-height: 1.4;
}

.user-name {
    color: #1890ff;
    cursor: pointer;
    font-weight: 500;
}

.user-name:hover {
    text-decoration: underline;
}

.message-time {
    font-size: 12px;
    color: #999;
}

.message-actions {
    display: flex;
    align-items: center;
    flex-shrink: 0;
}

.follow-action {
    display: flex;
    align-items: center;
}

.follow-btn {
    padding: 6px 16px;
    border: 1px solid #1890ff;
    background: #fff;
    color: #1890ff;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
    font-weight: 500;
    transition: all 0.3s ease;
    min-width: 60px;
    text-align: center;
}

.follow-btn:hover:not(:disabled) {
    background: #1890ff;
    color: #fff;
}

.follow-btn.following {
    background: #f0f0f0;
    border-color: #d9d9d9;
    color: #999;
}

.follow-btn.following:hover:not(:disabled) {
    background: #ff4d4f;
    border-color: #ff4d4f;
    color: #fff;
}

.follow-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: #999;
}

.empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
}

.empty-text {
    font-size: 16px;
}

.loading-state {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 60px 20px;
}

.loading-text {
    color: #999;
    font-size: 14px;
}

.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    margin-top: 24px;
    padding: 16px;
}

.pagination button {
    padding: 8px 16px;
    border: 1px solid #d9d9d9;
    background: #fff;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.pagination button:hover:not(:disabled) {
    border-color: #1890ff;
    color: #1890ff;
}

.pagination button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.page-info {
    color: #666;
    font-size: 14px;
}
</style>