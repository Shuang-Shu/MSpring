package com.mdc.mspring.mvc.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/17:27
 * @Description:
 */
public interface ViewResolver {
    void init();

    void render(String viewName, Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
