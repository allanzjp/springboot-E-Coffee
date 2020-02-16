SET FOREIGN_KEY_CHECKS=0;

DROP database IF EXISTS `example`;
create database example;
use example;

-- 商品信息表
DROP TABLE IF EXISTS `commodity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `commodity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category_id` int(11) DEFAULT NULL COMMENT '商品分类',
  `commodity_name` varchar(50) NOT NULL COMMENT '商品名',
  `price` decimal(12,2) NOT NULL COMMENT '商品单价',
  `stock` int(11) NOT NULL COMMENT '商品库存',
  `label` varchar(50) DEFAULT NULL COMMENT '商品标签',
  `description` varchar(32) DEFAULT '' COMMENT '商品描述',
  `img_url` varchar(256) DEFAULT '' COMMENT '商品图片',
  `state` tinyint(32) DEFAULT 1 COMMENT '当前状态:0.上架，1.下架',
  `remark` varchar(256) DEFAULT '' COMMENT '备注',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
  -- UNIQUE KEY `uk_uid_type` (`uid`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品信息';
/*!40101 SET character_set_client = @saved_cs_client */;


-- 客户信息
DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_name` varchar(50) NOT NULL COMMENT '客户名称',
  `company` varchar(50) DEFAULT NULL COMMENT '客户公司',
  `balance` decimal(12,2) NOT NULL COMMENT '客户余额',
  `mobile` varchar(32) NOT NULL DEFAULT '' COMMENT '客户联系方式,电话',
  `email` varchar(32) DEFAULT '' COMMENT '客户联系方式,邮箱',
  `remark` varchar(256) DEFAULT '' COMMENT '备注',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客户信息';
/*!40101 SET character_set_client = @saved_cs_client */;


-- 订单表
DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_id` int(11) NOT NULL COMMENT '客户id',
  `commodity_id` int(11) NOT NULL COMMENT '商品id',
  `quantity` int(11) NOT NULL COMMENT '订单数量',
  `total` decimal(12,2) NOT NULL COMMENT '订单总额',
  `remark` varchar(256) DEFAULT '' COMMENT '备注',
  `delivery_time` timestamp COMMENT '发货时间',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客户信息';
/*!40101 SET character_set_client = @saved_cs_client */;


-- 用户表
DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `country_code` varchar(8) NOT NULL DEFAULT '' COMMENT '手机号对应的国家编码',
  `mobile_number` varchar(16) NOT NULL DEFAULT '' COMMENT '手机号码',
  `email` varchar(128) NOT NULL DEFAULT '' COMMENT '邮箱',
  `login_pword` varchar(64) NOT NULL DEFAULT '' COMMENT '登录密码',
  `nickname` varchar(256) NOT NULL DEFAULT '' COMMENT '姓名',
  `login_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '用户登录冻结状态：0冻结，1：正常',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8 COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

-- token
DROP TABLE IF EXISTS `token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `token` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户uid',
  `token` varchar(64) NOT NULL COMMENT 'token值',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'token创建时间',
  `mtime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP  COMMENT 'token更新时间，每使用一次都会更新',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

