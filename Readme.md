#  Consistent Hashing based Key-Value Memory Storage  #

基于一致性哈希的分布式内存键值存储——CHKV。


## 系统设计 ##

- **NameNode** : 维护key与节点的映射关系（Hash环），用心跳检测DataNode（一般被动，被动失效时主动询问三次），节点增减等系统信息变化时调整数据并通知Client；
- **DataNode** : 存储具体的数据，向NameNode主动发起心跳并采用请求响应的方式来实现上下线，便于NameNode挪动数据
- **Client** : 负责向NameNode请求DataNode数据和Hash算法等系统信息并监听其变化，操纵数据时直接向对应DataNode发起请求就行，暂时只包含set,get,delete三个操作
****
NameNode失效则整个系统不可用

若当成内存数据库使用则 只要有一个 DataNode 失效（未经请求与数据转移就下线了）整个系统就不可对外服务；
若当成内存缓存使用则 DataNode 失效只是失去了一部分缓存，系统仍然可用。
客户要使用**CHKV**就必须使用Client库或者自己实现，可以是多种语言的API。


## 分析 ##

要想实现高可用有两点： **NameNode** 要主从双机热备，避免单点失效；每个 **DataNode** 可以做成主从复制甚至集群。

各个组件之间的连接情况：

- **NameNode** 要保持和 **N** 个 **Client** 的TCP长连接，但是只有在集群发生变化时才有交互，所以使用IO多路复用负载就不大
- **NameNode** 要和 **M** 个 **DataNode** 保持心跳，TCP请求响应式，负载与 **M** 和心跳间隔秒数 **interval** 有关
- **DataNode** 与 **Client** 是TCP请求响应式操作，操作结束断开连接，也可以考虑加入连接池
- **DataNode** 与 **NameNode** 保持心跳
- **Client** 与 **NameNode** 保持TCP长连接
- **Client** 与 **DataNode** TCP请求响应式操作

如下图所示，有4个连接，其中1、2要保持连接，3、4完成请求后就断开连接

                         NameNode
                       ||       ||     
      1、心跳请求响应||              ||2、监听长连接 
                 ||   3、数据请求响应   ||     
              DataNodes  ==========  Clients
               ||    ||
                  ||
          4、数据转移，可复用3  

开发优先级：3、1、4、2

具体性能瓶颈要结合压测来分析
    
## 使用方法 ##

**DataNode** 运行起来就可以直接使用 **redis-cli** 连接，如`redis-cli -h 127.0.0.1 -p 10100`，并进行`set、get、del`操作；

注意：现在必须首先运行 **NameNode**，然后通过JVM参数的方式调整端口，可以在同一台机器上运行多个 **DataNode**，
若要在不同机器上运行 **DataNode** 则可以直接修改配置文件

新的DataNode可以直接上线，NameNode会自动通知下一个节点转移相应数据给新节点；DataNode若要下线，
则可以通过telnet DataNode 节点的下线监听端口（TCP监听） 如 `telnet 127.0.0.1 6666` ，
并发送 **k** 字符即可，待下线的DataNode收到命令 **k** 后会自动把数据全部转移给下一个DataNode
然后提示进程pid，用户就可以关闭该DataNode进程了，如 **Linux**： `kill -s 9 23456`，**Windows**:`taskkill /pid 23456`

## 代码结构 ##

- **NameNode** : 实现 NameNode 功能
    - d : 
    - d : 
- **DataNode** : 实现 DataNode 功能
    - command : 处理客户端各个命令的具体命令对象
    - cron : 一些具有定时性质的任务 
    - handler : 处理连接的handler
- **Client** : 实现 Client 功能
    - d : 
    - d : 
- **Common** : 实现一些公共的功能，上面三个模块依赖于此模块 
    - command : 命令抽象类
    - model : 一些公用的pojo 
    - util : 一些工具类 
    - helper : 辅助脚本

全部代码在[Github][1]上，欢迎 star，欢迎 issue，欢迎 pull request......

[戳此][2]看原文，来自[MageekChiu][3]

[1]: https://github.com/MageekChiu/CHKV
[2]: http://mageek.cn/archives/96/
[3]: http://mageek.cn/