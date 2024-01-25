package baidu;

import baidu.util.FileUtil;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/*
* 原本的百度代码示例
* */
class RtAsrClient1 extends WebSocketClient {
    private List<Byte> receiveBinaryData;

    public RtAsrClient1(URI serverUri) {
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

public class Demo_real {

    public static void main(String[] args) throws Exception {
        RtAsrClient1 client = new RtAsrClient1(new URI(Config.WSS_ASR_URL));

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
    private static void sendStartFrame(RtAsrClient1 client) {
        client.send(JSONObject.toJSONString(Config.START_FRAME));
    }

    /**
     * 发送结束帧
     * @param client
     */
    private static void sendFinishFrame(RtAsrClient1 client) {
        client.send(JSONObject.toJSONString(Config.FINISH_FRAME));
    }

    /**
     * 发送音频数据帧
     * @param client
     */
    private static void sendAudioData(RtAsrClient1 client) {
        try {
            // 读取
            byte[] bytes = FileUtil.readFromByteFile(Config.AUDIO_FILE_PATH);
            System.out.println(bytes.length);
            // 分割
            byte[][] chunks = sliceByteData(bytes);
            // 按块发送音频数据帧
            for (byte[] chunk : chunks) {
                client.send(chunk);
                Thread.sleep(Config.AUDIO_SLICE_MS / 1000);
            }
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
