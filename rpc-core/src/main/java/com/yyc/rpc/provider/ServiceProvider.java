package com.yyc.rpc.provider;

/**
 * 保存和提供服务的对象
 */
public interface ServiceProvider {
    //register 注册信息
   // <T> void addServiceProvider(T service,String serviceName);
    <T> void addServiceProvider(T service,String ServiceName);

    //getRegister获得信息。
    Object getServiceProvider(String serviceName);
}
