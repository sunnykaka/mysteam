package com.akkafun.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liubin on 15/4/5.
 */
public class SQLUtils {

    /**
     * hql语句如果用了fetch,删除查询总条数的fetch语句,否则会报错
     * @param countQL
     * @return
     */
    public static String removeFetchInCountQl(String countQL) {
        if(StringUtils.contains(countQL, " fetch ")) {
            countQL = (countQL.toString().replaceAll("fetch", ""));
        }
        return countQL;
    }

    public static boolean hasGroupBy(String ql) {
        if(ql != null && !"".equals(ql)){
            if(ql.indexOf("group by") > -1){
                return true;
            }
        }
        return false;
    }

    /**
     * 去除ql语句中的select子句
     * @param ql 查询语句
     * @return 删除后的语句
     */
    public static String removeSelect(String ql) {
        Assert.hasText(ql);
        int beginPos = ql.toLowerCase().indexOf("from");
        Assert.isTrue(beginPos != -1, " ql : " + ql + " must has a keyword 'from'");
        return ql.substring(beginPos);
    }

    // 删除order by字句使用的正则表达式
    public static Pattern removeOrderByPattern = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);

    /**
     * 删除ql语句中的order by字句
     * @param ql 查询语句
     * @return 删除后的查询语句
     */
    public static String removeOrderBy(String ql){
        if(ql != null && !"".equals(ql)){
            Matcher m = removeOrderByPattern.matcher(ql);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, "");
            }
            m.appendTail(sb);
            return sb.toString();
        }
        return "";
    }

    /**
     * 批量对id数组进行操作, 默认一次操作次数不大于50.
     * 主要是针对where id in(...) 这种语句
     * @param ids
     * @param function
     * @return
     */
    public static int updateByIdBatch(Long[] ids, Function<Long[], Integer> function) {
        if(ids == null || ids.length == 0) return 0;
        //一次执行sql更新的最大数量不超过50
        int maxLength = 50;
        if(ids.length <= maxLength) {
            return function.apply(ids);
        }
        int count = 0;
        for(int i=0; i<=(ids.length / maxLength); i++) {
            int start = i * maxLength;
            int end = Math.min(start + maxLength, ids.length);
            if(start == end) continue;
            count += function.apply(Arrays.copyOfRange(ids, start, end));
        }
        return count;
    }

}
