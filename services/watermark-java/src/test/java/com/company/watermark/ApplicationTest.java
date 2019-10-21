package com.company.watermark;

import org.junit.Test;

public class ApplicationTest {

    private Application application = new Application();

    @Test
    public void testWithArgs() throws Exception {
        application.main(new String[]{"myarg"});
    }
}
