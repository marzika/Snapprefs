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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public static void writeToSDFolder(String data, String filename) {
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
            Logger.log("FileUtils: File SDFolderwrite failed: " + e.toString());
        }
    }

    public static String readFromSDFolder(String filename) {
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
        } catch (FileNotFoundException e) {
            Logger.log("FILE NOT FOUND - ARE YOU SURE YOU CREATED IT?", true);
        } catch (Exception e) {
            Logger.log("INSTALL HANDLEEXTERNALSTORAGE TO FIX THE ISSUE -- FileUtils: File SDread failed " + e.toString(), true);
        }
        if (aBuffer.equalsIgnoreCase(""))
            aBuffer="0";
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

    public static String readFromSD(File fileToRead) {
        String aBuffer = "";
        try {
            FileInputStream fIn = new FileInputStream(fileToRead);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(fIn));
            String aDataRow = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            myReader.close();
        } catch (Exception e) {
            aBuffer = "0";
            Logger.log("readFromSD error: " + e.toString(), true);
        }
        return aBuffer;
    }

    public static void writeToSDFile(String data, File fileToWrite) {
        try {
            new File(Environment.getExternalStorageDirectory() + "/Snapprefs/Groups/").mkdir();
            fileToWrite.createNewFile();
            FileOutputStream fOut = new FileOutputStream(fileToWrite);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            Logger.log("FileUtils: File SDwrite failed: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void deleteSDFile(File fileToDelete) {
        fileToDelete.delete();
    }

    public static Object readObjectFile(File f) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
            Object result = stream.readObject();
            stream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeObjectFile(File f, Object obj) {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(f));
            stream.writeObject(obj);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
