package com.example.tibsolg.ethiotaxi;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLogin extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mLogin, mRegistration;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser customer = FirebaseAuth.getInstance().getCurrentUser();
                if(customer!=null){
                    Intent i = new Intent(CustomerLogin.this, CustomerMap.class);
                    startActivity(i);
                    finish();
                    return;
                }
            }
        };

        Button forgetPassword=(Button) findViewById(R.id.btn_forgot_password);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mLogin = (Button) findViewById(R.id.login);
        mRegistration = (Button) findViewById(R.id.registration);

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CustomerLogin.this, ResetPasswordActivity.class);
                startActivity(i);
            }
        });

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String customerPassword = mPassword.getText().toString();
                final String customerEmail = mEmail.getText().toString();
                mAuth.createUserWithEmailAndPassword(customerEmail, customerPassword).addOnCompleteListener(CustomerLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(CustomerLogin.this, "Error: Unable to sign up", Toast.LENGTH_SHORT).show();
                        }else{
                            String uid = mAuth.getCurrentUser().getUid();
                            DatabaseReference currentCustomerDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(uid);
                            currentCustomerDB.setValue(true);
                        }
                    }
                });
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String customerEmail = mEmail.getText().toString();
                final String customerPassword = mPassword.getText().toString();
                mAuth.signInWithEmailAndPassword(customerEmail, customerPassword).addOnCompleteListener(CustomerLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(CustomerLogin.this, "Error: Unable to sign in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
}

