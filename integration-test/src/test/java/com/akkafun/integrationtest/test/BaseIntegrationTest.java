package com.akkafun.integrationtest.test;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by liubin on 2016/3/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestApplication.class)
public abstract class BaseIntegrationTest {

    protected Logger logger = LoggerFactory.getLogger(BaseIntegrationTest.class);

    protected void waitForEventProcessed() {
        try {
            Thread.sleep(20000L);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }


}
