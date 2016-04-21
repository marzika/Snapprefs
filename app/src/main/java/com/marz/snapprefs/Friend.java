package com.marz.snapprefs;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by MARZ on 2016. 04. 14..
 */
public class Friend implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private boolean isSelected;

    public Friend() {

    }

    public Friend(String name) {

        this.name = name;
    }

    public Friend(String name, boolean isSelected) {

        this.name = name;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public static class friendComparator implements Comparator<Friend> {
        @Override
        public int compare(Friend o1, Friend o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
