package com.itcast.minio.model.base;

/**
 * 结果集返回响应状态码
 * @author zheng.zhang
 */
public class ResponseModel {
    /**
     * 响应业务状态
     */
    private Integer status;
    /**
     * 响应消息
     */
    private String msg;
    /**
     * 响应中的数据
     */
    private Object responseData;

    public ResponseModel() {
        this.status = 0;
        this.msg = "OK";
    }

    /**
     * 只返回状态码和消息
     * @param status 状态码
     * @param msg 消息
     */
    public ResponseModel(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    /**
     * 可以手动设置消息和状态码
     * @param status 状态码
     * @param msg 消息
     * @param responseData 返回数据
     */
    public ResponseModel(Integer status, String msg, Object responseData) {
        this.status = status;
        this.msg = msg;
        this.responseData = responseData;
    }

    /**
     * 返回数据对象
     * @param responseData 响应数据
     */
    public ResponseModel(Object responseData) {
        this.status = 0;
        this.msg = "OK";
        this.responseData = responseData;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }

    public static ResponseModel ok() {
        return new ResponseModel();
    }

    public static ResponseModel ok(Object data) {
        return new ResponseModel(data);
    }

    public static ResponseModel build(Integer status, String msg) {
        return new ResponseModel(status, msg);
    }

    public static ResponseModel build(Integer status, String msg, Object data) {
        return new ResponseModel(status, msg, data);
    }
}
