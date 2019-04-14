/*
 *   HiFiToyPreset.java
 *
 *   Created by Artem Khlyupin on 24/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Xml;

import com.hifitoy.ApplicationContext;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.ByteUtility;
import com.hifitoy.hifitoynumbers.Checksummer;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.basstreble.BassTreble;
import com.hifitoy.hifitoyobjects.drc.Drc;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.hifitoyobjects.Loudness;
import com.hifitoy.hifitoyobjects.Volume;
import com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel;
import com.hifitoy.hifitoyobjects.drc.DrcCoef;
import com.hifitoy.hifitoyobjects.drc.DrcTimeConst;
import com.hifitoy.tas5558.TAS5558;
import com.hifitoy.xml.XmlData;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.BassFreq.BASS_FREQ_125;
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.BassTrebleCh.BASS_TREBLE_CH_127;
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.TrebleFreq.TREBLE_FREQ_9000;
import static com.hifitoy.hifitoyobjects.drc.Drc.DrcEvaluation.POST_VOLUME_EVAL;
import static com.hifitoy.hifitoyobjects.drc.DrcChannel.DRC_CH_1_7;
import static com.hifitoy.hifitoyobjects.drc.DrcCoef.POINT0_INPUT_DB;
import static com.hifitoy.hifitoyobjects.drc.DrcCoef.POINT3_INPUT_DB;

public class HiFiToyPreset implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private String  name;
    private short   checkSum;

    // HiFiToy CHARACTERISTICS, pointer to all characteristics
    private List<HiFiToyObject> characteristics;

    private Filters     filters;
    private Volume      masterVolume;
    private BassTreble  bassTreble;
    private Loudness    loudness;
    private Drc         drc;

    public HiFiToyPreset() {
        characteristics = new ArrayList<>();
        setDefault();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HiFiToyPreset that = (HiFiToyPreset) o;

        boolean b0 = true;//checkSum == that.checkSum;//!!!Warning

        return b0 &&
                Objects.equals(filters, that.filters) &&
                Objects.equals(masterVolume, that.masterVolume) &&
                Objects.equals(bassTreble, that.bassTreble) &&
                Objects.equals(loudness, that.loudness) &&
                Objects.equals(drc, that.drc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkSum, filters, masterVolume, bassTreble, loudness, drc);
    }

    @Override
    public HiFiToyPreset clone() throws CloneNotSupportedException{
        HiFiToyPreset preset = (HiFiToyPreset) super.clone();

        preset.filters = filters.clone();
        preset.masterVolume = masterVolume.clone();
        preset.bassTreble = bassTreble.clone();
        preset.loudness = loudness.clone();
        preset.drc = drc.clone();

        preset.characteristics = new ArrayList<>();
        preset.updateCharacteristics();

        return preset;
    }


    public void updateCharacteristics() {
        characteristics.clear();
        characteristics.add(filters);
        characteristics.add(masterVolume);
        characteristics.add(bassTreble);
        characteristics.add(loudness);
        characteristics.add(drc);
    }

    public void setDefault() {
        name = "DefaultPreset";

        //Filters
        filters = new Filters(TAS5558.BIQUAD_FILTER_REG, (byte)(TAS5558.BIQUAD_FILTER_REG + 7));

        //MasterVolume
        masterVolume = new Volume(TAS5558.MASTER_VOLUME_REG, 0.0f, 0.0f, Volume.HW_MUTE_DB);

        //Bass Treble
        bassTreble = new BassTreble();
        bassTreble.setEnabledChannel((byte)0, 1.0f);
        bassTreble.setEnabledChannel((byte)1, 1.0f);

        //Loudness
        loudness = new Loudness();

        //Drc
        DrcCoef drcCoef17 = new DrcCoef(DRC_CH_1_7,
                                        new DrcCoef.DrcPoint(POINT0_INPUT_DB, -120.0f),
                                        new DrcCoef.DrcPoint(-72.0f, -72.0f),
                                        new DrcCoef.DrcPoint(-24.0f, -24.0f),
                                        new DrcCoef.DrcPoint(POINT3_INPUT_DB, -24.0f));
        DrcTimeConst drcTimeConst17 = new DrcTimeConst(DRC_CH_1_7, 0.1f, 10.0f, 100.0f);

        drc = new Drc(drcCoef17, drcTimeConst17);
        drc.setEvaluation(POST_VOLUME_EVAL, (byte)0);
        drc.setEvaluation(POST_VOLUME_EVAL, (byte)1);

        updateCharacteristics();
        updateChecksum();
    }

    //setters / getters
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public short getChecksum() {
        return checkSum;
    }
    public Filters getFilters() {
        return filters;
    }
    public Volume getVolume() {
        return masterVolume;
    }
    public BassTreble getBassTreble() {
        return bassTreble;
    }
    public Loudness getLoudness() {
        return loudness;
    }

    public void updateChecksum() {
        updateChecksum(getDataBufs());
    }

    private void updateChecksum(List<HiFiToyDataBuf> dataBufs) {
        checkSum = Checksummer.calc(BinaryOperation.getBinary(dataBufs));
        Log.d(TAG, String.format(Locale.getDefault(), "Update checksum = 0x%x", checkSum));
    }

    public void storeToPeripheral() {
        PeripheralData peripheralData = new PeripheralData();
        peripheralData.setBiquadTypes(filters.getBiquadTypes());
        peripheralData.setDataBufs(getDataBufs());
        peripheralData.exportPresetWithDialog("Sending Preset...");
    }

    @Override
    public byte getAddress() {
        return 0;
    }

    @Override
    public String getInfo() {
        return name;
    }

    @Override
    public void sendToPeripheral(boolean response) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        //init progress dialog
        //DialogSystem.getInstance().showProgressDialog("Send Dsp Parameters...", 1);

        for (int i = 0; i < characteristics.size(); i++){
            characteristics.get(i).sendToPeripheral(true);
        }
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>();

        for (int i = 0; i < characteristics.size(); i++){
            l.addAll(characteristics.get(i).getDataBufs());
        }

        return l;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        for (int i = 0; i < characteristics.size(); i++){
            if (!characteristics.get(i).importFromDataBufs(dataBufs)) {
                return false;
            }
        }

        updateChecksum(dataBufs);
        return true;
    }

    @Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();
        for (int i = 0; i < characteristics.size(); i++){
            xmlData.addXmlData(characteristics.get(i).toXmlData());
        }

        XmlData presetXmlData = new XmlData();
        Map<String, String> attrib = new HashMap<>();
        attrib.put("Type", "HiFiToy");
        attrib.put("Version", "1.0");
        attrib.put("Checksum", Short.toString(checkSum));

        presetXmlData.addXmlElement("Preset", xmlData, attrib);
        return presetXmlData;
    }

    @Override
    public boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        String elementName = null;
        int count = 0;

        do {
            //if (xmlParser.getDepth() != 0) xmlParser.next();
            xmlParser.next();

            if (xmlParser.getEventType() == XmlPullParser.START_TAG){
                elementName = xmlParser.getName();

                if (elementName.equals("Preset")) {
                    String type = xmlParser.getAttributeValue(null, "Type");
                    String version = xmlParser.getAttributeValue(null, "Version");
                    String checkSumStr = xmlParser.getAttributeValue(null, "Checksum");

                    if ((type == null) || (!type.equals("HiFiToy")) ||
                            (version == null) || (!version.equals("1.0")) || (checkSumStr == null)) {

                        Log.d(TAG, "Preset xml file is not correct. See \"Type\", \"Version\" or \"Checksum\" fields.");
                        return false;
                    }

                    checkSum = Short.parseShort(checkSumStr);
                    Log.d(TAG, "import preset has checksum = " + checkSumStr);

                } else {

                    String addrStr = xmlParser.getAttributeValue(null, "Address");
                    if (addrStr == null) continue;
                    byte addr = ByteUtility.parse(addrStr);

                    Log.d(TAG, "addr = " + addrStr);

                    //parse hiFiToyObjects
                    for (int i = 0; i < characteristics.size(); i++){
                        HiFiToyObject o = characteristics.get(i);

                        if ( (o.getAddress() == addr) && (o.importFromXml(xmlParser)) ) {
                            count++;
                        }
                    }
                }


            }
            if (xmlParser.getEventType() == XmlPullParser.END_TAG){
                if (xmlParser.getName().equals("Preset")) break;

                elementName = null;
            }

            if ((xmlParser.getEventType() == XmlPullParser.TEXT) && (elementName != null)){

            }
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

        if (count == characteristics.size()){
            Log.d(TAG, "Xml parsing is success complete.");
            return true;
        } else {
            Log.d(TAG, "Xml parsing is not success complete.");

        }

        return false;
    }

    public boolean importFromXml(Uri uri) {
        if (uri == null) return false;

        //get preset name
        String filename = uri.getPathSegments().get(uri.getPathSegments().size() - 1);
        int index = filename.lastIndexOf(".");
        if (index != -1){
            filename = filename.substring(0, index);
        }

        //get scheme
        String scheme = uri.getScheme();
        if (scheme == null) return false;


        try {
            //get file input stream
            FileInputStream in;

            if ( (scheme.equals("file")) && (uri.getPath() != null) ) {
                in = new FileInputStream(new File(uri.getPath()));

            } else if (scheme.equals("content")) { // else "content"
                ContentResolver resolver = ApplicationContext.getInstance().getContext().getContentResolver();
                ParcelFileDescriptor fd = resolver.openFileDescriptor(uri, "r");
                if (fd == null) return false;

                in = new FileInputStream(fd.getFileDescriptor());

            } else {
                return false;
            }

            //get xml parser
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(in, null);

            if (importFromXml(xmlParser)) {
                name = filename;
                return true;
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());

        } catch (XmlPullParserException e) {
            Log.d(TAG, e.toString());

        } catch (IOException e) {
            Log.d(TAG, e.toString());

        }

        return false;
    }
}
