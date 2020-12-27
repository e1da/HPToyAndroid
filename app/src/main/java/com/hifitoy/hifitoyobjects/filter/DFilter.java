/*
 *   DFilter.java
 *
 *   Created by Artem Khlyupin on 24/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.filter;

import android.support.annotation.NonNull;
import android.util.Log;


import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.tas5558.TAS5558;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DFilter implements HiFiToyObject,  Cloneable{
    private static final String TAG = "HiFiToy";

    private boolean binded;

    @NonNull
    private Filter filterCh0;

    private Filter filterCh1;

    public DFilter() {
        binded = true;
        filterCh0 = new Filter(TAS5558.BIQUAD_FILTER_REG, (byte)(TAS5558.BIQUAD_FILTER_REG + 7));
        filterCh1 = null;
    }

    @Override
    public DFilter clone() throws CloneNotSupportedException{
        DFilter dFilter = (DFilter) super.clone();

        dFilter.filterCh0 = filterCh0.clone();
        if (filterCh1 != null) {
            dFilter.filterCh1 = filterCh1.clone();
        } else {
            dFilter.filterCh1 = null;
        }

        return dFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFilter df = (DFilter) o;
        if ( (binded != df.binded) || (!filterCh0.equals(df.filterCh0)) ) {
            return false;
        }

        if (filterCh1 != null) {
            return filterCh1.equals(df.filterCh1);
        }

        return df.filterCh1 == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(binded, filterCh0, filterCh1);
    }


    public boolean isChannelsBinded() {
        return binded;
    }

    public void bindChannels(boolean bind) {
        if (binded == bind) return;
        binded = bind;

        if (binded) {
            for (byte i = 0; i < filterCh0.getBiquadLength(); i++) {
                Biquad b = filterCh0.getBiquad(i);
                b.setBindAddr( (byte)(TAS5558.BIQUAD_FILTER_REG + 7 + i) );
            }

            //filterCh0.sendToPeripheral(true);
            filterCh1 = null;

        } else {
            try {
                filterCh1 = filterCh0.clone();
                for (byte i = 0; i < filterCh0.getBiquadLength(); i++) {
                    filterCh0.getBiquad(i).setBindAddr((byte) 0);
                    filterCh1.getBiquad(i).moveBindAddr();
                }
            } catch (CloneNotSupportedException e) {
                Log.d(TAG, e.toString());
            }


        }
    }

    public Filter getFilterCh0() {
        return filterCh0;
    }
    public void setFilterCh0(Filter f) {
        filterCh0 = f;
    }

    @Override
    public byte getAddress() {
        return filterCh0.getAddress();
    }

    @Override
    public void sendToPeripheral(boolean response) {
        filterCh0.sendToPeripheral(true);

        if ((!binded) && (filterCh1 != null)) {
            filterCh1.sendToPeripheral(true);
        }
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>(filterCh0.getDataBufs());

        if ((!binded) && (filterCh1 != null)) {
            l.addAll(filterCh1.getDataBufs());
        }
        return l;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        binded = false;
        filterCh0 = new Filter(TAS5558.BIQUAD_FILTER_REG);
        filterCh1 = new Filter((byte)(TAS5558.BIQUAD_FILTER_REG + 7));

        if (!filterCh0.importFromDataBufs(dataBufs)) return false;
        if (!filterCh1.importFromDataBufs(dataBufs)) return false;

        if (filterCh0.dataEquals(filterCh1)) {
            bindChannels(true);
        }

        return true;
    }
}
