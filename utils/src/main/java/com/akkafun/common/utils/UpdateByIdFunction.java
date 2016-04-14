package com.akkafun.common.utils;

/**
 * Created by liubin on 2016/4/13.
 */
public interface UpdateByIdFunction {

    int execute(Long[] ids, Object... args);

}
