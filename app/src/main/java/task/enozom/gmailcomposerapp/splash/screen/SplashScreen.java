package task.enozom.gmailcomposerapp.splash.screen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import task.enozom.gmailcomposerapp.gmail.composer.MainActivity;
import task.enozom.gmailcomposerapp.R;

public class SplashScreen extends Activity {


    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

            setContentView(R.layout.activity_splash_screen);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //  will be executed once the timer is over
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    // close this activity
                    finish();
                }
            }, SPLASH_TIME_OUT);

        }

    }