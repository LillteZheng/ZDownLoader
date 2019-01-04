# ZDownLoader

开发中，我们常常会需要有apk升级，或者下载某个文件的问题。所以这里就写了个通用的文件下载的功能 **ZDloader**。通过这篇文章你将看到
 - 常用框架 API 接口设计
 - 多线程下载原理与实现
 - 后台下载，界面退出之后，进来继续显示下载UI的原理

[原理请参考这篇博客](https://blog.csdn.net/u011418943/article/details/85760069)

## 配置
```
allprojects {
    repositories {
    ...
    maven { url 'https://jitpack.io' }
    }
}
```
然后把 ZDloader 写上：

```
implementation 'com.github.LillteZheng:ZDownLoader:1.3'
```
ZDloader 的下载配置非常简单：
```
//如果不是正在下载，则让它继续下载即可
if (!ZDloader.isDownloading()) {
    ZDloader.with(MainActivity.this)
            .url(URL)
            //路径不写默认在Environment.getExternalStorageDirectory().getAbsolutePath()/ZDloader
            .savePath(Environment.getExternalStorageDirectory().getAbsolutePath())
            .fileName("test.apk")  //文件名不写，则默认以链接的后缀名当名字
            .threadCount(3)  //线程个数
            .reFreshTime(1000) //刷新时间，不低于200ms
            .allowBackDownload(true) //是否允许后台下载
            .listener(MainActivity.this)
            .download();
}
```
## 效果图
![下载任务](https://img-blog.csdnimg.cn/20190104091200408.gif)