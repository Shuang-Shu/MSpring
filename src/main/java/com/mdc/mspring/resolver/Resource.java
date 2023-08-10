package com.mdc.mspring.resolver;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Description: Resource presents a resource file
 */
public class Resource {
    private final String path; // absolute path
    private final String name; // full qualified name(sparer is /)

    public Resource(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
