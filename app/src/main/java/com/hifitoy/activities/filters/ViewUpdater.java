/*
 *   FilterUpdateView.java
 *
 *   Created by Artem Khlyupin on 04/07/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters;

import java.util.LinkedList;
import java.util.List;

public class ViewUpdater {
    private static ViewUpdater instance;
    private List<IFilterUpdateView> updateViewList = new LinkedList<IFilterUpdateView>();

    public interface IFilterUpdateView {
        void updateView();
    }

    public static synchronized ViewUpdater getInstance() {
        if (instance == null){
            instance = new ViewUpdater();
        }
        return instance;
    }

    public void addUpdateView(IFilterUpdateView updateView) {
        updateViewList.add(updateView);
    }

    public void removeUpdateView(IFilterUpdateView updateView) {
        updateViewList.remove(updateView);
    }

    public void update() {
        for (IFilterUpdateView uv : updateViewList) {
            if (uv != null) uv.updateView();
        }
    }
}
