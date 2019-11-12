# SerialWorker

```groovy

allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

  dependencies {
        implementation 'com.github.licheedev:SerialWorker:1.1.3'
}

```

## 使用
### 实现收发数据逻辑的`SerialWorker`
可以选择继承

1. BaseSerialWorker
 参考[读卡器 CardReaderWorker.java](https://github.com/licheedev/SerialWorker/blob/master/app/src/main/java/com/licheedev/serialworkerdemo/serial/CardReaderWorker.java)
2. Rs232SerialWorker
 参考[售货贵控制板 DoorSerialWorker.java](https://github.com/licheedev/SerialWorker/blob/master/app/src/main/java/com/licheedev/serialworkerdemo/serial/DoorSerialWorker.java)
3. Rs232SerialWorkerX
4. Rs485SerialWorker

### 实现处理收到数据的`DataReceiver`（可选）
 参考[售货柜数据接收器 DoorDataReceiver.java](https://github.com/licheedev/SerialWorker/blob/master/app/src/main/java/com/licheedev/serialworkerdemo/serial/DoorDataReceiver.java)