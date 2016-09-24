package th.ac.mwits.www.ambientdetector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.support.annotation.NonNull;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.hardware.camera2.*;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created on 01-Sep-16.
 */
public class show_recordedsounds extends AppCompatActivity {
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    ArrayList<HashMap<String, String>> SoundList = new ArrayList<HashMap<String, String>>();
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_recordedsounds);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        list = (ListView) findViewById(R.id.listView);
        getData();
    }

    protected void getData() {
        String temp = Environment.getExternalStorageDirectory().toString();
        temp = temp + "/FFT";
        File dir = new File(temp);
        dir.mkdir();
        //Get the text file
        int filenum = 0;
        StringBuilder text = new StringBuilder();
        FileInputStream stream;
        DataInputStream dis;
        while (true) {
            File file = new File(dir, filenum + ".txt");
            stream = null;

            //Read text from file
            try {
                //BufferedReader br = new BufferedReader(new FileReader(file));
                stream = new FileInputStream(file);
                dis = new DataInputStream(stream);

                String line = dis.readUTF();

                if (line != null) {
                    HashMap<String,String> Sound = new HashMap<String,String>();

                    Log.d("TAG", filenum + 1 + "");
                    Log.d("TAG", "name = " + line);

                    Sound.put("ID", filenum + 1 + "");
                    Sound.put("Name",line);
                    SoundList.add(Sound);
                }
                else break;
                //br.close();
                dis.close();
                stream.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
                e.printStackTrace();
                break;
            }
            filenum++;
        }

        showList();
    }
    protected void showList(){
        //Log.d("TAG", "show list");
        try {

            ListAdapter adapter = new SimpleAdapter(
                    show_recordedsounds.this, SoundList, R.layout.sound_list_element,
                    new String[]{"ID","Name"},
                    new int[]{R.id.ID, R.id.Name}
            );

            list.setAdapter(adapter);
            /*list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> list, View view, int position, long id) {
                    Intent intentDetails = new Intent(SoundList.this, ChemicalDetails.class);
                    intentDetails.putExtra("EXTRA_ID", ID);
                    intentDetails.putExtra("EXTRA_ROLE", Role);
                    intentDetails.putExtra("EXTRA_PARENT", Parent);
                    try {
                        intentDetails.putExtra("EXTRA_JSON", Chemicals.getJSONObject(position).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intentDetails);
                }
            });*/
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}