package com.marz.snapprefs;

/**
 * Created by Marcell on 2016.02.06..
 */
public class StorePrefs {
    /**
     * This method is called upon creating instance of this class. It is called from separate thread, so here is place for things like files loading and connecting with server.
     */
    void init(){
    };
    /**
     * This method is called when user clicks update button. This will not block updating, It will only warn user.
     *
     * @param packageName package of the application
     * @param versionCode version code
     * @param versionName version name
     * @return should user update this application
     */
    boolean shouldUserUpdate(String packageName, int versionCode, String versionName){
        if (packageName.equals("com.snapchat.android") && versionCode==Obfuscator.SUPPORTED_VERSION_CODE && versionName.equals(Obfuscator.SUPPORTED_VERSION_CODENAME)){
            return  false;
        }else{
            return true;
        }
    }
    /**
     * This method is called when play store tries to auto update the application. Returning false causes to block the auto update.
     *
     * @param packageName package of the application
     * @param versionCode version code
     * @return can play store auto update this application
     */
    boolean canAutoUpdate(String packageName, int versionCode){
        if (packageName.equals("com.snapchat.android") && versionCode==Obfuscator.SUPPORTED_VERSION_CODE){
            return  false;
        }else{
            return true;
        }
    }
}
