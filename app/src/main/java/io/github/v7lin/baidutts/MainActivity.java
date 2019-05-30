package io.github.v7lin.baidutts;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.baidu.tts.f.n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    private static final String APP_ID = "16357689";
    private static final String APP_KEY = "GtDrwbB5f7Hu9oQGBRljye69";
    private static final String SECRET_KEY = "VYKyRtGERGemDmwepD1Rkyab0NcPV99o";

    SpeechSynthesizer speechSynthesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.online).setOnClickListener(onClickListener);
        findViewById(R.id.mix).setOnClickListener(onClickListener);
        findViewById(R.id.online_speaker).setOnClickListener(onClickListener);
        findViewById(R.id.mix_speaker).setOnClickListener(onClickListener);
        findViewById(R.id.play).setOnClickListener(onClickListener);
        findViewById(R.id.pause).setOnClickListener(onClickListener);
        findViewById(R.id.resume).setOnClickListener(onClickListener);
        findViewById(R.id.destroy).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.online:
                    initOnlineTts(OnlineResource.ONLINE_SPEAKER_FEMALE_DEFAULT);
                    break;
                case R.id.mix:
                    initMixTts(OnlineResource.ONLINE_SPEAKER_FEMALE_DEFAULT);
                    break;
                case R.id.online_speaker:
                    if (speechSynthesizer != null) {
                        //0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
                        int speaker = new Random().nextInt(5);
//                        destroy();
//                        initOnlineTts(speaker);
                        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(speaker)); // 设置发声的人声音，在线生效
                        speechSynthesizer.stop();
                        speak();
                    }
                    break;
                case R.id.mix_speaker:
                    try {
                        if (speechSynthesizer != null) {
                            int speaker = new Random().nextInt(5);
//                            destroy();
//                            initMixTts(speaker);
                            speechSynthesizer.stop();
                            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(speaker)); // 设置发声的人声音，在线生效
                            OfflineResource offlineResource = createOfflineResource(speaker);
                            speechSynthesizer.loadModel(offlineResource.getTextFilename(), offlineResource.getModelFilename());
                            speak();
                        }
                    } catch (IOException e) {
                        destroy();
                    }
                    break;
                case R.id.play:
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
                    destroy();
                    break;
                default:
                    break;
            }
        }
    };

    void initOnlineTts(int speaker) {
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
            AuthInfo authInfo = speechSynthesizer.auth(TtsMode.ONLINE);
            if (authInfo == null || !authInfo.isSuccess()) {
                destroy();
                return;
            }
            // 设置合成的音量，0-9 ，默认 5
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
            // 设置合成的语速，0-9 ，默认 5
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
            // 设置合成的语调，0-9 ，默认 5
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
            speechSynthesizer.setStereoVolume(1.0f, 1.0f); // 设置播放器的音量，即使用speak 播放音量时生效。范围为[0.0f-1.0f]。
            speechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            speechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(speaker)); // 设置发声的人声音，在线生效
            Map<String, String> params = new HashMap<>();
            InitConfig config = new InitConfig(APP_ID, APP_KEY, SECRET_KEY, TtsMode.ONLINE, params, speechSynthesizerListener);
            AutoCheck.getInstance(getApplicationContext()).check(config, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 100) {
                        AutoCheck autoCheck = (AutoCheck) msg.obj;
                        synchronized (autoCheck) {
                            String message = autoCheck.obtainDebugMessage();
                             Log.w("AutoCheckMessage", message);
                        }
                    }
                }
            });
            speechSynthesizer.initTts(TtsMode.ONLINE); // 初始化离在线混合模式，如果只需要在线合成功能，使用 TtsMode.ONLINE
        }
    }

    void initMixTts(int speaker) {
        try {
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
                AuthInfo authInfo = speechSynthesizer.auth(TtsMode.MIX);
                if (authInfo == null || !authInfo.isSuccess()) {
                    destroy();
                    return;
                }
                // 设置合成的音量，0-9 ，默认 5
                speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
                // 设置合成的语速，0-9 ，默认 5
                speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
                // 设置合成的语调，0-9 ，默认 5
                speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
                speechSynthesizer.setStereoVolume(1.0f, 1.0f); // 设置播放器的音量，即使用speak 播放音量时生效。范围为[0.0f-1.0f]。
                speechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                speechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);
                speechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
                OfflineResource offlineResource = createOfflineResource(speaker);
                // 文本模型文件路径 (离线引擎使用)，注意TEXT_FILENAME必须存在并且可读
                speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
                // 声学模型文件路径 (离线引擎使用)，注意TEXT_FILENAME必须存在并且可读
                speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
                speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(speaker)); // 设置发声的人声音，在线生效
                Map<String, String> params = new HashMap<>();
                params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
                params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
                InitConfig config = new InitConfig(APP_ID, APP_KEY, SECRET_KEY, TtsMode.ONLINE, null, speechSynthesizerListener);
                AutoCheck.getInstance(getApplicationContext()).check(config, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == 100) {
                            AutoCheck autoCheck = (AutoCheck) msg.obj;
                            synchronized (autoCheck) {
                                String message = autoCheck.obtainDebugMessage();
                                Log.w("AutoCheckMessage", message);
                            }
                        }
                    }
                });
                speechSynthesizer.initTts(TtsMode.MIX); // 初始化离在线混合模式，如果只需要在线合成功能，使用 TtsMode.ONLINE
            }
        } catch (IOException e) {
            destroy();
        }
    }

    private OfflineResource createOfflineResource(int speaker) throws IOException {
        OfflineResource offlineResource;
        switch (speaker) {
            case OnlineResource.ONLINE_SPEAKER_DUYY:
                offlineResource = new OfflineResource(getApplicationContext(), OfflineResource.OFFLINE_SPEAKER_DUYY);
                break;
            case OnlineResource.ONLINE_SPEAKER_DUXY:
                offlineResource = new OfflineResource(getApplicationContext(), OfflineResource.OFFLINE_SPEAKER_DUXY);
                break;
            case OnlineResource.ONLINE_SPEAKER_SPMALE:
            case OnlineResource.ONLINE_SPEAKER_MALE:
                offlineResource = new OfflineResource(getApplicationContext(), OfflineResource.OFFLINE_SPEAKER_MALE);
                break;
            case OnlineResource.ONLINE_SPEAKER_FEMALE_DEFAULT:
            default:
                offlineResource = new OfflineResource(getApplicationContext(), OfflineResource.OFFLINE_SPEAKER_FEMALE);
                break;
        }
        return offlineResource;
    }

    private SpeechSynthesizerListener speechSynthesizerListener = new SpeechSynthesizerListener() {
        /**
         * 播放开始，每句播放开始都会回调
         *
         * @param utteranceId
         */
        @Override
        public void onSynthesizeStart(String utteranceId) {
            Log.e("TAG", "onSynthesizeStart: " + utteranceId);
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
            Log.e("TAG", "onSynthesizeDataArrived: " + utteranceId);
        }

        /**
         * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
         *
         * @param utteranceId
         */
        @Override
        public void onSynthesizeFinish(String utteranceId) {
            Log.e("TAG", "onSynthesizeFinish: " + utteranceId);
        }

        @Override
        public void onSpeechStart(String utteranceId) {
            Log.e("TAG", "onSpeechStart: " + utteranceId);
        }

        /**
         * 播放进度回调接口，分多次回调
         *
         * @param utteranceId
         * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法保证和合成到第几个字对应。
         */
        @Override
        public void onSpeechProgressChanged(String utteranceId, int progress) {
            Log.e("TAG", "onSpeechProgressChanged: " + utteranceId);
        }

        /**
         * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
         *
         * @param utteranceId
         */
        @Override
        public void onSpeechFinish(String utteranceId) {
            Log.e("TAG", "onSpeechFinish: " + utteranceId);
        }

        /**
         * 当合成或者播放过程中出错时回调此接口
         *
         * @param utteranceId
         * @param speechError 包含错误码和错误信息
         */
        @Override
        public void onError(String utteranceId, SpeechError speechError) {
            Log.e("TAG", "onError: " + utteranceId + speechError.toString());
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
    void speak() {
        if (speechSynthesizer != null) {
            speechSynthesizer.speak("该接口线程安全，可以重复调用。内部采用排队策略，调用后将自动加入队列，SDK会按照队列的顺序进行合成及播放。 注意需要合成的每个文本text不超过1024的GBK字节，即512个汉字或英文字母数字。超过请自行按照句号问号等标点切分，调用多次合成接口。");
        }
    }

    // 仅合成
    void synthesize() {
        if (speechSynthesizer != null) {
            speechSynthesizer.speak("该接口线程安全，可以重复调用。内部采用排队策略，调用后将自动加入队列，SDK会按照队列的顺序进行合成及播放。 注意需要合成的每个文本text不超过1024的GBK字节，即512个汉字或英文字母数字。超过请自行按照句号问号等标点切分，调用多次合成接口。");
        }
    }

    // 批量合成并播放接口
    void batchSpeak() {
        if (speechSynthesizer != null) {
            speechSynthesizer.batchSpeak(buildSpeechSynthesizeBags(Arrays.asList("该接口线程安全，可以重复调用。", "内部采用排队策略，调用后将自动加入队列，SDK会按照队列的顺序进行合成及播放。")));
        }
    }

    private List<SpeechSynthesizeBag> buildSpeechSynthesizeBags(List<String> texts) {
        List<SpeechSynthesizeBag> bags = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            SpeechSynthesizeBag bag = new SpeechSynthesizeBag();
            bag.setText(texts.get(i));
            bag.setUtteranceId(String.valueOf(i));
            bags.add(bag);
        }
        return bags;
    }

    void destroy() {
        if (speechSynthesizer != null) {
            speechSynthesizer.stop();
            speechSynthesizer.freeCustomResource();
            speechSynthesizer.release();
            speechSynthesizer = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroy();
        super.onDestroy();
    }
}
