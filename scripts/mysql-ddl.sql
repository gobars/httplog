create database id;
use id;

drop table if exists biz_log;
create table biz_log
(
    id          bigint auto_increment primary key comment '日志记录ID',
    created     datetime default current_timestamp comment '创建时间',
    started     datetime comment '请求时间',
    end         datetime comment '结束时间',
    cost        int comment '费时毫秒',
    ip          varchar(60) comment '当前机器IP',
    hostname    varchar(60) comment '当前机器名称',
    pid         int comment '应用程序PID',
    biz         varchar(60) comment '当前业务名称',
    req_path_id varchar(60) comment '请求路径变量id',
    req_url     varchar(60) comment '请求url',
    req_heads   varchar(600) comment '请求头',
    req_method  varchar(60) comment '请求方法',
    rsp_body    varchar(150) not null default '0' comment '响应体',
    pre_hi      varchar(60) comment 'hi',
    post_bye    varchar(60) comment 'bye',
    bizdesc     varchar(60) comment '响应体 httplog:"fix_desc"'
) engine = innodb
  default charset = utf8mb4 comment 'biz_log';

drop table if exists biz_log_post;
create table biz_log_post
(
    id          bigint primary key comment '日志记录ID',
    created     datetime default current_timestamp comment '创建时间',
    started     datetime comment '请求时间',
    end         datetime comment '结束时间',
    cost        int comment '费时毫秒',
    ip          varchar(60) comment '当前机器IP',
    hostname    varchar(60) comment '当前机器名称',
    pid         int comment '应用程序PID',
    biz         varchar(60) comment '当前业务名称',
    req_path_id varchar(60) comment '请求路径变量id',
    req_url     varchar(60) comment '请求url',
    req_heads   varchar(600) comment '请求头',
    req_method  varchar(60) comment '请求方法',
    exception   text comment '异常',
    rsp         varchar(60) comment '响应体 httplog:"rsp_body"',
    dtoid       varchar(60) comment '响应体 httplog:"rsp_json_id"'
) engine = innodb
  default charset = utf8mb4 comment 'biz_log_post';

drop table if exists biz_log_custom;
create table biz_log_custom
(
    id      bigint primary key comment '日志记录ID',
    req_url varchar(60) comment '请求url',
    created datetime default current_timestamp comment '创建时间',
    started datetime comment '请求时间',
    end     datetime comment '结束时间',
    cost    int comment '费时毫秒',
    name    varchar(60) comment '响应体 httplog:"custom_name"'
) engine = innodb
  default charset = utf8mb4 comment 'biz_log_custom';