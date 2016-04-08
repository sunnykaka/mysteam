/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/4/8 14:07:39                            */
/*==============================================================*/


drop table if exists event_process;

drop table if exists event_publish;

drop table if exists user;

/*==============================================================*/
/* Table: event_process                                         */
/*==============================================================*/
create table event_process
(
   id                   bigint unsigned not null auto_increment,
   status               national varchar(100) not null comment 'NEW, PUBLISHED',
   payload              national varchar(1024) not null,
   createTime           datetime,
   updateTime           datetime,
   primary key (id)
);

/*==============================================================*/
/* Table: event_publish                                         */
/*==============================================================*/
create table event_publish
(
   id                   bigint unsigned not null auto_increment,
   status               national varchar(100) not null comment 'NEW, PUBLISHED',
   payload              national varchar(1024) not null,
   createTime           datetime,
   updateTime           datetime,
   primary key (id)
);

/*==============================================================*/
/* Table: user                                                  */
/*==============================================================*/
create table user
(
   id                   bigint unsigned not null auto_increment,
   username             national varchar(100) not null,
   password             national varchar(255) not null,
   createTime           datetime,
   updateTime           datetime,
   optlock              int default 0,
   primary key (id)
);

