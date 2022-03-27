package com.yyc.rpc.transport.netty.client;


import com.alibaba.nacos.api.exception.NacosException;
import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.Serializer.JsonSerializer;
import com.yyc.rpc.Serializer.KryoSerializer;
import com.yyc.rpc.codec.CommonDecoder;
import com.yyc.rpc.codec.CommonEncoder;
import com.yyc.rpc.entity.RpcRequest;
import com.yyc.rpc.entity.RpcResponse;
import com.yyc.rpc.enumeration.RpcError;
import com.yyc.rpc.exception.RpcException;
import com.yyc.rpc.factory.SingletonFactory;
import com.yyc.rpc.loadbalancer.LoadBalancer;
import com.yyc.rpc.loadbalancer.RandomLoadBalancer;
import com.yyc.rpc.registry.NacosServiceDiscovery;
import com.yyc.rpc.registry.NacosServiceRegistry;
import com.yyc.rpc.registry.ServiceDiscovery;
import com.yyc.rpc.registry.ServiceRegistry;
import com.yyc.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class NettyClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final EventLoopGroup group;
    private static final Bootstrap bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class);
    }

    private final ServiceDiscovery serviceDiscovery;
    private final CommonSerializer serializer;

    private final UnprocessedRequests unprocessedRequests;

    public NettyClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }

    public NettyClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }

    public NettyClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    public NettyClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }


    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        //      AtomicReference<Object> result = new AtomicReference<>(null);
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        try {
            //启动客户端去连接服务器 要分析，涉及到Netty的异步模型
//            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
//            logger.info("客户端连接到服务器 {}:{}", host, port);
//            Channel channel = channelFuture.channel();

            //得到服务的网址
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
                } else {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("发送消息时有错误发生: ", future1.cause());
                }
            });
        } catch (InterruptedException e) {
            unprocessedRequests.remove(rpcRequest.getRequestId());
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }
}
//            if (channel.isActive()) {
//                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
//                    if (future1.isSuccess()) {
//                        logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
//                    } else {
//                        logger.error("发送消息时有错误发生: ", future1.cause());
//                    }
//                });
//
//
//                channel.closeFuture().sync();
////                通过这种方式获得全局可见的返回结果，在获得返回结果 RpcResponse 后，
////                将这个对象以 key 为 rpcResponse 放入 ChannelHandlerContext 中，
////                这里就可以立刻获得结果并返回，我们会在 NettyClientHandler 中看到放入的过程
//
//                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
//                RpcResponse rpcResponse = channel.attr(key).get();
//                //debug
//                return rpcResponse.getData();





