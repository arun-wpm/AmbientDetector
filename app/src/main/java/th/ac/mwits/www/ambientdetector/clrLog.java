package th.ac.mwits.www.ambientdetector;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

/**
 * Created by windows on 10-Nov-16.
 */
public class clrLog extends AppCompatActivity {
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String temp=Environment.getExternalStorageDirectory().toString();
        temp=temp+"/FFT";
        File dir=new File(temp);
        dir.mkdir();
        File file=new File(dir,"log.txt");
        boolean deleted=file.delete();
        finish();
    }
}
