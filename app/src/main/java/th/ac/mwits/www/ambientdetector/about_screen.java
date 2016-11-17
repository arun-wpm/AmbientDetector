package th.ac.mwits.www.ambientdetector;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;


public class about_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Intent intent = getIntent();
        String src = intent.getStringExtra("src");
        //String src = intent.getDataString();
        ImageView image = (ImageView) findViewById(R.id.imageView2);

        Resources res = getResources();
        int resID = res.getIdentifier(src, "drawable", getPackageName());
        image.setImageResource(resID);
    }
}
