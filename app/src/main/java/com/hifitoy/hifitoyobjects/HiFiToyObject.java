/*
 *   HiFiToyObject.java
 *
 *   Created by Artem Khlyupin on 06/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoyobjects;

import com.hifitoy.xml.XmlData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public interface HiFiToyObject {

    int getAddress();
    String getInfo();

    void sendToPeripheral(boolean response);

    byte[] getBinary();
    boolean importData(byte[] data);

    XmlData toXmlData();
    boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException;
    //-(void) importFromXml:(XmlParserWrapper *)xmlParser withAttrib:(NSDictionary<NSString *, NSString *> *)attributeDict;

}
