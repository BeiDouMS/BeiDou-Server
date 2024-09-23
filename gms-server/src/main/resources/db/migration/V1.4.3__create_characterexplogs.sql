/*
 Navicat Premium Data Transfer

 Source Server         : windows-server
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : 192.168.3.5:3307
 Source Schema         : beidou

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 21/09/2024 17:32:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for characterexplogs
-- ----------------------------
DROP TABLE IF EXISTS `characterexplogs`;
CREATE TABLE `characterexplogs`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `world_exp_rate` int(0) NULL DEFAULT NULL,
  `exp_coupon` int(0) NULL DEFAULT NULL,
  `gained_exp` bigint(0) NULL DEFAULT NULL,
  `current_exp` bigint(0) NULL DEFAULT NULL,
  `exp_gain_time` timestamp(0) NULL DEFAULT NULL,
  `charid` int(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
