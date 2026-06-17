package com.salon.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 用于业务逻辑中抛出可预知的异常，由 GlobalExceptionHandler 统一捕获处理
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    /**
     * 构造业务异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用错误码枚举构造异常
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码枚举 + 自定义消息构造异常
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /** 资源不存在异常（404） */
    public static BusinessException notFound(String message) {
        return new BusinessException(ErrorCode.NOT_FOUND.getCode(), message);
    }

    /** 参数错误异常（400） */
    public static BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    /** 未授权异常（401） */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), message);
    }
}
