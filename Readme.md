#  Consistent Hashing based Key-Value Memory Storage  #

基于[一致性哈希][5]的分布式内存键值存储——**CHKV**。
目前的定位就是作为 **Cache**，**DataBase** 的功能先不考虑。

## 系统设计 ##

- **NameNode** : 维护 **key、DataNode节点** 在Hash环上的映射关系，用心跳检测 **DataNode**（一般被动，被动失效时主动询问三次），节点增减等系统信息变化时调整数据并通知 **Client**；
- **DataNode** : 存储具体的数据，向 **NameNode** 主动发起心跳并采用请求响应的方式来实现上下线，便于 **NameNode** 发起挪动数据指令；
- **Client** : 负责向 **NameNode** 请求 **DataNode** 数据和Hash算法等系统信息并监听其变化，操纵数据时直接向对应 **DataNode** 发起请求就行，暂时只包含`set,get,delete`三个操作；

**NameNode** 失效则整个系统不可用。

<del>若当成内存数据库使用，则只要有一个 **DataNode** 失效（未经请求与数据转移就下线了）整个系统就不可对外服务；
若当成内存缓存使用，则 **DataNode** 失效只是失去了一部分缓存，系统仍然可用。
</del>

**DataNode** 失效（未经请求与数据转移就断开了和 **NameNode** 的连接）则 **NameNode** 需要及时通知 **Client**。


**客户** 要使用 **CHKV** 就必须使用 **Client** 库或者自己依据协议（兼容redis）实现，可以是多种语言的API。
也可以把 **Client** 当做 **Proxy**，使得 **CHKV** 内部结构对 **客户** 透明。

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

具体性能要结合压测来分析。

## 代码结构 ##

- **NameNode** : 实现 NameNode 功能

    - handler : handler
    - res : 资源，如常量，命令工厂 
    - service : 服务，含Client管理，DataNode管理
    
- **DataNode** : 实现 DataNode 功能

    - command : 处理客户端各个命令的具体命令对象
    - job : 一些的任务如心跳、数据迁移 
    - handler : 处理连接的handler
    - service : 服务，含定时任务管理，数据请求管理
    
- **Client** : 实现 Client 功能

    - handler : handler
    - Client : 暴露给用户的命令管理 
    - Connection : 发出网络请求 
    
- **Common** : 实现一些公共的功能，上面三个模块依赖于此模块 

    - command : 命令抽象类
    - model : 一些公用的pojo，如请求响应对象 
    - util : 一些工具类 
    - helper : 辅助脚本
        
## 使用方法 ##

**DataNode** 运行起来就可以直接使用 **redis-cli** 连接，如`redis-cli -h 127.0.0.1 -p 10100`，并进行`set、get、del`操作；

注意：现在必须首先运行 **NameNode**，然后通过JVM参数的方式调整端口，可以在同一台机器上运行多个 **DataNode**，
若要在不同机器上运行 **DataNode** 则可以直接修改配置文件

新的DataNode可以直接上线，NameNode会自动通知下一个节点转移相应数据给新节点；DataNode若要下线，
则可以通过telnet DataNode 节点的下线监听端口（TCP监听） 如 `telnet 127.0.0.1 6666` ，
并发送 **k** 字符即可，待下线的DataNode收到命令 **k** 后会自动把数据全部转移给下一个DataNode
然后提示进程pid，用户就可以关闭该DataNode进程了，如 **Linux**： `kill -s 9 23456`，**Windows**:`taskkill /pid 23456`

NameNode和DataNode启动后就可以使用Client了，代码示例如下：

Client代码示例[在此，关键如下：][4]

        try(Client client = new Client("192.168.0.136","10102")){
            logger.debug(client.set("192.168.0.136:10099","123456")+"");
            logger.debug(client.get("192.168.0.136:10099")+"");
            logger.debug(client.set("112","23")+"");
            logger.debug(client.del("1321")+"");
            logger.debug(client.del("112")+"");
        }

## 压力测试 ##

在本机开启1个NameNode和1个DataNode直接压测，4次

`redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -q`
- SET: 5006.76 requests per second
- SET: 5056.43 requests per second
- SET: 5063.55 requests per second
- SET: 5123.74.55 requests per second


把以上2个节点日志级别都调整为info（实际上DataNode节点才会影响qps），重启

`redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -q`
- SET: 62421.97 requests per second
- SET: 87260.03 requests per second
- SET: 92592.59 requests per second
- SET: 94517.96 requests per second

可见日志对**qps**影响很大，是 **几k** 与 **几十k** 的不同数量级的概念，若把级别改成error，**平均qps**还能提升 **几k**，所以生产环境一定要注意日志级别。

此外观察，不重启并且每次压测间隔都很小的话，qps一般会从 **65k** 附近开始，经过1、2次的 **88k** 左右，最终稳定在 **98k** 附近，数十次测试，最低 **62.4k**，最高**101.2k**。

重启的话，**qps**就会重复上述变化过程，这应该是和内存分配等初始化工作有关，第1次压测有大量的初始化，而后面就没了，所以第一次**qps**都比较低；还可能与 **JIT** 有关，所以 **Java** 的性能测试严格上来说要忽略掉最初的几个样本才对。

经观察，DataNode进程启动后，内存消耗在59M附近，第1次压测飙升到134M然后稳定到112M，第2次上升到133M然后稳定到116M，后面每次压测内存都是先增加几M然后减小更多，最终稳定在76M。


在本机运行一个redis-server进程，然后压测一下

`redis-benchmark -h 127.0.0.1 -p 6379 -c 100 -t set -q`
- SET: 129032.27 requests per second
- SET: 124533.27 requests per second
- SET: 130208.34 requests per second
- SET: 132450.33 requests per second

经数十次测试，**qps** 稳定在 **128k** 附近，最高 **132.3k** ，最低 **122.7k** 可见**CHKV**的单个 **DataNode** 目前性能还比不过单个 **redis**。

DataNode经过重构后，现在的压测结果如下

`redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -q`

- SET: 78554.59 requests per second
- SET: 114285.71 requests per second
- SET: 119047.63 requests per second
- SET: 123628.14 requests per second

经过多次测试，**qps** 稳定在 **125k** 附近，最高 **131.9k** ，最低 **78.6k**（这是启动后第一次压测的特例，后期稳定时最低是 **114.3k**），可见重构后
单个 **DataNode** 和单个 **redis-server** 的 **qps** 差距已经很小了，优化效果还是比较明显的。

主要优化两个：去掉单独的 **BusinessHandler** 的单独逻辑线程，因为没有耗时操作，直接在IO线程操作反而能省掉切换时间；
**DataNode** 通过 `public static volatile Map<String,String> DATA_POOL` 共享数据池，其他相关操作类减少了这个域，省一些内存；
第一条对比明显，很容易直接测试，第二条没直接测，只是分析。

然后通过` -Xint` 或者 `-Djava.compiler=NONE` 关闭 **JIT** 使用 **解释模式**，再压测试试。

`redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -q`
- SET: 16105.65 requests per second
- SET: 16244.31 requests per second
- SET: 16183.85 requests per second
- SET: 16170.76 requests per second

可见关闭 **JIT** 后 **qps** 降低了 **7倍多**，而且每次差别不大（即使是第一次），这也能说明上面（默认是**混合模式**）第一次压测的 **qps** 比后面低了那么多的原因确实和 **JIT** 有关。

通过 `-Xcomp` 使用 **编译模式** ，启动会很慢。

`redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -q`
- SET: 83612.04 requests per second
- SET: 117647.05 requests per second
- SET: 121802.68 requests per second
- SET: 120048.02 requests per second

可见 **编译模式** 并没有比 **混合模式** 效果好，因为即使是不热点的代码也要编译，反而浪费时间，所以一般还是选择默认的 **混合模式** 较好。

然后来验证**线程数、客户端操作**与 **qps** 的关系，实验机器是 `4 core、8 processor`，我把 **DataNode** 的 `DataManager` 中 `workerGroup`的线程数依次减少从 **8** 调到为 **1** （之前的测试都是 **4** ），
发现 **qps** 先升后降，在值为 **2** 的时候达到最大值，**超过了redis**，下面是数据

`redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -q`
- SET: 93283.04 requests per second
- SET: 141043.05 requests per second
- SET: 145560.68 requests per second
- SET: 145384.02 requests per second

经数十次测试，**qps** 稳定在 **142k** 附近，最高 **150.6k** ，稳定后最低 **137.2k**。
Netty本身使用了IO多路复用，在客户端操作都比较轻量（压测这个 **set** 也确实比较轻量）择时线程数较少是合理的，
因为这时候线程切换的代价超过了多线程带来的好处，这样我们也能理解 **redis** 单线程设计的初衷了，
单线程虽然有些极端，但是如果考虑 **面向快速轻量操作的客户端** 和 **单线程的安全与简洁特性**，也是最佳的选择。

但是如果客户端操作不是轻量级的，比如我们把 `set` 数据大小调为`500bytes`，再对 **CKHV** 不同的 `workerGroup`线程数进行压测

2 `redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -d 500 -q`

- SET: 80450.52 requests per second
- SET: 102459.02 requests per second
- SET: 108813.92 requests per second
- SET: 99206.34 requests per second

3 `redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -d 500 -q`
- SET: 92592.59 requests per second
- SET: 133868.81 requests per second
- SET: 133868.81 requests per second
- SET: 135685.22 requests per second

4 `redis-benchmark -h 127.0.0.1 -p 10100 -c 100 -t set -d 500 -q`
- SET: 72046.11 requests per second
- SET: 106723.59 requests per second
- SET: 114810.56 requests per second
- SET: 119047.63 requests per second

可见这个时候4、3个线程**qps**都大于2个线程，符合验证，但是4的**qps**又比3少，说明线程太多反而不好，
然而把数据大小调到`900byte`时，4个线程又比3个线程的**qps**大了，
所以这个参数真的要针对不同的应用场景做出不同的调整，总结起来就是轻量快速的操作适宜线程 **适当少**，重量慢速操作适宜线程 **适当多**。


## 未来工作 ##

水平有限，目前项目的问题还很多，可以改进的地方还很多，先列个清单：

- 高可用性保证
- 断线重连
- DataNode迁移数据的完整性保证
- 迁移过程数据的一致性
- 对于WeakReference的支持
- 更多数据类型
- 更多操作
- 完整的校验机制
- 等等......

全部代码在[Github][1]上，欢迎 **star**，欢迎 **issue**，欢迎 **fork**，欢迎 **pull request**......
总之就是欢迎大家和我一起完善这个项目，一起进步。

[戳此][2]看原文，来自[MageekChiu][3]

[1]: https://github.com/MageekChiu/CHKV
[2]: http://mageek.cn/archives/96/
[3]: http://mageek.cn/
[4]: https://github.com/MageekChiu/CHKV/blob/master/Client/src/test/java/cn/mageek/client/ConnectionTest.java
[5]: https://zh.wikipedia.org/wiki/%E4%B8%80%E8%87%B4%E5%93%88%E5%B8%8C