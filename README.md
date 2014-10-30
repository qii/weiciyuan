四次元（原微次元） weiciyuan
=========
Sina Weibo Android App, require Android 4.1+, GPL v3 License

<a href="https://play.google.com/store/apps/details?id=org.qii.weiciyuan">
  <img alt="Android app on Google Play"
       src="https://developer.android.com/images/brand/en_app_rgb_wo_45.png" />
</a>

<a href="https://play.google.com/store/apps/details?id=org.qii.weiciyuan">
  <img alt="Google Play"  width="200" height="200"
       src="https://raw.github.com/qii/weiciyuan/slidingmenu/qrcode.png" />
</a>

截图
--------------
<img width="30%" height="30%" src="https://lh5.ggpht.com/liao4yraseucSncbq9ZOAspCb7xZZ-E7iHsSv3OBGbFwLi6pSys8G4jap132pUmuYQ=h900-rw"/>

<img width="30%" height="30%" src="https://lh5.ggpht.com/hlf2Hy7nyvGZ2l6WV3LEd2IiXVp_xYh76_bPUSEaQf0epRwxx3XA-7dAFjQBiZy7Tw=h900-rw"/>

文档
--------------
https://github.com/qii/weiciyuan/wiki

Gradle 构建
--------------
- 版本
    - 最新 Android SDK
    - Gradle
- 环境变量
    - ANDROID_HOME
    - GRADLE_HOME，同时把bin放入path变量
- Android SDK 安装，都更新到最新
    - Android SDK Build-tools
    - Google Repository
    - Android Support Repository
    - Android Support Library
- 移除配置
    - 移除AndroidManifest.xml里面`com.crashlytics.ApiKey`和GlobalContext的`Crashlytics.start(this)`，以免影响四次元的崩溃统计数据
- 编译
    - `./gradlew assembleDebug`，编译好的apk在build/outputs/apk下面，默认用的是 debug.keystore 签名，可与Google Play上的正式版共存

黄粱一梦二十年
--------------
黄粱一梦二十年

依旧是不懂爱也不懂情

写歌的人假正经阿

听歌的人最无情

于是歌手从吉林到北京

从台北到上海

伦敦到马德里

去寻找他梦中的青鸟

郎对花 姐对花 是一段不知道是怎么开始

也不知道要怎么样结束的旅程 一对对到人间

他发觉…这世间…有点假 这个人间有点假

可我莫名的 爱上了她… 可我莫名爱上了她

莫非再过二十年

依旧是不懂爱也不懂情

写歌的人断了魂阿

听歌的人最无情
