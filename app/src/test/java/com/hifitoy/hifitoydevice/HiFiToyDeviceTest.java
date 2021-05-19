package com.hifitoy.hifitoydevice;

import com.hifitoy.hifitoyobjects.AMMode;

import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

public class HiFiToyDeviceTest {
    @Test
    public void testSerialize() {
        HiFiToyDevice dev = new HiFiToyDevice();

        try {
            FileOutputStream stream = new FileOutputStream("yourfile.txt");
            ObjectOutputStream ostream = new ObjectOutputStream(stream);
            ostream.writeObject(dev);
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