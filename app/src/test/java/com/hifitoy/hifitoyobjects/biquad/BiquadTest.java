package com.hifitoy.hifitoyobjects.biquad;

import android.util.Xml;

import com.hifitoy.hifitoyobjects.biquad.Biquad;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import java.util.Locale;

import static org.junit.Assert.*;

//@RunWith(RobolectricTestRunner.class)
public class BiquadTest {
    private Biquad b0;
    private Biquad b1;

    @Before
    public void setUp() {
        System.out.println("setUp");

        b0 = new Biquad((byte)0x51, (byte)0x52);
        //System.out.println(b0.toString());

        try {
            b1 = b0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testGetBinary() {
        System.out.println("testGetBinary");
        System.out.println(String.format(Locale.getDefault(), "%d", b0.getDataBufs().size()));
    }

    @Test
    public void testCloneAndEquals() {
        System.out.println("testClone");

        b1.setEnabled(false);
        assertNotEquals(b0, b1);

        b1.setEnabled(true);

        b1.setCoefs(2.0f, 3.0f, 4.0f, 5.0f,6.0f);
        assertNotEquals(b0, b1);
        assertEquals(b0, b0);
        assertEquals(b1, b1);

    }

    @Test
    public void testImport() {
        b1.setCoefs(2.0f, 3.0f, 4.0f, 5.0f,6.0f);
        assertNotEquals(b0, b1);

        if (!b1.importFromDataBufs(b0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(b0, b1);

    }

    /*@Test
    public void testXmlExportImport() {
        b0.getParams().setFreq((short)1000);
        assertNotEquals(b0, b1);

        StringReader data = new StringReader(b0.toXmlData().toString());

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(data);

            b1.importFromXml(xmlParser);
            assertEquals(b0, b1);

        } catch (XmlPullParserException e) {
            System.out.println(e.toString());
            fail("XmlPullParser Exception.");
        } catch (IOException e) {
            System.out.println(e.toString());
            fail("IO Exception.");
        }

    }*/
}