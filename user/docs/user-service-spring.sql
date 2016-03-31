/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/3/29 10:28:37                           */
/*==============================================================*/


drop table if exists user;

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

