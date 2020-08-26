package com.licheedev.serialworkerdemo.serial

import android.os.Handler
import com.licheedev.serialworker.core.DataReceiver
import com.licheedev.serialworker.worker.RxRs232SerialWorkerX
import com.licheedev.serialworkerdemo.serial.command.RecvCommand
import com.licheedev.serialworkerdemo.serial.command.SendCommand

/**
 * 柜子的串口操作
 */
class KtDoorSerialWorker(private val mRecvHandler: Handler?) :
    RxRs232SerialWorkerX<SendCommand, RecvCommand>() {
    override fun isMyResponse(sendData: SendCommand, recvData: RecvCommand): Boolean {
        // 如果收到的命令跟发送的命令是同类型
        return sendData.cmd == recvData.cmd
    }

    override fun newReceiver(): DataReceiver<RecvCommand>? {
        return DoorDataReceiver()
    }

    override fun onReceiveData(recvData: RecvCommand) {
        // 把数据暴露出去
        if (mReceiveCallback != null) {
            mRecvHandler?.post { mReceiveCallback!!.onReceive(recvData) }
                ?: mReceiveCallback!!.onReceive(recvData)
        }
    }

    private var mReceiveCallback: ReceiveCallback? = null

    interface ReceiveCallback {
        fun onReceive(recvCommand: RecvCommand?)
    }

    fun setReceiveCallback(receiveCallback: ReceiveCallback?) {
        mReceiveCallback = receiveCallback
    }
}