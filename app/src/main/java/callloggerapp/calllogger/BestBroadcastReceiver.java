package callloggerapp.calllogger;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Julian on 17/10/2017.
 */

public abstract class BestBroadcastReceiver extends BroadcastReceiver {


    // The receiver will be recreated whenever Android feels like it.
    // We need a static variable to remember data between instantiations

    static phoneCallStartEndDetector listener;
    String outgoingSavedNumber;
    protected Context savedContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        savedContext = context;
        if (listener == null) {
            listener = new phoneCallStartEndDetector();
        }

        // We listen to two intents. The new outgoing call only tells us of an outgoing call. We use it to get the no
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            listener.setOutgoingNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
            return;
        }

        // The other intent tells us the phone state changed. Here we set a listener to deal with it
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    // Derived classes should override these to respond to specific events of interest

    protected abstract void onIncomingCallStarted(String number, Date start);

    protected abstract void onOutgoingCallStarted(String number, Date start);

    protected abstract void onIncomingCallEnded(String number, Date start, Date end);

    protected abstract void onOutgoingCallEnded(String number, Date start, Date end);

    protected abstract void onMissedCall(String number, Date start);


    // Deals with actual events
    public class phoneCallStartEndDetector extends PhoneStateListener {
        int lastState = TelephonyManager.CALL_STATE_IDLE;
        Date callStartTime;
        boolean isIncoming;
        String savedNumber; // Because the passed incoming is only valid in ringing

        public phoneCallStartEndDetector() {

        }

        // The outgoing number is only sent via a separate intent, so we need to store it out of band
        public void setOutgoingNumber(String number) {
            savedNumber = number;
        }

        // Incoming call - goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when it's hung up
        // Outgoing call - goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (lastState == state) {
                // No change, debounce extras
                return;
            }

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    callStartTime = new Date();
                    savedNumber = incomingNumber;
                    onIncomingCallStarted(incomingNumber, callStartTime);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // Transition of ringing -> offhook are pickups of incoming calls. Nothing done on them
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        isIncoming = false;
                        callStartTime = new Date();
                        onOutgoingCallStarted(savedNumber, callStartTime);
                    }
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    // Went to idle - this is the end of a call. What type depends on previous state(s)
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        // Ring but no pickup - a miss
                        onMissedCall(savedNumber, callStartTime);
                    } else if (isIncoming) {
                        onIncomingCallEnded(savedNumber, callStartTime, new Date());
                    } else {
                        onOutgoingCallEnded(savedNumber, callStartTime, new Date());
                    }
                    break;
            }
            lastState = state;
        }

        // New missed call
        String where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + "=1";


        // Making a list of outgoing calls one by one automatically
        // https://stackoverflow.com/questions/9766002/making-a-list-of-outgoing-calls-one-by-one-automatically?rq=1


        public class CallsActivity extends Activity {

            final Context context = this;
            public String num;
            String LOG_TAG = "EMERGENCY CALL AAAH";

            public String[] pnum = {"9666848344", "9603939029", "7404230210", "9030109791"};
            ArrayList<String> b = new ArrayList<String>(Arrays.asList(pnum));

            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                num = b.get(0);
                call(num);
                // add PhoneStateListener

                PhoneCallListener phoneListener = new PhoneCallListener();
                TelephonyManager telephonymanager = (TelephonyManager) this.
                        getSystemService(Context.TELEPHONY_SERVICE);
                telephonymanager.listen(phoneListener,
                        PhoneStateListener.LISTEN_CALL_STATE);

            }

            private void call(String num1) {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + num1));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                savedContext.startActivity(callIntent);
                int indx = b.indexOf(num1);

                // Log.i (LOG_TAG, "indx" + indx);
                if (indx != b.size()) {
                    num1 = b.get(indx + 1);
                }

            }

        }
    }

    private class PhoneCallListener extends PhoneStateListener {
        private boolean isPhoneCalling = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended, need detect flag from CALL_STATE_OFFHOOK

//                if (isPhoneCalling) {
//                    // restart app
//                    Intent i = getBaseContext().getPackageManager().getLaunchIntentforPackage(
//                            getBaseContext().getPackageName());
//                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(i);
//                    call(num);
//                    isphoneCalling = false;

                }
            }


        }


    }






