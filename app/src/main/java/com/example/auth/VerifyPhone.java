package com.example.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhone extends AppCompatActivity {

    Button verifyBtn;
    EditText otpTxt;
    ProgressBar pBar;
    String phoneNum;
    String verificationCodeBySystem;
    PhoneAuthOptions options;
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        phoneNum = getIntent().getStringExtra("phoneNum");
        otpTxt = findViewById(R.id.otp);
        verifyBtn = findViewById(R.id.verifyBtn);
        pBar = (ProgressBar) findViewById(R.id.progress_bar);
        pBar.setVisibility(View.GONE);
        firebaseAuth = FirebaseAuth.getInstance();


        sendVerificationCode();

        verifyBtn.setOnClickListener(v ->{
            pBar.setVisibility(View.VISIBLE);
            Log.e(TAG, "onclick: started, process started");
            if (TextUtils.isEmpty(otpTxt.getText().toString())) {
                // if the OTP text field is empty display
                // a message to user to enter OTP
                Toast.makeText(VerifyPhone.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            } else {
                pBar.setVisibility(View.VISIBLE);
                // if OTP field is not empty calling
                // method to verify the OTP.
                verifyCode(otpTxt.getText().toString());
            }
        });


    }

    private void sendVerificationCode() {

        options = PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber("+254"+phoneNum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                verificationCodeBySystem = s;
                                Log.e(TAG, "onCodeSent: success");
                                Toast.makeText(VerifyPhone.this, "code sent", Toast.LENGTH_SHORT).show();
                                mResendToken = forceResendingToken;

                            }
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                Toast.makeText(VerifyPhone.this, "verification complete", Toast.LENGTH_LONG).show();
                                Log.e(TAG, "onVerificationCompleted: "+phoneAuthCredential.getSmsCode() );
                                String code = phoneAuthCredential.getSmsCode();
                                if(code != null){
                                    pBar.setVisibility(View.VISIBLE);
                                    otpTxt.setText(code);
                                    verifyCode(code);
                                }

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(VerifyPhone.this, "verification failed", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onVerificationFailed: "+e.getLocalizedMessage());
                            }
                        })          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void verifyCode(String codeByUser){
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredential(phoneAuthCredential);

    }

    private void signInTheUserByCredential(PhoneAuthCredential phoneAuthCredential) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(VerifyPhone.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(getApplicationContext(), UpdateActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            pBar.setVisibility(View.GONE);
                            startActivity(intent);

                        }else{
                            Toast.makeText(VerifyPhone.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }
}