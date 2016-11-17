package th.ac.mwits.www.ambientdetector;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AppHelp extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_help);
    }
}