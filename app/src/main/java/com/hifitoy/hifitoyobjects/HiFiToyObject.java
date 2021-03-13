/*
 *   HiFiToyObject.java
 *
 *   Created by Artem Khlyupin on 06/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoyobjects;

import java.io.Serializable;
import java.util.List;

public interface HiFiToyObject extends Serializable {
    int FS = 96000;

    byte getAddress();

    void sendToPeripheral(boolean response);

    List<HiFiToyDataBuf> getDataBufs();
    boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs);

}
