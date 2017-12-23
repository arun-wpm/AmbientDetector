package th.ac.mwits.www.ambientdetector;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class LiveLoudness extends AppCompatActivity {

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 44100; /* in Hz*/
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
    private int sampleNumBits = 16;
    private int numChannels = 1;

    short[] data = new short[441000];
    AudioRecord recorder;
    AudioTrack audioPlayer;
    int readBytes, writtenBytes = 0;

    int i;
    int min = 1000000;
    int max = -1;
    int avg = 0, thisavg = 0;
    int count = 0;
    TextView[] Display = new TextView[4];
    MyTask myTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_loudness);

        Display[0] = (TextView) findViewById(R.id.textView);
        Display[1] = (TextView) findViewById(R.id.textView2);
        Display[2] = (TextView) findViewById(R.id.textView3);
        Display[3] = (TextView) findViewById(R.id.textView4);

        recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        Log.d("TAG", "Start recording");

        myTask = new MyTask();
        myTask.execute();
    }

    @Override
    public void onBackPressed() {
        myTask.cancel(true);
        Log.d("TAG", "Back Pressed");
        super.onBackPressed();
    }

    class MyTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            recorder.startRecording();
            while (true) {
                writtenBytes = 0;
                do {
                    readBytes = recorder.read(data, writtenBytes, bufferSize);
                    writtenBytes += readBytes;
                    Log.d("TAG", "Read and Write" + writtenBytes);
                } while (writtenBytes < bufferSize*2);

                for (i = 0; i < writtenBytes; i++) {
                    thisavg += Math.abs(data[i]);
                }
                thisavg /= writtenBytes;
                if (count != 0) {
                    if (thisavg > max)
                        max = thisavg;
                    if (thisavg < min)
                        min = thisavg;
                }
                if (count == 0)
                    avg = thisavg;
                else
                    avg = (avg*count + thisavg)/(count + 1);

                if (count != 0)
                    publishProgress();
                count++;

                if (Thread.currentThread().isInterrupted()) {
                    //recorder.stop();
                    break;
                }
            }
            recorder.stop();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Display[0].setText(Math.round(20 * Math.log10(thisavg)) + "dB");
            Display[1].setText("Average: " + Math.round(20*Math.log10(avg)) + "dB");
            Display[2].setText("Min: " + Math.round(20*Math.log10(min)) + "dB");
            Display[3].setText("Max: " + Math.round(20*Math.log10(max)) + "dB");
        }
    }
}
