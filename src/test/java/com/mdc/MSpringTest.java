package com.mdc;

import com.mdc.mspring.resolver.Resource;
import com.mdc.mspring.resolver.ResourceResolver;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/10/16:37
 * @Description:
 */
public class MSpringTest {
    @Test
    public void test() throws IOException {
//        System.out.println("hello world");
    }

    @Test
    public void testResourceResolver() throws IOException, URISyntaxException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        System.out.println(resolver.scan(Resource::getName));
    }

    @Test
    public void testAddAllNull() throws IOException, URISyntaxException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        System.out.println(resolver.scan(Resource::getPath));
    }
}
