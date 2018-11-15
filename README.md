MyBatis Generator 增强版
=======================

基于Mybatis Generator 1.3.5，未修改官方代码，所有特性使用插件实现。已加mysql驱动，不需要单独加载。
没在mysql外的其他数据库上使用过，请谨慎。


### 特性
1. 字段、getter、setter注释。自动读取表字段注释，加到Model类上。
2. 逻辑删除。可以设置逻辑删除字段、删除时的值、未删除时的值。
3. Mysql物理分页。使用offset limit分页
4. 通过Example找出一条数据
5. 批量插入

### 使用方法

1. 注释

        <plugin type="com.pocketdigi.generator.plugins.FieldCommentPlugin" />
 
2. 逻辑删除
    
        <plugin type="com.pocketdigi.generator.plugins.LogicallyDeletePlugin" >
            <property name="column" value="is_deleted" />
            <property name="deletedValue" value="Y" />
            <property name="unDeletedValue" value="N" />
        </plugin>
        
      使用Example查找未删除的数据，通过orNotDeleted方法创建Criteria
            
            example.orNotDeleted();
    
3. 物理分页

       <plugin type="com.pocketdigi.generator.plugins.MySQLPagingPlugin" />

4. SelectOneByExample

        <plugin type="com.pocketdigi.generator.plugins.SelectOneByExamplePlugin" />
        
5. 批量插入
    
        <plugin type="com.pocketdigi.generator.plugins.InsertBatchPlugin" />