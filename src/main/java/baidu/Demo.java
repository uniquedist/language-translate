package baidu;

import baidu.util.FileUtil;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/*
* 接入javafx显示在屏幕上
* */
class RtAsrClient extends WebSocketClient {
    Label label;
    private List<Byte> receiveBinaryData;

    public RtAsrClient(URI serverUri,Label label) {
        super(serverUri);
        receiveBinaryData = new ArrayList<>();
        this.label = label;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        //对于返回的json数据处理
        if(jsonObject.containsKey("data")&&jsonObject.getJSONObject("data").containsKey("result")){
            JSONObject result = jsonObject.getJSONObject("data").getJSONObject("result");
            //type的fin代表完整的一句话
            if(result.containsKey("type") && "FIN".equals(result.getString("type"))){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        label.setText(result.getString("sentence_trans"));
                    }
                });
            }
        }

    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        byte[] temp = bytes.array();
        for (int i = 1; i < temp.length; i++) {
            receiveBinaryData.add(temp[i]);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // 如果返回tts音频结果, 保存数据
        FileUtil.saveTtsAudio(receiveBinaryData, Config.TTS_AUDIO_SAVE_PATH);

        System.out.println("Disconnected");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}

public class Demo implements Runnable{
    Label label;

    public Demo(Label label) {
        this.label = label;
    }
    @Override
    public void run() {
        RtAsrClient client = null;
        // 连接
        try {
            client = new RtAsrClient(new URI(Config.WSS_ASR_URL),label);
            client.connectBlocking();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 交互
        sendStartFrame(client);
        sendAudioData(client);
        sendFinishFrame(client);
    }

    /**
     * 发送开始帧
     * @param client
     */
    private static void sendStartFrame(RtAsrClient client) {
        client.send(JSONObject.toJSONString(Config.START_FRAME));
    }

    /**
     * 发送结束帧
     * @param client
     */
    private static void sendFinishFrame(RtAsrClient client) {
        client.send(JSONObject.toJSONString(Config.FINISH_FRAME));
    }

    /**
     * 发送音频数据帧
     * @param client
     */
    private void sendAudioData(RtAsrClient client)  {
        TargetDataLine targetDataLine = null;
        try {
            //读取麦克风数据
            AudioFormat audioFormat = new AudioFormat(16000.0F, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
             targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            int nByte = 0;
            final int bufSize = 1280;
            byte[] bytes = new byte[bufSize];
            while ((nByte = targetDataLine.read(bytes, 0, bufSize)) > 0) {
                // Step4 直接发送麦克风数据流
                // 分割 是因为读取文件太大，所以要分割，直接参照官网示例，一次只传送1280bytes
                //byte[][] chunks = sliceByteData(bytes);
                // 按块发送音频数据帧
                client.send(bytes);
                Thread.sleep(Config.AUDIO_SLICE_MS / 1000);
            }
            System.out.println("停止了");
        } catch (Exception e) {
            e.printStackTrace();
            //必须要关掉，否则重连报错：line with format PCM_SIGNED 16000.0 Hz, 16 bit, mono, 2 bytes/frame, little-endian not supported.
            targetDataLine.close();
            //重新连接
            Demo  task = new Demo(label);
            try{
                Thread.sleep(2000);
            }
            catch (Exception m) {}
            new Thread(task).start();
        }
    }



}
