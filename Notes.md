# EasyUpdate

支持 GitHub  HTTPS ?

小米通知适配

合成新包在子线程？

targetN 测试

运行时权限原理



简单易用的 App 更新库，支持增量更新

- 简单易用，一共只向外提供了 6 个方法
- 默认 3 条线程下载，支持断点续传
- 更新时支持暂停、恢复、取消
- 绑定前台服务，通知栏随下载状态实时变化
- 下载完成后再次点击下载，如果 apk 可用直接安装
- 通知栏支持点击事件，下载时点击暂停，暂停时点击恢复下载，下载失败点击重试
- 支持 HTTPS
- 下载完成后自动安装，适配了 Android 7.0 FileProvider
- 支持增量更新

注意：

- 这不是一个下载框架，请不要用它来做其他的下载操作
- 目前不支持定制通知栏
- Android 6.0 后读写存储卡需要运行时权限，本库不做权限申请，仅在没有权限时给出 Toast 提示
- 增量升级需要自己事先准备好 patch 包，方法见：https://juejin.im/entry/580f83e067f3560057cb6c1a
- 增量升级的 so 包保留了 3 个：arm64-v8a、armeabi、armeabi-v7a，一共 269 KB
- 下载的文件后缀名 .apk 直接安装，是 .patch 则合成新包安装，其他后缀名什么也不做



## 使用

#### 权限

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

#### 代码

```java
// 初始化，仅仅缓存下 context 和通知栏图标，不耗时
EasyUpdate.init(application, R.mipmap.ic_beach);   

// 设置下载进度回调
EasyUpdate.instance().setProgressCallback(new OnProgressCallback() {
  	@Override public void onProgress(int progress) {
    	// progress 是 [0,100] 之间的整数
  	}
});

EasyUpdate.instance().start(downUrl,savePath);  // 开始下载、恢复下载也是调用它
EasyUpdate.instance().pause();                  // 暂停下载
EasyUpdate.instance().cancel();                 // 取消下载

EasyUpdate.instance().destory();                // 解绑服务
```



## Thanks

- [LessCode-Update](https://github.com/openproject/LessCode-Update)
- [BsDiff_And_Patch](https://github.com/hongyangAndroid/BsDiff_And_Patch)



## License

```
Copyright 2016 ssyijiu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```



