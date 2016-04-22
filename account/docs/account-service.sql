/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/4/22 15:10:35                           */
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
   primary key (id)
);

