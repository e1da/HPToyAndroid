package com.hifitoy.hifitoydevice;


import android.util.Xml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ToyPresetTest {
    private ToyPreset p0;
    private ToyPreset p1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        p0 = new ToyPreset();
        System.out.println(String.format(Locale.getDefault(), "%f", p0.getVolume().getDb()));

        try {
            p1 = p0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        p1.getVolume().setDb(-1.0f);
        System.out.println(String.format(Locale.getDefault(), "%f %f",
                                    p0.getVolume().getDb(), p1.getVolume().getDb()));

        assertTrue(Float.compare(p0.getVolume().getDb(), p0.getVolume().getDb()) == 0);
        assertTrue(Float.compare(p1.getVolume().getDb(), p1.getVolume().getDb()) == 0);
        assertTrue(Float.compare(p0.getVolume().getDb(), p1.getVolume().getDb()) != 0);
    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        p1.getVolume().setDb(-1.0f);
        assertEquals(p0, p0);
        assertEquals(p1, p1);
        assertNotEquals(p0, p1);

        p1.getVolume().setDb(0.0f);
        assertEquals(p0, p1);
    }

    @Test
    public void testImport() {
        p0.getVolume().setDb(-1.0f);
        assertNotEquals(p0, p1);

        if (!p1.importFromDataBufs(p0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(p0, p1);

    }

    @Test
    public void testImport1() {
        p0.getFilters().getBiquad((byte)6).getParams().setDbVolume(12.0f);
        assertNotEquals(p0, p1);

        if (!p1.importFromDataBufs(p0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(p0, p1);

    }

    @Test
    public void testXmlExportImport() {
        p0.getVolume().setDb(-1.0f);
        assertNotEquals(p0, p1);

        StringReader data = new StringReader(p0.toXmlData().toString());

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(data);

            p1.importFromXml(xmlParser);
            assertEquals(p0, p1);

        } catch (XmlPullParserException e) {
            System.out.println(e.toString());
            fail("XmlPullParser Exception.");
        } catch (IOException e) {
            System.out.println(e.toString());
            fail("IO Exception.");
        }

    }
}