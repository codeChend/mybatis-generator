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

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.*;

/**
 * 逻辑删除
 * Created by Exception on 2017/6/2.
 */
public class LogicallyDeletePlugin extends PluginAdapter {
    private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd;

    private String column,deletedValue,unDeletedValue;


    public LogicallyDeletePlugin() {
        elementsToAdd = new HashMap<FullyQualifiedTable, List<XmlElement>>();
    }
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        try {
            column = properties.get("column").toString();
            deletedValue = properties.get("deletedValue").toString();
            unDeletedValue = properties.get("unDeletedValue").toString();
        }catch (Exception e) {
            throw new IllegalArgumentException("please add column,deletedValue,unDeletedValue properties");
        }
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        IntrospectedColumn deletedColumn = introspectedTable.getColumn(this.column);
        if(deletedColumn==null) {
            return true;
        }
        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            addDeleteByPrimaryKeyJava(method, interfaze, introspectedTable);
            addDeleteByExampleJava(method, interfaze, introspectedTable);
        }
        return true;
    }
    @Override
    public boolean sqlMapDocumentGenerated(Document document,
                                           IntrospectedTable introspectedTable) {
        IntrospectedColumn deletedColumn = introspectedTable.getColumn(this.column);
        if(deletedColumn==null)
            return true;
        List<XmlElement> elements = elementsToAdd.get(introspectedTable.getFullyQualifiedTable());
        if (elements != null) {
            for (XmlElement element : elements) {
                document.getRootElement().addElement(element);
            }
        }

        return true;
    }
    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        IntrospectedColumn deletedColumn = introspectedTable.getColumn(this.column);
        if(deletedColumn==null)
            return true;

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            addDeleteByPrimaryKeyXml(introspectedTable.getFullyQualifiedTable(),introspectedTable);
            addDeletedByExampleXml(introspectedTable.getFullyQualifiedTable(),introspectedTable);
        }
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        IntrospectedColumn deletedColumn = introspectedTable.getColumn(this.column);
        if(deletedColumn==null)
            return true;

        Method newMethod = new Method("orNotDeleted");
        List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
        FullyQualifiedJavaType returnType=null;
        for(InnerClass innerClass:innerClasses) {
            FullyQualifiedJavaType type = innerClass.getType();
            if(type.getShortName().equals("Criteria")) {
                returnType=type;
            }
        }
        newMethod.setReturnType(returnType);
        newMethod.setVisibility(JavaVisibility.PUBLIC);
        newMethod.addBodyLine("Criteria criteria = createCriteriaInternal();");
        String javaProperty = deletedColumn.getJavaProperty();
        javaProperty=javaProperty.substring(0, 1).toUpperCase() + javaProperty.substring(1);
        if(deletedColumn.isStringColumn()){
            newMethod.addBodyLine("criteria.and"+ javaProperty +"EqualTo(\""+unDeletedValue+"\");");
        }else{
            if(deletedColumn.isByteColumn()) {
                newMethod.addBodyLine("criteria.and"+ javaProperty +"EqualTo((byte)"+unDeletedValue+");");
            }else if(deletedColumn.isIntegerColumn()){
                newMethod.addBodyLine("criteria.and"+ javaProperty +"EqualTo("+unDeletedValue+");");
            }else{
                throw new IllegalArgumentException("Not support logically delete type \""+deletedColumn.getJdbcTypeName()+"\"");
            }
        }
        newMethod.addBodyLine("oredCriteria.add(criteria);");
        newMethod.addBodyLine("return criteria;");
        topLevelClass.addMethod(newMethod);

//
//        oredCriteria.add(criteria);
//        return criteria;

//        topLevelClass.addMethod();
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    private void addDeleteByPrimaryKeyJava(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
         FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(
                introspectedTable.getExampleType());
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method newMethod = new Method("logicallyDeleteByPrimaryKey");


        List<IntrospectedColumn> introspectedColumns = introspectedTable
                .getPrimaryKeyColumns();
        boolean annotate = introspectedColumns.size() > 1;
        if (annotate) {
            importedTypes.add(new FullyQualifiedJavaType(
                    "org.apache.ibatis.annotations.Param")); //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn
                    .getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn
                    .getJavaProperty());
            if (annotate) {
                sb.setLength(0);
                sb.append("@Param(\""); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\")"); //$NON-NLS-1$
                parameter.addAnnotation(sb.toString());
            }
            newMethod.addParameter(parameter);
        }

        interfaze.addImportedTypes(importedTypes);
        newMethod.setReturnType(new FullyQualifiedJavaType("int")); //$NON-NLS-1$
        interfaze.addMethod(newMethod);
    }


    private void addDeleteByExampleJava(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(
                introspectedTable.getExampleType());
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method newMethod = new Method("logicallyDeleteByExample");


        newMethod.setVisibility(JavaVisibility.PUBLIC);
        newMethod.setReturnType(new FullyQualifiedJavaType("int")); //$NON-NLS-1$
        interfaze.addImportedTypes(importedTypes);
        newMethod.addParameter(new Parameter(exampleType, "example")); //$NON-NLS-1$
        interfaze.addMethod(newMethod);
    }



    /**
     * 根据主键逻辑删除
     * @param fqt
     * @param introspectedTable
     */
    private void addDeleteByPrimaryKeyXml(FullyQualifiedTable fqt, IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("update");
        answer.addAttribute(new Attribute("id","logicallyDeleteByPrimaryKey"));
//        answer.addAttribute(new Attribute("resultType", "java.lang.Long"));
        String parameterType;
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            parameterType = introspectedTable.getPrimaryKeyType();
        } else {
            // PK fields are in the base class. If more than on PK
            // field, then they are coming in a map.
            if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
                parameterType = "map"; //$NON-NLS-1$
            } else {
                parameterType = introspectedTable.getPrimaryKeyColumns().get(0)
                        .getFullyQualifiedJavaType().toString();
            }
        }
        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                parameterType));

        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());

        IntrospectedColumn deletedColumn = introspectedTable.getColumn(this.column);

        if(deletedColumn.isStringColumn()){
            sb.append(" set ").append(column).append("=").append("'").append(deletedValue).append("'");
        }else if(deletedColumn.isIntegerColumn()||deletedColumn.isByteColumn()) {
            sb.append(" set ").append(column).append("=").append(deletedValue);
        }

        answer.addElement(new TextElement(sb.toString()));
        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable
                .getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and "); //$NON-NLS-1$
            } else {
                sb.append("where "); //$NON-NLS-1$
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities
                    .getParameterClause(introspectedColumn));
            answer.addElement(new TextElement(sb.toString()));
        }
        List<XmlElement> elements = elementsToAdd.get(fqt);
        if (elements == null) {
            elements = new ArrayList<XmlElement>();
            elementsToAdd.put(fqt, elements);
        }
        elements.add(answer);
    }

    private void addDeletedByExampleXml(FullyQualifiedTable fqt, IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("update");
        answer.addAttribute(new Attribute("id","logicallyDeleteByExample"));

        answer.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType())); //$NON-NLS-1$
//        answer.addAttribute(new Attribute("resultType", "java.lang.Long")); //$NON-NLS-1$ //$NON-NLS-2$
        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());

        IntrospectedColumn deletedColumn = introspectedTable.getColumn(this.column);
        if(deletedColumn.isStringColumn()){
            sb.append(" set ").append(column).append("=").append("'").append(deletedValue).append("'");
        }else if(deletedColumn.isIntegerColumn()||deletedColumn.isByteColumn()) {
            sb.append(" set ").append(column).append("=").append(deletedValue);
        }
        answer.addElement(new TextElement(sb.toString()));

        answer.addElement(getExampleIncludeElement(introspectedTable));

        List<XmlElement> elements = elementsToAdd.get(fqt);
        if (elements == null) {
            elements = new ArrayList<XmlElement>();
            elementsToAdd.put(fqt, elements);
        }
        elements.add(answer);

    }


    protected XmlElement getExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "_parameter != null")); //$NON-NLS-1$ //$NON-NLS-2$

        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        includeElement.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }

}
