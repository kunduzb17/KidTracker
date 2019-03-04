package com.google.kaist.lavi.java.kidtracker;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_NEED_PERMISSION = 1;

    private BluetoothAdapter mBluetoothAdapter;

    private ListView m_lvCarSeatList ;
//    private CarSeatListViewAdapter m_AdaptCarSeatList;

    private int m_nPosition;    //삭제할 CarSeat 의 리스트뷰에서의 position
//    private CarSeatListViewItem pItem;
    private ArrayList<String> maDeviceList;

    private String msSelectedCarSeatName;       //CarSeat ListView 에서 삭제하기 위해 선택한 카시스 이름
    private String msSelectedCarSeatMACAddr;    //CarSeat ListView 에서 삭제하기 위해 선택한 카시스 MAC Addr

    private LinearLayout m_layoutBG;

    private final String BROADCAST_MESSAGE = "kr.re.ciss.android.sleepingchildcare.ACTION_REFRESH_DATA";
    private  final IntentFilter intentFilter = new IntentFilter();
    private boolean mbIsMessageBoxShow = false;     //알림 메세지 창이 노출되었는지 여부
    private String TAG = "BluetoothChecking";
    private String PROVIDER = "providers";
    private String AUTH = "auth";
    private String BEACON = "beacons";
    private String HELLO = "hello";
    private String BYE = "bye";
    private String INDEX = "index";
    private boolean curIsBLEConnected;
    private boolean prevIsBLEConnected;
    private boolean authListChecked = false;

    private Integer kidBit;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference messagesRef;
    private DatabaseReference ref;
    private ValueEventListener eventListener;
    private List<String> authList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Log.d(TAG, "InitLayout Start");
        InitLayout();   //Activity 레이아웃 초기화
        Log.e(TAG, "InitLayout Done");

        if (!CheckPermission())	//마시멜로 이상(API >= 23) 에서만 퍼미션 체크
            return;
        kidBit = 0;

        curIsBLEConnected = false;
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        messagesRef = mFirebaseDatabaseReference.child("beacons");

        intentFilter.addAction(BROADCAST_MESSAGE);

        StartApp();

        if (!CheckTransmitServiceRunning()) {   //TransmitService 가 실행 중인지 확인.(true : 서비스 중, false : 서비스 중단 중)
            Intent intent = new Intent (this, TransmitService.class);
            startService(intent);

            Toast.makeText(this,"BLE tracking START",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

//        m_layoutBG.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.main_bg))); //MainActivity 배경그림 그리기

//        InitCarSeatList();  //프리퍼런스에서 등록된 카시트 데이타 가져오기

        DisplayData();  //Application 전역변수에서 데이타 가져와서 표시 하기  --> 경고메세지를 1회만 표시하기 위해서 onResume() 에서는 삭제 함.

        registerReceiver(mDataReceiver, intentFilter);
        Log.e(TAG, "onResume");

    }

    @Override
    protected void onStop() {
        super.onStop();

//        recycleView(m_layoutBG);

//        unregisterReceiver(mDataReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().BatteryValue = -1; //저장된 데이타 초기화

        ((KidTrackerApplication)getApplication()).SavePreferenceInfo();

        if (CheckTransmitServiceRunning()) {   //TransmitService 가 실행 중인지 확인.(true : 서비스 중, false : 서비스 중단 중)
            Intent intent = new Intent(this, TransmitService.class);
            stopService(intent);

            Toast.makeText(this,"Tracking Kid Terminated",Toast.LENGTH_SHORT).show();

        }

    }

    private void StartApp() {
        if (!CheckForBLE()) {   //블루투스가 꼭 필요하므로 블루투스 지원 안하면 어플을 강제 종료 시킴
            finish();

            return;

        }

        Log.e(TAG, "START APP");

        if (!mBluetoothAdapter.isEnabled()) {//블루투스가 꺼져있으면 블루투스 켜기
            mBluetoothAdapter.enable();
            Log.e(TAG, "Bluetooth just turned on");
        } else {
            Log.e(TAG, "Bluetooth was already on");
        }

        //GPS 가 켜져 있는 상태인지 확인.-시작 --> GPS 를 켜지 않았을 경우에는 어플 종료 시키는 코드 추가할 것
//        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
//        boolean bRet = lm.isProviderEnabled (LocationManager.GPS_PROVIDER);
//        if(!bRet)
//            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
        //GPS 가 켜져 있는 상태인지 확인.-끝
        Log.e(TAG, "START APP SUCCESS");
    }

    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.imgbtn_Setup: //SetupActivity 띄우기
//                Intent intent = new Intent (this, SetupActivity.class);
//                startActivity(intent);
//
//                break;
//
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100 :    //???? 결과

                break;

        }

    }

    private boolean CheckForBLE() {
        //BLE장치가 제공되는지를 확인한다.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This device does not support bluetooth", Toast.LENGTH_LONG).show();

            return false;

        }

        //안드로이드 폰에서 블루투스를 제공하는 OS인지 확인한다.(안드로이드 4.3- API 18 이상의 버전인지를 확인한다.)
        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //블루투스가 연결이 되었는지 체크한다.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "No Bluetooth connected", Toast.LENGTH_LONG).show();

            return false;

        }

        return true;

    }


    //퍼미션 관련 함수들-시작
    private boolean CheckPermission() {	//필요한 퍼미션 셋팅
        boolean bRet = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //마시멜로 이상(API > 23) 에서만 퍼미션 체크
            if (CheckPermission_Sub(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    && CheckPermission_Sub(this, Manifest.permission.SEND_SMS)
                    && CheckPermission_Sub(this, Manifest.permission.ACCESS_FINE_LOCATION)) {					//모든 퍼미션 있음.

                bRet = true;

            }
            else {	//필요한 퍼미션이 모두 없으므로 퍼미션 요청
                String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION};

                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_NEED_PERMISSION);

                bRet = false;

            }

        }

        return bRet;

    }

    //현재 특정 퍼미션이 앱에 있는지 확인
    private boolean CheckPermission_Sub(Activity a, String p) {
        int permissionResult = ContextCompat.checkSelfPermission(a, p);

        if (permissionResult == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;

    }

    private boolean verifyPermission(int[] grantresults){
        if (grantresults.length < 1) {
            return false;

        }

        for (int result : grantresults) {
            if (result != PackageManager.PERMISSION_GRANTED)
                return false;

        }

        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_NEED_PERMISSION) {
            if (verifyPermission(grantResults)){	//필요한 퍼미션 획득 성공(모두 얻음)
                StartApp();

            }
            else {	//필요한 퍼미션 획득 실패
                Toast.makeText(this, "Permissions are required for Kid Tracker", Toast.LENGTH_LONG).show();

                finish();

            }

        }

    }
    //퍼미션 관련 함수들-끝

    private void InitLayout() {

    }

    private boolean CheckTransmitServiceRunning() {
        boolean bRet = false;
        Log.e(TAG, "Check Transmit Service Running");

        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> l = mActivityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo item : l) {
            if (item.service.getClassName().equals(TransmitService.class.getName())) {
                bRet = true;

                break;
            }
        }
        return  bRet;
    }

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DisplayData();  //Application 전역변수에서 데이타 가져와서 표시 하기
        }
    };

    private boolean DisplayData() {

        //RSSI 값으로 BLE 연결 여부 판단 - 시작
        prevIsBLEConnected = curIsBLEConnected;


        if (((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().RSSI > ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().Leave_RSSI_Value)    //RSSI 값이 > -80 면 BLE 연결로 취급
        {
            curIsBLEConnected = true;
            Log.e(TAG, "BLE connected");
        } else {
            curIsBLEConnected = false;    //RSSI 값이 <= -80 면 BLE 연결 끊김으로 취급
            Log.e(TAG, "BLE not connected");
        }

        Log.d(TAG, "prev: " + Boolean.toString(prevIsBLEConnected));
        Log.d(TAG, "cur: " + Boolean.toString(curIsBLEConnected));

        if (prevIsBLEConnected ^ curIsBLEConnected) {
            // Dummy info
//            List<String> authList = new ArrayList<String>();
//            authList.add("F9hizkzVuCgtLzGC673dIOUk8jr2");
//            authList.add("mwu0obeeaSP52Gkn9PZ5Pdf3p7F2");

            // Fetch authlist from db
            if ( !authListChecked) {
                BeaconInfo beaconInfo = new BeaconInfo(authList, 0);
            }
            BeaconInfo beaconInfo = new BeaconInfo(authList, 0); //Kunduz's Change

            for (int i = 0; i < ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().bleList.size(); i++) {

                writeDatabase(beaconInfo, ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().bleList.get(i), curIsBLEConnected);
            }
            writeDatabase(beaconInfo, "0C:61:CF:A4:92:7D", curIsBLEConnected);
        }

        return curIsBLEConnected;

        //RSSI 값으로 BLE 연결 여부 판단 - 끝
    }

    private void writeDatabase(final BeaconInfo beaconInfo, String macAddr, final boolean isConnected) {
        String currUid = mFirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference currProviderRef = mFirebaseDatabaseReference.child(PROVIDER).child(currUid);
        final DatabaseReference beaconStageRef = mFirebaseDatabaseReference.child(BEACON).child(macAddr);
        final ValueEventListener roleListener = new ValueEventListener() {
            Role role;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                role = dataSnapshot.getValue(Role.class);
                String Hello = role.getHello();
                String Bye = role.getBye();
                String location;

                Date date = new Date();

                SimpleDateFormat format = new SimpleDateFormat("EEE hh:mm:ss aa, MMM dd");
                SimpleDateFormat path_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");

                String time = format.format(date);
                String path_str = path_format.format(date);

                Map<String, Object> authMsgUpdate = new HashMap<>();
                authMsgUpdate.put("/" + path_str + "/time", time);

                List<String> listAuth = beaconInfo.getAuth();
                Integer stage = beaconInfo.getStage();
                if (isConnected) {
                    location = Hello;
                } else {
                    location = Bye;
                }
                authMsgUpdate.put("/" + path_str + "/location", location);
                for (int i = 0; i < listAuth.toArray().length; i++) {
                    mFirebaseDatabaseReference.child(AUTH).child(listAuth.get(i)).updateChildren(authMsgUpdate);
                }

                Map<String, Object> beaconStageUpdate = new HashMap<>();
                beaconStageUpdate.put("/auth", listAuth);
                beaconStageUpdate.put("/stage", stage + 1);
                beaconInfo.setStage(stage + 1);

                beaconStageRef.updateChildren(beaconStageUpdate);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(getApplicationContext(),"loadPost:onCancelled",Toast.LENGTH_SHORT).show();
                // ...
            }
        };
        currProviderRef.addListenerForSingleValueEvent(roleListener);





//        ref.child(path_str).child("location").setValue(location);
//        ref.child(path_str).child("time").setValue(time);


//        currProviderRef.child(INDEX).setValue(ind + 1);
    }
}
