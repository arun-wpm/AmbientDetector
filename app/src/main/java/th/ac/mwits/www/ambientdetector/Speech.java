package th.ac.mwits.www.ambientdetector;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class Speech extends AppCompatActivity {

    protected static final int RESULT_SPEECH = 1;
    Button TTSen, TTSth, STT;
    EditText Text;
    TextToSpeech tts;
    Locale locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        tts = new TextToSpeech(Speech.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });
        Text = (EditText) findViewById(R.id.editText2);
        TTSen = (Button) findViewById(R.id.button5);
        TTSen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tts.setLanguage(Locale.US);
                Log.d("TAG", Text.getText().toString());
                if (Build.VERSION.SDK_INT >= 21)
                    tts.speak(Text.getText(), TextToSpeech.QUEUE_ADD, null, "Hello");
                else
                    tts.speak(Text.getText().toString(), TextToSpeech.QUEUE_ADD, null);
            }
        });
        /*TTSth = (Button) findViewById(R.id.button7);
        TTSth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                locale = new Locale("th");
                tts.setLanguage(locale);
                Log.d("TAG", Text.getText().toString());
                if (Build.VERSION.SDK_INT >= 21)
                    tts.speak(Text.getText(), TextToSpeech.QUEUE_ADD, null, "Hello");
                else
                    tts.speak(Text.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                Text.setText("");
            }
        });*/
        STT = (Button) findViewById(R.id.button6);
        STT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    Text.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    Text.setText(text.get(0));
                }
                break;
            }

        }
    }
}
