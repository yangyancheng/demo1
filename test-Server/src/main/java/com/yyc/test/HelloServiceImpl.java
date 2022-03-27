package com.yyc.test;


import com.yyc.rpc.api.HelloObject;
import com.yyc.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class HelloServiceImpl implements HelloService {

    /*
    定义成static final,logger变量不可变，读取速度快

    static 修饰的变量是不管创建了new了多少个实例，也只创建一次，节省空间，
    如果每次都创建Logger的话比较浪费内存；final修饰表示不可更改，常量
    将域定义为static,每个类中只有一个这样的域.而每一个对象对于所有的实例域却都有自己的一份拷贝.，
    用static修饰既节约空间，效率也好。

    final 是本 logger 不能再指向其他 Logger 对象
     */
    private  static  final Logger logger =  LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到：{}",object.getMessage());
        return "调用的返回值为：id："+object.getId();
    }
}
