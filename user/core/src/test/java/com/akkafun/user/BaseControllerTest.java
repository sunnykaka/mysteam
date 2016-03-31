package com.akkafun.user;

import com.akkafun.BaseTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Created by liubin on 2016/3/30.
 */
@WebIntegrationTest(randomPort = true)
public abstract class BaseControllerTest extends BaseTest {

    @Value("${local.server.port}")
    protected int serverPort;

    protected String localServerUrl(){
        return String.format("http://localhost:%d", serverPort);
    }

    protected String buildRequestUrl(String url) {
        return String.format("%s/%s", localServerUrl(), url);
    }

    protected HttpEntity<String> createJsonEntity(String json) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, requestHeaders);
    }

}
