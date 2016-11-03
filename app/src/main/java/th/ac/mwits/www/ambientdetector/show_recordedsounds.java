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
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.common.server.converter.StringToIntConverter;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
    public HashMap<Integer,Integer> M=new HashMap<>();
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_recordedsounds);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        list = (ListView) findViewById(R.id.listView);
        try {
            getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int toInt(String in)
    {
        int val=0;
        for(int i=0;i<in.length();i++)
        {
            if(in.charAt(i)>='0'&&in.charAt(i)<='9') val=val*10+in.charAt(i)-'0';
        }
        return val;
    }
    protected void getData() throws IOException {
        String temp = Environment.getExternalStorageDirectory().toString();
        temp = temp + "/FFT";
        File dir = new File(temp);
        dir.mkdir();
        FileInputStream stream;
        DataInputStream dis;
        temp=temp+"/max.txt";
        int ma=0;
        try
        {
            InputStream fis = new FileInputStream(temp);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String line=br.readLine();
            if(line!=null) ma=toInt(line);
            fis.close();
            isr.close();
            br.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        //Get the text file
        int filenum = 0;
        StringBuilder text = new StringBuilder();
        int c=0;
        int t=1;
        while (t-1<=ma) {
            File file = new File(dir, filenum + ".txt");
            File file2 = new File(dir, filenum + "stats.txt");
            stream = null;

            //Read text from file
            try {
                //BufferedReader br = new BufferedReader(new FileReader(file));
                stream = new FileInputStream(file);
                dis = new DataInputStream(stream);

                String line = dis.readUTF();
                HashMap<String,String> Sound = new HashMap<String,String>();

                Log.d("TAG", t + "");
                Log.d("TAG", "name = " + line);

                Sound.put("ID", c + 1 + "");
                Sound.put("Name", line);
                M.put(c++, filenum);
                dis.close();
                stream.close();

                if (file2.exists()) {
                    stream = new FileInputStream(file2);
                    dis = new DataInputStream(stream);
                    int i = dis.readInt();
                    Sound.put("Correct", i + "");
                    i = dis.readInt();
                    Sound.put("Incorrect", i + "");
                    i = dis.readInt();
                    Sound.put("Unsure", i + "");
                    SoundList.add(Sound);
                    dis.close();
                    stream.close();
                }
                else {
                    Sound.put("Correct", 0 + "");
                    Sound.put("Incorrect", 0 + "");
                    Sound.put("Unsure", 0 + "");
                    SoundList.add(Sound);
                }
            } catch (IOException e) {
                //You'll need to add proper error handling here
                e.printStackTrace();
            }
            filenum++;
            t++;
        }

        showList();
    }
    protected void showList(){
        //Log.d("TAG", "show list");
        try {

            ListAdapter adapter = new SimpleAdapter(
                    show_recordedsounds.this, SoundList, R.layout.sound_list_element,
                    new String[]{"ID","Name","Correct","Incorrect","Unsure"},
                    new int[]{R.id.ID, R.id.Name, R.id.correct, R.id.incorrect, R.id.unsure}
            );

            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> list, View view, final int position, long id){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(show_recordedsounds.this);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setTitle("Delete Sound");
                    alertDialogBuilder.setMessage("Are you sure you want to delete this sound?");

                    // set dialog message
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String temp = Environment.getExternalStorageDirectory().toString();
                            temp = temp + "/FFT/";
                            int filenum = M.get(position);
                            Log.d("filenumm", String.valueOf(filenum));
                            File file = new File(temp + String.valueOf(filenum) + ".txt");
                            File file2;
                            file.delete();
                            file = new File(temp + String.valueOf(filenum) + "stats.txt");
                            if (file.exists())
                                file.delete();
                            while (true) {
                                filenum++;
                                file = new File(temp + String.valueOf(filenum) + ".txt");
                                if (!file.exists())
                                    break;
                                file2 = new File(temp + String.valueOf(filenum - 1) + ".txt");
                                file.renameTo(file2);

                                file = new File(temp + String.valueOf(filenum) + "stats.txt");
                                if (file.exists()) {
                                    file2 = new File(temp + String.valueOf(filenum - 1) + "stats.txt");
                                    file.renameTo(file2);
                                }
                            }

                            int ma=filenum - 1;
                            Log.d("TAG", "ma=" + ma);
                            file = new File(temp,"max.txt");
                            file.delete();
                            file = new File(temp,"max.txt");
                            PrintWriter pw = null;
                            try {
                                pw = new PrintWriter(new FileWriter(file));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            pw.print(ma+"");
                            pw.flush();
                            pw.close();
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    });

                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}