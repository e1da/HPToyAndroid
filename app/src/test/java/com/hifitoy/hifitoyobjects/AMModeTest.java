package com.hifitoy.hifitoyobjects;

import com.hifitoy.hifitoyobjects.AMMode;

import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

public class AMModeTest {
    @Test
    public void testSerialize() {
        AMMode am = new AMMode();

        try {
            FileOutputStream stream = new FileOutputStream("yourfile.txt");
            ObjectOutputStream ostream = new ObjectOutputStream(stream);
            ostream.writeObject(am);
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