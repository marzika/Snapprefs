package com.marz.snapprefs;

/**
 * Media.java created on 2013-12-12.
 * <p/>
 * Copyright (C) 2013 Alec McGavin <alec.mcgavin@gmail.com>
 * <p/>
 * This file is part of Snapshare.
 * <p/>
 * Snapshare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Snapshare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * a gazillion times. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This is a place to store media objects.
 */
public class Media {

    private Object content;

    public Object getContent() {
        return this.content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
