-- 创建关注关系表
CREATE TABLE IF NOT EXISTS `follow` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '关注关系ID',
  `follower_id` INT(11) NOT NULL COMMENT '关注者用户ID',
  `following_id` INT(11) NOT NULL COMMENT '被关注者用户ID',
  `create_date` DATETIME NOT NULL COMMENT '关注时间',
  `is_deleted` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0否 1是',
  `delete_date` DATETIME DEFAULT NULL COMMENT '取消关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_follow` (`follower_id`, `following_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_following` (`following_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='用户关注关系表';

-- 添加外键约束（可选）
-- ALTER TABLE `follow` 
-- ADD CONSTRAINT `fk_follow_follower` 
-- FOREIGN KEY (`follower_id`) REFERENCES `user`(`uid`) ON DELETE CASCADE;

-- ALTER TABLE `follow` 
-- ADD CONSTRAINT `fk_follow_following` 
-- FOREIGN KEY (`following_id`) REFERENCES `user`(`uid`) ON DELETE CASCADE;
