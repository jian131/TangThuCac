
package com.jian.tangthucac.API;

/**
 * Generic class để đóng gói các phản hồi từ API
 * Giúp xử lý thành công và thất bại một cách nhất quán
 */
public class ApiResponse<T> {
    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    private final Status status;
    private final T data;
    private final String message;
    private final Throwable error;

    private ApiResponse(Status status, T data, String message, Throwable error) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Status.SUCCESS, data, null, null);
    }

    public static <T> ApiResponse<T> error(String msg, Throwable error) {
        return new ApiResponse<>(Status.ERROR, null, msg, error);
    }

    public static <T> ApiResponse<T> loading() {
        return new ApiResponse<>(Status.LOADING, null, null, null);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isSuccessful() {
        return status == Status.SUCCESS;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
