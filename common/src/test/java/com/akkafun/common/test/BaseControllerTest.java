package com.akkafun.common.test;

import com.akkafun.common.spring.WebApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Created by liubin on 2016/3/30.
 */
@WebIntegrationTest(randomPort = true)
@SpringApplicationConfiguration(classes = WebApplication.class)
public abstract class BaseControllerTest extends BaseTest {

    @Value("${local.server.port}")
    protected int serverPort;

    protected String localServerUrl(){
        return String.format("http://localhost:%d", serverPort);
    }

    protected String buildRequestUrl(String url) {
        return String.format("%s/%s", localServerUrl(), url);
    }

}
