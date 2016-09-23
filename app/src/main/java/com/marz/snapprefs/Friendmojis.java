package com.marz.snapprefs;

import android.os.AsyncTask;
import android.os.Environment;

import com.marz.snapprefs.Util.CommonUtils;
import com.marz.snapprefs.Util.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by stirante
 */

public class Friendmojis {

    private static final long CACHE_VALIDITY = 604800000;
    private static final String BASE_URL = "http://snapprefs.com/checkEmoji.php";

    private static FriendmojiDatabase db;
    private static File file;

    static void init(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            file = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/friendmojis.dat");
            load();
            XposedHelpers.findAndHookMethod(Obfuscator.select.FRIEND_CLASS, lpparam.classLoader, Obfuscator.friendmojis.GET_FRIENDMOJI_STRING_METHOD, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String username = (String) XposedHelpers.callMethod(param.thisObject, Obfuscator.save.GET_FRIEND_USERNAME);
                    if (db != null && db.emojis.containsKey(username.toLowerCase()))
                        param.setResult(db.emojis.get(username.toLowerCase()) + param.getResult());
                }
            });
            XposedHelpers.findAndHookMethod(Obfuscator.select.FRIEND_CLASS, lpparam.classLoader, Obfuscator.friendmojis.IS_IT_ME_METHOD, XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(Obfuscator.friendmojis.FRIEND_MANAGER_CLASS, lpparam.classLoader, Obfuscator.friendmojis.ON_FRIENDS_UPDATE_METHOD, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    List l = (List) XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, Obfuscator.friendmojis.FRIENDS_MAP_FIELD), Obfuscator.friendmojis.GET_VALUES_METHOD);
                    String[] params = new String[l.size()];
                    int i = 0;
                    for (Object o : l) {
                        params[i] = (String) XposedHelpers.callMethod(o, Obfuscator.save.GET_FRIEND_USERNAME);
                        i++;
                    }
                    new FriendmojiTask().execute(params);
                }
            });
        }
        catch (Throwable t) {
            Logger.log("Friendmojis failed to initialize!");
            Logger.log(t);
        }
    }

    private static void load() {
        db = (FriendmojiDatabase) FileUtils.readObjectFile(file);
    }

    private static void save() {
        if (db != null)
            FileUtils.writeObjectFile(file, db);
    }

    private static class FriendmojiDatabase implements Serializable {
        HashMap<String, String> emojis = new HashMap<>();
        String hash = "";
        long timestamp = System.currentTimeMillis();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FriendmojiDatabase that = (FriendmojiDatabase) o;

            if (timestamp != that.timestamp) return false;
            return hash != null ? hash.equals(that.hash) : that.hash == null;

        }

        @Override
        public int hashCode() {
            int result = hash != null ? hash.hashCode() : 0;
            result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "FriendmojiDatabase{" +
                    "emojis=" + new JSONObject(emojis).toString() +
                    ", hash='" + hash + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    private static class FriendmojiTask extends AsyncTask<String, Void, FriendmojiDatabase> {

        @Override
        protected FriendmojiDatabase doInBackground(String... params) {
            FriendmojiDatabase result = new FriendmojiDatabase();
            String s = "";
            ArrayList<String> usernames = new ArrayList<>();
            Collections.addAll(usernames, params);
            Collections.sort(usernames);
            for (String username : usernames) {
                s += username;
            }
            try {
                result.hash = CommonUtils.sha256(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (db != null && result.hash.equals(db.hash) && System.currentTimeMillis() - db.timestamp < CACHE_VALIDITY)
                return null;
            for (String username : usernames) {
                try {
                    URL website = new URL(BASE_URL);
                    HttpURLConnection connection = (HttpURLConnection) website.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    try {
                        writer.write("username=" + URLEncoder.encode(CommonUtils.sha256(username.toLowerCase()), "UTF-8"));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    writer.flush();
                    writer.close();
                    os.close();
                    connection.connect();
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null)
                        response.append(inputLine);
                    in.close();
                    connection.disconnect();
                    String jsonString = response.toString();
                    try {
                        JSONObject object = new JSONObject(jsonString);
                        if (object.getInt("status") == 1) {
                            result.emojis.put(username.toLowerCase(), object.getString("error_msg"));//don't ask me why it's called error_msg
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(FriendmojiDatabase friendmojiDatabase) {
            if (friendmojiDatabase == null) {
                Logger.log("Cached friendmoji match!");
                return;
            }
            db = friendmojiDatabase;
            save();
            Logger.log("Loaded friendmojis!");
        }
    }

}
