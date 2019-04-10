package com.example.gmx15.phonemaster;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.util.Log;

import java.util.Locale;

import com.huawei.hiai.asr.AsrConstants;
import com.huawei.hiai.asr.AsrError;
import com.huawei.hiai.asr.AsrListener;
import com.huawei.hiai.asr.AsrRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AsrActivity";
    static private TextToSpeech mTextToSpeech=null;

    public static TextToSpeech getmTextToSpeech() {
        return MainActivity.mTextToSpeech;
    }

    private AsrRecognizer asrRecognizer = null;
    private HWAsrListener asrListener;
    //HUAWEI ASR引擎参数
    private HuaweiAsrModel huaweiModel;
    private boolean isSupportHwAsr = true;

    private long startTime;

    public static KeyUtil keyutil;
    public static Boolean isStarted = false;


//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        keyutil.dispatchKeyEvent(event);
//        return super.onKeyUp(keyCode, event);
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        keyutil.dispatchKeyEvent(event);
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaPlayer startMediaPlayer = MediaPlayer.create(this, R.raw.start);
        MediaPlayer endMediaPlayer = MediaPlayer.create(this, R.raw.end);
        keyutil = new KeyUtil(this, "keypress", startMediaPlayer, endMediaPlayer);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Log.i("Start", "start test!");


        fab.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  if (isSupportHwAsr) {
                      Intent intent = new Intent();
                      intent.putExtra(AsrConstants.ASR_VAD_FRONT_WAIT_MS, 10000);
                      //intent.putExtra(AsrConstants.ASR_VAD_END_WAIT_MS, 30000);
                      //intent.putExtra(AsrConstants.ASR_RESULT_TIME_WAIT_MS, 30000);
                      //intent.putExtra(AsrConstants.ASR_TIMEOUT_THRESHOLD_MS, 1000);

                      huaweiModel.startListening(intent);
                      Toast.makeText(MainActivity.this, "识别中....", Toast.LENGTH_LONG).show();
                  } else {
                      Toast.makeText(MainActivity.this, "该设备不支持华为语音引擎！", Toast.LENGTH_SHORT).show();
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

        //判断设备是否支持华为语音引擎
        if (HuaweiAsrModel.isSupportAsr(this)) {
            //初始化华为语音引擎
            initHuaweiAsr();
        } else {
            Toast.makeText(this, "该设备不支持华为语音引擎！", Toast.LENGTH_SHORT).show();
        }
    }

    //初始化华为语音引擎
    private void initHuaweiAsr() {
        if (asrRecognizer == null) {
            asrRecognizer = AsrRecognizer.createAsrRecognizer(this);
            huaweiModel = new HuaweiAsrModel(this, asrRecognizer);
        }
        Intent initIntent = new Intent();
        initIntent.putExtra(AsrConstants.ASR_AUDIO_SRC_TYPE, AsrConstants.ASR_SRC_TYPE_RECORD);
        asrListener = new HWAsrListener();
        asrRecognizer.init(initIntent, asrListener);
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

    @Override
    protected void onPause() {
        super.onPause();
        huaweiModel.cancelListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        huaweiModel.destroyEngine();
    }

    //华为语音监听回调
    private class HWAsrListener implements AsrListener {

        @Override
        public void onInit(Bundle params) {
            Log.d(TAG, "onInit() called with: params = [" + params + "]");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech() called");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged() called with: rmsdB = [" + rmsdB + "]");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.i(TAG, "onBufferReceived() called with: buffer = [" + buffer + "]");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: ");
            startTime = System.currentTimeMillis();
        }

        @Override
        public void onError(int error) {
            Log.d(TAG, "onError() called with: error = [" + error + "]");
            if (error == AsrError.ERROR_SERVER_INSUFFICIENT_PERMISSIONS) {
                Log.d(TAG, "insufficient permission");
                if (asrRecognizer != null) {
                    asrRecognizer.startPermissionRequestForEngine();
                }
            }
        }

        @Override
        public void onResults(Bundle results) {
            Log.d(TAG, "onResults() called with: results = [" + results + "]");
            long costTime = System.currentTimeMillis() - startTime;
            String finalResult = getOnResult(results, AsrConstants.RESULTS_RECOGNITION);
            finalResult = finalResult.replaceAll("[,，。]", "");

            Log.d(TAG, "onResults() called with: final_results = [" + finalResult + "]");
            huaweiModel.stopListening();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults() called with: partialResults = [" + partialResults + "]");
            ;
        }

        @Override
        public void onEnd() {

        }

        private String getOnResult(Bundle partialResults, String key) {
            Log.d(TAG, "getOnResult() called with: getOnResult = [" + partialResults + "]");
            String json = partialResults.getString(key);
            final StringBuffer sb = new StringBuffer();
            try {
                JSONObject result = new JSONObject(json);
                JSONArray items = result.getJSONArray("result");
                for (int i = 0; i < items.length(); i++) {
                    String word = items.getJSONObject(i).getString("word");
                    double confidences = items.getJSONObject(i).getDouble("confidence");
                    sb.append(word);
                    Log.d(TAG, "asr_engine: result str " + word);
                    Log.d(TAG, "asr_engine: confidence " + String.valueOf(confidences));
                }
                Log.d(TAG, "getOnResult: " + sb.toString());
            } catch (JSONException exp) {
                Log.w(TAG, "JSONException: " + exp.toString());
            }
            return sb.toString();
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent() called with: eventType = [" + eventType + "], params = [" + params + "]");
        }

        @Override
        public void onLexiconUpdated(String s, int i) {

        }
    }
}
