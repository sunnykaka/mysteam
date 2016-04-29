/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/4/29 15:24:21                           */
/*==============================================================*/


drop table if exists coupon;

/*==============================================================*/
/* Table: coupon                                                */
/*==============================================================*/
create table coupon
(
   id                   bigint unsigned not null auto_increment,
   amount               bigint not null,
   userId               bigint not null,
   state                varchar(32) not null,
   orderId              bigint,
   useTime              datetime,
   code                 varchar(32),
   optlock              int default 0,
   createTime           datetime,
   updateTime           datetime,
   primary key (id)
);

