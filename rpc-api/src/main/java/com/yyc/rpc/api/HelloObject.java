package com.yyc.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 测试API（应用程序测试接口）的实体
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloObject implements Serializable {
    private int id;
    private String message;

}
