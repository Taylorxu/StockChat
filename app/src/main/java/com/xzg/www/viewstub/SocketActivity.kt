package com.xzg.www.viewstub

import android.app.IntentService
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_socket.*
import java.io.*
import java.net.Socket

class SocketActivity : AppCompatActivity(), View.OnClickListener {


    var CONNECT_SERVER_SUCCESS = 0 //与服务端连接成功
    var MESSAGE_RECEIVE_SUCCESS = 1 //接受到服务端的消息
    var MESSAGE_SEND_SUCCESS = 2 //消息发送
    var mHandler = Handler(Handler.Callback {
        when (it.what) {
            CONNECT_SERVER_SUCCESS -> {
                //与服务端连接成功
                textViewContent.text = "与聊天室连接成功\n"
            }

            MESSAGE_RECEIVE_SUCCESS -> {
                var msgContent = textViewContent.text.toString()
                textViewContent.text = msgContent + it.obj.toString() + "\n"

            }

            MESSAGE_SEND_SUCCESS -> {
                inputArea.setText("")
                textViewContent.text = textViewContent.text.toString() + it.obj.toString() + "\n"
            }


        }
        return@Callback false
    })

    lateinit var mPrintWriter: PrintWriter
    lateinit var myIntent: Intent
    var mySocket: Socket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket)
        btnSend.setOnClickListener(this)
        myIntent = Intent(this, TcpService::class.java)
        startService(myIntent)

        //连接服务端，实现通信交互
        //IO操作必须放在子线程执行
        Thread(Runnable { connectTCPServer() }).start()
    }


    private fun connectTCPServer() {
//通过循环来判断Socket是否有被创建，若没有则会每隔1s尝试创建，目的是保证客户端与服务端能够连接
        while (mySocket == null) {
            try {
                //创建Socket对象，指定IP地址和端口号
                mySocket = Socket("192.168.1.145", 30000)
                mPrintWriter = PrintWriter(OutputStreamWriter(mySocket!!.getOutputStream()), true)
                if (mySocket!!.isConnected) //判断是否连接成功
                    mHandler.sendEmptyMessage(CONNECT_SERVER_SUCCESS)
            } catch (e: IOException) {
                e.printStackTrace()
                //设计休眠机制，每次重试的间隔时间为1s
                SystemClock.sleep(1000)
            }
        }
        //通过循环来，不断的接受服务端发来的消息
        try {
            var reader = BufferedReader(InputStreamReader(mySocket!!.getInputStream()))
            while (!SocketActivity@ this.isFinishing) { //当Activity销毁后将不接受
                var msg = reader.readLine()
                if (!TextUtils.isEmpty(msg)) {
                    //发消息通知更新UI
                    mHandler.obtainMessage(MESSAGE_RECEIVE_SUCCESS, msg).sendToTarget()
                }

            }
            //关闭流
            mPrintWriter.close()
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        //必须开启子线程，不能在UI线程操作网络
        Thread(Runnable {
            var msg = inputArea.text.toString()
            if (mPrintWriter != null && !TextUtils.isEmpty(msg)) {
                mPrintWriter.println(msg + "\n")
                //通知更新UI
                mHandler.obtainMessage(MESSAGE_SEND_SUCCESS, msg).sendToTarget();
            }
        }).start()

    }

    var TAG = "SocketActivity"
    override fun onDestroy() {
        super.onDestroy()
        //关闭输入流和连接
        if (mySocket != null) {
            try {
                mySocket!!.shutdownInput()
                mySocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        //停止后台服务
        stopService(myIntent)
        Log.e(TAG, "onDestroy")
    }


}
