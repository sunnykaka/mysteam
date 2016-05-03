package com.akkafun.common.spring.mvc;

import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.api.Error;
import com.akkafun.base.api.ErrorCode;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一异常处理
 */
@ControllerAdvice
public class AppExceptionHandlerController {

    protected Logger logger = LoggerFactory.getLogger(AppExceptionHandlerController.class);


    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    public ResponseEntity<String> handleError(HttpServletRequest request, Exception e) {

        ErrorCode errorCode = CommonErrorCode.InternalError;
        String errorMessage = errorCode.getMessage();
        if(e instanceof AppBusinessException) {
            //业务异常
            errorMessage = e.getMessage();
            errorCode = ((AppBusinessException)e).getErrorCode();
            logger.debug(e.getMessage());
        } else {
            logger.error("服务器发生错误: " + e.getMessage(), e);
        }

        Error error = new Error(errorCode, request.getRequestURI(), errorMessage);
        String json = JsonUtils.object2Json(error);
        return ResponseEntity.status(HttpStatus.valueOf(errorCode.getStatus())).body(json);
    }
}