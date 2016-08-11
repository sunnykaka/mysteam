/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/8/10 11:33:27                           */
/*==============================================================*/


drop table if exists user;

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

