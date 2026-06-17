package com.salon.common.result;

import com.salon.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一响应结果
 * <p>
 * 所有 API 接口统一返回此格式，包含状态码、消息和数据
 * </p>
 *
 * @param <T> 数据类型
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> {

    /** 状态码，200 表示成功 */
    @Schema(description = "状态码", example = "200")
    private int code;

    /** 提示消息 */
    @Schema(description = "提示消息", example = "操作成功")
    private String msg;

    /** 响应数据 */
    @Schema(description = "响应数据")
    private T data;

    private Result() {}

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 失败响应
     *
     * @param code 错误码
     * @param msg  错误消息
     * @param <T>  数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 通过 ErrorCode 枚举构建错误响应
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 通过 ErrorCode 枚举构建错误响应（自定义消息，覆盖枚举默认消息）
     *
     * @param errorCode 错误码枚举（取其 HTTP 状态码）
     * @param msg       自定义错误消息
     * @param <T>       数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(ErrorCode errorCode, String msg) {
        return new Result<>(errorCode.getCode(), msg, null);
    }

    /**
     * 请求参数错误（400）
     *
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(400, msg, null);
    }

    /**
     * 未授权（401）
     *
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未登录或Token已过期", null);
    }

    /**
     * 资源未找到（404）
     *
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> notFound(String msg) {
        return new Result<>(404, msg, null);
    }

    /**
     * 服务器内部错误（500）
     *
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> serverError() {
        return new Result<>(500, "服务器内部错误", null);
    }
}
