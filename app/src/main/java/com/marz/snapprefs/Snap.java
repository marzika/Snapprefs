package com.marz.snapprefs;

import android.graphics.Bitmap;

import java.io.FileInputStream;

/**
 * Created by 723183 on 9/1/2015.
 */
public class Snap {
    public Bitmap img;
    public FileInputStream vid;
    public Saving.MediaType mediaType;


    public Snap(Bitmap b, boolean overlay) {
        img = b;
        if (overlay)
            mediaType = Saving.MediaType.IMAGE_OVERLAY;
        else
            mediaType = Saving.MediaType.IMAGE;
    }

    public Snap(FileInputStream fis) {
        vid = fis;
        mediaType = Saving.MediaType.VIDEO;
    }

    public Bitmap getImage() {
        return img;
    }

    public FileInputStream getVideo() {
        return vid;
    }

    public Saving.MediaType getMediaType() {
        return mediaType;
    }
}