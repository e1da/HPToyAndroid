/*
 *   StoreInterface.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

public interface StoreInterface {
    boolean restore(String filename, String key);
    void store(String filename, String key);
}
