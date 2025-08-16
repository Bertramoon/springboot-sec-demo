/* 创建测试数据库 */
CREATE DATABASE IF NOT EXISTS test;
USE test;
/* 创建用户表 */
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255)
);
/* 插入用户数据 */
INSERT INTO user (username, password) VALUES ('admin', 'admin123456');
INSERT INTO user (username, password) VALUES ('common', '123456');
