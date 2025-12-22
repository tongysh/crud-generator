package com.tongysh.generator.service;

import com.tongysh.generator.dto.DbConnectionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库服务
 * 
 * @author tongysh
 */
@Slf4j
@Service
public class DatabaseService {

    /**
     * 获取数据库列表
     */
    public List<String> getDatabases(DbConnectionRequest request) throws Exception {
        List<String> databases = new ArrayList<>();
        
        String url = request.getDbUrl();
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
            ResultSet rs = metaData.getCatalogs();
            
            while (rs.next()) {
                String dbName = rs.getString("TABLE_CAT");
                // 过滤系统数据库
                if (!dbName.equals("information_schema") 
                    && !dbName.equals("mysql") 
                    && !dbName.equals("performance_schema") 
                    && !dbName.equals("sys")) {
                    databases.add(dbName);
                }
            }
            rs.close();
        }
        
        return databases;
    }

    /**
     * 获取指定数据库的表列表
     */
    public List<String> getTables(DbConnectionRequest request) throws Exception {
        List<String> tables = new ArrayList<>();
        
        String url = request.getDbUrl();
        // 添加数据库名
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
            ResultSet rs = metaData.getTables(request.getDatabaseName(), null, "%", new String[]{"TABLE"});
            
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tables.add(tableName);
            }
            rs.close();
        }
        
        return tables;
    }
}
