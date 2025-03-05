package tdtu.todo.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Introduce extends AppCompatActivity {
    private static  final int SPLASH=1000;
    Animation topAnim;
    ImageView imageView;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_introduce);

        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animation);
        imageView=findViewById(R.id.imageView2);

        imageView.setAnimation(topAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(Introduce.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },SPLASH);
    }
}