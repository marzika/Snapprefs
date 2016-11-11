package com.marz.snapprefs;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Util.BiHashMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.newInstance;

public class Stickers {
    private static BiHashMap<String, String> emojiNames = new BiHashMap<>();
    private static ArrayList<String> existing = new ArrayList<>();
    static boolean isResizing = false;

    private static void initEmojiNames() {
        emojiNames.put("sticker1", "1f550");
        emojiNames.put("sticker2", "1f551");
        emojiNames.put("sticker3", "1f552");
        emojiNames.put("sticker4", "1f553");
        emojiNames.put("sticker5", "1f554");
        emojiNames.put("sticker6", "1f555");
        emojiNames.put("sticker7", "1f556");
        emojiNames.put("sticker8", "1f557");
        emojiNames.put("sticker9", "1f558");
        emojiNames.put("sticker10", "1f559");
        emojiNames.put("sticker11", "1f55a");
        emojiNames.put("sticker12", "1f55b");
        emojiNames.put("sticker13", "1f55c");
        emojiNames.put("sticker14", "1f55d");
        emojiNames.put("sticker15", "1f55e");
        emojiNames.put("sticker16", "1f55f");
        emojiNames.put("sticker17", "1f560");
        emojiNames.put("sticker18", "1f561");
        emojiNames.put("sticker19", "1f562");
        emojiNames.put("sticker20", "1f563");
        emojiNames.put("sticker21", "1f564");
        emojiNames.put("sticker22", "1f565");
        emojiNames.put("sticker23", "1f566");
    }
    static void initStickers(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context snapContext) {

        initEmojiNames();//init unicode-cool name map
        //List single emojis
        /*File myFile = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/Stickers/");
        File[] files = myFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".svg") && new File(dir, filename.substring(0, filename.lastIndexOf(".")) + ".png").exists();
            }
        });

        if( files == null )

        for (File f : files) {
            String s = f.getName().substring(0, f.getName().lastIndexOf("."));
            existing.add(s);
        }
        //This method loads contents of a zip
        XposedHelpers.findAndHookMethod(Obfuscator.stickers.ASSETREADER_CLASS, lpparam.classLoader, Obfuscator.stickers.ASSETREADER_READ, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                if (XposedHelpers.getBooleanField(methodHookParam.thisObject, "mIsUnzipped"))
                    return null;
                Context mContext = (Context) XposedHelpers.getObjectField(methodHookParam.thisObject, "mContext");
                InputStream is = null;
                try {
                    XposedHelpers.callMethod(methodHookParam.thisObject, "b");
                    is = mContext.getAssets().open((String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mPath"));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                ZipInputStream zis = new ZipInputStream(is);
                ZipEntry entry;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                HashMap mAssets = (HashMap) XposedHelpers.getObjectField(methodHookParam.thisObject, "mAssets");
                while ((entry = zis.getNextEntry()) != null) {
                    String coolName = entry.getName().substring(0, entry.getName().lastIndexOf("."));
                    String type = entry.getName().substring(entry.getName().lastIndexOf("."));
                    String unicodeName = coolName;
                    int length = 0;
                    if (emojiNames.containsKey(coolName)) {
                        unicodeName = emojiNames.get(coolName);
                    } else {
                        coolName = emojiNames.getByValue(unicodeName);
                    }
                    if (existing.contains(unicodeName)) {
                        byte[] bytes = readFile(unicodeName + type);
                        output.write(bytes, 0, bytes.length);
                    } else if (existing.contains(coolName)) {
                        byte[] bytes = readFile(coolName + type);
                        output.write(bytes, 0, bytes.length);
                    } else {
                        int i;
                        byte[] buffer = new byte[100000];
                        while ((i = zis.read(buffer)) > 0) {
                            length += i;
                            output.write(buffer, 0, i);
                        }
                    }
                    mAssets.put(unicodeName + type, output.toByteArray());
                }
                output.close();
                zis.close();
                is.close();
                XposedHelpers.setBooleanField(methodHookParam.thisObject, "mIsUnzipped", true);
                AtomicBoolean mIsUnzipping = (AtomicBoolean) XposedHelpers.getObjectField(methodHookParam.thisObject, "mIsUnzipping");
                mIsUnzipping.set(false);
                synchronized (mIsUnzipping) {
                    XposedHelpers.callMethod(mIsUnzipping, "notifyAll");
                }

                return null;
            }
        });*/

        //TODO: Vj = regular emoji sticker
        //TODO: Vj.k -> akQ = aet
        //TODO: akQ.f -> akV = agm aka FastZippedAssetReader -- MINOR REFACTOR HERE
        XposedHelpers.findAndHookMethod("Vu", lpparam.classLoader, "a", MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, "scale") == null)
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "scale", 1.0F);
                ImageView previevStickerView = (ImageView) XposedHelpers.getObjectField(param.thisObject, "f");
                float diff = previevStickerView.getScaleY() - (float) XposedHelpers.getAdditionalInstanceField(param.thisObject, "scale");
                if (diff > .5F && !isResizing) {
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "scale", previevStickerView.getScaleY());
                    byte[] bArr = null;
                    try{
                        Object aet = XposedHelpers.getObjectField(param.thisObject, "k");
                        Object agm = XposedHelpers.getObjectField(aet, "f");
                        String svgfile = XposedHelpers.callMethod(aet, "b", XposedHelpers.getObjectField(param.thisObject, "l"))+".svg";
                        Logger.printMessage("SVGFILE: " + svgfile, LogType.DEBUG);
                        bArr = (byte[]) XposedHelpers.callMethod(agm, "a", XposedHelpers.callMethod(aet, "b", XposedHelpers.getObjectField(param.thisObject, "l"))+".svg");
                    }catch (NoSuchMethodError | NoSuchFieldError e2){
                        Logger.log("Scaling non-emoji sticker", true);
                        return;
                    }
                    Object gz = newInstance(findClass(Obfuscator.stickers.SVG_CLASS, lpparam.classLoader));//new. hc
                    Object svg = XposedHelpers.callMethod(gz, "a", new ByteArrayInputStream(bArr));//new.
                    Bitmap emoji = Bitmap.createBitmap((int) (previevStickerView.getHeight() * previevStickerView.getScaleY()), (int) (previevStickerView.getHeight() * previevStickerView.getScaleY()), Bitmap.Config.ARGB_8888);
                    new ResizeTask(previevStickerView, svg, emoji).execute();
                }
            }
        });

        findAndHookMethod("android.content.res.AssetManager", lpparam.classLoader, "open", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("Open asset: " + param.args[0], LogType.DEBUG);
                String str = (String) param.args[0];
                if (str.contains("twemoji_2_")) {
                    String url = Environment.getExternalStorageDirectory() + "/Snapprefs/Stickers/" + str;
                    File file;
                    try {
                        Logger.log("Sdcard path: " + url, LogType.DEBUG);
                        file = new File(url);
                    } catch (Exception e){
                        Logger.log("Stickers file/folder not found", LogType.DEBUG);
                        return;
                    }

                    if( !file.exists() ) {
                        Logger.log( "Error loading STICKERS file: " + str, LogType.DEBUG);
                        return;
                    }
                    InputStream is = null;
                    is = new BufferedInputStream(new FileInputStream(file));
                    param.setResult(is);
                    Logger.log("setResult for AssetManager", LogType.DEBUG);
                }
            }
        });
    }

    public static byte[] readFile(String filename) {
        byte[] data = new byte[0];
        try {
            File myFile = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/Stickers/" + filename);
            FileInputStream fIn = new FileInputStream(myFile);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int i;
            byte[] buffer = new byte[100000];
            while ((i = fIn.read(buffer)) > 0) {
                outputStream.write(buffer, 0, i);
            }
            data = outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            //Logger.log("INSTALL HANDLEEXTERNALSTORAGE TO FIX THE ISSUE -- FileUtils: File SDread failed " + e.toString(), true);
        }
        return data;
    }
    static class ResizeTask extends AsyncTask<Void, Void, Void> {

        private final Object thisObject;
        private final Object svg;
        private final Bitmap emoji;

        public ResizeTask(Object thisObject, Object svg, Bitmap emoji) {
            this.thisObject = thisObject;
            this.svg = svg;
            this.emoji = emoji;
            isResizing = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            XposedHelpers.callMethod(svg, "a", new Canvas(emoji));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ((ImageView) thisObject).setImageBitmap(emoji);
            isResizing = false;
        }
    }
}
