package com.tongysh.generator.dto;

import lombok.Data;

/**
 * 代码生成请求参数
 * 
 * @author tongysh
 */
@Data
public class GeneratorRequest {
    
    /**
     * 数据库连接URL
     */
    private String dbUrl;
    
    /**
     * 数据库用户名
     */
    private String dbUsername;
    
    /**
     * 数据库密码
     */
    private String dbPassword;
    
    /**
     * 数据库名
     */
    private String databaseName;
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 包名
     */
    private String packageName;
    
    /**
     * 输出目录名（与src同级）
     */
    private String outputDir;
}
