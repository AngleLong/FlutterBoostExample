import 'package:flutter/material.dart';
import 'package:flutter_boost/flutter_boost.dart';

class SimplePage extends StatefulWidget {
  final Object? data;

  const SimplePage({super.key, required this.data});

  @override
  State<SimplePage> createState() => _SimplePageState();
}

class _SimplePageState extends State<SimplePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
            onPressed: () {
              BoostNavigator.instance.pop();
            },
            icon: const Icon(Icons.arrow_back_ios_new)),
        title: const Text("商品详情"),
      ),
      body: GestureDetector(
        child: const Center(
            child: Text(
                '寒蝉凄切，对长亭晚，骤雨初歇。都门帐饮无绪，留恋处，兰舟催发。执手相看泪眼，竟无语凝噎。')),
        onTap: () {
          BoostNavigator.instance.push("second");
        },
      ),
    );
  }
}
