package tdtu.todo.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import java.util.HashMap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    private EditText regUserName, regPassword;
    private Button regButton;
    private TextView regQn;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);

        mAuth=FirebaseAuth.getInstance();
        loader=new ProgressDialog(this);

        regUserName=findViewById(R.id.registrationUserName);
        regPassword=findViewById(R.id.registrationPassword);
        regButton=findViewById(R.id.btnRegistration);
        regQn=findViewById(R.id.textViewLink);

        regQn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegistrationActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regUserName.getText().toString().trim();
                String password=regPassword.getText().toString().trim();


                if (TextUtils.isEmpty(email)){
                    regUserName.setError("User name is required!");
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    regPassword.setError("Password is required!");
                    return;
                }else{
                    loader.setMessage("Registration - Please Wait!");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(RegistrationActivity.this, dashboardhome.class);
                                startActivity(intent);
                                loader.dismiss();
                                FirebaseUser user=mAuth.getCurrentUser();
                                String email = user.getEmail();
                                String uid=user.getUid();
                                HashMap<Object, String> hashMap=new HashMap<>();
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name","");
                                hashMap.put("phone","");
                                hashMap.put("image","");
                                FirebaseDatabase database=FirebaseDatabase.getInstance();
                                DatabaseReference reference=database.getReference("Account");
                                reference.child(uid).setValue(hashMap);
                                finish();
                            }else{
                                String error=task.getException().toString();
                                Toast.makeText(RegistrationActivity.this, "Registration Failed! "+error,Toast.LENGTH_LONG).show();
                                loader.dismiss();
                            }

                        }
                    });

                }


            }
        });
    }
}