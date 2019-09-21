package com.example.firebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {
    private final static int REQUEST_CODE = 100;
    private TextView mainInfoTV;
    private EditText nameEditText;
    private EditText emailEditText;
    private Button saveButton;
    private ImageView profileImageView;
    private ProgressBar progressBar;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        saveOnButtonClock();
        readDataFromFireBase();

        SharedPreferences prefs = getSharedPreferences("save_user_info", MODE_PRIVATE);
        String names = prefs.getString("save_name", "Hello, User!");
        mainInfoTV.setText(" Привет, " + names);
        Log.d("ololo", "getSharedName" + names);
    }

    private void initViews() {
        mainInfoTV = findViewById(R.id.main_info_tv);
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.lastName_edit_text);
        saveButton = findViewById(R.id.save_button);
        profileImageView = findViewById(R.id.profile_image_view);
        progressBar = findViewById(R.id.show_loading_process);
    }

    //region GetUserInfo
    private void setDataToFireBase() {
        if (nameEditText.getText().toString().equals("") || emailEditText.getText().toString().equals("")) {
            Toast.makeText(this, "name and last name are empty!", Toast.LENGTH_LONG).show();
        } else {
            uploadImage();
        }
    }

    private void saveUser(Uri downloadUrl){
        String name = nameEditText.getText().toString().trim();
        String lastName = emailEditText.getText().toString().trim();
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", lastName);
        map.put("avatar", downloadUrl.toString());
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .set(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Успешно", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        Toaster.showMessage("Неуспешно!");
                    }
                });
    }


    private void readDataFromFireBase() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String name = task.getResult().getString("name");
                String email = task.getResult().getString("email");
                String avatar = task.getResult().getString("avatar");
                nameEditText.setText(name);
                emailEditText.setText(email);
                showAvatar(avatar);

                SharedPreferences.Editor editor = getSharedPreferences("save_user_info", MODE_PRIVATE).edit();
                editor.putString("save_name", name);
                editor.putString("save_email", email);
                editor.putString("avatar", avatar);
                editor.apply();
                Log.d("ololo", "onSaveShared" + name + email + avatar);
            }
        });

    }

    private void showAvatar(String avatar) {
        Glide.with(this)
                .load(avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImageView);
    }

    private void saveOnButtonClock() {
        saveButton.setOnClickListener((View v) -> {
            progressBar.setVisibility(View.VISIBLE);
            setDataToFireBase();
        });
    }

    public void onClickOpenGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            uri = data.getData();
            profileImageView.setImageURI(uri);
        }
    }

    private void uploadImage() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("avatars/" + sdf.format(date) + " avatars.jpg" );
        Task<Uri> task = ref.putFile(uri).continueWithTask((Task<UploadTask.TaskSnapshot> tasks) -> {
            if (tasks.isSuccessful()){
                return ref.getDownloadUrl();
            }
            return null;
        }).addOnCompleteListener(tasks -> {
            if (tasks.isSuccessful()){
                Uri downloadUrl = tasks.getResult();
                Log.e("ololo", "url " + downloadUrl);
                saveUser(downloadUrl);
            }else{
                Toaster.showMessage("Не успешно");
            }
        });
    }
}
