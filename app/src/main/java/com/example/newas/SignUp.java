package com.example.newas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {
    private EditText Fname;
    private EditText Lname;
    private EditText Email;
    private EditText Pass;
    private EditText Age;
    private EditText Address;
    private Button Submit;

    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private boolean locationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Fname = findViewById(R.id.Fname);
        Lname = findViewById(R.id.Lname);
        Age = findViewById(R.id.Age);
        Address = findViewById(R.id.Address);
        Email = findViewById(R.id.Email);
        Pass = findViewById(R.id.Pass);

        Submit = findViewById(R.id.Submit);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseDatabase.getInstance();

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckSignUp()) {
                    if (PermissionHelper.checkLocationPermission(SignUp.this)) {
                        System.out.println("before create");
                        Log.d("main", "before create signup");
                        CreateUser(Email.getText().toString(), Pass.getText().toString());
                        System.out.println("after create");
                        Log.d("main", "after create signup");
                    } else {
                        PermissionHelper.requestLocationPermission(SignUp.this);
                    }
                }
            }
        });
    }

    public boolean CheckSignUp(){
        String fname = String.valueOf(Fname.getText());
        String lname = String.valueOf(Lname.getText());
        //Integer age = Integer.valueOf(String.valueOf(Age.getText()));
        //String address = String.valueOf(Address.getText());
        String email = String.valueOf(Email.getText());
        String pass = String.valueOf(Pass.getText());

        char[] checkChar = {'1','2','3','4','5','6','7','8','9','0','!','@','#','$','%','^','&','*','(',')','-','_','+','=','~','`',';','?','>','<',':'};
        char[] symbols = {'!','@','#','$','%','^','&','*','(',')','-','_','+','=','~','`',';','?','>','<',':'};

        if (fname==""||fname==null){
            Toast.makeText(SignUp.this, "First Name Is Empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        for (int i =0;i<checkChar.length;i++){
            char c = checkChar[i];
            if (fname.contains(String.valueOf(c))){
                Toast.makeText(SignUp.this, "First Name Contains Symbols", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (lname==""||lname==null){
            Toast.makeText(SignUp.this, "Last Name Is Empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        for (int i =0;i<checkChar.length;i++){
            char c = checkChar[i];
            if (lname.contains(String.valueOf(c))){
                Toast.makeText(SignUp.this, "Last Name Contains Symbols", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (!(email.contains("@") && email.contains(".com"))){
            Toast.makeText(SignUp.this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.contains(" ")){
            Toast.makeText(SignUp.this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pass.length()<6){
            Toast.makeText(SignUp.this, "Password Too Short", Toast.LENGTH_SHORT).show();
            return false;
        }

//        if (age==0||age==null) {
//            Toast.makeText(SignUp.this, "Age Is Empty", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (age<=0){
//            Toast.makeText(SignUp.this, "Age Invalid", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (address==""||address==null) {
//            Toast.makeText(SignUp.this, "Address IS Empty", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        for (int i =0;i<symbols.length;i++){
//            char c = symbols[i];
//            if (address.contains(String.valueOf(c))){
//                Toast.makeText(SignUp.this, "Address Contains Symbols", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        }

        return true;
    }

    public void CreateUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String fname = String.valueOf(Fname.getText());
                            String lname = String.valueOf(Lname.getText());
                            //Integer age = Integer.valueOf(String.valueOf(Age.getText()));
                            Integer age = 18;
                            //String address = String.valueOf(Address.getText());
                            String address = "aaa";
                            String email = String.valueOf(Email.getText());
                            String pass = String.valueOf(Pass.getText());

                            User user = new User(fname, lname, age, address, email, pass);

                            String uid = mAuth.getCurrentUser().getUid();

                            db.getReference("Users").child(uid).setValue(user);

                            System.out.println("here create user");

                            Log.d("main", "on complete in create user");

                            Intent I = new Intent(SignUp.this, Home .class);
                            startActivity(I);
                        } else {
                            Toast.makeText(SignUp.this, "Auth Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    Intent intent = new Intent(SignUp.this, Navigation.class);
                    startActivity(intent);
                }
            }
        }
    }
}
