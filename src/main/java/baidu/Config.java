package baidu;

import java.util.HashMap;
import java.util.Map;

public class Config {

    // 请在此填写appid和appkey
    public static final String APP_ID = "";
    public static final String APP_KEY = "";
    //来源语言
    public static final String FROM = "en";
    //目标语言
    public static final String TO = "zh" ;

    // 需要翻译的音频文件
    public static final String AUDIO_FILE_PATH = "src/main/resources/1.wav";
    // tts音频播报保存路径
    public static final String TTS_AUDIO_SAVE_PATH = "src/main/resources/1.wav";
    // 服务地址
    public static final String WSS_ASR_URL = "ws://aip.baidubce.com/ws/realtime_speech_trans";
    // 音频采样率
    public static final int AUDIO_RATE = 16000;
    // 每帧音频数据时长: 毫秒
    public static final int AUDIO_SLICE_MS = 40;


    public static final Map<String, Object> START_FRAME = new HashMap<String, Object>() {
        {
            //固定值
            put("type", "START");
            put("from", FROM);
            put("to", TO);
            put("sampling_rate", 16000);
            put("app_id", APP_ID);
            put("app_key", APP_KEY);
            //选填，是否返回翻译结果的TTS播报结果，默认false
            put("return_target_tts", false);
            //选填，tts播报人声选项，当前目标语言是英语时支持配置
            put("tts_speaker", "man");
        }
    };

    public static final Map<String, Object> FINISH_FRAME = new HashMap<String, Object>() {
        {
            put("type", "FINISH");
        }
    };

}
