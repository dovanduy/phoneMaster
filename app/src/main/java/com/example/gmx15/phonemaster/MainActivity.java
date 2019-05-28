package com.example.gmx15.phonemaster;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import android.util.Log;

import com.example.gmx15.phonemaster.automation.ServerThread;
import com.example.gmx15.phonemaster.recording.Recorder;
import com.example.gmx15.phonemaster.utilities.CreateSocket;
import com.example.gmx15.phonemaster.utilities.KeyUtil;
import com.example.gmx15.phonemaster.utilities.MyThread;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public static MainActivity self;

    public TextToSpeech mTextToSpeech=null;

    public static KeyUtil keyutil;
    public static Boolean isStarted = false;

    public static Recorder recorder;

    private InitListener mInitListener;

    private static SpeechRecognizer mAsr;

    private AlertDialog.Builder builder;
    private AlertDialog interactingDialog;

    public void tipClick() {
        builder.setTitle("是否创建变量？");
        builder.setMessage("等待中...");
        builder.setCancelable(false);
        interactingDialog = builder.create();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            interactingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            interactingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
        interactingDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        interactingDialog.show();
    }

    public void cancelInteractingDialog() {
        interactingDialog.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        self = this;

        builder = new AlertDialog.Builder(this);

        CreateSocket t = new CreateSocket();
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        recorder = new Recorder(t.sck);

        // init xunfei speech recognizer
        SpeechUtility.createUtility(this, "appid=5cadd877");

        mInitListener = new InitListener() {

            @Override
            public void onInit(int code) {
                Log.i("ERR", "初始化");
                if (code != ErrorCode.SUCCESS) {
                    Log.i("ERR", "初始化失败,错误码："+code);
                }
            }
        };

        mAsr = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);

        mAsr.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置语言
        mAsr.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mAsr.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mAsr.setParameter(SpeechConstant.VAD_BOS,"4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mAsr.setParameter(SpeechConstant.VAD_EOS,  "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mAsr.setParameter(SpeechConstant.ASR_PTT,  "0");


        // init voice hints
        MediaPlayer startMediaPlayer = MediaPlayer.create(this, R.raw.start);
        MediaPlayer endMediaPlayer = MediaPlayer.create(this, R.raw.end);
        MediaPlayer yesMediaPlayer = MediaPlayer.create(this, R.raw.yes);

        keyutil = new KeyUtil(this, "RecordKey", startMediaPlayer, endMediaPlayer, yesMediaPlayer);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Thread t = new ServerThread();
                  t.start();
              }
          });

        //实例并初始化TTS对象
        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
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

    public static void startRecognizer() {

        mAsr.stopListening();

        mAsr.startListening(new RecognizerListener() {

            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

            }

            @Override
            public void onBeginOfSpeech() {
                // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
                Log.i("xunfei", "谈话中····");
            }

            @Override
            public void onEndOfSpeech() {
                Log.i("xunfei", "结束");
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                if (!b) {
                    parseResult(recognizerResult.getResultString());
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                Log.e("test_xunfei", speechError.getErrorCode() + "");
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }

        });
    }

    public static String parseResult(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            JSONArray jsonArray = null;
            jsonArray = jsonObject.getJSONArray("ws");
            StringBuffer stringBuffer = new StringBuffer();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                JSONArray jsonArray1 = jsonObject1.getJSONArray("cw");
                JSONObject jsonObject2 = jsonArray1.getJSONObject(0);
                String w = jsonObject2.getString("w");
                stringBuffer.append(w);
            }
            String result = stringBuffer.toString();
            Log.i("RecordEnd", "识别结果为：" + result);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
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
