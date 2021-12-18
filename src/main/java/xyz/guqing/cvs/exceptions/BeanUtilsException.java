package xyz.guqing.cvs.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BeanUtilsException extends RuntimeException{

    public BeanUtilsException(String message) {
        super(message);
    }

    public BeanUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanUtilsException(Throwable cause) {
        super(cause);
    }

    protected BeanUtilsException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
