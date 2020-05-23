# 『Kubernetes on AWS～アプリケーションエンジニア　本番環境へ備える』コンテンツ配布用リポジトリ
『Kubernetes on AWS～アプリケーションエンジニア　本番環境へ備える』（リックテレコム刊　2020年）内で使用しているファイルの配布用リポジトリです。

## db-docker-composeフォルダについて

db-docker-composeフォルダには、開発端末上でアプリケーションのテストを行うために使う
docker-compose用設定ファイルとDBユーザ、データベースの作成用スクリプトを配置しています。

これらは書籍上は使用しませんが、開発端末でアプリケーションを動作させてみる場合にご利用ください。

使用方法は以下のとおりです。db-docker-comopseフォルダに移動して実行してください。

### DB起動

```
$ docker-compose up -d
```

### DB停止

```
$ docker-compose down
```

### DBユーザ作成

```
$ ./createuser.sh
```

### DB作成

```
$ ./createdb.sh
```
