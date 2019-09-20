package com.example.firebase.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase.ProfileActivity;
import com.example.firebase.R;
import com.example.firebase.models.CountryCode;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private TextView infoTextView;
    private EditText phoneNumberEditText;
    private EditText phoneCodeEditText;
    private EditText codeVerifyEditText;
    private Button verifyButton;
    private Button nextButton;
    private String verify;
    private Spinner countryCodeSpinner;
    private DrawerLayout drawerLayout;
    private ProgressDialog progressDialog;

    NavigationView navigationView;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
    List<CountryCode> countryCodes;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Init views*/
        Toolbar toolbar = findViewById(R.id.toolBar);

        drawerLayout = findViewById(R.id.drawer_layout);
        countryCodeSpinner = findViewById(R.id.country_code_spinner);
        phoneNumberEditText = findViewById(R.id.edit_phone_number);
        phoneCodeEditText = findViewById(R.id.edit_country_code);
        infoTextView = findViewById(R.id.phone_text_view);
        codeVerifyEditText = findViewById(R.id.code_verification_editText);
        verifyButton = findViewById(R.id.verify_button);
        nextButton = findViewById(R.id.next_button);

        phoneCodeEditText.setFocusable(false);

        countryCodes = new ArrayList<>();
        countryCodes.add(new CountryCode("KYRGYZSTAN  ","(+996)"));
        countryCodes.add(new CountryCode("RUSSIA  ",  "(+7)"));
        countryCodes.add(new CountryCode("UZBEKISTAN  ",  "(+998)"));
        countryCodes.add(new CountryCode("KAZAKHSTAN  ",  "(+7)"));

        ArrayAdapter<CountryCode>countryCodeAdapter = new ArrayAdapter<CountryCode>(
                MainActivity.this, android.R.layout.simple_spinner_item, countryCodes){
        };

        countryCodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryCodeSpinner.setAdapter(countryCodeAdapter);
        countryCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedItem = countryCodeSpinner.getSelectedItemPosition();
                if (selectedItem == 0){
                    phoneCodeEditText.setText("+996"); }
                if (selectedItem == 1){
                    phoneCodeEditText.setText("+7"); }
                if (selectedItem == 2){
                    phoneCodeEditText.setText("+998"); }
                if (selectedItem == 3){
                    phoneCodeEditText.setText("+7"); }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        setSupportActionBar(toolbar);

        /* Drawer layout */

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        showUserInfo(headerView);
        progressDialog = new ProgressDialog(this);

        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
            }

            @Override
            public void onCodeSent(@NonNull String verification, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification, verify);
                credential.getSmsCode();
                verify = verification;
            }
        };
        verifyCode();
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verify = codeVerifyEditText.getText().toString().trim();
                Log.d("ololo", "verify " + verify);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
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

        String phoneNumber = phoneCodeEditText.getText().toString().trim() +
                phoneNumberEditText.getText().toString().trim();

        Log.d("ololo", "onButtonClick " + phoneNumber);

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "You have to enter your phone number!", Toast.LENGTH_SHORT).show(); }

        if (phoneNumberEditText.getText() != null || phoneCodeEditText != null) {
            phoneNumberEditText.getText().clear();
            phoneCodeEditText.getText().clear(); }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError("Укажите номер телефона!");
            return; }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneCodeEditText.setError("Укажите код страны!");
            return; }

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

  private void showUserInfo(View view){
      Intent data = getIntent();
      String userName = data.getStringExtra("name");
      String userLastName = data.getStringExtra("lastName");

      TextView textViewName = view.findViewById(R.id.user_name);
      TextView textViewLastName = view.findViewById(R.id.user_lastName);

      textViewName.setText(userName);
      textViewLastName.setText(userLastName);
  }

    private void hideSmsSendViews() {
        phoneCodeEditText.setVisibility(View.GONE);
        phoneNumberEditText.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        countryCodeSpinner.setVisibility(View.GONE);
        findViewById(R.id.select_country_info).setVisibility(View.GONE);

    }
    private void verifyCode(){
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
