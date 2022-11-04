package com.example.textrecognitionapplication;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handwritingtotext.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {

    private ImageView capturedIV;
    private TextView resultingTV;
    private Button snapBtn, detectBtn;
    private Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPUTRE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        capturedIV = findViewById(R.id.scanner_page_icon);
        resultingTV = findViewById(R.id.text);
        snapBtn = findViewById(R.id.snap_button);
        detectBtn = findViewById(R.id.detect_button);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectText();
            }
        });

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()){
                    captureImage();
                }
                else{
                    requestPermission();
                }
            }
        });
    }

    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }

    // Method to open camera
    private void captureImage(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPUTRE);
        }
    }

    // For checking the result when the permission is asked to the user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission) {
                Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
                captureImage();
            }
            else{
                Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // For getting the data from the image capture & setting up the new image instead of the cartoon icon
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPUTRE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            capturedIV.setImageBitmap(imageBitmap);
        }
    }

    // Detecting text from the image with the help of Firebase
    private void detectText() {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(@NonNull Text text) {
                StringBuilder result = new StringBuilder();
                for(Text.TextBlock block: text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for(Text.Line line : block.getLines()){
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for(Text.Element element : line.getElements()){
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        resultingTV.setText(blockText);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Failed to Detect the Text from the Image."+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}