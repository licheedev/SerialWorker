package com.licheedev.serialworkerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.licheedev.serialworker.core.Callback;
import com.licheedev.serialworkerdemo.serial.SerialManager;
import com.licheedev.serialworkerdemo.serial.command.recv.RecvA4OpenDoor;
import com.licheedev.serialworkerdemo.serial.command.recv.RecvSetReadTemp;
import com.licheedev.serialworkerdemo.serial.command.send.SendA4OpenDoor;
import com.licheedev.serialworkerdemo.serial.command.send.SendA8SetTemp;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    @butterknife.BindView(R.id.btn_open_door)
    Button mBtnOpenDoor;
    @butterknife.BindView(R.id.btn_set_temp)
    Button mBtnSetTemp;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SerialManager.get().initDevice();
    }

    @Override
    protected void onDestroy() {
        SerialManager.get().release();
        super.onDestroy();
    }

    @butterknife.OnClick({ R.id.btn_open_door, R.id.btn_set_temp })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open_door: {
                SendA4OpenDoor command = new SendA4OpenDoor(1, 10);

                SerialManager.get().openDoor(command, new Callback<RecvA4OpenDoor>() {
                    @Override
                    public void onSuccess(RecvA4OpenDoor recvA4OpenDoor) {
                        showToast("open_result=" + recvA4OpenDoor.getResult());
                    }

                    @Override
                    public void onFailure(Throwable tr) {
                        showToast(tr.getMessage());
                    }
                });
                break;
            }
            case R.id.btn_set_temp: {
                SendA8SetTemp command = new SendA8SetTemp(0, 100, 0);

                SerialManager.get()
                    .rxsetTemp(command)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<RecvSetReadTemp>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(RecvSetReadTemp recvSetReadTemp) {
                            showToast(recvSetReadTemp.toString());
                        }

                        @Override
                        public void onError(Throwable e) {
                            showToast(e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                break;
            }
        }
    }

    private void showToast(String s) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG);
        mToast.show();
    }
}
