/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2016/5/16 14:30:29                           */
/*==============================================================*/


drop table if exists product;

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

