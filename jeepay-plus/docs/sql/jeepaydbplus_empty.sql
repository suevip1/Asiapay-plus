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

 Date: 28/08/2023 02:26:37
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
) ENGINE = InnoDB AUTO_INCREMENT = 15947 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商资金账户流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_agent_account_history
-- ----------------------------

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
INSERT INTO `t_agent_account_info` VALUES ('A1691231069', '测试代理', 0, 1, NULL, 100034, 801, '2023-08-05 18:24:29.464283', '2023-08-05 18:24:29.535498', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 137 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分账记录表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 87600 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户资金账户流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_history
-- ----------------------------

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
INSERT INTO `t_mch_info` VALUES ('M1691231056', '测试商户', 1, NULL, 100033, 801, '2023-08-05 18:24:16.000000', '2023-08-16 23:25:23.025187', 'XFjGbHNUho8MqYWycYsNFzSwBVvOAIepDz1QDTfoIfKFojXVyTFHReNteWzGSVoXyXLJzDAo9p0G6GIjQYPAPJXSttlymJ9pqvaVUsna8v6cRezJvmTSJleEPImlTqDI', NULL, '', 0, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 87293 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户通知记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_notify_record
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 17839 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户-通道关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_pay_passage
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 3307 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户-产品关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mch_product
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 72720 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通道余额调额记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_passage_transaction_history
-- ----------------------------

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
INSERT INTO `t_pay_interface_define` VALUES ('changsheng', '昌盛、腾达', '[{\"desc\": \"商户号AccessKey\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#121AA9', 1, NULL, '2023-08-11 11:14:04.000000', '2023-08-11 11:14:04.000000');
INSERT INTO `t_pay_interface_define` VALUES ('changsheng2', '昌盛支付2', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#2334BA', 1, NULL, '2023-08-23 17:25:50.599861', '2023-08-23 17:25:50.599861');
INSERT INTO `t_pay_interface_define` VALUES ('chuangxin', '创新支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#208684', 1, NULL, '2023-08-13 20:04:53.062313', '2023-08-13 20:04:53.062313');
INSERT INTO `t_pay_interface_define` VALUES ('gawasy', 'Gawasy支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#45CABB', 1, NULL, '2023-08-09 18:19:52.684416', '2023-08-09 18:19:52.684416');
INSERT INTO `t_pay_interface_define` VALUES ('jomalongpay', '祖玛珑,lv,鸿蒙', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"产品编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#065C6B', 1, NULL, '2023-08-05 22:44:38.000000', '2023-08-05 22:44:38.000000');
INSERT INTO `t_pay_interface_define` VALUES ('languifang', '兰桂坊支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"查询网关\", \"name\": \"queryUrl\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#B72E07', 1, NULL, '2023-08-05 23:21:43.441595', '2023-08-05 23:21:43.441595');
INSERT INTO `t_pay_interface_define` VALUES ('naicha', '奶茶支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#640033', 1, NULL, '2023-08-19 16:19:47.387714', '2023-08-19 16:19:47.387714');
INSERT INTO `t_pay_interface_define` VALUES ('pay731', '731支付接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#7F71FE', 1, NULL, '2023-08-13 02:15:42.946802', '2023-08-13 02:15:42.946802');
INSERT INTO `t_pay_interface_define` VALUES ('qipay', '百川支付接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#CBD3CF', 1, NULL, '2023-08-05 20:20:53.000000', '2023-08-05 20:20:53.000000');
INSERT INTO `t_pay_interface_define` VALUES ('rixinpay', '日鑫/JD支付/刀哥/ 万家/吉吉', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#0CC145', 1, NULL, '2023-08-06 16:33:51.000000', '2023-08-06 16:33:51.000000');
INSERT INTO `t_pay_interface_define` VALUES ('shayupay', '鲨鱼支付 桃园星河', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#9248E8', 1, '', '2023-08-07 20:36:40.000000', '2023-08-07 20:36:40.000000');
INSERT INTO `t_pay_interface_define` VALUES ('shengyang', '盛阳支付亨利', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#C9CD52', 1, NULL, '2023-08-09 00:40:06.000000', '2023-08-09 00:40:06.000000');
INSERT INTO `t_pay_interface_define` VALUES ('testpay', '测试接口', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"支付类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#E2791E', 1, NULL, '2023-06-29 01:30:32.000000', '2023-06-29 01:30:32.000000');
INSERT INTO `t_pay_interface_define` VALUES ('tianhepay', '天合支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#3E463F', 1, NULL, '2023-08-08 17:25:54.459954', '2023-08-08 17:25:54.459954');
INSERT INTO `t_pay_interface_define` VALUES ('xiaobawang', '小霸王支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#104A23', 1, NULL, '2023-08-15 12:25:42.096118', '2023-08-15 12:25:42.096118');
INSERT INTO `t_pay_interface_define` VALUES ('xiaoji', '小鸡支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道类型\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"查询网关\", \"name\": \"queryUrl\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔，*表示允许全部IP)\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#38FFEB', 1, NULL, '2023-08-14 23:05:27.214268', '2023-08-14 23:05:27.214268');
INSERT INTO `t_pay_interface_define` VALUES ('xxpay', 'xxpay接口库利南最支付丰胸鲁班买卖通最支付光支付', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"产品编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#A0A351', 1, '库利南最支付丰胸鲁班买卖通', '2023-08-06 13:10:35.000000', '2023-08-06 13:10:35.000000');
INSERT INTO `t_pay_interface_define` VALUES ('xxpay2', 'XXPay2接口宇宙系统  大白兔', '[{\"desc\": \"商户号\", \"name\": \"mchNo\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"秘钥\", \"name\": \"secret\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"通道编码\", \"name\": \"payType\", \"type\": \"text\", \"verify\": \"required\"}, {\"desc\": \"支付网关\", \"name\": \"payGateway\", \"type\": \"textarea\", \"verify\": \"required\"}, {\"desc\": \"回调白名单(多个地址以 | 分隔)，*表示允许全部IP\", \"name\": \"whiteList\", \"type\": \"textarea\", \"verify\": \"required\"}]', '#CFE987', 1, NULL, '2023-08-07 20:37:33.000000', '2023-08-07 20:37:33.000000');
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
) ENGINE = InnoDB AUTO_INCREMENT = 1346 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付通道表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_pay_passage
-- ----------------------------
INSERT INTO `t_pay_passage` VALUES (1008, '测试通道', 'testpay', 1000, 0.100000, 1, '10-5000', '{\"mchNo\": \"1\", \"secret\": \"2\", \"payType\": \"3\", \"whiteList\": \"*\", \"payGateway\": \"4\"}', 0, 0, 1, 0, 0, '2023-08-05 18:24:56.000000', '2023-08-14 00:14:18.118000', '', 0.000000, 0, '22:59|23:25');

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
INSERT INTO `t_product` VALUES (1000, '测试专用', '2023-08-06 17:21:54.572724', '2023-08-06 17:21:54.571000');

-- ----------------------------
-- Table structure for t_robots_mch
-- ----------------------------
DROP TABLE IF EXISTS `t_robots_mch`;
CREATE TABLE `t_robots_mch`  (
  `chat_id` bigint(30) NOT NULL COMMENT 'chat id',
  `mch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '商户号',
  `balance` bigint(30) NULL DEFAULT 0 COMMENT '群记账余额',
  PRIMARY KEY (`chat_id`) USING BTREE,
  INDEX `index`(`mch_no`, `chat_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'chat-商户绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_robots_mch
-- ----------------------------

-- ----------------------------
-- Table structure for t_robots_mch_records
-- ----------------------------
DROP TABLE IF EXISTS `t_robots_mch_records`;
CREATE TABLE `t_robots_mch_records`  (
  `robot_mch_record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `chat_id` bigint(60) NULL DEFAULT NULL COMMENT 'chat_id',
  `amount` bigint(30) NOT NULL DEFAULT 0 COMMENT '金额',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作者用户名',
  `created_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '类型：下发-DAY,记账-TOTAL',
  `state` tinyint(6) NOT NULL DEFAULT 1 COMMENT '记录状态：0-撤销 1-可用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`robot_mch_record_id`) USING BTREE,
  INDEX `index`(`user_name`, `created_at`, `chat_id`, `type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_robots_mch_records
-- ----------------------------

-- ----------------------------
-- Table structure for t_robots_passage
-- ----------------------------
DROP TABLE IF EXISTS `t_robots_passage`;
CREATE TABLE `t_robots_passage`  (
  `passage_id` bigint(20) NOT NULL COMMENT '通道ID/通道群组ID',
  `chat_id` bigint(30) NOT NULL COMMENT '群组ID',
  PRIMARY KEY (`passage_id`) USING BTREE,
  INDEX `index`(`passage_id`, `chat_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_robots_passage
-- ----------------------------

-- ----------------------------
-- Table structure for t_robots_user
-- ----------------------------
DROP TABLE IF EXISTS `t_robots_user`;
CREATE TABLE `t_robots_user`  (
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机器人用户名',
  PRIMARY KEY (`user_name`) USING BTREE,
  INDEX `index`(`user_name`) USING BTREE
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
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_agent
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商-商户日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_agent_mch
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 239 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代理商-通道日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_agent_passage
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 611 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_mch
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 2211 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户-产品日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_mch_product
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 1346 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付通道日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_passage
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 389 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付产品日统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_statistics_product
-- ----------------------------

-- ----------------------------
-- Table structure for t_sys_config
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_config`;
CREATE TABLE `t_sys_config`  (
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置KEY',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
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
INSERT INTO `t_sys_config` VALUES ('errorOrderWarnConfig', '异常订单预警 (0关闭，任意正数打开，如5)', '每分钟检测一次，异常订单数超过设置的数量时机器人发预警到管理群', 'robotsConfigGroup', '机器人配置', '10', 'text', 10, '2023-08-20 13:06:53.183269');
INSERT INTO `t_sys_config` VALUES ('forceOrderWarnConfig', '强制补单预警(0关闭，任意正数打开，如5)', '每分钟检测一次，强制补单数超过设置的数量时机器人发预警到管理群', 'robotsConfigGroup', '机器人配置', '5', 'text', 9, '2023-08-20 13:06:53.183269');
INSERT INTO `t_sys_config` VALUES ('loginWhiteList', '运营平台登录白名单(多个IP | 隔开,允许所有IP登录为*)', '运营平台登录白名单', 'applicationConfig', '系统应用配置', '*', 'textarea', 0, '2023-07-26 15:16:34.581903');
INSERT INTO `t_sys_config` VALUES ('mchSiteUrl', '商户平台网址(不包含结尾/)', '商户平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://mch-api.eluosi-pay.com', 'text', 0, '2023-08-06 16:30:07.514215');
INSERT INTO `t_sys_config` VALUES ('mgrSiteUrl', '运营平台网址(不包含结尾/)', '运营平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://mgr-api.eluosi-pay.com', 'text', 0, '2023-08-06 16:30:07.494783');
INSERT INTO `t_sys_config` VALUES ('passageConfig', '通道配置预警(0关闭，1打开)', '三方通道商户信息被修改时机器人发预警到管理群', 'robotsConfigGroup', '机器人配置', '1', 'text', 8, '2023-08-20 13:06:53.183269');
INSERT INTO `t_sys_config` VALUES ('payPassageAutoClean', '通道余额过零点自动清零', '通道余额过零点自动清零', 'payConfigGroup', '支付配置', '1', 'text', 0, '2023-08-13 18:35:07.601811');
INSERT INTO `t_sys_config` VALUES ('paySiteUrl', '支付网关地址(不包含结尾/)', '支付网关地址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://pay-api.eluosi-pay.com', 'text', 0, '2023-08-06 16:30:07.465521');
INSERT INTO `t_sys_config` VALUES ('robotsAdmin', '机器人管理员(飞机用户名)', '机器人管理员', 'robotsConfigGroup', '机器人配置', '', 'text', 0, '2023-08-28 02:23:32.363813');
INSERT INTO `t_sys_config` VALUES ('robotsToken', '机器人token(勿乱动，可能导致机器人失效)', '机器人token', 'robotsConfigGroup', '机器人配置', '', 'text', 0, '2023-08-28 02:23:35.727446');
INSERT INTO `t_sys_config` VALUES ('robotsUserName', '机器人用户名(勿乱动，可能导致机器人失效)', '机器人用户名(不带@)', 'robotsConfigGroup', '机器人配置', '', 'text', 0, '2023-08-28 02:23:37.410225');

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
) ENGINE = InnoDB AUTO_INCREMENT = 8542 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_log
-- ----------------------------

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
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_SYS_CONFIG_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_SYS_CONFIG_INFO');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_SYS_LOG');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_SYS_LOG_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_TRANSFER_ORDER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_TRANSFER_ORDER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_TRANSFER_ORDER_VIEW');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_DEL');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_DIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_ENT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_ENT_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_ENT_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_ROLE_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_USER');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_USER_ADD');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_USER_DELETE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_USER_EDIT');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_USER_LIST');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_USER_SEARCH');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_USER_UPD_ROLE');
INSERT INTO `t_sys_role_ent_rela` VALUES ('ROLE_OP', 'ENT_UR_USER_VIEW');

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
) ENGINE = InnoDB AUTO_INCREMENT = 100120 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_user
-- ----------------------------
INSERT INTO `t_sys_user` VALUES (801, 'admin', 1, 1, 'MGR', '0', '2020-06-12 16:00:00.000000', '2023-06-15 08:38:15.078204');
INSERT INTO `t_sys_user` VALUES (100033, 'test123', 1, 1, 'MCH', 'M1691231056', '2023-08-05 10:24:16.352531', '2023-08-05 10:24:16.352531');
INSERT INTO `t_sys_user` VALUES (100034, 'atest123', 1, 1, 'AGENT', 'A1691231069', '2023-08-05 10:24:29.466723', '2023-08-05 10:24:29.466723');

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
) ENGINE = InnoDB AUTO_INCREMENT = 1122 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户认证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_user_auth
-- ----------------------------
INSERT INTO `t_sys_user_auth` VALUES (801, 801, 1, 'admin', '$2a$10$8/eu5o3x5eRDuo0uo4j9Mu/U/Wrqzfn.Tj.zsc4Z3oSpclBr0ti92', 'testkey', 'MGR', 0, 'MLC72VZKZRDOTCTM73TVGIN6F7FZOKPT');
INSERT INTO `t_sys_user_auth` VALUES (1035, 100033, 1, 'test123', '$2a$10$9IEn0gfKOENa1tLxoRWUgehP2rJ/7dOLysyrWtm..oz0GHCjwRUVi', 'f9026c', 'MCH', 0, NULL);
INSERT INTO `t_sys_user_auth` VALUES (1036, 100034, 1, 'atest123', '$2a$10$EBoDVY9Q7MgPLX0ALnvxwOjt9kCXF.bP1Rc1V2Sn/dka76E4lf9P.', '066634', 'AGENT', 0, NULL);

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

SET FOREIGN_KEY_CHECKS = 1;
