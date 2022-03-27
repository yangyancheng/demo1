package com.yyc.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    void Register(String serviceName, InetSocketAddress inetSocketAddress);
    //InetSocketAddress lookupService(String serviceName) throws NacosException;
}
