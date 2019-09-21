package com.example.firebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        saveOnButtonClock();
        readDataFromFireBase();

    }

    private void initViews() {
        mainInfoTV = findViewById(R.id.main_info_tv);
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.lastName_edit_text);
        saveButton = findViewById(R.id.save_button);
        profileImageView = findViewById(R.id.profile_image_view);
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
        Task<Uri> task = ref.putFile(uri).continueWithTask(task12 -> {
            if (task12.isSuccessful()){
                return ref.getDownloadUrl();
            }
            return null;
        }).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()){
                Uri downloadUrl = task1.getResult();
                Log.e("ololo", "url " + downloadUrl);
                saveUser(downloadUrl);
            }else{
                Toaster.showMessage("Не успешно");
            }
        });
    }
}
