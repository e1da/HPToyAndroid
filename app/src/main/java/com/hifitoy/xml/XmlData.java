/*
 *   XmlData.java
 *
 *   Created by Artem Khlyupin on 07/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.xml;

import android.net.Uri;
import org.xmlpull.v1.XmlSerializer;
import java.io.File;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XmlData {
    private static final String TAG = "IWoofer_XML";

    private String xmlHeader;
    private List<String> xmlStringList;

    XmlSerializer xmlSerializer;
    StringWriter writer;
    File file;
    Uri uri;

    public XmlData() {
        xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        xmlStringList = new LinkedList<String>();
    }

    public void setXmlHeader(String str) {
        xmlHeader = str;
    }
    public String getXmlHeader() {
        return xmlHeader;
    }

    public void clear(){
        xmlStringList.clear();
    }

    public int size() {
        return xmlStringList.size();
    }

    public void addString(String str){
        xmlStringList.add(str);
    }

    public String get(int index){
        if (index > size()) return null;
        return xmlStringList.get(index);
    }

    public String toString(){
        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append(xmlHeader);
        for (int i = 0; i < size(); i++){
            strBuilder.append(get(i));
        }

        return strBuilder.toString();
    }

    public void addXmlData(XmlData xmlData){
        if (xmlData == null) return;

        for (int i = 0; i < xmlData.size(); i++){
            addString(xmlData.get(i));
        }
    }


    public void addXmlElement(String name, XmlData value, Map<String, String> attrib, int level) {
        String levelStr = getLevelStr(level);
        String levelValueStr = getLevelStr(level + 1);
        String attribStr = getAttribStr(attrib);

        if (attribStr.length() > 0){
            if (value != null){
                addString(levelStr + "<" + name + " " + attribStr + ">\n");
            } else {
                addString(levelStr + "<" + name + " " + attribStr + "/>\n");
                return;
            }
        } else {
            if (value != null){
                addString(levelStr + "<" + name + ">\n");
            } else {
                addString(levelStr + "<" + name + "/>\n");
                return;
            }
        }

        //add value strings
        for (int i = 0; i < value.size(); i++){
            addString(levelValueStr + value.get(i));
        }

        addString(levelStr + "</" + name + ">\n");


    }

    public void addXmlElement(String name, String value, Map<String, String> attrib, int level) {
        String levelStr = getLevelStr(level);
        String attribStr = getAttribStr(attrib);

        String elementStr;
        if (attribStr.length() > 0){
            elementStr = String.format(levelStr + "<" + name + " " + attribStr + ">" + value + "</" + name + ">\n");
        } else {
            elementStr = String.format(levelStr + "<" + name + ">" + value + "</" + name + ">\n");
        }

        addString(elementStr);
    }

    public void addXmlElement(String name, int value, Map<String, String> attrib, int level){
        addXmlElement(name, Integer.toString(value), attrib, level);
    }
    public void addXmlElement(String name, double value, Map<String, String> attrib, int level){
        addXmlElement(name, Double.toString(value), attrib, level);
    }
    public void addXmlElement(String name, boolean value, Map<String, String> attrib, int level){
        addXmlElement(name, Boolean.toString(value), attrib, level);
    }

    //without level
    public void addXmlElement(String name, XmlData value, Map<String, String> attrib){
        addXmlElement(name, value, attrib, 0);
    }
    public void addXmlElement(String name, String value, Map<String, String> attrib){
        addXmlElement(name, value, attrib, 0);
    }
    public void addXmlElement(String name, int value, Map<String, String> attrib){
        addXmlElement(name, value, attrib, 0);
    }
    public void addXmlElement(String name, double value, Map<String, String> attrib){
        addXmlElement(name, value, attrib, 0);
    }
    public void addXmlElement(String name, boolean value, Map<String, String> attrib){
        addXmlElement(name, value, attrib, 0);
    }

    //without level and attrib
    public void addXmlElement(String name, XmlData value){
        addXmlElement(name, value, null, 0);
    }
    public void addXmlElement(String name, String value){
        addXmlElement(name, value, null, 0);
    }
    public void addXmlElement(String name, int value){
        addXmlElement(name, value, null, 0);
    }
    public void addXmlElement(String name, double value){
        addXmlElement(name, value, null, 0);
    }
    public void addXmlElement(String name, boolean value){
        addXmlElement(name, value, null, 0);
    }

    private String getLevelStr(int level) {
        StringBuilder levelStr = new StringBuilder();

        for (int i = 0; i < level; i++){
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    private String getAttribStr(Map<String, String> attrib){
        if (attrib == null) return "";

        StringBuilder attribStr = new StringBuilder();

        for (Map.Entry<String, String> entry: attrib.entrySet()) {
            attribStr.append(entry.getKey() + "=\"" + entry.getValue() + "\" ");
        }
        return attribStr.toString();
    }

}
