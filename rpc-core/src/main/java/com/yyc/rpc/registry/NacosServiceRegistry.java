package com.yyc.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.yyc.rpc.enumeration.RpcError;
import com.yyc.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class NacosServiceRegistry implements ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);
    private static final String SERVER_ADDR = "127.0.0.1:8848";
    private static final NamingService namingService;
/*
通过Nacos提供的NamingFatory创建 NamingService 来连接Nacos
这个过程放在静态代码块中了，在类加载时执行，从而自动连接。



这个

 */

    static {
        try {
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接到Nacos时有错误发生: ", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }

    }


    @Override
    public void Register(String serviceName, InetSocketAddress inetSocketAddress) {

//            namingService 提供了两个很方便的接口，registerInstance
        //这些方法都需要抛出异常
        try {
            namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        }catch (NacosException e){
            logger.error("注册时有错误发生",e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }

//    @Override
//    public InetSocketAddress lookupService(String serviceName) {
//        //namingService 提供了两个很方便的接口，registerInstance 和 getAllInstances 方法
//        try {
//            List<Instance> instances = namingService.getAllInstances(serviceName);
//
//            //得到所有的服务提供者列表之后，需要选择一个，涉及到负载均衡策略！！
//            //选择第0个
//            Instance instance = instances.get(0);
//            return new InetSocketAddress(instance.getIp(), instance.getPort());
//        }catch (NacosException e){
//            logger.error("获取服务时有错误发生：", e);
//            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
//        }

//    }
}
