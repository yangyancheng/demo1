package com.yyc.rpc.transport.socket.server;

import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.factory.ThreadPoolFactory;
import com.yyc.rpc.handler.RequestHandler;
import com.yyc.rpc.hook.ShutdownHook;
import com.yyc.rpc.provider.ServiceProvider;
import com.yyc.rpc.provider.ServiceProviderImpl;
import com.yyc.rpc.registry.NacosServiceRegistry;
import com.yyc.rpc.transport.AbstractRpcServer;
import com.yyc.rpc.transport.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class SocketRpcServer extends AbstractRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcServer.class);


//    private static final int CORE_POOL_SIZE = 5;
//    private static final int MAXIMUM_POOL_SIZE = 50;
//    private static final int KEEP_ALIVE_TIME = 60;
//    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private final ExecutorService threadPool;
//    private final ServiceProvider serviceProvider;
    RequestHandler requestHandler = new RequestHandler();
    private CommonSerializer serializer;

    public SocketRpcServer(String host,int port){
        this(host,port,DEFAULT_SERIALIZER);
    }

    public SocketRpcServer(String host,int port,Integer serializer) {
//        this.serviceProvider = serviceProvider;
//        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
//        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        //自定义线程池
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
        scanServices();
    }


    //将注册功能独立出去，因为要实现多对一，分离出去后改为启动方法。
    @Override
    public void start( ) {
        try (ServerSocket serverSocket = new ServerSocket( )) {
            serverSocket.bind(new InetSocketAddress(host,port));
            logger.info("服务器正在启动...");
            ShutdownHook.getShutdownHook().addClearAllHook();;
            Socket socket;
            //accept()方法
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接！Ip为：{}{}" + socket.getInetAddress(), socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时候有错误发生：", e);
        }
    }



}














//    public void register(Object service, int port) {
//        try (ServerSocket serverSocket = new ServerSocket(port)) {
//            logger.info("服务器正在启动...");
//            Socket socket;
//            while((socket = serverSocket.accept()) != null) {
//                logger.info("客户端连接！Ip为：" + socket.getInetAddress());
//                threadPool.execute(new WorkerThread(socket, service));
//            }
//        } catch (IOException e) {
//            logger.error("连接时有错误发生：", e);
//        }
//    }
