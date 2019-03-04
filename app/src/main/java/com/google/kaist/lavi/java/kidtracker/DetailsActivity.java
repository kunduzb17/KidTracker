package com.google.kaist.lavi.java.kidtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailsActivity extends Fragment {

    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference ref;
    private String PROVIDER = "providers";
    private String ROLE = "role";
    private String HELLO = "hello";
    private String BYE = "bye";
    private String INDEX = "index";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_details, container, false);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        final TextInputLayout roleTextInput = view.findViewById(R.id.role_input);
        final TextInputLayout helloMsgInput = view.findViewById(R.id.hello_msg_input);
        final TextInputLayout byeMsgInput = view.findViewById(R.id.bye_msg_input);

        final TextInputEditText roleEditText = view.findViewById(R.id.role_edit_text);
        final TextInputEditText helloEditText = view.findViewById(R.id.hello_edit_text);
        final TextInputEditText byeEditText = view.findViewById(R.id.bye_edit_text);

        MaterialButton confirmButton = view.findViewById(R.id.confirm_button);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer i = 0;
                if (!isFormValid(roleEditText.getText())) {
                    roleTextInput.setError(getString(R.string.ktr_error_role));
                } else {
                    roleTextInput.setError(null);
                    i++;
                }
                if (!isFormValid(helloEditText.getText())) {
                    helloMsgInput.setError(getString(R.string.ktr_error_hello));
                } else {
                    helloMsgInput.setError(null);
                    i++;
                }
                if (!isFormValid(byeEditText.getText())) {
                    byeMsgInput.setError(getString(R.string.ktr_error_bye));
                } else {
                    byeMsgInput.setError(null);
                    i++;
                }


                if (i == 3) {
                    // Add provider's role into firebase
                    ref = mFirebaseDatabaseReference.child(PROVIDER);
                    String currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    String role = roleEditText.getText().toString();
                    String hello = helloEditText.getText().toString();
                    String bye = byeEditText.getText().toString();

                    ref.child(currUid).child(ROLE).setValue(role);
                    ref.child(currUid).child(HELLO).setValue(hello);
                    ref.child(currUid).child(BYE).setValue(bye);
                    ref.child(currUid).child(INDEX).setValue(0);

                    // Switch to bluetooth activity
                    Intent intent = new Intent(getActivity(), BluetoothActivity.class);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    private boolean isFormValid(@Nullable Editable text) {
        return text != null && text.length() > 0;
    }

}
