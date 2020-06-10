drop table biz_log;

create table biz_log
(
    id          number(19) primary key,
    created     timestamp default systimestamp,
    "START"     timestamp,
    end         timestamp,
    cost        number(10),
    ip          varchar2(60),
    hostname    varchar2(60),
    pid         number(10),
    biz         varchar2(60),
    req_path_id varchar2(60),
    req_url     varchar2(60),
    req_heads   varchar2(600),
    req_method  varchar2(60),
    rsp_body    varchar2(60),
    bizdesc     varchar2(60)
)
;

comment on table biz_log is 'biz_log';

comment on column biz_log.bizdesc is '响应体 httplog:"fix_desc"';

drop table biz_log_post;

create table biz_log_post
(
    id          number(19) primary key,
    created     timestamp default systimestamp,
    "START"     timestamp,
    end         timestamp,
    cost        number(10),
    ip          varchar2(60),
    hostname    varchar2(60),
    pid         number(10),
    biz         varchar2(60),
    req_path_id varchar2(60),
    req_url     varchar2(60),
    req_heads   varchar2(600),
    req_method  varchar2(60),
    exception   clob,
    rsp         varchar2(60),
    dtoid       varchar2(60)
)
;

comment on table biz_log_post is 'biz_log_post';
comment on column biz_log_post.rsp is '响应体 httplog:"rsp_body"';
comment on column biz_log_post.dtoid is '响应体 httplog:"rsp_json_id"';
/
