package com.akkafun.common.utils.spring;

import com.akkafun.base.exception.ServiceUnavailableException;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.*;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

/**
 * Created by liubin on 2016/7/19.
 */
public class CustomRestTemplate extends RestTemplate {

    public CustomRestTemplate() {
        super();
    }

    public CustomRestTemplate(ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
    }

    public CustomRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
                              ResponseExtractor<T> responseExtractor) throws RestClientException {
        try {
            return super.doExecute(url, method, requestCallback, responseExtractor);
        } catch (ResourceAccessException e) {

            //目标服务请求失败
            throw new ServiceUnavailableException(e.getMessage());

        } catch (IllegalStateException e) {

            if(e.getMessage().contains("No instances")) {
                //目标服务没启动的时候RibbonLoadBalancerClient.execute会抛出IllegalStateException
                //转化成ServiceUnavailableException
                throw new ServiceUnavailableException(e.getMessage());
            } else {
                throw e;
            }
        }
    }


    public static CustomRestTemplate assembleRestTemplate(
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {

        CustomRestTemplate restTemplate = new CustomRestTemplate();
        //自定义异常处理
        restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for(Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator(); iterator.hasNext();) {
            HttpMessageConverter<?> converter = iterator.next();
            if(converter instanceof MappingJackson2HttpMessageConverter) {
                iterator.remove();
            }
        }
        messageConverters.add(mappingJackson2HttpMessageConverter);
        return restTemplate;
    }
}
