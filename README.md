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
> 1. BaseSerialWorker
> 2. Rs232SerialWorker
> 3. Rs232SerialWorkerX
> 4. Rs485SerialWorker

### 实现处理收到数据的`DataReceiver`（可选）