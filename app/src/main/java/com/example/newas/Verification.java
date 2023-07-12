//package com.example.newas;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import org.tensorflow.lite.DataType;
//import org.tensorflow.lite.Interpreter;
////import org.tensorflow.lite.support.common.FileUtil;
////import org.tensorflow.lite.support.image.ImageProcessor;
////import org.tensorflow.lite.support.image.TensorImage;
////import org.tensorflow.lite.support.image.ops.ResizeOp;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.MappedByteBuffer;
//
//public class Verification extends AppCompatActivity {
//
//    private static final int REQUEST_IMAGE_PICK = 1;
//    private ImageView facialRecognitionImageView;
//
//    private Interpreter interpreter;
//    private ImageProcessor imageProcessor;
//
//    private EditText editText;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_verification);
//
//        facialRecognitionImageView = findViewById(R.id.facialRecognitionImageView);
//        Button facialRecognitionButton = findViewById(R.id.facialRecognitionButton);
//        Button button4 = findViewById(R.id.button4);
//        editText = findViewById(R.id.IdEditText);
//
//        facialRecognitionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openImagePicker();
//            }
//        });
//
//        button4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Verification.this, Signed_in.class);
//                startActivity(intent);
//            }
//        });
//
//        try {
//            interpreter = new Interpreter(loadModelFile());
//            imageProcessor = new ImageProcessor.Builder()
//                    .add(new ResizeOp(128, 128, ResizeOp.ResizeMethod.BILINEAR))
//                    .build();
//        } catch (IOException e) {
//            Log.e("Verification", "Error initializing TensorFlow Lite interpreter", e);
//        }
//    }
//
//    private MappedByteBuffer loadModelFile() throws IOException {
//        return FileUtil.loadMappedFile(this, "model_gender_nonq.tflite");
//    }
//
//    private void openImagePicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, REQUEST_IMAGE_PICK);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
//            if (data != null && data.getData() != null) {
//                Uri imageUri = data.getData();
//                setImageViewUri(imageUri);
//                performGenderClassification(imageUri);
//            }
//        }
//    }
//
//    private void setImageViewUri(Uri uri) {
//        facialRecognitionImageView.setImageURI(uri);
//    }
//
//    private void performGenderClassification(Uri imageUri) {
//        try {
//            if (interpreter != null) {
//                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//                TensorImage tensorImage = new TensorImage(DataType.UINT8);
//                tensorImage.load(bitmap);
//                tensorImage = imageProcessor.process(tensorImage);
//
//                // Perform inference here
//
//                float[][] genderOutputArray = new float[1][2];
//                ByteBuffer genderOutputBuffer = ByteBuffer.allocateDirect(4 * 2);
//                genderOutputBuffer.order(java.nio.ByteOrder.nativeOrder());
//                interpreter.run(tensorImage.getBuffer(), genderOutputBuffer);
//                genderOutputBuffer.rewind();
//                genderOutputBuffer.asFloatBuffer().get(genderOutputArray[0]);
//
//                boolean isFemale = genderOutputArray[0][0] > genderOutputArray[0][1];
//                if (isFemale) {
//                    displayMessage("The user is female.");
//                } else {
//                    displayMessage("The user is male.");
//                }
//            } else {
//                displayMessage("Gender classification model not loaded.");
//            }
//        } catch (Exception e) {
//            Log.e("Verification", "Error performing gender classification", e);
//        }
//    }
//
//    private void displayMessage(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//    }
//}
