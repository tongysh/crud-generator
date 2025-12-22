package ${packageName}.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * ${tableName} 实体类
 * 
 * @author tongysh
 */
@Data
public class ${entityName} implements Serializable {

    private static final long serialVersionUID = 1L;

<#list columns as column>
    /**
     * ${column.comment!''}
     */
    private ${column.javaType} ${column.javaFieldName};

</#list>
}
