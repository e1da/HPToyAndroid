/*
 *   FilterCollection.java
 *
 *   Created by Artem Khlyupin on 16/02/22
 *   Copyright © 2022 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.import_fragment;

import android.util.Log;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;
import com.hifitoy.hifitoydevice.ToyPreset;
import com.hifitoy.hifitoyobjects.Filters;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

class FilterCollection extends Observable {
    private final String TAG = "HiFiToy";

    private final List<String> nameList = HiFiToyPresetManager.getInstance().getPresetNameList();
    private final List<Filters> filterList = new LinkedList<>();
    private int activeIndex;

    public FilterCollection() {
        for (String name : nameList) {
            try {
                ToyPreset p = HiFiToyPresetManager.getInstance().getPreset(name);
                filterList.add(p.getFilters());

            } catch (IOException | XmlPullParserException e) {
                Log.d(TAG, e.toString());
            }
        }

        String presetName = HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset();
        setActiveIndex(presetName);
    }


    public int size() {
        return filterList.size();
    }

    public int getActiveIndex() {
        return activeIndex;
    }
    public void setActiveIndex(int index) {
        activeIndex = index;
        notifyObservers();
    }
    public void setActiveIndex(String presetName) {
        setActiveIndex(HiFiToyPresetManager.getInstance().getPresetIndex(presetName));
    }

    public List<String> getNameList() {
        return nameList;
    }

    public List<Filters> getFilterList() {
        return filterList;
    }
    public Filters getActiveFilter() {
        return getFilterList().get(activeIndex);
    }
}
