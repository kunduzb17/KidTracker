package com.google.kaist.lavi.java.kidtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationHost {

//    private List<String> maDeviceList = new ArrayList<String>();
    private String TAG = "QRCODE";
    private String BEACON = "beacons";
    private String AUTH = "auth";
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shr_main_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new DefaultFragment())
                    .commit();
        }
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment       Fragment to navigate to.
     * @param addToBackstack Whether or not the current fragment should be added to the backstack.
     */
    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 49374 :    //QR코드 스캔 결과
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

                if (result.getContents() == null) { //QR코드 스캔을 취소한 경우
                    Toast.makeText(MainActivity.this,"Beacon register terminated.",Toast.LENGTH_SHORT).show();

                    return;

                }

                else {  //QR코드 스캔 성공
                    String sRet = result.getContents();     //바코드 값
                    Log.e(TAG, sRet);

                    if (sRet.contains("Kids Tracker")) { //정해진 형식이 아니면 처리하지 않는다.
                        Log.e(TAG, "A");
                        String[] sDeviceInfo = sRet.split("\\s");

                        String sDeviceName = sDeviceInfo[0] + " " + sDeviceInfo[1];
                        Log.e(TAG, "Device name: "+ sDeviceName);

                        if (!sDeviceName.equals("Kids Tracker"))  {
                            //문자열이 Kids Tracker 이 아니면 처리하지 않음
                            Toast.makeText(MainActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final String sDeviceMAC = sDeviceInfo[2];
                        final String currUid = mFirebaseAuth.getInstance().getCurrentUser().getUid();
                        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                        final DatabaseReference beaconListRef = mFirebaseDatabaseReference.child(BEACON);
                        final DatabaseReference currAuthRef = mFirebaseDatabaseReference.child(AUTH).child(currUid);
                        final ValueEventListener beaconListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                                    if (Objects.equals(sDeviceMAC, dataSnapshot1.getKey())) {
                                        Toast.makeText(MainActivity.this, "Already registered", Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
                                        Map<String, Object> beaconInfoUpdate = new HashMap<>();
                                        List<String> listAuth = new ArrayList<String>();
                                        listAuth.add(currUid);
                                        beaconInfoUpdate.put("/auth", listAuth);
                                        beaconInfoUpdate.put("/stage", 0);
                                        beaconListRef.child(sDeviceMAC).updateChildren(beaconInfoUpdate);

                                        Map<String, Object> welcomeMsgUpdate = new HashMap<>();
                                        welcomeMsgUpdate.put("/location", "Welocome!");
                                        welcomeMsgUpdate.put("/time", "You're now registered to Kid Tracker");
                                        currAuthRef.child("Welcome").updateChildren(welcomeMsgUpdate);

                                        Toast.makeText(MainActivity.this, "Register success: " + sDeviceMAC, Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                Toast.makeText(getApplicationContext(),"loadPost:onCancelled",Toast.LENGTH_SHORT).show();
                                // ...
                            }
                        };
                        beaconListRef.addListenerForSingleValueEvent(beaconListener);
                    }

                }

                break;

        }

        //((RCAREApp)getActivity().getApplication()).getPreferneceSettingInfo().Make_AccessToken_Time;

    }
}
