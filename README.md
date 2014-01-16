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

文档
--------------
https://github.com/qii/weiciyuan/wiki

Gradle 构建
--------------
- 版本
    - 最新 Android SDK
    - Gradle 1.8
- 环境变量
    - ANDROID_HOME
    - GRADLE_HOME，同时把bin放入path变量
- Android SDK 安装
    - Android SDK Build-tools 19+
    - Google Repository 4+
    - Google Play services 13+
    - Android Support Repository 3+
    - Android Support Library 19+
- 移除配置
    - 修改AndroidManifest.xml里面`com.google.android.maps.v2.API_KEY`为你的Google Map key
    - 移除AndroidManifest.xml里面`com.crashlytics.ApiKey`和GlobalContext的`Crashlytics.start(this)`，以免影响四次元的崩溃统计数据
- 编译
    - `gradle build`，编译好的apk在build/apk下面，没签名，需要签名的修改build.gradle

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
