package com.marz.snapprefs;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by MARZ on 2016. 04. 14..
 */
public class Friend implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private String displayName;

    private boolean isSelected;

    public Friend() {

    }

    public Friend(String name) {

        this.name = name;
    }

    public Friend(String name, String disName, boolean isSelected) {

        this.name = name;
        if(disName != "") {
            Logger.log("Logging Display Name In Friend.java: " + disName, true, true);
        }
        this.displayName = disName;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String disName) {
        this.displayName = disName;
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
