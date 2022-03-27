package com.yyc.rpc.transport;

import com.yyc.rpc.entity.RpcRequest;
import com.yyc.rpc.entity.RpcResponse;
import com.yyc.rpc.transport.netty.client.NettyClient;
import com.yyc.rpc.transport.socket.client.SocketRpcClient;
import com.yyc.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RpcClientProxy implements InvocationHandler {
    //①实现接口
    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);
    //②被代理对象和构造方法放进来
    private final RpcClient client;
    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    //让编译器忽略未参数化的警告。
    //给某类生成一个代理对象
    public <T> T getProxy(Class<T> clazz) {
        // 返回指定接口的代理类的实例，该接口将方法调用分派给指定的调用处理程序。
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    //③重写invoke方法
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        RpcRequest rpcRequest = RpcRequest.builder()
//                .interfaceName(method.getDeclaringClass().getName())
//                .methodName(method.getName())
//                .parameters(args)
//                .paramTypes(method.getParameterTypes())
//                .build();
//相当于以下创建，只不过lombok更便捷
        //我们获取了代理对象的方法、参数
        //getDec拉ringclass返回枚举类
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        //生成request对象
        RpcRequest rpcRequest = new RpcRequest(
                UUID.randomUUID().toString(),
                //方法所属于的类
                method.getDeclaringClass().getName(),
                //得到方法名
                method.getName(),
//                参数
                args,
                //参数类型
                method.getParameterTypes(),
                false);
        //返回response对象
        RpcResponse rpcResponse = null;
        if (client instanceof NettyClient) {
            try {
                //debug
                CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) client.sendRequest(rpcRequest);
                rpcResponse = completableFuture.get();
            } catch (Exception e) {
                logger.error("方法调用请求发送失败", e);
                return null;
            }
        }
        if (client instanceof SocketRpcClient) {
            rpcResponse = (RpcResponse) client.sendRequest(rpcRequest);
        }
        RpcMessageChecker.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }
}
