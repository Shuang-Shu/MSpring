package com.mdc.mspring.mvc.servlet;

import com.mdc.mspring.context.anno.Aware;
import com.mdc.mspring.context.entity.ioc.BeanDefinition;
import com.mdc.mspring.context.factory.ConfigurableApplicationContext;
import com.mdc.mspring.context.factory.Context;
import com.mdc.mspring.mvc.anno.*;
import com.mdc.mspring.mvc.entity.Dispatcher;
import com.mdc.mspring.mvc.entity.Param;
import com.mdc.mspring.mvc.utils.JsonUtils;
import com.mdc.mspring.mvc.utils.RegUtils;

import com.mdc.mspring.mvc.utils.StringUtils;
import com.mdc.mspring.mvc.utils.UrlUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/18:43
 * @Description:
 */
public class DispatcherServlet extends HttpServlet implements Aware {
    final List<Dispatcher> dispatcherList = new ArrayList<>();

    private ConfigurableApplicationContext context;

    public DispatcherServlet() {
        super();
        initDispatchers();
    }

    public DispatcherServlet(ConfigurableApplicationContext context) {
        super();
        this.context = context;
        initDispatchers();
    }

    private void initDispatchers() {
        List<BeanDefinition> controllerDefinitions = context.getBeanDefinitionsByAnnotation(Controller.class);
        controllerDefinitions.stream().forEach(
                bd -> {
                    // TODO
                    // 1 获取controller属性：isRest，baseUrlPattern，controller
                    boolean isRest = bd.getDeclaredClass().isAnnotationPresent(RestController.class);
                    StringBuilder baseSb = new StringBuilder();
                    if (bd.getDeclaredClass().getAnnotation(RequestMapping.class) != null) {
                        baseSb.append(bd.getDeclaredClass().getAnnotation(RequestMapping.class).value());
                    }
                    if (baseSb.toString().endsWith("/")) {
                        // 截断最后一个/
                        baseSb.deleteCharAt(baseSb.length() - 1);
                    }
                    // 2 获取方法级别属性：isResponseBody，isVoid，urlPattern，handlerMethod，methodParameters，
                    Method[] methods = bd.getDeclaredClass().getDeclaredMethods();
                    for (Method method : methods) {
                        StringBuilder sb = new StringBuilder();
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            sb.append(method.getAnnotation(RequestMapping.class).value());
                        } else if (method.isAnnotationPresent(GetMapping.class)) {
                            sb.append(method.getAnnotation(GetMapping.class).value());
                        } else if (method.isAnnotationPresent(PostMapping.class)) {
                            sb.append(method.getAnnotation(PostMapping.class).value());
                        } else {
                            continue;
                        }
                        boolean isResponseBody = method.isAnnotationPresent(ResponseBody.class);
                        boolean isVoid = method.getReturnType().equals(Void.TYPE);
                        if (!sb.toString().startsWith("/")) {
                            sb.insert(0, "/");
                        }
                        if (sb.toString().endsWith("/")) {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                        Param[] params = Param.parse(method);
                        String urlPattern = baseSb.toString() + sb.toString();
                        Dispatcher dispatcher = Dispatcher.builder()
                                .isRest(isRest)
                                .isResponseBody(isResponseBody)
                                .isVoid(isVoid)
                                .urlPatternStr(urlPattern)
                                .urlPattern(Pattern.compile(RegUtils.formatPatternString(urlPattern)))
                                .controller(context.getBean(bd.getBeanName()))
                                .handlerMethod(method)
                                .methodParameters(params)
                                .build();
                        dispatcherList.add(dispatcher);
                    }
                });
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 判断req类型
        if (req.getMethod().equals("GET")) {
            doGet(req, resp);
        } else if (req.getMethod().equals("POST")) {
            doPost(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = UrlUtils.getRelativeUrl(req.getRequestURL().toString());
        for (Dispatcher dispatcher : this.dispatcherList) {
            // 如果匹配到url
            if (dispatcher.getUrlPattern().matcher(url).matches()) {
                // 解析所有参数
                Map<String, Object> allParams = parseAllParams(dispatcher, req, url);
                // TODO 获取Param数组
                Object[] params = parseParams(allParams, dispatcher.getMethodParameters());
                // TODO 调用方法
                Object result = null;
                try {
                    result = dispatcher.getHandlerMethod().invoke(dispatcher.getController(), params);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    resp.sendError(500, "server error");
                    throw new RuntimeException(e);
                }
                // TODO 处理返回值（目前仅支持ResponseBody）
                resp.getWriter().write((String) result);
                return;
            }
        }
        throw new ServletException("no matching handler");
    }

    private Object[] parseParams(Map<String, Object> allParams, Param[] paramDefs) {
        Object[] result = new Object[paramDefs.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = StringUtils.parseStr((String) allParams.get(paramDefs[i].getName()), paramDefs[i].getClassType());
        }
        return result;
    }

    private Map<String, Object> parseAllParams(Dispatcher dispatcher, HttpServletRequest req, String url)
            throws IOException {
        Map<String, Object> allParams = new HashMap<>();
        // 1 获取header、body和url中的所有参数
        // 1.1 获取url中的所有参数
        allParams.putAll(RegUtils.parse(dispatcher.getUrlPatternStr(), url));
        // 1.2 获取header中的所有参数
        var headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            allParams.put(headerName, req.getHeader(headerName));
        }
        // 1.3 解析body中的所有参数
        if (req.getMethod().equals("POST")) {
            // 以Json串形式获取body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            while (reader.readLine() != null) {
                sb.append(reader.readLine());
            }
            allParams.putAll(JsonUtils.parseMap(sb.toString()));
        }
        return allParams;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void setContext(Context context) {
        this.context = (ConfigurableApplicationContext) context;
    }
}