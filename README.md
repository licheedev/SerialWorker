# SerialWorker

[![](https://jitpack.io/v/licheedev/SerialWorker.svg)](https://jitpack.io/#licheedev/SerialWorker)

```groovy

allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

  dependencies {
        implementation 'com.github.licheedev:SerialWorker:2.0.0'
}

```

## 使用
### 实现收发数据逻辑的`SerialWorker`
可以选择继承

[BaseSerialWorker](https://github.com/licheedev/SerialWorker/blob/master/serialworker/src/main/java/com/licheedev/serialworker/worker/BaseSerialWorker.java)
> 参考 [读卡器 CardReaderWorker.java](https://github.com/licheedev/SerialWorker/blob/master/app/src/main/java/com/licheedev/serialworkerdemo/serial/CardReaderWorker.java)

[Rs232SerialWorker](https://github.com/licheedev/SerialWorker/blob/master/serialworker/src/main/java/com/licheedev/serialworker/worker/Rs232SerialWorker.java)
> 参考 [售货柜控制板 DoorSerialWorker.java](https://github.com/licheedev/SerialWorker/blob/master/app/src/main/java/com/licheedev/serialworkerdemo/serial/DoorSerialWorker.java)

[Rs232SerialWorkerX](https://github.com/licheedev/SerialWorker/blob/master/serialworker/src/main/java/com/licheedev/serialworker/worker/Rs232SerialWorkerX.java)

[Rs485SerialWorker](https://github.com/licheedev/SerialWorker/blob/master/serialworker/src/main/java/com/licheedev/serialworker/worker/Rs485SerialWorker.java)

### 实现处理收到数据的[`DataReceiver`](https://github.com/licheedev/SerialWorker/blob/master/serialworker/src/main/java/com/licheedev/serialworker/core/DataReceiver.java)（可选）
> 参考 [售货柜数据接收器 DoorDataReceiver.java](https://github.com/licheedev/SerialWorker/blob/master/app/src/main/java/com/licheedev/serialworkerdemo/serial/DoorDataReceiver.java)


### 封装发送命令[`SendData`](https://github.com/licheedev/SerialWorker/blob/master/serialworker/src/main/java/com/licheedev/serialworker/core/SendData.java)（可选）
> 参考 [售货柜开门指令 SendA4OpenDoor.java](https://github.com/licheedev/SerialWorker/blob/master/app/src/main/java/com/licheedev/serialworkerdemo/serial/command/send/SendA4OpenDoor.java)

### 封装接收数据[`RecvData`](https://github.com/licheedev/SerialWorker/blob/master/serialworker/src/main/java/com/licheedev/serialworker/core/RecvData.java)（可选）
> 参考 [售货柜开门结果 RecvA4OpenDoor.java](https://github.com/licheedev/SerialWorker/blob/master/app/src/main/java/com/licheedev/serialworkerdemo/serial/command/recv/RecvA4OpenDoor.java)
