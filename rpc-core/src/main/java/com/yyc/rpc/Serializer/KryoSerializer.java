package com.yyc.rpc.Serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.yyc.rpc.entity.RpcRequest;
import com.yyc.rpc.entity.RpcResponse;
import com.yyc.rpc.enumeration.SerializerCode;
import com.yyc.rpc.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements CommonSerializer{

    /*
    kryo和json的区别？
    Kryo——一是基于字节的序列化，对空间利用率较高，在网络传输时可以减小体积；
    二是序列化时记录属性对象的类型信息，这样在反序列化时就不会出现之前的问题了

    json——
    ②某个类的属性反序列化时，如果属性声明为 Object 的，就会造成反序列化出错，
    通常会把 Object 属性直接反序列化成 String 类型，就需要其他参数辅助序列化。
    ①JSON 序列化器是基于字符串（JSON 串）的，占用空间较大且速度较慢。
     */
    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    //创建一个ThreadLocal来保障线程安全。
    //lamda表达式
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        //lambda表达式，不需要参数，返回值为代码块中内容。
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             //先创建一个output对象
             Output output = new Output(byteArrayOutputStream)){
            Kryo kryo = kryoThreadLocal.get();
            //使用writeObject方法将对象写入Output中
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            //调用toByte获取对象的字节数组
            return output.toBytes();
        } catch (Exception e) {
            logger.error("序列化时有错误发生:", e);
            throw new SerializeException("序列化时有错误发生");
        }

    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return o;
        } catch (Exception e) {
            logger.error("反序列化时有错误发生:", e);
            throw new SerializeException("反序列化时有错误发生");
        }
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("KRYO").getCode();
    }
}
