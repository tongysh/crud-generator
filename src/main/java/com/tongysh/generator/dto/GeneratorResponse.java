package com.tongysh.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码生成响应结果
 * 
 * @author tongysh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratorResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 提示信息
     */
    private String message;
    
    /**
     * 返回数据（可以是String、List等）
     */
    private Object data;
    
    public static GeneratorResponse success(String message, Object data) {
        return new GeneratorResponse(true, message, data);
    }
    
    public static GeneratorResponse fail(String message) {
        return new GeneratorResponse(false, message, null);
    }
}
