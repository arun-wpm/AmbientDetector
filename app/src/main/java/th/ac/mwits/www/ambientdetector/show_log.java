package th.ac.mwits.www.ambientdetector;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by windows on 10-Nov-16.
 */
public class show_log extends AppCompatActivity {
    ArrayList<HashMap<String,String>> Logs=new ArrayList<HashMap<String,String>>();
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_log);
        list=(ListView) findViewById(R.id.listView2);
        getData();
    }
    private void getData()
    {
        String temp= Environment.getExternalStorageDirectory().toString();
        temp=temp+"/FFT";
        File dir=new File(temp);
        dir.mkdir();
        FileInputStream stream;
        DataInputStream dis;
        temp=temp+"/log.txt";

        Log.d("TAG", "hello");
        try
        {
            File file=new File(dir,"log.txt");
            stream=new FileInputStream(file);
            dis=new DataInputStream(stream);
            String line;
            Log.d("TAG", "helloo");
            line=dis.readUTF(); Log.d("TAG", line);
            while(line!=""&&line!="\n")
            {
                HashMap<String,String> L=new HashMap<String,String>();
                L.put("Time",line);
                Log.d("TAG", line);
                line=dis.readUTF();
                L.put("Data",line);
                Logs.add(L);
                Log.d("TAG", line);
                line=dis.readUTF();
            }
            stream.close();
            dis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        showList();
    }
    protected void showList()
    {
        try
        {
            ListAdapter adapter = new SimpleAdapter(
                    show_log.this,Logs,R.layout.log_element,
                    new String[]{"Time","Data"},
                    new int[]{R.id.Time,R.id.Data}
            );
            list.setAdapter(adapter);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
