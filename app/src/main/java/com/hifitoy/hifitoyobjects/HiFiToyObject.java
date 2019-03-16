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
import java.util.List;

public interface HiFiToyObject {

    byte getAddress();
    String getInfo();

    void sendToPeripheral(boolean response);

    List<HiFiToyDataBuf> getDataBufs();
    boolean importData(byte[] data);

    XmlData toXmlData();
    boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException;
    //-(void) importFromXml:(XmlParserWrapper *)xmlParser withAttrib:(NSDictionary<NSString *, NSString *> *)attributeDict;

}
