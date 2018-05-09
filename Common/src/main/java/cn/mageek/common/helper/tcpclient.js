var net = require('net');

var HOST = '127.0.0.1';var PORT = 10100;// 自己实现的兼容redis服务 会收到消息 +OK $3 gem  $-1 :1  :0
// var HOST = '1*3.*0*.*4.6*';var PORT = 6***;//一个远程实际的redis服务器，会收到消息: -NOAUTH Authentication required. 因为没发送auth 信息
// var HOST = '127.0.0.1';var PORT = 6379;// 本机redis服务器 会收到消息 +OK $3 gem  $-1 :1  :0

var client = new net.Socket();

client.connect(PORT, HOST, function() {
    console.log('连接到: ' + HOST + ':' + PORT);
    // // set
    // var msg = "*3\r\n" +
    //     "$3\r\n" +
    //     "SET\r\n" +
    //     "$2\r\n" +
    //     "sm\r\n" +
    //     "$3\r\n" +
    //     "gem\r\n";
    // console.log("发送"+msg);
    // client.write( msg );
    //

    // set 多个命令，一次发六条收到消息就会粘包
    // 收到消息: +OK<br>
    // 收到消息: +OK<br>+OK<br>+OK<br>+OK<br>+OK<br>

    var msg = "*3\r\n" +
        "$3\r\n" +
        "SET\r\n" +
        "$2\r\n" +
        "sm\r\n" +
        "$3\r\n" +
        "gem\r\n\t\n"+
        "*3\r\n" +
        "$3\r\n" +
        "SET\r\n" +
        "$2\r\n" +
        "sf\r\n" +
        "$3\r\n" +
        "gem\r\n\t\n"+
        "*3\r\n" +
        "$3\r\n" +
        "SET\r\n" +
        "$2\r\n" +
        "rm\r\n" +
        "$3\r\n" +
        "gem\r\n\t\n"+
        "*3\r\n" +
        "$3\r\n" +
        "SET\r\n" +
        "$2\r\n" +
        "tm\r\n" +
        "$3\r\n" +
        "gem\r\n\t\n"+
        "*3\r\n" +
        "$3\r\n" +
        "SET\r\n" +
        "$2\r\n" +
        "sw\r\n" +
        "$3\r\n" +
        "gem\r\n\t\n"+
        "*3\r\n" +
        "$3\r\n" +
        "SET\r\n" +
        "$2\r\n" +
        "sg\r\n" +
        "$3\r\n" +
        "gam\r\n";
    console.log("发送"+msg);
    client.write( msg );


    // // get 存在
    // setTimeout(function () {
    //     msg = "*2\r\n" +
    //         "$3\r\n" +
    //         "GET\r\n" +
    //         "$2\r\n" +
    //         "sm\r\n";
    //     console.log("发送"+msg);
    //     client.write(msg);
    // },1500);
    //
    // // get 不存在
    // setTimeout(function () {
    //     msg = "*2\r\n" +
    //         "$3\r\n" +
    //         "GET\r\n" +
    //         "$2\r\n" +
    //         "em\r\n";
    //     console.log("发送"+msg);
    //     client.write(msg);
    // },2300);
    //
    // // del 存在
    // setTimeout(function () {
    //     msg = "*2\r\n" +
    //         "$3\r\n" +
    //         "del\r\n" +
    //         "$2\r\n" +
    //         "sm\r\n";
    //     console.log("发送"+msg);
    //     client.write(msg);
    // },3200);
    //
    // // del 不存在
    // setTimeout(function () {
    //     msg = "*2\r\n" +
    //         "$3\r\n" +
    //         "del\r\n" +
    //         "$2\r\n" +
    //         "sg\r\n";
    //     console.log("发送"+msg);
    //     client.write(msg);
    // },4200);

    // COMMAND
    // setTimeout(function () {
    //     msg = "*1\r\n" +
    //         "$7\r\n" +
    //         "COMMAND\r\n" +
    //     console.log("发送"+msg);
    //     client.write(msg);
    // },5200);// 返回所有命令 很长很长
});

// 为客户端添加“data”事件处理函数
// data是服务器发回的数据
client.on('data', function(data) {
    data = data.toString("utf8");
    if (data.length>160)
        console.log('收到缩略消息: ' + data.substring(0,160));
    else
        console.log('收到消息: ' + data.replace(/\r\n/g,"<br>"));
    // console.log(data.length)
});

client.on('error', function(e) {
    console.log('连接出错'+e);
});

// 为客户端添加“close”事件处理函数
client.on('close', function() {
    console.log('连接关闭');
});
