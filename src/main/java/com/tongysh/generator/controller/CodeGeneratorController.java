package com.tongysh.generator.controller;

import com.tongysh.generator.dto.DbConnectionRequest;
import com.tongysh.generator.dto.GeneratorRequest;
import com.tongysh.generator.dto.GeneratorResponse;
import com.tongysh.generator.service.CodeGeneratorService;
import com.tongysh.generator.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码生成器控制器
 * 
 * @author tongysh
 */
@Slf4j
@RestController
@RequestMapping("/api/generator")
public class CodeGeneratorController {

    @Autowired
    private CodeGeneratorService codeGeneratorService;
    
    @Autowired
    private DatabaseService databaseService;

    /**
     * 获取数据库列表
     */
    @PostMapping("/databases")
    public GeneratorResponse getDatabases(@RequestBody DbConnectionRequest request) {
        try {
            List<String> databases = databaseService.getDatabases(request);
            return GeneratorResponse.success("获取数据库列表成功", databases);
        } catch (Exception e) {
            log.error("获取数据库列表失败", e);
            return GeneratorResponse.fail("连接数据库失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定数据库的表列表
     */
    @PostMapping("/tables")
    public GeneratorResponse getTables(@RequestBody DbConnectionRequest request) {
        try {
            List<String> tables = databaseService.getTables(request);
            return GeneratorResponse.success("获取表列表成功", tables);
        } catch (Exception e) {
            log.error("获取表列表失败", e);
            return GeneratorResponse.fail("获取表列表失败: " + e.getMessage());
        }
    }

    /**
     * 生成CRUD代码
     * 
     * @param request 生成请求参数（表名、包名）
     * @return 生成结果
     */
    @PostMapping("/generate")
    public GeneratorResponse generateCode(@RequestBody GeneratorRequest request) {
        log.info("开始生成代码，表名: {}, 包名: {}", request.getTableName(), request.getPackageName());
        
        // 参数校验
        if (request.getDbUrl() == null || request.getDbUrl().trim().isEmpty()) {
            return GeneratorResponse.fail("数据库连接地址不能为空");
        }
        if (request.getDbUsername() == null || request.getDbUsername().trim().isEmpty()) {
            return GeneratorResponse.fail("数据库用户名不能为空");
        }
        if (request.getDatabaseName() == null || request.getDatabaseName().trim().isEmpty()) {
            return GeneratorResponse.fail("数据库名不能为空");
        }
        if (request.getTableName() == null || request.getTableName().trim().isEmpty()) {
            return GeneratorResponse.fail("表名不能为空");
        }
        if (request.getPackageName() == null || request.getPackageName().trim().isEmpty()) {
            return GeneratorResponse.fail("包名不能为空");
        }
        
        return codeGeneratorService.generateCode(request);
    }
}