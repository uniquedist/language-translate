package baidu;

import javafx.application.Application;

/**
 * 又单独弄了一个类来启动,否则起不来
 * https://www.cnblogs.com/jynszpd/p/15107813.html
 * https://blog.csdn.net/highlighters/article/details/129410172
 * @author wgx
 * @date 2024/8/7
 */
public class MainBaidu {
    public static void main(String[] args) {
        //launch(args);
        Application.launch(Main_baidu.class);
    }
}
