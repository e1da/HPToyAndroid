package com.hifitoy.hifitoyobjects;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import java.util.List;
import java.util.Locale;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class VolumeTest {
    private Volume v0;
    private Volume v1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        v0 = new Volume((byte)0x10);
        System.out.println(String.format(Locale.getDefault(), "%f", v0.getDb()));

        try {
            v1 = v0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        v1.setDb(-0.0001f);
        System.out.println(String.format(Locale.getDefault(), "%f %f", v0.getDb(), v1.getDb()));

        assertTrue(Float.compare(v0.getDb(), v0.getDb()) == 0);
        assertTrue(Float.compare(v1.getDb(), v1.getDb()) == 0);
        assertTrue(Float.compare(v0.getDb(), v1.getDb()) != 0);
    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        v1.setDb(-0.05f);
        assertEquals(v0, v0);
        assertEquals(v1, v1);
        assertNotEquals(v0, v1);

    }

    @Test
    public void testGetDataBufs() {
        List<HiFiToyDataBuf> l = v0.getDataBufs();
        System.out.println(String.format(Locale.getDefault(), "%d", l.size()));
        assertTrue(l.size() == 1);

        HiFiToyDataBuf b = l.get(0);
        assertTrue(b.getLength() == 4);

        System.out.println(String.format(Locale.getDefault(), "%d", b.getData().getInt(0)));
        assertTrue(b.getData().getInt(0) == 72);


    }

    @Test
    public void testImport() {
        v0.setDb(-1.0f);
        assertNotEquals(v0, v1);

        if (!v1.importFromDataBufs(v0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(v0, v1);

    }

    /*@Test
    public void testXmlExportImport() {
        v0.setDbPercent(0.05f);
        assertNotEquals(v0, v1);

        StringReader data = new StringReader(v0.toXmlData().toString());

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(data);

            v1.importFromXml(xmlParser);
            assertEquals(v0, v1);

        } catch (XmlPullParserException e) {
            System.out.println(e.toString());
            fail("XmlPullParser Exception.");
        } catch (IOException e) {
            System.out.println(e.toString());
            fail("IO Exception.");
        }

    }*/
}