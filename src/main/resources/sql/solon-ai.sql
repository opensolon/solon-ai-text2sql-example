/*
 Navicat Premium Dump SQL

 Source Server         : 本机
 Source Server Type    : MySQL
 Source Server Version : 90001 (9.0.1)
 Source Host           : localhost:3306
 Source Schema         : solon-ai

 Target Server Type    : MySQL
 Target Server Version : 90001 (9.0.1)
 File Encoding         : 65001

 Date: 28/05/2025 13:44:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for class_info
-- ----------------------------
DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `class_id` tinyint NULL DEFAULT NULL COMMENT '班级id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班级名',
  `class_teacher_id` bigint NOT NULL COMMENT '教师id',
  `create_time` datetime NOT NULL COMMENT '创建时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班级信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_info
-- ----------------------------
INSERT INTO `class_info` VALUES (1, 1, '一年级', 1, '2023-07-05 09:06:07');
INSERT INTO `class_info` VALUES (3, 2, '二年级', 2, '2023-07-05 09:06:07');

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `age` int NOT NULL COMMENT '年龄',
  `sex` tinyint NOT NULL COMMENT '性别;1:男性；2：女性',
  `enrollment_time` datetime NOT NULL COMMENT '入学时间',
  `student_class` tinyint NOT NULL COMMENT '所属班级',
  `create_time` datetime NOT NULL COMMENT '创建时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (1, '王小明', 8, 1, '2025-09-01 00:00:00', 1, '2025-09-01 00:00:00');
INSERT INTO `student` VALUES (2, '王奇奇', 8, 0, '2025-09-01 00:00:00', 1, '2025-09-01 00:00:00');
INSERT INTO `student` VALUES (3, '李明明', 9, 1, '2024-09-01 00:00:00', 2, '2024-09-01 00:00:00');

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `age` int NOT NULL COMMENT '年龄',
  `sex` tinyint NOT NULL COMMENT '性别;1:男性；2：女性',
  `teaching_time` int NOT NULL COMMENT '教学年限',
  `create_time` datetime NOT NULL COMMENT '创建时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教师信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, '张老师', 30, 1, 10, '2025-05-28 09:20:53');
INSERT INTO `teacher` VALUES (2, '李老师', 33, 0, 8, '2025-05-28 09:21:42');

-- ----------------------------
-- Table structure for train_question
-- ----------------------------
DROP TABLE IF EXISTS `train_question`;
CREATE TABLE `train_question`  (
  `question` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户问题',
  `answer_sql` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sql语句',
  `create_time` datetime NOT NULL COMMENT '创建时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '问题训练表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of train_question
-- ----------------------------
INSERT INTO `train_question` VALUES ('What are the details of all students, including their enrollment time, class, and record creation time?', 'select name, sex, age, enrollment_time, student_class, create_time from student', '2025-05-28 10:15:00');
INSERT INTO `train_question` VALUES ('请列出所有学生的姓名、性别、年龄、入学时间、所在班级及信息创建时间。', 'select name, sex, age, enrollment_time, student_class, create_time from student', '2025-05-28 10:36:02');

SET FOREIGN_KEY_CHECKS = 1;
