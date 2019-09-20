package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase.main.MainActivity;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {
    final  static String SAVE_STATE = "save_state";

    private TextView mainInfoTV;
    private EditText nameEditText;
    private EditText lastNameEditText;
    private Button saveButton;
    private String name;
    private String lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        save();
    }

    private void initViews() {
        mainInfoTV = findViewById(R.id.main_info_tv);
        nameEditText = findViewById(R.id.name_edit_text);
        lastNameEditText = findViewById(R.id.lastName_edit_text);
        saveButton = findViewById(R.id.save_button);

        SharedPreferences preferences = getSharedPreferences(SAVE_STATE, MODE_PRIVATE);
        String name = preferences.getString("name", null );
        String lastName = preferences.getString("lastName", null);
        Log.d("ohoho", "sharedPref " + name + " " + lastName);

        nameEditText.setText(name);
        lastNameEditText.setText(lastName);
    }

    private void getUserInfo(){
        if (nameEditText.getText().toString().equals("") || lastNameEditText.getText().toString().equals("")){
            Toast.makeText(this, "name and last name are empty!", Toast.LENGTH_LONG).show();
        }else {
            name = nameEditText.getText().toString();
            lastName = lastNameEditText.getText().toString();
            Log.d("ololo" , "intent " + name + " " + lastName);
        }
    }

    private void saveUser(){
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("lastName", lastName);
        startActivityForResult(intent, 1);
    }

    private void saveState(){
        SharedPreferences preferences = getSharedPreferences(SAVE_STATE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name", name);
        editor.putString("lastName", lastName);
        editor.apply();
    }

    private void save(){
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserInfo();
                saveUser();
                saveState();
            }
        });
    }
}
