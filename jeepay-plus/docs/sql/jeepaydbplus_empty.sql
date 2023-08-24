/*
 Navicat Premium Data Transfer

 Source Server         : LocalMysql
 Source Server Type    : MySQL
 Source Server Version : 50736
 Source Host           : localhost:3306
 Source Schema         : jeepaydbplus

 Target Server Type    : MySQL
 Target Server Version : 50736
 File Encoding         : 65001

 Date: 17/08/2023 15:09:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_agent_account_history
-- ----------------------------
DROP TABLE IF EXISTS `t_agent_account_history`;
CREATE TABLE `t_agent_account_history`  (
  `agent_account_history_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理商商户号',
  `agent_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '代理商名字',
  `amount` bigint(20) NOT NULL COMMENT '变动金额',
  `before_balance` bigint(20) NOT NULL COMMENT '变更前账户余额',
  `after_balance` bigint(20) NOT NULL COMMENT '变更后账户余额',
  `fund_direction` tinyint(6) NOT NULL COMMENT '资金变动方向,1-加款,2-减款',
  `biz_type` tinyint(6) NOT NULL DEFAULT 1 COMMENT '业务类型,1-分润,2-提现,3-调账',
  `created_uid` bigint(20) NULL DEFAULT NULL COMMENT '创建者ID(2-提现,3-调账 操作时不为空)',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `pay_order_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '平台订单号',
  `pay_order_amount` bigint(20) NULL DEFAULT 0 COMMENT '订单金额',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `created_login_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者登录名',
  PRIMARY KEY (`agent_account_history_id`) USING BTREE,
  UNIQUE INDEX `id`(`agent_account_history_id`) USING BTREE,
  INDEX `agent_no_index`(`agent_no`, `pay_order_id`, `created_at`, `biz_type`, `fund_direction`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8512 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商资金账户流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_agent_account_history
-- ----------------------------
INSERT INTO `t_agent_account_history` VALUES (8503, 'A1691231069', '测试代理', 30, 0, 30, 1, 1, NULL, '2023-08-17 14:57:06.107830', 'P1692067678568984577', 2000, '', '');
INSERT INTO `t_agent_account_history` VALUES (8504, 'A1692255110', '测试通道代理', 20, 0, 20, 1, 1, NULL, '2023-08-17 14:57:06.109707', 'P1692067678568984577', 2000, '', '');
INSERT INTO `t_agent_account_history` VALUES (8505, 'A1691231069', '测试代理', 30, 30, 60, 1, 1, NULL, '2023-08-17 15:03:06.105399', 'P1692068961581514753', 2000, '', '');
INSERT INTO `t_agent_account_history` VALUES (8506, 'A1692255110', '测试通道代理', 20, 20, 40, 1, 1, NULL, '2023-08-17 15:03:06.107153', 'P1692068961581514753', 2000, '', '');
INSERT INTO `t_agent_account_history` VALUES (8507, 'A1691231069', '测试代理', 75, 60, 135, 1, 1, NULL, '2023-08-17 15:03:31.105139', 'P1692068961648623617', 5000, '', '');
INSERT INTO `t_agent_account_history` VALUES (8508, 'A1691231069', '测试代理', 300, 135, 435, 1, 1, NULL, '2023-08-17 15:03:36.111958', 'P1692068961384382465', 20000, '', '');
INSERT INTO `t_agent_account_history` VALUES (8509, 'A1691231069', '测试代理', 300, 435, 735, 1, 1, NULL, '2023-08-17 15:03:41.110878', 'P1692068961187250177', 20000, '', '');
INSERT INTO `t_agent_account_history` VALUES (8510, 'A1691231069', '测试代理', 300, 735, 1035, 1, 1, NULL, '2023-08-17 15:03:46.107923', 'P1692068961124335618', 20000, '', '');
INSERT INTO `t_agent_account_history` VALUES (8511, 'A1691231069', '测试代理', -75, 1035, 960, 2, 6, NULL, '2023-08-17 15:04:17.183390', 'P1692068961648623617', -5000, '', '');

-- ----------------------------
-- Table structure for t_agent_account_info
-- ----------------------------
DROP TABLE IF EXISTS `t_agent_account_info`;
CREATE TABLE `t_agent_account_info`  (
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理商商户号',
  `agent_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理商名称',
  `balance` bigint(20) NOT NULL DEFAULT 0 COMMENT '账户余额',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '账户状态,1-可用,0-停止使用',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户备注',
  `init_user_id` bigint(20) NULL DEFAULT NULL COMMENT '初始用户ID（创建时分配的用户ID）',
  `created_uid` bigint(20) NULL DEFAULT NULL COMMENT '创建者用户ID',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `freeze_balance` bigint(20) NULL DEFAULT 0 COMMENT '冻结金额',
  PRIMARY KEY (`agent_no`) USING BTREE,
  UNIQUE INDEX `id`(`agent_no`, `init_user_id`) USING BTREE,
  INDEX `agent_account_info_index`(`state`, `balance`, `freeze_balance`, `agent_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商账户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_agent_account_info
-- ----------------------------
INSERT INTO `t_agent_account_info` VALUES ('A1691231069', '测试代理', 960, 1, NULL, 100034, 801, '2023-08-05 18:24:29.000000', '2023-08-17 15:04:17.182182', 0);
INSERT INTO `t_agent_account_info` VALUES ('A1692255110', '测试通道代理', 40, 1, NULL, 100115, 801, '2023-08-17 14:51:50.680112', '2023-08-17 15:03:06.113268', 0);

-- ----------------------------
-- Table structure for t_agent_mch
-- ----------------------------
DROP TABLE IF EXISTS `t_agent_mch`;
CREATE TABLE `t_agent_mch`  (
  `agent_mch_id` bigint(20) NOT NULL COMMENT 'ID',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上级代理商商户号',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `product_id` bigint(20) NOT NULL COMMENT '对应产品ID',
  `rate` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '对应产品费率',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`agent_mch_id`) USING BTREE,
  UNIQUE INDEX `id`(`agent_mch_id`) USING BTREE,
  INDEX `index`(`agent_no`, `mch_no`, `product_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_agent_mch
-- ----------------------------

-- ----------------------------
-- Table structure for t_agent_passage
-- ----------------------------
DROP TABLE IF EXISTS `t_agent_passage`;
CREATE TABLE `t_agent_passage`  (
  `agent_passage_id` int(11) NOT NULL COMMENT 'ID',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '代理商商户号',
  `pay_passage_id` bigint(20) NOT NULL COMMENT '通道ID',
  `agent_rate` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '代理商费率,百分比',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`agent_passage_id`) USING BTREE,
  UNIQUE INDEX `id`(`agent_passage_id`) USING BTREE,
  INDEX `agent_passage_index`(`agent_no`, `pay_passage_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商-通道关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_agent_passage
-- ----------------------------

-- ----------------------------
-- Table structure for t_division_record
-- ----------------------------
DROP TABLE IF EXISTS `t_division_record`;
CREATE TABLE `t_division_record`  (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分账记录ID',
  `user_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号/代理商号',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户名称/代理商名称',
  `user_type` tinyint(6) NULL DEFAULT 0 COMMENT '用户类型:1-商户，2-代理',
  `pay_type` tinyint(6) NOT NULL DEFAULT 0 COMMENT '结算模式：0-手动结算，1-api结算',
  `division_passage_id` bigint(20) NULL DEFAULT NULL COMMENT '结算通道ID',
  `pay_order_channel_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '支付订单渠道支付订单号',
  `amount` bigint(20) NOT NULL COMMENT '申请金额,单位分',
  `division_amount` bigint(20) NOT NULL COMMENT '订单实际分账金额, 单位：分（订单金额 - 手续费）',
  `division_fee_rate` decimal(20, 6) NOT NULL COMMENT '费率',
  `division_amount_fee` bigint(20) NOT NULL COMMENT '手续费, 单位：分',
  `channel_batch_order_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '分账批次号',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '状态: 1-待结算 2-结算成功, 3-结算失败(取消)，4-超时关闭',
  `channel_resp_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '上游返回数据包',
  `acc_type` tinyint(6) NOT NULL COMMENT '账号快照》 分账接收账号类型: 0-银行卡 1-USDT 2-其他',
  `acc_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '账号快照》 分账接收账号/地址',
  `acc_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '账号快照》 分账接收账号名称',
  `cal_division_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '实际接收金额,单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `expired_time` datetime NULL DEFAULT NULL COMMENT '申请失效时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `id`(`record_id`) USING BTREE,
  INDEX `index`(`user_no`, `user_name`, `user_type`, `pay_type`, `division_passage_id`, `pay_order_channel_order_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 84 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分账记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_division_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_mch_history
-- ----------------------------
DROP TABLE IF EXISTS `t_mch_history`;
CREATE TABLE `t_mch_history`  (
  `mch_history_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `mch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户名',
  `amount` bigint(20) NOT NULL COMMENT '变动金额',
  `before_balance` bigint(20) NOT NULL COMMENT '变更前账户余额',
  `after_balance` bigint(20) NOT NULL COMMENT '变更后账户余额',
  `mch_rate_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '单笔手续费',
  `fund_direction` tinyint(6) NOT NULL COMMENT '资金变动方向,1-加款,2-减款',
  `biz_type` tinyint(6) NOT NULL DEFAULT 1 COMMENT '业务类型,1-支付,2-提现,3-调账',
  `created_uid` bigint(20) NULL DEFAULT NULL COMMENT '创建者id(2-提现,3-调账 操作时不为空)',
  `created_login_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建者姓名(2-提现,3-调账 操作时不为空)',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `pay_order_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '平台订单号',
  `pay_order_amount` bigint(20) NULL DEFAULT 0 COMMENT '订单金额',
  `passage_order_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '通道订单号',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '备注',
  `plat_income` bigint(20) NULL DEFAULT 0 COMMENT '平台收入',
  `agent_income` bigint(20) NULL DEFAULT 0 COMMENT '商户代理收入',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '代理商户号',
  `agent_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '代理商名字',
  `mch_order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户订单号',
  PRIMARY KEY (`mch_history_id`) USING BTREE,
  UNIQUE INDEX `id`(`mch_history_id`) USING BTREE,
  INDEX `mch_history_index`(`mch_no`, `fund_direction`, `biz_type`, `pay_order_id`, `passage_order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40775 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户资金账户流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_history
-- ----------------------------
INSERT INTO `t_mch_history` VALUES (40768, 'M1691231056', '测试商户', 1700, 0, 1700, 300, 1, 1, NULL, '', '2023-08-17 14:57:06.105050', 'P1692067678568984577', 2000, '', '', 70, 30, 'A1691231069', '测试代理', 'AZsHBX8gtgUGeuj');
INSERT INTO `t_mch_history` VALUES (40769, 'M1691231056', '测试商户', 1700, 1700, 3400, 300, 1, 1, NULL, '', '2023-08-17 15:03:06.103634', 'P1692068961581514753', 2000, '', '', 70, 30, 'A1691231069', '测试代理', 'XceEIzfKwkeE3DA');
INSERT INTO `t_mch_history` VALUES (40770, 'M1691231056', '测试商户', 4250, 3400, 7650, 750, 1, 1, NULL, '', '2023-08-17 15:03:31.102634', 'P1692068961648623617', 5000, '', '', 275, 75, 'A1691231069', '测试代理', 'ADXwjziSpy7Gwok');
INSERT INTO `t_mch_history` VALUES (40771, 'M1691231056', '测试商户', 17000, 7650, 24650, 3000, 1, 1, NULL, '', '2023-08-17 15:03:36.109519', 'P1692068961384382465', 20000, '', '', 1100, 300, 'A1691231069', '测试代理', 'oouje5vhM5cImSw');
INSERT INTO `t_mch_history` VALUES (40772, 'M1691231056', '测试商户', 17000, 24650, 41650, 3000, 1, 1, NULL, '', '2023-08-17 15:03:41.109473', 'P1692068961187250177', 20000, '', '', 1100, 300, 'A1691231069', '测试代理', 'Tj9kYzlYiOwt222');
INSERT INTO `t_mch_history` VALUES (40773, 'M1691231056', '测试商户', 17000, 41650, 58650, 3000, 1, 1, NULL, '', '2023-08-17 15:03:46.106023', 'P1692068961124335618', 20000, '', '', 1100, 300, 'A1691231069', '测试代理', 'JclIH1nnXe5BhLY');
INSERT INTO `t_mch_history` VALUES (40774, 'M1691231056', '测试商户', -4250, 58650, 54400, -750, 2, 6, NULL, '', '2023-08-17 15:04:17.181153', 'P1692068961648623617', -5000, '', '', -275, -75, 'A1691231069', '测试代理', 'ADXwjziSpy7Gwok');

-- ----------------------------
-- Table structure for t_mch_info
-- ----------------------------
DROP TABLE IF EXISTS `t_mch_info`;
CREATE TABLE `t_mch_info`  (
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `mch_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户名称',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '商户状态: 0-停用, 1-正常',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户备注',
  `init_user_id` bigint(20) NULL DEFAULT NULL COMMENT '初始用户ID（创建商户时，初始用户ID）',
  `created_uid` bigint(20) NOT NULL COMMENT '创建者用户ID',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `secret` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户密钥',
  `mch_group_id` bigint(20) NULL DEFAULT NULL COMMENT '商户分组ID',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上级代理商户号,为空则无',
  `balance` bigint(20) NOT NULL DEFAULT 0 COMMENT '商户余额',
  `freeze_balance` bigint(20) NULL DEFAULT 0 COMMENT '冻结金额',
  PRIMARY KEY (`mch_no`) USING BTREE,
  UNIQUE INDEX `id`(`mch_no`, `init_user_id`) USING BTREE,
  INDEX `index`(`state`, `mch_name`, `balance`, `created_uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_info
-- ----------------------------
INSERT INTO `t_mch_info` VALUES ('M1691231056', '测试商户', 1, NULL, 100033, 801, '2023-08-05 18:24:16.000000', '2023-08-17 15:04:17.179831', 'XFjGbHNUho8MqYWycYsNFzSwBVvOAIepDz1QDTfoIfKFojXVyTFHReNteWzGSVoXyXLJzDAo9p0G6GIjQYPAPJXSttlymJ9pqvaVUsna8v6cRezJvmTSJleEPImlTqDI', NULL, 'A1691231069', 54400, 0);

-- ----------------------------
-- Table structure for t_mch_notify_record
-- ----------------------------
DROP TABLE IF EXISTS `t_mch_notify_record`;
CREATE TABLE `t_mch_notify_record`  (
  `notify_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商户通知记录ID',
  `order_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单ID',
  `order_type` tinyint(6) NOT NULL COMMENT '订单类型:1-支付,2-代付,3-api提现',
  `passage_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通道订单号',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '代理商号',
  `pay_passage_id` bigint(20) NOT NULL COMMENT '通道ID',
  `notify_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知地址',
  `res_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '通知响应结果',
  `notify_count` int(11) NOT NULL DEFAULT 0 COMMENT '通知次数',
  `notify_count_limit` int(11) NOT NULL DEFAULT 6 COMMENT '最大通知次数, 默认6次',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '通知状态,1-通知中,2-通知成功,3-通知失败',
  `last_notify_time` datetime NULL DEFAULT NULL COMMENT '最后一次通知时间',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`notify_id`) USING BTREE,
  UNIQUE INDEX `Uni_OrderId_Type`(`order_id`) USING BTREE,
  INDEX `index`(`passage_order_no`, `state`, `order_type`, `order_id`, `mch_no`, `agent_no`, `pay_passage_id`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40598 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户通知记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_notify_record
-- ----------------------------
INSERT INTO `t_mch_notify_record` VALUES (40592, 'P1692067678568984577', 1, '', 'M1691231056', 'A1691231069', 1272, 'https://www.test.com?ifCode=testpay&amount=2000&payOrderId=P1692067678568984577&mchOrderNo=AZsHBX8gtgUGeuj&sign=24BC8C475586B2B84B947C2FCA689C03&reqTime=1692255422942&createdAt=1692255346336&clientIp=154.82.113.215&successTime=1692255423000&state=2&mchNo=M1691231056', '连接[www.test.com]异常:【SocketTimeoutException: connect timed out】', 6, 6, 3, '2023-08-17 14:59:04', '2023-08-17 14:57:02.946405', '2023-08-17 14:59:04.158326');
INSERT INTO `t_mch_notify_record` VALUES (40593, 'P1692068961581514753', 1, '', 'M1691231056', 'A1691231069', 1272, 'https://www.test.com?ifCode=testpay&amount=2000&payOrderId=P1692068961581514753&mchOrderNo=XceEIzfKwkeE3DA&sign=2D0E06509555AAB31A53DAD53AC78269&reqTime=1692255785275&createdAt=1692255652228&clientIp=154.82.113.215&successTime=1692255785000&state=2&mchNo=M1691231056', '连接[www.test.com]异常:【SocketTimeoutException: connect timed out】', 6, 6, 3, '2023-08-17 15:05:06', '2023-08-17 15:03:05.287868', '2023-08-17 15:05:06.256921');
INSERT INTO `t_mch_notify_record` VALUES (40594, 'P1692068961648623617', 1, '', 'M1691231056', 'A1691231069', 1273, 'https://www.test.com?ifCode=testpay&amount=5000&payOrderId=P1692068961648623617&mchOrderNo=ADXwjziSpy7Gwok&sign=DCA1F8D2EC4F9FD7395DA5793F45664B&reqTime=1692255809318&createdAt=1692255652242&clientIp=154.82.113.215&successTime=1692255809000&state=2&mchNo=M1691231056', '连接[www.test.com]异常:【SocketTimeoutException: connect timed out】', 6, 6, 3, '2023-08-17 15:05:29', '2023-08-17 15:03:29.321354', '2023-08-17 15:05:29.419086');
INSERT INTO `t_mch_notify_record` VALUES (40595, 'P1692068961384382465', 1, '', 'M1691231056', 'A1691231069', 1273, 'https://www.test.com?ifCode=testpay&amount=20000&payOrderId=P1692068961384382465&mchOrderNo=oouje5vhM5cImSw&sign=7F5D1816CAA6AEA061985E776093D9E0&reqTime=1692255813704&createdAt=1692255652178&clientIp=154.82.113.215&successTime=1692255814000&state=2&mchNo=M1691231056', '连接[www.test.com]异常:【SocketTimeoutException: connect timed out】', 6, 6, 3, '2023-08-17 15:05:33', '2023-08-17 15:03:33.706348', '2023-08-17 15:05:33.831994');
INSERT INTO `t_mch_notify_record` VALUES (40596, 'P1692068961187250177', 1, '', 'M1691231056', 'A1691231069', 1273, 'https://www.test.com?ifCode=testpay&amount=20000&payOrderId=P1692068961187250177&mchOrderNo=Tj9kYzlYiOwt222&sign=862856A193EEC2DBFF1B43106F780137&reqTime=1692255818134&createdAt=1692255652137&clientIp=154.82.113.215&successTime=1692255818000&state=2&mchNo=M1691231056', '连接[www.test.com]异常:【SocketTimeoutException: connect timed out】', 6, 6, 3, '2023-08-17 15:05:38', '2023-08-17 15:03:38.136378', '2023-08-17 15:05:38.252795');
INSERT INTO `t_mch_notify_record` VALUES (40597, 'P1692068961124335618', 1, '', 'M1691231056', 'A1691231069', 1273, 'https://www.test.com?ifCode=testpay&amount=20000&payOrderId=P1692068961124335618&mchOrderNo=JclIH1nnXe5BhLY&sign=E9102EC8499F1DA7B859297414A075A7&reqTime=1692255822000&createdAt=1692255652118&clientIp=154.82.113.215&successTime=1692255822000&state=2&mchNo=M1691231056', '连接[www.test.com]异常:【SocketTimeoutException: connect timed out】', 6, 6, 3, '2023-08-17 15:05:42', '2023-08-17 15:03:42.002326', '2023-08-17 15:05:42.088918');

-- ----------------------------
-- Table structure for t_mch_pay_passage
-- ----------------------------
DROP TABLE IF EXISTS `t_mch_pay_passage`;
CREATE TABLE `t_mch_pay_passage`  (
  `mch_pay_passage_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `pay_passage_id` bigint(20) NOT NULL COMMENT '通道ID',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-正常',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`mch_pay_passage_id`) USING BTREE,
  UNIQUE INDEX `id`(`mch_pay_passage_id`) USING BTREE,
  INDEX `index`(`mch_no`, `pay_passage_id`, `state`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13080 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户-通道关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_pay_passage
-- ----------------------------
INSERT INTO `t_mch_pay_passage` VALUES (13078, 'M1691231056', 1272, 1, '2023-08-17 14:55:36.075733', '2023-08-17 14:55:36.075733');
INSERT INTO `t_mch_pay_passage` VALUES (13079, 'M1691231056', 1273, 1, '2023-08-17 14:59:59.021196', '2023-08-17 14:59:59.021196');

-- ----------------------------
-- Table structure for t_mch_product
-- ----------------------------
DROP TABLE IF EXISTS `t_mch_product`;
CREATE TABLE `t_mch_product`  (
  `mch_product_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `product_id` bigint(20) NOT NULL COMMENT '通道ID',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-正常',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `mch_rate` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '商户产品费率',
  `agent_rate` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '商户代理费率',
  PRIMARY KEY (`mch_product_id`) USING BTREE,
  UNIQUE INDEX `id`(`mch_product_id`) USING BTREE,
  INDEX `index`(`mch_no`, `product_id`, `state`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2793 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户-产品关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_product
-- ----------------------------
INSERT INTO `t_mch_product` VALUES (2792, 'M1691231056', 1000, 1, '2023-08-17 14:51:36.055149', '2023-08-17 14:51:36.055149', 0.150000, 0.015000);

-- ----------------------------
-- Table structure for t_passage_transaction_history
-- ----------------------------
DROP TABLE IF EXISTS `t_passage_transaction_history`;
CREATE TABLE `t_passage_transaction_history`  (
  `passage_transaction_history_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pay_passage_id` bigint(20) NOT NULL COMMENT '通道ID',
  `pay_passage_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通道名',
  `amount` bigint(20) NOT NULL COMMENT '变动金额',
  `before_balance` bigint(20) NOT NULL COMMENT '变更前账户余额',
  `after_balance` bigint(20) NOT NULL COMMENT '变更后账户余额',
  `fund_direction` tinyint(6) NOT NULL COMMENT '资金变动方向,1-加款,2-减款',
  `biz_type` tinyint(6) NOT NULL COMMENT '业务类型,4-订单,5-通道调账',
  `created_uid` bigint(20) NOT NULL COMMENT '操作者ID',
  `created_login_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '操作者 用户名',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备注',
  `pay_order_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '订单号',
  PRIMARY KEY (`passage_transaction_history_id`) USING BTREE,
  UNIQUE INDEX `id`(`passage_transaction_history_id`) USING BTREE,
  INDEX `index`(`pay_passage_id`, `fund_direction`, `pay_order_id`, `biz_type`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25443 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通道余额调额记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_passage_transaction_history
-- ----------------------------
INSERT INTO `t_passage_transaction_history` VALUES (25436, 1272, '测试通道', 1800, 0, 1800, 1, 4, 0, '', '2023-08-17 14:57:06.111732', '', 'P1692067678568984577');
INSERT INTO `t_passage_transaction_history` VALUES (25437, 1272, '测试通道', 1800, 1800, 3600, 1, 4, 0, '', '2023-08-17 15:03:06.109356', '', 'P1692068961581514753');
INSERT INTO `t_passage_transaction_history` VALUES (25438, 1273, '测试通道2', 4600, 0, 4600, 1, 4, 0, '', '2023-08-17 15:03:31.107667', '', 'P1692068961648623617');
INSERT INTO `t_passage_transaction_history` VALUES (25439, 1273, '测试通道2', 18400, 4600, 23000, 1, 4, 0, '', '2023-08-17 15:03:36.114031', '', 'P1692068961384382465');
INSERT INTO `t_passage_transaction_history` VALUES (25440, 1273, '测试通道2', 18400, 23000, 41400, 1, 4, 0, '', '2023-08-17 15:03:41.112474', '', 'P1692068961187250177');
INSERT INTO `t_passage_transaction_history` VALUES (25441, 1273, '测试通道2', 18400, 41400, 59800, 1, 4, 0, '', '2023-08-17 15:03:46.110145', '', 'P1692068961124335618');
INSERT INTO `t_passage_transaction_history` VALUES (25442, 1273, '测试通道2', -4600, 59800, 55200, 2, 4, 0, '', '2023-08-17 15:04:17.185420', '测试冲正', 'P1692068961648623617');

-- ----------------------------
-- Table structure for t_pay_interface_define
-- ----------------------------
DROP TABLE IF EXISTS `t_pay_interface_define`;
CREATE TABLE `t_pay_interface_define`  (
  `if_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口代码 全小写  wxpay alipay ',
  `if_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口名称',
  `if_params` json NOT NULL COMMENT '普通商户接口配置定义描述,json字符串',
  `bg_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '页面展示：卡片-背景色',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`if_code`) USING BTREE,
  UNIQUE INDEX `id`(`if_code`) USING BTREE,
  INDEX `index`(`if_name`, `state`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付接口定义表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_pay_interface_define
-- ----------------------------
INSERT INTO `t_pay_interface_define` VALUES ('benchi', '奔驰支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#818646', 1, NULL, '2023-08-15 11:56:21.533510', '2023-08-15 11:56:21.533510');
INSERT INTO `t_pay_interface_define` VALUES ('cardpay', '卡密支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"支付类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#D0AF6D', 1, NULL, '2023-07-27 11:45:58.000000', '2023-07-27 11:45:58.000000');
INSERT INTO `t_pay_interface_define` VALUES ('changsheng', '昌盛支付', '[{\"desc\": \"商户号AccessKey\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#121AA9', 1, NULL, '2023-08-11 11:14:04.000000', '2023-08-11 11:14:04.000000');
INSERT INTO `t_pay_interface_define` VALUES ('chuangxin', '创新支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#208684', 1, NULL, '2023-08-13 20:04:53.062313', '2023-08-13 20:04:53.062313');
INSERT INTO `t_pay_interface_define` VALUES ('gawasy', 'Gawasy支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#45CABB', 1, NULL, '2023-08-09 18:19:52.684416', '2023-08-09 18:19:52.684416');
INSERT INTO `t_pay_interface_define` VALUES ('jomalongpay', '祖玛珑支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"产品编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#065C6B', 1, NULL, '2023-08-05 22:44:38.000000', '2023-08-05 22:44:38.000000');
INSERT INTO `t_pay_interface_define` VALUES ('languifang', '兰桂坊支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"查询网关\", \"name\": \"queryUrl\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#B72E07', 1, NULL, '2023-08-05 23:21:43.441595', '2023-08-05 23:21:43.441595');
INSERT INTO `t_pay_interface_define` VALUES ('pay731', '731支付接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#7F71FE', 1, NULL, '2023-08-13 02:15:42.946802', '2023-08-13 02:15:42.946802');
INSERT INTO `t_pay_interface_define` VALUES ('qipay', '百川支付接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#CBD3CF', 1, NULL, '2023-08-05 20:20:53.000000', '2023-08-05 20:20:53.000000');
INSERT INTO `t_pay_interface_define` VALUES ('rixinpay', '日鑫支付接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#0CC145', 1, NULL, '2023-08-06 16:33:51.000000', '2023-08-06 16:33:51.000000');
INSERT INTO `t_pay_interface_define` VALUES ('shayupay', '鲨鱼支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#9248E8', 1, '', '2023-08-07 20:36:40.000000', '2023-08-07 20:36:40.000000');
INSERT INTO `t_pay_interface_define` VALUES ('shengyang', '盛阳支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#C9CD52', 1, NULL, '2023-08-09 00:40:06.000000', '2023-08-09 00:40:06.000000');
INSERT INTO `t_pay_interface_define` VALUES ('testpay', '测试接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"支付类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#E2791E', 1, NULL, '2023-06-29 01:30:32.000000', '2023-06-29 01:30:32.000000');
INSERT INTO `t_pay_interface_define` VALUES ('tianhepay', '天合支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#3E463F', 1, NULL, '2023-08-08 17:25:54.459954', '2023-08-08 17:25:54.459954');
INSERT INTO `t_pay_interface_define` VALUES ('xiaobawang', '小霸王支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#104A23', 1, NULL, '2023-08-15 12:25:42.096118', '2023-08-15 12:25:42.096118');
INSERT INTO `t_pay_interface_define` VALUES ('xiaoji', '小鸡支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"查询网关\", \"name\": \"queryUrl\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#38FFEB', 1, NULL, '2023-08-14 23:05:27.214268', '2023-08-14 23:05:27.214268');
INSERT INTO `t_pay_interface_define` VALUES ('xxpay', 'xxpay接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"产品编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#A0A351', 1, '库利南最支付丰胸鲁班买卖通', '2023-08-06 13:10:35.000000', '2023-08-06 13:10:35.000000');
INSERT INTO `t_pay_interface_define` VALUES ('xxpay2', 'XXPay2接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#CFE987', 1, NULL, '2023-08-07 20:37:33.000000', '2023-08-07 20:37:33.000000');
INSERT INTO `t_pay_interface_define` VALUES ('xxpay3', 'xxpay3接口道道', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#665929', 1, NULL, '2023-08-10 17:27:57.000000', '2023-08-10 17:27:57.000000');
INSERT INTO `t_pay_interface_define` VALUES ('xxpay4', 'XXPay4接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#319B48', 1, NULL, '2023-08-11 19:59:32.385876', '2023-08-11 19:59:32.385876');
INSERT INTO `t_pay_interface_define` VALUES ('yifupay', '亿付支付 ', '[{\"desc\": \"appId\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"支付方式\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#76E3FF', 1, NULL, '2023-07-27 23:24:25.000000', '2023-07-27 23:24:25.000000');
INSERT INTO `t_pay_interface_define` VALUES ('yongheng', '永恒支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#773B80', 1, NULL, '2023-08-13 16:42:15.848487', '2023-08-13 16:42:15.848487');

-- ----------------------------
-- Table structure for t_pay_order
-- ----------------------------
DROP TABLE IF EXISTS `t_pay_order`;
CREATE TABLE `t_pay_order`  (
  `pay_order_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付订单号',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '代理商号',
  `passage_id` bigint(20) NOT NULL COMMENT '通道ID',
  `mch_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户名称',
  `passage_order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '通道订单号',
  `if_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付接口代码',
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `product_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `amount` bigint(20) NOT NULL COMMENT '支付金额,单位分',
  `mch_fee_rate` decimal(20, 6) NOT NULL COMMENT '商户手续费费率快照',
  `mch_fee_amount` bigint(20) NOT NULL COMMENT '商户手续费,单位分',
  `agent_rate` decimal(20, 6) NULL DEFAULT NULL COMMENT '代理商费率快照',
  `agent_fee_amount` bigint(20) NULL DEFAULT NULL COMMENT '代理商分润,单位分',
  `state` tinyint(6) NOT NULL DEFAULT 0 COMMENT '支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭 ,7-出码失败',
  `notify_state` tinyint(6) NOT NULL DEFAULT 0 COMMENT '向下游回调状态, 0-未发送,  1-已发送',
  `notify_params` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '回调参数(三方回调我方接口)',
  `notify_resp` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '回调响应(我方给三方的响应)',
  `client_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端IP',
  `passage_resp` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '下单请求返回',
  `err_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道支付错误码',
  `err_msg` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道支付错误描述',
  `ext_param_json` json NULL COMMENT '订单扩展参数(暂时保留)',
  `notify_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '异步通知地址',
  `return_url` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '页面跳转地址',
  `expired_time` datetime NULL DEFAULT NULL COMMENT '订单失效时间',
  `success_time` datetime NULL DEFAULT NULL COMMENT '订单支付成功时间',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `mch_order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户订单号',
  `passage_rate` decimal(20, 6) NOT NULL COMMENT '通道费率快照',
  `passage_fee_amount` bigint(20) NOT NULL COMMENT '通道费用快照',
  `passage_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '三方通道用户标识',
  `agent_passage_fee` bigint(20) NULL DEFAULT NULL COMMENT '通道代理商手续费,单位分',
  `agent_passage_rate` decimal(20, 6) NULL DEFAULT NULL COMMENT '通道代理商费率',
  `agent_no_passage` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道的代理商户',
  `force_change_state` tinyint(6) NULL DEFAULT 0 COMMENT '是否手动补单:0-否，1-是',
  `force_change_login_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '手动补单操作人',
  `force_change_before_state` tinyint(6) NULL DEFAULT NULL COMMENT '手动补单前订单状态',
  PRIMARY KEY (`pay_order_id`) USING BTREE,
  UNIQUE INDEX `id`(`pay_order_id`) USING BTREE,
  INDEX `created_at`(`created_at`) USING BTREE,
  INDEX `Uni_MchNo_MchOrderNo`(`mch_no`, `passage_order_no`, `agent_no`, `product_id`, `state`, `mch_order_no`, `force_change_state`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_pay_order
-- ----------------------------
INSERT INTO `t_pay_order` VALUES ('P1692067662941007874', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 5000, 0.150000, 750, 0.015000, 75, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.611000', '2023-08-17 14:55:42.647608', 'ECmr33dswKPnvt5', 0.090000, 450, NULL, 50, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067663268163586', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.691000', '2023-08-17 14:55:42.696518', 'MAavuS3qCc7u8gK', 0.090000, 1800, NULL, 200, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067663402381314', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 1000, 0.150000, 150, 0.015000, 15, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.716000', '2023-08-17 14:55:42.721960', 'IU5BH4BMniCkSfX', 0.090000, 90, NULL, 10, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067663486267394', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 30000, 0.150000, 4500, 0.015000, 450, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.740000', '2023-08-17 14:55:42.746171', 'Eg6Qq7MQkrdwPU5', 0.090000, 2700, NULL, 300, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067663595319298', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 10000, 0.150000, 1500, 0.015000, 150, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.763000', '2023-08-17 14:55:42.767306', '6eJZf2ju4sYlC59', 0.090000, 900, NULL, 100, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067663725342721', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 30000, 0.150000, 4500, 0.015000, 450, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.788000', '2023-08-17 14:55:42.792829', 'z0tuC82tiO7ZAV7', 0.090000, 2700, NULL, 300, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067663788257281', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.812000', '2023-08-17 14:55:42.816685', '5HIHxYKCya3k0qO', 0.090000, 1800, NULL, 200, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067663918280705', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 10000, 0.150000, 1500, 0.015000, 150, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.836000', '2023-08-17 14:55:42.839826', '1RAC4MSKWrRRnkZ', 0.090000, 900, NULL, 100, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067663981195266', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 1000, 0.150000, 150, 0.015000, 15, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.858000', '2023-08-17 14:55:42.861702', 'TF8XSr8WA1MRa1y', 0.090000, 90, NULL, 10, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067664111218689', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:43', NULL, '2023-08-17 14:55:42.879000', '2023-08-17 14:55:42.884118', 'JnzQPPXjpThDwRl', 0.090000, 180, NULL, 20, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067677910478849', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 10000, 0.150000, 1500, 0.015000, 150, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.171000', '2023-08-17 14:55:46.174884', 'avw14v8q4Qric1s', 0.090000, 900, NULL, 100, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678044696577', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.202000', '2023-08-17 14:55:46.205262', 'bootGJqLdJ1WBO3', 0.090000, 1800, NULL, 200, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678107611138', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 10000, 0.150000, 1500, 0.015000, 150, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.221000', '2023-08-17 14:55:46.224027', 'LINnRNUjsNfK8ac', 0.090000, 900, NULL, 100, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678170525697', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 30000, 0.150000, 4500, 0.015000, 450, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.239000', '2023-08-17 14:55:46.241737', 'oFiceBdqrwXmnoL', 0.090000, 2700, NULL, 300, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678237634561', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.257000', '2023-08-17 14:55:46.260014', 'KBr0u8XJPoa3BKp', 0.090000, 180, NULL, 20, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678304743426', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.276000', '2023-08-17 14:55:46.280650', 'JJIMBBNKzcV7K1S', 0.090000, 1800, NULL, 200, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678367657986', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.293000', '2023-08-17 14:55:46.296049', 'CKMn8tBHBpfUVfL', 0.090000, 180, NULL, 20, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678434766849', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.307000', '2023-08-17 14:55:46.310863', 'LmggWxg9NpCD61u', 0.090000, 1800, NULL, 200, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678497681410', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', NULL, '2023-08-17 14:55:46.322000', '2023-08-17 14:55:46.325240', 'bT5yt6HI9g7nbQR', 0.090000, 180, NULL, 20, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692067678568984577', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 2, 1, '{\"state\":\"1\"}', NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 15:55:46', '2023-08-17 14:57:03', '2023-08-17 14:55:46.336000', '2023-08-17 14:57:23.806557', 'AZsHBX8gtgUGeuj', 0.090000, 180, NULL, 20, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068804689379330', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 10000, 0.150000, 1500, 0.015000, 150, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:14.826000', '2023-08-17 15:00:14.853167', 'bKP8pnsO9ZgZ7jA', 0.090000, 900, NULL, 100, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805146558466', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:14.933000', '2023-08-17 15:00:14.939740', 'vvcSI0Y7tDCj7Zi', 0.090000, 1800, NULL, 200, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805276581889', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 1000, 0.150000, 150, 0.015000, 15, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:14.969000', '2023-08-17 15:00:14.974472', 'BSQEGHzB6fcxbaU', 0.090000, 90, NULL, 10, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805410799618', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:14.995000', '2023-08-17 15:00:14.999058', 'KdltKPuzXrhDpOp', 0.090000, 1800, NULL, 200, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805540823041', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:15.019000', '2023-08-17 15:00:15.023797', 'nLRBcf2lWwISgwQ', 0.090000, 180, NULL, 20, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805607931905', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 30000, 0.150000, 4500, 0.015000, 450, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:15.041000', '2023-08-17 15:00:15.044601', 'hNfGmplHn4wumch', 0.090000, 2700, NULL, 300, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805675040770', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 30000, 0.150000, 4500, 0.015000, 450, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:15.061000', '2023-08-17 15:00:15.064964', '9Ld7R0QmppRU3Te', 0.090000, 2700, NULL, 300, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805800869889', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 5000, 0.150000, 750, 0.015000, 75, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:15.086000', '2023-08-17 15:00:15.088908', 'LE6hwDvX1CWnYFu', 0.090000, 450, NULL, 50, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805867978753', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:15.105000', '2023-08-17 15:00:15.109345', 'No2rQnFEfdmQLim', 0.090000, 180, NULL, 20, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068805930893314', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 1000, 0.150000, 150, 0.015000, 15, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:15', NULL, '2023-08-17 15:00:15.127000', '2023-08-17 15:00:15.129971', 'B3mXffsNhXyeCHT', 0.090000, 90, NULL, 10, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068960998506498', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', NULL, '2023-08-17 15:00:52.090000', '2023-08-17 15:00:52.093544', 'vlB667lRPqhvt0Y', 0.080000, 160, NULL, 0, 0.000000, '', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961124335618', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 2, 1, '{\"state\":\"1\"}', NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', '2023-08-17 15:03:42', '2023-08-17 15:00:52.118000', '2023-08-17 15:04:02.011163', 'JclIH1nnXe5BhLY', 0.080000, 1600, NULL, 0, 0.000000, '', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961187250177', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 2, 1, '{\"state\":\"1\"}', NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', '2023-08-17 15:03:38', '2023-08-17 15:00:52.137000', '2023-08-17 15:03:58.187258', 'Tj9kYzlYiOwt222', 0.080000, 1600, NULL, 0, 0.000000, '', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961254359041', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 30000, 0.150000, 4500, 0.015000, 450, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', NULL, '2023-08-17 15:00:52.157000', '2023-08-17 15:00:52.161195', '4xCsLNgNdKIvd4K', 0.080000, 2400, NULL, 0, 0.000000, '', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961384382465', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 20000, 0.150000, 3000, 0.015000, 300, 2, 1, '{\"state\":\"1\"}', NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', '2023-08-17 15:03:34', '2023-08-17 15:00:52.178000', '2023-08-17 15:03:53.724434', 'oouje5vhM5cImSw', 0.080000, 1600, NULL, 0, 0.000000, '', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961451491329', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 5000, 0.150000, 750, 0.015000, 75, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', NULL, '2023-08-17 15:00:52.199000', '2023-08-17 15:00:52.203136', '04Z8zCabVs5GsyS', 0.080000, 400, NULL, 0, 0.000000, '', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961514405890', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 5000, 0.150000, 750, 0.015000, 75, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', NULL, '2023-08-17 15:00:52.214000', '2023-08-17 15:00:52.217541', 'qAxMghBkBU8Kknp', 0.080000, 400, NULL, 0, 0.000000, '', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961581514753', 'M1691231056', 'A1691231069', 1272, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 2, 1, '{\"state\":\"1\"}', NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', '2023-08-17 15:03:05', '2023-08-17 15:00:52.228000', '2023-08-17 15:03:26.189994', 'XceEIzfKwkeE3DA', 0.090000, 180, NULL, 20, 0.010000, 'A1692255110', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961648623617', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 5000, 0.150000, 750, 0.015000, 75, 5, 1, '{\"state\":\"1\"}', NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', '2023-08-17 15:03:29', '2023-08-17 15:00:52.242000', '2023-08-17 15:04:17.155000', 'ADXwjziSpy7Gwok', 0.080000, 400, NULL, 0, 0.000000, '', 0, '', NULL);
INSERT INTO `t_pay_order` VALUES ('P1692068961715732482', 'M1691231056', 'A1691231069', 1273, '测试商户', '', 'testpay', 1000, '测试产品', 2000, 0.150000, 300, 0.015000, 30, 1, 0, NULL, NULL, '154.82.113.215', NULL, NULL, NULL, NULL, 'https://www.test.com', '', '2023-08-17 16:00:52', NULL, '2023-08-17 15:00:52.257000', '2023-08-17 15:00:52.260219', 'qEiT8UBi7tgdBTH', 0.080000, 160, NULL, 0, 0.000000, '', 0, '', NULL);

-- ----------------------------
-- Table structure for t_pay_passage
-- ----------------------------
DROP TABLE IF EXISTS `t_pay_passage`;
CREATE TABLE `t_pay_passage`  (
  `pay_passage_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pay_passage_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通道名称',
  `if_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付接口代码',
  `product_id` bigint(20) NOT NULL COMMENT '对应产品ID',
  `rate` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '通道费率(实际三方通道成本)',
  `pay_type` tinyint(6) NULL DEFAULT 0 COMMENT '收款方式：1-区间范围 如:10-5000，2-固定金额 如:100|200|500 以|为分隔符',
  `pay_rules` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '收款规则',
  `pay_interface_config` json NULL COMMENT '三方通道配置数据',
  `balance` bigint(20) NULL DEFAULT 0 COMMENT '通道余额',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `weights` int(20) NULL DEFAULT 1 COMMENT '轮询权重:任意正整数,设置为0则不会拉起该通道',
  `quota_limit_state` tinyint(6) NULL DEFAULT 0 COMMENT '额度限制状态: 0-停用, 1-启用',
  `quota` bigint(20) NULL DEFAULT 0 COMMENT '通道授信额度',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '通道的上级代理',
  `agent_rate` decimal(20, 6) NOT NULL DEFAULT 0.000000 COMMENT '通道的上级代理费率',
  `time_limit` tinyint(6) NULL DEFAULT 0 COMMENT '通道可用时间设置:0-停用, 1-启用',
  `time_rules` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '通道执行时间规则',
  PRIMARY KEY (`pay_passage_id`) USING BTREE,
  UNIQUE INDEX `id`(`pay_passage_id`) USING BTREE,
  UNIQUE INDEX `Pay_Passage_Index`(`product_id`, `if_code`, `pay_passage_name`, `state`, `pay_type`, `created_at`, `agent_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1274 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付通道表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_pay_passage
-- ----------------------------
INSERT INTO `t_pay_passage` VALUES (1272, '测试通道', 'testpay', 1000, 0.090000, 1, '10-5000', '{\"mchNo\": \"1\", \"secret\": \"2\", \"payType\": \"3\", \"whiteList\": \"*\", \"payGateway\": \"4\"}', 3600, 1, 1, 0, 0, '2023-08-17 14:52:20.000000', '2023-08-17 15:03:06.114864', 'A1692255110', 0.010000, 0, '');
INSERT INTO `t_pay_passage` VALUES (1273, '测试通道2', 'testpay', 1000, 0.080000, 1, '10-50000', '{\"mchNo\": \"1\", \"secret\": \"2\", \"payType\": \"3\", \"whiteList\": \"*\", \"payGateway\": \"4\"}', 55200, 1, 5, 0, 0, '2023-08-17 14:59:51.000000', '2023-08-17 15:04:17.186533', '', 0.000000, 0, '');

-- ----------------------------
-- Table structure for t_product
-- ----------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product`  (
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `product_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`product_id`) USING BTREE,
  UNIQUE INDEX `id`(`product_id`) USING BTREE,
  INDEX `index`(`product_name`, `created_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付产品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_product
-- ----------------------------
INSERT INTO `t_product` VALUES (1000, '测试产品', '2023-08-17 14:51:25.856249', '2023-08-17 14:51:25.855000');

-- ----------------------------
-- Table structure for t_robots_mch
-- ----------------------------
DROP TABLE IF EXISTS `t_robots_mch`;
CREATE TABLE `t_robots_mch`  (
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `chat_id` bigint(30) NOT NULL COMMENT 'chat id',
  PRIMARY KEY (`mch_no`) USING BTREE,
  INDEX `index`(`mch_no`, `chat_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_robots_mch
-- ----------------------------

-- ----------------------------
-- Table structure for t_robots_user
-- ----------------------------
DROP TABLE IF EXISTS `t_robots_user`;
CREATE TABLE `t_robots_user`  (
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机器人用户名',
  `chat_id` bigint(30) NOT NULL COMMENT '频道ID',
  `mch_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  PRIMARY KEY (`user_name`) USING BTREE,
  INDEX `index`(`user_name`, `mch_no`, `chat_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_robots_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_statistics_agent
-- ----------------------------
DROP TABLE IF EXISTS `t_statistics_agent`;
CREATE TABLE `t_statistics_agent`  (
  `statistics_agent_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理商号',
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `total_order_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款订单总数',
  `order_success_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款成功订单数',
  `total_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款总金额，单位分',
  `total_success_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款成功金额，单位分',
  `total_agent_income` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款手续费，单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`statistics_agent_id`) USING BTREE,
  UNIQUE INDEX `id`(`statistics_agent_id`) USING BTREE,
  INDEX `index`(`agent_no`, `statistics_date`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_agent
-- ----------------------------
INSERT INTO `t_statistics_agent` VALUES (29, 'A1691231069', '2023-08-17', 40, 6, 479000, 64000, 960, '2023-08-17 14:55:51.114000', '2023-08-17 14:55:51.114000');
INSERT INTO `t_statistics_agent` VALUES (30, 'A1692255110', '2023-08-17', 31, 2, 370000, 4000, 40, '2023-08-17 14:55:51.116000', '2023-08-17 14:55:51.116000');

-- ----------------------------
-- Table structure for t_statistics_agent_mch
-- ----------------------------
DROP TABLE IF EXISTS `t_statistics_agent_mch`;
CREATE TABLE `t_statistics_agent_mch`  (
  `statistics_agent_mch_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理商号',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `total_order_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款订单总数',
  `order_success_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款成功订单数',
  `total_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款总金额，单位分',
  `total_success_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款成功金额，单位分',
  `total_agent_income` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款手续费，单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`statistics_agent_mch_id`) USING BTREE,
  UNIQUE INDEX `id`(`statistics_agent_mch_id`) USING BTREE,
  INDEX `index`(`agent_no`, `statistics_date`, `mch_no`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商-商户日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_agent_mch
-- ----------------------------
INSERT INTO `t_statistics_agent_mch` VALUES (21, 'A1691231069', 'M1691231056', '2023-08-17', 40, 6, 479000, 64000, 960, '2023-08-17 14:55:51.123000', '2023-08-17 14:55:51.123000');

-- ----------------------------
-- Table structure for t_statistics_agent_passage
-- ----------------------------
DROP TABLE IF EXISTS `t_statistics_agent_passage`;
CREATE TABLE `t_statistics_agent_passage`  (
  `statistics_agent_passage_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `agent_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理商号',
  `pay_passage_id` bigint(20) NOT NULL COMMENT '通道编码',
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `total_order_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款订单总数',
  `order_success_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款成功订单数',
  `total_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款总金额，单位分',
  `total_success_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款成功金额，单位分',
  `total_agent_income` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款手续费，单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`statistics_agent_passage_id`) USING BTREE,
  UNIQUE INDEX `id`(`statistics_agent_passage_id`) USING BTREE,
  INDEX `index`(`agent_no`, `statistics_date`, `pay_passage_id`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 110 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商-通道日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_agent_passage
-- ----------------------------
INSERT INTO `t_statistics_agent_passage` VALUES (109, 'A1692255110', 1272, '2023-08-17', 31, 2, 370000, 4000, 40, '2023-08-17 14:55:51.130000', '2023-08-17 14:55:51.130000');

-- ----------------------------
-- Table structure for t_statistics_mch
-- ----------------------------
DROP TABLE IF EXISTS `t_statistics_mch`;
CREATE TABLE `t_statistics_mch`  (
  `statistics_mch_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `total_order_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款订单总数',
  `order_success_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款成功订单数',
  `total_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款总金额，单位分',
  `total_success_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款成功金额，单位分',
  `total_mch_cost` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款手续费，单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`statistics_mch_id`) USING BTREE,
  UNIQUE INDEX `id`(`statistics_mch_id`) USING BTREE,
  INDEX `index`(`mch_no`, `statistics_date`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 308 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_mch
-- ----------------------------
INSERT INTO `t_statistics_mch` VALUES (307, 'M1691231056', '2023-08-17', 40, 6, 479000, 64000, 9600, '2023-08-17 14:55:51.110000', '2023-08-17 14:55:51.110000');

-- ----------------------------
-- Table structure for t_statistics_mch_product
-- ----------------------------
DROP TABLE IF EXISTS `t_statistics_mch_product`;
CREATE TABLE `t_statistics_mch_product`  (
  `statistics_product_mch_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `product_id` bigint(20) NOT NULL COMMENT '支付通道ID',
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `total_order_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款订单总数',
  `order_success_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款成功订单数',
  `total_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款总金额，单位分',
  `total_success_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款成功金额，单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `total_cost` bigint(20) NOT NULL DEFAULT 0 COMMENT '商户服务费',
  `mch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  PRIMARY KEY (`statistics_product_mch_id`) USING BTREE,
  UNIQUE INDEX `id`(`statistics_product_mch_id`) USING BTREE,
  INDEX `index`(`product_id`, `statistics_date`, `mch_no`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1045 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户-产品日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_mch_product
-- ----------------------------
INSERT INTO `t_statistics_mch_product` VALUES (1044, 1000, '2023-08-17', 40, 6, 479000, 64000, '2023-08-17 14:55:51.144000', '2023-08-17 14:55:51.144000', 9600, 'M1691231056');

-- ----------------------------
-- Table structure for t_statistics_passage
-- ----------------------------
DROP TABLE IF EXISTS `t_statistics_passage`;
CREATE TABLE `t_statistics_passage`  (
  `statistics_passage_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `pay_passage_id` bigint(20) NOT NULL COMMENT '支付通道ID',
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `total_order_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款订单总数',
  `order_success_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款成功订单数',
  `total_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款总金额，单位分',
  `total_success_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款成功金额，单位分',
  `total_passage_cost` bigint(20) NOT NULL DEFAULT 0 COMMENT '总通道成本，单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`statistics_passage_id`) USING BTREE,
  UNIQUE INDEX `id`(`statistics_passage_id`) USING BTREE,
  INDEX `index`(`pay_passage_id`, `statistics_date`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 717 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付通道日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_passage
-- ----------------------------
INSERT INTO `t_statistics_passage` VALUES (715, 1272, '2023-08-17', 31, 2, 370000, 4000, 360, '2023-08-17 14:55:51.134000', '2023-08-17 14:55:51.134000');
INSERT INTO `t_statistics_passage` VALUES (716, 1273, '2023-08-17', 9, 4, 109000, 60000, 4800, '2023-08-17 15:01:01.121000', '2023-08-17 15:01:01.121000');

-- ----------------------------
-- Table structure for t_statistics_plat
-- ----------------------------
DROP TABLE IF EXISTS `t_statistics_plat`;
CREATE TABLE `t_statistics_plat`  (
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `total_order_count` int(11) NOT NULL DEFAULT 0 COMMENT '订单总数',
  `order_success_count` int(11) NOT NULL DEFAULT 0 COMMENT '成功订单数',
  `total_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款总金额，单位分',
  `total_success_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款成功金额，单位分',
  `plat_total_income` bigint(20) NOT NULL DEFAULT 0 COMMENT '平台总收款利润，单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`statistics_date`) USING BTREE,
  UNIQUE INDEX `id`(`statistics_date`) USING BTREE,
  INDEX `index`(`created_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '平台日总统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_plat
-- ----------------------------
INSERT INTO `t_statistics_plat` VALUES ('2023-08-17', 40, 6, 479000, 64000, 3440, '2023-08-17 14:55:51.098000', '2023-08-17 14:55:51.104000');

-- ----------------------------
-- Table structure for t_statistics_product
-- ----------------------------
DROP TABLE IF EXISTS `t_statistics_product`;
CREATE TABLE `t_statistics_product`  (
  `statistics_product_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `product_id` bigint(20) NOT NULL COMMENT '支付通道ID',
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `total_order_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款订单总数',
  `order_success_count` int(11) NOT NULL DEFAULT 0 COMMENT '收款成功订单数',
  `total_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款总金额，单位分',
  `total_success_amount` bigint(20) NOT NULL DEFAULT 0 COMMENT '收款成功金额，单位分',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`statistics_product_id`) USING BTREE,
  UNIQUE INDEX `id`(`statistics_product_id`) USING BTREE,
  INDEX `index`(`product_id`, `statistics_date`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 198 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付产品日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_product
-- ----------------------------
INSERT INTO `t_statistics_product` VALUES (197, 1000, '2023-08-17', 40, 6, 479000, 64000, '2023-08-17 14:55:51.139000', '2023-08-17 14:55:51.139000');

-- ----------------------------
-- Table structure for t_sys_config
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_config`;
CREATE TABLE `t_sys_config`  (
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置KEY',
  `config_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `config_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '描述信息',
  `group_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组key',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组名称',
  `config_val` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置内容项',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'text' COMMENT '类型: text-输入框, textarea-多行文本, uploadImg-上传图片, switch-开关',
  `sort_num` bigint(20) NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`config_key`) USING BTREE,
  INDEX `index`(`config_key`, `group_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_config
-- ----------------------------
INSERT INTO `t_sys_config` VALUES ('agentSiteUrl', '代理商平台网址(不包含结尾/)', '代理商平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://agent-api.eluosi-pay.com', 'text', 0, '2023-08-06 16:30:07.485074');
INSERT INTO `t_sys_config` VALUES ('dataOffset', '数据清理设置(保留xx天的数据)', '数据清理设置(保留xx天的数据)', 'applicationConfig', '系统应用配置', '15', 'text', 0, '2023-08-06 21:43:57.736626');
INSERT INTO `t_sys_config` VALUES ('loginWhiteList', '运营平台登录白名单(多个IP | 隔开,允许所有IP登录为*)', '运营平台登录白名单', 'applicationConfig', '系统应用配置', '*', 'textarea', 0, '2023-07-26 15:16:34.581903');
INSERT INTO `t_sys_config` VALUES ('mchSiteUrl', '商户平台网址(不包含结尾/)', '商户平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://mch-api.eluosi-pay.com', 'text', 0, '2023-08-06 16:30:07.514215');
INSERT INTO `t_sys_config` VALUES ('mgrSiteUrl', '运营平台网址(不包含结尾/)', '运营平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://mgr-api.eluosi-pay.com', 'text', 0, '2023-08-06 16:30:07.494783');
INSERT INTO `t_sys_config` VALUES ('payPassageAutoClean', '通道余额过零点自动清零', '通道余额过零点自动清零', 'payConfigGroup', '支付配置', '1', 'text', 0, '2023-08-13 18:35:07.601811');
INSERT INTO `t_sys_config` VALUES ('paySiteUrl', '支付网关地址(不包含结尾/)', '支付网关地址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://pay-api.eluosi-pay.com', 'text', 0, '2023-08-06 16:30:07.465521');
INSERT INTO `t_sys_config` VALUES ('robotsAdmin', '机器人管理员(飞机用户名)', '机器人管理员', 'robotsConfigGroup', '机器人配置', '', 'text', 0, '2023-08-05 18:25:29.421483');
INSERT INTO `t_sys_config` VALUES ('robotsToken', '机器人token(勿乱动，可能导致机器人失效)', '机器人token', 'robotsConfigGroup', '机器人配置', '', 'text', 0, '2023-08-05 18:25:29.425972');
INSERT INTO `t_sys_config` VALUES ('robotsUserName', '机器人用户名(勿乱动，可能导致机器人失效)', '机器人用户名(不带@)', 'robotsConfigGroup', '机器人配置', '', 'text', 0, '2023-08-05 18:25:29.423765');

-- ----------------------------
-- Table structure for t_sys_entitlement
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_entitlement`;
CREATE TABLE `t_sys_entitlement`  (
  `ent_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD',
  `ent_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限名称',
  `menu_icon` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `menu_uri` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单uri/路由地址',
  `component_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件Name（前后端分离使用）',
  `ent_type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限类型 ML-左侧显示菜单, MO-其他菜单, PB-页面/按钮',
  `quick_jump` tinyint(6) NOT NULL DEFAULT 0 COMMENT '快速开始菜单 0-否, 1-是',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '状态 0-停用, 1-启用',
  `pid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '父ID',
  `ent_sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序字段, 规则：正序',
  `sys_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心,AGENT-代理中心',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`ent_id`, `sys_type`) USING BTREE,
  INDEX `index`(`ent_id`, `ent_name`, `state`, `sys_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_entitlement
-- ----------------------------
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ACCOUNT_HISTORY', '账户记录', 'form', '', 'RouteView', 'ML', 0, 1, 'ROOT', 65, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_COMMONS', '系统通用菜单', 'no-icon', '', 'RouteView', 'MO', 0, 1, 'ROOT', -1, 'AGENT', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_COMMONS', '系统通用菜单', 'no-icon', '', 'RouteView', 'MO', 0, 1, 'ROOT', -1, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_COMMONS', '系统通用菜单', 'no-icon', '', 'RouteView', 'MO', 0, 1, 'ROOT', -1, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_C_MAIN', '主页', 'home', '/main', 'MainPage', 'ML', 0, 1, 'ROOT', 1, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_C_MAIN_PAY_COUNT', '主页交易统计', 'no-icon', '', '', 'PB', 0, 1, 'ENT_C_MAIN', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_C_USERINFO', '个人中心', 'no-icon', '/current/userinfo', 'CurrentUserInfo', 'MO', 0, 1, 'ENT_COMMONS', -1, 'AGENT', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_C_USERINFO', '个人中心', 'no-icon', '/current/userinfo', 'CurrentUserInfo', 'MO', 0, 1, 'ENT_COMMONS', -1, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_C_USERINFO', '个人中心', 'no-icon', '/current/userinfo', 'CurrentUserInfo', 'MO', 0, 1, 'ENT_COMMONS', -1, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_DIVISION_AGENT', '代理结算管理', 'carry-out', '', 'DivisionAgentPage', 'ML', 0, 1, 'ENT_DIVISION_MANAGE', 20, 'MGR', '2023-06-15 16:19:37.000000', '2023-07-23 11:53:01.999806');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_DIVISION_MANAGE', '结算管理', 'apartment', '', 'RouteView', 'ML', 0, 1, 'ROOT', 100, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_DIVISION_MCH', '商户结算管理', 'team', '/divisionMch', 'DivisionMchPage', 'ML', 0, 1, 'ENT_DIVISION_MANAGE', 10, 'MGR', '2023-06-15 16:19:37.000000', '2023-07-20 15:45:04.385316');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ISV', '代理商管理', 'block', '', 'RouteView', 'ML', 0, 1, 'ROOT', 40, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-25 23:53:48.466996');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ISV_INFO', '代理商列表', 'profile', '/isv', 'IsvListPage', 'ML', 0, 1, 'ENT_ISV', 10, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-25 23:54:01.789605');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ISV_INFO_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1, 'ENT_ISV_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ISV_INFO_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1, 'ENT_ISV_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ISV_INFO_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1, 'ENT_ISV_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ISV_INFO_HISTORY', '代理资金流水', 'red-envelope', '/isvHistory', 'IsvHistoryList', 'ML', 0, 1, 'ENT_ACCOUNT_HISTORY', 20, 'MGR', '2023-06-15 16:19:37.000000', '2023-08-13 20:20:08.295722');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ISV_INFO_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_ISV_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ISV_LIST', '页面：代理商列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_ISV_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_LOG_LIST', '页面：系统日志列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_SYS_LOG', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH', '商户管理', 'shop', '', 'RouteView', 'ML', 0, 1, 'ROOT', 30, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP', '支付通道', 'appstore', '/apps', 'MchAppPage', 'ML', 0, 1, 'ENT_PC', 10, 'AGENT', '2023-06-15 16:19:37.000000', '2023-07-23 01:29:33.674681');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP', '支付产品', 'appstore', '/apps', 'MchAppPage', 'ML', 0, 1, 'ENT_PC', 10, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP', '通道列表', 'ordered-list', '/apps', 'MchAppPage', 'ML', 0, 1, 'ENT_PC', 10, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-27 22:19:12.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_APP', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP_CONFIG', '产品-通道配置', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_APP', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_APP', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP_LIST', '页面：通道列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_APP', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_APP_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_APP', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_DAY_STAT', '日终统计', 'area-chart', '/dayStat', 'DayStatList', 'ML', 0, 1, 'ENT_PC', 40, 'AGENT', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_DAY_STAT', '日终统计', 'area-chart', '/dayStat', 'DayStatList', 'ML', 0, 1, 'ENT_PC', 40, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_DIVISION', '结算管理', 'apartment', '/division', 'DivisionPage', 'ML', 0, 1, 'ENT_PC', 30, 'AGENT', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_DIVISION', '结算管理', 'apartment', '/division', 'DivisionPage', 'ML', 0, 1, 'ENT_PC', 30, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_HISTORY', '资金流水', 'container', '/history', 'HistoryPage', 'ML', 0, 1, 'ENT_PC', 10, 'AGENT', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_HISTORY', '资金流水', 'container', '/history', 'HistoryPage', 'ML', 0, 1, 'ENT_PC', 10, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_INFO', '商户列表', 'profile', '/mch', 'MchListPage', 'ML', 0, 1, 'ENT_MCH', 10, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_INFO_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_INFO_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_INFO_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_INFO_HISTORY', '商户资金流水', 'red-envelope', '/mchHistory', 'MchHistoryList', 'ML', 0, 1, 'ENT_ACCOUNT_HISTORY', 21, 'MGR', '2023-06-15 16:19:37.000000', '2023-08-13 20:20:09.775921');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_INFO_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_LIST', '页面：商户列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_MAIN', '主页', 'home', '/main', 'MainPage', 'ML', 0, 1, 'ROOT', 1, 'AGENT', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_MAIN', '主页', 'home', '/main', 'MainPage', 'ML', 0, 1, 'ROOT', 1, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_NOTIFY', '商户通知', 'notification', '/notify', 'MchNotifyListPage', 'ML', 0, 1, 'ENT_ORDER', 30, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_NOTIFY_RESEND', '按钮：重发通知', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_NOTIFY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_NOTIFY_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_NOTIFY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_PAY_PASSAGE_ADD', '支付通道配置保存', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_PAY_PASSAGE_LIST', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_PAY_PASSAGE_CONFIG', '支付通道配置入口', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_PAY_PASSAGE_LIST', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MCH_PAY_PASSAGE_LIST', '支付通道配置列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_APP', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MGR_AGENT_STAT', '代理统计', 'align-left', '/agentStat', 'AgentStatPage', 'ML', 0, 1, 'ENT_MGR_STAT', 70, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MGR_MCH_PRODUCT_STAT', '商户⇆产品统计', 'align-left', '/mchProductStat', 'MchProductStatPage', 'ML', 0, 1, 'ENT_MGR_STAT', 40, 'MGR', '2023-06-15 16:19:37.000000', '2023-07-29 19:33:49.240067');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MGR_MCH_STAT', '商户统计', 'align-left', '/mchStat', 'MchStatPage', 'ML', 0, 1, 'ENT_MGR_STAT', 30, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MGR_PASSAGE_STAT', '通道统计', 'align-left', '/passageStat', 'PassageStatPage', 'ML', 0, 1, 'ENT_MGR_STAT', 50, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MGR_PLAT_STAT', '平台统计', 'align-left', '/platStat', 'PlatStatPage', 'ML', 0, 1, 'ENT_MGR_STAT', 20, 'MGR', '2023-06-15 16:19:37.000000', '2023-07-29 19:08:13.806846');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MGR_PRODUCT_STAT', '产品统计', 'align-left', '/productStat', 'ProductStatPage', 'ML', 0, 1, 'ENT_MGR_STAT', 60, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_MGR_STAT', '数据统计', 'ordered-list', '', 'RouteView', 'ML', 0, 1, 'ROOT', 70, 'MGR', '2023-06-15 16:19:37.000000', '2023-07-29 19:06:36.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_NOTIFY_LIST', '页面：商户通知列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_MCH_NOTIFY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ORDER', '订单中心', 'transaction', '', 'RouteView', 'ML', 0, 1, 'ROOT', 20, 'AGENT', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ORDER', '订单中心', 'transaction', '', 'RouteView', 'ML', 0, 1, 'ROOT', 20, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ORDER', '订单管理', 'transaction', '', 'RouteView', 'ML', 0, 1, 'ROOT', 50, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_ORDER_LIST', '页面：订单列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PAY_ORDER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PASSAGE_HISTORY', '通道资金流水', 'red-envelope', '/passageHistory', 'PassageHistoryList', 'ML', 0, 1, 'ENT_ACCOUNT_HISTORY', 10, 'MGR', '2023-06-15 16:19:37.000000', '2023-08-13 20:20:08.295722');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PASSAGE_PAY_ORDER', '通道订单', 'account-book', '/passagePayOrder', 'PassagePayOrderListPage', 'ML', 0, 1, 'ENT_ORDER', 20, 'AGENT', '2023-06-15 16:19:37.000000', '2023-07-23 00:07:08.424727');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PAY_FORCE_ORDER', '补单列表', 'wallet', '/orderForceList', 'OrderForceListPage', 'ML', 0, 1, 'ENT_ORDER', 15, 'MGR', '2023-06-15 16:19:37.000000', '2023-07-15 11:29:17.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PAY_ORDER', '商户订单', 'account-book', '/payOrder', 'PayOrderListPage', 'ML', 0, 1, 'ENT_ORDER', 10, 'AGENT', '2023-06-15 16:19:37.000000', '2023-07-23 00:06:58.276087');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PAY_ORDER', '订单管理', 'account-book', '/pay', 'PayOrderListPage', 'ML', 0, 1, 'ENT_ORDER', 10, 'MCH', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PAY_ORDER', '支付订单', 'account-book', '/pay', 'PayOrderListPage', 'ML', 0, 1, 'ENT_ORDER', 10, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PAY_ORDER_EDIT', '按钮：强制补单', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PAY_ORDER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PAY_ORDER_SEARCH_PAY_WAY', '筛选项：支付产品', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PAY_ORDER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PAY_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PAY_ORDER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC', '支付配置', 'file-done', '', 'RouteView', 'ML', 0, 1, 'ROOT', 60, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_IF_DEFINE', '支付接口', 'interaction', '/ifdefines', 'IfDefinePage', 'ML', 0, 1, 'ENT_PC', 30, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_IF_DEFINE_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_IF_DEFINE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_IF_DEFINE_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_IF_DEFINE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_IF_DEFINE_EDIT', '按钮：修改', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_IF_DEFINE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_IF_DEFINE_LIST', '页面：支付接口定义列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_IF_DEFINE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_IF_DEFINE_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_IF_DEFINE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_IF_DEFINE_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_IF_DEFINE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_WAY', '支付产品', 'appstore', '/payways', 'PayWayPage', 'ML', 0, 1, 'ENT_PC', 20, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_WAY_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_WAY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_WAY_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_WAY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_WAY_EDIT', '按钮：修改', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_WAY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_WAY_LIST', '页面：支付产品列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_WAY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_WAY_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_WAY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_PC_WAY_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_PC_WAY', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_SYS_CONFIG', '系统管理', 'setting', '', 'RouteView', 'ML', 0, 1, 'ROOT', 200, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_SYS_CONFIG_EDIT', '按钮： 修改', 'no-icon', '', '', 'PB', 0, 1, 'ENT_SYS_CONFIG_INFO', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_SYS_CONFIG_INFO', '系统配置', 'setting', '/config', 'SysConfigPage', 'ML', 0, 1, 'ENT_SYS_CONFIG', 15, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_SYS_CONFIG_ROBOTS', '机器人配置', 'robot', '/robotsConfig', 'RobotsConfigPage', 'ML', 0, 1, 'ENT_SYS_CONFIG', 20, 'MGR', '2023-06-15 16:19:37.000000', '2023-07-24 15:32:07.140957');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_SYS_LOG', '系统日志', 'file-text', '/log', 'SysLogPage', 'ML', 0, 1, 'ENT_SYS_CONFIG', 30, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_SYS_LOG_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_SYS_LOG', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_TRANSFER_ORDER', '转账订单', 'property-safety', '/transfer', 'TransferOrderListPage', 'ML', 0, 0, 'ENT_ORDER', 25, 'MGR', '2023-06-15 16:19:37.000000', '2023-07-06 10:05:04.254038');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_TRANSFER_ORDER_LIST', '页面：转账订单列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_TRANSFER_ORDER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_TRANSFER_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1, 'ENT_TRANSFER_ORDER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR', '用户角色管理', 'team', '', 'RouteView', 'ML', 0, 1, 'ENT_SYS_CONFIG', 10, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE', '角色管理', 'user', '/roles', 'RolePage', 'ML', 0, 1, 'ENT_UR', 20, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_ADD', '按钮：添加角色', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_ROLE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_DEL', '按钮： 删除', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_ROLE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_DIST', '按钮： 分配权限', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_ROLE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_EDIT', '按钮： 修改基本信息', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_ROLE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_ENT', '权限管理', 'apartment', '/ents', 'EntPage', 'ML', 0, 1, 'ENT_UR', 30, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_ENT_EDIT', '按钮： 权限变更', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_ROLE_ENT', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_ENT_LIST', '页面： 权限列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_ROLE_ENT', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_LIST', '页面：角色列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_ROLE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_ROLE_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_ROLE', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_USER', '操作员管理', 'contacts', '/users', 'SysUserPage', 'ML', 0, 1, 'ENT_UR', 10, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_USER_ADD', '按钮：添加操作员', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_USER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_USER_DELETE', '按钮： 删除操作员', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_USER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_USER_EDIT', '按钮： 修改基本信息', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_USER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_USER_LIST', '页面：操作员列表', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_USER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_USER_SEARCH', '按钮：搜索', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_USER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_USER_UPD_ROLE', '按钮： 角色分配', 'no-icon', '', '', 'PB', 0, 1, 'ENT_UR_USER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');
INSERT INTO `t_sys_entitlement` VALUES ('ENT_UR_USER_VIEW', '按钮： 详情', '', 'no-icon', '', 'PB', 0, 1, 'ENT_UR_USER', 0, 'MGR', '2023-06-15 16:19:37.000000', '2023-06-15 16:19:37.000000');

-- ----------------------------
-- Table structure for t_sys_log
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_log`;
CREATE TABLE `t_sys_log`  (
  `sys_log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `login_username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `user_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户IP',
  `sys_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心,AGENT-代理中心',
  `method_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '方法名',
  `method_remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '方法描述',
  `req_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '请求地址',
  `opt_req_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '操作请求参数',
  `opt_res_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '操作响应结果',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`sys_log_id`) USING BTREE,
  UNIQUE INDEX `id`(`sys_log_id`) USING BTREE,
  INDEX `index`(`login_username`, `sys_type`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6287 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_log
-- ----------------------------
INSERT INTO `t_sys_log` VALUES (6248, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.anon.AuthController.validate', '登录认证', 'http://localhost:9217/api/anon/auth/validate', '{\"ip\":\"c2lyaXVzOTE1\",\"ia\":\"YWRtaW4=\",\"gc\":\"dW5kZWZpbmVk\",\"vc\":\"NTU5Mg==\",\"vt\":\"MTY5MmI5ZjQtNWJmYS00NzA0LWJmMjUtODIzNzE5OTk2MWJi\"}', '{\"msg\":\"SUCCESS\",\"code\":0,\"data\":{\"iToken\":\"eyJhbGciOiJIUzUxMiJ9.eyJjYWNoZUtleSI6IlRPS0VOXzgwMV84OTQ2NGFmMi00NzY5LTQ5MGMtODk3Mi0wZTc4NmMxYmViNTAiLCJjcmVhdGVkIjoxNjkyMjU1MDQwMTI4LCJzeXNVc2VySWQiOjgwMX0.W0J8r2Opgj4u8yZIbz1QXf_NqqIgUnilkQF8Awub-SaNxrxRizr3XqK_kHorEn_2eNzD-0ApvpWOI75kf3t5Ug\"}}', '2023-08-17 14:50:40.141000');
INSERT INTO `t_sys_log` VALUES (6249, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.merchant.MchInfoController.update', '更新商户信息', 'http://localhost:9217/api/mchInfo/M1691231056', '{\"defaultPass\":true,\"agentNo\":\"A1691231069\",\"mchName\":\"测试商户\",\"confirmPwd\":\"\",\"createdUid\":801,\"secret\":\"XFjGbHNUho8MqYWycYsNFzSwBVvOAIepDz1QDTfoIfKFojXVyTFHReNteWzGSVoXyXLJzDAo9p0G6GIjQYPAPJXSttlymJ9pqvaVUsna8v6cRezJvmTSJleEPImlTqDI\",\"createdAt\":\"2023-08-05 18:24:16\",\"loginUserName\":\"test123\",\"initUserId\":100033,\"balance\":0,\"state\":1,\"freezeBalance\":0,\"mchNo\":\"M1691231056\",\"resetPass\":true,\"updatedAt\":\"2023-08-16 23:25:23\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:50:52.828000');
INSERT INTO `t_sys_log` VALUES (6250, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.agent.AgentInfoController.update', '更新代理商信息', 'http://localhost:9217/api/isvInfo/A1691231069', '{\"defaultPass\":true,\"agentNo\":\"A1691231069\",\"confirmPwd\":\"\",\"agentName\":\"测试代理\",\"createdUid\":801,\"createdAt\":\"2023-08-05 18:24:29\",\"loginUserName\":\"atest123\",\"initUserId\":100034,\"balance\":0,\"state\":1,\"freezeBalance\":0,\"resetPass\":true,\"updatedAt\":\"2023-08-05 18:24:29\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:50:59.827000');
INSERT INTO `t_sys_log` VALUES (6251, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.PayWayController.add', '新增支付方式', 'http://localhost:9217/api/payWays', '{\"productId\":\"1000\",\"productName\":\"测试产品\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:51:25.856000');
INSERT INTO `t_sys_log` VALUES (6252, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.ProductMchController.update', '更新产品-商户绑定信息', 'http://localhost:9217/api/productMchInfo/', '{\"productId\":1000,\"mchRate\":0.15,\"state\":1,\"mchNo\":\"M1691231056\",\"agentRate\":0.015}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:51:36.055000');
INSERT INTO `t_sys_log` VALUES (6253, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.agent.AgentInfoController.add', '新增代理商', 'http://localhost:9217/api/isvInfo', '{\"loginUserName\":\"ptest123\",\"agentName\":\"测试通道代理\",\"state\":1}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:51:50.752000');
INSERT INTO `t_sys_log` VALUES (6254, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.passage.MchAppController.add', '新建通道', 'http://localhost:9217/api/mchApps', '{\"ifCode\":\"testpay\",\"payType\":1,\"productId\":1000,\"agentNo\":\"A1692255110\",\"rate\":0.09,\"payPassageName\":\"测试通道\",\"state\":1,\"payRules\":\"10-5000\",\"agentRate\":0.01}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:52:20.142000');
INSERT INTO `t_sys_log` VALUES (6255, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.PayInterfaceDefineController.update', '更新支付接口', 'http://localhost:9217/api/payIfDefines/rixinpay', '{\"ifCode\":\"rixinpay\",\"createdAt\":\"2023-08-06 16:33:51\",\"bgColor\":\"#0CC145\",\"ifParams\":\"[{\\\"desc\\\": \\\"商户号\\\", \\\"name\\\": \\\"mchNo\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"秘钥\\\", \\\"name\\\": \\\"secret\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"通道编码\\\", \\\"name\\\": \\\"payType\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"支付网关\\\", \\\"name\\\": \\\"payGateway\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\\\", \\\"name\\\": \\\"whiteList\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}]\",\"ifName\":\"日鑫支付接口\",\"state\":1,\"updatedAt\":\"2023-08-06 16:33:51\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:52:43.651000');
INSERT INTO `t_sys_log` VALUES (6256, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.PayInterfaceDefineController.update', '更新支付接口', 'http://localhost:9217/api/payIfDefines/jomalongpay', '{\"ifCode\":\"jomalongpay\",\"createdAt\":\"2023-08-05 22:44:38\",\"bgColor\":\"#065C6B\",\"ifParams\":\"[{\\\"desc\\\": \\\"商户号\\\", \\\"name\\\": \\\"mchNo\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"秘钥\\\", \\\"name\\\": \\\"secret\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"产品编码\\\", \\\"name\\\": \\\"payType\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"支付网关\\\", \\\"name\\\": \\\"payGateway\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\\\", \\\"name\\\": \\\"whiteList\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}]\",\"ifName\":\"祖玛珑支付\",\"state\":1,\"updatedAt\":\"2023-08-05 22:44:38\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:52:54.196000');
INSERT INTO `t_sys_log` VALUES (6257, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.PayInterfaceDefineController.update', '更新支付接口', 'http://localhost:9217/api/payIfDefines/xxpay', '{\"ifCode\":\"xxpay\",\"createdAt\":\"2023-08-06 13:10:35\",\"bgColor\":\"#A0A351\",\"ifParams\":\"[{\\\"desc\\\": \\\"商户号\\\", \\\"name\\\": \\\"mchNo\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"秘钥\\\", \\\"name\\\": \\\"secret\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"产品编码\\\", \\\"name\\\": \\\"payType\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"支付网关\\\", \\\"name\\\": \\\"payGateway\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\\\", \\\"name\\\": \\\"whiteList\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}]\",\"ifName\":\"xxpay接口\",\"remark\":\"库利南最支付丰胸鲁班买卖通\",\"state\":1,\"updatedAt\":\"2023-08-06 13:10:35\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:53:00.550000');
INSERT INTO `t_sys_log` VALUES (6258, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.PayInterfaceDefineController.update', '更新支付接口', 'http://localhost:9217/api/payIfDefines/shayupay', '{\"ifCode\":\"shayupay\",\"createdAt\":\"2023-08-07 20:36:40\",\"bgColor\":\"#9248E8\",\"ifParams\":\"[{\\\"desc\\\": \\\"商户号\\\", \\\"name\\\": \\\"mchNo\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"秘钥\\\", \\\"name\\\": \\\"secret\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"通道编码\\\", \\\"name\\\": \\\"payType\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"支付网关\\\", \\\"name\\\": \\\"payGateway\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\\\", \\\"name\\\": \\\"whiteList\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}]\",\"ifName\":\"鲨鱼支付\",\"remark\":\"\",\"state\":1,\"updatedAt\":\"2023-08-07 20:36:40\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:53:05.768000');
INSERT INTO `t_sys_log` VALUES (6259, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.PayInterfaceDefineController.update', '更新支付接口', 'http://localhost:9217/api/payIfDefines/xxpay2', '{\"ifCode\":\"xxpay2\",\"createdAt\":\"2023-08-07 20:37:33\",\"bgColor\":\"#CFE987\",\"ifParams\":\"[{\\\"desc\\\": \\\"商户号\\\", \\\"name\\\": \\\"mchNo\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"秘钥\\\", \\\"name\\\": \\\"secret\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"通道编码\\\", \\\"name\\\": \\\"payType\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"支付网关\\\", \\\"name\\\": \\\"payGateway\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\\\", \\\"name\\\": \\\"whiteList\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}]\",\"ifName\":\"XXPay2接口\",\"state\":1,\"updatedAt\":\"2023-08-07 20:37:33\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:53:09.647000');
INSERT INTO `t_sys_log` VALUES (6260, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.PayInterfaceDefineController.update', '更新支付接口', 'http://localhost:9217/api/payIfDefines/shengyang', '{\"ifCode\":\"shengyang\",\"createdAt\":\"2023-08-09 00:40:06\",\"bgColor\":\"#C9CD52\",\"ifParams\":\"[{\\\"desc\\\": \\\"商户号\\\", \\\"name\\\": \\\"mchNo\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"秘钥\\\", \\\"name\\\": \\\"secret\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"通道编码\\\", \\\"name\\\": \\\"payType\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"支付网关\\\", \\\"name\\\": \\\"payGateway\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\\\", \\\"name\\\": \\\"whiteList\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}]\",\"ifName\":\"盛阳支付\",\"state\":1,\"updatedAt\":\"2023-08-09 00:40:06\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:53:15.583000');
INSERT INTO `t_sys_log` VALUES (6261, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.payconfig.PayInterfaceDefineController.update', '更新支付接口', 'http://localhost:9217/api/payIfDefines/changsheng', '{\"ifCode\":\"changsheng\",\"createdAt\":\"2023-08-11 11:14:04\",\"bgColor\":\"#121AA9\",\"ifParams\":\"[{\\\"desc\\\": \\\"商户号AccessKey\\\", \\\"name\\\": \\\"mchNo\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"秘钥\\\", \\\"name\\\": \\\"secret\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"通道类型\\\", \\\"name\\\": \\\"payType\\\", \\\"type\\\": \\\"text\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"支付网关\\\", \\\"name\\\": \\\"payGateway\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}, {\\\"desc\\\": \\\"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\\\", \\\"name\\\": \\\"whiteList\\\", \\\"type\\\": \\\"textarea\\\", \\\"verify\\\": \\\"required\\\"}]\",\"ifName\":\"昌盛支付\",\"state\":1,\"updatedAt\":\"2023-08-11 11:14:04\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:53:23.500000');
INSERT INTO `t_sys_log` VALUES (6262, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController.add', '添加管理员', 'http://localhost:9217/api/sysUsers', '{\"loginUsername\":\"admin1\",\"sex\":1,\"isAdmin\":1,\"state\":1}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:53:51.034000');
INSERT INTO `t_sys_log` VALUES (6263, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController.add', '添加管理员', 'http://localhost:9217/api/sysUsers', '{\"loginUsername\":\"admin2\",\"sex\":1,\"isAdmin\":1,\"state\":1}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:53:55.629000');
INSERT INTO `t_sys_log` VALUES (6264, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController.add', '添加管理员', 'http://localhost:9217/api/sysUsers', '{\"loginUsername\":\"admin3\",\"sex\":1,\"isAdmin\":1,\"state\":1}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:53:59.700000');
INSERT INTO `t_sys_log` VALUES (6265, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController.add', '添加管理员', 'http://localhost:9217/api/sysUsers', '{\"loginUsername\":\"admin4\",\"sex\":1,\"isAdmin\":1,\"state\":1}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:05.213000');
INSERT INTO `t_sys_log` VALUES (6266, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController.add', '添加管理员', 'http://localhost:9217/api/sysUsers', '{\"loginUsername\":\"admin5\",\"sex\":1,\"isAdmin\":1,\"state\":1}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:10.013000');
INSERT INTO `t_sys_log` VALUES (6267, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController.update', '修改操作员信息', 'http://localhost:9217/api/sysUsers/100120', '{\"defaultPass\":true,\"createdAt\":\"2023-08-17 14:54:09\",\"loginUsername\":\"admin5\",\"sysType\":\"MGR\",\"confirmPwd\":\"\",\"isAdmin\":0,\"state\":1,\"sysUserId\":100120,\"belongInfoId\":\"0\",\"resetPass\":false,\"updatedAt\":\"2023-08-17 14:54:09\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:15.902000');
INSERT INTO `t_sys_log` VALUES (6268, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController.update', '修改操作员信息', 'http://localhost:9217/api/sysUsers/100119', '{\"defaultPass\":true,\"createdAt\":\"2023-08-17 14:54:05\",\"loginUsername\":\"admin4\",\"sysType\":\"MGR\",\"confirmPwd\":\"\",\"isAdmin\":0,\"state\":1,\"sysUserId\":100119,\"belongInfoId\":\"0\",\"resetPass\":false,\"updatedAt\":\"2023-08-17 14:54:05\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:19.705000');
INSERT INTO `t_sys_log` VALUES (6269, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController.update', '修改操作员信息', 'http://localhost:9217/api/sysUsers/100120', '{\"defaultPass\":true,\"createdAt\":\"2023-08-17 14:54:09\",\"loginUsername\":\"admin5\",\"sysType\":\"MGR\",\"confirmPwd\":\"\",\"isAdmin\":0,\"state\":1,\"sysUserId\":100120,\"belongInfoId\":\"0\",\"resetPass\":false,\"updatedAt\":\"2023-08-17 14:54:09\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:26.889000');
INSERT INTO `t_sys_log` VALUES (6270, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserRoleRelaController.relas', '更改用户角色信息', 'http://localhost:9217/api/sysUserRoleRelas/relas/100116', '{\"roleIdListStr\":\"[\\\"ROLE_ADMIN\\\"]\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:32.588000');
INSERT INTO `t_sys_log` VALUES (6271, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserRoleRelaController.relas', '更改用户角色信息', 'http://localhost:9217/api/sysUserRoleRelas/relas/100117', '{\"roleIdListStr\":\"[\\\"ROLE_ADMIN\\\"]\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:37.109000');
INSERT INTO `t_sys_log` VALUES (6272, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserRoleRelaController.relas', '更改用户角色信息', 'http://localhost:9217/api/sysUserRoleRelas/relas/100118', '{\"roleIdListStr\":\"[\\\"ROLE_ADMIN\\\"]\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:40.007000');
INSERT INTO `t_sys_log` VALUES (6273, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserRoleRelaController.relas', '更改用户角色信息', 'http://localhost:9217/api/sysUserRoleRelas/relas/100119', '{\"roleIdListStr\":\"[\\\"ROLE_ADMIN\\\"]\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:42.530000');
INSERT INTO `t_sys_log` VALUES (6274, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserRoleRelaController.relas', '更改用户角色信息', 'http://localhost:9217/api/sysUserRoleRelas/relas/100120', '{\"roleIdListStr\":\"[\\\"ROLE_ADMIN\\\"]\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:54:45.927000');
INSERT INTO `t_sys_log` VALUES (6275, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.merchant.PassageMchController.blindAll', '通道-商户一键全绑定', 'http://localhost:9217/api/passageMchInfo/blindAll/1272', '{}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:55:36.076000');
INSERT INTO `t_sys_log` VALUES (6276, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.passage.MchAppController.update', '更新通道信息', 'http://localhost:9217/api/mchApps/1272', '{\"ifCode\":\"testpay\",\"agentNo\":\"A1692255110\",\"productId\":1000,\"successRate\":\"0.00\",\"weights\":1,\"productName\":\"测试产品\",\"timeLimit\":0,\"createdAt\":\"2023-08-17 14:52:20\",\"timeRules\":\"\",\"payType\":1,\"balance\":0,\"rate\":0.09,\"quota\":0,\"payPassageName\":\"测试通道\",\"payInterfaceConfig\":\"{\\\"mchNo\\\":\\\"1\\\",\\\"secret\\\":\\\"2\\\",\\\"payType\\\":\\\"3\\\",\\\"payGateway\\\":\\\"4\\\",\\\"whiteList\\\":\\\"*\\\"}\",\"payRules\":\"10-5000\",\"quotaLimitState\":0,\"state\":1,\"payPassageId\":1272,\"agentRate\":0.01,\"updatedAt\":\"2023-08-17 14:52:20\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:56:40.528000');
INSERT INTO `t_sys_log` VALUES (6277, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.passage.MchAppController.add', '新建通道', 'http://localhost:9217/api/mchApps', '{\"ifCode\":\"testpay\",\"payType\":1,\"productId\":1000,\"agentNo\":\"\",\"rate\":0.14,\"payPassageName\":\"测试通道2\",\"state\":1,\"payRules\":\"10-50000\",\"agentRate\":0}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:59:51.956000');
INSERT INTO `t_sys_log` VALUES (6278, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.merchant.PassageMchController.blindAll', '通道-商户一键全绑定', 'http://localhost:9217/api/passageMchInfo/blindAll/1273', '{}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 14:59:59.021000');
INSERT INTO `t_sys_log` VALUES (6279, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.passage.MchAppController.update', '更新通道信息', 'http://localhost:9217/api/mchApps/1273', '{\"ifCode\":\"testpay\",\"agentNo\":\"\",\"productId\":1000,\"successRate\":\"0.00\",\"weights\":\"5\",\"productName\":\"测试产品\",\"timeLimit\":0,\"createdAt\":\"2023-08-17 14:59:51\",\"timeRules\":\"\",\"payType\":1,\"balance\":0,\"rate\":0.14,\"quota\":0,\"payPassageName\":\"测试通道2\",\"payRules\":\"10-50000\",\"quotaLimitState\":0,\"state\":1,\"payPassageId\":1273,\"agentRate\":0,\"updatedAt\":\"2023-08-17 14:59:51\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 15:00:08.953000');
INSERT INTO `t_sys_log` VALUES (6280, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.passage.MchAppController.update', '更新通道信息', 'http://localhost:9217/api/mchApps/1273', '{\"ifCode\":\"testpay\",\"agentNo\":\"\",\"productId\":1000,\"weights\":5,\"timeLimit\":0,\"createdAt\":\"2023-08-17 14:59:51\",\"timeRules\":\"\",\"payType\":1,\"balance\":0,\"rate\":0.08,\"quota\":0,\"payPassageName\":\"测试通道2\",\"payRules\":\"10-50000\",\"quotaLimitState\":0,\"state\":1,\"payPassageId\":1273,\"agentRate\":0,\"updatedAt\":\"2023-08-17 15:00:08\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 15:00:44.965000');
INSERT INTO `t_sys_log` VALUES (6281, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.passage.MchAppController.update', '更新通道信息', 'http://localhost:9217/api/mchApps/1273', '{\"ifCode\":\"testpay\",\"agentNo\":\"\",\"productId\":1000,\"successRate\":\"0.00\",\"weights\":5,\"productName\":\"测试产品\",\"timeLimit\":0,\"createdAt\":\"2023-08-17 14:59:51\",\"timeRules\":\"\",\"payType\":1,\"balance\":0,\"rate\":0.08,\"quota\":0,\"payPassageName\":\"测试通道2\",\"payInterfaceConfig\":\"{\\\"mchNo\\\":\\\"1\\\",\\\"secret\\\":\\\"2\\\",\\\"payType\\\":\\\"3\\\",\\\"payGateway\\\":\\\"4\\\",\\\"whiteList\\\":\\\"*\\\"}\",\"payRules\":\"10-50000\",\"quotaLimitState\":0,\"state\":1,\"payPassageId\":1273,\"agentRate\":0,\"updatedAt\":\"2023-08-17 15:00:44\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 15:03:18.637000');
INSERT INTO `t_sys_log` VALUES (6282, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.order.PayOrderController.forcePayOrderRedo', '订单测试冲正', 'http://localhost:9217/api/payOrder/P1692068961648623617/forcePayOrderRedo', '{}', '{\"msg\":\"为了您的资金安全，该操作需要先绑定谷歌验证码\",\"code\":5007}', '2023-08-17 15:04:01.872000');
INSERT INTO `t_sys_log` VALUES (6283, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.order.PayOrderController.forcePayOrderRedo', '订单测试冲正', 'http://localhost:9217/api/payOrder/P1692068961648623617/forcePayOrderRedo', '{}', '{\"msg\":\"SUCCESS\",\"code\":0,\"data\":{\"ifCode\":\"testpay\",\"payOrderId\":\"P1692068961648623617\",\"mchOrderNo\":\"ADXwjziSpy7Gwok\",\"productName\":\"测试产品\",\"passageFeeAmount\":400,\"createdAt\":\"2023-08-17 15:00:52\",\"successTime\":\"2023-08-17 15:03:29\",\"forceChangeLoginName\":\"\",\"state\":2,\"returnUrl\":\"\",\"agentFeeAmount\":75,\"mchNo\":\"M1691231056\",\"notifyParams\":\"{\\\"state\\\":\\\"1\\\"}\",\"agentRate\":0.015000,\"updatedAt\":\"2023-08-17 15:03:49\",\"mchFeeAmount\":750,\"agentPassageFee\":0,\"amount\":5000,\"agentNo\":\"A1691231069\",\"productId\":1000,\"mchName\":\"测试商户\",\"agentNoPassage\":\"\",\"expiredTime\":\"2023-08-17 16:00:52\",\"mchFeeRate\":0.150000,\"notifyState\":1,\"passageOrderNo\":\"\",\"passageRate\":0.080000,\"agentPassageRate\":0.000000,\"clientIp\":\"154.82.113.215\",\"notifyUrl\":\"https://www.test.com\",\"passageId\":1273,\"forceChangeState\":0}}', '2023-08-17 15:04:17.176000');
INSERT INTO `t_sys_log` VALUES (6284, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysRoleController.update', '更新角色信息', 'http://localhost:9217/api/sysRoles/ROLE_cab448', '{\"entIdListStr\":\"[\\\"ENT_C_MAIN_PAY_COUNT\\\",\\\"ENT_C_MAIN\\\",\\\"ENT_C_USERINFO\\\",\\\"ENT_COMMONS\\\",\\\"ENT_DIVISION_AGENT\\\",\\\"ENT_DIVISION_MANAGE\\\",\\\"ENT_DIVISION_MCH\\\",\\\"ENT_ISV_INFO_ADD\\\",\\\"ENT_ISV_INFO\\\",\\\"ENT_ISV\\\",\\\"ENT_ISV_INFO_DEL\\\",\\\"ENT_ISV_INFO_EDIT\\\",\\\"ENT_ISV_INFO_HISTORY\\\",\\\"ENT_ACCOUNT_HISTORY\\\",\\\"ENT_ISV_INFO_VIEW\\\",\\\"ENT_ISV_LIST\\\",\\\"ENT_MCH_APP_ADD\\\",\\\"ENT_MCH_APP\\\",\\\"ENT_PC\\\",\\\"ENT_MCH_APP_CONFIG\\\",\\\"ENT_MCH_INFO\\\",\\\"ENT_MCH\\\",\\\"ENT_MCH_APP_DEL\\\",\\\"ENT_MCH_APP_EDIT\\\",\\\"ENT_MCH_APP_LIST\\\",\\\"ENT_MCH_APP_VIEW\\\",\\\"ENT_MCH_INFO_ADD\\\",\\\"ENT_MCH_INFO_DEL\\\",\\\"ENT_MCH_INFO_EDIT\\\",\\\"ENT_MCH_INFO_HISTORY\\\",\\\"ENT_MCH_INFO_VIEW\\\",\\\"ENT_MCH_LIST\\\",\\\"ENT_MCH_NOTIFY_RESEND\\\",\\\"ENT_MCH_NOTIFY\\\",\\\"ENT_ORDER\\\",\\\"ENT_MCH_NOTIFY_VIEW\\\",\\\"ENT_MCH_PAY_PASSAGE_ADD\\\",\\\"ENT_MCH_PAY_PASSAGE_LIST\\\",\\\"ENT_MCH_PAY_PASSAGE_CONFIG\\\",\\\"ENT_NOTIFY_LIST\\\",\\\"ENT_ORDER_LIST\\\",\\\"ENT_PAY_ORDER\\\",\\\"ENT_PASSAGE_HISTORY\\\",\\\"ENT_PAY_FORCE_ORDER\\\",\\\"ENT_PAY_ORDER_EDIT\\\",\\\"ENT_PAY_ORDER_SEARCH_PAY_WAY\\\",\\\"ENT_PAY_ORDER_VIEW\\\",\\\"ENT_PC_IF_DEFINE_ADD\\\",\\\"ENT_PC_IF_DEFINE\\\",\\\"ENT_PC_IF_DEFINE_DEL\\\",\\\"ENT_PC_IF_DEFINE_EDIT\\\",\\\"ENT_PC_IF_DEFINE_LIST\\\",\\\"ENT_PC_IF_DEFINE_SEARCH\\\",\\\"ENT_PC_IF_DEFINE_VIEW\\\",\\\"ENT_PC_WAY_ADD\\\",\\\"ENT_PC_WAY\\\",\\\"ENT_PC_WAY_DEL\\\",\\\"ENT_PC_WAY_EDIT\\\",\\\"ENT_PC_WAY_LIST\\\",\\\"ENT_PC_WAY_SEARCH\\\",\\\"ENT_PC_WAY_VIEW\\\",\\\"ENT_TRANSFER_ORDER_LIST\\\",\\\"ENT_TRANSFER_ORDER\\\",\\\"ENT_TRANSFER_ORDER_VIEW\\\"]\",\"sysType\":\"MGR\",\"roleId\":\"ROLE_cab448\",\"roleName\":\"财务运营\",\"belongInfoId\":\"0\",\"updatedAt\":\"2023-07-27 23:46:55\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 15:07:51.111000');
INSERT INTO `t_sys_log` VALUES (6285, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysRoleController.update', '更新角色信息', 'http://localhost:9217/api/sysRoles/ROLE_OP', '{\"entIdListStr\":\"[\\\"ENT_C_MAIN_PAY_COUNT\\\",\\\"ENT_C_MAIN\\\",\\\"ENT_C_USERINFO\\\",\\\"ENT_COMMONS\\\",\\\"ENT_ISV_INFO_ADD\\\",\\\"ENT_ISV_INFO\\\",\\\"ENT_ISV\\\",\\\"ENT_ISV_INFO_DEL\\\",\\\"ENT_ISV_INFO_EDIT\\\",\\\"ENT_ISV_INFO_VIEW\\\",\\\"ENT_ISV_LIST\\\",\\\"ENT_ISV_INFO_HISTORY\\\",\\\"ENT_ACCOUNT_HISTORY\\\",\\\"ENT_PASSAGE_HISTORY\\\",\\\"ENT_MCH_INFO_HISTORY\\\",\\\"ENT_LOG_LIST\\\",\\\"ENT_SYS_LOG\\\",\\\"ENT_SYS_CONFIG\\\",\\\"ENT_SYS_LOG_VIEW\\\",\\\"ENT_MCH_APP_ADD\\\",\\\"ENT_MCH_APP\\\",\\\"ENT_PC\\\",\\\"ENT_MCH_APP_DEL\\\",\\\"ENT_MCH_APP_EDIT\\\",\\\"ENT_MCH_APP_LIST\\\",\\\"ENT_MCH_APP_VIEW\\\",\\\"ENT_MCH_PAY_PASSAGE_LIST\\\",\\\"ENT_MCH_PAY_PASSAGE_ADD\\\",\\\"ENT_MCH_PAY_PASSAGE_CONFIG\\\",\\\"ENT_PC_WAY\\\",\\\"ENT_PC_WAY_ADD\\\",\\\"ENT_PC_WAY_DEL\\\",\\\"ENT_PC_WAY_EDIT\\\",\\\"ENT_PC_WAY_LIST\\\",\\\"ENT_PC_WAY_SEARCH\\\",\\\"ENT_PC_WAY_VIEW\\\",\\\"ENT_PC_IF_DEFINE\\\",\\\"ENT_PC_IF_DEFINE_ADD\\\",\\\"ENT_PC_IF_DEFINE_DEL\\\",\\\"ENT_PC_IF_DEFINE_EDIT\\\",\\\"ENT_PC_IF_DEFINE_LIST\\\",\\\"ENT_PC_IF_DEFINE_SEARCH\\\",\\\"ENT_PC_IF_DEFINE_VIEW\\\",\\\"ENT_MCH_APP_CONFIG\\\",\\\"ENT_MCH_INFO\\\",\\\"ENT_MCH\\\",\\\"ENT_MCH_INFO_ADD\\\",\\\"ENT_MCH_INFO_DEL\\\",\\\"ENT_MCH_INFO_EDIT\\\",\\\"ENT_MCH_INFO_VIEW\\\",\\\"ENT_MCH_LIST\\\",\\\"ENT_MCH_NOTIFY_RESEND\\\",\\\"ENT_MCH_NOTIFY\\\",\\\"ENT_ORDER\\\",\\\"ENT_MCH_NOTIFY_VIEW\\\",\\\"ENT_NOTIFY_LIST\\\",\\\"ENT_PAY_ORDER\\\",\\\"ENT_ORDER_LIST\\\",\\\"ENT_PAY_ORDER_EDIT\\\",\\\"ENT_PAY_ORDER_SEARCH_PAY_WAY\\\",\\\"ENT_PAY_ORDER_VIEW\\\",\\\"ENT_PAY_FORCE_ORDER\\\",\\\"ENT_TRANSFER_ORDER\\\",\\\"ENT_TRANSFER_ORDER_LIST\\\",\\\"ENT_TRANSFER_ORDER_VIEW\\\"]\",\"sysType\":\"MGR\",\"roleId\":\"ROLE_OP\",\"roleName\":\"普通操作员\",\"belongInfoId\":\"0\",\"updatedAt\":\"2021-05-01 00:00:00\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 15:08:42.227000');
INSERT INTO `t_sys_log` VALUES (6286, 'admin', '0:0:0:0:0:0:0:1', 'MGR', 'com.jeequan.jeepay.mgr.ctrl.sysuser.SysRoleController.update', '更新角色信息', 'http://localhost:9217/api/sysRoles/ROLE_354990', '{\"entIdListStr\":\"[\\\"ENT_C_USERINFO\\\",\\\"ENT_COMMONS\\\",\\\"ENT_ISV_INFO_ADD\\\",\\\"ENT_ISV_INFO\\\",\\\"ENT_ISV\\\",\\\"ENT_ISV_INFO_DEL\\\",\\\"ENT_ISV_INFO_EDIT\\\",\\\"ENT_ISV_INFO_HISTORY\\\",\\\"ENT_ACCOUNT_HISTORY\\\",\\\"ENT_ISV_INFO_VIEW\\\",\\\"ENT_ISV_LIST\\\",\\\"ENT_MCH_APP_CONFIG\\\",\\\"ENT_MCH_INFO\\\",\\\"ENT_MCH\\\",\\\"ENT_MCH_INFO_ADD\\\",\\\"ENT_MCH_INFO_DEL\\\",\\\"ENT_MCH_INFO_EDIT\\\",\\\"ENT_MCH_INFO_HISTORY\\\",\\\"ENT_MCH_INFO_VIEW\\\",\\\"ENT_MCH_LIST\\\",\\\"ENT_MCH_NOTIFY_RESEND\\\",\\\"ENT_MCH_NOTIFY\\\",\\\"ENT_ORDER\\\",\\\"ENT_MCH_NOTIFY_VIEW\\\",\\\"ENT_NOTIFY_LIST\\\",\\\"ENT_ORDER_LIST\\\",\\\"ENT_PAY_ORDER\\\",\\\"ENT_PASSAGE_HISTORY\\\",\\\"ENT_PAY_FORCE_ORDER\\\",\\\"ENT_PAY_ORDER_EDIT\\\",\\\"ENT_PAY_ORDER_SEARCH_PAY_WAY\\\",\\\"ENT_PAY_ORDER_VIEW\\\",\\\"ENT_TRANSFER_ORDER_LIST\\\",\\\"ENT_TRANSFER_ORDER\\\",\\\"ENT_TRANSFER_ORDER_VIEW\\\"]\",\"sysType\":\"MGR\",\"roleId\":\"ROLE_354990\",\"roleName\":\"客服\",\"belongInfoId\":\"0\",\"updatedAt\":\"2023-07-27 23:47:32\"}', '{\"msg\":\"SUCCESS\",\"code\":0}', '2023-08-17 15:08:49.158000');

-- ----------------------------
-- Table structure for t_sys_role
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_role`;
CREATE TABLE `t_sys_role`  (
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID, ROLE_开头',
  `role_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `sys_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
  `belong_info_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '所属商户ID / 0(平台)',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  INDEX `id`(`role_id`, `sys_type`, `belong_info_id`, `updated_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_role
-- ----------------------------
INSERT INTO `t_sys_role` VALUES ('ROLE_354990', '客服', 'MGR', '0', '2023-07-27 23:47:32.000000');
INSERT INTO `t_sys_role` VALUES ('ROLE_ADMIN', '系统管理员', 'MGR', '0', '2021-05-01 00:00:00.000000');
INSERT INTO `t_sys_role` VALUES ('ROLE_cab448', '财务运营', 'MGR', '0', '2023-07-27 23:46:55.000000');
INSERT INTO `t_sys_role` VALUES ('ROLE_OP', '普通操作员', 'MGR', '0', '2021-05-01 00:00:00.000000');

-- ----------------------------
-- Table structure for t_sys_role_ent_rela
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_role_ent_rela`;
CREATE TABLE `t_sys_role_ent_rela`  (
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  `ent_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `ent_id`) USING BTREE,
  INDEX `id`(`role_id`, `ent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_role_ent_rela
-- ----------------------------
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ACCOUNT_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_COMMONS');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_C_USERINFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ISV');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ISV_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ISV_INFO_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ISV_INFO_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ISV_INFO_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ISV_INFO_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ISV_INFO_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ISV_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_APP_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_INFO_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_INFO_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_INFO_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_INFO_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_INFO_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_NOTIFY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_NOTIFY_RESEND');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_MCH_NOTIFY_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_NOTIFY_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_PASSAGE_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_PAY_FORCE_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_PAY_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_PAY_ORDER_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_PAY_ORDER_SEARCH_PAY_WAY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_PAY_ORDER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_TRANSFER_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_TRANSFER_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_354990', 'ENT_TRANSFER_ORDER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ACCOUNT_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_COMMONS');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_C_MAIN');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_C_MAIN_PAY_COUNT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_C_USERINFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_DIVISION_AGENT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_DIVISION_MANAGE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_DIVISION_MCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ISV');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ISV_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ISV_INFO_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ISV_INFO_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ISV_INFO_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ISV_INFO_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ISV_INFO_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ISV_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_LOG_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_APP');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_APP_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_APP_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_APP_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_APP_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_APP_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_APP_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_INFO_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_INFO_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_INFO_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_INFO_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_INFO_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_NOTIFY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_NOTIFY_RESEND');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_NOTIFY_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_PAY_PASSAGE_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_PAY_PASSAGE_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MCH_PAY_PASSAGE_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MGR_AGENT_STAT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MGR_MCH_PRODUCT_STAT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MGR_MCH_STAT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MGR_PASSAGE_STAT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MGR_PLAT_STAT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MGR_PRODUCT_STAT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_MGR_STAT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_NOTIFY_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PASSAGE_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PAY_FORCE_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PAY_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PAY_ORDER_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PAY_ORDER_SEARCH_PAY_WAY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PAY_ORDER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_IF_DEFINE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_IF_DEFINE_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_IF_DEFINE_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_IF_DEFINE_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_IF_DEFINE_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_IF_DEFINE_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_IF_DEFINE_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_WAY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_WAY_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_WAY_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_WAY_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_WAY_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_WAY_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_PC_WAY_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_SYS_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_SYS_CONFIG_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_SYS_CONFIG_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_SYS_CONFIG_ROBOTS');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_SYS_LOG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_SYS_LOG_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_TRANSFER_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_TRANSFER_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_TRANSFER_ORDER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_DIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_ENT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_ENT_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_ENT_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_ROLE_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_USER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_USER_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_USER_DELETE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_USER_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_USER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_USER_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_USER_UPD_ROLE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_ADMIN', 'ENT_UR_USER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ACCOUNT_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_COMMONS');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_C_MAIN');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_C_MAIN_PAY_COUNT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_C_USERINFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_DIVISION_AGENT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_DIVISION_MANAGE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_DIVISION_MCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ISV');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ISV_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ISV_INFO_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ISV_INFO_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ISV_INFO_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ISV_INFO_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ISV_INFO_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ISV_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_APP');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_APP_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_APP_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_APP_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_APP_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_APP_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_APP_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_INFO_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_INFO_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_INFO_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_INFO_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_INFO_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_NOTIFY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_NOTIFY_RESEND');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_NOTIFY_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_PAY_PASSAGE_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_PAY_PASSAGE_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_MCH_PAY_PASSAGE_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_NOTIFY_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PASSAGE_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PAY_FORCE_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PAY_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PAY_ORDER_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PAY_ORDER_SEARCH_PAY_WAY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PAY_ORDER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_IF_DEFINE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_IF_DEFINE_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_IF_DEFINE_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_IF_DEFINE_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_IF_DEFINE_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_IF_DEFINE_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_IF_DEFINE_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_WAY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_WAY_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_WAY_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_WAY_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_WAY_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_WAY_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_PC_WAY_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_TRANSFER_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_TRANSFER_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_cab448', 'ENT_TRANSFER_ORDER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ACCOUNT_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_COMMONS');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_C_MAIN');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_C_MAIN_PAY_COUNT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_C_USERINFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ISV');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ISV_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ISV_INFO_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ISV_INFO_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ISV_INFO_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ISV_INFO_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ISV_INFO_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ISV_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_LOG_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_APP');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_APP_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_APP_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_APP_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_APP_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_APP_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_APP_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_INFO_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_INFO_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_INFO_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_INFO_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_INFO_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_NOTIFY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_NOTIFY_RESEND');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_NOTIFY_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_PAY_PASSAGE_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_PAY_PASSAGE_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_MCH_PAY_PASSAGE_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_NOTIFY_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PASSAGE_HISTORY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PAY_FORCE_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PAY_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PAY_ORDER_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PAY_ORDER_SEARCH_PAY_WAY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PAY_ORDER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_IF_DEFINE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_IF_DEFINE_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_IF_DEFINE_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_IF_DEFINE_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_IF_DEFINE_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_IF_DEFINE_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_IF_DEFINE_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_WAY');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_WAY_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_WAY_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_WAY_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_WAY_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_WAY_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_PC_WAY_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_SYS_CONFIG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_SYS_LOG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_SYS_LOG_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_TRANSFER_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_TRANSFER_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_TRANSFER_ORDER_VIEW');

-- ----------------------------
-- Table structure for t_sys_user
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user`;
CREATE TABLE `t_sys_user`  (
  `sys_user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '系统用户ID',
  `login_username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录用户名',
  `is_admin` tinyint(6) NOT NULL DEFAULT 0 COMMENT '是否超管（超管拥有全部权限） 0-否 1-是',
  `state` tinyint(6) NOT NULL DEFAULT 0 COMMENT '状态 0-停用 1-启用',
  `sys_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心,AGENT-代理中心',
  `belong_info_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '所属商户ID / 0(平台)',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`sys_user_id`) USING BTREE,
  INDEX `sys_type`(`sys_type`, `login_username`, `state`, `belong_info_id`, `created_at`, `is_admin`, `sys_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100121 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_user
-- ----------------------------
INSERT INTO `t_sys_user` VALUES (801, 'admin', 1, 1, 'MGR', '0', '2020-06-13 00:00:00.000000', '2023-06-15 16:38:15.078204');
INSERT INTO `t_sys_user` VALUES (100033, 'test123', 1, 1, 'MCH', 'M1691231056', '2023-08-05 18:24:16.352531', '2023-08-05 18:24:16.352531');
INSERT INTO `t_sys_user` VALUES (100034, 'atest123', 1, 1, 'AGENT', 'A1691231069', '2023-08-05 18:24:29.466723', '2023-08-05 18:24:29.466723');
INSERT INTO `t_sys_user` VALUES (100115, 'ptest123', 1, 1, 'AGENT', 'A1692255110', '2023-08-17 14:51:50.684388', '2023-08-17 14:51:50.684388');
INSERT INTO `t_sys_user` VALUES (100116, 'admin1', 1, 1, 'MGR', '0', '2023-08-17 14:53:50.967866', '2023-08-17 14:53:50.967866');
INSERT INTO `t_sys_user` VALUES (100117, 'admin2', 1, 1, 'MGR', '0', '2023-08-17 14:53:55.564041', '2023-08-17 14:53:55.564041');
INSERT INTO `t_sys_user` VALUES (100118, 'admin3', 1, 1, 'MGR', '0', '2023-08-17 14:53:59.635474', '2023-08-17 14:53:59.635474');
INSERT INTO `t_sys_user` VALUES (100119, 'admin4', 1, 1, 'MGR', '0', '2023-08-17 14:54:05.145022', '2023-08-17 14:54:05.145022');
INSERT INTO `t_sys_user` VALUES (100120, 'admin5', 1, 1, 'MGR', '0', '2023-08-17 14:54:09.948398', '2023-08-17 14:54:09.948398');

-- ----------------------------
-- Table structure for t_sys_user_auth
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_auth`;
CREATE TABLE `t_sys_user_auth`  (
  `auth_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT 'user_id',
  `identity_type` tinyint(6) NOT NULL DEFAULT 0 COMMENT '登录类型  1-登录账号 2-手机号 3-邮箱  10-微信  11-QQ 12-支付宝 13-微博',
  `identifier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '认证标识 ( 用户名 | open_id )',
  `credential` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码凭证',
  `salt` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'salt',
  `sys_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
  `google_auth_status` tinyint(6) NOT NULL DEFAULT 0 COMMENT '绑定谷歌验证状态,0-未绑定,1-已绑定',
  `google_auth_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '谷歌验证密钥',
  PRIMARY KEY (`auth_id`) USING BTREE,
  INDEX `index`(`auth_id`, `user_id`, `identity_type`, `sys_type`, `google_auth_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1123 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户认证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_user_auth
-- ----------------------------
INSERT INTO `t_sys_user_auth` VALUES (801, 801, 1, 'admin', '$2a$10$8/eu5o3x5eRDuo0uo4j9Mu/U/Wrqzfn.Tj.zsc4Z3oSpclBr0ti92', 'testkey', 'MGR', 0, 'MLC72VZKZRDOTCTM73TVGIN6F7FZOKPT');
INSERT INTO `t_sys_user_auth` VALUES (1035, 100033, 1, 'test123', '$2a$10$LQDxeLAX37AT56UHCUubG.BAVe8cUOM3rzZ027yZ13GTASwjQ1oC2', 'f9026c', 'MCH', 0, NULL);
INSERT INTO `t_sys_user_auth` VALUES (1036, 100034, 1, 'atest123', '$2a$10$abd0fqbyjp0OmN.f3qTTdu7lRODI/Z6gCKXVRhGpIthPvdAQmHs0W', '066634', 'AGENT', 0, NULL);
INSERT INTO `t_sys_user_auth` VALUES (1117, 100115, 1, 'ptest123', '$2a$10$6F6IiSwpqU3sKyErVfV/POwNcrzOUv.Xhm/hGvCpX.VXAK5bA953i', '5f8530', 'AGENT', 0, NULL);
INSERT INTO `t_sys_user_auth` VALUES (1118, 100116, 1, 'admin1', '$2a$10$pi/ZIgAXF66Ijm1DLm7YxuA2AgNvPjuoqH7ubsMCmxmiLFBpbY7Fq', '52c2b9', 'MGR', 0, NULL);
INSERT INTO `t_sys_user_auth` VALUES (1119, 100117, 1, 'admin2', '$2a$10$n9B1.49.1AAeUv2bggfuyOYbmROYajuS1Fy4iabqjYWx9VrEWt86K', 'dc81ce', 'MGR', 0, NULL);
INSERT INTO `t_sys_user_auth` VALUES (1120, 100118, 1, 'admin3', '$2a$10$31FS1Ee9glSHePNuLmg8Lu9FkDCdLCgIZJk4xMEfDJG0d4ctGZhsi', 'd3462e', 'MGR', 0, NULL);
INSERT INTO `t_sys_user_auth` VALUES (1121, 100119, 1, 'admin4', '$2a$10$66ouVXC4wzX/3sI0ShIlI.2pkm1I7JJypw2UB.620LkCvNgkD7V7a', 'bfab67', 'MGR', 0, NULL);
INSERT INTO `t_sys_user_auth` VALUES (1122, 100120, 1, 'admin5', '$2a$10$JY9TLOSMx8QV6Gn7ywHkXen0mF67B1Oyq2FWsValRelEQbeiBu2ue', '95bb18', 'MGR', 0, NULL);

-- ----------------------------
-- Table structure for t_sys_user_role_rela
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_role_rela`;
CREATE TABLE `t_sys_user_role_rela`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `index`(`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作员<->角色 关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_user_role_rela
-- ----------------------------
INSERT INTO `t_sys_user_role_rela` VALUES (801, 'ROLE_ADMIN');
INSERT INTO `t_sys_user_role_rela` VALUES (100022, 'ROLE_OP');
INSERT INTO `t_sys_user_role_rela` VALUES (100027, 'ROLE_ADMIN');
INSERT INTO `t_sys_user_role_rela` VALUES (100029, 'ROLE_354990');
INSERT INTO `t_sys_user_role_rela` VALUES (100035, 'ROLE_ADMIN');
INSERT INTO `t_sys_user_role_rela` VALUES (100108, 'ROLE_cab448');
INSERT INTO `t_sys_user_role_rela` VALUES (100116, 'ROLE_ADMIN');
INSERT INTO `t_sys_user_role_rela` VALUES (100117, 'ROLE_ADMIN');
INSERT INTO `t_sys_user_role_rela` VALUES (100118, 'ROLE_ADMIN');
INSERT INTO `t_sys_user_role_rela` VALUES (100119, 'ROLE_ADMIN');
INSERT INTO `t_sys_user_role_rela` VALUES (100120, 'ROLE_ADMIN');

SET FOREIGN_KEY_CHECKS = 1;
