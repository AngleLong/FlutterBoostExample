import 'package:flutter/material.dart';
import 'package:flutter_boost/flutter_boost.dart';

class MainPage extends StatefulWidget {
  const MainPage({super.key});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
            onPressed: () {
              BoostNavigator.instance.pop();
            },
            icon: const Icon(Icons.arrow_back_ios_new)),
        title: const Text("商品列表"),
      ),
      body: Container(
        color: Colors.grey.withOpacity(0.1),
        width: double.infinity,
        height: double.infinity,
        child: Center(
          child: InkWell(
            child: const Text("显示内容"),
            onTap: () {
              BoostNavigator.instance.push("simplePage");
            },
          ),
        ),
      ),
    );
  }
}
