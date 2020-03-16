package com.example.tibsolg.ethiotaxi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomerSettings extends AppCompatActivity {





    private ImageView profileImage;

    private EditText nameEdit;
    private EditText mPhoneF;
    private Button back;
    private Button confirm;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mCustomerDB;

    private String name, userUID, phone, mProfileImage;

    private Uri resultUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);

        confirm = (Button) findViewById(R.id.confirm);
        mPhoneF = (EditText) findViewById(R.id.phone);

        profileImage = (ImageView) findViewById(R.id.profileImage);


        nameEdit = (EditText) findViewById(R.id.name);

        back = (Button) findViewById(R.id.back);


        firebaseAuth = FirebaseAuth.getInstance();
        userUID = firebaseAuth.getCurrentUser().getUid();
        mCustomerDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userUID);

        getUserInformation();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, 1);
            }
        });



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
    private void getUserInformation(){
        mCustomerDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        nameEdit.setText(name);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneF.setText(phone);
                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImage = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImage).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void saveUserInfo() {
        name = nameEdit.getText().toString();
        phone = mPhoneF.getText().toString();

        Map userInformations = new HashMap();
        userInformations.put("name", name);
        userInformations.put("phone", phone);
        mCustomerDB.updateChildren(userInformations);

        if(resultUri != null) {

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(userUID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            UploadTask task = storageReference.putBytes(data);

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri urlDownload = taskSnapshot.getDownloadUrl();

                    Map imageNew = new HashMap();
                    imageNew.put("profileImageUrl", urlDownload.toString());
                    mCustomerDB.updateChildren(imageNew);

                    finish();
                    return;
                }
            });
        }else{
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }
    }
}
