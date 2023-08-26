package com.mdc.mspring.mvc.servlet;

import com.mdc.mspring.context.anno.Aware;
import com.mdc.mspring.context.entity.ioc.BeanDefinition;
import com.mdc.mspring.context.factory.ConfigurableApplicationContext;
import com.mdc.mspring.context.factory.Context;
import com.mdc.mspring.mvc.anno.*;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;
import com.mdc.mspring.mvc.entity.Dispatcher;
import com.mdc.mspring.mvc.entity.ModelAndView;
import com.mdc.mspring.mvc.entity.Param;
import com.mdc.mspring.mvc.exception.ServletParamParseException;
import com.mdc.mspring.mvc.utils.JsonUtils;
import com.mdc.mspring.mvc.utils.RegUtils;
import com.mdc.mspring.mvc.utils.StringUtils;
import com.mdc.mspring.mvc.utils.UrlUtils;
import com.mdc.mspring.mvc.view.ViewResolver;
import com.mdc.mspring.mvc.view.impl.FreeMarkerViewResolver;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
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
    private String resourcePath;
    private ViewResolver viewResolver;

    public DispatcherServlet() {
        super();
        init();
    }

    public DispatcherServlet(ConfigurableApplicationContext context) {
        super();
        this.resourcePath = context.getResourceResolver().getProperty("${mspring.web.static-path:/static/}");
        this.context = context;
        init();
    }

    private void initViewResolver() {
        this.viewResolver = new FreeMarkerViewResolver(WebMvcConfiguration.getServletContext(), context.getResourceResolver().getProperty("${mspring.web.web.template-path:/templates/}"), "UTF-8");
    }

    @Override
    public void init() {
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
                        if (method.isAnnotationPresent(GetMapping.class)) {
                            sb.append(method.getAnnotation(GetMapping.class).value());
                        } else if (method.isAnnotationPresent(PostMapping.class)) {
                            sb.append(method.getAnnotation(PostMapping.class).value());
                        } else {
                            continue;
                        }
                        boolean isResponseBody = method.isAnnotationPresent(ResponseBody.class) || bd.getDeclaredClass().isAnnotationPresent(ResponseBody.class);
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
        initViewResolver();
    }

    @Override
    public void destroy() {
        super.destroy();
        this.context.close();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 判断req类型
        if (req.getMethod().equals("GET")) {
            if (req.getRequestURI().startsWith(resourcePath)) {
                doResource(req.getRequestURI(), req, resp);
                return;
            } else {
                doGet(req, resp);
            }
        } else if (req.getMethod().equals("POST")) {
            doPost(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp);
    }

    // 用于获取资源
    private void doResource(String url, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletContext ctx = req.getServletContext();
        try (InputStream input = ctx.getResourceAsStream(url)) {
            if (input == null) {
                resp.sendError(404, "Not Found");
            } else {
                int n = url.indexOf("/");
                if (n > 0) {
                    url = url.substring(n + 1);
                }
                String mime = ctx.getMimeType(url);
                if (com.mdc.mspring.context.utils.StringUtils.isEmpty(mime)) {
                    mime = "application/octet-stream";
                }
                resp.setContentType(mime);
                input.transferTo(resp.getOutputStream());
                resp.getOutputStream().flush();
            }
        }
    }

    private void doService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = UrlUtils.getRelativeUrl(req.getRequestURL().toString());
        for (Dispatcher dispatcher : this.dispatcherList) {
            // 如果匹配到url
            if (dispatcher.getUrlPattern().matcher(url).matches()) {
                // 解析所有参数
                Object[] params = null;
                try {
                    params = parseParams(dispatcher.getMethodParameters(), dispatcher, req, resp);
                } catch (ServletParamParseException e) {
                    resp.sendError(400, e.getMessage());
                    return;
                }
                Object result = null;
                try {
                    dispatcher.getHandlerMethod().setAccessible(true);
                    result = dispatcher.getHandlerMethod().invoke(dispatcher.getController(), params);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    resp.sendError(500, "server error");
                    throw new RuntimeException(e);
                }
                // TODO 处理返回值（目前仅支持String和void的返回值）
                postResponseProcess(dispatcher.isRest(), dispatcher.isResponseBody(), dispatcher.isVoid(), result, req, resp);
                return;
            }
        }
        throw new ServletException("no matching handler");
    }

    private void postResponseProcess(boolean isRest, boolean isResponseBody, boolean isVoid, Object result, HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        if (isRest) {
            if (!response.isCommitted()) {
                response.setContentType("application/json;charset=utf-8");
            }
            if (isResponseBody) {
                if (result instanceof String s) {
                    PrintWriter pw = response.getWriter();
                    pw.write(s);
                    pw.flush();
                } else if (result instanceof byte[] data) {
                    var os = response.getOutputStream();
                    os.write(data);
                    os.flush();
                } else {
                    throw new ServletException("Unable to process REST result when handling url");
                }
            } else if (!isVoid) {
                // 如果不是String，且不是byte[]对象，则直接转换为Json字符串
                PrintWriter pw = response.getWriter();
                var jsonStr = JsonUtils.writeJson(result);
                pw.write(jsonStr);
                pw.flush();
            }
        } else {
            if (!response.isCommitted()) {
                response.setContentType("text/html;charset=utf-8");
            }
            if (result instanceof String s) {
                if (isResponseBody) {
                    // send as response body:
                    PrintWriter pw = response.getWriter();
                    pw.write(s);
                    pw.flush();
                } else if (s.startsWith("redirect:")) {
                    // send redirect:
                    response.sendRedirect(s.substring(9));
                } else {
                    // error:
                    throw new ServletException("Unable to process String result when handling url");
                }
            } else if (result instanceof byte[] data) {
                if (isResponseBody) {
                    // send as response body:
                    var output = response.getOutputStream();
                    output.write(data);
                    output.flush();
                } else {
                    // error:
                    throw new ServletException("Unable to process byte[] result when handling url ");
                }
            } else if (result instanceof ModelAndView mv) {
                String view = mv.getView();
                if (view.startsWith("redirect:")) {
                    response.sendRedirect(view.substring(9));
                } else {
                    this.viewResolver.render(mv.getView(), mv.getModel(), req, response);
                }
            } else if (!isVoid && result != null) {
                throw new ServletException("Unable to process " + result.getClass().getName() + " result when handling url");
            }
        }
    }

    private Object[] parseParams(Param[] paramDefs, Dispatcher dispatcher, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletParamParseException {
        Object[] result = new Object[paramDefs.length];
        var urlParams = parseUrl(dispatcher, req);
        var extraParams = parseParam(req); // http://localhost:8080/hello?name=123 (name is an extra param)
        var bodyParams = parseBody(req);
        for (int i = 0; i < result.length; i++) {
            switch (paramDefs[i].getParamType()) {
                case PATH_VARIABLE ->
                        result[i] = StringUtils.parseStr((String) urlParams.get(paramDefs[i].getName()), paramDefs[i].getClassType());
                case REQUEST_PARAM ->
                        result[i] = StringUtils.parseStr((String) extraParams.get(paramDefs[i].getName()), paramDefs[i].getClassType());
                case REQUEST_BODY -> {
                    result[i] = StringUtils.parseStr((String) bodyParams.get(paramDefs[i].getName()), paramDefs[i].getClassType());
                    if (result[i] == null) {
                        result[i] = JsonUtils.parseObject(JsonUtils.writeJson(bodyParams), paramDefs[i].getClassType());
                    }
                }
                case SERVLET_VARIABLE -> {
                    if (paramDefs[i].getClassType().equals(HttpServletRequest.class)) {
                        result[i] = req;
                    } else if (paramDefs[i].getClassType().equals(HttpServletResponse.class)) {
                        result[i] = resp;
                    } else if (paramDefs[i].getClassType().equals(HttpSession.class)) {
                        result[i] = req.getSession();
                    } else if (paramDefs[i].getClassType().equals(ServletContext.class)) {
                        result[i] = req.getServletContext();
                    }
                }
                default -> throw new RuntimeException("unknown param type");
            }
            if (result[i] == null) {
                if (!com.mdc.mspring.context.utils.StringUtils.isEmpty(paramDefs[i].getDefaultValue())) {
                    result[i] = paramDefs[i].getDefaultValue();
                } else if (paramDefs[i].isRequired()) {
                    throw new ServletParamParseException("param " + paramDefs[i].getName() + " is required");
                }
            }
        }
        return result;
    }

    private Map<String, Object> parseUrl(Dispatcher dispatcher, HttpServletRequest req) {
        Map<String, Object> allParams = new HashMap<>();
        // 1 获取header、body和url中的所有参数
        // 1.1 获取url中的所有参数
        allParams.putAll(RegUtils.parse(dispatcher.getUrlPatternStr(), req.getRequestURL().toString()));
        return allParams;
    }

    private Map<String, Object> parseHeader(HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        var headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            result.put(headerName, req.getHeader(headerName));
        }
        return result;
    }

    private Map<String, Object> parseParam(HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        var paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            result.put(paramName, req.getParameter(paramName));
        }
        return result;
    }

    private Map<String, Object> parseBody(HttpServletRequest req) throws IOException {
        Map<String, Object> result = new HashMap<>();
        byte[] bodyData = req.getInputStream().readAllBytes();
        String bodyStr = new String(bodyData, Charset.forName("UTF-8"));
        result.putAll(JsonUtils.parseMap(bodyStr));
        return result;
    }

    private Map<String, Object> parseAllParams(Dispatcher dispatcher, HttpServletRequest req)
            throws IOException {
        String url = UrlUtils.getRelativeUrl(req.getRequestURL().toString());
        Map<String, Object> allParams = new HashMap<>();
        // 1 获取header、body和url中的所有参数
        // 1.1 获取url中的所有参数
        allParams.putAll(parseUrl(dispatcher, req));
        // 1.2 获取header中的所有参数
        allParams.putAll(parseHeader(req));
        // 1.3 解析body中的所有参数
        if (req.getMethod().equals("POST")) {
            // 以Json串形式获取body
            allParams.putAll(parseBody(req));
        }
        return allParams;
    }

    @Override
    public void setContext(Context context) {
        this.context = (ConfigurableApplicationContext) context;
    }
}