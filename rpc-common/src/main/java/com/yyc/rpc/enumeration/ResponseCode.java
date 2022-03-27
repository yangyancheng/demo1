package com.yyc.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {
    SUCCESS(200,"调用方法成功"),
    FAIL(400,"调用方法失败"),
    METHOD_NOT_FOUND(401,"方法没有找到");

    private final int code;
    private final  String message;
//
//    ResponseCode(int code, String message) {
//        this.code = code;
//        this.message = message;
//    }
//    public int getCode() {
//        return code;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//

}
