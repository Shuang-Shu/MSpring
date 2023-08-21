package com.mdc.mspring.mvc.exception;

import javax.servlet.ServletContextAttributeEvent;
import java.rmi.ServerException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/21/17:36
 * @Description:
 */
public class ServerErrorException extends RuntimeException {
    public ServerErrorException(String message) {
        super(message);
    }
}
