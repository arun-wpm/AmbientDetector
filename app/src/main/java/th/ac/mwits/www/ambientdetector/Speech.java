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
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Locale;

public class Speech extends AppCompatActivity {

    protected static final int RESULT_SPEECH = 1;
    ToggleButton TBen, TBth, TBjp;
    Button TTS, STT;
    EditText Text;
    TextToSpeech tts;
    Locale locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        TBen = (ToggleButton) findViewById(R.id.togglebutton7);
        TBth = (ToggleButton) findViewById(R.id.togglebutton8);
        TBjp = (ToggleButton) findViewById(R.id.togglebutton9);
        TBen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TBen.setChecked(true);
                TBth.setChecked(false);
                TBjp.setChecked(false);
                TBen.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                TBth.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                TBjp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            }
        });
        TBth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TBen.setChecked(false);
                TBth.setChecked(true);
                TBjp.setChecked(false);
                TBen.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                TBth.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                TBjp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            }
        });
        TBjp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TBen.setChecked(false);
                TBth.setChecked(false);
                TBjp.setChecked(true);
                TBen.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                TBth.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                TBjp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
            }
        });
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
        TTS = (Button) findViewById(R.id.button5);
        TTS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TBen.isChecked())
                    tts.setLanguage(Locale.US);
                else if (TBth.isChecked())
                {
                    locale = new Locale("th_TH");
                    if (tts.isLanguageAvailable(locale) < 0)
                        Toast.makeText(Speech.this, "No support for this language", Toast.LENGTH_LONG).show();
                    tts.setLanguage(locale);
                }
                else if (TBjp.isChecked())
                {
                    locale = new Locale("ja_JP");
                    if (tts.isLanguageAvailable(locale) < 0)
                        Toast.makeText(Speech.this, "No support for this language", Toast.LENGTH_LONG).show();
                    tts.setLanguage(locale);
                }
                else
                    Toast.makeText(Speech.this, "Please select language", Toast.LENGTH_LONG).show();
                Log.d("TAG", Text.getText().toString());
                if (Build.VERSION.SDK_INT >= 21)
                    tts.speak(Text.getText(), TextToSpeech.QUEUE_ADD, null, "Hello");
                else
                    tts.speak(Text.getText().toString(), TextToSpeech.QUEUE_ADD, null);
            }
        });
        STT = (Button) findViewById(R.id.button6);
        STT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                if (TBen.isChecked())
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en_US");
                else if (TBth.isChecked())
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "th_TH");
                else if (TBjp.isChecked())
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "ja_JP");
                else
                    Toast.makeText(Speech.this, "Please select language", Toast.LENGTH_LONG).show();

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
