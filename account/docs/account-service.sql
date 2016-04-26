/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/4/26 9:54:25                            */
/*==============================================================*/


drop table if exists account;

drop table if exists account_flow;

/*==============================================================*/
/* Table: account                                               */
/*==============================================================*/
create table account
(
   id                   bigint unsigned not null auto_increment,
   balance              bigint not null,
   userId               bigint not null,
   optlock              int default 0,
   createTime           datetime,
   updateTime           datetime,
   primary key (id)
);

/*==============================================================*/
/* Table: account_flow                                          */
/*==============================================================*/
create table account_flow
(
   id                   bigint unsigned not null auto_increment,
   balance              bigint not null,
   accountId            bigint not null,
   description          varchar(255),
   type                 varchar(32),
   createTime           datetime,
   updateTime           datetime,
   primary key (id)
);

