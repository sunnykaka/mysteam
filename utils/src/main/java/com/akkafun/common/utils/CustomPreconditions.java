package com.akkafun.common.utils;

import com.akkafun.base.Constants;
import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;

/**
 * Created by liubin on 2016/6/23.
 */
public class CustomPreconditions {

    public static void assertNotGreaterThanMaxQueryBatchSize(int size) {
        if(size > Constants.MAX_BATCH_QUERY_SIZE) throw new AppBusinessException(CommonErrorCode.BAD_REQUEST,
                "一次查询的id数量不能超过" + Constants.MAX_BATCH_QUERY_SIZE);

    }


}
