/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : beidou

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 19/09/2024 20:22:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gachapon_reward_pool
-- ----------------------------
DROP TABLE IF EXISTS `gachapon_reward_pool`;
CREATE TABLE `gachapon_reward_pool`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `name` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '转蛋机奖池名称',
  `gachapon_id` int NOT NULL COMMENT '绑定转蛋机ID',
  `weight` int NOT NULL COMMENT '权重',
  `is_public` tinyint(1) UNSIGNED ZEROFILL NOT NULL DEFAULT 0 COMMENT '是否公共奖池 0为否 1为是',
  `prob` int NOT NULL DEFAULT 0 COMMENT '概率',
  `start_time` datetime NOT NULL COMMENT '奖池的启用日期',
  `end_time` datetime NULL DEFAULT NULL COMMENT '奖池的结束日期',
  `notification` tinyint(1) UNSIGNED ZEROFILL NOT NULL DEFAULT 0 COMMENT '是否喇叭通知 0为否 1为是',
  `comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gachapon_reward_pool
-- ----------------------------
INSERT INTO `gachapon_reward_pool` VALUES (1, '9100100(CommonItems)', 9100100, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (2, '9100101(CommonItems)', 9100101, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (3, '9100102(CommonItems)', 9100102, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (4, '9100103(CommonItems)', 9100103, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (5, '9100104(CommonItems)', 9100104, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (6, '9100105(CommonItems)', 9100105, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (7, '9100106(CommonItems)', 9100106, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (8, '9100107(CommonItems)', 9100107, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (9, '9100108(CommonItems)', 9100108, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (10, '9100109(CommonItems)', 9100109, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (11, '9100110(CommonItems)', 9100110, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (12, '9100117(CommonItems)', 9100117, 9000, 0, 0, '2024-09-19 19:00:54', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (13, '9100100(UncommonItems)', 9100100, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (14, '9100101(UncommonItems)', 9100101, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (15, '9100102(UncommonItems)', 9100102, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (16, '9100103(UncommonItems)', 9100103, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (17, '9100104(UncommonItems)', 9100104, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (18, '9100105(UncommonItems)', 9100105, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (19, '9100106(UncommonItems)', 9100106, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (20, '9100107(UncommonItems)', 9100107, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (21, '9100108(UncommonItems)', 9100108, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (22, '9100109(UncommonItems)', 9100109, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (23, '9100110(UncommonItems)', 9100110, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (24, '9100117(UncommonItems)', 9100117, 800, 0, 0, '2024-09-19 19:02:30', NULL, 0, '');
INSERT INTO `gachapon_reward_pool` VALUES (25, '9100100(RareItems)', 9100100, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (26, '9100101(RareItems)', 9100101, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (27, '9100102(RareItems)', 9100102, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (28, '9100103(RareItems)', 9100103, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (29, '9100104(RareItems)', 9100104, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (30, '9100105(RareItems)', 9100105, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (31, '9100106(RareItems)', 9100106, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (32, '9100107(RareItems)', 9100107, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (33, '9100108(RareItems)', 9100108, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (34, '9100109(RareItems)', 9100109, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (35, '9100110(RareItems)', 9100110, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');
INSERT INTO `gachapon_reward_pool` VALUES (36, '9100117(RareItems)', 9100117, 200, 0, 0, '2024-09-19 19:04:16', NULL, 1, '');

SET FOREIGN_KEY_CHECKS = 1;
