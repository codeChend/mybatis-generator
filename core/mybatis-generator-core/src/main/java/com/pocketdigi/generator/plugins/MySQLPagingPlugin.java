/**
 *    Copyright 2006-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.pocketdigi.generator.plugins;

import java.util.Properties;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * MySQL分页插件
 * Created by Exception on 2017/6/1.
 */
public class MySQLPagingPlugin extends PluginAdapter {

    private FullyQualifiedJavaType offset, limit;
    private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd;
    private String pageHelperPackage;

    public MySQLPagingPlugin() {
        offset = new FullyQualifiedJavaType("int");
        limit = new FullyQualifiedJavaType("int");
        elementsToAdd = new HashMap<FullyQualifiedTable, List<XmlElement>>();
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        Object pageHelperPackageProp = properties.get("pageHelperPackage");
        if(pageHelperPackageProp!=null){
            //配置了PageHelper
            this.pageHelperPackage=pageHelperPackageProp.toString();
        }
    }


    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        if(pageHelperPackage!=null&& pageHelperPackage.length()>0) {
            List<GeneratedJavaFile> javaFileList=new ArrayList<GeneratedJavaFile>(2);
            javaFileList.add(pageJavaFile());
            javaFileList.add(pageHelperJavaFile());
            return javaFileList;
        }
        return null;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(
            Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            copyAndAddMethodWithoutBLOBs(method, interfaze, introspectedTable);
        }
        return true;
    }

    private void copyAndAddMethodWithoutBLOBs(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(
                introspectedTable.getExampleType());
        Method newMethod = new Method("selectByExamplePaging");
        newMethod.addParameter(new Parameter(exampleType, "example", "@Param(\"example\")"));
        newMethod.addParameter(new Parameter(offset, "offset", "@Param(\"offset\")"));
        newMethod.addParameter(new Parameter(limit, "limit", "@Param(\"limit\")"));
        FullyQualifiedJavaType returnType = FullyQualifiedJavaType
                .getNewListInstance();
        FullyQualifiedJavaType fqjt;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            fqjt = new FullyQualifiedJavaType(introspectedTable
                    .getRecordWithBLOBsType());
        } else {
            // the blob fields must be rolled up into the base class
            fqjt = new FullyQualifiedJavaType(introspectedTable
                    .getBaseRecordType());
        }

        interfaze.addImportedType(fqjt);
        returnType.addTypeArgument(fqjt);
        newMethod.setReturnType(returnType);


        interfaze.addMethod(newMethod);
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            copyAndSaveElementWithoutBLOBs(element, introspectedTable.getFullyQualifiedTable(),introspectedTable);
        }
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document,
                                           IntrospectedTable introspectedTable) {
        List<XmlElement> elements = elementsToAdd.get(introspectedTable.getFullyQualifiedTable());
        if (elements != null) {
            for (XmlElement element : elements) {
                document.getRootElement().addElement(element);
            }
        }

        return true;
    }

    private void copyAndSaveElementWithoutBLOBs(XmlElement element, FullyQualifiedTable fqt, IntrospectedTable introspectedTable) {

        String fqjt = introspectedTable.getExampleType();

        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", //$NON-NLS-1$
                "selectByExamplePaging"));
        answer.addAttribute(new Attribute(
                "resultMap", introspectedTable.getBaseResultMapId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("select")); //$NON-NLS-1$
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "distinct")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("distinct")); //$NON-NLS-1$
        answer.addElement(ifElement);

        StringBuilder sb = new StringBuilder();
        if (stringHasValue(introspectedTable
                .getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByExampleQueryId());
            sb.append("' as QUERYID,"); //$NON-NLS-1$
            answer.addElement(new TextElement(sb.toString()));
        }
        answer.addElement(getBaseColumnListElement(introspectedTable));

        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(introspectedTable
                .getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        answer.addElement(getUpdateByExampleIncludeElement(introspectedTable));

        ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "example.orderByClause != null")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("order by ${example.orderByClause}")); //$NON-NLS-1$
        answer.addElement(ifElement);
        answer.addElement(new TextElement("limit ${offset},${limit}"));


        List<XmlElement> elements = elementsToAdd.get(fqt);
        if (elements == null) {
            elements = new ArrayList<XmlElement>();
            elementsToAdd.put(fqt, elements);
        }
        elements.add(answer);
    }


    protected XmlElement getBaseColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getBaseColumnListId()));
        return answer;
    }


    protected XmlElement getUpdateByExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "_parameter != null")); //$NON-NLS-1$ //$NON-NLS-2$

        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        includeElement.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }


    private GeneratedJavaFile pageJavaFile() {
        FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(pageHelperPackage+".Page");
        fullyQualifiedJavaType.addTypeArgument(new FullyQualifiedJavaType("T"));
        TopLevelClass topLevelClass=new TopLevelClass(fullyQualifiedJavaType);
        topLevelClass.setFinal(true);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.List"));

        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" * 由Mybatis Generator增强版生成，不要手动修改");
        topLevelClass.addJavaDocLine(" * @see <a href=\"https://github.com/pocketdigi/mybatis-generator\">https://github.com/pocketdigi/mybatis-generator</a>");
        topLevelClass.addJavaDocLine(" * @author Exception");
        topLevelClass.addJavaDocLine(" */");

        //currentPage
        Field currentPageField=new Field();
        currentPageField.setName("currentPage");
        currentPageField.setType(new FullyQualifiedJavaType("java.lang.Integer"));
        currentPageField.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(currentPageField);
        topLevelClass.addMethod(getJavaBeansGetter(currentPageField));
        topLevelClass.addMethod(getJavaBeansSetter(currentPageField));

        //totalPage
        Field totalPage=new Field();
        totalPage.setName("totalPage");
        totalPage.setType(new FullyQualifiedJavaType("java.lang.Integer"));
        totalPage.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(totalPage);
        topLevelClass.addMethod(getJavaBeansGetter(totalPage));
        topLevelClass.addMethod(getJavaBeansSetter(totalPage));


        //pageSize
        Field pageSize=new Field();
        pageSize.setName("pageSize");
        pageSize.setType(new FullyQualifiedJavaType("java.lang.Integer"));
        pageSize.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(pageSize);
        topLevelClass.addMethod(getJavaBeansGetter(pageSize));
        topLevelClass.addMethod(getJavaBeansSetter(pageSize));

        //currentPage
        Field totalCount=new Field();
        totalCount.setName("totalCount");
        totalCount.setType(new FullyQualifiedJavaType("java.lang.Long"));
        totalCount.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(totalCount);
        topLevelClass.addMethod(getJavaBeansGetter(totalCount));
        topLevelClass.addMethod(getJavaBeansSetter(totalCount));

        //currentPage
        Field dataList=new Field();
        dataList.setName("dataList");
        FullyQualifiedJavaType newListInstance = FullyQualifiedJavaType.getNewListInstance();
        newListInstance.addTypeArgument(new FullyQualifiedJavaType("T"));
        dataList.setType(newListInstance);
        dataList.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(dataList);

        topLevelClass.addMethod(getJavaBeansGetter(dataList));
        topLevelClass.addMethod(getJavaBeansSetter(dataList));

        String targetProject=context.getJavaClientGeneratorConfiguration().getTargetProject();
        String encoding = context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING);

        return new GeneratedJavaFile(topLevelClass,targetProject,encoding,context.getJavaFormatter());
    }

    private GeneratedJavaFile pageHelperJavaFile() {
        FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(pageHelperPackage+".PageHelper");
        TopLevelClass topLevelClass=new TopLevelClass(fullyQualifiedJavaType);
        topLevelClass.setFinal(true);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.session.SqlSession"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.session.SqlSessionFactory"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(context.getJavaModelGeneratorConfiguration().getTargetPackage()+".*"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(context.getJavaClientGeneratorConfiguration().getTargetPackage()+".*"));
        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" * 由Mybatis Generator增强版生成，不要手动修改");
        topLevelClass.addJavaDocLine(" * @see <a href=\"https://github.com/pocketdigi/mybatis-generator\">https://github.com/pocketdigi/mybatis-generator</a>");
        topLevelClass.addJavaDocLine(" * @author Exception");
        topLevelClass.addJavaDocLine(" */");

        //sessionFactory
        Field sessionFactory=new Field();
        sessionFactory.setName("sessionFactory");
        sessionFactory.setType(new FullyQualifiedJavaType("org.apache.ibatis.session.SqlSessionFactory"));
        sessionFactory.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(sessionFactory);

        //construct
        Method constructMethod=new Method();
        constructMethod.setVisibility(JavaVisibility.PUBLIC);
        constructMethod.setConstructor(true);
        constructMethod.setName(fullyQualifiedJavaType.getShortName());
        constructMethod.addParameter(new Parameter(sessionFactory.getType(),"sessionFactory"));
        constructMethod.addBodyLine("this.sessionFactory = sessionFactory;");
        topLevelClass.addMethod(constructMethod);

        List<TableConfiguration> tableConfigurations = context.getTableConfigurations();
        for(TableConfiguration tableConfiguration:tableConfigurations) {
            Method selectMethod=new Method();
            selectMethod.setVisibility(JavaVisibility.PUBLIC);
            String domainObjectName = tableConfiguration.getDomainObjectName();
            FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(pageHelperPackage+".Page");
            returnType.addTypeArgument(new FullyQualifiedJavaType(
                domainObjectName));
            selectMethod.setReturnType(returnType);
            selectMethod.setName("selectByExamplePaging");
            selectMethod.addParameter(new Parameter(new FullyQualifiedJavaType(
                domainObjectName +"Example"),"example"));
            selectMethod.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(),"currentPage"));
            selectMethod.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(),"pageSize"));
            selectMethod.addBodyLine("try(SqlSession sqlSession = sessionFactory.openSession()) {");
            String mapper=domainObjectName+"Mapper"+" mapper = "+"sqlSession.getMapper("+domainObjectName+"Mapper.class);";
            selectMethod.addBodyLine(mapper);
            selectMethod.addBodyLine("if(currentPage <= 0) {");
            selectMethod.addBodyLine("currentPage = 1;");
            selectMethod.addBodyLine("}");

            selectMethod.addBodyLine("if(pageSize <= 0) {");
            selectMethod.addBodyLine("pageSize = 10;");
            selectMethod.addBodyLine("}");
            selectMethod.addBodyLine("long totalCount = mapper.countByExample(example);");
            selectMethod.addBodyLine("List<"+domainObjectName+"> dataList = mapper.selectByExamplePaging(example, (currentPage - 1) * pageSize, pageSize);");
            selectMethod.addBodyLine("Page<"+domainObjectName+"> pageObj=new Page<>();");
            selectMethod.addBodyLine("pageObj.setCurrentPage(currentPage);");
            selectMethod.addBodyLine("pageObj.setPageSize(pageSize);");
            selectMethod.addBodyLine("pageObj.setDataList(dataList);");
            selectMethod.addBodyLine("pageObj.setTotalCount(totalCount);");
            selectMethod.addBodyLine("pageObj.setTotalPage((int)Math.ceil(totalCount/(float)pageSize));");
            selectMethod.addBodyLine("return pageObj;");
            selectMethod.addBodyLine("}");
            topLevelClass.addMethod(selectMethod);
        }


        String targetProject=context.getJavaClientGeneratorConfiguration().getTargetProject();
        String encoding = context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING);
        return new GeneratedJavaFile(topLevelClass,targetProject,encoding,context.getJavaFormatter());
    }



    private static Method getJavaBeansGetter(Field field) {
        String getter = JavaBeansUtil.getGetterMethodName(field.getName(),field.getType());
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(getter);
        StringBuilder sb = new StringBuilder();
        sb.append("return ");
        sb.append(field.getName());
        sb.append(';');
        method.addBodyLine(sb.toString());

        return method;
    }
    private static Method getJavaBeansSetter(Field field) {
        String getter = JavaBeansUtil.getSetterMethodName(field.getName());
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("void"));
        method.setName(getter);
        method.addParameter(new Parameter(field.getType(),field.getName()));
        StringBuilder sb = new StringBuilder();
        sb.append("this.");
        sb.append(field.getName());
        sb.append(" = ");
        sb.append(field.getName()).append(";");
        method.addBodyLine(sb.toString());

        return method;
    }

}
