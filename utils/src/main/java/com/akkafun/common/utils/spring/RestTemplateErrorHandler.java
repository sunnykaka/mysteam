package com.akkafun.common.utils.spring;

import com.akkafun.base.api.Error;
import com.akkafun.base.exception.RemoteCallException;
import com.akkafun.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by liubin on 2016/7/18.
 */
public class RestTemplateErrorHandler extends DefaultResponseErrorHandler {

    protected Logger logger = LoggerFactory.getLogger(RestTemplateErrorHandler.class);

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        Charset charset = getCharset(response);
        byte[] responseBody = getResponseBody(response);
        if(responseBody == null || responseBody.length == 0) {
            super.handleError(response);
            return;
        }

        String responseText = new String(charset.decode(ByteBuffer.wrap(responseBody)).array());
        if(StringUtils.isBlank(responseText)) {
            return;
        }

        Error error = null;
        try {
            error = JsonUtils.json2Object(responseText, Error.class);
        } catch (Exception ignore) {
            super.handleError(response);
        }

        if(error != null) {
            throw new RemoteCallException(error, getHttpStatusCode(response).value());
        }
    }

    private byte[] getResponseBody(ClientHttpResponse response) {
        try {
            InputStream responseBody = response.getBody();
            if (responseBody != null) {
                return FileCopyUtils.copyToByteArray(responseBody);
            }
        }
        catch (IOException ex) {
            // ignore
        }
        return new byte[0];
    }


    private Charset getCharset(ClientHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        MediaType contentType = headers.getContentType();
        return contentType != null ? contentType.getCharSet() : Charset.forName("UTF-8");
    }

    private HttpStatus getHttpStatusCode(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode;
        try {
            statusCode = response.getStatusCode();
        }
        catch (IllegalArgumentException ex) {
            logger.warn("", ex);
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return statusCode;
    }

}
