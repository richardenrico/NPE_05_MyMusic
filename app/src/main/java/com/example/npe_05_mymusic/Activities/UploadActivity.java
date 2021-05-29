package com.example.npe_05_mymusic.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.npe_05_mymusic.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private Button btnUpload, btnUploadImg, btnUploadSong, btnPlaySong;
    private TextInputEditText etTitle, etArtist, etGenre;
    private ImageView ivCover;
    private AutoCompleteTextView tvGenre;
    private Spinner spinGenre;
    private DatabaseReference dbReference;
    private String name, artist;

    private final int PICK_IMAGE_REQUEST = 22;
    private final int PICK_AUDIO_REQUEST = 33;
    private String[] genre = {"Pop", "RnB", "Dance"};

    // Uri indicates, where the image will be picked from
    private Uri audioFilePath, imageFilePath;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);


        btnUpload = findViewById(R.id.btnUpload);
        btnUploadImg = findViewById(R.id.btnUploadImage);
        btnUploadSong = findViewById(R.id.btnUploadSong);
        btnPlaySong = findViewById(R.id.btnPlaySong);
        etTitle = findViewById(R.id.etSongTitle);
        etArtist = findViewById(R.id.etSongArtist);
        tvGenre = findViewById(R.id.tvGenre);
//        spinGenre = findViewById(R.id.genre_spinner);
        ivCover = findViewById(R.id.ivCover);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dbReference = FirebaseDatabase.getInstance().getReference();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genre);
        tvGenre.setAdapter(adapter);
        tvGenre.setOnItemSelectedListener(this);

        btnUploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnUploadSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAudio();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
                deployToDB();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    private void selectAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Audio from here..."),
                PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the Uri of data
            imageFilePath = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(getContentResolver(), imageFilePath);
                ivCover.setImageBitmap(bitmap);
                Log.d("PATH", String.valueOf(imageFilePath));
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
        else if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            audioFilePath = data.getData();

            Uri uriData = Uri.parse(String.valueOf(audioFilePath));
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), uriData);
            btnPlaySong.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    if (btnPlaySong.getText().toString().equalsIgnoreCase("Putar Lagu")) {
                        btnPlaySong.setText("Pause Lagu");
                        mediaPlayer.start();
                    } else if (btnPlaySong.getText().toString().equalsIgnoreCase("Pause Lagu")) {
                        btnPlaySong.setText("Putar Lagu");
                        mediaPlayer.pause();
                    }
                }
            });
            Log.d("PATH", String.valueOf(uriData));
        }
    }

    private void uploadData() {
        if (imageFilePath != null && audioFilePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            name = etTitle.getText().toString();
            artist = etArtist.getText().toString();

            StorageReference ref = storageReference.child("songs/").child(name + " - " + artist +"/");

            ref.child("cover/").putFile(imageFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image uploaded successfully
                    // Dismiss dialog
                    Toast.makeText(UploadActivity.this,
                            "Image Uploaded!!",
                            Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast.makeText(UploadActivity.this,
                                    "Failed " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                                }
                            });

            ref.child("audio/").putFile(audioFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(UploadActivity.this, "Audio Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadActivity.this,
                                    "Failed " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int)progress + "%");
                        }
                    });
        }
    }

    private void deployToDB() {
        storageReference.child("songs/").child(name + " - " + artist +"/");

        Map<String, Object> map = new HashMap<>();
        map.put("title", etTitle.getText().toString());
        map.put("artist", etArtist.getText().toString());
        map.put("genre", tvGenre.getText().toString());
//        map.put("song_url", storageReference.child("songs/").child(name + " - " + artist +"/").child("audio").getDownloadUrl());
        Log.d("URL", storageReference.child("songs/Alan Walker - Tired/audio").getDownloadUrl().toString());



    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}