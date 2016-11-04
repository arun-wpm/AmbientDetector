package th.ac.mwits.www.ambientdetector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.Image;
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
import android.util.TypedValue;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import static be.tarsos.dsp.beatroot.Peaks.findPeaks;

public class Project extends AppCompatActivity {

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 44100; /* in Hz*/
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
    private int sampleNumBits = 16;
    private int numChannels = 1;

    private double lastused = 0;
    double dB, temp;

    CameraManager camManager;
    CameraDevice mCamera;

    int count = 0;
    short[] data = new short[441000];
    ImageButton start, stop,record;
    Button stop_vibrate;
    ImageView isRecording;

    AudioRecord recorder;
    AudioTrack audioPlayer;

    Vibrator vibrator;

    String root;
    File dir;
    File file;
    String SoundName;
    int filenum = 0;

    int readBytes, writtenBytes = 0;
    int i, j;
    ProgressBar[] pb = new ProgressBar[40];
    TextView[][] tv = new TextView[3][3];
    double max = 10000000.0;
    int ii = 0;
    FFT fft = new FFT(2048, new HammingWindow());
    float[] fdata = new float[2048];
    double[] amp = new double[1024];
    double[] accu = new double[1024];
    double[] disp = new double[40];

    double quicksum[] = new double[2005];

    double[] ref = new double[1024];
    boolean[] peaks = new boolean[1024];

    private static final int[] pbid = {
            R.id.progressBar0,
            R.id.progressBar1,
            R.id.progressBar2,
            R.id.progressBar3,
            R.id.progressBar4,
            R.id.progressBar5,
            R.id.progressBar6,
            R.id.progressBar7,
            R.id.progressBar8,
            R.id.progressBar9,
            R.id.progressBar10,
            R.id.progressBar11,
            R.id.progressBar12,
            R.id.progressBar13,
            R.id.progressBar14,
            R.id.progressBar15,
            R.id.progressBar16,
            R.id.progressBar17,
            R.id.progressBar18,
            R.id.progressBar19,
            R.id.progressBar20,
            R.id.progressBar21,
            R.id.progressBar22,
            R.id.progressBar23,
            R.id.progressBar24,
            R.id.progressBar25,
            R.id.progressBar26,
            R.id.progressBar27,
            R.id.progressBar28,
            R.id.progressBar29,
            R.id.progressBar30,
            R.id.progressBar31,
            R.id.progressBar32,
            R.id.progressBar33,
            R.id.progressBar34,
            R.id.progressBar35,
            R.id.progressBar36,
            R.id.progressBar37,
            R.id.progressBar38,
            R.id.progressBar39
    };

    LayoutInflater inflater;
    View vi;
    private static final int[] tvid = {
            R.id.textView,
            R.id.textView2,
            R.id.textView3,
            R.id.textView4,
            R.id.textView5,
            R.id.textView6,
            R.id.textView7,
            R.id.textView8,
            R.id.textView9
    };

    MyTask myTask;

    final Context context = this;
    Camera cam;

    double black = 0.0, bref = 0.0;
    double white = 0.0, wref = 0.0;
    int bnum = 0, wnum = 0;
    //TreeMap in Java == std::map in C++
    TreeMap<Integer, String> Detected = new TreeMap<Integer, String>(Collections.reverseOrder());
    ArrayList<Integer> DetectedIndex = new ArrayList<>();
    ArrayList<Integer> DetectedIndexCopy = new ArrayList<>();

    private GoogleApiClient client;

    private AlertDialog.Builder alertDialogLiteBuilder;
    private AlertDialog alertDialogLite;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    double Threshold;
    boolean lite, activate = false, first=true;
    int notidelay = 2000;
    Switch LiteMode;
    TextView Thresh;

    public double S2(int k, int i) {
        double t = k * accu[i] - (quicksum[i - 1] - quicksum[i - k - 1]);
        t = t + k * accu[i] - (quicksum[i + k] - quicksum[i]);
        return t / (double) (k * 2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (start.getVisibility() == View.INVISIBLE) {
            Toast.makeText(Project.this, "Cannot access settings now! Stop detecting first",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        int res_id = item.getItemId();
        if (res_id == R.id.action_settings) {
            Intent i = new Intent(Project.this, AppPreferences.class);
            startActivity(i);
        }
        return true;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        root = Environment.getExternalStorageDirectory().toString();
        dir = new File(root + "/FFT");
        dir.mkdir();

        Thresh = (TextView) findViewById(R.id.textView17);
        file = new File(dir, "settings.txt");
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(stream);
            try {
                Threshold = dis.readDouble();
                Log.d("TAG", "Threshold: " + Threshold);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Threshold = 1;
            e.printStackTrace();
        }
        dB = Math.round(20.0*Math.log10(Threshold));
        Thresh.setText("Threshold: " +  new DecimalFormat("#0.##").format(dB));
        Thresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (start.getVisibility() == View.INVISIBLE) {
                    Toast.makeText(Project.this, "Cannot change now! Stop detecting first",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Set threshold and save to settings
                file = new File(dir, "settings.txt");
                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final DataOutputStream dos = new DataOutputStream(stream);
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.dialog_thresh, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.setTitle("Set threshold of lite mode (0 to 90 dB):");

                final EditText[] userInput = {(EditText) promptsView
                        .findViewById(R.id.editText)};
                final Button[] liveLoudness = {(Button) promptsView.findViewById(R.id.button4)};

                liveLoudness[0].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent LiveLoudness = new Intent(Project.this, LiveLoudness.class);
                        Project.this.startActivity(LiveLoudness);
                    }
                });

                // set dialog message
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String input = userInput[0].getText().toString();
                        if (input.equals("")) dB = 0;
                        else dB = Double.valueOf(userInput[0].getText().toString());
                        if (dB > 90) dB = 90;
                        Threshold = Math.pow(10, dB / 20);
                        Log.d("TAG", "values " + Threshold);
                        try {
                            dos.writeDouble(Threshold);
                            Log.d("TAG", "write " + Threshold);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Thresh.setText("Threshold: " + new DecimalFormat("#0.##").format(dB));
                    }
                });

                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            dos.writeDouble(Threshold);
                            Log.d("TAG", "write " + Threshold);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Thresh.setText("Threshold: " + dB);
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
        LiteMode = (Switch) findViewById(R.id.switch1);
        LiteMode.setChecked(false);

        alertDialogLiteBuilder = new AlertDialog.Builder(context);

        alertDialogLiteBuilder.setTitle("Environment sound intensity reaches threshold!");
        alertDialogLiteBuilder.setMessage("Event occured at: " + getCurrentTimeStamp());

        // set dialog message
        alertDialogLiteBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        // create alert dialog
        alertDialogLite = alertDialogLiteBuilder.create();

        bufferSize += 2048;

        for (i = 0; i < 40; i++) {
            pb[i] = (ProgressBar) findViewById(pbid[i]);
            pb[i].getProgressDrawable().setColorFilter(
                    Color.parseColor("#ff5c00"), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi = inflater.inflate(R.layout.dialog_alert2, null, false); //dialog_alert2.xml is your file.
        for (i = 0; i < 3; i++)
            for (j = 0; j < 3; j++)
                tv[i][j] = (TextView) vi.findViewById(tvid[3 * i + j]); //get a reference to the textview on the dialog_alert2.xml file.

        recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        Log.d("TAG", "Start recording");

        audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 50, AudioTrack.MODE_STREAM);
        Log.d("TAG", "Initialized playback");

        record = (ImageButton) findViewById(R.id.button3);

        start = (ImageButton) findViewById(R.id.button);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (record.isPressed()) {
                    Toast.makeText(Project.this, "Cannot start now! Recording must finish first",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
                lite = LiteMode.isChecked();
                activate = false;
                //first = true;
                myTask = new MyTask();

                myTask.execute();
            }
        });

        stop = (ImageButton) findViewById(R.id.button2);
        stop.setVisibility(View.INVISIBLE);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
                myTask.cancel(true);
                for (j = 0; j < 40; j++) {
                    pb[j].setMax((int) Math.round(max));
                    pb[j].setProgress(0);
                }
            }
        });

        LiteMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (start.getVisibility() == View.INVISIBLE) {
                    LiteMode.toggle();
                    Toast.makeText(Project.this, "Cannot change now! Stop detecting first",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        stop_vibrate = (Button) findViewById(R.id.off);
        stop_vibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrator.cancel();
                if (Build.VERSION.SDK_INT >= 23) {
                    //String cameraId = null; // Usually front camera is at 0 position.
                    try {
                        for (String cameraId : camManager.getCameraIdList()) {
                            try {
                                CameraCharacteristics camCharacteristics = camManager.getCameraCharacteristics(cameraId);
                                if (camCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                                    camManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                                        @Override
                                        public void onOpened(@NonNull CameraDevice camera) {
                                            mCamera = camera;
                                            //initPreview();
                                        }

                                        @Override
                                        public void onDisconnected(@NonNull CameraDevice camera) {

                                        }

                                        @Override
                                        public void onError(@NonNull CameraDevice camera, int error) {

                                        }
                                    }, null);
                                    camManager.setTorchMode(cameraId, false);
                                }
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Camera.Parameters p = cam.getParameters();
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        cam.setParameters(p);
                        cam.stopPreview();
                    } catch (Exception e) {
                        Log.e(getString(R.string.app_name), "failed to open Camera");
                        e.printStackTrace();
                    }
                }
            }
        });

        //isRecording = (ImageView) findViewById(R.id.imageView2);
        //isRecording.setVisibility(View.GONE);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //isRecording.setVisibility(View.VISIBLE);
                //isRecording.requestLayout();
                //record.setVisibility(View.INVISIBLE);
                if (start.getVisibility() == View.INVISIBLE) {
                    Toast.makeText(Project.this, "Cannot record now! Stop detecting first",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                writtenBytes = 0;
                max = 10000000.0;
                ii = 0;
                recorder.startRecording();
                do {
                    readBytes = recorder.read(data, writtenBytes, bufferSize);
                    if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
                        writtenBytes += audioPlayer.write(data, writtenBytes, readBytes);
                    }
                }
                while (writtenBytes < bufferSize * 25);
                recorder.stop();
                Log.d("TAG", "Read and Write" + writtenBytes);

                for (i = 0; i < 1024; i++)
                    accu[i] = 0.0;

                while (ii < writtenBytes) {
                    for (i = 0; i < 2048; i++) {
                        fdata[i] = (float) data[ii + i];
                    }
                    fft.forwardTransform(fdata);
                    for (i = 0; i < 1024; i++)
                        amp[i] = Math.sqrt(fdata[2 * i] * fdata[2 * i] + fdata[2 * i + 1] * fdata[2 * i + 1]);
                    for (i = 0; i < 1024; i++)
                        accu[i] += amp[i];

                    ii += i;
                }

                file = new File(dir, filenum + ".txt");
                while (file.exists()) {
                    filenum++;
                    file = new File(dir, filenum + ".txt");
                }

                //Simple Algorithms for Peak Detection in Time-Series
                //C++ implementation by Poon
                //Assume <= 2005 elements
                ArrayList<Integer> peak = new ArrayList<Integer>();
                double a[] = new double[2005];
                int k = 5;
                int h = 1; // 1<=h<=3
                double mean, s, sum = 0;
                for (i = 1; i <= 1024; i++)
                    quicksum[i] = quicksum[i - 1] + accu[i - 1];
                int c = (1024 - 2 * k);
                for (int i = 1; i < 1024; i++) {
                    if (i <= k || i + k > 1024) continue;
                    a[i - k] = S2(k, i);
                    sum = sum + a[i - k];
                }
                mean = sum / c;
                sum = 0;
                for (int i = 1; i <= c; i++)
                    sum = sum + (mean - a[i]) * (mean - a[i]);
                sum = sum / c;
                s = Math.sqrt(sum);
                for (int i = 1; i <= 1024 - 2 * k; i++) {
                    if (a[i] > 0 && (a[i] - mean) > (h * s)) peak.add(i + k);
                }
                for (int i = 0; i < peak.size() - 1; ) {
                    if (accu[peak.get(i) - 1] < accu[peak.get(i + 1) - 1]) {
                        peak.remove(i);
                        continue;
                    } else if (accu[peak.get(i) - 1] > accu[peak.get(i + 1) - 1]) {
                        peak.remove(i + 1);
                    }
                    i++;
                }
                for (i = 0; i < 1024; i++)
                    peaks[i] = false;
                for (i = 0; i < peak.size(); i++) {
                    peaks[peak.get(i) - 1] = true;
                    Log.d("TAG", "peak " + (peak.get(i) - 1));
                }

                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final DataOutputStream dos = new DataOutputStream(stream);

                for (j = 0; j < 40; j++) {
                    sum = 0;
                    for (k = 0; k < 6; k++)
                        sum += accu[6*j + k];
                    disp[j] = sum/6;

                    if (sum/6 > max)
                        max = sum/6;
                }
                for (j = 0; j < 40; j++) {
                    pb[j].setMax((int) Math.round(max));
                    pb[j].setProgress((int) Math.round(disp[j]));
                }

                audioPlayer.play();
                do {                                                     // Montior playback to find when done
                    i = audioPlayer.getPlaybackHeadPosition();
                } while (i < writtenBytes);

                audioPlayer.stop();
                audioPlayer.flush();

                // Save sound name
                // get prompts.xml view
                final String[] Name = new String[1];
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.dialog_name, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.setTitle("Set name of new sound:");

                final EditText[] userInput = {(EditText) promptsView
                        .findViewById(R.id.DialogName)};

                // set dialog message
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int ma=-1;
                        FileInputStream stream;
                        DataInputStream dis;
                        String temp = Environment.getExternalStorageDirectory().toString();
                        temp = temp + "/FFT";
                        File file =new File(temp,"max.txt");

                        try {
                            stream = new FileInputStream(file);
                            dis = new DataInputStream(stream);
                            String line = dis.readUTF();
                            if(line!=null) ma=toInt(line);
                            Log.d("TAG", "ma=" + ma);
                            dis.close();
                            stream.close();
                        }
                        catch(IOException e)
                        {
                            e.printStackTrace();
                        }

                        Name[0] = userInput[0].getText().toString();
                        try {
                            dos.writeUTF(Name[0]);
                            Log.d("TAG", "write " + Name[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            for (i = 0; i < 1024; i++) {
                                dos.writeDouble(accu[i]);
                                dos.writeBoolean(peaks[i]);
                            }
                            Log.d("TAG", "Write Results");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(filenum>ma)
                        {
                            ma=filenum;
                            Log.d("TAG", "ma=" + ma);
                            file.delete();
                            File file2=new File(temp,"max.txt");
                            PrintWriter pw = null;
                            try {
                                pw = new PrintWriter(new FileWriter(file2));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            pw.print(ma+"");
                            pw.flush();
                            pw.close();
                        }
                    }
                });

                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            dos.close();
                            file.delete();
                            filenum = 0;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                //isRecording.setVisibility(View.GONE);
                //record.setVisibility(View.VISIBLE);
            }
        });
    }

    class MyTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            recorder.startRecording();
            while (true) {
                Log.d("TAG", "" + count);


                if (lite) {
                    //lite mode
                    writtenBytes = 0;
                    do {
                        readBytes = recorder.read(data, writtenBytes, bufferSize);
                        writtenBytes += readBytes;
                    }
                    while (writtenBytes < bufferSize * 2);
                    Log.d("TAG", "Read and Write" + writtenBytes);
                    file = new File(dir, "settings.txt");
                    FileInputStream stream = null;
                    try {
                        stream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    DataInputStream dis = new DataInputStream(stream);
                    try {
                        Threshold = dis.readDouble();
                        Log.d("TAG", "Threshold: " + Threshold);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Log.d("TAG", "after try");
                    activate = false;
                    double temp=0.0;
                    for (i = 0; i < writtenBytes; i++) {
                        temp+=Math.abs(data[i]);
                    }
                    temp/=(double)writtenBytes;
                    //Log.d("TAG", "Temp"+temp);
                    if(temp>=Threshold) activate=true;
                    publishProgress(String.valueOf(-3), String.valueOf(count));
                } else {
                    if (count == 0)
                        writtenBytes = 0;
                    else
                        writtenBytes = bufferSize * 25 / 2;
                    max = 10000000.0;
                    ii = 0;
                    do {
                        readBytes = recorder.read(data, writtenBytes, bufferSize);
                        writtenBytes += readBytes;
                    }
                    while (writtenBytes < bufferSize * 25);
                    for (i = 0; i < 1024; i++)
                        accu[i] = 0.0;

                    while (ii < writtenBytes) {
                        for (i = 0; i < 2048; i++) {
                            fdata[i] = (float) data[ii + i];
                        }
                        fft.forwardTransform(fdata);
                        for (i = 0; i < 1024; i++)
                            amp[i] = Math.sqrt(fdata[2 * i] * fdata[2 * i] + fdata[2 * i + 1] * fdata[2 * i + 1]);
                        for (i = 0; i < 1024; i++)
                            accu[i] += amp[i];

                        ii += i;
                    }

                    double sum;
                    int k;
                    for (j = 0; j < 40; j++) {
                        sum = 0;
                        for (k = 0; k < 6; k++)
                            sum += accu[6*j + k];
                        disp[j] = sum/6;

                        if (sum/6 > max)
                            max = sum/6;
                    }

                    publishProgress(String.valueOf(-2));

                    SoundName = null;
                    filenum = 0;
                    file = new File(dir, filenum + ".txt");
                    FileInputStream stream;
                    DataInputStream dis;
                    while (file.exists()) {
                        stream = null;
                        try {
                            stream = new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        dis = new DataInputStream(stream);
                        try {
                            SoundName = dis.readUTF();
                            for (i = 0; i < 1024; i++) {
                                ref[i] = dis.readDouble();
                                peaks[i] = dis.readBoolean();
                            }
                            Log.d("TAG", "Read from file" + filenum);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            dis.close();
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        black = 0.0;
                        bref = 0.0;
                        white = 0.0;
                        wref = 0.0;

                        bnum = 0;
                        wnum = 0;
                        for (i = 0; i < 1024; i++) {
                            if (peaks[i]) {
                                black += accu[i];
                                bref += ref[i];
                                bnum++;
                            } else {
                                white += accu[i];
                                wref += ref[i];
                                wnum++;
                            }
                        }
                        black /= bnum;
                        white /= wnum;
                        bref /= bnum;
                        wref /= wnum;

                        publishProgress(String.valueOf(filenum), String.valueOf(Math.round((black / white) / (bref / wref) * 100)), String.valueOf(black / white <= 1.0), SoundName);

                        filenum++;
                        file = new File(dir, filenum + ".txt");
                    }
                    publishProgress(String.valueOf(-1));
                }

                if (Thread.currentThread().isInterrupted()) {
                    count = 0;
                    break;
                }

                for (i = 0; i < bufferSize * 25 / 2; i++)
                    data[i] = data[i + bufferSize * 25 / 2];
                count++;
            }
            recorder.stop();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            int j;
            j = Integer.valueOf(values[0]);

            if (j >= 0) {
                if (j == 0) {
                    Detected.clear();
                    DetectedIndex.clear();
                    DetectedIndexCopy.clear();
                }
                if (values[2].equals("false") && Integer.valueOf(values[1]) >= 30) {
                    Detected.put(Integer.valueOf(values[1]), values[3]);
                    DetectedIndex.add(j);
                    DetectedIndexCopy.add(j);
                    activate = true;
                }
                /*tv[j][0].setText(values[0] + " " + values[3]);
                tv[j][1].setText(values[1] + "%");
                Log.d("TAG", "read " + values[3]);
                if (values[2].equals("true") || Integer.valueOf(values[1]) <= 30) {
                    tv[j][0].append(" = NOISE");
                }
                else {
                    tv[j][0].append(" = EVENT");

                    activate = true;
                }*/
            }
            else if (j == -2) {
                for (j = 0; j < 40; j++) {
                    pb[j].setProgress(0);
                    pb[j].setMax((int) Math.round(max));
                    pb[j].setProgress((int) Math.round(disp[j]));
                }
            }
            else if (j == -1) {
                if (activate) {
                    if(System.currentTimeMillis()-lastused<notidelay) activate=false;
                    if(activate) {
                        if (!first) {
                            if (alertDialog.isShowing()) {
                                alertDialog.dismiss();
                                writeToStats(0, 0, 1);
                                DetectedIndexCopy.clear();
                            }
                            if (vi != null) {
                                ViewGroup parent = (ViewGroup) vi.getParent();
                                if (parent != null) {
                                    parent.removeView(vi);
                                }
                            }
                            try {
                                vi = inflater.inflate(R.layout.dialog_alert2, null, false);
                            } catch (InflateException e) {
                                e.printStackTrace();
                            }
                        }
                        Iterator entries = Detected.entrySet().iterator();
                        for (int i = 0; i < 3; i++) {
                            for (j = 0; j < 3; j++) {
                                tv[i][j] = (TextView) vi.findViewById(tvid[3 * i + j]); //get a reference to the textview on the dialog_alert2.xml file.
                            }
                            if (entries.hasNext()) {
                                Map.Entry thisEntry = (Map.Entry) entries.next();
                                tv[i][0].setText(thisEntry.getValue().toString());
                                if (i == 0) {
                                    String S = thisEntry.getValue().toString().toLowerCase();
                                    ImageView I = (ImageView) vi.findViewById(R.id.imageView);
                                    if (S.contains("car")) {
                                        I.setImageResource(R.drawable.car);
                                    }
                                    else if (S.contains("phone")) {
                                        I.setImageResource(R.drawable.phone);
                                    }
                                    else if (S.contains("fire")) {
                                        I.setImageResource(R.drawable.fire);
                                    }
                                }
                                tv[i][0].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30 - thisEntry.getValue().toString().length());
                                tv[i][1].setText(" = ");
                                tv[i][2].setText(thisEntry.getKey().toString() + "%");
                            }
                            else {
                                tv[i][0].setText("");
                                tv[i][1].setText("");
                                tv[i][2].setText("");
                            }
                        }
                        first = false;
                        lastused = System.currentTimeMillis();
                        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.app_preferences, false);
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        if(settings.getBoolean("vibrate_noti",false)==true)
                            vibrator.vibrate(notidelay);
                        if(settings.getBoolean("LED_noti",false)==true)
                            turnOnFlash();

                        alertDialogBuilder = new AlertDialog.Builder(
                                context);

                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(vi);
                        alertDialogBuilder.setTitle("Sound Detected!");
                        //alertDialogBuilder.setCancelable(false);

                        // set dialog message
                        alertDialogBuilder.setPositiveButton("Correct", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                writeToStats(1, 0, 0);
                            }
                        });

                        alertDialogBuilder.setNegativeButton("Incorrect", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                writeToStats(0, 1, 0);
                            }
                        });

                        alertDialogBuilder.setNeutralButton("Unsure", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                writeToStats(0, 0, 1);
                            }
                        });

                        // create alert dialog
                        alertDialog = alertDialogBuilder.create();
                        //alertDialog.setCanceledOnTouchOutside(false);

                        // show it
                        alertDialog.show();

                        activate = false;
                    }
                }
            }
            else {
                //lite mode
                for (j = 0; j < 40; j++) {
                    pb[j].setMax((int) Math.round(max));
                    if (j % 2 == Integer.valueOf(values[1])%2)
                        pb[j].setProgress((int) Math.round(max));
                    else
                        pb[j].setProgress(0);
                }
                if (activate) {
                    if(System.currentTimeMillis()-lastused<notidelay) activate=false;
                    if(activate) {
                        if (!first) {
                            if (alertDialogLite.isShowing()) alertDialogLite.dismiss();
                        }
                        first = false;
                        lastused = System.currentTimeMillis();
                        // get prompts.xml view
                        alertDialogLiteBuilder = new AlertDialog.Builder(
                                context);

                        alertDialogLiteBuilder.setTitle("Environment sound intensity reaches threshold!");
                        alertDialogLiteBuilder.setMessage("Event occured at: " + getCurrentTimeStamp());

                        // set dialog message
                        alertDialogLiteBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                        // create alert dialog
                        alertDialogLite = alertDialogLiteBuilder.create();

                        // show it
                        alertDialogLite.show();

                        PreferenceManager.setDefaultValues(getApplicationContext(),R.xml.app_preferences,false);
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        if(settings.getBoolean("vibrate_noti",false)==true)
                            vibrator.vibrate(notidelay);
                        if(settings.getBoolean("LED_noti",false)==true)
                            turnOnFlash();
                    }
                }
            }
        }
    }

    private void turnOnFlash() {
        if (Build.VERSION.SDK_INT >= 23) {
            //String cameraId = null; // Usually front camera is at 0 position.
            try {
                for (String cameraId : camManager.getCameraIdList()) {
                    try {
                        CameraCharacteristics camCharacteristics = camManager.getCameraCharacteristics(cameraId);
                        if (camCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                            camManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                                @Override
                                public void onOpened(@NonNull CameraDevice camera) {
                                    mCamera = camera;
                                }

                                @Override
                                public void onDisconnected(@NonNull CameraDevice camera) {

                                }

                                @Override
                                public void onError(@NonNull CameraDevice camera, int error) {

                                }
                            }, null);
                            camManager.setTorchMode(cameraId, true);
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                releaseCameraAndPreview();
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "failed to open Camera");
                e.printStackTrace();
            }
        }
    }

    private void releaseCameraAndPreview() {
        if (cam != null) {
            cam.release();
            cam = null;
        }
    }

    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    private void writeToStats(int dcor, int dincor, int duns) {
        int cor, incor, uns;
        for (Integer i : DetectedIndexCopy) {
            cor = 0;
            incor = 0;
            uns = 0;
            file = new File(dir, i + "stats.txt");
            FileInputStream fis;
            DataInputStream dis;
            if (file.exists()) {
                fis = null;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                dis = new DataInputStream(fis);
                try {
                    cor = dis.readInt();
                    incor = dis.readInt();
                    uns = dis.readInt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    dis.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d("TAG", "was" + cor + " " + incor + " " + uns);
            cor += dcor;
            incor += dincor;
            uns += duns;
            Log.d("TAG", "now" + cor + " " + incor + " " + uns);
            file = new File(dir, i + "stats.txt");
            FileOutputStream fos;
            DataOutputStream dos;
            fos = null;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            dos = new DataOutputStream(fos);
            try {
                dos.writeInt(cor);
                dos.writeInt(incor);
                dos.writeInt(uns);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
