package baidu;

import baidu.util.FileUtil;
import com.alibaba.fastjson.JSONObject;
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
* 本地测试使用
* */
class RtAsrClient2 extends WebSocketClient {
    private List<Byte> receiveBinaryData;

    public RtAsrClient2(URI serverUri) {
        super(serverUri);
        receiveBinaryData = new ArrayList<>();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
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

public class Demo2 {

    public static void main(String[] args) throws Exception {
        RtAsrClient2 client = new RtAsrClient2(new URI(Config.WSS_ASR_URL));

        // 连接
        client.connectBlocking();

        // 交互
        sendStartFrame(client);
        sendAudioData(client);
        sendFinishFrame(client);
    }

    /**
     * 发送开始帧
     * @param client
     */
    private static void sendStartFrame(RtAsrClient2 client) {
        client.send(JSONObject.toJSONString(Config.START_FRAME));
    }

    /**
     * 发送结束帧
     * @param client
     */
    private static void sendFinishFrame(RtAsrClient2 client) {
        client.send(JSONObject.toJSONString(Config.FINISH_FRAME));
    }

    /**
     * 发送音频数据帧
     * @param client
     */
    private static void sendAudioData(RtAsrClient2 client) {

        try {
            //读取麦克风数据
            AudioFormat audioFormat = new AudioFormat(16000.0F, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
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
        }
    }

    /**
     * 切分byte数据
     * @param bytes
     * @return
     */
    private static byte[][] sliceByteData(byte[] bytes) {
        int chunkLength = Config.AUDIO_RATE * 2 / 1000 * Config.AUDIO_SLICE_MS;
        int c = bytes.length / chunkLength;
        if (bytes.length % chunkLength != 0) {
            c++;
        }

        byte[][] chunks = new byte[c][chunkLength];
        for (int i = 0; i < c; i++) {
            for (int j = 0; j < chunkLength; j++) {
                int idx = i * chunkLength + j;
                if (idx >= bytes.length) {
                    return chunks;
                }

                chunks[i][j] = bytes[idx];
            }
        }

        return chunks;
    }
}
