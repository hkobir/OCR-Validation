package com.example.ocrvalidation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AppCompatImageView imageView;
    private AppCompatTextView recognizeTV;
    private AppCompatButton capture, detect;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        init();

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                recognizeTV.setText("");
                detect.setText("Detect Text");
            }
        });
        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectText();
            }
        });
    }

    private void detectText() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayDetectText(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void displayDetectText(FirebaseVisionText firebaseVisionText) {
        StringBuilder builder = new StringBuilder();
        List<FirebaseVisionText.TextBlock> textBlockList = firebaseVisionText.getTextBlocks();
        if (textBlockList.size() == 0) {
            Toast.makeText(MainActivity.this, "No text in this image", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.TextBlock block : textBlockList) {
                for (FirebaseVisionText.Line line : block.getLines()) {
                    String lineText = line.getText();
                    builder.append(lineText+"\n");
                    Log.d(MainActivity.class.getSimpleName(), "line: " + lineText);
                    if (lineText.contains("ID")) {
                        String id = lineText.substring(lineText.lastIndexOf("NO: ") + 3);
                        checkUser(id);
                    }

                }
            }
            recognizeTV.setText(builder);
        }
    }

    private void checkUser(String id) {
        Toast.makeText(MainActivity.this, id, Toast.LENGTH_SHORT).show();
        detect.setText(id);
    }

    public void init() {
        imageView = findViewById(R.id.captureIV);
        recognizeTV = findViewById(R.id.detectTV);
        capture = findViewById(R.id.captureBtn);
        detect = findViewById(R.id.detectBtn);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }
}