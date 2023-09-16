package com.mdc.mspring.context.io;

import com.mdc.mspring.context.common.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.net.URL;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/15 16:01
 */
@Data
@Builder
@AllArgsConstructor
public class DefaultResource implements Resource {
    private String name;
    private URL url;
    private ResourceType type;
    private InputStream inputStream;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public ResourceType getType() {
        return type;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object instanceof DefaultResource) {
            DefaultResource resource = (DefaultResource) object;
            return resource.getName().equals(this.getName()) && resource.getType().equals(this.getType()) && resource.getURL().equals(this.getURL());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode() + this.getType().hashCode() + this.getURL().hashCode();
    }
}
