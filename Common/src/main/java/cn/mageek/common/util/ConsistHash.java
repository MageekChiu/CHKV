package cn.mageek.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Mageek Chiu
 * @date 2018/5/5 0005:20:17
 */
public class ConsistHash {
    private static final Logger logger = LoggerFactory.getLogger(ConsistHash.class);

    //key表示服务器的hash值，value表示服务器 ip:port
    private ConcurrentSkipListMap<Integer, String> sortedServerMap;

    /**
     * 程序初始化，将所有的服务器放入sortedMap中
     * @param dataNodeClientMap 待添加入Hash环的服务器
     */
    public ConsistHash(ConcurrentSkipListMap<Integer, String> sortedServerMap,Map<String,String> dataNodeClientMap) {
        this.sortedServerMap = sortedServerMap;
        for (Map.Entry<String, String> entry : dataNodeClientMap.entrySet()) {
            String IPPort = entry.getValue();
            int hash = getHash(IPPort);
//            logger.debug("{} 入环, Hash {}",IPPort,hash);
            // 维护集合
            sortedServerMap.put(hash,IPPort);
        }
    }

    /**
     * 得到数据根据key应当路由到的server结点,也就是 >= hash(key) 的server
     * 如果是server节点，就要不能包含，防止找到本身
     * @param key data的key 或者 server 的 ip:port
     * @param isServer  是server还是data
     * @return 服务器节点
     */
    public String getServer(String key, boolean isServer) {
        //得到该key的hash值
        int keyHash = getHash(key);
        //得到 >= 该Hash值的所有节点构成的子map
        SortedMap<Integer, String> subMap = sortedServerMap.tailMap(keyHash,!isServer);// submap中所有节点的hash值 >= keyHash 是server就不包含，不是server是data就包含
        if(subMap.isEmpty()){
            //如果没有比该key的hash值大的，则从第一个node开始
            Integer i = sortedServerMap.firstKey();
            //返回对应的服务器
            return sortedServerMap.get(i);
        }else{
            //第一个Key就是顺时针过去离node最近的那个结点
            Integer i = subMap.firstKey();
            //返回对应的服务器
            return subMap.get(i);
        }
    }



    /**
     * 使用FNV1_32_HASH算法计算字符串的Hash值
     * @param str 服务器 ip:port 或者数据的 key
     * @return hash 值
     */
    public static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

}

//public class ConsistentHashingWithVirtualNode {
//
//    //待添加入Hash环的服务器列表
//    private static String[] servers = {"192.168.0.0:111", "192.168.0.1:111", "192.168.0.2:111",
//            "192.168.0.3:111", "192.168.0.4:111"};
//
//    //真实结点列表,考虑到服务器上线、下线的场景，即添加、删除的场景会比较频繁，这里使用LinkedList会更好
//    private static List<String> realNodes = new LinkedList<String>();
//
//    //虚拟节点，key表示虚拟节点的hash值，value表示虚拟节点的名称
//    private static SortedMap<Integer, String> virtualNodes = new TreeMap<Integer, String>();
//
//    //虚拟节点的数目，这里写死，为了演示需要，一个真实结点对应5个虚拟节点
//    private static final int VIRTUAL_NODES = 5;
//
//    static{
//        //先把原始的服务器添加到真实结点列表中
//        for(int i=0; i<servers.length; i++)
//            realNodes.add(servers[i]);
//
//        //再添加虚拟节点，遍历LinkedList使用foreach循环效率会比较高
//        for (String str : realNodes){
//            for(int i=0; i<VIRTUAL_NODES; i++){
//                String virtualNodeName = str + "&&VN" + String.valueOf(i);
//                int hash = getHash(virtualNodeName);
//                System.out.println("虚拟节点[" + virtualNodeName + "]被添加, hash值为" + hash);
//                virtualNodes.put(hash, virtualNodeName);
//            }
//        }
//        System.out.println();
//    }
//
//    //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
//    private static int getHash(String str){
//        final int p = 16777619;
//        int hash = (int)2166136261L;
//        for (int i = 0; i < str.length(); i++)
//            hash = (hash ^ str.charAt(i)) * p;
//        hash += hash << 13;
//        hash ^= hash >> 7;
//        hash += hash << 3;
//        hash ^= hash >> 17;
//        hash += hash << 5;
//
//        // 如果算出来的值为负数则取其绝对值
//        if (hash < 0)
//            hash = Math.abs(hash);
//        return hash;
//    }
//
//    //得到应当路由到的结点
//    private static String getServer(String key){
//        //得到该key的hash值
//        int hash = getHash(key);
//        // 得到大于该Hash值的所有Map
//        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
//        String virtualNode;
//        if(subMap.isEmpty()){
//            //如果没有比该key的hash值大的，则从第一个node开始
//            Integer i = virtualNodes.firstKey();
//            //返回对应的服务器
//            virtualNode = virtualNodes.get(i);
//        }else{
//            //第一个Key就是顺时针过去离node最近的那个结点
//            Integer i = subMap.firstKey();
//            //返回对应的服务器
//            virtualNode = subMap.get(i);
//        }
//        //virtualNode虚拟节点名称要截取一下
//        if(StringUtils.isNotBlank(virtualNode)){
//            return virtualNode.substring(0, virtualNode.indexOf("&&"));
//        }
//        return null;
//    }
//
//    public static void main(String[] args){
//        String[] keys = {"太阳", "月亮", "星星"};
//        for(int i=0; i<keys.length; i++)
//            System.out.println("[" + keys[i] + "]的hash值为" +
//                    getHash(keys[i]) + ", 被路由到结点[" + getServer(keys[i]) + "]");
//    }
//}


