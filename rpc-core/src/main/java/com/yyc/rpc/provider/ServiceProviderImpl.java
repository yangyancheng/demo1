package com.yyc.rpc.provider;

import com.yyc.rpc.enumeration.RpcError;
import com.yyc.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceProviderImpl implements ServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderImpl.class);
    //多线程时用的hashmap转变为ConcurrentHashMap

    /**
     * 将服务名与提供服务的对象的对应关系保存在一个 ConcurrentHashMap 中，
     * 并且使用一个 Set 来保存当前有哪些对象已经被注册
     */

    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private final Set<String> registeredService = ConcurrentHashMap.newKeySet();


    @Override
    public <T> void addServiceProvider(T service,String serviceName) {
        if (registeredService.contains(serviceName)) return;
        serviceMap.put(serviceName, service);
        logger.info("向接口: {} 注册服务: {}", service.getClass().getInterfaces(), serviceName);
    }
    @Override
    public synchronized Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;

    }
}
