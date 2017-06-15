package com.pocketdigi.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * 字段注释
 * Created by Exception on 2017/6/2.
 */
public class FieldCommentPlugin extends PluginAdapter {
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if(introspectedColumn.getRemarks()!=null&&!introspectedColumn.getRemarks().equals("")) {
            field.addJavaDocLine("/** "+introspectedColumn.getRemarks()+" **/");
        }
        return true;

    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if(introspectedColumn.getRemarks()!=null&&!introspectedColumn.getRemarks().equals("")) {
            method.addJavaDocLine("/** "+introspectedColumn.getRemarks()+" **/");
        }
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if(introspectedColumn.getRemarks()!=null&&!introspectedColumn.getRemarks().equals("")) {
            method.addJavaDocLine("/** "+introspectedColumn.getRemarks()+" **/");
        }
        return true;
    }
}
