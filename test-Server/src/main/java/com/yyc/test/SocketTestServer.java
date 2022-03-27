package com.yyc.test;

import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.annotation.ServiceScan;
import com.yyc.rpc.transport.RpcServer;
import com.yyc.rpc.transport.socket.server.SocketRpcServer;
import com.yyc.rpc.api.HelloService;
import com.yyc.rpc.provider.ServiceProviderImpl;
import com.yyc.rpc.provider.ServiceProvider;


@ServiceScan
public class SocketTestServer {
    public static void main(String[] args) {
//        HelloService helloService = new HelloServiceImpl();
//        ServiceProvider serviceProvider = new ServiceProviderImpl();
//        serviceProvider.register(helloService);
//        SocketRpcServer rpcServer = new SocketRpcServer();
//        rpcServer.start( );
        RpcServer server = new SocketRpcServer("127.0.0.1",1988, CommonSerializer.PROTOBUF_SERIALIZER);
//       SocketRpcServer rpcServer = new SocketRpcServer() ;
        //service是服务端方法的一个实现类，将这个类传入到register
      //  rpcServer.register(helloService, 9000);
    }
}
