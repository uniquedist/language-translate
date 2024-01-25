package aliyun;

import aliyun.util.Sender;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.aliyuncs.exceptions.ClientException;
import javafx.application.Platform;
import javafx.scene.control.Label;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.HashMap;
import java.util.Map;

class Task implements Runnable {
    Label label;

    private static final String AccessKeyID = "";
    private static final String AccessKeySecret = "";
    private static final String APPKEY = "";

    //原本语言
    private static final String SourceLanguage ="ja";
    //目标语言
    private static final String TargetLanguage ="zh";
    //翻译接口
    private static final String serviceURL = "http://mt.cn-hangzhou.aliyuncs.com/api/translate/web/ecommerce";

    NlsClient client;

    //获取动态的token
    public String getToken() throws ClientException {
        AccessToken apply = AccessToken.apply(AccessKeyID, AccessKeySecret);
        return apply.getToken();
    }

    public Task(Label label) {
        this.label = label;
    }

    public SpeechTranscriberListener getTranscriberListener() {
        SpeechTranscriberListener listener = new SpeechTranscriberListener() {
            // 识别出中间结果.服务端识别出一个字或词时会返回此消息.仅当setEnableIntermediateResult(true)时,才会有此类消息返回
            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                System.out.println("name: " + response.getName() +
                        // 状态码 20000000 表示正常识别
                        ", status: " + response.getStatus() +
                        // 句子编号，从1开始递增
                        ", index: " + response.getTransSentenceIndex() +
                        // 当前句子的中间识别结果
                        ", result: " + response.getTransSentenceText() +
                        // 当前已处理的音频时长，单位是毫秒
                        ", time: " + response.getTransSentenceTime());

                final String r = response.getTransSentenceText();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        // Update UI here.
                        label.setText(r);
                    }
                });
            }

            // 识别出一句话.服务端会智能断句,当识别到一句话结束时会返回此消息
            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                System.out.println("name: " + response.getName() +
                        // 状态码 20000000 表示正常识别
                        ", status: " + response.getStatus() +
                        // 句子编号，从1开始递增
                        ", index: " + response.getTransSentenceIndex() +
                        // 当前句子的完整识别结果
                        ", result: " + response.getTransSentenceText() +
                        // 当前已处理的音频时长，单位是毫秒
                        ", time: " + response.getTransSentenceTime() +
                        // SentenceBegin事件的时间，单位是毫秒
                        ", begin time: " + response.getSentenceBeginTime() +
                        // 识别结果置信度，取值范围[0.0, 1.0]，值越大表示置信度越高
                        ", confidence: " + response.getConfidence());

                final String r = response.getTransSentenceText();
                System.out.println(r);

                Map<String,String> map = new HashMap<>();
                //翻译文本的格式，html（ 网页格式。设置此参数将对待翻译文本以及翻译后文本按照html格式进行处理）、
                // text（文本格式。设置此参数将对传入待翻译文本以及翻译后结果不做文本格式处理，统一按纯文本格式处理。
                map.put("FormatType","text");
                map.put("SourceLanguage",SourceLanguage);
                map.put("TargetLanguage",TargetLanguage);
                map.put("SourceText",r);
                map.put("Scene","title");
                String postBody = JSON.toJSONString(map);
                // Sender代码请参考帮助文档“签名方法”
                String result =  Sender.sendPost(serviceURL, postBody, AccessKeyID, AccessKeySecret);
                System.out.println(result);
                String tranString = JSONObject.parseObject(result).getJSONObject("Data").getString("Translated");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(tranString);
                        // Update UI here.
                        label.setText(tranString);
                    }
                });
            }

            // 识别完毕
            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                System.out.println("name: " + response.getName() +
                        ", status: " + response.getStatus());
            }
        };
        return listener;
    }

    public void process() {
        SpeechTranscriber transcriber = null;
        try {
            // Step1 创建实例,建立连接
            transcriber = new SpeechTranscriber(client, getTranscriberListener());
            transcriber.setAppKey(APPKEY);
            // 输入音频编码方式
            transcriber.setFormat(InputFormatEnum.PCM);
            // 输入音频采样率
            transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
            // 是否返回中间识别结果
            //transcriber.setEnableIntermediateResult(true);
            // 是否生成并返回标点符号
            transcriber.setEnablePunctuation(true);
            // 是否将返回结果规整化,比如将一百返回为100
            transcriber.setEnableITN(false);

            // Step2 此方法将以上参数设置序列化为json发送给服务端,并等待服务端确认
            transcriber.start();

            // Step3 读取麦克风数据
            AudioFormat audioFormat = new AudioFormat(16000.0F, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    // Update UI here.
                    label.setText("You can speak now!");
                }
            });
//            label.setText("You can speak now!");
            int nByte = 0;
            final int bufSize = 6400;
            byte[] buffer = new byte[bufSize];
            while ((nByte = targetDataLine.read(buffer, 0, bufSize)) > 0) {
                // Step4 直接发送麦克风数据流
                transcriber.send(buffer);
            }

            // Step5 通知服务端语音数据发送完毕,等待服务端处理完成
            transcriber.stop();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            // Step6 关闭连接
            if (null != transcriber) {
                transcriber.close();
            }
        }
    }

    @Override
    public void run() {
        // Step0 创建NlsClient实例,应用全局创建一个即可,默认服务地址为阿里云线上服务地址
        try {
            client = new NlsClient(getToken());
        } catch (ClientException e) {
            e.printStackTrace();
        }
        this.process();
    }



}