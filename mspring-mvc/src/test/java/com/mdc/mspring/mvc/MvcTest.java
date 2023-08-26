package com.mdc.mspring.mvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.mvc.config.WebMvcConfiguration;
import com.mdc.mspring.mvc.controller.ControllerConfiguration;
import com.mdc.mspring.mvc.servlet.DispatcherServlet;
import com.mdc.mspring.mvc.utils.JsonUtils;
import jakarta.servlet.ServletException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class MvcTest {
    AnnotationConfigApplicationContext context;
    DispatcherServlet dispatcherServlet;
    MockServletContext ctx;

    @Before
    public void init() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.ctx = createMockServletContext();
        WebMvcConfiguration.setServletContext(ctx);
        // String basePackage =
        var applicationContext = new AnnotationConfigApplicationContext(ControllerConfiguration.class);
        doSetProperties(applicationContext);
        this.dispatcherServlet = new DispatcherServlet(applicationContext);
    }

    private void doSetProperties(AnnotationConfigApplicationContext applicationContext) {
        var ps = new Properties();
        ps.put("app.title", "Scan App");
        ps.put("app.version", "v1.0");
        ps.put("summer.web.favicon-path", "/icon/favicon.ico");
        ps.put("summer.web.freemarker.template-path", "/WEB-INF/templates");
        ps.put("jdbc.username", "sa");
        ps.put("jdbc.password", "");
        applicationContext.setProperties(ps);
    }

    MockServletContext createMockServletContext() {
        Path path = Path.of("./src/test/resources").toAbsolutePath().normalize();
        var ctx = new MockServletContext("file://" + path.toString());
        ctx.setRequestCharacterEncoding("UTF-8");
        ctx.setResponseCharacterEncoding("UTF-8");
        return ctx;
    }

    @Test
    public void getHello() throws ServletException, IOException {
        var req = createMockRequest("GET", "/hello/Bob", null, null);
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("Hello, Bob", resp.getContentAsString());
    }

    @Test
    public void getApiHello() throws ServletException, IOException {
        var req = createMockRequest("GET", "/api/hello/Bob", null, null);
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json;charset=utf-8", resp.getContentType());
        assertEquals("{\"name\":\"Bob\"}", resp.getContentAsString());
    }

    @Test
    public void getGreeting() throws ServletException, IOException {
        var req = createMockRequest("GET", "/greeting", null, Map.of("name", "Bob"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("Hello, Bob", resp.getContentAsString());
    }

    @Test
    public void getApiGreeting() throws ServletException, IOException {
        var req = createMockRequest("GET", "/api/greeting", null, Map.of("name", "Bob"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json;charset=utf-8", resp.getContentType());
        assertEquals("{\"action\":{\"name\":\"Bob\"}}", resp.getContentAsString());
    }

    @Test
    public void getGreeting2() throws ServletException, IOException {
        var req = createMockRequest("GET", "/greeting", null, Map.of("action", "Morning", "name", "Bob"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("Morning, Bob", resp.getContentAsString());
    }

    @Test
    public void getGreeting3() throws ServletException, IOException {
        var req = createMockRequest("GET", "/greeting", null, Map.of("action", "Morning"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(400, resp.getStatus());
    }

    @Test
    public void getDownload() throws ServletException, IOException {
        var req = createMockRequest("GET", "/download/server.jar", null,
                Map.of("hasChecksum", "true", "length", "8", "time", "123.4", "md5",
                        "aee9e38cb4d40ec2794542567539b4c8"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertArrayEquals("AAAAAAAA".getBytes(), resp.getContentAsByteArray());
    }

    @Test
    public void getApiDownload() throws ServletException, IOException {
        var req = createMockRequest("GET", "/api/download/server.jar", null,
                Map.of("hasChecksum", "true", "length", "8", "time", "123.4", "md5",
                        "aee9e38cb4d40ec2794542567539b4c8"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json", resp.getContentType());
        assertTrue(resp.getContentAsString().contains("\"file\":\"server.jar\""));
        assertTrue(resp.getContentAsString().contains("\"length\":8"));
        assertTrue(resp.getContentAsString().contains("\"content\":\"QUFBQUFBQUE=\""));
    }

    @Test
    public void getDownloadPart() throws ServletException, IOException {
        var req = createMockRequest("GET", "/download-part", null, null);
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(206, resp.getStatus());
        assertEquals("bytes=100-108", resp.getHeader("Range"));
        assertArrayEquals("AAAAAAAA".getBytes(), resp.getContentAsByteArray());
    }

    @Test
    public void getApiDownloadPart() throws ServletException, IOException {
        var req = createMockRequest("GET", "/api/download-part", null,
                Map.of("file", "server.jar", "hasChecksum", "true", "length", "8", "time", "123.4", "md5",
                        "aee9e38cb4d40ec2794542567539b4c8"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json", resp.getContentType());
        assertTrue(resp.getContentAsString().contains("\"file\":\"server.jar\""));
        assertTrue(resp.getContentAsString().contains("\"length\":8"));
        assertTrue(resp.getContentAsString().contains("\"content\":\"QUFBQUFBQUE=\""));
    }

    @Test
    public void getLogin() throws ServletException, IOException {
        var req = createMockRequest("GET", "/login", null, null);
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(302, resp.getStatus());
        assertEquals("/signin", resp.getRedirectedUrl());
    }

    @Test
    public void getProduct() throws ServletException, IOException {
        var req = createMockRequest("GET", "/product/123", null, Map.of("name", "Bob"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("<h1>Hello, Bob</h1>"));
        assertTrue(resp.getContentAsString().contains("<a href=\"/product/123\">Summer Software</a>"));
    }

    @Test
    public void postSignin() throws ServletException, IOException {
        var req = createMockRequest("POST", "/signin", null, Map.of("name", "Bob", "password", "hello123"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(302, resp.getStatus());
        assertEquals("/home?name=Bob", resp.getRedirectedUrl());
    }

    @Test
    public void postRegister() throws ServletException, IOException {
        var req = createMockRequest("POST", "/register", null, Map.of("name", "Bob", "password", "hello123"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("<h1>Welcome, Bob</h1>"));
    }

    @Test
    public void postApiRegister() throws ServletException, IOException {
        var signin = new SigninObj();
        signin.name = "Bob";
        signin.password = "hello123";
        var req = createMockRequest("POST", "/api/register", signin, null);
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json", resp.getContentType());
        assertEquals("[\"Bob\",true,12345]", resp.getContentAsString());
    }

    @Test
    public void postSignout() throws ServletException, IOException {
        var req = createMockRequest("POST", "/signout", null, Map.of("name", "Bob"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(302, resp.getStatus());
        assertEquals("/signin?name=Bob", resp.getRedirectedUrl());
        assertEquals(Boolean.TRUE, req.getSession().getAttribute("signout"));
    }

    MockHttpServletRequest createMockRequest(String method, String path, Object body, Map<String, String> params)
            throws JsonProcessingException {
        var req = new MockHttpServletRequest(this.ctx, method, path);
        if (method.equals("GET") && params != null) {
            params.keySet().forEach(key -> {
                req.setParameter(key, params.get(key));
            });
        } else if (method.equals("POST")) {
            if (body != null) {
                req.setContentType("application/json");
                req.setContent(JsonUtils.writeJson(body).getBytes(StandardCharsets.UTF_8));
            } else {
                req.setContentType("application/x-www-form-urlencoded");
                if (params != null) {
                    params.keySet().forEach(key -> {
                        req.setParameter(key, params.get(key));
                    });
                }
            }
        }
        var session = new MockHttpSession();
        req.setSession(session);
        return req;
    }

    MockHttpServletResponse createMockResponse() {
        var resp = new MockHttpServletResponse();
        resp.setDefaultCharacterEncoding("UTF-8");
        return resp;
    }

    public static class FileObj {
        public String file;
        public int length;
        public Float downloadTime;
        public String md5;
        public byte[] content;
    }

    public static class SigninObj {
        public String name;
        public String password;
    }
}
