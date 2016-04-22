/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/4/22 15:14:33                           */
/*==============================================================*/


drop table if exists event_process;

drop table if exists event_publish;

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

