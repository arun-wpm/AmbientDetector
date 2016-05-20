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
import android.widget.TextView;
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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

    String root;
    File dir;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = Environment.getExternalStorageDirectory().toString();
        Log.d("TAG", root);
        dir = new File(root + "/FFT");
        dir.mkdir();
        file = new File(dir, "FFTdata.txt");

        bufferSize += 2048;

        recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        recorder.startRecording();
        Log.d("TAG", "Start recording");

        audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 50, AudioTrack.MODE_STREAM);
        Log.d("TAG", "Initialized playback");

        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int readBytes = 0, writtenBytes = 0;
        if (first) {
            button.setVisibility(View.GONE);

        /*if(audioPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
            audioPlayer.play();*/

            //capture data and record to file
            int i;
            do {
                readBytes = recorder.read(data, writtenBytes, bufferSize);
                //Log.d("TAG", "Read");

                if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
                    writtenBytes += audioPlayer.write(data, writtenBytes, readBytes);
                    //Log.d("TAG", "Write" + writtenBytes);
                    textView.setText(String.valueOf(writtenBytes));

        /*if (writtenBytes > 441000)
            break;*/
                }
            }
            while (writtenBytes < bufferSize * 50);
            Log.d("TAG", "Read and Write" + writtenBytes);

            recorder.stop();
            audioPlayer.play();
            do{                                                     // Montior playback to find when done
                i = audioPlayer.getPlaybackHeadPosition();
                //Log.d("TAG", "Play" + i);
            }while (i < writtenBytes/2);
            Log.d("TAG", "Played" + i);

            audioPlayer.stop();
            audioPlayer.flush();

            first = false;
            button.setVisibility(View.VISIBLE);
        }
        else {
            button.setVisibility(View.GONE);

            int i;
            for (i = bufferSize*25; i < bufferSize*50; i++)
                data[i - bufferSize*25] = data[i];
            Log.d("TAG", "Moved");

        /*if(audioPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
            audioPlayer.play();*/

            //capture data and record to file
            recorder.startRecording();
            do {
                readBytes = recorder.read(data, writtenBytes + bufferSize*25, bufferSize);
                //Log.d("TAG", "Read");

                if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
                    writtenBytes += audioPlayer.write(data, writtenBytes, readBytes);
                    //Log.d("TAG", "Write" + writtenBytes);
                    textView.setText(String.valueOf(writtenBytes));

        /*if (writtenBytes > 441000)
            break;*/
                }
            }
            while (writtenBytes < bufferSize * 50);
            Log.d("TAG", "Read and Write" + writtenBytes);

            recorder.stop();
            audioPlayer.play();
            do{                                                     // Montior playback to find when done
                i = audioPlayer.getPlaybackHeadPosition();
                //Log.d("TAG", "Play" + i);
            }while (i < writtenBytes/2);
            Log.d("TAG", "Played" + i);

            audioPlayer.stop();
            audioPlayer.flush();

            button.setVisibility(View.VISIBLE);
        }

        int i;
        int ii = 0;
        FFT fft = new FFT(1024, new HammingWindow());
        float[] fdata = new float[1024];
        double[] amp = new double[512];
        double[] accu = new double[512];

        while (ii < writtenBytes/2) {
            for (i = 0; i < 1024; i++) {
                fdata[i] = (float) data[ii + i];
                //Log.d("TAG", String.valueOf(fdata[i]));
            }
        /*try {
            for (i = 0; i < writtenBytes/2; i++)
                stream.write((String.valueOf(fdata[i]) + '\n').getBytes());
            Log.d("TAG", "Write Audio");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
            fft.forwardTransform(fdata);
            for (i = 0; i < 512; i++)
                amp[i] = Math.sqrt(fdata[2*i]*fdata[2*i] + fdata[2*i + 1]*fdata[2*i + 1]);
            for (i = 0; i < 512; i++)
                accu[i] += amp[i];
            Log.d("TAG", "FFT done to " + ii);
            ii += i;
        }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            for (i = 0; i < 512; i++)
                stream.write((String.valueOf(accu[i]) + '\n').getBytes());
            Log.d("TAG", "Write Results");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
