package ${packageName}.mapper;

import ${packageName}.entity.${entityName};
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * ${entityName} Mapper接口
 * 
 * @author tongysh
 */
@Mapper
public interface ${entityName}Mapper {

    /**
     * 根据ID查询
     */
    ${entityName} selectById(@Param("${primaryKey.javaFieldName}") ${primaryKey.javaType} ${primaryKey.javaFieldName});

    /**
     * 分页条件查询
     */
    List<${entityName}> selectByPage(Map<String, Object> params);

    /**
     * 条件查询所有记录（不分页）
     */
    List<${entityName}> selectAll(Map<String, Object> params);

    /**
     * 条件查询总数
     */
    int countByCondition(Map<String, Object> params);

    /**
     * 插入数据
     */
    int insert(${entityName} entity);

    /**
     * 根据ID更新
     */
    int updateById(${entityName} entity);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("${primaryKey.javaFieldName}") ${primaryKey.javaType} ${primaryKey.javaFieldName});

    /**
     * 批量删除
     */
    int deleteBatchByIds(@Param("ids") List<${primaryKey.javaType}> ids);
}
