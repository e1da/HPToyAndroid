package com.hifitoy.hifitoyobjects;

import android.util.Xml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class FiltersTest {
    Filters f0;
    Filters f1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        f0 = new Filters();

        try {
            f1 = f0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        f1.getBiquad((byte)0).getParams().setFreq((short)150);
        assertTrue(f0.getBiquad((byte)0).getParams().getFreq() != f1.getBiquad((byte)0).getParams().getFreq());

    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        f1.getBiquad((byte)0).getParams().setFreq((short)150);
        assertEquals(f0, f0);
        assertEquals(f1, f1);
        assertNotEquals(f0, f1);

    }

    @Test
    public void testImport() {
        f1.getBiquad((byte)0).getParams().setFreq((short)1000);
        assertNotEquals(f0, f1);

        if (!f1.importFromDataBufs(f0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(f0, f1);
    }

    @Test
    public void testXmlExportImport() {
        f0.getBiquad((byte)1).getParams().setFreq((short)1000);
        assertNotEquals(f0, f1);

        StringReader data = new StringReader(f0.toXmlData().toString());

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(data);

            f1.importFromXml(xmlParser);
            assertEquals(f0, f1);

        } catch (XmlPullParserException e) {
            System.out.println(e.toString());
            fail("XmlPullParser Exception.");
        } catch (IOException e) {
            System.out.println(e.toString());
            fail("IO Exception.");
        }

    }
}