package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.lang.UCharacter;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    TextView infoTextView;
    EditText phoneNumberEditText;
    EditText phoneCodeEditText;
    EditText codeVerifyEditText;
    Button verifyButton;
    Button nextButton;
    String phoneNumber;

    ProgressDialog progressDialog;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);

        phoneNumberEditText = findViewById(R.id.edit_phone_number);
        phoneCodeEditText = findViewById(R.id.edit_phone_code);
        infoTextView = findViewById(R.id.phone_text_view);
        codeVerifyEditText = findViewById(R.id.code_verification_editText);
        verifyButton = findViewById(R.id.verify_button);
        nextButton = findViewById(R.id.next_button);


        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d("ololo", "onVerificationFailed");
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
            }

            @Override
            public void onCodeSent(@NonNull String verification, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

            }
        };
        verifyCode();
    }

    private void signIn(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    public void OnPhoneButtonClick(View view) {
        progressDialog.setMessage("Sending verifying code...");
        progressDialog.show();

        phoneNumber =
                phoneCodeEditText.getText().toString().trim() +
                        phoneNumberEditText.getText().toString().trim();


        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "You have to enter your phone number!", Toast.LENGTH_SHORT).show();
        }

        if (phoneNumberEditText.getText() != null || phoneCodeEditText != null) {
            phoneNumberEditText.getText().clear();
            phoneCodeEditText.getText().clear();
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError("Укажите номер телефона!");
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneCodeEditText.setError("Укажите код страны!");
            return;
        }


        onCodeVerification();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.MILLISECONDS,
                this,
                mCallBack);
    }

    private void onCodeVerification() {
        if (phoneNumberEditText.getText() != null && phoneCodeEditText.getText() != null) {
            infoTextView.setText("Enter the code what you received");
            codeVerifyEditText.setVisibility(VISIBLE);
            verifyButton.setVisibility(VISIBLE);
            hideSmsSendViews();
        }
    }

    private void hideSmsSendViews() {
        phoneCodeEditText.setVisibility(View.GONE);
        phoneNumberEditText.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
    }
    private void verifyCode(){
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
