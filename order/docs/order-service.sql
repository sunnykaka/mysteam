/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/4/29 17:48:13                           */
/*==============================================================*/


drop table if exists order_coupon;

drop table if exists order_item;

drop table if exists order_table;

drop table if exists product;

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

/*==============================================================*/
/* Table: product                                               */
/*==============================================================*/
create table product
(
   id                   bigint unsigned not null auto_increment,
   name                 national varchar(255) not null,
   description          text,
   price                bigint,
   category             varchar(32),
   primary key (id)
);

