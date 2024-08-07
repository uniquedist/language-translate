# 实时语音翻译，桌面字幕
效果非常一般，想以此看电影的同学可以放弃了，也许语音转文字还行，但是机器翻译就完蛋了，
毕竟机器翻译大家也用过，翻译十分生硬，难以理解，能找字幕还是找字幕吧  
开发流程可以简单参考下这个（自己找api的记录，想折腾试试的可以参考下）

【金山文档】 语音翻译
https://kdocs.cn/l/csMx5JS1l9yq
## potplayer
一开始是想弄这个的，结果发现它的翻译是基于外挂字幕的，所以不得以有了下面两个
## ~~阿里云~~
**最近拉了下代码,发现跑不起来了,似乎是阿里的jar包变化了,懒得修改了,有空研究研究通义听悟**

参考该仓库： 
https://github.com/yi-ge/desktop-subtitle  
在其上添加了翻译功能,javafx说实话是不会用的。。。  
运行就是main,java  
设置上这三个参数就可以用了，在Task.java中
```java
private static final String AccessKeyID = "";
private static final String AccessKeySecret = "";
private static final String APPKEY = "";

//原本语言
private static final String SourceLanguage ="ja";
//目标语言
private static final String TargetLanguage ="zh";
```
## 百度云
也是参考官方的api弄的，复制粘贴弄弄就差不多了，运行就是main_baidu.java
在Config.java中设置上这两参数即可
```java
public static final String APP_ID = "";
public static final String APP_KEY = "";

//来源语言
public static final String FROM = "en";
//目标语言
public static final String TO = "zh" ;

```
# 后记
最近拉下来想看看,结果跑不起来了,研究一通是因为javafx,发现以前可能用的是Oraclejdk8,现在用的是openjdk8,
导致这种结果,后来一通百度
两种方式:
* 一是用Oraclejdk
* 二是用openjdk,但是要使用jdk11之后的版本,只有11之后才有对应的mavenjar包,现在用的是jdk17