package com.github.gobars.httplog;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class Hostname {
    public static final String HOSTNAME = getHostname();


    public static String getHostname() {
        // IS_OS_WINDOWS ? System.getenv("COMPUTERNAME") : System.getenv("HOSTNAME")
        // https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/SystemUtils.java
        String host = System.getenv("COMPUTERNAME");
        if (host != null) {
            log.info("got hostname {} from env COMPUTERNAME", host);
            return host;
        }

        host = System.getenv("HOSTNAME");
        if (host != null) {
            log.info("got hostname {} from env HOSTNAME", host);
            return host;
        }

        try {
            InputStream is = Runtime.getRuntime().exec("hostname").getInputStream();
            String s = new BufferedReader(new InputStreamReader(is)).readLine();
            log.info("got hostname {} from exec hostname", s);
            return s;
        } catch (Exception ex) {
            log.warn("exec hostname", ex);
        }

        // Maybe very slow
        // [main] WARN org.springframework.boot.StartupInfoLogger -
        // InetAddress.getLocalHost().getHostName() took 5006 milliseconds to respond.
        // Please verify your network configuration (macOS machines may need to add entries to
        // /etc/hosts).

        return "Unknown";
    }
}
