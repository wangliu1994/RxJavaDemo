package com.example.winnie.rxjavademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doObserver1();
            }
        });
    }

    private static void doObserver1() {
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onCompleted() {
                System.out.println("complete");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.toString());
            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }
        };

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                System.out.println("complete");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.toString());
            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }
        };

        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {

            }
        });

        observable = Observable.just("Hello", "Hi", "Aloha");

        Integer[] data = {1,2, 3};
        observable = Observable.from(data);

        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        };

        Action1<Throwable> onErrorAction = new Action1<Throwable>() {
            @Override
            public void call(Throwable s) {
                System.out.println(s.toString());
            }
        };

        Action0 onCompleteAction = new Action0() {
            @Override
            public void call() {
                System.out.println("complete");
            }
        };

        observable
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer o) {
                        return String.valueOf(o);
                    }
                })
                .lift(new Observable.Operator() {
                    @Override
                    public Object call(Object o) {
                        return null;
                    }
                })
                .subscribe(onNextAction, onErrorAction, onCompleteAction);
    }

    private void doObserver(){
        File[]folders = new File[5];
        Observable.from(folders)
                .flatMap(new Func1<File, Observable<File>>() {
                    @Override
                    public Observable<File> call(File file) {
                        return Observable.from(file.listFiles());
                    }
                })
                .filter(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        return file.getName().endsWith("png");
                    }
                })
                .map(new Func1<File, Bitmap>() {
                    @Override
                    public Bitmap call(File file) {
                        return getBitmapFromFile(file);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
    }

    private Bitmap getBitmapFromFile(File file){
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }
}
