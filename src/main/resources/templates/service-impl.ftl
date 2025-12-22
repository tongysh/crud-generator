package ${packageName}.service.impl;

import ${packageName}.entity.${entityName};
import ${packageName}.mapper.${entityName}Mapper;
import ${packageName}.service.I${entityName}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${entityName} Service实现类
 * 
 * @author tongysh
 */
@Service
public class ${entityName}ServiceImpl implements I${entityName}Service {

    @Autowired
    private ${entityName}Mapper ${entityVarName}Mapper;

    /**
     * 根据ID查询
     */
    @Override
    public ${entityName} getById(${primaryKey.javaType} ${primaryKey.javaFieldName}) {
        return ${entityVarName}Mapper.selectById(${primaryKey.javaFieldName});
    }

    /**
     * 分页条件查询
     */
    @Override
    public Map<String, Object> getByPage(Map<String, Object> params) {
        // 计算分页参数
        int page = params.get("page") != null ? (Integer) params.get("page") : 1;
        int pageSize = params.get("pageSize") != null ? (Integer) params.get("pageSize") : 10;
        int offset = (page - 1) * pageSize;
        
        params.put("offset", offset);
        params.put("limit", pageSize);
        
        // 查询数据
        List<${entityName}> list = ${entityVarName}Mapper.selectByPage(params);
        int total = ${entityVarName}Mapper.countByCondition(params);
        
        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        
        return result;
    }

    /**
     * 条件查询所有记录（不分页）
     */
    @Override
    public List<${entityName}> getAll(Map<String, Object> params) {
        return ${entityVarName}Mapper.selectAll(params);
    }

    /**
     * 创建
     */
    @Override
    public int create(${entityName} ${entityVarName}) {
        return ${entityVarName}Mapper.insert(${entityVarName});
    }

    /**
     * 根据ID更新
     */
    @Override
    public int updateById(${entityName} ${entityVarName}) {
        return ${entityVarName}Mapper.updateById(${entityVarName});
    }

    /**
     * 根据ID删除
     */
    @Override
    public int deleteById(${primaryKey.javaType} ${primaryKey.javaFieldName}) {
        return ${entityVarName}Mapper.deleteById(${primaryKey.javaFieldName});
    }

    /**
     * 批量删除
     */
    @Override
    public int deleteBatchByIds(List<${primaryKey.javaType}> ids) {
        return ${entityVarName}Mapper.deleteBatchByIds(ids);
    }
}
