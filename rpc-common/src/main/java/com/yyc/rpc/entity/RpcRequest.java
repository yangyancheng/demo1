package com.yyc.rpc.entity;

import lombok.*;

import java.io.Serializable;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data

public class RpcRequest implements Serializable {
    //服务端需要信息

    /**
     * 请求号
     */
    private String requestId;
    /**
     * 待调用接口名称
     */
    private String interfaceName;

    /**
     * 待调用方法的名称
     */
    private String methodName;

    /**
     * 待调用方法的参数值
     */
    private Object[] parameters;

    /**
     * 待调用方法的参数类型
     * 参数类型直接使用Class对象，也可用字符串。
     */
    private  Class<?>[] paramTypes;

    /**
     * 是否是心跳包
     */
    private Boolean heartBeat;

}
