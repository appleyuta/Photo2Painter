# Photo2Painter
これは画風変換を行うAndroidアプリです。

## 画風変換とは
画風変換は写真を有名画家の絵の雰囲気に変換する技術です。  
このアプリでは深層学習の技術を用いており、画家の作風を学習させて変換ネットワークを構築しています。

## 開発方法
画風変換ネットワークの学習にKerasを用いた。
また、Androidでネットワークを実行するためにKerasのモデルをTensorflowの.pb形式に変換した。
Androidでのネットワーク実行にTensorflowInferenceInterfaceを用いた。

## 実行例
実行した結果の一例を以下に示す。
![result](http://github.com/appleyuta/Photo2Painter/blob/master/exec_screen.png)

## 公開
このアプリは[Google Play Store](https://play.google.com/store/apps/details?id=com.yutakobayashi.artisticconverter&hl=ja)で公開されています。