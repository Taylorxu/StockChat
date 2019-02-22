package com.xzg.www.viewstub

import android.app.IntentService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import java.io.*
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket

class TcpService : IntentService("CSServer") {

    val defaultMessages = arrayListOf(
        "你好啊，嘻嘻",
        "看了你相片，你好帅哦，很喜欢你这样的",
        "我是江西人，你呢？",
        "你在哪里工作？"
    )
    var index = 0
    var isServiceDestroy = false
    override fun onCreate() {
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        try {
            //监听本地端口
            var serviceSocket = ServerSocket(30000)
            var socket = serviceSocket.accept()

            //获取输入流，接收客户端的消息
            var inputStream = socket.getInputStream()
            var bufferReader = BufferedReader(InputStreamReader(inputStream))

            //获取输出流，向客户端发送消息
            var outputStream = socket.getOutputStream()
            var printWriter = PrintWriter(OutputStreamWriter(outputStream))

//4、通过循环不断读取客户端发来的消息 ,并发送
            while (true) {
                var readLine = bufferReader.readLine()
                if (readLine.isNotEmpty()) {
                    val sendMag = if (index < defaultMessages.size) defaultMessages[index] else "已离线"
                    SystemClock.sleep(300)
                    printWriter.println("$sendMag\n")  //'\r' 必须有
                    printWriter.flush()// 刷新流
                    index++
                }
            }

            inputStream.close()
            bufferReader.close()
            outputStream.close()
            printWriter.close()
            socket.close()
            serviceSocket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    var TAG = "TCPServerService"
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}