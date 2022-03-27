package com.yyc.test;

import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.api.HelloService;
import com.yyc.rpc.provider.ServiceProviderImpl;
import com.yyc.rpc.provider.ServiceProvider;
import com.yyc.rpc.transport.RpcServer;
import com.yyc.rpc.transport.netty.server.NettyServer;

public class NettyTestServer {

    //    public static void main(String[] args) {
//        HelloService helloService;
//        helloService = new HelloServiceImpl();
//        ServiceProvider registry = new ServiceProviderImpl();
//        registry.register(helloService);
////        NettyServer server = new NettyServer();
////        server.start(9999);
//    }
    public static void main(String[] args) {
        RpcServer server = new NettyServer("127.0.0.1", 9999, CommonSerializer.KRYO_SERIALIZER);
        server.start();
//        HelloService helloService = new HelloServiceImpl();
//     //   NettyServer server = new NettyServer("127.0.0.1", 9999);
//       // server.setSerializer(new ProtobufSerializer());
//        server.publishService(helloService, HelloService.class);
//        server.start();
    }
}


