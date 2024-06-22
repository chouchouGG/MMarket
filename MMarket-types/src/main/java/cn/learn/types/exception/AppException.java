package cn.learn.types.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 98389
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 5317680961212299217L;

    /** 异常码 */
    private String code;

    /** 异常信息 */
    private String info;

    // 构造方法，仅初始化异常码
    public AppException(String code) {
        this.code = code;
    }

    // 构造方法，初始化异常码和原始异常
    public AppException(String code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    // 构造方法，初始化异常码和异常信息
    public AppException(String code, String message) {
        this.code = code;
        this.info = message;
    }

    // 构造方法，初始化异常码、异常信息和原始异常
    public AppException(String code, String message, Throwable cause) {
        this.code = code;
        this.info = message;
        super.initCause(cause);
    }

    // 重写 toString 方法，提供自定义的异常描述
    @Override
    public String toString() {
        return "AppException{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}

