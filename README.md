# ZDownLoader

开发中，我们常常会需要有apk升级，或者下载某个文件的问题。所以这里就写了个通用的文件下载的功能 **ZDloader**。通过这篇文章你将看到
 - 常用框架 API 接口设计
 - 多线程下载原理与实现
 - 后台下载，界面退出之后，进来继续显示下载UI的原理

[原理请参考这篇博客](https://blog.csdn.net/u011418943/article/details/85760069)

更新如下
- 1.9 : 增加 getJson post 请求
- 1.8 : 优化数据库，处理从任务列表去除之后，重新下载文件长度为-1的问题
- 1.6 : 添加异常断电续传，方便下次继续下载；增加删除任务是否删除数据库和文件功能
- 1.4 : 完善再次进入不会更新UI问题，并添加 json 解析
- 1.3 : 基本完成功能

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
[![](https://jitpack.io/v/LillteZheng/ZDownLoader.svg)](https://jitpack.io/#LillteZheng/ZDownLoader)
```
implementation 'com.github.LillteZheng:ZDownLoader:lastest'
```
## 一、下载文件
```
//如果不是正在下载，则让它继续下载即可
if (!ZDloader.isDownloading()) {
    ZDloader.with(MainActivity.this)
            .url(URL)
            .threadCount(3)
            .reFreshTime(1000)
            .allowBackDownload(true)
            .listener(MainActivity.this)
            .download();
}else {
    //否则，则更新接口，让它可以继续显示UI
    ZDloader.updateListener(MainActivity.this);
}
```
## 二、解析 Json
通过在ZJsonListener的泛型和添加 class 就能自动解析成 Bean 了

** 这里如果填写了 params 或者 paramsMap 则自动解析成 post 请求 **
```
ZDloader.with(this)
        .jsonUrl(JSONURL)
        //.paramsMap(params)
        .params("useId","12")
        .jsonListener(new ZJsonListener<FileJson>(FileJson.class) {
            @Override
            public void fail(String errorMsg) {
                super.fail(errorMsg);
                Log.d(TAG, "zsr fail: "+errorMsg);
            }

            @Override
            public void response(FileJson data) {
                super.response(data);
                Log.d(TAG, "zsr response: "+data.toString());
            }
        }).parseJson();
```
Json 解析如下：
```
zsr response: FileJson{name='zhengsr', url='http://192.168.1.103:9090/new/兰州拉面.mp4'}
```
## 下载效果图
![下载任务](https://img-blog.csdnimg.cn/20190104091200408.gif)
