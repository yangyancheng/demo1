package com.yyc.rpc.transport;

import com.alibaba.nacos.api.exception.NacosException;
import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.entity.RpcRequest;

public interface RpcClient {
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;
    Object sendRequest(RpcRequest rpcRequest) throws NacosException;

}
