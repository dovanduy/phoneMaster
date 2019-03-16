package com.example.gmx15.phonemaster;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.TextView;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import static android.support.v4.app.ActivityCompat.startActivityForResult;


public class MainActivity extends AppCompatActivity {

    static private TextToSpeech mTextToSpeech=null;

    public static TextToSpeech getmTextToSpeech() {
        return MainActivity.mTextToSpeech;
    }


    private static final String TAG = "VoiceRecognition";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                    // Display an hint to the user about what he should say.
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说标准普通话");//注意不要硬编码

                    // Given an hint to the recognizer about what the user is going to say
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                    // Specify how many results you want to receive. The results will be sorted
                    // where the first result is the one with   higher confidence.
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);//通常情况下，第一个结果是最准确的。

                    try {
                        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
                    } catch (ActivityNotFoundException a) {
                        Toast t = Toast.makeText(getApplicationContext(),
                                "Opps! Your device doesn't support Speech to Text",
                                Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            });



        //实例并初始化TTS对象
        mTextToSpeech=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status==TextToSpeech.SUCCESS) {
                    //设置朗读语言
                    int supported=mTextToSpeech.setLanguage(Locale.US);
                    if ((supported!=TextToSpeech.LANG_AVAILABLE)&&(supported!=TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
                        Toast.makeText(MainActivity.this, "不支持当前语言！", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            StringBuilder stringBuilder = new StringBuilder();
            int Size = matches.size();
            for(int i=0;i<Size;++i)
            {
                stringBuilder.append(matches.get(i));
                stringBuilder.append("\n");
            }
            Log.i("Voice:", stringBuilder.toString());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
