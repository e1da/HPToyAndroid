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
import java.util.Locale;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class LoudnessTest {
    private Loudness l0;
    private Loudness l1;
    private Loudness l2;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        l0 = new Loudness();
        System.out.println(String.format(Locale.getDefault(), "gain=%f freq=%d", l0.getGain(), l0.getFreq()));

        try {
            l1 = l0.clone();
            l2 = l0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        l1.setGain(0.0001f);
        System.out.println(String.format(Locale.getDefault(), "%f %f", l0.getGain(), l1.getGain()));

        assertTrue(Float.compare(l0.getGain(), l0.getGain()) == 0);
        assertTrue(Float.compare(l1.getGain(), l1.getGain()) == 0);
        assertTrue(Float.compare(l0.getGain(), l1.getGain()) != 0);

        l2.setFreq((short)70);
        System.out.println(String.format(Locale.getDefault(), "%d %d", l0.getFreq(), l2.getFreq()));

        assertTrue(Float.compare(l0.getFreq(), l0.getFreq()) == 0);
        assertTrue(Float.compare(l2.getFreq(), l2.getFreq()) == 0);
        assertTrue(Float.compare(l0.getFreq(), l2.getFreq()) != 0);
    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        l1.setGain(0.05f);
        System.out.println(String.format(Locale.getDefault(), "%f %f", l0.getGain(), l1.getGain()));

        assertEquals(l0, l0);
        assertEquals(l1, l1);
        assertNotEquals(l0, l1);

        l2.setFreq((short)70);
        System.out.println(String.format(Locale.getDefault(), "%d %d", l0.getFreq(), l2.getFreq()));

        assertEquals(l0, l0);
        assertEquals(l2, l2);
        assertNotEquals(l0, l2);

    }

    @Test
    public void testImport() {
        l1.setGain(0.05f);
        assertNotEquals(l0, l1);

        if (!l1.importFromDataBufs(l0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(l0, l1);

    }

    /*@Test
    public void testXmlExportImport() {
        l0.setGain(0.05f);
        assertNotEquals(l0, l1);

        StringReader data = new StringReader(l0.toXmlData().toString());

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(data);

            l1.importFromXml(xmlParser);
            assertEquals(l0, l1);

        } catch (XmlPullParserException e) {
            System.out.println(e.toString());
            fail("XmlPullParser Exception.");
        } catch (IOException e) {
            System.out.println(e.toString());
            fail("IO Exception.");
        }

    }*/

}