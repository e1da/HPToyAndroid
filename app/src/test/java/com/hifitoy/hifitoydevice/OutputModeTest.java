/*
 *   OutputModeTest.java
 *
 *   Created by Artem Khlyupin on 05/17/2021.
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoydevice;

import android.util.Log;

import org.junit.Test;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

import static org.junit.Assert.fail;

public class OutputModeTest {

    @Test
    public void testSerialize() {
        OutputMode om = new OutputMode();

        try {
            FileOutputStream stream = new FileOutputStream("yourfile.txt");
            ObjectOutputStream ostream = new ObjectOutputStream(stream);
            ostream.writeObject(om);
            ostream.close();

        } catch (NotSerializableException e) {
            System.out.println(e.toString());
            fail();
        } catch (IOException e) {
            System.out.println(e.toString());
            fail();
        }
    }

}