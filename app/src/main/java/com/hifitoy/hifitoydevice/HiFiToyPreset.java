/*
 *   HiFiToyPreset.java
 *
 *   Created by Artem Khlyupin on 24/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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

    public Filters     filters;
    public Volume      masterVolume;
    public BassTreble  bassTreble;
    public Loudness    loudness;
    public Drc         drc;

    public HiFiToyPreset() {
        setDefault();
    }

    public HiFiToyPreset(String presetName) {
        this();
        this.name = presetName;
    }

    public HiFiToyPreset(File file) throws XmlPullParserException, IOException {
        this();
        if (file == null) throw new IOException("File is null.");
        //get preset name
        this.name = getPresetName(file);

        FileInputStream fis = new FileInputStream(file);
        importFromXml(fis);
    }

    public HiFiToyPreset(Uri uri) throws XmlPullParserException, IOException {
        this();
        if (uri == null) throw new IOException("Uri is null.");
        //get preset name
        this.name = getPresetName(uri);

        //get scheme
        String scheme = uri.getScheme();
        if (scheme == null) throw new IOException("Uri scheme is not correct.");

        //get file input stream
        ContentResolver resolver = ApplicationContext.getInstance().getContext().getContentResolver();
        InputStream in = resolver.openInputStream(uri);

        importFromXml(in);
    }

    public HiFiToyPreset(String presetName, String xmlData) throws XmlPullParserException, IOException {
        this(presetName);
        if (xmlData == null) throw new IOException("Xml data is not correct.");

        InputStream is = new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8));
        importFromXml(is);
    }

    public HiFiToyPreset(String filename, InputStream is) throws XmlPullParserException, IOException {
        this();
        if (is == null) throw new IOException("InputStream is null.");

        //get preset name
        this.name = getPresetName(filename);

        importFromXml(is);
    }

    public HiFiToyPreset(String presetName, List<HiFiToyDataBuf> dataBufs, byte[] biquadTypes) throws IOException {
        this(presetName);

        if (filters.setBiquadTypes(biquadTypes)) {
            if (!importFromDataBufs(dataBufs)) {
                throw new IOException("Import from data bufs error in Preset contructor.");
            }
        }
        throw new IOException("Biquad type length error.");
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


        return preset;
    }


    private List<HiFiToyObject> getCharacteristics() {
        return new ArrayList<HiFiToyObject>(
                Arrays.asList(filters, masterVolume, bassTreble, loudness, drc));
    }

    private void setDefault() {
        name = "No processing";

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
    public void setFilters(Filters f) {
        filters = f;
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
    public Drc getDrc() {
        return drc;
    }

    public void updateChecksum() {
        updateChecksum(getDataBufs());
    }

    private void updateChecksum(List<HiFiToyDataBuf> dataBufs) {
        checkSum = Checksummer.calc(BinaryOperation.getBinary(dataBufs));
        Log.d(TAG, String.format(Locale.getDefault(), "Update checksum = 0x%x", checkSum));
    }

    public void storeToPeripheral() {
        PeripheralData peripheralData = new PeripheralData(filters.getBiquadTypes(), getDataBufs());
        peripheralData.exportPresetWithDialog("Sending Preset...");
    }

    public void save(boolean rewrite) throws IOException {
        File dir = HiFiToyPresetManager.getInstance().getUserDir();
        if (dir == null) throw new IOException();

        File file = new File(dir, name + ".tpr");

        if ((file.exists()) && (!rewrite)) {
            Log.d(TAG, "Error. Preset with this name already exist!");
            throw new IOException("Preset with this name already exist!");
        }

        //get xml string of preset
        String xmlString = toXmlData().toString();

        //write to file
        FileWriter fw = new FileWriter(file);
        fw.write(xmlString);
        fw.close();

        Log.d(TAG, "Save preset.");
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
        //if (!HiFiToyControl.getInstance().isConnected()) return;

        //init progress dialog
        //DialogSystem.getInstance().showProgressDialog("Send Dsp Parameters...", 1);

        for (HiFiToyObject o : getCharacteristics()) {
            o.sendToPeripheral(true);
        }
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>();

        for (HiFiToyObject o : getCharacteristics()) {
            l.addAll(o.getDataBufs());
        }

        return l;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        for (HiFiToyObject o : getCharacteristics()) {
            if (!o.importFromDataBufs(dataBufs)) {
                return false;
            }
        }

        //get only preset buf from dataBufs
        //need for correct calc checksum
        List<HiFiToyDataBuf> presetDataBufs = getDataBufs();
        ListIterator<HiFiToyDataBuf> it =  dataBufs.listIterator();

        while (it.hasNext()) {
            byte addr = it.next().getAddr();
            boolean isContain = false;

            for (HiFiToyDataBuf pdb : presetDataBufs) {
                if (pdb.getAddr() == addr) {
                    isContain = true;
                    break;
                }
            }
            
            if (!isContain) {
                it.remove();
            }
        }
        //update checksum
        updateChecksum(dataBufs);
        return true;
    }

    @Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();
        for (HiFiToyObject o : getCharacteristics()) {
            xmlData.addXmlData(o.toXmlData());
        }

        XmlData presetXmlData = new XmlData();
        Map<String, String> attrib = new HashMap<>();
        attrib.put("Type", "HiFiToy");
        attrib.put("Version", "1.0");

        presetXmlData.addXmlElement("Preset", xmlData, attrib);
        return presetXmlData;
    }

    @Override
    public void importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
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

                    if ((type == null) || (!type.equals("HiFiToy")) ||
                            (version == null) || (!version.equals("1.0"))) {

                        String msg = "Preset xml file is not correct. See \"Type\" or \"Version\" fields.";
                        Log.d(TAG, msg);
                        throw new IOException(msg);
                    }

                } else {

                    String addrStr = xmlParser.getAttributeValue(null, "Address");
                    if (addrStr == null) continue;
                    byte addr = ByteUtility.parse(addrStr);

                    Log.d(TAG, "addr = " + addrStr);

                    //parse hiFiToyObjects
                    for (HiFiToyObject o : getCharacteristics()) {
                        if (o.getAddress() == addr) {
                            o.importFromXml(xmlParser);
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

        if (count == getCharacteristics().size()){
            updateChecksum();

            Log.d(TAG, "Xml parsing is successfully.");

        } else {
            String msg = "Xml parsing is not success.";
            Log.d(TAG, msg);
            throw new IOException(msg);
        }
    }

    private void importFromXml(InputStream in) throws XmlPullParserException, IOException{
        if (in == null) throw new IOException("InputStream is null.");

        //get xml parser
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        xmlParser.setInput(in, null);

        importFromXml(xmlParser);
    }


    private String getPresetName(String filename) {
        if (filename == null) return null;

        int end = filename.indexOf(".tpr");
        if (end != -1) {
            return filename.substring(0, end);
        }

        return filename;
    }

    private String getPresetName(File file) {
        return getPresetName(file.getName());
    }

    private String getPresetName(Uri uri) {
        if (uri == null) return null;

        if ( (uri.getScheme() != null) && (uri.getScheme().equals("content")) ) {
            Context c = ApplicationContext.getInstance().getContext();
            Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    return getPresetName(filename);
                }
                cursor.close();
            }

        }

        if (uri.getLastPathSegment() != null) {
            return getPresetName(uri.getLastPathSegment());
        }

        return null;
    }

    private String checkPresetName(String name) throws IOException{
        if (name == null) throw new IOException("Preset name is not correct");

        if (HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset().equals(name)) {
            String msg = String.format("Preset %s is exists and active. Import is not success.", name);
            throw new IOException(msg);
        }

        int index = 1;
        String modifyName = name;

        while (HiFiToyPresetManager.getInstance().isPresetExist(modifyName)) {
            modifyName = name + String.format(Locale.getDefault(),"_%d", index++);
        }

        return modifyName;
    }
}
