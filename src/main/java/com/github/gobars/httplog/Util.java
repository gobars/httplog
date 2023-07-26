package com.github.gobars.httplog;


import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Scanner;

@Slf4j
@UtilityClass
public class Util {

    @SneakyThrows
    public String exec(String execCommand) {
        val proc = Runtime.getRuntime().exec(execCommand);
        @Cleanup val stream = proc.getInputStream();
        @Cleanup val scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}