package com.example.food_delivery_server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.food_delivery_server.Commons.Commons;
import com.example.food_delivery_server.Model.ServerUserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;

    DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inits();
    }

    private void inits() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        serverRef = FirebaseDatabase.getInstance().getReference(Commons.SERVIER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuthLocal -> {
            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if (user != null) {

                //Check User From Firebase
                checkServerUserFromFirebase(user);
            } else {

                phoneLogin();
            }
        };
    }

    private void checkServerUserFromFirebase(FirebaseUser user) {
        dialog.show();
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            ServerUserModel userModel=snapshot.getValue(ServerUserModel.class);

                            if (userModel.isActive()){

                                goToHomeActivtiy(userModel);


                            }else {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "You must be allowed from Admin to access this app", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //User Not exists in database
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please Fill Infromation \n Admon will accept your account late");


        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edtName = itemView.findViewById(R.id.edt_name);
        EditText edtPhone = itemView.findViewById(R.id.edt_phone);


        //Set Data
        edtPhone.setText(user.getPhoneNumber());
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).setPositiveButton("Register", (dialogInterface, i) -> {

            if (TextUtils.isEmpty(edtName.getText().toString())) {
                Toast.makeText(MainActivity.this, "Please Enter Your Name!", Toast.LENGTH_SHORT).show();
                return;
            }

            ServerUserModel serverUserModel = new ServerUserModel();
            serverUserModel.setUid(user.getUid());
            serverUserModel.setName(edtName.getText().toString());
            serverUserModel.setPhone(edtPhone.getText().toString());
            serverUserModel.setActive(false);

            dialog.show();
            serverRef.child(serverUserModel.getUid())
                    .setValue(serverUserModel)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Congratulation! Register Success! Admin will check ", Toast.LENGTH_SHORT).show();

                        //    goToHomeActivtiy(serverUserModel);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog registerDialog=builder.create();
        registerDialog.show();



    }

    private void goToHomeActivtiy(ServerUserModel serverUserModel) {

        dialog.dismiss();
        Commons.currentSerVerUser = serverUserModel;
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            } else {

                Toast.makeText(this, "Failed to Sign in!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}