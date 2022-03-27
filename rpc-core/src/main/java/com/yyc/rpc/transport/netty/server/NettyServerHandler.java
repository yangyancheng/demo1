package com.yyc.rpc.transport.netty.server;

import com.yyc.rpc.factory.SingletonFactory;
import com.yyc.rpc.factory.ThreadPoolFactory;
import com.yyc.rpc.handler.RequestHandler;
import com.yyc.rpc.entity.RpcRequest;
import com.yyc.rpc.entity.RpcResponse;
import com.yyc.rpc.provider.ServiceProvider;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
说明：
1.我们自定义一个Handler，需要继承netty规定好的Handler
这是我们自定义一个Handler，才能成为一个Handler
 */
//继承一个Channel入栈的handler的适配器
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RequestHandler requestHandler;
    public NettyServerHandler() {
        this.requestHandler = SingletonFactory.getInstance(RequestHandler.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        try {
            if(msg.getHeartBeat()) {
                logger.info("接收到客户端心跳包...");
                return;
            }
            logger.info("服务器接收到请求: {}", msg);
            Object result = requestHandler.handle(msg);
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
            } else {
                logger.error("通道不可写");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                logger.info("长时间未收到心跳包，断开连接...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

//    private static ServiceProvider serviceProvider;
//
//    static {
//        requestHandler = new RequestHandler();
//        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_
//    }
//
//    //读取数据实际（这里我们可以读取客户端发送的消息）
//    /*
//    1.ChannelHandlerContext ctx：上下文对象，含有管道pipeline（业务逻辑处理管道），包含很多信息
//    还有通道channel（注重数据的读和写），地址
//    2.Object msg:就是客户端发送的数据 默认是Object形式
//
//     */
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
////        try {
////            logger.info("服务器接收到请求: {}", msg);
////
////            String interfaceName = msg.getInterfaceName();
////            Object service = serviceProvider.getService(interfaceName);
////            Object result = requestHandler.handle(msg, service);
////
////            //这个是获取地址；我自己加的
////            ctx.channel().remoteAddress();
////            //write+flush 把数据写到一个缓存，并刷新。
////            //我们一般对发送的数据进行编码，不过我们这里输出的基本都是在控制台上的日志文件以方便调试。
////            ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result));
////            future.addListener(ChannelFutureListener.CLOSE);
////        } finally {
////            ReferenceCountUtil.release(msg);
////        }
//        threadPool.execute(() -> {
//            try {
//                logger.info("服务器接收到请求: {}", msg);
//                Object result = requestHandler.handle(msg);
//                ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
//                future.addListener(ChannelFutureListener.CLOSE);
//            } finally {
//                ReferenceCountUtil.release(msg);
//            }
//        });
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        logger.error("处理过程调用时有错误发生:");
//        cause.printStackTrace();
//        ctx.close();
//    }
}

