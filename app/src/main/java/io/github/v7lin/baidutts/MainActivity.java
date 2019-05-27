package io.github.v7lin.baidutts;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.baidu.tts.f.n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private static final String APP_ID = "16357689";
    private static final String APP_KEY = "GtDrwbB5f7Hu9oQGBRljye69";
    private static final String SECRET_KEY = "VYKyRtGERGemDmwepD1Rkyab0NcPV99o";

    private SpeechSynthesizer speechSynthesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.init).setOnClickListener(onClickListener);
        findViewById(R.id.start).setOnClickListener(onClickListener);
        findViewById(R.id.pause).setOnClickListener(onClickListener);
        findViewById(R.id.resume).setOnClickListener(onClickListener);
        findViewById(R.id.destroy).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.init:
                    initTts();
                    break;
                case R.id.start:
                    speak();
                    break;
                case R.id.pause:
                    if (speechSynthesizer != null) {
                        speechSynthesizer.pause();
                    }
                    break;
                case R.id.resume:
                    if (speechSynthesizer != null) {
                        speechSynthesizer.resume();
                    }
                    break;
                case R.id.destroy:
                    if (speechSynthesizer != null) {
                        speechSynthesizer.stop();
                        speechSynthesizer.freeCustomResource();
                        speechSynthesizer.release();
                        speechSynthesizer = null;
                    }
                    break;
            }
        }
    };

    private void initTts() {
        if (speechSynthesizer == null) {
            LoggerProxy.printable(true);

            speechSynthesizer = SpeechSynthesizer.getInstance();
            speechSynthesizer.setContext(getApplicationContext());
            speechSynthesizer.setSpeechSynthesizerListener(speechSynthesizerListener);

            int errorCode = speechSynthesizer.setAppId(APP_ID);
            if (errorCode != 0) {
                Log.e("TAG", "xxx: " + convertErrMsg(errorCode));
            }
            errorCode = speechSynthesizer.setApiKey(APP_KEY, SECRET_KEY);
            if (errorCode != 0) {
                Log.e("TAG", "xxx: " + convertErrMsg(errorCode));
            }
//            speechSynthesizer.auth(TtsMode.ONLINE);// 首次联网状态时调用，非必要
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0"); // 设置发声的人声音，在线生效
            speechSynthesizer.setStereoVolume(1.0f, 1.0f); // 设置播放器的音量，即使用speak 播放音量时生效。范围为[0.0f-1.0f]。
            speechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            speechSynthesizer.initTts(TtsMode.MIX); // 初始化离在线混合模式，如果只需要在线合成功能，使用 TtsMode.ONLINE
        }
    }

    private SpeechSynthesizerListener speechSynthesizerListener = new SpeechSynthesizerListener() {
        /**
         * 播放开始，每句播放开始都会回调
         *
         * @param utteranceId
         */
        @Override
        public void onSynthesizeStart(String utteranceId) {
        }

        /**
         * 语音流 16K采样率 16bits编码 单声道 。
         *
         * @param utteranceId
         * @param bytes       二进制语音 ，注意可能有空data的情况，可以忽略
         * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法和合成到第几个字对应。
         */
        @Override
        public void onSynthesizeDataArrived(String utteranceId, byte[] bytes, int progress) {
        }

        /**
         * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
         *
         * @param utteranceId
         */
        @Override
        public void onSynthesizeFinish(String utteranceId) {
        }

        @Override
        public void onSpeechStart(String utteranceId) {
        }

        /**
         * 播放进度回调接口，分多次回调
         *
         * @param utteranceId
         * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法保证和合成到第几个字对应。
         */
        @Override
        public void onSpeechProgressChanged(String utteranceId, int progress) {
        }

        /**
         * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
         *
         * @param utteranceId
         */
        @Override
        public void onSpeechFinish(String utteranceId) {
        }

        /**
         * 当合成或者播放过程中出错时回调此接口
         *
         * @param utteranceId
         * @param speechError 包含错误码和错误信息
         */
        @Override
        public void onError(String utteranceId, SpeechError speechError) {
        }
    };

    private String convertErrMsg(int errorCode) {
        for (n e : n.values()) {
            if (e.b() == errorCode) {
                return e.c();
            }
        }
        return n.al.c();
    }

    // 合成并播放，512字节
    private void speak() {
        if (speechSynthesizer != null) {
            speechSynthesizer.speak("该接口线程安全，可以重复调用。内部采用排队策略，调用后将自动加入队列，SDK会按照队列的顺序进行合成及播放。 注意需要合成的每个文本text不超过1024的GBK字节，即512个汉字或英文字母数字。超过请自行按照句号问号等标点切分，调用多次合成接口。");
        }
    }

    // 仅合成
    private void synthesize() {
        if (speechSynthesizer != null) {
            speechSynthesizer.speak("该接口线程安全，可以重复调用。内部采用排队策略，调用后将自动加入队列，SDK会按照队列的顺序进行合成及播放。 注意需要合成的每个文本text不超过1024的GBK字节，即512个汉字或英文字母数字。超过请自行按照句号问号等标点切分，调用多次合成接口。");
        }
    }

    // 批量合成并播放接口
    private void batchSpeak() {
        if (speechSynthesizer != null) {
            speechSynthesizer.batchSpeak(buildSpeechSynthesizeBags(Arrays.asList("该接口线程安全，可以重复调用。", "内部采用排队策略，调用后将自动加入队列，SDK会按照队列的顺序进行合成及播放。")));
        }
    }

    private List<SpeechSynthesizeBag> buildSpeechSynthesizeBags(List<String> texts) {
        List<SpeechSynthesizeBag> bags = new ArrayList<>();
        for (int i = 0; i < texts.size(); i ++) {
            SpeechSynthesizeBag bag = new SpeechSynthesizeBag();
            bag.setText(texts.get(i));
            bag.setUtteranceId(String.valueOf(i));
            bags.add(bag);
        }
        return bags;
    }
}
