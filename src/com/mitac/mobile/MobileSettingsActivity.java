package com.mitac.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.telephony.TelephonyManager;

import com.mitac.xml.model.APN;
import com.mitac.xml.parser.APNParser;
import com.mitac.xml.parser.PullAPNParser;

public class MobileSettingsActivity extends Activity {
    /** Called when the activity is first created. */
    static String TAG = "MobileSettingsActivity";
    static Uri CURRENT_APN_URI = Uri
            .parse("content://telephony/carriers/preferapn");
    static Uri APN_LIST_URI = Uri.parse("content://telephony/carriers");
    final String APN_APP_FILE = "apns.xml";
    final String APP_FILE_PATH = "/data/data/com.mitac.mobile/files/apns.xml";

    private APNParser parser = null;
    private List<APN> apns = null;
    String sdpath = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
            Log.e(TAG, e.getMessage());
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

    public void onGetStatus(View v) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.Status");
        startActivity(intent);        
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

}