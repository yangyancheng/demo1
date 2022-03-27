package com.yyc.rpc.transport;

import com.yyc.rpc.Serializer.CommonSerializer;

public interface RpcServer {
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    void start();
    <T> void publishService(T service,String serviceName);
}
