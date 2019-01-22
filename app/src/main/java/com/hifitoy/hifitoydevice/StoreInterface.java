/*
 *   StoreInterface.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

public interface StoreInterface {
    public boolean restore(String filename, String key);
    public void store(String filename, String key);
}
