package tdtu.todo.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText loginEmail, loginPassword;
    private Button loginBtn;
    private TextView loginQn;
    private TextView textView;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    private  TextView mRecoverPassTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

            textView = findViewById(R.id.textViewLink);
            //textView.setMovementMethod(LinkMovementMethod.getInstance());

            mAuth = FirebaseAuth.getInstance();
            loader = new ProgressDialog(this);
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

            if (connected){
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent intent = new Intent(MainActivity.this, dashboardhome.class);
                    startActivity(intent);
                }
            }else{
                Toast.makeText(MainActivity.this,"- Please Check Your Internet Access -",Toast.LENGTH_LONG).show();
            }

            loginEmail = findViewById(R.id.loginUserName);
            loginPassword = findViewById(R.id.loginPassword);
            loginBtn = findViewById(R.id.btnLogin);
            loginQn = findViewById(R.id.textViewLink);
            mRecoverPassTv = findViewById(R.id.fgPassword);

            loginQn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                }
            });

            mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    androidx.appcompat.app.AlertDialog.Builder myDialog = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View myView = inflater.inflate(R.layout.forgot_password, null);
                    myDialog.setView(myView);
                    final AlertDialog dialog = myDialog.create();
                    dialog.setCancelable(false);
                    dialog.show();

                    final EditText emailRe = myView.findViewById(R.id.emailRe);
                    Button recovery = myView.findViewById(R.id.recoveryBtnFG);
                    Button cancel = myView.findViewById(R.id.cancelBtnFG);

                    recovery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String email = emailRe.getText().toString().trim();
                            loader.setMessage("Sending Email...");
                            loader.show();
                            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "- Please Check Your Email -", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                        loader.dismiss();
                                    } else {
                                        Toast.makeText(MainActivity.this, "- Your Email Wrong -", Toast.LENGTH_LONG).show();
                                        loader.dismiss();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();

                        }
                    });
                }
            });

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = loginEmail.getText().toString().trim();
                    String password = loginPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(email)) {
                        loginEmail.setError("User name is required!");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        loginPassword.setError("Password is required!");
                        return;
                    } else {
                        loader.setMessage("Login - Please Wait!");
                        loader.setCanceledOnTouchOutside(false);
                        loader.show();

                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(MainActivity.this, dashboardhome.class);
                                    startActivity(intent);
                                    finish();
                                    loader.dismiss();
                                } else {
                                    String error = task.getException().toString();
                                    Toast.makeText(MainActivity.this, "Login Failed! User Name or Password Uncorrect!", Toast.LENGTH_LONG).show();
                                    loader.dismiss();
                                }
                            }
                        });
                    }
                }
            });
        }

}