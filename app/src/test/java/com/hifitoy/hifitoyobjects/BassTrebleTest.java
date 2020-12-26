package com.hifitoy.hifitoyobjects;

import android.util.Xml;

import com.hifitoy.hifitoyobjects.basstreble.BassTreble;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class BassTrebleTest {
    private BassTreble bt0;
    private BassTreble bt1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        bt0 = new BassTreble();
        System.out.println(String.format(Locale.getDefault(), "%f", bt0.getEnabledChannel((byte)0)));

        try {
            bt1 = bt0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        bt1.setEnabledChannel((byte)0, 0.1f);
        System.out.println(String.format(Locale.getDefault(), "%f %f",
                bt0.getEnabledChannel((byte)0), bt1.getEnabledChannel((byte)0)));

        assertTrue(Float.compare(bt0.getEnabledChannel((byte)0), bt0.getEnabledChannel((byte)0)) == 0);
        assertTrue(Float.compare(bt1.getEnabledChannel((byte)0), bt1.getEnabledChannel((byte)0)) == 0);
        assertTrue(Float.compare(bt0.getEnabledChannel((byte)0), bt1.getEnabledChannel((byte)0)) != 0);
    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        bt1.setEnabledChannel((byte)0, 0.1f);
        assertEquals(bt0, bt0);
        assertEquals(bt1, bt1);
        assertNotEquals(bt0, bt1);

    }


    @Test
    public void testImport() {
        bt0.setEnabledChannel((byte)0, 0.1f);
        assertNotEquals(bt0, bt1);

        if (!bt1.importFromDataBufs(bt0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(bt0, bt1);

    }

    /*@Test
    public void testXmlExportImport() {
        bt0.setEnabledChannel((byte)0, 0.1f);
        assertNotEquals(bt0, bt1);

        StringReader data = new StringReader(bt0.toXmlData().toString());

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(data);

            bt1.importFromXml(xmlParser);
            assertEquals(bt0, bt1);

        } catch (XmlPullParserException e) {
            System.out.println(e.toString());
            fail("XmlPullParser Exception.");
        } catch (IOException e) {
            System.out.println(e.toString());
            fail("IO Exception.");
        }

    }*/
}