package io.github.v7lin.baidutts;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class OfflineResource {

    public static final String OFFLINE_SPEAKER_FEMALE = "F";
    public static final String OFFLINE_SPEAKER_MALE = "M";
    public static final String OFFLINE_SPEAKER_DUYY = "Y";
    public static final String OFFLINE_SPEAKER_DUXY = "X";

    private String textFilename;
    private String modelFilename;

    private static HashMap<String, Boolean> mapInitied = new HashMap<String, Boolean>();

    public OfflineResource(Context context, String voiceType) throws IOException {
        String text = "bd_etts_text.dat";
        String model;
        if (OFFLINE_SPEAKER_MALE.equals(voiceType)) {
            model = "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
        } else if (OFFLINE_SPEAKER_FEMALE.equals(voiceType)) {
            model = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (OFFLINE_SPEAKER_DUXY.equals(voiceType)) {
            model = "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (OFFLINE_SPEAKER_DUYY.equals(voiceType)) {
            model = "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";
        } else {
            throw new RuntimeException("voice type is not in list");
        }
        textFilename = copyAssetsFile(context, text);
        modelFilename = copyAssetsFile(context, model);
    }

    public String getModelFilename() {
        return modelFilename;
    }

    public String getTextFilename() {
        return textFilename;
    }

    private String copyAssetsFile(Context context, String sourceFilename) throws IOException {
        String destFilename = new File(context.getExternalFilesDir("BaiduTTS"), sourceFilename).getAbsolutePath();
        boolean recover = false;
        Boolean existed = mapInitied.get(sourceFilename); // 启动时完全覆盖一次
        if (existed == null || !existed) {
            recover = true;
        }
        FileUtil.copyFromAssets(context.getAssets(), sourceFilename, destFilename, recover);
        Log.i(TAG, "文件复制成功：" + destFilename);
        return destFilename;
    }
}
