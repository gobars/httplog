package com.github.gobars.httplog;


import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.management.ManagementFactory;

@Slf4j
@UtilityClass
public class Pid {
    public final int PROCESS_ID = getProcessId(0);

    private int getProcessId(int fallback) {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        val jvmName = ManagementFactory.getRuntimeMXBean().getName();
        val index = jvmName.indexOf('@');

        if (index < 1) {
            // part before '@' empty (index = 0) / '@' not found (index = -1)
            return fallback;
        }

        try {
            return Integer.parseInt(jvmName.substring(0, index));
        } catch (NumberFormatException e) {
            // ignore
        }

        return fallback;
    }

    public boolean isStillAlive(int pid) {
        String os = System.getProperty("os.name").toLowerCase();
        String command;
        if (os.contains("win")) {
            log.debug("Check alive Windows mode. Pid: [{}]", pid);
            command = "cmd /c tasklist /FI \"PID eq " + pid + "\"";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            log.debug("Check alive Linux/Unix mode. Pid: [{}]", pid);
            command = "ps -p " + pid;
        } else {
            log.warn("Unsupported OS: Check alive for Pid: [{}] return false", pid);
            return false;
        }

        // call generic implementation
        return isProcessIdRunning(pid, command);
    }

    private boolean isProcessIdRunning(int pid, String command) {
        log.debug("exec command {}", command);
        String result = Util.exec(command);
        return result.contains(" " + pid + " ");
    }
}