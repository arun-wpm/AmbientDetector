package th.ac.mwits.www.ambientdetector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import th.ac.mwits.www.ambientdetector.R;

@RuntimePermissions
public class SplashScreen extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash_screen);

        /* New Handler to start the Menu-Activity 
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */

                Log.d("TAG", "before dummy");
                SplashScreenPermissionsDispatcher.dummyWithCheck(SplashScreen.this);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    //@NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE, Manifest.permission.CAMERA, Manifest.permission.FLASHLIGHT})
    public void dummy() {
        Log.d("TAG", "after dummy");
        Toast.makeText(SplashScreen.this, "Permissions Enabled, Great!",
                Toast.LENGTH_LONG).show();

        dummy2();
    }

    public void dummy2() {
        Intent Project = new Intent(SplashScreen.this, Project.class);
        SplashScreen.this.startActivity(Project);
        SplashScreen.this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        SplashScreenPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}