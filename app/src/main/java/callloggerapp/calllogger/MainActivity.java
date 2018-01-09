package callloggerapp.calllogger;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.Manifest.permission.CALL_PHONE;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;

public class MainActivity extends AppCompatActivity {

    // Declaring variables
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int UNIQUE_REQUEST_CODE = 1;

    static final String KEY_NUMBER = "Nummer";
    static final String KEY_TIME = "00:00";
    static final String KEY_NAME = "Name";

    private ListView mListView;
    private ListAdapter mAdapter;
    private ArrayList<HashMap<String, String>> numberList;
    private ArrayList<String> numbers = new ArrayList<>();
    private Button stopBtn;
    private FloatingActionButton addBtn;
    private Button moveUpBtn;
    private boolean shouldExecuteOnResume;
    private boolean startedCalling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ActivityCompat.checkSelfPermission(this, permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{CALL_PHONE},
                    UNIQUE_REQUEST_CODE);
        }

        Log.d(TAG, "onCreate called");

        shouldExecuteOnResume = false;

//        Does it ever do this?
//        if (savedInstanceState != null) {
//            Log.d(TAG, "onCreate() Restoring previous state");
//        } else {
//            Log.d(TAG, "onCreate() No saved state available");
//        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing variables
        numberList = new ArrayList<HashMap<String, String>>();
        addBtn = (FloatingActionButton) findViewById(R.id.btn_addContact);
        stopBtn = (Button) findViewById(R.id.btn_stopCall);
        moveUpBtn = (Button) findViewById(R.id.moveup_btn);
        mListView = (ListView) findViewById(R.id.callListView);
        mAdapter = new ListAdapter(this, numberList);
        mListView.setAdapter(mAdapter);

        // phoneListener monitors the phone calls, reads status of phone
        MyPhoneListener phoneListener = new MyPhoneListener();
        TelephonyManager telephonyManager =
                (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        // Without this call, records aren't shown
        getCallDetails();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME},
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        String name = c.getString(1);
                        Date currentTime = Calendar.getInstance().getTime();
                        onMissedCall(name, number, currentTime);
                        mAdapter.notifyDataSetChanged();
                    }

                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.callbtnmenu, menu);
        return true;

    }

    // Perform call(s)
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml
        try {

            startedCalling = true;

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
//                        Toast.makeText(MainActivity.this, "Permission granted! Thank you!", Toast.LENGTH_SHORT).show();

            }

        } catch (Exception e) {
            startedCalling = false;
            Toast.makeText(getApplicationContext(), "Oh no, your call has failed!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }


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


    }

        return super.onOptionsItemSelected(item);
    }

    public void dismissCall(Context testContext, String phNumber) {

        Log.d(TAG, "dismissing call " + phNumber);

        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NEW, 0);
        values.put(CallLog.Calls.IS_READ, 1);
        StringBuilder where = new StringBuilder();
        where.append(CallLog.Calls.NUMBER);
        where.append(" = " + phNumber);

        if (ActivityCompat.checkSelfPermission(testContext, permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            testContext.getContentResolver().update(CallLog.Calls.CONTENT_URI, values, where.toString(),
                    new String[]{Integer.toString(CallLog.Calls.MISSED_TYPE)});

        }
    }


    // Method onMissedCall which adds missed calls to the ArrayList and the ListView
    public void onMissedCall(String cachedName, String numberToAdd, Date start) {


        Log.d(TAG, "Adding number to the list...");

        HashMap<String, String> map = new HashMap<String, String>();
        SimpleDateFormat date_to_time_format = new SimpleDateFormat("HH:mm:ss");

        map.put(KEY_NUMBER, numberToAdd);
        map.put(KEY_TIME, date_to_time_format.format(start).toString());
        map.put(KEY_NAME, cachedName);

        String missedNo = numberToAdd;
        numbers.add(missedNo);
        numberList.add(map);
    }


    // Method that deals with the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == UNIQUE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    public String getCallDetails() {

        Log.d(TAG, "Getting the call details");

        StringBuffer sb = new StringBuffer();
        String timestamp = String.valueOf(getTodayTimestamp());

        if (ActivityCompat.checkSelfPermission(this, permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
        }

        Cursor managedCursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE, CallLog.Calls.NEW}, null, null, CallLog.Calls.DATE + " DESC");

        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int isNew = managedCursor.getColumnIndex(CallLog.Calls.NEW);

        sb.append("\n");

        while (managedCursor.moveToNext()) {

            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            String cachedName = managedCursor.getString(name);
            String phIsNew = managedCursor.getString(isNew);

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
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";

                    if (DateUtils.isToday(Long.parseLong(callDate)) && phIsNew.equals("1")) {

                        onMissedCall(cachedName, phNumber, callDayTime);
                        break;
                    } else {
                        break;
                    }
            }
        }
        // Closing the cursor and returning the sb
        managedCursor.close();
        return sb.toString();
    }

    // Today at midnight
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



    //////////// Inner class that listens to phone states
    public class MyPhoneListener extends PhoneStateListener {

        private boolean isCalling = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {

                // Phone's a-ringin'
                case TelephonyManager.CALL_STATE_RINGING:
                    break;

                // Phone is off the hook, AKA busy, active, or on hold
                case CALL_STATE_OFFHOOK:

                    // When user answers the incoming call, isCalling becomes true
                    isCalling = true;
                    break;

                // Phone is idle, which is when the phone call is finished or when it's not in a call
                case CALL_STATE_IDLE:

                    final Handler mHandler = new Handler();

                    if (startedCalling) {
                        numbers.size();
                        numberList.remove(0);
                        mAdapter.notifyDataSetChanged();
                        stopBtn.setVisibility(View.VISIBLE);
                    }

                    stopBtn.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            startedCalling = false;
                            mHandler.removeCallbacksAndMessages(null);
                        }
                    });

                    if (numberList.size() == 0) {
                        stopBtn.setVisibility(View.INVISIBLE);
                        startedCalling = false;
                        shouldExecuteOnResume = false;
                    }

                    if (startedCalling && shouldExecuteOnResume) {
//                        Toast.makeText(MainActivity.this, " Bla!", Toast.LENGTH_LONG).show();

                        numbers.size();

                        String numberToCall = null;

                        if (numberList.size() == 0) {
                          stopBtn.setVisibility(View.INVISIBLE);
                        } else {
                            numberToCall = numberList.get(0).get(KEY_NUMBER);
                        }

                        Intent callIntentTwo = new Intent(Intent.ACTION_CALL);
                        callIntentTwo.setData(Uri.parse("tel: " + numberToCall));

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        final String finalNumberToCall = numberToCall;

                        mHandler.postDelayed(new Runnable() {

                            public void run() {

                                Intent callIntentTwo = new Intent(Intent.ACTION_CALL);
                                callIntentTwo.setData(Uri.parse("tel: " + finalNumberToCall));
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }

                                startActivity(callIntentTwo);
                            }
                        }, 5000);

                    }


                    if (isCalling && numbers.size() == 0) {
                        stopBtn.setVisibility(View.INVISIBLE);
                    } else if (isCalling) {


                    }

                    isCalling = false;


                default:

                    break;

            }
        }
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        shouldExecuteOnResume = true;
    }

    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }


    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        mAdapter.notifyDataSetChanged();
    }

    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart called");
    }

    public void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "onPostResume called");
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        if (numbers.isEmpty()) {
            stopBtn.setVisibility(View.INVISIBLE);
        }
    }

}