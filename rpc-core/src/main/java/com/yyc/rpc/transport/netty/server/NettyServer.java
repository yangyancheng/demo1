package com.yyc.rpc.transport.netty.server;


import com.yyc.rpc.Serializer.CommonSerializer;
import com.yyc.rpc.Serializer.KryoSerializer;
import com.yyc.rpc.codec.CommonDecoder;
import com.yyc.rpc.codec.CommonEncoder;
import com.yyc.rpc.enumeration.RpcError;
import com.yyc.rpc.exception.RpcException;
import com.yyc.rpc.hook.ShutdownHook;
import com.yyc.rpc.provider.ServiceProvider;
import com.yyc.rpc.provider.ServiceProviderImpl;
import com.yyc.rpc.registry.NacosServiceRegistry;
import com.yyc.rpc.registry.ServiceRegistry;
import com.yyc.rpc.transport.AbstractRpcServer;
import com.yyc.rpc.transport.RpcServer;
import com.yyc.rpc.transport.netty.client.NettyClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyServer extends AbstractRpcServer {

    private final CommonSerializer serializer;

    public NettyServer(String host,int port){
        //不输入serializer时默认为Keyo
        this(host,port,DEFAULT_SERIALIZER);
    }

    public NettyServer(String host, int port, Integer serializer) {      //NettyServer 初始化
        this.host = host;
        this.port = port;
        this.serializer = CommonSerializer.getByCode(serializer);
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();

        scanServices();
       }



    @Override
    public void start( ) {


        ShutdownHook.getShutdownHook().addClearAllHook();
        EventLoopGroup bossGroup = new NioEventLoopGroup();    // bossGroup 用于接收客户端传过来的请求，接收到请求后将后续操作交由 workerGroup 处理。 //构建两个线程组--可以这么理解，bossGroup 和 workerGroup 是两个线程池,
        EventLoopGroup workerGroup = new NioEventLoopGroup();    // 它们默认线程数为 CPU 核心数乘以 2，   // 我们需要两种类型的人干活，一个是老板，一个是工人，老板负责从外面接活，        // 接到的活分配给工人干，放到这里，bossGroup的作用就是不断地accept到新的连接， // 将新的连接丢给workerGroup来处理

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();  //生成服务端启动辅助类实例——bootstrap  配置必要组件     //是服务端的一个启动辅助类，通过给他设置一系列参数来绑定端口启动服务
            //使用链式编程进行设置。
            serverBootstrap.group(bossGroup, workerGroup)    //指定服务器端监听套接字通道 NioServerSocketChannel，  // 其内部管理了一个 Java NIO 中的ServerSocketChannel实例。          /// 设置使用NioServerSocketChannel作为服务器通道的实现
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)//设置线程队列中等待连接的个数
                    .option(ChannelOption.SO_KEEPALIVE, true)//保持活动连接状态
                    .childOption(ChannelOption.TCP_NODELAY, true)     //childHandler用于设置业务职责链 //给我们的workergroup的EventLoop设置处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {      //创建一个通道对象（匿名对象）
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));           //心跳机制
                            pipeline.addLast(new CommonEncoder(serializer)); //处理 命令的编码、解码
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());       //添加自定义的channelHandler，添加自定义的处理器。
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(host,port).sync();       // 这里就是真正的启动过程了，绑定端口并且同步，等待服务器启动完毕，才会进入下行代码 //启动服务器（并绑定端口）  //调用serverBootstrap的bind方法将服务绑定端口上。            ChannelFuture future = serverBootstrap.bind(host,port).sync();       // 这里就是真正的启动过程了，绑定端口并且同步，等待服务器启动完毕，才会进入下行代码 //启动服务器（并绑定端口）  //调用serverBootstrap的bind方法将服务绑定端口上。
            ShutdownHook.getShutdownHook().addClearAllHook(); //启动服务器之前调用增加清除全部钩子方法。
            future.channel().closeFuture().sync();  //对关闭通道进行监听
        } catch (InterruptedException e) {
            logger.error("启动服务器时有错误发生: ", e);
        } finally {
            bossGroup.shutdownGracefully(); //优雅退出，释放线程池资源。关闭两组死循环。
            workerGroup.shutdownGracefully();
        }
    }



}
