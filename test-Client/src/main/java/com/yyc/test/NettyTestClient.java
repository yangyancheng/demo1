package com.yyc.test;

import com.yyc.rpc.transport.RpcClientProxy;
import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.api.HelloObject;
import com.yyc.rpc.api.HelloService;
import com.yyc.rpc.transport.RpcClient;
import com.yyc.rpc.transport.netty.client.NettyClient;

public class NettyTestClient {

    public static void main(String[] args) {
     //   RpcClient client = new NettyClient("127.0.0.1", 9999);

        RpcClient client = new NettyClient(CommonSerializer.PROTOBUF_SERIALIZER);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        //debug
        String res = helloService.hello(object);
        System.out.println(res);
    }

}
