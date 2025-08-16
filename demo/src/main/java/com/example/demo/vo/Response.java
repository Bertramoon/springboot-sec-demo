package com.example.demo.vo;

import lombok.Data;

@Data
public class Response<T> {
    private int code;
    private String message;
    private T data;

    public Response() {}
    public Response(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public Response(int code, String message) {
        this(code, message, null);
    }
    public Response(int code) {
        this(code, null);
    }
    public Response(String message) {
        this(200, message, null);
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(200, "success", data);
    }

    public static <T> Response<T> success() {
        return success(null);
    }

    public static <T> Response<T> fail(String message) {
        return new Response<>(500, message, null);
    }

    public static <T> Response<T> fail() {
        return fail(null);
    }
}
