/**
 *    Copyright 2006-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * 批量插入
 * Created by Exception on 2017/6/19.
 */
public class InsertBatchPlugin extends PluginAdapter {

    private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd;

    public InsertBatchPlugin() {
        elementsToAdd = new HashMap<FullyQualifiedTable, List<XmlElement>>();
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }


    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
        IntrospectedTable introspectedTable) {
        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
            addInsertBatchJava(method, interfaze, introspectedTable);
        }
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element,
        IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("insert");
        answer.addAttribute(new Attribute("id","insertBatch"));
        FullyQualifiedJavaType parameterType = introspectedTable.getRules()
            .calculateAllFieldsClass();
        answer.addAttribute(new Attribute("parameterType",parameterType.getFullyQualifiedName()));

        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        insertClause.append("insert into ");
        insertClause.append(introspectedTable
            .getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" (");

        valuesClause.append("values \n");
        valuesClause.append("<foreach collection=\"list\" index=\"index\" item=\"item\" separator=\",\" >\n");
        valuesClause.append("(\n<trim suffixOverrides=\",\" >\n");


        List<String> valuesClauses = new ArrayList<String>();
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        for (int i = 0; i < columns.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);

            insertClause.append(MyBatis3FormattingUtilities
                .getEscapedColumnName(introspectedColumn));
            valuesClause.append(MyBatis3FormattingUtilities
                .getParameterClause(introspectedColumn,"item."));
            if (i + 1 < columns.size()) {
                insertClause.append(", ");
                valuesClause.append(", ");
            }

            if (valuesClause.length() > 80) {
                answer.addElement(new TextElement(insertClause.toString()));
                insertClause.setLength(0);
                OutputUtilities.xmlIndent(insertClause, 1);

                valuesClauses.add(valuesClause.toString());
                valuesClause.setLength(0);
                OutputUtilities.xmlIndent(valuesClause, 1);
            }
        }

        insertClause.append(')');
        answer.addElement(new TextElement(insertClause.toString()));

        valuesClause.append("\n</trim>\n");
        valuesClause.append(')');
        valuesClause.append("</foreach>\n");

        valuesClauses.add(valuesClause.toString());

        for (String clause : valuesClauses) {
            answer.addElement(new TextElement(clause));
        }
        FullyQualifiedTable fqt = introspectedTable.getFullyQualifiedTable();
        List<XmlElement> elements = elementsToAdd.get(fqt);
        if (elements == null) {
            elements = new ArrayList<XmlElement>();
            elementsToAdd.put(fqt, elements);
        }
        elements.add(answer);
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

    private void addInsertBatchJava(Method method, Interface interfaze,
        IntrospectedTable introspectedTable) {
        Method newMethod = new Method("insertBatch");
        newMethod.setReturnType(FullyQualifiedJavaType.getIntInstance());
        newMethod.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType parameterType = FullyQualifiedJavaType.getNewListInstance();
        parameterType.addTypeArgument(new FullyQualifiedJavaType(
            introspectedTable.getBaseRecordType()));
        newMethod.addParameter(new Parameter(parameterType, "records"));
        interfaze.addMethod(newMethod);
    }


}
