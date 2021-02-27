/*
 *   XmlSerializer.java
 *
 *   Created by Artem Khlyupin on 22/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XmlSerializer {
    static String TAG = "HiFiToy";

    private XmlData xmlData = new XmlData();

    public XmlSerializer(Object o) throws Exception {
        if (!XmlSerializable.class.isAssignableFrom(o.getClass())) {
            throw new Exception("XmlSerializable not assignable from " + o.getClass().getSimpleName());
        }

        Field[] fields = o.getClass().getDeclaredFields();
        XmlData data = parseFields(fields, o);

        List<Class<?>> superclasses = getSuperClassHierarchy(o);
        XmlData xmlSuperClasses = new XmlData();

        for (int i = superclasses.size() - 1; i >= 0; i--) {
            Class<?> c = superclasses.get(i);

            if (!XmlSerializable.class.isAssignableFrom(c)) {
                throw new Exception("XmlSerializable not assignable from " + c.getSimpleName());
            }

            XmlData d = parseFields(c.getDeclaredFields(), o);

            d.addXmlData(xmlSuperClasses);
            xmlSuperClasses.clear();
            xmlSuperClasses.addXmlElement(c.getSimpleName(), d);

        }
        if(superclasses.size() > 0) {
            data.addXmlData(xmlSuperClasses);
        }

        xmlData.clear();
        xmlData.addXmlElement(o.getClass().getSimpleName(), data, null);

    }

    private XmlData parseFields(Field[] fields, Object o) throws Exception{
        XmlData d = new XmlData();
        for (Field f : fields) {

            f.setAccessible(true);
            if (f.get(o) == null) continue;

            int m = f.getModifiers();
            if (Modifier.isTransient(m) || (Modifier.isStatic(m))) {
                continue;
            }

            String type = f.getType().toString();
            if ( (type.equals("float")) || (type.equals("double")) ||
                    (type.equals("byte")) || (type.equals("short")) || (type.equals("int")) ||
                    (type.equals("boolean")) ) {

                d.addXmlElement(f.getName(), f.get(o).toString());

            } else if ((type.contains("class ["))) {
                Object[] arr = (Object[])f.get(o);
                if (arr == null) continue;

                Map<String, String> attrib = new HashMap<>();
                attrib.put("length", Integer.toString(arr.length));

                XmlData itemsXml = new XmlData();

                for (int i = 0; i < arr.length; i++) {
                    XmlSerializer item = new XmlSerializer(arr[i]);
                    itemsXml.addXmlData(item.getXmlData());
                }
                d.addXmlElement(f.getName(), itemsXml, attrib);

            } else {
                XmlSerializer xmlUtil = new XmlSerializer(f.get(o));
                d.addXmlElement(f.getName(), xmlUtil.getXmlData());
            }
        }
        return d;
    }

    private List<Class<?>> getSuperClassHierarchy(Object o) {
        List<Class<?>> list = new LinkedList<>();

        Class<?> superclass = o.getClass().getSuperclass();

        while ( (superclass != null) && (!superclass.getSimpleName().equals("Object")) ) {
            list.add(superclass);

            superclass = superclass.getSuperclass();
        }
        return list;
    }

    public XmlData getXmlData() {
        return xmlData;
    }
}
