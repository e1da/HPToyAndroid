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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class BiquadTest {
    private Biquad b0;
    private Biquad b1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        b0 = new Biquad((byte)0x51, (byte)0x52);
        System.out.println(String.format(Locale.getDefault(), "%d", b0.getParams().getFreq()));

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
    public void testClone() {
        System.out.println("testClone");

        b1.setEnabled(false);
        assertTrue(b0.isEnabled() != b1.isEnabled());

        b1.getParams().setFreq((short)150);
        System.out.println(String.format(Locale.getDefault(), "%d %d",
                b0.getParams().getFreq(), b1.getParams().getFreq()));
        assertTrue(b0.getParams().getFreq() != b1.getParams().getFreq());

    }

    @Test
    public void testEqual() {
        b1.getParams().setFreq((short)150);
        assertEquals(b0, b0);
        assertEquals(b1, b1);
        assertNotEquals(b0, b1);

    }

    @Test
    public void testImport() {
        b1.getParams().setFreq((short)1000);
        assertNotEquals(b0, b1);

        if (!b1.importFromDataBufs(b0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(b0, b1);

    }

    @Test
    public void testXmlExportImport() {
        b0.getParams().setFreq((short)1000);
        assertNotEquals(b0, b1);

        StringReader data = new StringReader(b0.toXmlData().toString());

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(data);

            if (b1.importFromXml(xmlParser)) {
                assertEquals(b0, b1);

            } else {
                fail("Import from XML fail.");
            }

        } catch (XmlPullParserException e) {
            System.out.println(e.toString());
            fail("XmlPullParser Exception.");
        } catch (IOException e) {
            System.out.println(e.toString());
            fail("IO Exception.");
        }

    }

    @Test
    public void testCoefs() {
        Biquad.BiquadParam p = b0.getParams();

        p.setFreq((short)1017);
        p.setQFac(1.41f);
        p.setDbVolume(6.0f);

        System.out.println(String.format(Locale.getDefault(), "coefs = %f %f %f %f %f",
                p.getB0(), p.getB1(), p.getB2(), p.getA1(), p.getA2()));

        ByteBuffer bb = b0.getDataBufs().get(0).getData();
        int ib0 = bb.getInt();
        int ib1 = bb.getInt();
        int ib2 = bb.getInt();
        int ia1 = bb.getInt();
        int ia2 = bb.getInt();

        System.out.println(String.format(Locale.getDefault(), "coefs = %x %x %x %x %x",
                ib0, ib1, ib2, ia1, ia2));

    }
}