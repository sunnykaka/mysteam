/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/5/16 14:30:42                           */
/*==============================================================*/


drop table if exists order_coupon;

drop table if exists order_item;

drop table if exists order_table;

/*==============================================================*/
/* Table: order_coupon                                          */
/*==============================================================*/
create table order_coupon
(
   id                   bigint unsigned not null auto_increment,
   couponId             bigint not null,
   couponAmount         bigint not null,
   couponCode           varchar(32) default '0',
   orderId              bigint not null,
   primary key (id)
);

/*==============================================================*/
/* Table: order_item                                            */
/*==============================================================*/
create table order_item
(
   id                   bigint unsigned not null auto_increment,
   productId            bigint not null,
   quantity             int not null default 0,
   price                bigint,
   orderId              bigint not null,
   optlock              int default 0,
   primary key (id)
);

/*==============================================================*/
/* Table: order_table                                           */
/*==============================================================*/
create table order_table
(
   id                   bigint unsigned not null auto_increment,
   orderNo              bigint not null comment '¶©µ¥ºÅ',
   totalAmount          bigint default 0,
   couponAmount         bigint default 0,
   payAmount            bigint default 0,
   userId               bigint,
   status               varchar(32),
   optlock              int default 0,
   createTime           datetime,
   updateTime           datetime,
   primary key (id)
);

