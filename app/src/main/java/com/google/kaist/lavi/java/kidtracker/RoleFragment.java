package com.google.kaist.lavi.java.kidtracker;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.plus.PlusOneButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A fragment with a Google +1 button.
 */
public class RoleFragment extends Fragment {

    // The request code must be 0 or greater.
//    private static final int PLUS_ONE_REQUEST_CODE = 0;
    // The URL to +1.  Must be a valid URL.
//    private final String PLUS_ONE_URL = "http://developer.android.com";
//    private PlusOneButton mPlusOneButton;


//    public RoleFragment() {
//        // Required empty public constructor
//    }
    private String TAG = "ROLE_FRAGMENT";
    private String PROVIDER = "providers";
    private DatabaseReference providersRef;
    private ValueEventListener eventListener;
    private DatabaseReference mFirebaseDatabaseReference;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_role, container, false);
        MaterialButton authButton = view.findViewById(R.id.auth_button);
        MaterialButton providerButton = view.findViewById(R.id.provider_button);

//        authButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(getActivity(), DetailsActivity.class);
//                startActivity(i);
//                ((Activity) getActivity()).overridePendingTransition(0, 0);
//            }
//        });

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        providersRef = mFirebaseDatabaseReference.child(PROVIDER);




        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ProductGridFragment is where we can view KidMessages
                ((NavigationHost) getActivity()).navigateTo(new ProductGridFragment(), true);
            }
        });

        providerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Log.e(TAG, FirebaseAuth.getInstance().getCurrentUser().getUid());
                eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean match = false;
                        for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                            Log.e(TAG, currUid);
                            Log.e(TAG, dataSnapshot1.getKey());
                            if (Objects.equals(currUid, dataSnapshot1.getKey())) {
                                match = true;
                                Intent i = new Intent(getActivity(), BluetoothActivity.class);
                                startActivity(i);
                                ((Activity) getActivity()).overridePendingTransition(0, 0);
                            }
                        }
                        if (!match) {
                            ((NavigationHost) getActivity()).navigateTo(new DetailsActivity(), true);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Fetching data failed");
                    }
                };
                providersRef.addValueEventListener(eventListener);


            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the state of the +1 button each time the activity receives focus.
//        mPlusOneButton.initialize(PLUS_ONE_URL, PLUS_ONE_REQUEST_CODE);
    }


}