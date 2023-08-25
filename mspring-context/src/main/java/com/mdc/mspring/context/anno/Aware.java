package com.mdc.mspring.context.anno;


import com.mdc.mspring.context.factory.Context;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/14:32
 * @Description:
 */
public interface Aware {
    public void setContext(Context context);
}
