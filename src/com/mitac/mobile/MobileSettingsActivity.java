package com.mitac.mobile;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.telephony.TelephonyManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.mitac.xml.model.APN;
import com.mitac.xml.parser.APNParser;
import com.mitac.xml.parser.PullAPNParser;

public class MobileSettingsActivity extends Activity {
    /** Called when the activity is first created. */
    static String TAG = "MobileSettingsActivity";
    static Uri CURRENT_APN_URI = Uri
            .parse("content://telephony/carriers/preferapn");
    static Uri APN_LIST_URI = Uri.parse("content://telephony/carriers");
    final String APN_APP_FILE = "mobile/apns.xml";
    final String APP_FILE_PATH = "/data/data/com.mitac.mobile/files/apns.xml";
    final String MOBILE_FOLDER = "mobile";
    final String MOBILE_ZIP = "/mobile.zip";
    private String srcPath = null;
    private String dstPath = null;

    private APNParser parser = null;
    private List<APN> apns = null;
    String sdpath = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sdpath = getSDPath();
        dstPath = sdpath+"/"+MOBILE_FOLDER;
        new File(dstPath).mkdirs(); //if folder isn't exist, then create
    }

    public void onGetAPN(View v) {
        // Get the current APN from the databases(telephony.db),
        // Then write it into the SD card.
        try {
            parser = new PullAPNParser();
            apns = new ArrayList<APN>();
            getCurrentAPNFromSetting(getContentResolver());
            Log.d(TAG, "APN ok");
            String xml = parser.serialize(apns);
            Log.d(TAG, "serialize ok");

            sdpath = getSDPath();
            Log.d(TAG, "sdpath: "+sdpath);
            if (sdpath != null) {
                String path = sdpath + "/" + APN_APP_FILE;
                //Android40 solution
                //Issue: Android23, if claim the system permission, cannot create a file in SD card.
                //FileOutputStream fos = openFileOutput(APN_APP_FILE, Context.MODE_PRIVATE);
                FileOutputStream fos = new FileOutputStream(path);
                Log.d(TAG, "FileOutputStream");
                fos.write(xml.getBytes("UTF-8"));
                fos.close();

                //CopySdcardFile(APP_FILE_PATH, path);
            }
            apns = null;
            parser = null;

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void onSetAPN(View v) {
        // Read the APN setting from SD card,
        // Then set it as current APN through the databases(telephony.db).
        try {
            sdpath = getSDPath();
            if (sdpath != null) {
                String path = sdpath + "/" + APN_APP_FILE;
                InputStream is = new FileInputStream(path);
                // InputStream is = new FileInputStream(APP_FILE_PATH);
                boolean bfirst = true;

                parser = new PullAPNParser();
                apns = parser.parse(is);
                for (APN apn : apns) {
                    Log.i(TAG, apn.toString());
                    if (bfirst) {
                        insertAPN(apn);
                        bfirst = false;
                    }
                }
                apns = null;
                parser = null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void onConnectInfo(View v) {
//        Intent intent = new Intent();
//        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.Status");
//        //intent.setClass(this, Status.class);
//        startActivity(intent);
        
        CopyProperty();
        //CopySettings();
        //CopyAPN();
        onGetAPN(null);
        getRadioProperty();
        getUserSettings();
        zipSettings();
    }
    
    public void CopyProperty() {
        srcPath = "/default.prop";
        copyFile(srcPath, dstPath);
        srcPath = "/system/build.prop";
        copyFile(srcPath, dstPath);
    }
    
    //XXX: Android40: need the parameters in AndroidManefest.xml
    //android:sharedUserId="android.uid.system"
    public void CopySettings() {
        srcPath = "/data/data/com.android.providers.settings/databases/settings.db";
        copyFile(srcPath, dstPath);
        srcPath = "/data/data/com.android.providers.settings/databases/settings.db-shm";
        copyFile(srcPath, dstPath);
        srcPath = "/data/data/com.android.providers.settings/databases/settings.db-wal";
        copyFile(srcPath, dstPath);
    }

    //XXX: Android40: need the parameters in AndroidManefest.xml
    //android:sharedUserId="android.uid.phone"
    public void CopyAPN() {
        srcPath = "/data/data/com.android.providers.telephony/databases/telephony.db";
        copyFile(srcPath, dstPath);
        srcPath = "/data/data/com.android.providers.telephony/databases/telephony.db-shm";
        copyFile(srcPath, dstPath);
        srcPath = "/data/data/com.android.providers.telephony/databases/telephony.db-wal";
        copyFile(srcPath, dstPath);
    }
    
    public int getUserSettings() {
        try {
            OutputStream fosto = new FileOutputStream(dstPath+"/UserSettings.txt");
            String temp = null;

            //XXX: Android42: Settings.Global,  Android40: Settings.Secure
            boolean mUserDataEnabled = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.MOBILE_DATA, 1) == 1;
            //boolean mUserDataEnabled = Settings.Secure.getInt(getContentResolver(),
            //        Settings.Secure.MOBILE_DATA, 1) == 1;
            Log.d(TAG, "MOBILE_DATA: "+mUserDataEnabled);
            temp = "[MOBILE_DATA]: "+"["+mUserDataEnabled+"]\n";
            fosto.write(temp.getBytes());

            boolean roaming = Settings.Global.getInt( getContentResolver(),
                    Settings.Global.DATA_ROAMING) != 0;
            //boolean roaming = Settings.Secure.getInt( getContentResolver(),
            //       Settings.Secure.DATA_ROAMING) != 0;
            Log.d(TAG, "DATA_ROAMING: "+roaming);
            temp = "[DATA_ROAMING]: "+"["+roaming+"]\n";
            fosto.write(temp.getBytes());

            int networkMode = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE, 0); //default(0): WCDMA/GSM; 1: GSM
            //int networkMode = Settings.Secure.getInt(getContentResolver(),
            //        Settings.Secure.PREFERRED_NETWORK_MODE, 0);
            Log.d(TAG, "PREFERRED_NETWORK_MODE: "+networkMode);
            temp = "[PREFERRED_NETWORK_MODE]: "+"["+networkMode+"]\n";
            fosto.write(temp.getBytes());

            fosto.close();
            return 0;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            return -1;
        }
    }
    
    //Android42 has these properties, but Android40 hasn't such properties.
    public int getRadioProperty() {        
        try {
            OutputStream fosto = new FileOutputStream(dstPath+"/getProp.txt");
            String temp = null;
            
            String phone_type = SystemProperties.get("gsm.current.phone-type", " ");
            temp = "[gsm.current.phone-type]: "+"["+phone_type+"]\n";
            fosto.write(temp.getBytes());

            boolean active = SystemProperties.getBoolean("gsm.defaultpdpcontext.active", false);
            temp = "[gsm.defaultpdpcontext.active]: "+"["+active+"]\n";
            fosto.write(temp.getBytes());

            String type = SystemProperties.get("gsm.network.type", " ");
            temp = "[gsm.network.type]: "+"["+type+"]\n";
            fosto.write(temp.getBytes());
                       
            String operator = SystemProperties.get("gsm.operator.alpha", " ");
            temp = "[gsm.operator.alpha]: "+"["+operator+"]\n";
            fosto.write(temp.getBytes());

            String iso = SystemProperties.get("gsm.operator.iso-country", " ");
            temp = "[gsm.operator.iso-country]: "+"["+iso+"]\n";
            fosto.write(temp.getBytes());

            boolean roaming = SystemProperties.getBoolean("gsm.operator.isroaming", false);
            temp = "[gsm.operator.isroaming]: "+"["+roaming+"]\n";
            fosto.write(temp.getBytes());
            
            String numeric = SystemProperties.get("gsm.operator.numeric", " ");
            temp = "[gsm.operator.numeric]: "+"["+numeric+"]\n";
            fosto.write(temp.getBytes());

            String loaded = SystemProperties.get("gsm.sim.loaded", " ");
            temp = "[gsm.sim.loaded]: "+"["+loaded+"]\n";
            fosto.write(temp.getBytes());

            String alpha = SystemProperties.get("gsm.sim.operator.alpha", " ");
            temp = "[gsm.sim.operator.alpha]: "+"["+alpha+"\n";
            fosto.write(temp.getBytes());

            String sim_iso = SystemProperties.get("gsm.sim.operator.iso-country", " ");
            temp = "[gsm.sim.operator.iso-country]: "+"["+sim_iso+"]\n";
            fosto.write(temp.getBytes());

            String sim_num = SystemProperties.get("gsm.sim.operator.numeric", " ");
            temp = "[gsm.sim.operator.numeric]: "+"["+sim_num+"]\n";
            fosto.write(temp.getBytes());

            String state = SystemProperties.get("gsm.sim.state", " ");
            temp = "[gsm.sim.state]: "+"["+state+"]\n";
            fosto.write(temp.getBytes());

            String baseband = SystemProperties.get("gsm.version.baseband", " ");
            temp = "[gsm.version.baseband]: "+"["+baseband+"]\n";
            fosto.write(temp.getBytes());

            String ril = SystemProperties.get("gsm.version.ril-impl", " ");
            temp = "[gsm.version.ril-impl]: "+"["+ril+"]\n";
            fosto.write(temp.getBytes());

            String dns1 = SystemProperties.get("net.dns1", " ");
            temp = "[net.dns1]: "+"["+dns1+"]\n";
            fosto.write(temp.getBytes());

            String dns2 = SystemProperties.get("net.dns2", " ");
            temp = "[net.dns2]: "+"["+dns2+"]\n";
            fosto.write(temp.getBytes());

            fosto.close();
           return 0;
        } catch (Exception ex) {
            return -1;
        }
        
    }

    public boolean zipSettings() {
        boolean mResult = false;
        String destPath = null;
        Collection<File> mDealFiles = null;        
        sdpath = getSDPath();
        destPath = sdpath+MOBILE_ZIP;
        mDealFiles = new ArrayList<File>();
        mDealFiles.add(new File(dstPath));
        
        try{
            Log.i(TAG, mDealFiles.toString());
            zipFileUtil.setStopFlag(false);
            zipFileUtil.zipFiles(mDealFiles,new File(destPath));
            mResult = true;
        }catch(Exception e){
            mResult = false;
            delFile(new File(destPath));
        }
        mDealFiles = null;
        return mResult;    
    }
    
    private boolean delFile(File f) {
        boolean ret  = false;
        String path = f.getPath();
        try {
          if (f.exists()) {
            f.delete();
            Log.d(TAG,"delete file: " + path);
            ret = true ;
          }
        }
        catch (Exception e) {
          return false;
        }
        return ret;
      }
    
    protected String getSIMInfo() {
        TelephonyManager iPhoneManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        return iPhoneManager.getSimOperator();
    }

    
    public String getSDPath() {
        File sdDir = null;
        // Android23, Android40
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
            Log.d(TAG, "getExternalStorageDirectory: " + sdDir.toString());
            //XXX: Android42 need use  hide class(UserEnvironment)
        }
        //XXX: testing on ULMO, and no SD card;
        //sdDir = new File("/mnt/internal_sd");

        // Android41, add other storage device

        //         if(Environment.getSDInternalStorageState().equals("mounted")){
        //             sdDir = Environment.getSDInternalStorageDirectory();
        //             Log.d(TAG,"getSDInternalStorageDirectory 0: "+sdDir.toString());
        //         } else {
        //             sdDir = Environment.getSDInternalStorageDirectory();
        //             Log.d(TAG,"getSDInternalStorageDirectory 1: "+sdDir.toString());
        //             }
        //
        //        if(Environment.getExternal2StorageState().equals("mounted")){
        //            sdDir = Environment.getExternal2StorageDirectory();
        //            Log.d(TAG, "getExternal2StorageDirectory: "+sdDir.toString());
        //            }
        //
        //         if(Environment.getUSB1StorageState().equals("mounted")){
        //             sdDir = Environment.getUSB1StorageDirectory();
        //             Log.d(TAG, "getUSB1StorageDirectory: "+sdDir.toString());
        //             }
        //
        //         if(Environment.getUSB2StorageState().equals("mounted")){
        //             sdDir = Environment.getUSB2StorageDirectory();
        //             Log.d(TAG, "getUSB2StorageState: "+sdDir.toString());
        //             }
        //
        //         if(Environment.getExternal1StorageState().equals("mounted")){
        //             sdDir = Environment.getExternal1StorageDirectory();
        //             Log.d(TAG, "getExternal1StorageDirectory: "+sdDir.toString());
        //             }


        String path = null;
        if (sdDir != null) {
            path = sdDir.toString();
        }

        return path;
    }

    public int CopySdcardFile(String fromFile, String toFile) {
        Log.d(TAG, "From File: "+fromFile);
        Log.d(TAG, "To File: "+toFile);
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
           return 0;
        } catch (Exception ex) {
            return -1;
        }
    }


    public void checkAPN() {
        Cursor cursor = null;
        try {
            cursor =  getContentResolver().query(APN_LIST_URI, null, null, null, null);
            //cursor.moveToFirst();
            Log.d(TAG, "cursor: " + cursor);
            while (cursor != null && cursor.moveToNext()) {
                if(cursor != null) {
                    String id = cursor.getString(cursor.getColumnIndex("_id"));
                    Log.d(TAG, "id: " + id);
                    String apn = cursor.getString(cursor.getColumnIndex("apn"));
                    Log.d(TAG, "apn: " + apn);
                    // Toast.makeText(getApplicationContext(),
                    // "Current id:" + id + " apn:" + apn, Toast.LENGTH_LONG).show();
                }
            }
        }catch (SQLException e) {
            //do nothing here
        } finally { if (cursor != null) {
            cursor.close();
        } 
        }
    }

    public int addAPN() {
        int id = -1;
        Log.d(TAG, "addAPN");
        String NUMERIC =  getSIMInfo();
        Log.d(TAG, "NUMERIC: " + NUMERIC);
        if (NUMERIC == null) {
            return -1;
        }

        ContentResolver resolver = this.getContentResolver();
        ContentValues     values = new ContentValues();

        values.put("name", "mike");
        values.put("apn", "test");
        values.put("type",  "default");
        values.put("numeric", NUMERIC);
        values.put("mcc",NUMERIC.substring(0, 3));
        values.put("mnc", NUMERIC.substring(3,NUMERIC.length()));
        values.put("proxy", ""); values.put("port", "");
        values.put("mmsproxy", "");
        values.put("mmsport", ""); 
        values.put("user",    ""); 
        values.put("server", ""); 
        values.put("password", "");
        values.put("mmsc", "");

        Cursor c = null;
        Uri newRow = resolver.insert(APN_LIST_URI, values);
        if (newRow != null) {
            c = resolver.query(newRow, null, null, null, null);
            int idIndex = c.getColumnIndex("_id");
            c.moveToFirst();
            id = c.getShort(idIndex);
        }
        if (c != null) c.close();
        return id;
    }


    public void setAPN(int id) {
        ContentResolver resolver = this.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", id);
        resolver.update(CURRENT_APN_URI, values, null, null);
        // resolver.delete(url, where, selectionArgs)
    }


    public String getCurrentAPNFromSetting(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(CURRENT_APN_URI, null, null, null, null);
            String curApnId = null;
            if (cursor != null && cursor.moveToFirst()) {
                curApnId = cursor.getString(cursor.getColumnIndex("_id"));
                String apnName1 = cursor
                        .getString(cursor.getColumnIndex("apn"));
                Log.d(TAG, "getCurrentAPNFromSetting: " + apnName1);
            }
            cursor.close();
            // find apn name from apn list
            if (curApnId != null) {
                cursor = resolver.query(APN_LIST_URI, null, " _id = ?",
                        new String[] { curApnId }, null);
                if (cursor != null && cursor.moveToFirst()) {
                    APN apn = new APN();
                    int column = -1;
                    column = cursor.getColumnIndex("_id");
                    if (column >= 0)
                        apn.setId(cursor.getInt(column));

                    column = cursor.getColumnIndex("name");
                    if (column >= 0)
                        apn.setName(cursor.getString(column));

                    column = cursor.getColumnIndex("numeric");
                    if (column >= 0)
                        apn.setNumeric(cursor.getString(column));

                    column = cursor.getColumnIndex("mcc");
                    if (column >= 0)
                        apn.setMcc(cursor.getString(column));

                    column = cursor.getColumnIndex("mnc");
                    if (column >= 0)
                        apn.setMnc(cursor.getString(column));

                    column = cursor.getColumnIndex("apn");
                    if (column >= 0)
                        apn.setApn(cursor.getString(column));

                    column = cursor.getColumnIndex("user");
                    if (column >= 0)
                        apn.setUser(cursor.getString(column));

                    column = cursor.getColumnIndex("server");
                    if (column >= 0)
                        apn.setServer(cursor.getString(column));

                    column = cursor.getColumnIndex("password");
                    if (column >= 0)
                        apn.setPassword(cursor.getString(column));

                    column = cursor.getColumnIndex("proxy");
                    if (column >= 0)
                        apn.setProxy(cursor.getString(column));

                    column = cursor.getColumnIndex("port");
                    if (column >= 0)
                        apn.setPort(cursor.getString(column));

                    column = cursor.getColumnIndex("mmsporxy");
                    if (column >= 0)
                        apn.setMmsProxy(cursor.getString(column));

                    column = cursor.getColumnIndex("mmsport");
                    if (column >= 0)
                        apn.setMmsPort(cursor.getString(column));

                    column = cursor.getColumnIndex("mmsc");
                    if (column >= 0)
                        apn.setMmsc(cursor.getString(column));

                    column = cursor.getColumnIndex("authtype");
                    if (column >= 0)
                        apn.setAuthType(cursor.getInt(column));

                    column = cursor.getColumnIndex("type");
                    if (column >= 0)
                        apn.setType(cursor.getString(column));

                    column = cursor.getColumnIndex("current");
                    if (column >= 0)
                        apn.setCurrent(cursor.getString(column));

                    column = cursor.getColumnIndex("protocol");
                    if (column >= 0)
                        apn.setProtocol(cursor.getString(column));

                    column = cursor.getColumnIndex("roaming_protocol");
                    if (column >= 0)
                        apn.setRoamingProtocol(cursor.getString(column));

                    column = cursor.getColumnIndex("carrier_enabled");
                    if (column >= 0)
                        apn.setCarrierEnabled(cursor.getString(column));

                    column = cursor.getColumnIndex("bearer");
                    if (column >= 0)
                        apn.setBearer(cursor.getString(column));

                    Log.d(TAG, apn.toString());
                    apns.add(apn);
                    // apn = null;
                    return curApnId;
                }
            }
        } catch (SQLException e) {
            // do nothing here
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }


    public static int updateCurrentAPN(ContentResolver resolver, String newAPN) {
        Cursor cursor = null;
        try { // get new apn id from list
            cursor  = resolver.query(APN_LIST_URI, null, " apn = ? and current = 1", 
                    new String[] { newAPN.toLowerCase() }, null);
            String apnId = null;
            if (cursor != null && cursor.moveToFirst()) {
                apnId =  cursor.getString(cursor.getColumnIndex("_id"));
                Log.d(TAG,  "updateCurrentAPN: "+apnId);
            }
            cursor.close();

            // set new apn id as chosen one
            if (apnId != null) {
                ContentValues values  = new ContentValues();
                values.put("apn_id", apnId);
                resolver.update(CURRENT_APN_URI, values, null, null);
                Log.d(TAG, "updateCurrentAPN 2"); 
            } else { // apn id not found, return 0. 
                return 0;
            }
        } catch (SQLException e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 1;
    }


    public void insertAPN(APN apn) {
        int id = -1;
        String NUMERIC = getSIMInfo();
        Log.d(TAG, "NUMERIC: " + NUMERIC);
        if (NUMERIC == null) {
            return;
        }

        Cursor cursor = null;
        ContentResolver resolver = this.getContentResolver();
        String newAPN = apn.getApn();
        String apnId = null;
        try {
            cursor = resolver.query(APN_LIST_URI, null,
                    " apn = ? and current = 1",
                    new String[] { newAPN.toLowerCase() }, null);
            if (cursor != null && cursor.moveToFirst()) {
                apnId = cursor.getString(cursor.getColumnIndex("_id"));
                Log.d(TAG, "updateCurrentAPN: " + apnId);
            }
        } catch (SQLException e) {
            // do nothing here
        }

        if (apnId != null) {
            cursor.close();
            return;
        }

        int column = -1;
        ContentValues values = new ContentValues();
        column = cursor.getColumnIndex("name");
        if (column >= 0)
            values.put("name", apn.getName());

        column = cursor.getColumnIndex("numeric");
        if (column >= 0)
            values.put("numeric", apn.getNumeric());

        column = cursor.getColumnIndex("mcc");
        if (column >= 0)
            values.put("mcc", apn.getMcc());

        column = cursor.getColumnIndex("mnc");
        if (column >= 0)
            values.put("mnc", apn.getMnc());

        column = cursor.getColumnIndex("apn");
        if (column >= 0)
            values.put("apn", apn.getApn());

        column = cursor.getColumnIndex("user");
        if (column >= 0)
            values.put("user", apn.getUser());

        column = cursor.getColumnIndex("server");
        if (column >= 0)
            values.put("server", apn.getServer());

        column = cursor.getColumnIndex("password");
        if (column >= 0)
            values.put("password", apn.getPassword());

        column = cursor.getColumnIndex("proxy");
        if (column >= 0)
            values.put("proxy", apn.getProxy());

        column = cursor.getColumnIndex("port");
        if (column >= 0)
            values.put("port", apn.getPort());

        column = cursor.getColumnIndex("mmsproxy");
        if (column >= 0)
            values.put("mmsproxy", apn.getMmsProxy());

        column = cursor.getColumnIndex("mmsport");
        if (column >= 0)
            values.put("mmsport", apn.getMmsPort());

        column = cursor.getColumnIndex("mmsc");
        if (column >= 0)
            values.put("mmsc", apn.getMmsc());

        column = cursor.getColumnIndex("authtype");
        if (column >= 0)
            values.put("authtype", apn.getAuthType() + "");

        column = cursor.getColumnIndex("type");
        if (column >= 0)
            values.put("type", apn.getType());

        column = cursor.getColumnIndex("current");
        if (column >= 0)
            values.put("current", apn.getCurrent());

        column = cursor.getColumnIndex("protocol");
        if (column >= 0)
            values.put("protocol", apn.getProtocol());

        column = cursor.getColumnIndex("roaming_protocol");
        if (column >= 0)
            values.put("roaming_protocol", apn.getRoamingProtocol());

        column = cursor.getColumnIndex("carrier_enabled");
        if (column >= 0)
            values.put("carrier_enabled", apn.getCarrierEnabled());

        column = cursor.getColumnIndex("bearer");
        if (column >= 0)
            values.put("bearer", apn.getBearer());

        cursor.close();

        Cursor c = null;
        try {
            Uri newRow = resolver.insert(APN_LIST_URI, values);
            if (newRow != null) {
                Log.d(TAG, "InsertAPN: " + newRow);
                c = resolver.query(newRow, null, null, null, null);
                int idindex = c.getColumnIndex("_id");
                c.moveToFirst();
                id = c.getShort(idindex);
            }
        } catch (SQLException e) {
            // do nothing here
        }

        if (c != null) {
            c.close();
        }
        setNowAPN(id);
    }

    private void setNowAPN(final int id) {
        ContentResolver resolver = this.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", id);
        try {
            resolver.update(CURRENT_APN_URI, values, null, null);
        } catch (SQLException e) {
        }
    }

    private boolean copyFile(String oldPath, String newPath) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            String f_new = "";
            File f_old = new File(oldPath);
            // check if enough space for copy
            StatFs sf = new StatFs(newPath);
            long vblockSize = (long)sf.getFreeBlocks() * (long)sf.getBlockSize();
            if(vblockSize < f_old.length()) {
                return false;
            }
            if(newPath.endsWith(File.separator)) {
                f_new = newPath + f_old.getName();
            } else {
                f_new = newPath + File.separator + f_old.getName();
            }
            new File(newPath).mkdirs();              //if folder isn't exist, then create
            new File(f_new).createNewFile();         //if file isn't exist, then create
             //if file is exist
            if(f_old.exists()) {
                Log.d(TAG, "old: "+oldPath);
                Log.d(TAG, "new: "+f_new);
                inStream = new FileInputStream(oldPath); //read the old file
                fs = new FileOutputStream(f_new);
                byte[] buffer = new byte[1444];
                while((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //byte count
                    fs.write(buffer, 0, byteread);
                }
                
                if(inStream != null) {
                    inStream.close();
                    inStream = null;
                }

                if(fs != null) {
                    fs.close();
                    fs = null;
                }
            }
        } catch(Exception e) {
            Log.e(TAG, e.toString());
            try { // release the 
                if(inStream != null) {
                    inStream.close();
                    inStream = null;
                }

                if(fs != null) {
                    fs.close();
                    fs = null;
                }
            } catch(IOException ioe) {
                
            }
            return false;
        }
        return true;
    }
    
}