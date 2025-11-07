package com.example.plantdiseaseandroid;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;


public class HomePage extends AppCompatActivity {

    private Bitmap selectedBitmap;
    private Uri cameraImageUri;

    Button btnSelectImage, btnCamera;
    TextView textView;
    ImageView imageView;

    // Modern activity result launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnCamera = findViewById(R.id.btnCamera);
        textView = findViewById(R.id.txtResult);
        imageView = findViewById(R.id.imagePreview);

        // Setup gallery picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            selectedBitmap = decodeSampledBitmapFromUri(imageUri, 224, 224);
                            imageView.setImageBitmap(selectedBitmap);
                            runModelOnImage(selectedBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        // Setup camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        try {
                            selectedBitmap = decodeSampledBitmapFromUri(cameraImageUri, 224, 224);
                            imageView.setImageBitmap(selectedBitmap);
                            runModelOnImage(selectedBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        captureImageFromCamera(); // proceed if permission granted
                    } else {
                        Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                    }
                }
        );


        // Set up button listeners
        btnSelectImage.setOnClickListener(v -> pickImageFromGallery());
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                captureImageFromCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void captureImageFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        cameraLauncher.launch(cameraImageUri);
    }

    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream input = getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        input = getContentResolver().openInputStream(uri);
        Bitmap scaledBitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();
        return Bitmap.createScaledBitmap(scaledBitmap, reqWidth, reqHeight, true);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void runModelOnImage(Bitmap bitmap) {
        try {
            TFLiteModel tfLiteModel = new TFLiteModel(this);
            float[] prediction = tfLiteModel.classify(bitmap);
            int predictedIndex = argMax(prediction);
            String[] labels = {"Blight", "Cob_Root", "Common_Rust", "Gray_Leaf_Spot", "Healthy"};

            String predictedLabel = labels[predictedIndex];
            textView.setText("Predicted: " + predictedLabel);

            tfLiteModel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int argMax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}

/*
public class HomePage extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 1000;
    private Bitmap selectedBitmap;

    Button btnStImg;
    Button btnSelectImage;
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnStImg = findViewById(R.id.btnSelectImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        textView = findViewById(R.id.txtResult);
        imageView = findViewById(R.id.imagePreview);

        btnStImg.setOnClickListener(v -> {
            pickImage();
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            try {
                //selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                selectedBitmap = decodeSampledBitmapFromUri(imageUri, 224, 224);
                imageView.setImageBitmap(selectedBitmap);

                runModelOnImage(selectedBitmap);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream input = getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        input = getContentResolver().openInputStream(uri);
        Bitmap scaledBitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();
        return Bitmap.createScaledBitmap(scaledBitmap, reqWidth, reqHeight, true);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void runModelOnImage(Bitmap bitmap) {
        try {
            TFLiteModel tfLiteModel = new TFLiteModel(this);
            float[] prediction =  tfLiteModel.classify(bitmap);

            int predictedIndex = argMax(prediction);
            String[] labels = {"Blight", "Cob_Root", "Common_Rust", "Gray_Leaf_Spot", "Healthy"}; // match your model

            String predictedLabel = labels[predictedIndex];
            textView.setText("Predicted: " + predictedLabel);

            tfLiteModel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int argMax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}

*/