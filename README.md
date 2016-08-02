远程数据同步（源码地址）: https://github.com/xxonehjh/remote-files-sync
发布包下载地址：https://github.com/xxonehjh/remote-files-sync/tree/master/publish

####一.功能特性
支持同步本地文件
支持同步远程文件
支持代理同步
提供Android app作为服务端
数据传输采用thrift框架

####二.使用说明
#####服务端启动 
java -jar remote_sync.jar server config.properties
#####客户端启动 
java -jar remote_sync.jar client config.properties

注：
remote_sync.jar 下载地址：https://github.com/xxonehjh/remote-files-sync/raw/master/publish/remote_sync.1.0.jar
config.properties 为配置参数

####三.配置说明
````properties

#################server

#服务类型: simple(简单类型) nio(java nio) mult_thread(多线程)
server.type=mult_thread
#服务端口
server.port=9987
#提供的服务数据【可以是本地文件目录，也可以是其他服务地址（以此实现代理，可用于内网）】
server.folder.images=D:/hjh/test_sync/server/images
server.folder.datas=from:127.0.0.1:9987/files
#服务端 安全证书
server.keystore=D:/hjh/test_sync/.keystore@thrift

#################client
#同步时间间隔（单位：ms）
client.sync.interval=20000
#每次同步数据块大小（单位：byte）
client.block.size=524288

#客户端是否保留服务端已经删除的文件
client.copy.remove=true
#是否同步文件修改时间
client.copy.time=true

#客户端同步类 simple（简单） cache（提供缓存，直接断点续传）
client.copy.type=simple

#客户端保存文件目录
client.store=D:/hjh/test_sync/client
#客户端工作空间，用于保存缓存等（可不设置）
client.workspace=D:/hjh/test_sync/client
#客户端 安全证书
client.truststore=D:/hjh/test_sync/.truststore@thrift

#客户端同步目录
client.folder.datas=from:127.0.0.1:9987/datas
client.folder.local=D:/hjh/test_sync/server/local

####################config
#文件在此差异时间内，判断其相等（单位：ms）【因为不同平台时间精度可能不同】
config.min.diff.time=1000
#socket超时配置（单位：ms）
config.timeout=300000
````

#####简单客户端配置样例
````properties
client.folder.sync_folder=from:ip:端口/服务目录
client.store=保存文件路径
````

#####简单服务端配置样例
````properties
server.type=simple
server.port=9958
server.folder.sync_folder=本地文件路径
````

####三.安全性
服务端和客户端分别配置安全证书【路径格式： key路径@密码】，配置了之后，数据会采用加密传输
安全证书生成方式：http://www.code234.com/b5d5c7e0e44f0c4b42720bd1bf5cd4ce_detail.html
````properties
server.keystore=D:/hjh/test_sync/.keystore@thrift
client.truststore=D:/hjh/test_sync/.truststore@thrift
````
