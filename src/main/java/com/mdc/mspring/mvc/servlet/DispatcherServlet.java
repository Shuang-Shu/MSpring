package com.mdc.mspring.mvc.servlet;

import com.mdc.mspring.context.anno.Aware;
import com.mdc.mspring.context.entity.ioc.BeanDefinition;
import com.mdc.mspring.context.factory.ConfigurableApplicationContext;
import com.mdc.mspring.context.factory.Context;
import com.mdc.mspring.mvc.anno.Controller;
import com.mdc.mspring.mvc.anno.RequestMapping;
import com.mdc.mspring.mvc.anno.ResponseBody;
import com.mdc.mspring.mvc.anno.RestController;
import com.mdc.mspring.mvc.entity.Dispatcher;
import com.mdc.mspring.mvc.entity.Param;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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
                    if (!baseSb.toString().endsWith("/")) {
                        // 截断最后一个/
                        baseSb.deleteCharAt(baseSb.length() - 1);
                    }
                    // 2 获取方法级别属性：isResponseBody，isVoid，urlPattern，handlerMethod，methodParameters，
                    Method[] methods = bd.getDeclaredClass().getDeclaredMethods();
                    for (Method method : methods) {
                        StringBuilder sb = new StringBuilder();
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            sb.append(method.getAnnotation(RequestMapping.class).value());
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
                        String urlPattern = baseSb.toString() + sb.toString();
                        Dispatcher dispatcher = Dispatcher.builder()
                                .isRest(isRest)
                                .isResponseBody(isResponseBody)
                                .isVoid(isVoid)
                                .urlPattern(Pattern.compile(urlPattern))
                                .controller(context.getBean(bd.getBeanName()))
                                .handlerMethod(method)
                                .methodParameters(Param.parse(method))
                                .build();
                        dispatcherList.add(dispatcher);
                    }
                }
        );
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
        String url = req.getRequestURL().toString();
        for (Dispatcher dispatcher : this.dispatcherList) {
            // 如果匹配到url
            if (dispatcher.getUrlPattern().matcher(url).matches()) {
                // TODO
            }
        }
        super.doGet(req, resp);
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