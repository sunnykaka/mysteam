/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/4/19 15:38:28                           */
/*==============================================================*/


drop table if exists account;

drop table if exists account_flow;

drop table if exists event_process;

drop table if exists event_publish;

drop table if exists user;

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

/*==============================================================*/
/* Table: event_process                                         */
/*==============================================================*/
create table event_process
(
   id                   bigint unsigned not null auto_increment,
   status               varchar(100) not null comment 'NEW, PUBLISHED',
   payload              varchar(1024) not null,
   createTime           datetime,
   updateTime           datetime,
   eventId              varchar(128) not null,
   eventType            varchar(64),
   primary key (id),
   unique key AK_UN_event_process (eventId)
);

/*==============================================================*/
/* Table: event_publish                                         */
/*==============================================================*/
create table event_publish
(
   id                   bigint unsigned not null auto_increment,
   status               varchar(100) not null comment 'NEW, PUBLISHED',
   payload              varchar(1024) not null,
   createTime           datetime,
   updateTime           datetime,
   eventId              varchar(128) not null,
   eventType            varchar(64),
   primary key (id),
   unique key AK_UN_event_publish (eventId)
);

/*==============================================================*/
/* Table: user                                                  */
/*==============================================================*/
create table user
(
   id                   bigint unsigned not null auto_increment,
   username             varchar(100) not null,
   password             varchar(255) not null,
   createTime           datetime,
   updateTime           datetime,
   optlock              int default 0,
   primary key (id)
);

