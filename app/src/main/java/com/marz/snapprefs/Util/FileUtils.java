package com.marz.snapprefs.Util;

import android.content.Context;
import android.os.Environment;

import com.marz.snapprefs.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Marcell on 2015.07.30..
 */
public class FileUtils {
    public static void writeToFile(String data, Context context, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename + ".txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Logger.log("FileUtils: File write failed: " + e.toString());
        }
    }

    public static void writeToSDFile(String data, String filename) {
        try {
            File myFile = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/" + filename + ".txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            Logger.log("FileUtils: File SDwrite failed: " + e.toString());
        }
    }

    public static String readFromSDFile(String filename) {
        String aBuffer = "";
        try {
            File myFile = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/" + filename + ".txt");
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(fIn));
            String aDataRow = "";

            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            myReader.close();
        } catch (Exception e) {
            Logger.log("FileUtils: File SDread failed: " + e.toString());
        }
        return aBuffer;
    }

    public static String readFromFile(Context context, String filename) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename + ".txt");

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
