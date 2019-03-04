package com.google.kaist.lavi.java.kidtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.PersonBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductGridFragment extends Fragment {

    String TAG = "FirebaseTest";
    private String AUTH = "auth";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseApp mFirebaseApp;
    private DatabaseReference mFirebaseDatabaseReference;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView recyclerView;
    private String mUsername;
//    public static final String MESSAGES_CHILD = "messages";
    private static final String MESSAGE_URL = "https://kid-tracker-684c9.firebaseio.com/";
    private FirebaseRecyclerAdapter<KidMessage, ProductCardViewHolder> mFirebaseAdapter;
    ArrayList<KidMessage> kidMessageList;
    MyAdapter adapter;

    private DatabaseReference messagesRef;
    private ValueEventListener eventListener;
    private String currUid;
    private boolean isRegistered = false;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.e(TAG, "HELLO");
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.shr_product_grid_fragment, container, false);


        setUpToolBar(view);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        messagesRef = mFirebaseDatabaseReference.child(AUTH);
        recyclerView = view.findViewById(R.id.recycler_view);

        currUid = mFirebaseUser.getUid();

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                kidMessageList = new ArrayList<KidMessage>();
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    if (Objects.equals(currUid, dataSnapshot1.getKey())) {
                        isRegistered = true;
                        for (DataSnapshot child: dataSnapshot1.getChildren()) {
                            KidMessage kidMessage = child.getValue(KidMessage.class);
                            kidMessageList.add(kidMessage);
                        }
                    }
                }
                adapter = new MyAdapter(ProductGridFragment.this, kidMessageList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Fetching data failed");
            }
        };
        messagesRef.addValueEventListener(eventListener);


        // Set up the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));
        int largePadding = getResources().getDimensionPixelSize(R.dimen.shr_product_grid_spacing);
        int smallPadding = getResources().getDimensionPixelSize(R.dimen.shr_product_grid_spacing_small);
        recyclerView.addItemDecoration(new ProductGridItemDecoration(largePadding, smallPadding));

        MaterialButton registerBeaconButton = view.findViewById(R.id.register_beacon);
        MaterialButton signOutButton = view.findViewById(R.id.sign_out);

        registerBeaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // QR code register
                if (isRegistered) { //센서장치가 1대 등록되어 있으면 장치 추가 못함.
                    Toast.makeText(getContext(), "Beacon already registered for this account", Toast.LENGTH_LONG).show();
                    return;

                }

                IntentIntegrator intergrator = new IntentIntegrator(getActivity());
                intergrator.setOrientationLocked(false);
                intergrator.initiateScan();
            }
        });



        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext())
                        .setTitle("Sign out")
                        .setMessage("Are you sure you want to sign out?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation

                                AuthUI.getInstance()
                                        .signOut(getContext())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // user is now signed out
                                                startActivity(new Intent(getActivity(), DefaultFragment.class));
                                                onDestroy();
                                            }
                                        });
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        return view;
    }

    private void setUpToolBar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        toolbar.setNavigationOnClickListener(new NavigationIconClickListener(
                getContext(),
                view.findViewById(R.id.product_grid),
                new AccelerateDecelerateInterpolator(),
                getContext().getResources().getDrawable(R.drawable.shr_menu), // Menu open icon
                getContext().getResources().getDrawable(R.drawable.shr_close_menu)));
//        AppCompatActivity activity = (AppCompatActivity) getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

//    @Override
    public void onStart() {
        super.onStart();
        messagesRef.addValueEventListener(eventListener);
    }

    @Override
    public void onPause() {
        messagesRef.removeEventListener(eventListener);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        messagesRef.addValueEventListener(eventListener);
    }

}
