package com.mdc.mspring.mvc.view.impl;

import com.mdc.mspring.mvc.exception.ServerErrorException;
import com.mdc.mspring.mvc.view.ViewResolver;
import com.mysql.fabric.Server;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/17:32
 * @Description:
 */
public class FreeMarkerViewResolver implements ViewResolver {

    final String templatePath;
    final String templateEncoding;
    final ServletContext servletContext;

    Configuration config;

    public FreeMarkerViewResolver(ServletContext servletContext, String templatePath, String templateEncoding) {
        this.servletContext = servletContext;
        this.templatePath = templatePath;
        this.templateEncoding = templateEncoding;
    }

    @Override
    public void init() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
        cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
        cfg.setDefaultEncoding(this.templateEncoding);
        cfg.setTemplateLoader(new ServletTemplateLoader(this.servletContext, this.templatePath));
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setAutoEscapingPolicy(Configuration.ENABLE_IF_SUPPORTED_AUTO_ESCAPING_POLICY);
        cfg.setLocalizedLookup(false);
        var ow = new DefaultObjectWrapper(Configuration.VERSION_2_3_32);
        ow.setExposeFields(true);
        cfg.setObjectWrapper(ow);
        this.config = cfg;
    }

    @Override
    public void render(String viewName, Map<String, Object> model, HttpServletRequest req, HttpServletResponse resp) throws ServerErrorException, IOException {
        Template templ = null;
        try {
            templ = this.config.getTemplate(viewName);
        } catch (Exception e) {
            throw new ServerErrorException("View not found: " + viewName);
        }
        PrintWriter pw = resp.getWriter();
        try {
            templ.process(model, pw);
        } catch (TemplateException e) {
            throw new ServerErrorException(e.getMessage());
        }
        pw.flush();
    }
}

/**
 * copied from freemarker.cache.WebappTemplateLoader and modified to use
 * jakarta.servlet.ServletContext.
 * <p>
 * Because it is used old javax.servlet.ServletContext.
 */
class ServletTemplateLoader implements TemplateLoader {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final ServletContext servletContext;
    private final String subdirPath;

    public ServletTemplateLoader(ServletContext servletContext, String subdirPath) {
        Objects.requireNonNull(servletContext);
        Objects.requireNonNull(subdirPath);

        subdirPath = subdirPath.replace('\\', '/');
        if (!subdirPath.endsWith("/")) {
            subdirPath += "/";
        }
        if (!subdirPath.startsWith("/")) {
            subdirPath = "/" + subdirPath;
        }
        this.subdirPath = subdirPath;
        this.servletContext = servletContext;
    }

    @Override
    public Object findTemplateSource(String name) throws IOException {
        String fullPath = subdirPath + name;

        try {
            String realPath = servletContext.getRealPath(fullPath);
            if (realPath != null) {
                File file = new File(realPath);
                if (file.canRead() && file.isFile()) {
                    return file;
                }
            }
        } catch (SecurityException e) {
            ;// ignore
        }
        return null;
    }

    @Override
    public long getLastModified(Object templateSource) {
        if (templateSource instanceof File) {
            return ((File) templateSource).lastModified();
        }
        return 0;
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        if (templateSource instanceof File) {
            return new InputStreamReader(new FileInputStream((File) templateSource), encoding);
        }
        throw new IOException("File not found.");
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
    }

    public Boolean getURLConnectionUsesCaches() {
        return Boolean.FALSE;
    }
}