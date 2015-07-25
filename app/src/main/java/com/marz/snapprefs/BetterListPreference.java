/*
 * Copyright (C) 2014  Sturmen, stammler, Ramis and P1nGu1n
 *
 * This file is part of Keepchat.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.marz.snapprefs;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * ListPreference which automatically updates its summary when the value is changed.
 */
public class BetterListPreference extends ListPreference {
    public BetterListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BetterListPreference(Context context) {
        super(context);
    }

    @Override
    public void setValue(final String value) {
        super.setValue(value);
        notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
        int index = findIndexOfValue(getValue());
        return getEntries()[index];
    }

    @Override
    protected boolean persistString(String value) {
        if (value == null) {
            return false;
        } else {
            return persistInt(Integer.valueOf(value));
        }
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if (getSharedPreferences().contains(getKey())) {
            int intValue = getPersistedInt(0);
            return String.valueOf(intValue);
        } else {
            return defaultReturnValue;
        }
    }
}
