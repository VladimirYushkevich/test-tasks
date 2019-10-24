package com.yushkevich.watermark.client;

import com.yushkevich.watermark.Application;
import org.junit.Test;

public class ApplicationTest {

    @Test
    public void testWithArgs() {
        Application.main(new String[]{"myarg"});
    }
}
