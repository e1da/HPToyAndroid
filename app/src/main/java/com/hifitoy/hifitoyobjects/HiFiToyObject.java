/*
 *   HiFiToyObject.java
 *
 *   Created by Artem Khlyupin on 06/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoyobjects;

public interface HiFiToyObject {

    int getAddress();
    String getInfo();

    void sendToPeripheral(boolean response);

    byte[] getBinary();
    boolean importData(byte[] data);

    //-(XmlData *) toXmlData;
    //-(void) importFromXml:(XmlParserWrapper *)xmlParser withAttrib:(NSDictionary<NSString *, NSString *> *)attributeDict;

}
