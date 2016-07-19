package com.akkafun.common.spring.mvc;

import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.api.Error;
import com.akkafun.common.utils.JsonUtils;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by liubin on 2016/5/3.
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class AppErrorController extends AbstractErrorController {

    private final ErrorProperties errorProperties;

    public AppErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes);
        Assert.notNull(errorProperties, "ErrorProperties must not be null");
        this.errorProperties = errorProperties;
    }

    @Override
    public String getErrorPath() {
        return this.errorProperties.getPath();
    }

    @RequestMapping(produces = "text/html")
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {

        HttpStatus status = getStatus(request);
        CommonErrorCode errorCode = CommonErrorCode.fromHttpStatus(status.value());
        Error error = new Error(errorCode.getCode(), request.getRequestURI(), status.getReasonPhrase());
        ModelAndView mav = new ModelAndView();
        MappingJackson2JsonView view = new MappingJackson2JsonView(JsonUtils.OBJECT_MAPPER);
        view.setAttributesMap(JsonUtils.object2Map(error));
        mav.setView(view);
        return mav;

    }


    @RequestMapping
    @ResponseBody
    public ResponseEntity<String> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        CommonErrorCode errorCode = CommonErrorCode.fromHttpStatus(status.value());
        Error error = new Error(errorCode.getCode(), request.getRequestURI(), status.getReasonPhrase());
        return new ResponseEntity<>(JsonUtils.object2Json(error), status);
    }

}

