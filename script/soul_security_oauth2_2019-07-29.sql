# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.25)
# Database: soul_security_oauth2
# Generation Time: 2019-07-29 01:28:10 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table app_auth
# ------------------------------------------------------------

DROP TABLE IF EXISTS `app_auth`;

CREATE TABLE `app_auth` (
  `id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `app_key` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应用标识key',
  `app_secret` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密算法secret',
  `enabled` tinyint(4) NOT NULL COMMENT '是否删除',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



# Dump of table dashboard_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dashboard_user`;

CREATE TABLE `dashboard_user` (
  `id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `user_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户密码',
  `role` int(4) NOT NULL COMMENT '角色',
  `enabled` tinyint(4) NOT NULL COMMENT '是否删除',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `dashboard_user` WRITE;
/*!40000 ALTER TABLE `dashboard_user` DISABLE KEYS */;

INSERT INTO `dashboard_user` (`id`, `user_name`, `password`, `role`, `enabled`, `date_created`, `date_updated`)
VALUES
	('1','admin','123456',1,1,'2018-06-23 15:12:22','2018-06-23 15:12:23');

/*!40000 ALTER TABLE `dashboard_user` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table plugin
# ------------------------------------------------------------

DROP TABLE IF EXISTS `plugin`;

CREATE TABLE `plugin` (
  `id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `name` varchar(62) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '插件名称',
  `config` text COLLATE utf8mb4_unicode_ci COMMENT '插件配置',
  `role` int(4) NOT NULL COMMENT '插件角色',
  `enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启（0，未开启，1开启）',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

LOCK TABLES `plugin` WRITE;
/*!40000 ALTER TABLE `plugin` DISABLE KEYS */;

INSERT INTO `plugin` (`id`, `name`, `config`, `role`, `enabled`, `date_created`, `date_updated`)
VALUES
	('1','sign',NULL,0,0,'2018-06-14 10:17:35','2018-06-14 10:17:35'),
	('2','waf',NULL,0,0,'2018-06-23 10:26:30','2018-06-13 15:43:10'),
	('3','rewrite',NULL,0,0,'2018-06-23 10:26:34','2018-06-25 13:59:31'),
	('4','rate_limiter','{\"master\":\"mymaster\",\"mode\":\"Standalone\",\"url\":\"192.168.1.1:6379\",\"password\":\"abc\"}',0,0,'2018-06-23 10:26:37','2018-06-13 15:34:48'),
	('5','divide',NULL,0,1,'2018-06-25 10:19:10','2018-06-13 13:56:04'),
	('6','dubbo',NULL,0,1,'2018-06-23 10:26:41','2018-06-11 10:11:47'),
	('7','monitor','{\"userName\":\"xiaoyu\",\"database\":\"databases\",\"url\":\"http://localhost:8086\",\"password\":\"test222\"}',0,0,'2018-06-25 13:47:57','2018-06-25 13:47:57'),
	('8','springCloud',NULL,0,1,'2018-06-25 13:47:57','2018-06-25 13:47:57');

/*!40000 ALTER TABLE `plugin` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table rule
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rule`;

CREATE TABLE `rule` (
  `id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `selector_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '选择器id',
  `match_mode` int(2) NOT NULL COMMENT '匹配方式（0 and  1 or)',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `enabled` tinyint(4) NOT NULL COMMENT '是否开启',
  `loged` tinyint(4) NOT NULL COMMENT '是否记录日志',
  `sort` int(4) NOT NULL COMMENT '排序',
  `handle` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理逻辑（此处针对不同的插件，会有不同的字段来标识不同的处理，所有存储json格式数据）',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



# Dump of table rule_condition
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rule_condition`;

CREATE TABLE `rule_condition` (
  `id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `rule_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则id',
  `param_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数类型（post  query  uri等）',
  `operator` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '匹配符（=  > <  like match）',
  `param_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名称',
  `param_value` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数值',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



# Dump of table selector
# ------------------------------------------------------------

DROP TABLE IF EXISTS `selector`;

CREATE TABLE `selector` (
  `id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id varchar',
  `plugin_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '插件id',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '选择器名称',
  `match_mode` int(2) NOT NULL COMMENT '匹配方式（0 and  1 or)',
  `type` int(4) NOT NULL COMMENT '类型（0，全流量，1自定义流量）',
  `sort` int(4) NOT NULL COMMENT '排序',
  `handle` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理逻辑（此处针对不同的插件，会有不同的字段来标识不同的处理，所有存储json格式数据）',
  `enabled` tinyint(4) NOT NULL COMMENT '是否开启',
  `loged` tinyint(4) NOT NULL COMMENT '是否打印日志',
  `continued` tinyint(4) NOT NULL COMMENT '是否继续执行',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



# Dump of table selector_condition
# ------------------------------------------------------------

DROP TABLE IF EXISTS `selector_condition`;

CREATE TABLE `selector_condition` (
  `id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `selector_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '选择器id',
  `param_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数类型（post  query  uri等）',
  `operator` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '匹配符（=  > <  like match）',
  `param_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名称',
  `param_value` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数值',
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
