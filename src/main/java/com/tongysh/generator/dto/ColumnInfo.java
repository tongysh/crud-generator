package com.tongysh.generator.dto;

import lombok.Data;

/**
 * 列信息DTO
 *
 * @author tongysh
 */
@Data
public class ColumnInfo {
    /**
     * 列名
     */
    private String columnName;
    
    /**
     * 列类型
     */
    private String columnType;
    
    /**
     * 列注释
     */
    private String comment;
    
    /**
     * Java类型
     */
    private String javaType;
    
    /**
     * Java字段名
     */
    private String javaFieldName;
    
    /**
     * 是否为主键
     */
    private boolean primaryKey;
    
    /**
     * JDBC类型
     */
    private String jdbcType;
}