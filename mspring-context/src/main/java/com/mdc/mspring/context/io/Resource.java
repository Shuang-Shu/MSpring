package com.mdc.mspring.context.io;

import com.mdc.mspring.context.common.ResourceType;

import java.io.InputStream;
import java.net.URL;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/15 15:58
 */
public interface Resource {
    String getName();

    URL getURL();

    ResourceType getType();

    InputStream getInputStream();
}
