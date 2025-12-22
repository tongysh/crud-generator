package ${packageName}.service;

import ${packageName}.entity.${entityName};
import java.util.List;
import java.util.Map;

/**
 * ${entityName} Service接口
 * 
 * @author tongysh
 */
public interface I${entityName}Service {

    /**
     * 根据ID查询
     */
    ${entityName} getById(${primaryKey.javaType} ${primaryKey.javaFieldName});

    /**
     * 分页条件查询
     */
    Map<String, Object> getByPage(Map<String, Object> params);

    /**
     * 条件查询所有记录（不分页）
     */
    List<${entityName}> getAll(Map<String, Object> params);

    /**
     * 创建
     */
    int create(${entityName} ${entityVarName});

    /**
     * 根据ID更新
     */
    int updateById(${entityName} ${entityVarName});

    /**
     * 根据ID删除
     */
    int deleteById(${primaryKey.javaType} ${primaryKey.javaFieldName});

    /**
     * 批量删除
     */
    int deleteBatchByIds(List<${primaryKey.javaType}> ids);
}
