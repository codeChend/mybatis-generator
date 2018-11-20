MyBatis Generator 增强版
=======================

基于Mybatis Generator 1.3.5，所有功能都使用插件实现，但为了修复sonar扫描出来的坏味道，修改了部分源码。
没在mysql外的其他数据库上使用过，请谨慎。


### 特性
1. 字段、getter、setter注释。自动读取表字段注释，加到Model类上。
2. 逻辑删除。可以设置逻辑删除字段、删除时的值、未删除时的值。支持数字和字符类型(2018.11.16)。
3. Mysql物理分页。使用offset limit分页。增加PageHelper,一行代码直接出分页信息(currentPage，totalPage，pageSize，totalCount，dataList)(2018.11.16)
4. 通过Example找出一条数据
5. 批量插入
6. 优化生成的代码，去除部分sonar标记的'坏味道'
### 使用方法

1. 注释
 
``` xml
    <plugin type="com.pocketdigi.generator.plugins.FieldCommentPlugin" />

```
 
2. 逻辑删除

``` xml
      <plugin type="com.pocketdigi.generator.plugins.LogicallyDeletePlugin" >
          <property name="column" value="is_deleted" />
          <property name="deletedValue" value="1" />
          <property name="unDeletedValue" value="0" />
      </plugin>
```
        
使用Example查找未删除的数据，通过orNotDeleted方法创建`example.orNotDeleted();`
    
3. 物理分页

``` xml
      <plugin type="com.pocketdigi.generator.plugins.MySQLPagingPlugin" >
          <property name="pageHelperPackage" value="com.pocketdigi.demo.dal.page" />
      </plugin>
```
`pageHelperPackage`属性配置`PageHelper`生成的包名，如果不配置这个属性，默认不生成`PageHelper`

spring boot配置PageHelper Bean：

``` java
    @Bean
    public PageHelper pageHelper(SqlSessionFactory sessionFactory){
        return new PageHelper(sessionFactory);
    }
```

使用：

``` java
 ConfigExample example = new ConfigExample();
 example.orNotDeleted();
 Page<Config> configPage = pageHelper.selectByExamplePaging(example, 1, 2);
 
```

Page.java:

``` java
public final class Page<T> {
    private Integer currentPage;

    private Integer totalPage;

    private Integer pageSize;

    private Long totalCount;

    private List<T> dataList;
}
```

4. SelectOneByExample

``` xml
<plugin type="com.pocketdigi.generator.plugins.SelectOneByExamplePlugin" />
```
        
5. 批量插入
    
``` xml
<plugin type="com.pocketdigi.generator.plugins.InsertBatchPlugin" />
```
        