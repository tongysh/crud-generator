package com.tongysh.generator.service;

import com.tongysh.generator.dto.ColumnInfo;
import com.tongysh.generator.dto.GeneratorRequest;
import com.tongysh.generator.dto.GeneratorResponse;
import com.tongysh.generator.dto.TableInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 代码生成服务
 * 
 * @author tongysh
 */
@Slf4j
@Service
public class CodeGeneratorService {

    /**
     * 生成CRUD代码
     */
    public GeneratorResponse generateCode(GeneratorRequest request) {
        try {
            String tableName = request.getTableName();
            String packageName = request.getPackageName();
            
            // 获取输出目录，确保与项目src同级
            String projectRoot = System.getProperty("user.dir");
            String outputPath = request.getOutputDir();
            if (outputPath == null || outputPath.trim().isEmpty()) {
                outputPath = "generated-code";
            }
            
            // 确保输出目录与项目src同级
            outputPath = projectRoot + "/" + outputPath + "/";
            
            // 确保输出路径以斜杠结尾
            if (!outputPath.endsWith("/")) {
                outputPath += "/";
            }
            
            String srcPath = outputPath + "src/main/java/";
            String resourcePath = outputPath + "src/main/resources/";

            // 解析表结构（使用动态数据库连接）
            TableInfo tableInfo = parseTableInfo(request, tableName, packageName);

            // 生成各个文件
            generateEntity(tableInfo, srcPath);
            generateMapper(tableInfo, srcPath);
            generateMapperXml(tableInfo, resourcePath);
            generateService(tableInfo, srcPath);
            generateServiceImpl(tableInfo, srcPath);
            generateController(tableInfo, srcPath);

            // 返回相对于项目根目录的路径
            String relativePath = new File(outputPath).getName();
            return GeneratorResponse.success(
                    "代码生成成功！",
                    "代码已生成在项目根目录的 '../" + relativePath + "' 文件夹中"
            );
        } catch (Exception e) {
            log.error("代码生成失败", e);
            return GeneratorResponse.fail("代码生成失败: " + e.getMessage());
        }
    }

    /**
     * 解析表信息
     */
    private TableInfo parseTableInfo(GeneratorRequest request, String tableName, String packageName) throws Exception {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName(tableName);
        tableInfo.setPackageName(packageName);
        tableInfo.setEntityName(toCamelCase(tableName, true));
        tableInfo.setEntityVarName(toCamelCase(tableName, false));
        List<ColumnInfo> columns = new ArrayList<>();
        
        // 构建数据库连接URL
        String url = request.getDbUrl();
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += request.getDatabaseName();
        // 确保时区参数正确设置，避免拼接错误
        if (!url.contains("serverTimezone=")) {
            if (url.contains("?")) {
                url += "&serverTimezone=Asia/Shanghai";
            } else {
                url += "?serverTimezone=Asia/Shanghai";
            }
        } else {
            // 如果已有时区参数，确保它是正确的
            url = url.replaceAll("serverTimezone=[^&]*", "serverTimezone=Asia/Shanghai");
        }
        // 确保其他必要参数也存在
        if (!url.contains("useUnicode=true")) {
            if (url.contains("?")) {
                url += "&useUnicode=true";
            } else {
                url += "?useUnicode=true";
            }
        }
        if (!url.contains("characterEncoding=utf8")) {
            if (url.contains("?")) {
                url += "&characterEncoding=utf8";
            } else {
                url += "?characterEncoding=utf8";
            }
        }
        if (!url.contains("useSSL=false")) {
            if (url.contains("?")) {
                url += "&useSSL=false";
            } else {
                url += "?useSSL=false";
            }
        }
        
        try (Connection conn = DriverManager.getConnection(
                url,
                request.getDbUsername(),
                request.getDbPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 获取主键信息
            ResultSet pkRs = metaData.getPrimaryKeys(null, null, tableName);
            String primaryKey = null;
            if (pkRs.next()) {
                primaryKey = pkRs.getString("COLUMN_NAME");
            }
            pkRs.close();
            
            // 获取列信息
            ResultSet rs = metaData.getColumns(null, null, tableName, null);
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("TYPE_NAME");
                String remarks = rs.getString("REMARKS");
                
                column.setColumnName(columnName);
                column.setColumnType(columnType);
                column.setComment(remarks != null ? remarks : "");
                
                // 设置Java类型
                column.setJavaType(mapSqlTypeToJava(columnType));
                
                // 设置JDBC类型
                column.setJdbcType(mapSqlTypeToJdbc(columnType));
                
                // 设置Java字段名
                column.setJavaFieldName(toCamelCase(columnName, false));
                
                // 判断是否为主键
                column.setPrimaryKey(columnName.equals(primaryKey));
                
                columns.add(column);
            }
            rs.close();
            
            tableInfo.setColumns(columns);
            if (primaryKey != null) {
                // 查找主键列信息
                for (ColumnInfo col : columns) {
                    if (col.isPrimaryKey()) {
                        tableInfo.setPrimaryKey(col);
                        break;
                    }
                }
            }
        }
        
        return tableInfo;
    }

    /**
     * SQL类型映射到Java类型
     */
    private String mapSqlTypeToJava(String sqlType) {
        String upperType = sqlType.toUpperCase();
        
        if (upperType.contains("INT")) {
            if (upperType.contains("BIGINT")) {
                return "Long";
            } else {
                return "Integer";
            }
        } else if (upperType.contains("VARCHAR") || upperType.contains("CHAR") || upperType.contains("TEXT")) {
            return "String";
        } else if (upperType.contains("DATE") || upperType.contains("TIME")) {
            return "Date";
        } else if (upperType.contains("DECIMAL") || upperType.contains("NUMERIC")) {
            return "BigDecimal";
        } else if (upperType.contains("DOUBLE")) {
            return "Double";
        } else if (upperType.contains("FLOAT")) {
            return "Float";
        } else if (upperType.contains("BOOLEAN") || upperType.contains("BIT")) {
            return "Boolean";
        } else {
            return "Object";
        }
    }

    /**
     * SQL类型映射到JDBC类型
     */
    private String mapSqlTypeToJdbc(String sqlType) {
        String upperType = sqlType.toUpperCase();
        
        if (upperType.contains("INT")) {
            if (upperType.contains("BIGINT")) {
                return "BIGINT";
            } else {
                return "INTEGER";
            }
        } else if (upperType.contains("VARCHAR")) {
            return "VARCHAR";
        } else if (upperType.contains("CHAR")) {
            return "CHAR";
        } else if (upperType.contains("TEXT")) {
            return "LONGVARCHAR";
        } else if (upperType.contains("DATE")) {
            return "DATE";
        } else if (upperType.contains("TIME")) {
            return "TIME";
        } else if (upperType.contains("DATETIME")) {
            return "TIMESTAMP";
        } else if (upperType.contains("TIMESTAMP")) {
            return "TIMESTAMP";
        } else if (upperType.contains("DECIMAL") || upperType.contains("NUMERIC")) {
            return "DECIMAL";
        } else if (upperType.contains("DOUBLE")) {
            return "DOUBLE";
        } else if (upperType.contains("FLOAT")) {
            return "FLOAT";
        } else if (upperType.contains("BOOLEAN") || upperType.contains("BIT")) {
            return "BIT";
        } else {
            return "OTHER";
        }
    }

    /**
     * 驼峰命名转换
     */
    private String toCamelCase(String str, boolean capitalizeFirst) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = capitalizeFirst;
        
        for (char c : str.toCharArray()) {
            if (c == '_' || c == '-') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        
        return sb.toString();
    }

    /**
     * 创建目录
     */
    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 处理模板
     */
    private void processTemplate(String templateName, Map<String, Object> dataModel, String outputPath) throws Exception {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        
        Template template = cfg.getTemplate(templateName);
        
        createDirectory(new File(outputPath).getParent());
        
        try (Writer writer = new FileWriter(outputPath)) {
            template.process(dataModel, writer);
        }
    }

    /**
     * 生成实体类
     */
    private void generateEntity(TableInfo tableInfo, String srcPath) throws Exception {
        String packagePath = srcPath + tableInfo.getPackageName().replace(".", "/") + "/entity/";
        createDirectory(packagePath);

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("tableName", tableInfo.getTableName());
        dataModel.put("columns", tableInfo.getColumns());

        processTemplate("entity.ftl", dataModel, packagePath + tableInfo.getEntityName() + ".java");
    }

    /**
     * 生成Mapper接口
     */
    private void generateMapper(TableInfo tableInfo, String srcPath) throws Exception {
        String packagePath = srcPath + tableInfo.getPackageName().replace(".", "/") + "/mapper/";
        createDirectory(packagePath);

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());

        processTemplate("mapper.ftl", dataModel, packagePath + tableInfo.getEntityName() + "Mapper.java");
    }

    /**
     * 生成Mapper XML文件
     */
    private void generateMapperXml(TableInfo tableInfo, String resourcePath) throws Exception {
        String xmlPath = resourcePath + "mapper/";
        createDirectory(xmlPath);

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("tableName", tableInfo.getTableName());
        dataModel.put("columns", tableInfo.getColumns());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());

        processTemplate("mapper-xml.ftl", dataModel, xmlPath + tableInfo.getEntityName() + "Mapper.xml");
    }

    /**
     * 生成Service接口
     */
    private void generateService(TableInfo tableInfo, String srcPath) throws Exception {
        String servicePackagePath = srcPath + tableInfo.getPackageName().replace(".", "/") + "/service/";
        createDirectory(servicePackagePath);

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());

        processTemplate("service.ftl", dataModel, servicePackagePath + "I" + tableInfo.getEntityName() + "Service.java");
    }

    /**
     * 生成Service实现类
     */
    private void generateServiceImpl(TableInfo tableInfo, String srcPath) throws Exception {
        String serviceImplPackagePath = srcPath + tableInfo.getPackageName().replace(".", "/") + "/service/impl/";
        createDirectory(serviceImplPackagePath);
        
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());

        processTemplate("service-impl.ftl", dataModel, serviceImplPackagePath + tableInfo.getEntityName() + "ServiceImpl.java");
    }

    /**
     * 生成Controller
     */
    private void generateController(TableInfo tableInfo, String srcPath) throws Exception {
        String packagePath = srcPath + tableInfo.getPackageName().replace(".", "/") + "/controller/";
        createDirectory(packagePath);

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());

        processTemplate("controller.ftl", dataModel, packagePath + tableInfo.getEntityName() + "Controller.java");
    }
    
    /**
     * 生成CRUD代码并返回zip字节数组
     * 
     * @param request 生成请求参数
     * @return zip字节数组
     */
    public byte[] generateCodeAsZip(GeneratorRequest request) {
        try {
            String tableName = request.getTableName();
            String packageName = request.getPackageName();
            
            // 获取输出目录，用于zip文件名
            String outputDir = request.getOutputDir();
            if (outputDir == null || outputDir.trim().isEmpty()) {
                outputDir = "generated-code";
            }
            
            // 解析表结构（使用动态数据库连接）
            TableInfo tableInfo = parseTableInfo(request, tableName, packageName);
            
            // 使用字节数组输出流来收集所有文件内容
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                
                // 生成各个文件并添加到zip
                addFileToZip(zos, tableInfo, packageName);
                
                zos.finish();
            }
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("生成代码zip失败", e);
            throw new RuntimeException("生成代码zip失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将生成的文件添加到zip流中
     * 
     * @param zos zip输出流
     * @param tableInfo 表信息
     * @param packageName 包名
     */
    private void addFileToZip(ZipOutputStream zos, TableInfo tableInfo, String packageName) throws Exception {
        // 生成文件内容到内存中，然后添加到zip
        
        // 生成Entity
        String entityContent = generateFileContent("entity.ftl", createEntityDataModel(tableInfo));
        String entityPath = packageName.replace(".", "/") + "/entity/" + tableInfo.getEntityName() + ".java";
        addToZip(zos, "src/main/java/" + entityPath, entityContent);
        
        // 生成Mapper
        String mapperContent = generateFileContent("mapper.ftl", createMapperDataModel(tableInfo));
        String mapperPath = packageName.replace(".", "/") + "/mapper/" + tableInfo.getEntityName() + "Mapper.java";
        addToZip(zos, "src/main/java/" + mapperPath, mapperContent);
        
        // 生成Service
        String serviceContent = generateFileContent("service.ftl", createServiceDataModel(tableInfo));
        String servicePath = packageName.replace(".", "/") + "/service/I" + tableInfo.getEntityName() + "Service.java";
        addToZip(zos, "src/main/java/" + servicePath, serviceContent);
        
        // 生成ServiceImpl
        String serviceImplContent = generateFileContent("service-impl.ftl", createServiceImplDataModel(tableInfo));
        String serviceImplPath = packageName.replace(".", "/") + "/service/impl/" + tableInfo.getEntityName() + "ServiceImpl.java";
        addToZip(zos, "src/main/java/" + serviceImplPath, serviceImplContent);
        
        // 生成Controller
        String controllerContent = generateFileContent("controller.ftl", createControllerDataModel(tableInfo));
        String controllerPath = packageName.replace(".", "/") + "/controller/" + tableInfo.getEntityName() + "Controller.java";
        addToZip(zos, "src/main/java/" + controllerPath, controllerContent);
        
        // 生成Mapper XML
        String mapperXmlContent = generateFileContent("mapper-xml.ftl", createMapperXmlDataModel(tableInfo));
        String mapperXmlPath = "mapper/" + tableInfo.getEntityName() + "Mapper.xml";
        addToZip(zos, "src/main/resources/" + mapperXmlPath, mapperXmlContent);
    }
    
    /**
     * 将内容添加到zip流中
     * 
     * @param zos zip输出流
     * @param path 文件路径
     * @param content 文件内容
     */
    private void addToZip(ZipOutputStream zos, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zos.putNextEntry(entry);
        zos.write(content.getBytes("UTF-8"));
        zos.closeEntry();
    }
    
    /**
     * 生成文件内容到字符串
     * 
     * @param templateName 模板名称
     * @param dataModel 数据模型
     * @return 文件内容
     */
    private String generateFileContent(String templateName, Map<String, Object> dataModel) throws Exception {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        
        Template template = cfg.getTemplate(templateName);
        
        StringWriter stringWriter = new StringWriter();
        template.process(dataModel, stringWriter);
        
        return stringWriter.toString();
    }
    
    // 创建各种数据模型的辅助方法
    private Map<String, Object> createEntityDataModel(TableInfo tableInfo) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("tableName", tableInfo.getTableName());
        dataModel.put("columns", tableInfo.getColumns());
        return dataModel;
    }
    
    private Map<String, Object> createMapperDataModel(TableInfo tableInfo) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());
        return dataModel;
    }
    
    private Map<String, Object> createServiceDataModel(TableInfo tableInfo) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());
        return dataModel;
    }
    
    private Map<String, Object> createServiceImplDataModel(TableInfo tableInfo) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());
        return dataModel;
    }
    
    private Map<String, Object> createControllerDataModel(TableInfo tableInfo) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());
        return dataModel;
    }
    
    private Map<String, Object> createMapperXmlDataModel(TableInfo tableInfo) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", tableInfo.getPackageName());
        dataModel.put("entityName", tableInfo.getEntityName());
        dataModel.put("entityVarName", tableInfo.getEntityVarName());
        dataModel.put("tableName", tableInfo.getTableName());
        dataModel.put("columns", tableInfo.getColumns());
        dataModel.put("primaryKey", tableInfo.getPrimaryKey());
        return dataModel;
    }
}
