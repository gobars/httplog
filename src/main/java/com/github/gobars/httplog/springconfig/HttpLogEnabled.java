package com.github.gobars.httplog.springconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 自动激活HttpLog的相关Spring配置.
 *
 * @author bingoobjca
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(HttpLogSpringConfig.class)
public @interface HttpLogEnabled {}
