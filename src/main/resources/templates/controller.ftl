package ${packageName}.controller;

import ${packageName}.entity.${entityName};
import ${packageName}.service.I${entityName}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * ${entityName} Controller
 * 
 * @author tongysh
 */
@RestController
@RequestMapping("/api/${entityVarName}")
public class ${entityName}Controller {

    @Autowired
    private I${entityName}Service ${entityVarName}Service;

    /**
     * 根据ID查询
     */
    @GetMapping("/{${primaryKey.javaFieldName}}")
    public ${entityName} getById(@PathVariable ${primaryKey.javaType} ${primaryKey.javaFieldName}) {
        return ${entityVarName}Service.getById(${primaryKey.javaFieldName});
    }

    /**
     * 分页条件查询
     */
    @PostMapping("/page")
    public Map<String, Object> getByPage(@RequestBody Map<String, Object> params) {
        return ${entityVarName}Service.getByPage(params);
    }

    /**
     * 条件查询所有记录（不分页）
     */
    @PostMapping("/all")
    public List<${entityName}> getAll(@RequestBody Map<String, Object> params) {
        return ${entityVarName}Service.getAll(params);
    }

    /**
     * 创建
     */
    @PostMapping
    public int create(@RequestBody ${entityName} ${entityVarName}) {
        return ${entityVarName}Service.create(${entityVarName});
    }

    /**
     * 根据ID更新
     */
    @PutMapping
    public int update(@RequestBody ${entityName} ${entityVarName}) {
        return ${entityVarName}Service.updateById(${entityVarName});
    }

    /**
     * 根据ID删除
     */
    @DeleteMapping("/{${primaryKey.javaFieldName}}")
    public int deleteById(@PathVariable ${primaryKey.javaType} ${primaryKey.javaFieldName}) {
        return ${entityVarName}Service.deleteById(${primaryKey.javaFieldName});
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public int deleteBatch(@RequestBody List<${primaryKey.javaType}> ids) {
        return ${entityVarName}Service.deleteBatchByIds(ids);
    }
}
