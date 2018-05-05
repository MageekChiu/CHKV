#  Consistent Hashing based Key-Value Memory Storage  #

基于一致性哈希的分布式内存键值存储——CHKV。

## 系统设计 ##

- NameNode : 维护key与节点的映射关系（Hash环），用心跳检测DataNode（一般被动，被动失效时主动询问三次），节点增减等系统信息变化时调整数据并通知Client；
- DataNode : 存储具体的数据，向NameNode主动发起心跳并采用请求响应的方式来实现上下线，便于NameNode挪动数据
- Client : 负责向NameNode请求DataNode数据和Hash算法等系统信息并监听其变化，操纵数据时直接向对应DataNode发起请求就行，暂时只包含set,get,delete三个操作

NameNode失效则整个系统不可用

若当成内存数据库使用则 只要有一个DataNode失效（未经请求与数据转移就下线了）整个系统就不可对外服务；
若当成内存缓存使用则 DataNode失效只是失去了一部分缓存，系统仍然可用
客户要使用这个内存数据库就必须使用Client库，可以是多种语言的API。

## 代码结构 ##



## 分析 ##

要想实现高可用有两点： NameNode要主从双机热备，避免单点失效；每个DataNode可以做成主从复制甚至集群。

NameNode要保持和 **N** 个Client的TCP长连接，但是只有在集群发生变化时才有交互，所以使用IO多路复用问题就不大；

NameNode要和 **M** 个DataNode保持心跳，TCP请求响应式，与 **M** 和心跳间隔秒数 **interval** 有关；

DataNode与NameNode保持心跳；

DataNode与Client也是TCP请求响应式操作，可以考虑加入连接池；

Client与NameNode保持TCP长连接

Client与DataNodeTCP请求响应式操作

                     NameNode
                   ||       ||     
     心跳请求响应||            ||监听长连接 
              ||   数据请求响应   ||     
           DataNodes  ======  Clients


具体性能瓶颈要结合压测来分析


## 使用方法 ##