package com.yyc.test;

import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.api.HelloObject;
import com.yyc.rpc.api.HelloService;
import com.yyc.rpc.transport.RpcClientProxy;
import com.yyc.rpc.transport.RpcServer;
import com.yyc.rpc.transport.socket.client.SocketRpcClient;
import com.yyc.rpc.transport.socket.server.SocketRpcServer;

/**
 * 测试用消费者
 */
public class SoketTestClient {
    public static void main(String[] args) {
//        //127.0.0.1是回送地址，即本机地址，用来测试使用。
//        SocketRpcClient client = new SocketRpcClient(CommonSerializer.KRYO_SERIALIZER);
//        //代理类初始化
//        RpcClientProxy proxy = new RpcClientProxy(client);
//        //调用代理类函数，生成代理对象
//        HelloService helloService = proxy.getProxy(HelloService.class);
//        //传入客户端已知接口的参数
//        HelloObject object = new HelloObject(12, "This is a message");
//        //利用代理对象调用输出结果，invoke方法中规定了代理对象被调用时的动作。
//        String res = helloService.hello(object);
//        System.out.println(res);
        RpcServer server = new SocketRpcServer("127.0.0.1", 9998, CommonSerializer.HESSIAN_SERIALIZER);
        server.start();
    }
}
