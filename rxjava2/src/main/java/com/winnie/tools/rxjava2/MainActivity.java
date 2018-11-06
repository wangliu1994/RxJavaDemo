package com.winnie.tools.rxjava2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.winnie.rxjava2.myapplication.R;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * @author winnie
 */
public class MainActivity extends AppCompatActivity {

    private Subscription mSubscription;

    private TextView mTextView;

    private StringBuffer mStringBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);
        mStringBuffer = new StringBuffer(mTextView.getText().toString());

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readArticle();
        }
    }

    private void readArticle() {
        Flowable
                .create(new FlowableOnSubscribe<String>() {
                    @Override
                    public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                        try {
                            FileReader reader = new FileReader(Environment
                                    .getExternalStorageDirectory().getPath() + "/test.txt");
                            BufferedReader br = new BufferedReader(reader);

                            String str;
                            while ((str = br.readLine()) != null && !emitter.isCancelled()) {
                                while (emitter.requested() == 0) {
                                    if (emitter.isCancelled()) {
                                        break;
                                    }
                                }
                                emitter.onNext(str);
                            }

                            br.close();
                            reader.close();

                            emitter.onComplete();
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String string) throws Exception {
                        mStringBuffer.append("\n" + string);
                        Log.d("MainActivity", string);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return mStringBuffer.toString();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        mSubscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(String string) {
                        mTextView.setText(string);
                        mSubscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println(t);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 比较完整的例子
     */
    private void doObserverSample() {
        File[] folders = new File[5];
        Observable.fromArray(folders)
                .flatMap(new Function<File, ObservableSource<File>>() {
                    @Override
                    public ObservableSource<File> apply(File file) throws Exception {
                        return Observable.fromArray(file.listFiles());
                    }
                })
                .filter(new Predicate<File>() {
                    @Override
                    public boolean test(File file) throws Exception {
                        return file.getName().endsWith("png");
                    }
                })
                .map(new Function<File, Bitmap>() {
                    @Override
                    public Bitmap apply(File file) throws Exception {
                        return getBitmapFromFile(file);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {

                    }
                });
    }

    private Bitmap getBitmapFromFile(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }
}
