/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/7/1 15:51:46                            */
/*==============================================================*/


drop table if exists event_process;

drop table if exists event_publish;

drop table if exists event_watch;

drop table if exists event_watch_process;

/*==============================================================*/
/* Table: event_process                                         */
/*==============================================================*/
create table event_process
(
   id                   bigint unsigned not null auto_increment,
   status               varchar(100) not null comment 'NEW, PROCESSED, IGNORE',
   eventCategory        varchar(100) not null comment 'NOTIFY, ASK, REVOKE, ASKRESP',
   payload              varchar(1024) not null,
   eventId              bigint not null,
   eventType            varchar(64) not null,
   createTime           datetime not null,
   updateTime           datetime,
   optlock              int default 0,
   primary key (id),
   unique key AK_UN_event_process (eventId)
);

/*==============================================================*/
/* Table: event_publish                                         */
/*==============================================================*/
create table event_publish
(
   id                   bigint unsigned not null auto_increment,
   status               varchar(100) not null comment 'NEW, PROCESSED, IGNORE',
   eventCategory        varchar(100) not null comment 'NOTIFY, ASK, REVOKE, ASKRESP',
   payload              varchar(1024) not null,
   eventId              bigint not null,
   eventType            varchar(64) not null,
   createTime           datetime not null,
   updateTime           datetime,
   optlock              int default 0,
   askEventStatus       varchar(100) comment 'PENDING, TIMEOUT, FAILED, SUCCESS, CANCELLED',
   watchId              bigint,
   success              boolean,
   askEventId           bigint,
   primary key (id),
   unique key AK_UN_event_publish (eventId)
);

/*==============================================================*/
/* Table: event_watch                                           */
/*==============================================================*/
create table event_watch
(
   id                   bigint unsigned not null auto_increment,
   askEventIds          varchar(512) not null,
   askEventStatus       varchar(100) not null comment 'PENDING, TIMEOUT, FAILED, SUCCESS, CANCELLED',
   extraParams          varchar(1024),
   createTime           datetime not null,
   updateTime           datetime,
   optlock              int default 0,
   callbackClass        varchar(128),
   united               boolean default false,
   timeoutTime          datetime,
   primary key (id)
);

/*==============================================================*/
/* Table: event_watch_process                                   */
/*==============================================================*/
create table event_watch_process
(
   id                   bigint unsigned not null auto_increment,
   status               varchar(100) not null comment 'NEW, PROCESSED, IGNORE',
   failureInfo          varchar(1024),
   watchId              bigint not null,
   createTime           datetime not null,
   updateTime           datetime,
   primary key (id)
);

