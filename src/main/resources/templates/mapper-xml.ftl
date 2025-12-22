<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${packageName}.mapper.${entityName}Mapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="${packageName}.entity.${entityName}">
<#list columns as column>
        <#if column.primaryKey>
        <id column="${column.columnName}" jdbcType="${column.jdbcType}" property="${column.javaFieldName}" />
        <#else>
        <result column="${column.columnName}" jdbcType="${column.jdbcType}" property="${column.javaFieldName}" />
        </#if>
</#list>
    </resultMap>

    <!-- 基础字段列 -->
    <sql id="Base_Column_List">
        <#list columns as column>${column.columnName}<#if column_has_next>, </#if></#list>
    </sql>

    <!-- 根据ID查询 -->
    <select id="selectById" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${tableName}
        WHERE ${primaryKey.columnName} = ${r"#{"}${primaryKey.javaFieldName}${r"}"}
    </select>

    <!-- 分页条件查询 -->
    <select id="selectByPage" resultMap="BaseResultMap" parameterType="map">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${tableName}
        <where>
<#list columns as column>
            <#if column.javaType == "String">
            <if test="${column.javaFieldName} != null and ${column.javaFieldName} != ''">
                AND ${column.columnName} LIKE CONCAT('%', ${r"#{"}${column.javaFieldName}${r"}"}, '%')
            </if>
            <#elseif column.javaType == "Integer" || column.javaType == "Long">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <#elseif column.javaType == "java.math.BigDecimal" || column.javaType == "Float" || column.javaType == "Double">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <if test="${column.javaFieldName}Min != null">
                AND ${column.columnName} &gt;= ${r"#{"}${column.javaFieldName}Min${r"}"}
            </if>
            <if test="${column.javaFieldName}Max != null">
                AND ${column.columnName} &lt;= ${r"#{"}${column.javaFieldName}Max${r"}"}
            </if>
            <#elseif column.javaType == "java.util.Date">
            <if test="${column.javaFieldName}Start != null">
                AND ${column.columnName} &gt;= ${r"#{"}${column.javaFieldName}Start${r"}"}
            </if>
            <if test="${column.javaFieldName}End != null">
                AND ${column.columnName} &lt;= ${r"#{"}${column.javaFieldName}End${r"}"}
            </if>
            <#elseif column.javaType == "Boolean">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <#else>
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            </#if>
</#list>
        </where>
        <if test="orderBy != null and orderBy != ''">
            ORDER BY ${r"${orderBy}"}
        </if>
        <if test="offset != null and limit != null">
            LIMIT ${r"#{offset}"}, ${r"#{limit}"}
        </if>
    </select>

    <!-- 条件查询所有记录（不分页） -->
    <select id="selectAll" resultMap="BaseResultMap" parameterType="map">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${tableName}
        <where>
<#list columns as column>
            <#if column.javaType == "String">
            <if test="${column.javaFieldName} != null and ${column.javaFieldName} != ''">
                AND ${column.columnName} LIKE CONCAT('%', ${r"#{"}${column.javaFieldName}${r"}"}, '%')
            </if>
            <#elseif column.javaType == "Integer" || column.javaType == "Long">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <#elseif column.javaType == "java.math.BigDecimal" || column.javaType == "Float" || column.javaType == "Double">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <if test="${column.javaFieldName}Min != null">
                AND ${column.columnName} &gt;= ${r"#{"}${column.javaFieldName}Min${r"}"}
            </if>
            <if test="${column.javaFieldName}Max != null">
                AND ${column.columnName} &lt;= ${r"#{"}${column.javaFieldName}Max${r"}"}
            </if>
            <#elseif column.javaType == "java.util.Date">
            <if test="${column.javaFieldName}Start != null">
                AND ${column.columnName} &gt;= ${r"#{"}${column.javaFieldName}Start${r"}"}
            </if>
            <if test="${column.javaFieldName}End != null">
                AND ${column.columnName} &lt;= ${r"#{"}${column.javaFieldName}End${r"}"}
            </if>
            <#elseif column.javaType == "Boolean">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <#else>
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            </#if>
</#list>
        </where>
        <if test="orderBy != null and orderBy != ''">
            ORDER BY ${r"${orderBy}"}
        </if>
    </select>

    <!-- 条件查询总数 -->
    <select id="countByCondition" resultType="int" parameterType="map">
        SELECT COUNT(*)
        FROM ${tableName}
        <where>
<#list columns as column>
            <#if column.javaType == "String">
            <if test="${column.javaFieldName} != null and ${column.javaFieldName} != ''">
                AND ${column.columnName} LIKE CONCAT('%', ${r"#{"}${column.javaFieldName}${r"}"}, '%')
            </if>
            <#elseif column.javaType == "Integer" || column.javaType == "Long">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <#elseif column.javaType == "java.math.BigDecimal" || column.javaType == "Float" || column.javaType == "Double">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <if test="${column.javaFieldName}Min != null">
                AND ${column.columnName} &gt;= ${r"#{"}${column.javaFieldName}Min${r"}"}
            </if>
            <if test="${column.javaFieldName}Max != null">
                AND ${column.columnName} &lt;= ${r"#{"}${column.javaFieldName}Max${r"}"}
            </if>
            <#elseif column.javaType == "java.util.Date">
            <if test="${column.javaFieldName}Start != null">
                AND ${column.columnName} &gt;= ${r"#{"}${column.javaFieldName}Start${r"}"}
            </if>
            <if test="${column.javaFieldName}End != null">
                AND ${column.columnName} &lt;= ${r"#{"}${column.javaFieldName}End${r"}"}
            </if>
            <#elseif column.javaType == "Boolean">
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            <#else>
            <if test="${column.javaFieldName} != null">
                AND ${column.columnName} = ${r"#{"}${column.javaFieldName}${r"}"}
            </if>
            </#if>
</#list>
        </where>
    </select>

    <!-- 插入数据 -->
    <insert id="insert" parameterType="${packageName}.entity.${entityName}"<#if primaryKey.javaType == "Long" || primaryKey.javaType == "Integer"> useGeneratedKeys="true" keyProperty="${primaryKey.javaFieldName}"</#if>>
        INSERT INTO ${tableName}
        <trim prefix="(" suffix=")" suffixOverrides=",">
<#list columns as column>
            <if test="${column.javaFieldName} != null">
                ${column.columnName},
            </if>
</#list>
        </trim>
        <trim prefix="VALUES (" suffix=")" suffixOverrides=",">
<#list columns as column>
            <if test="${column.javaFieldName} != null">
                ${r"#{"}${column.javaFieldName}, jdbcType=${column.jdbcType}${r"}"},
            </if>
</#list>
        </trim>
    </insert>

    <!-- 根据ID更新 -->
    <update id="updateById" parameterType="${packageName}.entity.${entityName}">
        UPDATE ${tableName}
        <set>
<#list columns as column>
    <#if !column.primaryKey>
            <if test="${column.javaFieldName} != null">
                ${column.columnName} = ${r"#{"}${column.javaFieldName}, jdbcType=${column.jdbcType}${r"}"},
            </if>
    </#if>
</#list>
        </set>
        WHERE ${primaryKey.columnName} = ${r"#{"}${primaryKey.javaFieldName}${r"}"}
    </update>

    <!-- 根据ID删除 -->
    <delete id="deleteById">
        DELETE FROM ${tableName}
        WHERE ${primaryKey.columnName} = ${r"#{"}${primaryKey.javaFieldName}${r"}"}
    </delete>

    <!-- 批量删除 -->
    <delete id="deleteBatchByIds">
        DELETE FROM ${tableName}
        WHERE ${primaryKey.columnName} IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            ${r"#{id}"}
        </foreach>
    </delete>

</mapper>
