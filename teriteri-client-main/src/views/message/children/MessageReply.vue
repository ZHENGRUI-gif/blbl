<template>
    <div class="message-reply">
        <!-- 操作栏 -->
        <div class="action-bar" v-if="commentList.length > 0">
            <div class="action-info">
                <span class="total-count">共收到 {{ commentList.length }} 条评论</span>
            </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" v-if="commentList.length > 0">
            <div class="comment-item" v-for="(comment, index) in commentList" :key="index">
                <div class="comment-card">
                    <!-- 评论者信息和评论内容 -->
                    <div class="comment-header">
                        <div class="user-section">
                            <VAvatar :img="comment.user.avatar_url" :size="48" :auth="comment.user.auth"></VAvatar>
                            <div class="user-info">
                                <div class="user-name" :class="comment.user.vip !== 0 ? 'vip-name' : ''">
                                    {{ comment.user.nickname }}
                                </div>
                                <div class="comment-time">{{ handleDateTime(comment.createTime) }}</div>
                            </div>
                        </div>
                        <div class="comment-action">
                            <div class="comment-icon">💬</div>
                            <span class="comment-text">评论了你的视频</span>
                        </div>
                    </div>

                    <!-- 评论内容 -->
                    <div class="comment-content">
                        <div class="comment-text">{{ comment.content }}</div>
                    </div>

                    <!-- 视频信息 -->
                    <div class="video-section" @click="openVideo(comment.vid)">
                        <div class="video-cover">
                            <img :src="getVideoCover(comment.vid)" :alt="getVideoTitle(comment.vid)" />
                            <div class="video-duration">{{ getVideoDuration(comment.vid) }}</div>
                            <div class="play-overlay">
                                <svg class="play-icon" viewBox="0 0 24 24" fill="white">
                                    <path d="M8 5v14l11-7z"/>
                                </svg>
                            </div>
                        </div>
                        <div class="video-info">
                            <div class="video-title">{{ getVideoTitle(comment.vid) }}</div>
                            <div class="video-stats">{{ getVideoStats(comment.vid) }}</div>
                        </div>
                    </div>

                    <!-- 回复评论 -->
                    <div class="replies-section" v-if="comment.replies && comment.replies.length > 0">
                        <div class="replies-header">
                            <span class="replies-count">{{ comment.replies.length }} 条回复</span>
                        </div>
                        <div class="reply-item" v-for="(reply, replyIndex) in comment.replies" :key="replyIndex">
                            <div class="reply-header">
                                <VAvatar :img="reply.user.avatar_url" :size="32" :auth="reply.user.auth"></VAvatar>
                                <div class="reply-user-info">
                                    <span class="reply-user-name">{{ reply.user.nickname }}</span>
                                    <span class="reply-time">{{ handleDateTime(reply.createTime) }}</span>
                                </div>
                            </div>
                            <div class="reply-content">
                                <span class="reply-to" v-if="reply.toUser">@{{ reply.toUser.nickname }} </span>
                                <span class="reply-text">{{ reply.content }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else-if="!loading">
            <div class="empty-icon">💬</div>
            <div class="empty-text">还没有收到评论哦</div>
        </div>

        <!-- 加载中 -->
        <div class="loading" v-if="loading">
            <div class="loading-text">加载中...</div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore && !loading && commentList.length > 0">
            <button @click="loadMore" class="load-more-btn">加载更多</button>
        </div>
    </div>
</template>

<script>
import VAvatar from '@/components/avatar/VAvatar.vue';
import { handleNum, handleDateTime } from '@/utils/utils.js';

export default {
    name: "MessageReply",
    components: {
        VAvatar,
    },
    data() {
        return {
            commentList: [],
            loading: false,
            hasMore: true,
            offset: 0,
            quantity: 10,
            videoInfoCache: {}, // 缓存视频信息
        }
    },
    methods: {
        // 清除未读消息
        async clearUnread() {
            const formData = new FormData();
            formData.append("column", "reply");
            await this.$post("/msg-unread/clear", formData, {
                headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
            })
        },

        // 获取收到的评论列表
        async getReceivedComments() {
            if (this.loading || !this.hasMore) return;

            this.loading = true;
            console.log('开始获取收到的评论，offset:', this.offset, 'quantity:', this.quantity);
            try {
                const res = await this.$get("/msg-unread/reply-comments", {
                    params: {
                        offset: this.offset,
                        count: this.quantity
                    },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token")
                    }
                });

                if (res.data && res.data.data) {
                    const newComments = res.data.data.comments;
                    console.log('获取到的评论数据:', newComments);
                    console.log('评论数量:', newComments.length);
                    if (newComments.length < this.quantity) {
                        this.hasMore = false;
                    }
                    this.commentList = this.commentList.concat(newComments);
                    this.offset += newComments.length;

                    // 预加载视频信息
                    this.preloadVideoInfo(newComments);
                } else {
                    console.log('API返回数据格式错误:', res);
                }
            } catch (error) {
                console.error('获取收到的评论失败:', error);
            } finally {
                this.loading = false;
            }
        },

        // 预加载视频信息
        async preloadVideoInfo(comments) {
            const videoIds = [...new Set(comments.map(comment => comment.vid))];
            for (const vid of videoIds) {
                if (!this.videoInfoCache[vid]) {
                    await this.loadVideoInfo(vid);
                }
            }
        },

        // 加载视频信息
        async loadVideoInfo(vid) {
            try {
                const res = await this.$get("/video/getone", {
                    params: { vid },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("teri_token")
                    }
                });
                if (res.data && res.data.data) {
                    this.videoInfoCache[vid] = res.data.data.video;
                }
            } catch (error) {
                console.error('加载视频信息失败:', error);
            }
        },

        // 获取视频封面
        getVideoCover(vid) {
            return this.videoInfoCache[vid]?.coverUrl || '';
        },

        // 获取视频标题
        getVideoTitle(vid) {
            return this.videoInfoCache[vid]?.title || '未知视频';
        },

        // 获取视频时长
        getVideoDuration(vid) {
            const duration = this.videoInfoCache[vid]?.duration;
            if (!duration) return '00:00';
            const minutes = Math.floor(duration / 60);
            const seconds = Math.floor(duration % 60);
            return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        },

        // 获取视频统计信息
        getVideoStats(vid) {
            const video = this.videoInfoCache[vid];
            if (!video) return '';
            // 从video对象的属性中获取统计信息
            const playCount = video.play || 0;
            const likeCount = video.good || 0;
            return `${handleNum(playCount)} 播放 · ${handleNum(likeCount)} 点赞`;
        },

        // 打开视频
        openVideo(vid) {
            this.$router.push(`/detail/${vid}`);
        },

        // 加载更多
        loadMore() {
            this.getReceivedComments();
        },

        // 处理时间显示
        handleDateTime(time) {
            return handleDateTime(time);
        }
    },
    created() {
        this.clearUnread();
        this.getReceivedComments();
    }
}
</script>

<style scoped>
.message-reply {
    padding: 20px 24px;
    height: 100%;
    overflow-y: auto;
}

/* 操作栏 */
.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    margin-bottom: 20px;
    border-bottom: 1px solid #e9ecef;
}

.action-info {
    display: flex;
    align-items: center;
}

.total-count {
    font-size: 16px;
    color: #333;
    font-weight: 600;
}

/* 评论列表 */
.comment-list {
    margin-bottom: 20px;
}

.comment-item {
    margin-bottom: 16px;
}

.comment-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.comment-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

/* 评论头部 */
.comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px 16px;
    border-bottom: 1px solid #f0f0f0;
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

.comment-time {
    font-size: 13px;
    color: #666;
    font-weight: 400;
}

.comment-action {
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
    color: white;
    padding: 8px 16px;
    border-radius: 20px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(255, 107, 157, 0.3);
}

.comment-icon {
    font-size: 16px;
    margin-right: 6px;
}

.comment-text {
    font-size: 14px;
}

/* 评论内容 */
.comment-content {
    padding: 20px 24px;
    background-color: #fafbfc;
    border-bottom: 1px solid #f0f0f0;
}

.comment-text {
    font-size: 15px;
    color: #333;
    line-height: 1.5;
    word-break: break-word;
}

/* 视频信息 */
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

/* 回复评论 */
.replies-section {
    padding: 20px 24px;
    background-color: #fafbfc;
    border-top: 1px solid #f0f0f0;
}

.replies-header {
    margin-bottom: 16px;
}

.replies-count {
    font-size: 14px;
    color: #666;
    font-weight: 500;
}

.reply-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 16px;
    padding: 16px;
    background-color: #fff;
    border-radius: 8px;
    border-left: 3px solid var(--brand_pink);
}

.reply-item:last-child {
    margin-bottom: 0;
}

.reply-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.reply-user-info {
    margin-left: 12px;
    display: flex;
    align-items: center;
    gap: 12px;
}

.reply-user-name {
    font-size: 14px;
    color: #333;
    font-weight: 500;
}

.reply-time {
    font-size: 12px;
    color: #999;
}

.reply-content {
    margin-left: 44px;
    font-size: 14px;
    color: #333;
    line-height: 1.5;
}

.reply-to {
    color: var(--brand_pink);
    font-weight: 500;
}

.reply-text {
    word-break: break-word;
}

/* 空状态 */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 400px;
    color: #999;
    padding: 40px;
}

.empty-icon {
    font-size: 64px;
    margin-bottom: 24px;
    opacity: 0.6;
}

.empty-text {
    font-size: 18px;
    color: #666;
    margin-bottom: 8px;
    font-weight: 500;
}

/* 加载状态 */
.loading {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
    color: #999;
}

.loading-text {
    font-size: 16px;
    font-weight: 500;
}

/* 加载更多 */
.load-more {
    display: flex;
    justify-content: center;
    padding: 20px;
}

.load-more-btn {
    background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
    color: white;
    border: none;
    padding: 12px 32px;
    border-radius: 24px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}

.load-more-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(255, 107, 157, 0.4);
}

.load-more-btn:active {
    transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .message-reply {
        padding: 16px;
    }

    .comment-header {
        padding: 16px 20px;
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
    }

    .comment-content {
        padding: 16px 20px;
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

    .replies-section {
        padding: 16px 20px;
    }

    .reply-content {
        margin-left: 0;
        margin-top: 8px;
    }
}
</style>