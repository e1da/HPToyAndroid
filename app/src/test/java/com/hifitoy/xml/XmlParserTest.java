package com.hifitoy.xml;


import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.biquad.LowpassBiquad;
import com.hifitoy.hifitoyobjects.biquad.ParamBiquad;
import com.hifitoy.hifitoyobjects.drc.DrcCoef;
import com.hifitoy.hifitoyobjects.filter.DFilter;
import com.hifitoy.hifitoyobjects.filter.Filter;
import com.hifitoy.hifitoyobjects.filter.LowpassFilter;
import com.xmlorm.XmlCoder;
import com.xmlorm.XmlDecoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class XmlParserTest {
    static final boolean PRINT_FLAG = true;

    private void printBiquad(Biquad b) {
        if (!PRINT_FLAG) return;

        System.out.println( "biquad addr = " + b.getAddress() + " " + b.getBindAddr());
        System.out.println( "enabled = " + b.isEnabled());
        System.out.println( b.toString());
    }

    private void printLowpassBiquad(LowpassBiquad lp) {
        if (!PRINT_FLAG) return;

        System.out.println("lowpass addr = " + lp.getAddress() + " " + lp.getBindAddr());
        System.out.println("Q = " + lp.getQ());
        System.out.println(lp.toString());
    }

    private void printParamBiquad(ParamBiquad pb) {
        if (!PRINT_FLAG) return;

        System.out.println("param addr = " + pb.getAddress() + " " + pb.getBindAddr());
        System.out.println(pb.toString());
    }


    @Test
    public void testBiquadParse() {
        Biquad b = new Biquad((byte)3, (byte)4);
        b.setCoefs(2, 3, 4, 5, 6);
        b.setEnabled(true);

        try {
            XmlCoder s = new XmlCoder("b", b);
            System.out.println(s.toString());

            XmlDecoder d = new XmlDecoder(s.toString());
            Biquad b1 = (Biquad)d.parse("b", Biquad.class);
            printBiquad(b1);

            assertEquals(b, b1);

        } catch (Exception e) {
            System.out.println( e.toString());
            fail();
        }
    }

    @Test
    public void testLowpassBiquadParse() {
        LowpassBiquad lp = new LowpassBiquad((byte)3, (byte)4);
        lp.setFreq((short)3456);
        lp.setQ(3.56f);

        try {
            XmlCoder s = new XmlCoder("lp", lp);
            System.out.println(s.toString());

            XmlDecoder d = new XmlDecoder(s.toString());
            LowpassBiquad lp1 = (LowpassBiquad)d.parse("lp", LowpassBiquad.class);
            printLowpassBiquad(lp1);

            assertEquals(lp, lp1);

            //get Biquad from LPBiquad and compare
            Biquad b = new Biquad(lp);
            Biquad b1 = new Biquad(lp1);
            assertEquals(b, b1);

            printBiquad(b1);

        } catch (Exception e) {
            System.out.println( e.toString());
            fail();
        }
    }

    @Test
    public void testParamBiquadParse() {
        ParamBiquad pb = new ParamBiquad((byte)3, (byte)4);
        pb.setFreq((short)654);
        pb.setQ(5.43f);
        pb.setDbVolume(-14.67f);

        try {
            XmlCoder s = new XmlCoder("pb", pb);
            System.out.println(s.toString());

            XmlDecoder d = new XmlDecoder(s.toString());
            ParamBiquad pb1 = (ParamBiquad)d.parse("pb", ParamBiquad.class);
            printParamBiquad(pb1);

            assertEquals(pb, pb1);


        } catch (Exception e) {
            System.out.println( e.toString());
            fail();
        }
    }

    @Test
    public void testFilterParse() {
        Filter f = new Filter((byte)3, (byte)(3 + 7));
        LowpassFilter lpf = new LowpassFilter(f);
        lpf.upOrder();
        lpf.upOrder();

        try {
            XmlCoder s = new XmlCoder("f", f);
            System.out.println(s.toString());

            XmlDecoder d = new XmlDecoder(s.toString());
            Filter f1 = (Filter)d.parse("f", Filter.class);

            assertEquals(f, f1);

        } catch (Exception e) {
            System.out.println( e.toString());
            fail();
        }

    }

    @Test
    public void testDFilterParse() {
        DFilter df = new DFilter();
        df.bindChannels(false);
        LowpassFilter lpf = new LowpassFilter(df.getFilterCh0());
        lpf.upOrder();
        lpf.upOrder();

        try {
            XmlCoder s = new XmlCoder("df", df);
            System.out.println(s.toString());

            XmlDecoder d = new XmlDecoder(s.toString());
            DFilter df1 = (DFilter)d.parse("df", DFilter.class);

            assertEquals(df, df1);

        } catch (Exception e) {
            System.out.println( e.toString());
            fail();
        }
    }

    @Test
    public void testPresetParse() {
        HiFiToyPreset preset = new HiFiToyPreset();
        DFilter df = preset.getDFilter();
        df.bindChannels(false);
        LowpassFilter lpf = new LowpassFilter(df.getFilterCh0());
        lpf.upOrder();
        lpf.upOrder();

        preset.getVolume().setDb(-34);
        preset.getBassTreble().setEnabledChannel((byte)0, 0.1f);

        try {
            XmlCoder s = new XmlCoder("preset", preset);
            System.out.println(s.toString());

            XmlDecoder d = new XmlDecoder(s.toString());
            HiFiToyPreset preset1 = (HiFiToyPreset) d.parse("preset", HiFiToyPreset.class);

            assertEquals(preset, preset1);

        } catch (Exception e) {
            System.out.println( e.toString());
            fail();
        }
    }

    @Test
    public void testDefaultPresetParse() {
        DFilter df = new DFilter();


        try {
            XmlCoder s = new XmlCoder("df", df);
            System.out.println(s.toString());

            XmlDecoder d = new XmlDecoder(s.toString());
            DFilter df1 = (DFilter) d.parse("df", DFilter.class);

            assertEquals(df, df1);

        } catch (Exception e) {
            System.out.println( e.toString());
            fail();
        }
    }



}