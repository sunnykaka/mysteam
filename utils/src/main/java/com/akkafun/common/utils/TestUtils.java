package com.akkafun.common.utils;

import com.akkafun.base.api.Error;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.function.Consumer;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by liubin on 2016/5/5.
 */
public interface TestUtils {


    /**
     * 当使用RestTemplate调用测试接口时, 可能会抛出HttpClientErrorException或HttpClientErrorException
     * 这个方法运行runnable, 将捕获的异常交给consumer执行.
     * 如果runnable没有抛出指定异常, 则抛出AssertionError
     *
     * @param runnable
     * @param consumer
     */
    static void assertServerError(Runnable runnable, Consumer<Error> consumer) throws AssertionError {

        Exception unexpected = null;

        try {
            runnable.run();
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String json = e.getResponseBodyAsString();
            assertThat(json, notNullValue());
            Error error = JsonUtils.json2Object(json, Error.class);
            consumer.accept(error);
            return;
        } catch (Exception e) {
            unexpected = e;
        }
        if(unexpected == null) {
            throw new AssertionError("HttpServerErrorException or HttpClientErrorException was expected here, " +
                    "but nothing happened");
        } else {
            throw new AssertionError("HttpServerErrorException or HttpClientErrorException was expected here, " +
                    "but exception was " + unexpected.toString() );
        }

    }

    static HttpEntity<String> createJsonEntity(Object object) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(JsonUtils.object2Json(object), requestHeaders);
    }

}
