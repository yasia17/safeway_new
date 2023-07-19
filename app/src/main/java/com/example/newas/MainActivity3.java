package com.example.newas;

/*package whatever do not write package name here*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import java.util.List;

public class MainActivity3 extends AppCompatActivity {
    Button cameraButton;

    // whenever we request for our customized permission, we
    // need to declare an integer and initialize it to some
    // value .
    private final static int REQUEST_IMAGE_CAPTURE = 124;
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        // initializing our firebase in main activity
        FirebaseApp.initializeApp(this);

        // finding the elements by their id's allotted.
        cameraButton = findViewById(R.id.camera_button);

        // setting an onclick listener to the button so as
        // to request image capture using camera
        cameraButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // making a new intent for opening camera
                        Intent intent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(
                                getPackageManager())
                                != null) {
                            startActivityForResult(
                                    intent, REQUEST_IMAGE_CAPTURE);
                        } else {
                            // if the image is not captured, set
                            // a toast to display an error image.
                            Toast
                                    .makeText(
                                            MainActivity3.this,
                                            "Something went wrong",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {
        // after the image is captured, ML Kit provides an
        // easy way to detect faces from variety of image
        // types like Bitmap

        super.onActivityResult(requestCode, resultCode,
                data);
        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap) extra.get("data");
            detectFace(bitmap);
        }
    }

    // If you want to configure your face detection model
    // according to your needs, you can do that with a
    // FirebaseVisionFaceDetectorOptions object.
    private void detectFace(Bitmap bitmap) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);

            detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                            String resultText = "";
                            int i = 1;
                            for (FirebaseVisionFace face : firebaseVisionFaces) {
                                resultText += "\nFACE NUMBER. " + i + ":\n"
                                        + "Smile: " + (face.getSmilingProbability() * 100) + "%\n"
                                        + "Left eye open: " + (face.getLeftEyeOpenProbability() * 100) + "%\n"
                                        + "Right eye open: " + (face.getRightEyeOpenProbability() * 100) + "%\n";
                                i++;
                            }

                            if (firebaseVisionFaces.isEmpty()) {
                                Toast.makeText(MainActivity3.this, "No face detected", Toast.LENGTH_SHORT).show();
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putString(LCOFaceDetection.RESULT_TEXT, resultText);
                                DialogFragment resultDialog = new ResultDialog();
                                resultDialog.setArguments(bundle);
                                resultDialog.setCancelable(true);
                                resultDialog.show(getSupportFragmentManager(), LCOFaceDetection.RESULT_DIALOG);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity3.this, "Oops, something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}