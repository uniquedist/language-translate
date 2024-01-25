package baidu.util;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileUtil {

    public static byte[] readFromByteFile(String pathname) throws IOException {
        File filename = new File(pathname);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] temp = new byte[1024];
        int size = 0;
        while ((size = in.read(temp)) != -1){
            out.write(temp, 0, size);
        }
        in.close();
        byte[] content = out.toByteArray();
        return content;
    }

    /**
     * 保存tts 返回的audio数据
     * @param audioBinaryData
     */
    public static void saveTtsAudio(List<Byte> audioBinaryData, String savePath) {
        if (audioBinaryData.size() <= 0) {
            return;
        }

        try {
            Path path = Paths.get(savePath);
            byte[] bytes = new byte[audioBinaryData.size()];
            for (int i = 0; i < audioBinaryData.size(); i++) {
                bytes[i] = audioBinaryData.get(i);
            }
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
