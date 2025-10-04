-- 测试数据库中的点赞数据
-- 1. 查看用户表
SELECT uid, nickname, avatar FROM user LIMIT 5;

-- 2. 查看视频表
SELECT vid, uid, title, status FROM video LIMIT 5;

-- 3. 查看用户视频关联表（点赞记录）
SELECT id, uid, vid, love, love_time FROM user_video WHERE love = 1 LIMIT 10;

-- 4. 查看特定用户的视频
SELECT vid, title FROM video WHERE uid = 1 AND status != 3;

-- 5. 查看特定用户视频的点赞记录
SELECT uv.id, uv.uid, uv.vid, uv.love, uv.love_time, v.title 
FROM user_video uv 
JOIN video v ON uv.vid = v.vid 
WHERE uv.love = 1 
  AND uv.love_time IS NOT NULL 
  AND v.uid = 1 
  AND v.status != 3 
ORDER BY uv.love_time DESC 
LIMIT 10;

-- 6. 查看点赞者的用户信息
SELECT u.uid, u.nickname, u.avatar, uv.love_time
FROM user_video uv 
JOIN video v ON uv.vid = v.vid 
JOIN user u ON uv.uid = u.uid
WHERE uv.love = 1 
  AND uv.love_time IS NOT NULL 
  AND v.uid = 1 
  AND v.status != 3 
ORDER BY uv.love_time DESC 
LIMIT 5;
