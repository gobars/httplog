package com.github.gobars.httplog;

public interface Const {
  String REQ = "HTTPLOG_REQ";
  String RSP = "HTTPLOG_RSP";
  String PROCESSOR = "HTTPLOG_PROCESSOR";
  String INTERCEPTOR = "HTTPLOG_INTERCEPTOR";
  String CUSTOM = "HTTPLOG_CUSTOM";

  String[] WEB_IGNORES =
      new String[] {
        "/css/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/v2/**",
        "/doc.html",
        "/i18n/**",
        "/error",
        "/**/*.ico",
        "/service-worker.js",
        "/precache-manifest.*.*",
        "/index.html",
        "/druid/**",
        "/webjars/springfox-swagger-ui/**",
        "/webjars/bycdao-ui/**"
  };

}
