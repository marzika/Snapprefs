package com.marz.snapprefs.Util;

import android.content.Context;

import com.marz.snapprefs.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Marcell on 2015.07.30..
 */
public class FileUtils {
    public static void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("speed.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Logger.log("FileUtils: File write failed: " + e.toString());
        }
    }


    public static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("speed.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Logger.log("FileUtils: File not found: " + e.toString());
        } catch (IOException e) {
            Logger.log("FileUtils: Can not read file: " + e.toString());
        }

        return ret;
    }
}
