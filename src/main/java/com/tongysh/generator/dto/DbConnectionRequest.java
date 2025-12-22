package com.tongysh.generator.dto;

import lombok.Data;

/**
 * 数据库连接请求参数
 * 
 * @author tongysh
 */
@Data
public class DbConnectionRequest {
    
    /**
     * 数据库连接URL（不包含数据库名）
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
     * 数据库名（查询表列表时使用）
     */
    private String databaseName;
}
