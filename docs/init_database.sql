
create database if not exists user_service;
use user_service;
source user/docs/user-service.sql;
source common/docs/common.sql;

create database if not exists account_service;
use account_service;
source account/docs/account-service.sql;
source common/docs/common.sql;

create database if not exists coupon_service;
use coupon_service;
source coupon/docs/coupon-service.sql;
source common/docs/common.sql;

create database if not exists order_service;
use order_service;
source order/docs/order-service.sql;
source common/docs/common.sql;

create database if not exists product_service;
use product_service;
source product/docs/product-service.sql;
source product/docs/init-product.sql;
source common/docs/common.sql;