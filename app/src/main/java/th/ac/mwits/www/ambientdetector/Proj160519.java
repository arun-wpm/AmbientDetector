package th.ac.mwits.www.ambientdetector;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import static be.tarsos.dsp.beatroot.Peaks.findPeaks;

public class Proj160519 extends AppCompatActivity {

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 44100; /* in Hz*/
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
    private int sampleNumBits = 16;
    private int numChannels = 1;

    short[] data = new short[441000];
    TextView textView;
    Button start, stop, record;

    AudioRecord recorder;
    AudioTrack audioPlayer;

    String root;
    File dir;
    File file;
    File file2;
    File file3;
    int filenum = 0;

    int readBytes, writtenBytes = 0;
    int i, j;
    ProgressBar[] pb = new ProgressBar[40];
    TextView[][] tv = new TextView[6][2];
    double max = 100000.0;
    int ii = 0;
    FFT fft = new FFT(2048, new HammingWindow());
    float[] fdata = new float[2048];
    double[] amp = new double[1024];
    double[] accu = new double[1024];

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

    //TextView result, refresult, percent;
    double black = 0.0, bref = 0.0;
    double white = 0.0, wref = 0.0;
    int bnum = 0, wnum = 0;

    public double median(double a, double b, double c) {
        if ((a > b && a < c) || (a < b && a > c))
            return a;
        else if ((b > a && b < c) || (b < a && b > c))
            return b;
        else if ((c > a && c < b) || (c < a && c > b))
            return c;
        else
            return 0.0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

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
                tv[i][j] = (TextView) findViewById(tvid[2*i + j]);

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

        record = (Button) findViewById(R.id.button3);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writtenBytes = 0;
                max = 100000.0;
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

                for (i = 1; i < 1023; i++)
                    peaks[i] = (accu[i] > median(accu[i - 1], accu[i], accu[i + 1]) + 10000000);
                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                DataOutputStream dos = new DataOutputStream(stream);
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
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (i = 0; i < 40; i++) {
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
            }
        });

        //result = (TextView) findViewById(R.id.textView2);
        //refresult = (TextView) findViewById(R.id.textView3);
        //percent = (TextView) findViewById(R.id.textView4);
    }

    class MyTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            while (true) {
                writtenBytes = 0;
                max = 100000.0;
                ii = 0;

                recorder.startRecording();
                do {
                    readBytes = recorder.read(data, writtenBytes, bufferSize);
                    Log.d("TAG", "Read" + readBytes);

                    if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
                        writtenBytes += audioPlayer.write(data, writtenBytes, readBytes);
                        Log.d("TAG", "Write" + writtenBytes);
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

                file2 = new File(dir, "dump.txt");
                file3 = new File(dir, "raw.txt");
                FileOutputStream stream2 = null;
                FileOutputStream stream3 = null;
                try {
                    stream2 = new FileOutputStream(file2);
                    stream3 = new FileOutputStream(file3);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    for (i = 0; i < 1024; i++) {
                        stream2.write((String.valueOf(accu[i]) + "\n").getBytes());
                    }
                    for (i = 0; i < writtenBytes/2; i++)
                        stream3.write((String.valueOf(data[i]) + "\n").getBytes());
                    Log.d("TAG", "Dump data");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    stream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                publishProgress(String.valueOf(-1), "", "");

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
                        }
                        else {
                            white += accu[i];
                            wref += ref[i];
                            wnum++;
                        }
                    }
                    black /= bnum;
                    white /= wnum;
                    bref /= bnum;
                    wref /= wnum;

                    publishProgress(String.valueOf(filenum), String.valueOf(Math.round((black/white)/(bref/wref)*100) + "%"), String.valueOf(black - 20000 < white));

                    /*file2 = new File(dir, "dump" + filenum + ".txt");
                    stream2 = null;
                    try {
                        stream2 = new FileOutputStream(file2);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        for (i = 0; i < 1024; i++) {
                            stream2.write((String.valueOf(ref[i]) + " ").getBytes());
                            stream2.write((String.valueOf(peaks[i]) + "\n").getBytes());
                        }
                        Log.d("TAG", "Dump data");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        stream2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/

                    filenum++;
                    file = new File(dir, filenum + ".txt");
                }

                //publishProgress(String.valueOf(black), String.valueOf(white), String.valueOf(bref), String.valueOf(wref), String.valueOf((black/white)/(bref/wref)*100), String.valueOf(black < white));
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result.setText("Black" + black);
                        refresult.setText("White" + white);
                        percent.setText(String.valueOf((black/white)/(bref/wref)*100));

                        int j;
                        for (j = 0; j < 40; j++) {
                            pb[j].setMax((int) Math.round(max));
                            pb[j].setProgress((int) Math.round(accu[j]));
                            accu[j] = 0.0;
                        }
                    }
                });*/

                audioPlayer.play();
                do {                                                     // Montior playback to find when done
                    i = audioPlayer.getPlaybackHeadPosition();
                    //Log.d("TAG", "Play" + i);
                } while (i < writtenBytes);

                audioPlayer.stop();
                audioPlayer.flush();

                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                if (Thread.currentThread().isInterrupted()) break;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            /*result.setText("Black : " + values[0] + " White : " + values[1]);
            refresult.setText("Fingerprint :\nBlack : " + values[2] + " White : " + values[3]);
            percent.setText("Certainty : " + values[4]);*/
            int j;
            j = Integer.valueOf(values[0]);

            if (j > -1) {
                tv[j][0].setText(values[0]);
                if (values[2].equals("true"))
                    tv[j][0].append(" = NOISE");
                else
                    tv[j][0].append(" = EVENT");
                tv[j][1].setText(values[1]);
            }
            else {
                for (j = 0; j < 40; j++) {
                    pb[j].setMax((int) Math.round(max));
                    pb[j].setProgress((int) Math.round(accu[j]));
                }
                /*for (j = 0; j < 1024; j++) {
                    accu[j] = 0.0;
                }*/
            }

            /*if (values[5].equals("true"))
                textView.setText("NOISE");
            else
                textView.setText("EVENT");*/

            //result.invalidate();
            //refresult.invalidate();
            //percent.invalidate();
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
}
