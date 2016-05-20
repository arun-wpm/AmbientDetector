package th.ac.mwits.www.ambientdetector;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Environment;
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
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 44100; /* in Hz*/
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
    private int sampleNumBits = 16;
    private int numChannels = 1;

    byte[] data = new byte[441000];
    TextView textView;
    Button button;
    boolean first = true;

    AudioRecord recorder;
    AudioTrack audioPlayer;

    //String root;
    //File dir;
    //File file;

    int readBytes, writtenBytes = 0;
    int i;
    ProgressBar[] pb = new ProgressBar[40];
    double max = 1000000.0;
    int ii = 0;
    FFT fft = new FFT(2048, new HammingWindow());
    float[] fdata = new float[2048];
    double[] amp = new double[1024];
    double[] accu = new double[1024];

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

    //boolean goToLoop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //root = Environment.getExternalStorageDirectory().toString();
        //Log.d("TAG", root);
        //dir = new File(root + "/FFT");
        //dir.mkdir();
        //file = new File(dir, "FFTdata.txt");

        bufferSize += 2048;

        for (i = 0; i < 40; i++)
            pb[i] = (ProgressBar) findViewById(pbid[i]);

        recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        Log.d("TAG", "Start recording");

        audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 50, AudioTrack.MODE_STREAM);
        Log.d("TAG", "Initialized playback");

        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        /*if (goToLoop)
            loop();*/
    }

    @Override
    public void onClick(View v) {
        //while (true) {
            writtenBytes = 0;
            max = 1000000.0;
            ii = 0;
            recorder.startRecording();
            button.setVisibility(View.INVISIBLE);
            do {
                readBytes = recorder.read(data, writtenBytes, bufferSize);
                Log.d("TAG", "Read" + readBytes);

                if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
                    writtenBytes += audioPlayer.write(data, writtenBytes, readBytes);
                    Log.d("TAG", "Write" + writtenBytes);
                }
            }
            while (writtenBytes < bufferSize * 50);
            recorder.stop();
            Log.d("TAG", "Read and Write" + writtenBytes);

            while (ii < writtenBytes / 2) {
                for (i = 0; i < 2048; i++) {
                    fdata[i] = (float) data[ii + i];
                }
                fft.forwardTransform(fdata);
                for (i = 0; i < 1024; i++)
                    amp[i] = Math.sqrt(fdata[2 * i] * fdata[2 * i] + fdata[2 * i + 1] * fdata[2 * i + 1]);
                for (i = 0; i < 1024; i++)
                    accu[i] += amp[i];
                Log.d("TAG", "FFT done to " + ii);
                ii += i;
            }

            for (i = 0; i < 40; i++) {
                if (accu[i] > max)
                    max = accu[i];
            }
            for (i = 0; i < 40; i++) {
                pb[i].setMax((int) Math.round(max));
                pb[i].setProgress((int) Math.round(accu[i]));

                //pb[i].requestLayout();
                accu[i] = 0.0;
            }

            audioPlayer.play();
            do {                                                     // Montior playback to find when done
                i = audioPlayer.getPlaybackHeadPosition();
                Log.d("TAG", "Play" + i);
            } while (i < writtenBytes / 2);
            Log.d("TAG", "Played" + i);

            /*RelativeLayout rl = (RelativeLayout) findViewById (R.id.relativelayout);
            rl.requestLayout();*/

            audioPlayer.stop();
            audioPlayer.flush();
            //audioPlayer.release();
            button.setVisibility(View.VISIBLE);
            Log.d("TAG", "buffersize" + bufferSize);
        //goToLoop = true;
        loop();
        //}
    }

    public void loop() {
        for (i = 0; i < 40; i++) {
            pb[i].requestLayout();
        }
        button.performClick();
    }

}
