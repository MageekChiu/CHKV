var net = require('net');

var HOST = '127.0.0.1';
var PORT = 4567;

var client = new net.Socket();

client.connect(PORT, HOST, function() {
    console.log('连接到: ' + HOST + ':' + PORT);
    client.write("sfsdsadas");
    setTimeout(function(){
        client.write("33333333333333");
    },1000);

});

// 为客户端添加“data”事件处理函数
// data是服务器发回的数据
client.on('data', function(data) {
    console.log('收到消息: ' + data);
});

client.on('error', function() {
    console.log('连接出错');
});

// 为客户端添加“close”事件处理函数
client.on('close', function() {
    console.log('连接关闭');
    process.abort();
});
