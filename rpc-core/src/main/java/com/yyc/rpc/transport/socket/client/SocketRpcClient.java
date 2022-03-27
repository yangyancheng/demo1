package com.yyc.rpc.transport.socket.client;

import com.alibaba.nacos.api.exception.NacosException;
import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.entity.RpcRequest;
import com.yyc.rpc.entity.RpcResponse;
import com.yyc.rpc.enumeration.ResponseCode;
import com.yyc.rpc.enumeration.RpcError;
import com.yyc.rpc.exception.RpcException;
import com.yyc.rpc.loadbalancer.LoadBalancer;
import com.yyc.rpc.loadbalancer.RandomLoadBalancer;
import com.yyc.rpc.registry.NacosServiceDiscovery;
import com.yyc.rpc.registry.NacosServiceRegistry;
import com.yyc.rpc.registry.ServiceDiscovery;
import com.yyc.rpc.registry.ServiceRegistry;
import com.yyc.rpc.transport.RpcClient;
import com.yyc.rpc.transport.socket.server.SocketRpcServer;
import com.yyc.rpc.transport.socket.util.ObjectReader;
import com.yyc.rpc.transport.socket.util.ObjectWriter;
import com.yyc.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketRpcClient implements RpcClient {



    private static final Logger logger = LoggerFactory.getLogger(SocketRpcClient.class);

    private final ServiceDiscovery serviceDiscovery;
    private CommonSerializer serializer;

    public SocketRpcClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }
    public SocketRpcClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }
    public SocketRpcClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    public SocketRpcClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
    }


    @Override
    public Object sendRequest(RpcRequest rpcRequest) throws NacosException {
        if(serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);
            Object obj = ObjectReader.readObject(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;
            if (rpcResponse == null) {
                logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse.getData();
        } catch (IOException e) {
            logger.error("调用时有错误发生：", e);
            throw new RpcException("服务调用失败: ", e);
        }
    }

}


//    private static final Logger logger = LoggerFactory.getLogger(SocketRpcClient.class);
//
//    private final ServiceRegistry serviceRegistry;
//    private final CommonSerializer serializer;
//
//    public SocketRpcClient(Integer serializer) {
//        this.serializer = CommonSerializer.getByCode(serializer);
//    }
//
//    @Override
//    public Object sendRequest(RpcRequest rpcRequest) {
//        if(serializer == null) {
//            logger.error("未设置序列化器");
//            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
//        }
//       //端口号和host主机地址还没有输入
//        try (Socket socket = new Socket()) {
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//            objectOutputStream.writeObject(rpcRequest);
//            objectOutputStream.flush();
//            return objectInputStream.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            logger.error("调用时有错误发生：", e);
//            return null;
//        }
//    }