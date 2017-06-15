package com.pocketdigi.generator.plugins;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * MySQL分页插件
 * Created by Exception on 2017/6/1.
 */
public class MySQLPagingPlugin extends PluginAdapter {

    private FullyQualifiedJavaType offset, limit;
    private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd;

    public MySQLPagingPlugin() {
        offset = new FullyQualifiedJavaType("int");
        limit = new FullyQualifiedJavaType("int");
        elementsToAdd = new HashMap<FullyQualifiedTable, List<XmlElement>>();
    }

    public boolean validate(List<String> warnings) {
        return true;
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

}
