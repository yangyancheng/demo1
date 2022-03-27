package com.yyc.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Random;

/*
instances  一堆实例，size()可判断其数量规模
 */
public class RandomLoadBalancer implements LoadBalancer{
    @Override
    public Instance select(List<Instance> instances) {
       return instances.get(new Random().nextInt(instances.size()));
    }
}
