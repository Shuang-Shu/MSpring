package com.mdc.mspring.mvc.view;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
