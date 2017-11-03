package callloggerapp.calllogger;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.Manifest.permission.CALL_PHONE;
import static android.R.attr.keycode;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;


/// The beginning ///


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    boolean shouldExecuteOnResume;
    private static final int UNIQUE_REQUEST_CODE = 1;

    static final String KEY_NUMBER = "Nummer";
    static final String KEY_TIME = "00:00";
    static final String KEY_NAME = "Name";

    private ListView mListView;
    private ListAdapter mAdapter;
    private ArrayList<HashMap<String, String>> numberList;

    private Button callBtn;
    private LinearLayout buttons;
    private LinearLayout buttons2;
    private ArrayList<String> numbers = new ArrayList<>();
    private Button delBtn;
    private Button stopBtn;
    int i = 0;
//    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

//    java.util.Date date = new java.util.Date();
//    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");


    /// onCreate ///

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate called");


        shouldExecuteOnResume = false;

        // Does it ever do this?
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate() Restoring previous state");
        } else {
            Log.d(TAG, "onCreate() No saved state available");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberList = new ArrayList<HashMap<String, String>>();
        callBtn = (Button) findViewById(R.id.btn_startCall);
        stopBtn = (Button) findViewById(R.id.btn_stopCall);
        mListView = (ListView) findViewById(R.id.callListView);
        mAdapter = new ListAdapter(this, numberList);
        mListView.setAdapter(mAdapter);

        // Add a PhoneStateListener to monitor the phone calls
        MyPhoneListener phoneListener = new MyPhoneListener();
        TelephonyManager telephonyManager =
                (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        // Witness the changes
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


        callBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                try {

                    // Using the ACTION.CALL intent, you're going straight to the first call
                    Intent callIntentOne = new Intent(Intent.ACTION_CALL);


                    String numberToCall = numberList.get(0).get(KEY_NUMBER);

                    // Calling number (0)!
                    callIntentOne.setData(Uri.parse("tel: " + numberToCall));

                    startActivity(callIntentOne);

                    // Check for permission, write yes/no etc. here
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{CALL_PHONE},
                                UNIQUE_REQUEST_CODE);
                    } else {
                        Toast.makeText(MainActivity.this, "Permission granted! Thank you!", Toast.LENGTH_SHORT).show();

                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Oh no, your call has failed!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    permission.READ_CALL_LOG)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission.READ_CALL_LOG}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission.READ_CALL_LOG}, 1);
            }
        } else {
            // do stuff
            // TextView textView = (TextView) findViewById(R.id.listView);
            // textView.setText(getCallDetails());
            getCallDetails();
        }


        /// The end of onCreate ///
    }


    // Method onMissedCall which adds missed calls to the ArrayList and the ListView
    public void onMissedCall(String cachedName, String numberToAdd, Date start) {

        Log.d(TAG, "Adding number to the list...");

        HashMap<String, String> map = new HashMap<String, String>();

        // adding each child node to HashMap key value
        map.put(KEY_NUMBER, numberToAdd);
        map.put(KEY_TIME, start.toString());

        // Always null rn
        map.put(KEY_NAME, cachedName);

        String missedNo = numberToAdd;

        // Adds the correct numbers
        numbers.add(missedNo);
        numberList.add(map);
    }


    // Method that deals with the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == UNIQUE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thank you! Permission granted!", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, CALL_PHONE)) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                    dialog.setMessage("You can't call people without this permission! Please permit it!")
                            .setTitle("Important permission required!");

                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{CALL_PHONE},
                                    UNIQUE_REQUEST_CODE);
                        }
                    });

                    dialog.setNegativeButton("NO THANKS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(MainActivity.this, "Cannot be done!", Toast.LENGTH_SHORT).show();

                        }
                    });
                    dialog.show();
                }
            } else {
                Toast.makeText(this, "We will never show you this again!", Toast.LENGTH_SHORT).show();
            }
        }
    }

                /*

                 Method that fetches calls with their details.

                Creates an object with the details, e.g. number, type, date, etc. Then instead of appending to sb,
                create 1 object for each call log with the details stored in the vars.
                When you got the list of objects you can create a custom adapter to show them in a ListView.
                */

    public String getCallDetails() {

        Log.d(TAG, "Getting the call details");

        StringBuffer sb = new StringBuffer();
        String timestamp = String.valueOf(getTodayTimestamp());

        if (ActivityCompat.checkSelfPermission(this, permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//           return;
        }

//        Cursor managedCursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.DATE + ">= ?", new String[]{timestamp}, null);

        // prev:
        Cursor managedCursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");

        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);


        sb.append("\n");

        while (managedCursor.moveToNext()) {

            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            String cachedName = managedCursor.getString(name);

            Date callDayTime = new Date(Long.valueOf(callDate));
            String dir = null;
            int dircode = Integer.parseInt(callType);

            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                // If call belongs to the MISSED type, add it to the numbers Arraylist
                // With this code: displays the correct call times, but shows ALL the missed calls, not just the ones from today
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";

                    if (DateUtils.isToday(Long.parseLong(callDate))) {

                        onMissedCall(cachedName, phNumber, callDayTime);
                        break;
                    } else {
                        break;
                    }

//                    onMissedCall(phNumber, callDayTime);
//                    break;
            }

            // If you're a missed call, we want to display you in the log. THIS IS NOT USED ANYMORE.
//            if (dir == "MISSED") {
//
//                sb.append("\n   Phone Number: " + phNumber + " \n   Call Date: " + callDayTime + "\n");
//                sb.append("   -----------------------------------------------------\n \n");
//            }
        }

        // Closing the cursor and returning the sb
        managedCursor.close();
        return sb.toString();
    }



    /*
     Getting the timestamp of today at midnight
     TODO: Setting the timestamp to the time you received the missed call
     */

    public long getTodayTimestamp() {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(new Date());

        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
        c2.set(Calendar.MONTH, c1.get(Calendar.MONTH));
        c2.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);

        return c2.getTimeInMillis();
    }


    // Listens to phone states (IDLE, OFFHOOK, RINGING)
    public class MyPhoneListener extends PhoneStateListener {

        private boolean isCalling = false;

        // When the phone state changes, do something
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            // What do we do in what particular state?
            switch (state) {
                // Phone is ringing
                case TelephonyManager.CALL_STATE_RINGING:

                    Toast.makeText(MainActivity.this, incomingNumber + "INCOMING CALL!!!", Toast.LENGTH_LONG).show();
                    break;

                // Phone is off the hook, AKA busy, active, or on hold
                case CALL_STATE_OFFHOOK:

                    Toast.makeText(MainActivity.this, "On call...", Toast.LENGTH_LONG).show();
                    // When user answers the incoming call, isCalling becomes true
                    isCalling = true;
                    break;

                // Phone is idle, which is when the phone call is finished or when it's not in a call
                case CALL_STATE_IDLE:

                    //  IF NOT ON CALL, since it's set to false by default
                    if (isCalling && numbers.size() == 0) {
                        finish();
                    } else if (isCalling) {
                        // Restarting the application with this restart intent
//                        Intent restartApp = getBaseContext().getPackageManager().
//                                getLaunchIntentForPackage(getBaseContext().getPackageName());
//                        restartApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(restartApp);
                    }

                    // We're not on a call anymore, so we're setting onCall to false
                    isCalling = false;

                    // If none of this is happening, we're not doing anything, so just break it up
                default:
                    Toast.makeText(MainActivity.this, "Hi, I'm the switch default!",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    // This is where the app lives when a call is active
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        shouldExecuteOnResume = true;
    }

    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    // Where the app goes after onPause
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    // We never really reach this one
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        mAdapter.notifyDataSetChanged();

    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Log.d("Test", "Back button pressed!");
            super.onBackPressed();
        }

        return super.onKeyDown(keycode, event);

    }



//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        // Save the user's current state
//
//        int currentNumber = Integer.parseInt(KEY_NUMBER);
//        int currentDate = Integer.parseInt(KEY_TIME);
//
//        savedInstanceState.putInt(KEY_NUMBER, currentNumber);
//        savedInstanceState.putInt(KEY_TIME, currentDate);
//
//        // Always call the superclass so it can save the view hierarchy state
//        super.onSaveInstanceState(savedInstanceState);
//    }


    // We're calling this when we close a call. It will launch a call intent for the next number in the list
    // For convenience, if you reboot while the list is empty, you'll see the missed calls for today again
    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart called");

        callBtn.setText("Click to continue!");
        numbers.size();
        numberList.remove(0);
        mAdapter.notifyDataSetChanged();
        final Handler mHandler = new Handler();
        stopBtn.setVisibility(View.VISIBLE);

        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mHandler.removeCallbacksAndMessages(null);
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

//                mHandler.removeCallbacksAndMessages(null);

                try {

                    // Using the ACTION.CALL intent, you're going straight to the first call
                    Intent callIntentOne = new Intent(Intent.ACTION_CALL);


                    String numberToCall = numberList.get(0).get(KEY_NUMBER);

                    // Calling number (0)!
                    callIntentOne.setData(Uri.parse("tel: " + numberToCall));

                    startActivity(callIntentOne);

                    // Check for permission, write yes/no etc. here
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{CALL_PHONE},
                                UNIQUE_REQUEST_CODE);
                    } else {
                        Toast.makeText(MainActivity.this, "Permission granted! Thank you!", Toast.LENGTH_SHORT).show();

                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Oh no, your call has failed!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }

        });


        if (numberList.size() == 0) {
            shouldExecuteOnResume = false;
            onStop();
        }

        // If shouldExecuteOnResume = true
        if (shouldExecuteOnResume) {
            Toast.makeText(MainActivity.this, "Calling the next number in 5 seconds!", Toast.LENGTH_LONG).show();

            numbers.size();

            String numberToCall = null;
            // Quits the app when the list is empty <-- NOT REACHED? We're toasting, cheers
            if (numberList.size() == 0) {
                finish();
            } else {
                numberToCall = numberList.get(0).get(KEY_NUMBER);
                Log.d(TAG, "GETTING NEXT NUMBER: " + numberToCall);
            }

            Intent callIntentTwo = new Intent(Intent.ACTION_CALL);
            callIntentTwo.setData(Uri.parse("tel: " + numberToCall));

            if (ActivityCompat.checkSelfPermission(this, permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // Should already have permission, but check is necessary
                return;
            }

            if (ActivityCompat.checkSelfPermission(this, permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            final String finalNumberToCall = numberToCall;


            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    Intent callIntentTwo = new Intent(Intent.ACTION_CALL);
                    callIntentTwo.setData(Uri.parse("tel: " + finalNumberToCall));
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(callIntentTwo);


                }
            }, 5000);

//            startActivity(callIntentTwo);

        }

    }


    // We arrive here when we're out of numbers to call
    public void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "onPostResume called");

        if (numberList.size() == 0) {

            // Can make things invisible here, we're just going to show a long toast
            callBtn.setText("Finished!");
            Toast.makeText(MainActivity.this, "FINISHED CALLING MISSED CALLS!", Toast.LENGTH_LONG).show();

        }
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }
//
//        callBtn.setText("Click to continue!");
//        numbers.size();
//        numberList.remove(0);
//        mAdapter.notifyDataSetChanged();
//        final Handler mHandler = new Handler();
//        stopBtn.setVisibility(View.VISIBLE);
//
//        stopBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                mHandler.removeCallbacksAndMessages(null);
//            }
//        });
//
//        callBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
////                mHandler.removeCallbacksAndMessages(null);
//
//                try {
//
//                    // Using the ACTION.CALL intent, you're going straight to the first call
//                    Intent callIntentOne = new Intent(Intent.ACTION_CALL);
//
//
//                    String numberToCall = numberList.get(0).get(KEY_NUMBER);
//
//                    // Calling number (0)!
//                    callIntentOne.setData(Uri.parse("tel: " + numberToCall));
//
//                    startActivity(callIntentOne);
//
//                    // Check for permission, write yes/no etc. here
//                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) !=
//                            PackageManager.PERMISSION_GRANTED) {
//                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{CALL_PHONE},
//                                UNIQUE_REQUEST_CODE);
//                    } else {
//                        Toast.makeText(MainActivity.this, "Permission granted! Thank you!", Toast.LENGTH_SHORT).show();
//
//                    }
//
//                } catch (Exception e) {
//                    Toast.makeText(getApplicationContext(), "Oh no, your call has failed!",
//                            Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
//                }
//
//            }
//
//        });
//
//
//        if (numberList.size() == 0) {
//            shouldExecuteOnResume = false;
//            onStop();
//        }
//
//        // If shouldExecuteOnResume = true
//        if (shouldExecuteOnResume) {
//            Toast.makeText(MainActivity.this, "Calling the next number in 5 seconds!", Toast.LENGTH_LONG).show();
//
//            numbers.size();
//
//            String numberToCall = null;
//            // Quits the app when the list is empty <-- NOT REACHED? We're toasting, cheers
//            if (numberList.size() == 0) {
//                finish();
//            } else {
//                numberToCall = numberList.get(0).get(KEY_NUMBER);
//                Log.d(TAG, "GETTING NEXT NUMBER: " + numberToCall);
//            }
//
//            Intent callIntentTwo = new Intent(Intent.ACTION_CALL);
//            callIntentTwo.setData(Uri.parse("tel: " + numberToCall));
//
//            if (ActivityCompat.checkSelfPermission(this, permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                // Should already have permission, but check is necessary
//                return;
//            }
//
//            if (ActivityCompat.checkSelfPermission(this, permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//
//            final String finalNumberToCall = numberToCall;
//
//
//            mHandler.postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    Intent callIntentTwo = new Intent(Intent.ACTION_CALL);
//                    callIntentTwo.setData(Uri.parse("tel: " + finalNumberToCall));
//                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    startActivity(callIntentTwo);
//
//
//                }
//            }, 5000);
//
////            startActivity(callIntentTwo);
//
//        }
//    }







    // Can anyone tell me why the onResume isn't doing what it's supposed to do?

//    @Override
//    public void onResume() {
//
//        // Runs on call which is not OK!!!!! Removes (0)
//        Log.d(TAG, "onResume called");
//        super.onResume();
//
//    }
}

//        else if (isForeground("callloggerapp.calllogger")){
//            shouldExecuteOnResume = true;
//        }


//            for (int i = 0; i < numbers.size(); i++) {
//
//                Intent callIntentTwo = new Intent(Intent.ACTION_CALL);
//                callIntentTwo.setData(Uri.parse("tel: " + numbers.get(i)));
//
//
//                if (i == numbers.size()) {
//
//                    break;
//
//                }
//
//                if (ActivityCompat.checkSelfPermission(this, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                startActivity(callIntentTwo);
//
//            }



