package com.hifitoy.hifitoyobjects.drc;

import android.util.Xml;

import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.Volume;

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

import static com.hifitoy.hifitoyobjects.drc.DrcChannel.DRC_CH_1_7;
import static com.hifitoy.hifitoyobjects.drc.DrcCoef.POINT0_INPUT_DB;
import static com.hifitoy.hifitoyobjects.drc.DrcCoef.POINT3_INPUT_DB;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DrcTest {
    private Drc drc0;
    private Drc drc1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        DrcCoef drcCoef17 = new DrcCoef(DRC_CH_1_7,
                new DrcCoef.DrcPoint(POINT0_INPUT_DB, -120.0f),
                new DrcCoef.DrcPoint(-72.0f, -72.0f),
                new DrcCoef.DrcPoint(-24.0f, -24.0f),
                new DrcCoef.DrcPoint(POINT3_INPUT_DB, -24.0f));
        DrcTimeConst drcTimeConst17 = new DrcTimeConst(DRC_CH_1_7, 0.1f, 10.0f, 100.0f);

        drc0 = new Drc(drcCoef17, drcTimeConst17);
        System.out.println(String.format(Locale.getDefault(), "%f", drc0.getEnabledChannel((byte)0)));

        try {
            drc1 = drc0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        drc1.setEnabled(1.0f, (byte)0);
        System.out.println(String.format(Locale.getDefault(), "%f %f",
                                    drc0.getEnabledChannel((byte)0), drc1.getEnabledChannel((byte)0)));

        assertTrue(Float.compare(drc0.getEnabledChannel((byte)0), drc0.getEnabledChannel((byte)0)) == 0);
        assertTrue(Float.compare(drc1.getEnabledChannel((byte)0), drc1.getEnabledChannel((byte)0)) == 0);
        assertTrue(Float.compare(drc0.getEnabledChannel((byte)0), drc1.getEnabledChannel((byte)0)) != 0);
    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        drc1.setEnabled(1.0f, (byte)0);
        assertEquals(drc0, drc0);
        assertEquals(drc1, drc1);
        assertNotEquals(drc0, drc1);

    }

    @Test
    public void testImport() {
        drc0.setEnabled(1.0f, (byte)0);
        assertNotEquals(drc0, drc1);

        if (!drc1.importFromDataBufs(drc0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(drc0, drc1);

    }

    @Test
    public void testXmlExportImport() {
        drc0.setEnabled(1.0f, (byte)0);
        assertNotEquals(drc0, drc1);

        StringReader data = new StringReader(drc0.toXmlData().toString());

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(data);

            if (drc1.importFromXml(xmlParser)) {
                assertEquals(drc0, drc1);

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

}