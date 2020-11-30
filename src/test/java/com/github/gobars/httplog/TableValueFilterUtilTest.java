package com.github.gobars.httplog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TableValueFilterUtilTest {
    @Test
    public void testFilter() throws Exception {
        assertEquals("abc=___",TableValueFilterUtil.filter("xxx", null, "abc=1233"));
    }
}
