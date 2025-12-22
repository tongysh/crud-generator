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

}
