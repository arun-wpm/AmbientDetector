package th.ac.mwits.www.ambientdetector;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.hardware.camera2.*;
import android.widget.Toast;

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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;

import static be.tarsos.dsp.beatroot.Peaks.findPeaks;

public class Proj160623 extends AppCompatActivity {

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 44100; /* in Hz*/
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
    private int sampleNumBits = 16;
    private int numChannels = 1;
    private Flashlight F = new Flashlight();
    int count = 0;
    short[] data = new short[441000];
    TextView textView;
    Button start, stop, record, stop_vibrate;

    AudioRecord recorder;
    AudioTrack audioPlayer;

    Vibrator vibrator;

    //PreferenceManager.setDefaultValues(this,R.xml.app_preferences,false);
    //SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    File dir;
    File file;
    String SoundName;
    File file2;
    File file3;
    int filenum = 0;
    Toolbar toolbar;

    int readBytes, writtenBytes = 0;

    int i, j;
    ProgressBar[] pb = new ProgressBar[40];
    TextView[][] tv = new TextView[6][2];
    double max = 10000000.0;
    int ii = 0;
    FFT fft = new FFT(2048, new HammingWindow());
    float[] fdata = new float[2048];
    double[] amp = new double[1024];
    double[] accu = new double[1024];
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

    private static final int[] tvid = {
            R.id.textView5,
            R.id.textView6,
            R.id.textView7,
            R.id.textView8,
            R.id.textView9,
            R.id.textView10,
            R.id.textView11,
            R.id.textView12,
            R.id.textView13,
            R.id.textView14,
            R.id.textView15,
            R.id.textView16,
    };

    MyTask myTask;

    final Context context = this;

    Camera cam;
    Button FlOff;
    //TextView result, refresult, percent;
    double black = 0.0, bref = 0.0;

    double white = 0.0, wref = 0.0;
    int bnum = 0, wnum = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    public double S2(int k, int i) {
        double t = k * accu[i] - (quicksum[i - 1] - quicksum[i - k - 1]);
        t = t + k * accu[i] - (quicksum[i + k] - quicksum[i]);
        return t / (double) (k * 2);
    }

    String root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        root = Environment.getExternalStorageDirectory().toString();
        //Log.d("TAG", root);
        dir = new File(root + "/FFT");
        dir.mkdir();
        //file = new File(dir, "FFTdata.txt");

        bufferSize += 2048;

        for (i = 0; i < 40; i++)
            pb[i] = (ProgressBar) findViewById(pbid[i]);

        for (i = 0; i < 6; i++)
            for (j = 0; j < 2; j++)
                tv[i][j] = (TextView) findViewById(tvid[2 * i + j]);

        recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        Log.d("TAG", "Start recording");

        audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 50, AudioTrack.MODE_STREAM);
        Log.d("TAG", "Initialized playback");

        textView = (TextView) findViewById(R.id.textView);
        start = (Button) findViewById(R.id.button);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myTask = new MyTask();
                myTask.execute();
            }
        });

        stop = (Button) findViewById(R.id.button2);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myTask.cancel(true);
            }
        });

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        stop_vibrate = (Button) findViewById(R.id.off);
        stop_vibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrator.cancel();
                if (Build.VERSION.SDK_INT >= 21) F.turnOffFlashLight();
                else {
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

        record = (Button) findViewById(R.id.button3);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writtenBytes = 0;
                max = 10000000.0;
                ii = 0;
                recorder.startRecording();
                do {
                    readBytes = recorder.read(data, writtenBytes, bufferSize);
                    //Log.d("TAG", "Read" + readBytes);

                    if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
                        writtenBytes += audioPlayer.write(data, writtenBytes, readBytes);
                        //Log.d("TAG", "Write" + writtenBytes);
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

                    //Log.d("TAG", "FFT done to " + ii);
                    ii += i;
                }

                //findPeaks(accu, peaks, 1);
                file = new File(dir, filenum + ".txt");
                while (file.exists()) {
                    filenum++;
                    file = new File(dir, filenum + ".txt");
                }

                /*for (i = 1; i < 1023; i++)
                    peaks[i] = (accu[i] > median(accu[i - 1], accu[i], accu[i + 1]) + 10000000);*/

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
                    // printf("%f\n",S2(k,i));
                    sum = sum + a[i - k];
                }
                mean = (double) sum / c;
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
                try {
                    for (i = 0; i < 1024; i++) {
                        dos.writeDouble(accu[i]);
                        dos.writeBoolean(peaks[i]);
                    }
                    Log.d("TAG", "Write Results");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (i = 0; i < 1024; i++) {
                    if (accu[i] > max)
                        max = accu[i];
                }

                int j;
                for (j = 0; j < 40; j++) {
                    pb[j].setMax((int) Math.round(max));
                    pb[j].setProgress((int) Math.round(accu[j]));
                    accu[j] = 0.0;
                }

                audioPlayer.play();
                do {                                                     // Montior playback to find when done
                    i = audioPlayer.getPlaybackHeadPosition();
                    //Log.d("TAG", "Play" + i);
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
                        Name[0] = userInput[0].getText().toString();
                        try {
                            dos.writeUTF(Name[0]);
                            Log.d("TAG", "write " + Name[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int res_id = item.getItemId();
        if (res_id == R.id.action_settings) {
            //Toast.makeText(getApplicationContext(), "Settings Selected", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(Proj160623.this, AppPreferances.class);
            startActivity(i);
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Proj160623 Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://th.ac.mwits.www.ambientdetector/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Proj160623 Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://th.ac.mwits.www.ambientdetector/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    class MyTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            while (true) {
                audioPlayer.release();
                try {
                    Thread.sleep(100);
                    Log.d("TAG", "release AudioTrack");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize * 50, AudioTrack.MODE_STREAM);
                Log.d("TAG", "" + count);
                if (count == 0)
                    writtenBytes = 0;
                else
                    writtenBytes = bufferSize * 25 / 2;
                max = 10000000.0;
                ii = 0;

                recorder.startRecording();
                do {
                    readBytes = recorder.read(data, writtenBytes, bufferSize);
                    //Log.d("TAG", "Read" + readBytes);

                    if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
                        writtenBytes += audioPlayer.write(data, writtenBytes, readBytes);
                        //Log.d("TAG", "Write" + writtenBytes);
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

                    //Log.d("TAG", "FFT done to " + ii);
                    ii += i;
                }

                for (i = 0; i < 40; i++) {
                    if (accu[i] > max)
                        max = accu[i];
                }

                publishProgress(String.valueOf(-1), "", "");

                SoundName = null;
                filenum = 0;
                file = new File(dir, filenum + ".txt");
                while (file.exists()) {
                    FileInputStream stream = null;
                    try {
                        stream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    DataInputStream dis = new DataInputStream(stream);
                    try {
                        for (i = 0; i < 1024; i++) {
                            ref[i] = dis.readDouble();
                            peaks[i] = dis.readBoolean();
                        }
                        SoundName = dis.readUTF();
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

                if (Thread.currentThread().isInterrupted()) {
                    count = 0;
                    break;
                }

                for (i = 0; i < bufferSize * 25 / 2; i++)
                    data[i] = data[i + bufferSize * 25 / 2];
                count++;
            }
            //audioPlayer.release();
            /*Log.d("TAG", "onPostExecute release");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            int j;
            j = Integer.valueOf(values[0]);

            if (j > -1) {
                tv[j][0].setText(values[0] + " " + values[3]);
                Log.d("TAG", "read " + values[3]);
                if (values[2].equals("true") || Integer.valueOf(values[1]) <= 30)
                    tv[j][0].append(" = NOISE");
                else {
                    tv[j][0].append(" = EVENT");
                    //if(settings.getBoolean("vibrate_noti", false))
                        vibrator.vibrate(10000);
                    if (Build.VERSION.SDK_INT >= 21) F.turnOnFlashLight();
                    else {
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
                tv[j][1].setText(values[1] + "%");
            } else {
                for (j = 0; j < 40; j++) {
                    pb[j].setMax((int) Math.round(max));
                    pb[j].setProgress((int) Math.round(accu[j]));
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private void releaseCameraAndPreview() {
        //myCameraPreview.setCamera(null);
        if (cam != null) {
            cam.release();
            cam = null;
        }
    }
}
