package com.teriteri.backend.pojo;

import java.time.LocalDateTime;

public class Follow {
    private Integer id;
    private Integer followerId;
    private Integer followingId;
    private LocalDateTime createDate;
    private Integer isDeleted;
    private LocalDateTime deleteDate;
    
    public Follow() {}
    
    public Follow(Integer followerId, Integer followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.createDate = LocalDateTime.now();
        this.isDeleted = 0;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getFollowerId() {
        return followerId;
    }
    
    public void setFollowerId(Integer followerId) {
        this.followerId = followerId;
    }
    
    public Integer getFollowingId() {
        return followingId;
    }
    
    public void setFollowingId(Integer followingId) {
        this.followingId = followingId;
    }
    
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
    
    public Integer getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public LocalDateTime getDeleteDate() {
        return deleteDate;
    }
    
    public void setDeleteDate(LocalDateTime deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    @Override
    public String toString() {
        return "Follow{" +
                "id=" + id +
                ", followerId=" + followerId +
                ", followingId=" + followingId +
                ", createDate=" + createDate +
                ", isDeleted=" + isDeleted +
                ", deleteDate=" + deleteDate +
                '}';
    }
}
