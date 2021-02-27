package com.hifitoy.xml;

import android.util.Log;

import com.hifitoy.hifitoyobjects.biquad.Biquad;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


@RunWith(RobolectricTestRunner.class)
public class XmlParserTest {
    static String TAG = "HiFiToy";

    @Test
    public void testBiquadParse() {
        try {

            Biquad bb = new Biquad((byte)3, (byte)4);
            bb.setCoefs(2, 3, 4, 5, 6);
            bb.setEnabled(true);

            XmlSerializer s = new XmlSerializer(bb);
            String xmlString = s.getXmlData().toString();
            System.out.println(xmlString);

            Biquad b = new Biquad();
            XmlParser xmlParser = new XmlParser(xmlString, b);
            xmlParser.parse();
            System.out.println( "addr = " + b.getAddress() + " " + b.getBindAddr());
            System.out.println( "enabled = " + b.isEnabled());
            System.out.println( b.toString());


        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

}