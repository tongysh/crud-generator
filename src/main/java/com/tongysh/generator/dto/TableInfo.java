package com.tongysh.generator.dto;

import lombok.Data;
import java.util.List;

/**
 * 表信息DTO
 *
 * @author tongysh
 */
@Data
public class TableInfo {
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 包名
     */
    private String packageName;
    
    /**
     * 实体类名
     */
    private String entityName;
    
    /**
     * 实体变量名
     */
    private String entityVarName;
    
    /**
     * 列信息列表
     */
    private List<ColumnInfo> columns;
    
    /**
     * 主键列信息
     */
    private ColumnInfo primaryKey;
}