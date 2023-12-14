> 我在集成flutter_boost的时候,发现使用gradle8.0的时候,项目会报一些异常,目前还没有结果.可能升级到8.0的时候还要适配一下,不过我看Flutter生成model的时候也是7.5.所以这里我也用的是7.5.目前还好.

### 1. flutter侧需要做的处理

1. 关于集成

   ```yaml
   flutter_boost:
     git:
       url: 'https://github.com/alibaba/flutter_boost.git'
       ref: '4.4.0'
   ```

   

2. 其实关于这里.我理解的就是使用FlutterBoostApp来替换之前的MaterialApp,至于其中用到的参数,都是按照文档上写的.

```dart
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_boost/flutter_boost.dart';
import 'package:flutter_boost_module/simple_page.dart';

import 'main_page.dart';

void main() {
  CustomFlutterBinding();
  runApp(const MyApp());
}

class CustomFlutterBinding extends WidgetsFlutterBinding with BoostFlutterBinding {}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  /// 由于很多同学说没有跳转动画，这里是因为之前exmaple里面用的是 [PageRouteBuilder]，
  /// 其实这里是可以自定义的，和Boost没太多关系，比如我想用类似iOS平台的动画，
  /// 那么只需要像下面这样写成 [CupertinoPageRoute] 即可
  /// (这里全写成[MaterialPageRoute]也行，这里只不过用[CupertinoPageRoute]举例子)
  ///
  /// 注意，如果需要push的时候，两个页面都需要动的话，
  /// （就是像iOS native那样，在push的时候，前面一个页面也会向左推一段距离）
  /// 那么前后两个页面都必须是遵循CupertinoRouteTransitionMixin的路由
  /// 简单来说，就两个页面都是CupertinoPageRoute就好
  /// 如果用MaterialPageRoute的话同理

  Map<String, FlutterBoostRouteFactory> routerMap = {
    'mainPage': (settings, uniqueId) {
      print("步骤1");
      print("传递过来的参数 ${settings.arguments} $uniqueId");

      return CupertinoPageRoute(
          settings: settings,
          builder: (_) {
            // Map<String, Object> map = settings.arguments as Map<String, Object>;
            // String data = map['data'] as String;
            print("步骤2");
            return const MainPage();
          });
    },
    'simplePage': (settings, uniqueId) {
      return CupertinoPageRoute(
          settings: settings,
          builder: (_) {
            Map<String, dynamic>? map = settings.arguments as Map<String, dynamic>;
            String? data = map['data'];
            return SimplePage(
              data: data,
            );
          });
    },
  };

  Route<dynamic>? routeFactory(RouteSettings settings, String? uniqueId) {
    FlutterBoostRouteFactory? func = routerMap[settings.name!];
    if (func == null) {
      return null;
    }
    return func(settings, uniqueId);
  }

  Widget appBuilder(Widget home) {
    return MaterialApp(
      home: home,
      debugShowCheckedModeBanner: true,

      ///必须加上builder参数，否则showDialog等会出问题
      builder: (_, __) {
        return home;
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return FlutterBoostApp(
      routeFactory,
      appBuilder: appBuilder,
    );
  }
```



关于代码这里做下解释:

* 首先FlutterBoostApp需要两个参数,这两个参数分别是

  * routeFactory  这个是创建routeFactory的工厂方法,其实就是提供创建routeFactory的创建方法.
* appBuilder  这个返回的是一个Widget对象,这个对象就是上面的方法
  * initialRoute  这个是初始的router,应该是默认显示的内容.


2. 相关的跳转的方法

```dart
// 返回上一级页面
BoostNavigator.instance.pop();
// 跳转到指定页面
BoostNavigator.instance.push("simplePage");
```

### 2. Android侧处理

> 还是像上面说的我没有使用gradle8.0 而是使用gradle7.5 而且AGP使用的版本是7.3.0,这样就和flutter创建的module的版本一直,编译就没有问题了.

1. 原生项目关联flutter的module

```groovy
// 这里的flutter_boost_module是flutter模块的名称
setBinding(new Binding([gradle: this]))
evaluate(new File(
        settingsDir.parentFile,
        'flutter_boost_module/.android/include_flutter.groovy'
))
include ':flutter_boost_module'
project(':flutter_boost_module').projectDir = new File('../flutter_boost_module')
```



如果编译的时候报错:

> Caused by: org.gradle.api.InvalidUserCodeException: Build was configured to prefer settings repositories over project repositories but repository ‘maven’ was added by plugin class ‘FlutterPlugin’
> Caused by: org.gradle.api.internal.plugins.PluginApplicationException: Failed to apply plugin class ‘FlutterPlugin’.

需要修改

```groovy
// 需要将RepositoriesMode.FAIL_ON_PROJECT_REPOS改为RepositoriesMode.PREFER_PROJECT
// 这样处理就能解决上面的编译问题
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```



2. 在app的build.gradle下引入相关内容

> 但是这种引入方式的话,可以确定一下是否可以通过aar的方式引用了.

```groovy
// 很明显这个是个flutter和flutter_boost的引入
implementation project(':flutter')
implementation project(':flutter_boost')
```

3. 在AndroidManifest.xml中引入FlutterBootsActivity

```xml
  <activity
     android:name="com.idlefish.flutterboost.containers.FlutterBoostActivity"
     android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|layoutDirection|fontScale|screenLayout|density"
     android:hardwareAccelerated="true"
     android:theme="@style/Theme.AppCompat"
     android:windowSoftInputMode="adjustResize" />

  <meta-data
     android:name="flutterEmbedding"
     android:value="2" />
```



4. 在`Application`中添加`FlutterBoost`的启动流程，并设置代理

```kotlin
FlutterBoost.instance().setup(this, object : FlutterBoostDelegate {
    override fun pushNativeRoute(options: FlutterBoostRouteOptions?) {
        Log.e("TAG======>", "pushNativeRoute: ${options.toString()}")
        if (TextUtils.equals(options?.pageName(), "second")) {
            //这里根据options.pageName来判断你想跳转哪个页面，这里简单给一个
            val intent = Intent(
                FlutterBoost.instance().currentActivity(),
                SecondActivity::class.java
            )
            FlutterBoost.instance().currentActivity()
                .startActivityForResult(intent, options!!.requestCode())
        }

    }

    override fun pushFlutterRoute(options: FlutterBoostRouteOptions?) {
        Log.e("TAG======>", "pushFlutterRoute: ${options.toString()}")
        val intent =
            FlutterBoostActivity.CachedEngineIntentBuilder(FlutterBoostActivity::class.java)
                .backgroundMode(FlutterActivityLaunchConfigs.BackgroundMode.transparent)
                .destroyEngineWithActivity(false)
                .uniqueId(options!!.uniqueId())
                .url(options.pageName())
                .urlParams(options.arguments())
                .build(FlutterBoost.instance().currentActivity())
        FlutterBoost.instance().currentActivity().startActivity(intent)
    }
}) {
    Log.e("TAG======>", "onCreate: ")
}
```

其实我觉得这个方法只要在Flutter页面跳转的前面初始化就好, 这里应该算是一个全局的代理,其实原生侧的跳转方法都会通过代理走到这里.Flutter侧的跳转方法也会走到这里.

关于跳转:

```kotlin
val options = FlutterBoostRouteOptions.Builder()
    .pageName("mainPage")
    .arguments(hashMapOf())
    .build()
FlutterBoost.instance().open(options)
```

