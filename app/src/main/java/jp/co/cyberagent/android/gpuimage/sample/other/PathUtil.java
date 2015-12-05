package jp.co.cyberagent.android.gpuimage.sample.other;

import android.os.Environment;

public class PathUtil {

    public static String getPath(String name) {
        return Environment.getExternalStorageDirectory() + "/Snapprefs/VisualFilters/" + name + ".png";
    }

}
