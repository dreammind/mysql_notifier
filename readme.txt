Thriftを活用したMySQLのテーブル更新通知ツール

1. はじめに
  MySQLで、あるテーブルに行がINSERTやUPDATEされた場合に通知がおこなわ
  れるサンプルを作りました。通知は、C++, Java, Python,  PHP, Ruby,
  Erlang, Perl, Haskell, C#, Cocoa, Smalltalk, and OCamlなどの多言語で
  動作するthriftを利用しました。


  動作の仕組みは次のとおりです。
    1)テーブルの更新があれば通知してくれるようにsubscriber.rbが
      publisher.rbに事前にサブスクライブします。
    2)MySQLのトリガを使ってテーブルに対するINSERTやUPDATEを監視します。
      INSERTやUPDATEがおこなわれると、event_queuesテーブルにその情報が
      記録されます。
    3)event_queuesテーブルを監視しているpubliser.rbがイベントを検知し
      事前に登録しているsubscriber.rbに「通知」をおこないます。

  サンプルでは
    ・usersテーブルはユーザの情報を持ちます。
    ・user_attributesテーブルは、ユーザの属性情報を持ちます。
      多くの属性を持てるようにusersテーブルと分けて管理しています。
    ・event_queuesテーブルは、user_attributesテーブルにINSERTやUPDATE
      が発生すると、MySQLのトリガでそのイベント情報を格納します。

1.1 準備
  1)thriftのrubyバインディングを使えるようにruby本体とrspecを事前にイ
    ンストールしてください。
    例) $ gem install rspec -r 
  2)active_recordをインストールしてください。railsをインストールするの
    がお手軽だと思います。
    例) $ gem instal rails -r 
  3)Facebookが開発したメッセージングライブラリの「thrift」をインストー
    ルしてください。
    http://incubator.apache.org/thrift/

2．使い方
  1) MySQLのテーブルやトリガを生成
 
    $ mysql -u foo -p test_db < init_db.sql

  2) パブリッシャーを起動

    $ ./rb/publisher.rb

  3) サブスクライバを起動

    $ ./rb/subscriber.rb

  4) user_attribuesテーブルにデータをINSERTするサンプルを
    起動

    $ ./rb/add_attribute.rb

  5) テーブルにデータがINSERT/UPDATEされたことで、サブスクライバが
     notifyを送信できれば、動作は正常です。

以上。
