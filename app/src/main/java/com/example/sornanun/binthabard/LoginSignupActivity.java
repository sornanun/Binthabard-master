package com.example.sornanun.binthabard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginSignupActivity extends AppCompatActivity {
    // Declare Variables
    Button loginbutton;
    Button signup;

    String usernametxt;
    String passwordtxt;
    EditText password;
    EditText username;
    ImageView imageView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.imageView);
        // Locate EditTexts in main.xml
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        // Locate Buttons in main.xml
        loginbutton = (Button) findViewById(R.id.login);
        loginbutton.requestFocus();
        signup = (Button) findViewById(R.id.signup);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(
                            LoginSignupActivity.this,
                            HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                }
            }
        };

        // Login Button Click Listener
        loginbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                usernametxt = username.getText().toString();
                passwordtxt = password.getText().toString();

                if (usernametxt.equals("") || passwordtxt.equals("")) {
                    Toast.makeText(getApplicationContext(),"กรุณาใส่ข้อมูลให้ครบถ้วน",Toast.LENGTH_LONG).show();
                } else {
                    login();
                }
            }
        });
        // Sign up Button Click Listener
        signup.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                usernametxt = username.getText().toString();
                passwordtxt = password.getText().toString();

                if (usernametxt.equals("") || passwordtxt.equals("")) {
                    Toast.makeText(getApplicationContext(), "กรุณาใส่ข้อมูลให้ครบถ้วน", Toast.LENGTH_LONG).show();
                } else if (passwordtxt.length() < 6) {
                    Toast.makeText(getApplicationContext(), "กรุณาตั้งรหัสผ่าน 6 ตัวอักษรขึ้นไป", Toast.LENGTH_LONG).show();
                } else {
                    sign_up();
                }

            }
        });
        username.requestFocus();
    }

    private void login() {
        mAuth.signInWithEmailAndPassword(usernametxt + "@binthabard.com", passwordtxt)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginSignupActivity.this, "ไม่พบบัญชีผู้ใช้หรือรหัสผ่านผิด", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sign_up() {
        mAuth.createUserWithEmailAndPassword(usernametxt + "@binthabard.com", passwordtxt)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginSignupActivity.this, "ไม่สามารถสมัครสมาชิกได้ กรุณาตรวจสอบข้อมูล", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginSignupActivity.this, "สมัครสมาชิกสำเร็จ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
