package com.yyc.rpc.entity;

import com.yyc.rpc.enumeration.ResponseCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.ws.Response;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {
    /**
     * 响应对应的请求号
     */
    private String requestId;
    /**
     * 响应状态码
     *
     */
    private Integer statusCode;
    /**
     * 响应状态补充数信息
     */
    private String message;
    /**
     * 响应数据
     * 可用于定义通用返回结果 CommonResult<T> 通过参数 T 可根据具体的返回类型动态指定结果的数据类型
     */
    private T data;
    //静态方法，要在static后同样加泛型

    //服务器调用成功，返回客户端信息
    //方法返回值类型为：
    public static<T> RpcResponse<T> success(T data,String requestId){
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }
    //服务器调用失败，返回给客户端信息
    public static <T> RpcResponse<T> fail(ResponseCode code,String requestId){
        RpcResponse<T> response = new RpcResponse<>();

        response.setRequestId(requestId);
        //请求状态码
        response.setStatusCode(code.getCode());
        //请求信息
        response.setMessage(code.getMessage());

        return response;
    }


}
