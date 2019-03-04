package com.google.kaist.lavi.java.kidtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransmitService extends Service {
//    private final static String TAG = TransmitService.class.getSimpleName();
    private final static String TAG = "======BluetoothChecking";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private ArrayList<String> maDeviceList;
    private ArrayList<DeviceInfo> curTrackingList;
    private ArrayList<BeaconInfo> detectableBeaconList;

    private boolean mbIsPressure;	//좌석 착지 여부(true : ON, false : OFF)
    private int miBattery;			//좌석 배터리 잔량
    private int miTemperature;		//좌석 온도
    private int miRSSI;				//BLE RSSI 값

    private static int GetDataInterval = 3000;	    //데이타 갱신 시간 (3초)
    private static int BLECheckInterval = 10000;	//BLE 연결이 유효한지 체크하는 시간주기 (10초)

    private final String BROADCAST_MESSAGE = "kr.re.ciss.android.sleepingchildcare.ACTION_REFRESH_DATA";

    private boolean mbIsBLEConnected = false;	//BLE 장치와 연결되었는지 여부 flag

    private int FOREGROUND_NOTI_ID = 1;

    private PowerManager.WakeLock sCpuWakeLock;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference messagesRef;
    private DatabaseReference ref;
    private ValueEventListener eventListener;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        Log.e(TAG, "onCreate");

        // 안드로이드 폰에서 블루투스를 제공하는 OS인지 확인한다.(안드로이드 4.3- API 18 이상의 버전인지를 확인한다.)
        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // 파이에베이스 유저정보, DB 추출
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        messagesRef = mFirebaseDatabaseReference.child("beacons");
        maDeviceList = new ArrayList<String>();
        detectableBeaconList = new ArrayList<BeaconInfo>();
        curTrackingList = new ArrayList<DeviceInfo>();

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "Snapshot received " + dataSnapshot.getChildrenCount() + " key: " + dataSnapshot.getKey().toString() + " value: " + dataSnapshot.getValue().toString());
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    String macAddr = dataSnapshot1.getKey();
                    maDeviceList.add("Kids Tracker," + macAddr);
                    BeaconInfo beaconInfo = dataSnapshot1.getValue(BeaconInfo.class);
                    detectableBeaconList.add(beaconInfo);
//                    DeviceInfo deviceInfo = new DeviceInfo(macAddr, beaconInfo.getStage());
//                    curTrackingList.add(deviceInfo);
                }
                BLEScanStart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Fetching data failed");
            }
        };
        messagesRef.addValueEventListener(eventListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        //Application 전역변수에 데이타 저장하고, MainActivity 에 Broadcasting 하기
//        mbIsPressure = ((SleepingChildCareApp)getApplication()).getPreferneceSettingInfo().IsPressure;          //좌석 착지 여부(true : ON, false : OFF)
//        miBattery = ((SleepingChildCareApp)getApplication()).getPreferneceSettingInfo().BatteryValue;           //좌석 배터리 잔량
//        miTemperature = ((SleepingChildCareApp)getApplication()).getPreferneceSettingInfo().TemperatureValue;	//좌석 온도

        BLEScanStart();

        //foregound server 로 실행
        Intent P_intent =  new Intent(this, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, P_intent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >=26) {	//안드로이드 Oreo 버전 이상인 경우
            String Channel_ID = "Sleeping Child Care channel";
            NotificationChannel channel = new NotificationChannel(Channel_ID, "Sleeping Child Care Channel", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Channel_ID);
            builder.setContentTitle("Kid Tracker")
                    .setContentText("BLE tracking on Kid Tracker")
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.bluetooth_on)
                    .setPriority(NotificationCompat.PRIORITY_MIN);

            startForeground(FOREGROUND_NOTI_ID, builder.build());

        }
        else {
            Notification noti = new NotificationCompat.Builder(TransmitService.this)
                    .setContentTitle("Kid Tracker")
                    .setContentText("BLE tracking on Kid Tracker")
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.bluetooth_on)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build();

            startForeground(FOREGROUND_NOTI_ID, noti);

        }

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SleepingChildCare:");
        sCpuWakeLock.acquire();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }

        if (mGetDataHandler.hasMessages(1))
            mGetDataHandler.removeMessages(1);

        if (mBLECheckHandler.hasMessages(1))
            mBLECheckHandler.removeMessages(1);

        mBluetoothLeScanner.stopScan(mLeScanCallback);	//BLE 스캔 멈추기
    }

    private void LoadDeviceListFile() {
        // First BLE device to track
//        maDeviceList.add("Kids Tracker,0C:61:CF:A4:91:B7");

        FileInputStream fis = null;

        try {
            fis = openFileInput(((KidTrackerApplication)getApplication()).getPreferenceSettingInfo().DeviceListFileName);
            InputStreamReader in = new InputStreamReader(fis);

            BufferedReader br = new BufferedReader(in);

            while(true) {
                String a = br.readLine();

                Log.e(TAG, "Reading signals......");

                Log.d(TAG, a);

                if (a == null)
                    break;

                maDeviceList.add(a);

            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } finally {
            if (fis != null) {
                try {
                    fis.close();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

    }

    private void BLEScanStart() {
        Log.e(TAG, "BLEScan START");
        LoadDeviceListFile();   //BLE Device 리스트

        if (maDeviceList.size() == 0) {    //등록된 센서 장치가 없으면 리턴
            Log.e(TAG, "No BLE Found");
            return;
        }

        String[] sDeviceInfo;
        String sRet;
        List<ScanFilter> filters = new ArrayList<ScanFilter>();

        for (int i = 0; i < maDeviceList.size(); i++) {
            sRet = maDeviceList.get(i);
            sDeviceInfo = sRet.split(",");  //(예 : Sleeping Child Care,0C:61:CF:12:34:56)
            String sDeviceMAC = sDeviceInfo[1];
            //String sDeviceMAC = "0C:61:CF:A4:91:B6";

            ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(sDeviceMAC).build();

            filters.add(filter);
        }

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

        mBluetoothLeScanner.startScan(filters, settings, mLeScanCallback);
        messagesRef.removeEventListener(eventListener);// 초기 BLE 리스트 받아오기 종료
    }

    //패킷 데이타를 저장하고 MainActivity 에 이벤트로 호출하는 핸들러
    Handler mGetDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (miBattery != -1) {	//좌석 배터리 잔량(= -1 : 초기화 데이타, > 0 : 실제 데이타) - 초기화 데이타인 경우에는 Broadcast 발생 안 시킴
                if (miRSSI <= ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().Leave_RSSI_Value) {    //정해진 RSSI 값보다 작으면 BLE 연결이 끊긴걸로 간주
                    //BLE 연결이 끊긴 경우 변수들 초기화
                    ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().BatteryValue = 0;      //좌석 배터리 잔량
                    ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().TemperatureValue = 0;  //좌석 온도

                    mbIsBLEConnected = false;
                    Log.e(TAG, "mGedDataHandler");

//                    if (mbIsPressure) {
//                        //경고메세지 Activity 보이기
//                        Intent intent = new Intent (TransmitService.this, MessageActivity.class);
//                        startActivity(intent);
//                    }

                }
                else {	//정상 연결인 경우
                    //Application 전역변수에 데이타 저장하고, MainActivity 에 Broadcasting 하기
                    ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().BatteryValue = miBattery;           //좌석 배터리 잔량
                    ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().TemperatureValue = miTemperature;   //좌석 온도

                }

                ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().IsPressure = mbIsPressure;          //좌석 착지 여부(true : ON, false : OFF)
                ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().RSSI = miRSSI;  					 //BLE RSSI 값

                final Intent intent = new Intent(BROADCAST_MESSAGE);
                sendBroadcast(intent);

            }

            if (mbIsBLEConnected)	//BLE 이 연결되어 있는 경우에만 데이타 체크
                mGetDataHandler.sendEmptyMessageDelayed(1, GetDataInterval);    //GetDataInterval 시간 마다 다시 호출

        }

    };

    //BLE 가 살아 있는지 체크 하는 핸들러
    Handler mBLECheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "mGedDataHandler");
            //Application 전역변수에 데이타 저장하고, MainActivity 에 Broadcasting 하기
            miRSSI = -100;   //BLE RSSI 값 (연결 끊김으로 설정)

        }

    };

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // TODO Auto-generated method stub
            String sDeviceName = result.getDevice().getName();

            Log.e(TAG, sDeviceName);

            if (sDeviceName !=null)
                sDeviceName = sDeviceName.trim();

            //String sDeviceMAC = "0C:61:CF:12:34:56";
            String sDeviceMAC = result.getDevice().getAddress();  //테스트 기기의 MAC = "0C:61:CF:A4:91:B6"

            Log.e(TAG, sDeviceMAC);
            ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().bleList.add(sDeviceMAC);

            // ArrayList 에 디바이스 정보 추가(기존에 들어 있는 내용인지 여부 확인해서 없으면 추가)
            // Firebase에 추가할 수 있도록 연동?
            if (maDeviceList.contains(sDeviceName + "," + sDeviceMAC)) {
                miRSSI = result.getRssi();

                if (miRSSI > ((KidTrackerApplication) getApplication()).getPreferenceSettingInfo().Leave_RSSI_Value) {    //정해진 RSSI 값보다 크면 BLE 연결된 걸로 간주
                    if (!mbIsBLEConnected)  //기존에 BLE 접속이 끊어진걸로 인식했다가 연결로 바뀌는 순간에는 데이타 체크 핸들러 동작 개시
                        mGetDataHandler.sendEmptyMessageDelayed(1, GetDataInterval);

                    mbIsBLEConnected = true;

                }

                byte[] btData = result.getScanRecord().getBytes();

                GetBitData(btData[5], btData[6]);	//Bit 연산함수

                Log.d(TAG, "RSSI = " + miRSSI + " , Data = " + btData.toString());

                return;

            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // TODO Auto-generated method stub
            super.onBatchScanResults(results);

        }

        @Override
        public void onScanFailed(int errorCode) {
            // TODO Auto-generated method stub
            super.onScanFailed(errorCode);

        }

    };

    //bit 연산하는 함수
    private void GetBitData(byte data1, byte data2) {
        if (mBLECheckHandler.hasMessages(1))
            mBLECheckHandler.removeMessages(1);

        miBattery = data1 & 0x7F;	//하위 7비트 뽑아내기 - 좌석 배터리 잔량

        if ( (data1 & 0x80) == 0 ) { //상위 1비트 판단
            mbIsPressure = false;	//0 이면 OFF - 좌석 착지 여부(true : ON, false : OFF)

        }
        else {
            mbIsPressure = true; 	//1 이면 ON - 좌석 착지 여부(true : ON, false : OFF)

        }

        miTemperature = data2 & 0x7F; 	//하위 7비트 뽑아내기 -  좌석 온도
        if ((data2 & 0x80) == 0) { 		//상위 1비트 부호 판단
            miTemperature = miTemperature * (-1); //0이면 음수

        }

        mBLECheckHandler.sendEmptyMessageDelayed(1, BLECheckInterval);    //BLECheckInterval 시간 마다 다시 호출

    }

    //BLE 모듈 정보 저장 CLASS
    private static class BLE_Device {
        String[] user_alldata=new String[500];
        String[] user_data=new String[500];
        String[] user_TX_level=new String[500];
        int[] rssi=new int[500];


        public void setclear() {
            for (int i = 0; i < 500; i++) {
                user_alldata[i]="";
                user_data[i]="";
                user_TX_level[i]="";
                rssi[i]=0;
            }
        }
    }
}

