/*
 *   XmlParser.java
 *
 *   Created by Artem Khlyupin on 22/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.xml;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

public class XmlParser {
    static String TAG = "HiFiToy";

    private XmlPullParser   xmlParser;
    private final String    xmlData;
    private final Object    o;

    public XmlParser(String xmlData, Object o) throws Exception {
        if (o == null) throw new Exception("Object is null.");

        this.o = o;
        this.xmlData = xmlData;
    }

    public void parse() throws Exception {
        //init xmlParser
        xmlParser = Xml.newPullParser();
        xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        InputStream in = new ByteArrayInputStream(xmlData.getBytes());
        xmlParser.setInput(in, null);

        //parse
        parse(o, o.getClass());
    }


    private void parse(Object o, Class<?> c) throws Exception {
        String tag;
        boolean startParse = false;

        do {

            if (xmlParser.getEventType() == XmlPullParser.START_TAG) {
                tag = xmlParser.getName();
                if (c.getSimpleName().equals(tag)) {
                    Log.d(TAG, "Start parse " + tag);
                    startParse = true;

                } else if (startParse) {

                    //parse super class instance
                    Class<?> sclass = c.getSuperclass();
                    if ( (sclass != null) && (sclass.getSimpleName().equals(tag)) ) {
                        parse(o, sclass);
                        xmlParser.next();
                        continue;
                    }


                    //parse object field
                    try {
                        Field f = c.getDeclaredField(tag);
                        f.setAccessible(true);

                        if (f.getType().isArray()) {
                            Object[] arr = (Object[]) f.get(o);
                            String lengthStr = xmlParser.getAttributeValue(null, "length");
                            int length = Integer.parseInt(lengthStr);
                            if ((arr == null) || (arr.length != length)) {
                                arr = new Object[length];
                            }
                            Log.d(TAG, f.getName() + " length = " + length);

                            for (int i = 0; i < length; i++) {
                                do { } while (xmlParser.next() != XmlPullParser.START_TAG);

                                Class<?> cc = findClass(xmlParser.getName());
                                if (cc == null) {
                                    throw new Exception("Not find class " + xmlParser.getName());
                                }
                                arr[i] = cc.newInstance();

                                parse(arr[i], arr[i].getClass());
                            }

                        } else {

                            do {
                                xmlParser.next();
                                if (xmlParser.getEventType() == XmlPullParser.TEXT) {
                                    Log.d(TAG, tag + " = " + xmlParser.getText());
                                    setField(o, f, xmlParser.getText());
                                }
                                xmlParser.next();
                            } while (xmlParser.getEventType() != XmlPullParser.END_TAG);
                        }

                    } catch (NoSuchFieldException e) {
                        Log.d(TAG, e.toString());
                    }
                }
            } else if (xmlParser.getEventType() == XmlPullParser.END_TAG) {
                if (c.getSimpleName().equals(xmlParser.getName())) {
                    Log.d(TAG, "End parse " + xmlParser.getName());
                    break;
                }
            }

            xmlParser.next();
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

    }

    private void setField(Object o, Field f, String val) throws IllegalAccessException {
        f.setAccessible(true);

        if (f.getType() == int.class) {
            f.setInt(o, Integer.parseInt(val));
        } else if (f.getType() == short.class) {
            f.setShort(o, Short.parseShort(val));
        } else if (f.getType() == byte.class) {
            f.setByte(o, Byte.parseByte(val));
        } else if (f.getType() == float.class) {
            f.setFloat(o, Float.parseFloat(val));
        } else if (f.getType() == double.class) {
            f.setDouble(o, Double.parseDouble(val));
        } else if (f.getType() == boolean.class) {
            f.setBoolean(o, Boolean.parseBoolean(val));
        } else {
            throw new IllegalAccessException("Parse object field error.");
        }

    }

    private Class<?> findClass(String className) {
        final String[] packs = new String[]{
                "com.hifitoy.hifitoyobject.",
                "com.hifitoy.hifitoyobject.basstreble.",
                "com.hifitoy.hifitoyobject.biquad.",
                "com.hifitoy.hifitoyobject.drc.",
                "com.hifitoy.hifitoyobject.filter.",};
        Class<?> c = null;

        for (String packName : packs) {
            try {
                c = Class.forName(packName + className);
                break;
            } catch (ClassNotFoundException e) {

            }
        }

        return c;
    }

}
