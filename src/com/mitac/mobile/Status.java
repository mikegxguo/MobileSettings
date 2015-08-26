package com.mitac.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.telephony.TelephonyProperties;
//import com.android.internal.telephony.PhoneConstants; //Android42

import java.lang.ref.WeakReference;

/**
 * Display the following information
 * # Phone Number
 * # Network
 * # Roaming
 * # Device Id (IMEI in GSM and MEID in CDMA)
 * # Network type
 * # Signal Strength
 * # Battery Strength  : TODO
 * # Uptime
 * # Awake Time
 * # XMPP/buzz/tickle status : TODO
 *
 */
public class Status extends PreferenceActivity{
    /** Called when the activity is first created. */
    static String TAG = "Status";

    private static final String KEY_DATA_STATE = "data_state";
    private static final String KEY_SERVICE_STATE = "service_state";
    private static final String KEY_OPERATOR_NAME = "operator_name";
    private static final String KEY_ROAMING_STATE = "roaming_state";
    private static final String KEY_NETWORK_TYPE = "network_type";
    private static final String KEY_PHONE_NUMBER = "number";
    private static final String KEY_IMEI_SV = "imei_sv";
    private static final String KEY_IMEI = "imei";
    private static final String KEY_PRL_VERSION = "prl_version";
    private static final String KEY_MIN_NUMBER = "min_number";
    private static final String KEY_MEID_NUMBER = "meid_number";
    private static final String KEY_SIGNAL_STRENGTH = "signal_strength";   
    private static final String KEY_ICC_ID = "icc_id";
    
    private static final String[] PHONE_RELATED_ENTRIES = {
        KEY_DATA_STATE,
        KEY_SERVICE_STATE,
        KEY_OPERATOR_NAME,
        KEY_ROAMING_STATE,
        KEY_NETWORK_TYPE,
        KEY_PHONE_NUMBER,
        KEY_IMEI,
        KEY_IMEI_SV,
        KEY_PRL_VERSION,
        KEY_MIN_NUMBER,
        KEY_MEID_NUMBER,
        KEY_SIGNAL_STRENGTH,
        KEY_ICC_ID
    };

    private static final int EVENT_SIGNAL_STRENGTH_CHANGED = 200;
    private static final int EVENT_SERVICE_STATE_CHANGED = 300;

    private static final int EVENT_UPDATE_STATS = 500;
    private static boolean mSkuHas35GModule;

    private TelephonyManager mTelephonyManager;
    private Phone mPhone = null;
    private PhoneStateIntentReceiver mPhoneStateReceiver;
    private Resources mRes;
    private Preference mSignalStrength;

    private static String sUnknown;


    private Handler mHandler;

    private static class MyHandler extends Handler {
        private WeakReference<Status> mStatus;

        public MyHandler(Status activity) {
            mStatus = new WeakReference<Status>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Status status = mStatus.get();
            if (status == null) {
                return;
            }

            switch (msg.what) {
                case EVENT_SIGNAL_STRENGTH_CHANGED:
                    status.updateSignalStrength();
                    break;

                case EVENT_SERVICE_STATE_CHANGED:
                    ServiceState serviceState = status.mPhoneStateReceiver.getServiceState();
                    status.updateServiceState(serviceState);
                    break;
            }
        }
    }


    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onDataConnectionStateChanged(int state) {
            updateDataState();
            updateNetworkType();
        }
    };

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
           //return false; //Android23 doesn't support the interface above
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.layout.status);

        Preference removablePref;

        mHandler = new MyHandler(this);

        mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);


        mRes = getResources();
        if (sUnknown == null) {
            sUnknown = mRes.getString(R.string.device_info_default);
        }
        mSkuHas35GModule = true;//mRes.getBoolean(com.android.internal.R.bool.config_with_35Gmodule);
        if(mSkuHas35GModule == false) {
            removePreferenceFromScreen(KEY_OPERATOR_NAME);
            removePreferenceFromScreen(KEY_NETWORK_TYPE);
            removePreferenceFromScreen(KEY_DATA_STATE);
            removePreferenceFromScreen(KEY_SERVICE_STATE);
            removePreferenceFromScreen(KEY_ROAMING_STATE);
            removePreferenceFromScreen(KEY_PHONE_NUMBER);
            removePreferenceFromScreen(KEY_IMEI);
            removePreferenceFromScreen(KEY_IMEI_SV);
            removePreferenceFromScreen(KEY_SIGNAL_STRENGTH);
        }

        if(mSkuHas35GModule) {
            mPhone = PhoneFactory.getDefaultPhone();
        }
        // Note - missing in zaku build, be careful later...
        mSignalStrength = findPreference(KEY_SIGNAL_STRENGTH);

        if (isWifiOnly(getApplicationContext())) {
            for (String key : PHONE_RELATED_ENTRIES) {
                removePreferenceFromScreen(key);
            }
        } else {
            // NOTE "imei" is the "Device ID" since it represents
            //  the IMEI in GSM and the MEID in CDMA
            Log.d(TAG, "Mobile is available\n");
            if (mPhone!=null && mPhone.getPhoneName().equals("CDMA")) {
                setSummaryText(KEY_MEID_NUMBER, mPhone.getMeid());
                setSummaryText(KEY_MIN_NUMBER, mPhone.getCdmaMin());
                if (getResources().getBoolean(R.bool.config_msid_enable)) {
                    findPreference(KEY_MIN_NUMBER).setTitle(R.string.status_msid_number);
                }
                setSummaryText(KEY_PRL_VERSION, mPhone.getCdmaPrlVersion());
                removePreferenceFromScreen(KEY_IMEI_SV);

                //if (mPhone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) {//Android42
                if (mPhone.getLteOnCdmaMode() == Phone.LTE_ON_CDMA_TRUE) {//Android40
                //if (true) {//Android23
                    // Show ICC ID and IMEI for LTE device
                    setSummaryText(KEY_ICC_ID, mPhone.getIccSerialNumber());
                    setSummaryText(KEY_IMEI, mPhone.getImei());
                    //setSummaryText(KEY_IMEI, "Not supported");//Android23
                } else {
                    // device is not GSM/UMTS, do not display GSM/UMTS features
                    // check Null in case no specified preference in overlay xml
                    removePreferenceFromScreen(KEY_IMEI);
                    removePreferenceFromScreen(KEY_ICC_ID);
                }
            } else {
                Log.d(TAG, "Not CDMA");
                if(mPhone!=null) setSummaryText(KEY_IMEI, mPhone.getDeviceId());

                setSummaryText(KEY_IMEI_SV,
                        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                            .getDeviceSoftwareVersion());

                // device is not CDMA, do not display CDMA features
                // check Null in case no specified preference in overlay xml
                removePreferenceFromScreen(KEY_PRL_VERSION);
                removePreferenceFromScreen(KEY_MEID_NUMBER);
                removePreferenceFromScreen(KEY_MIN_NUMBER);
                removePreferenceFromScreen(KEY_ICC_ID);
            }

            String rawNumber = null;
            if(mPhone!=null) rawNumber = mPhone.getLine1Number();  // may be null or empty
            String formattedNumber = null;
            if (!TextUtils.isEmpty(rawNumber)) {
                formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
            }
            // If formattedNumber is null or empty, it'll display as "Unknown".
            setSummaryText(KEY_PHONE_NUMBER, formattedNumber);

            mPhoneStateReceiver = new PhoneStateIntentReceiver(this, mHandler);
            mPhoneStateReceiver.notifySignalStrength(EVENT_SIGNAL_STRENGTH_CHANGED);
            mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);
            Log.d(TAG, "onCreate");
        }
    }

        @Override
        protected void onResume() {
            super.onResume();

            if (!isWifiOnly(getApplicationContext())) {
                Log.d(TAG, "onResume");
                mPhoneStateReceiver.registerIntent();

                updateSignalStrength();
                if(mPhone!=null) updateServiceState(mPhone.getServiceState());
                updateDataState();

                mTelephonyManager.listen(mPhoneStateListener,
                          PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
            }
            mHandler.sendEmptyMessage(EVENT_UPDATE_STATS);
        }

        @Override
        public void onPause() {
            super.onPause();

            if (!isWifiOnly(getApplicationContext())) {
                mPhoneStateReceiver.unregisterIntent();
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
            mHandler.removeMessages(EVENT_UPDATE_STATS);
        }

        /**
         * Removes the specified preference, if it exists.
         * @param key the key for the Preference item
         */
        private void removePreferenceFromScreen(String key) {
            Preference pref = findPreference(key);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        /**
         * @param preference The key for the Preference item
         * @param property The system property to fetch
         * @param alt The default value, if the property doesn't exist
         */
        private void setSummary(String preference, String property, String alt) {
            try {
                findPreference(preference).setSummary(
                        SystemProperties.get(property, alt));
            } catch (RuntimeException e) {

            }
        }

        private void setSummaryText(String preference, String text) {
                if (TextUtils.isEmpty(text)) {
                   text = sUnknown;
                 }
                 // some preferences may be missing
                 if (findPreference(preference) != null) {
                     findPreference(preference).setSummary(text);
                 }
        }

        private void updateNetworkType() {
            // Whether EDGE, UMTS, etc...
            setSummary(KEY_NETWORK_TYPE, TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE, sUnknown);
        }

        private void updateDataState() {
            int state = mTelephonyManager.getDataState();
            String display = mRes.getString(R.string.radioInfo_unknown);

            switch (state) {
                case TelephonyManager.DATA_CONNECTED:
                    display = mRes.getString(R.string.radioInfo_data_connected);
                    break;
                case TelephonyManager.DATA_SUSPENDED:
                    display = mRes.getString(R.string.radioInfo_data_suspended);
                    break;
                case TelephonyManager.DATA_CONNECTING:
                    display = mRes.getString(R.string.radioInfo_data_connecting);
                    break;
                case TelephonyManager.DATA_DISCONNECTED:
                    display = mRes.getString(R.string.radioInfo_data_disconnected);
                    break;
            }

            setSummaryText(KEY_DATA_STATE, display);
        }

        private void updateServiceState(ServiceState serviceState) {
            int state = serviceState.getState();
            String display = mRes.getString(R.string.radioInfo_unknown);

            switch (state) {
                case ServiceState.STATE_IN_SERVICE:
                    display = mRes.getString(R.string.radioInfo_service_in);
                    break;
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_EMERGENCY_ONLY:
                    display = mRes.getString(R.string.radioInfo_service_out);
                    break;
                case ServiceState.STATE_POWER_OFF:
                    display = mRes.getString(R.string.radioInfo_service_off);
                    break;
            }

            setSummaryText(KEY_SERVICE_STATE, display);

            if (serviceState.getRoaming()) {
                setSummaryText(KEY_ROAMING_STATE, mRes.getString(R.string.radioInfo_roaming_in));
            } else {
                setSummaryText(KEY_ROAMING_STATE, mRes.getString(R.string.radioInfo_roaming_not));
            }
            setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaLong());
        }

        void updateSignalStrength() {
            // TODO PhoneStateIntentReceiver is deprecated and PhoneStateListener
            // should probably used instead.

            // not loaded in some versions of the code (e.g., zaku)
            if (mSignalStrength != null) {
                int state =
                        mPhoneStateReceiver.getServiceState().getState();
                Resources r = getResources();

                if ((ServiceState.STATE_OUT_OF_SERVICE == state) ||
                        (ServiceState.STATE_POWER_OFF == state)) {
                    mSignalStrength.setSummary("0");
                }

                int signalDbm = mPhoneStateReceiver.getSignalStrengthDbm();

                if (-1 == signalDbm) signalDbm = 0;

                int signalAsu = mPhoneStateReceiver.getSignalStrengthLevelAsu();
                //int signalAsu = mPhoneStateReceiver.getSignalStrength();//Android23

                if (-1 == signalAsu) signalAsu = 0;

                mSignalStrength.setSummary(String.valueOf(signalDbm) + " "
                            + r.getString(R.string.radioInfo_display_dbm) + "   "
                            + String.valueOf(signalAsu) + " "
                            + r.getString(R.string.radioInfo_display_asu));
            }
        }

    }
