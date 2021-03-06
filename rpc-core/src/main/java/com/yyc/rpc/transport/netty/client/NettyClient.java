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
            logger.error("?????????????????????");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        //      AtomicReference<Object> result = new AtomicReference<>(null);
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        try {
            //????????????????????????????????? ?????????????????????Netty???????????????
//            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
//            logger.info("??????????????????????????? {}:{}", host, port);
//            Channel channel = channelFuture.channel();

            //?????????????????????
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("?????????????????????: %s", rpcRequest.toString()));
                } else {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("??????????????????????????????: ", future1.cause());
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
//                        logger.info(String.format("?????????????????????: %s", rpcRequest.toString()));
//                    } else {
//                        logger.error("??????????????????????????????: ", future1.cause());
//                    }
//                });
//
//
//                channel.closeFuture().sync();
////                ??????????????????????????????????????????????????????????????????????????? RpcResponse ??????
////                ?????????????????? key ??? rpcResponse ?????? ChannelHandlerContext ??????
////                ????????????????????????????????????????????????????????? NettyClientHandler ????????????????????????
//
//                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
//                RpcResponse rpcResponse = channel.attr(key).get();
//                //debug
//                return rpcResponse.getData();





