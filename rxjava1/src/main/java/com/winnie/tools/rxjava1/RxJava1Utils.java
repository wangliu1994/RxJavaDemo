package com.winnie.tools.rxjava1;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by winnie on 2018/9/15.
 */

public class RxJava1Utils {

    /**
     * 使用Observer观察者
     * 使用create创建Observable, Observable决定什么时候触发事件以及触发怎样的事件
     * create() 方法是 RxJava 最基本的创造事件序列的方法
     */
    public static void doObserver1() {
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("hello");
                        subscriber.onNext("I am winnie");
                        subscriber.onNext("what is your name");
                        subscriber.onCompleted();
                    }
                })
                .subscribe(new Observer<String>() {
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
                });
    }

    /**
     * 使用Subscriber观察者
     * 使用create创建Observable
     * create() 方法是 RxJava 最基本的创造事件序列的方法
     */
    public static void doObserver2() {
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("hello");
                        subscriber.onNext("I am winnie");
                        subscriber.onNext("what is your name");
                        subscriber.onCompleted();
                    }
                })
                .subscribe(new Subscriber<String>() {
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
                });
    }

    /**
     * 使用 just 创建事件序列
     * just(T...): 将传入的参数依次发送出来
     */
    public static void doObserver3() {
        // 将会依次调用：
        // onNext("Hello");
        // onNext("I am winnie");
        // onNext("what is your name");
        // onCompleted();
        Observable
                .just("hello, ", "I am winnie", "what is your name")
                .subscribe(new Subscriber<String>() {
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
                });

    }

    /**
     * 使用 from 创建事件序列
     * from(T[]) / from(Iterable<? extends T>) : 将传入的数组或 Iterable 拆分成具体对象后，依次发送出来
     */
    public static void doObserver4() {
        // 将会依次调用：
        // onNext("Hello");
        // onNext("I am winnie");
        // onNext("what is your name");
        // onCompleted();
        String[] datas = new String[]{"hello, ", "I am winnie", "what is your name"};
        Observable
                .from(datas)
                .subscribe(new Subscriber<String>() {
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
                });

    }

    /**
     * 使用不完整定义回调
     * onNextAction， onErrorAction， onCompleteAction
     */
    public static void doObserver5() {
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

        Observable
                .just("hello, ", "I am winnie", "what is your name")
                .subscribe(onNextAction);
        //或者
        Observable
                .just("hello, ", "I am winnie", "what is your name")
                .subscribe(onNextAction, onErrorAction);

        //或者
        Observable
                .just("hello, ", "I am winnie", "what is your name")
                .subscribe(onNextAction, onErrorAction, onCompleteAction);
    }

    /**
     * 线程控制，加载resource图片
     * subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。或者叫做事件产生的线程。
     * observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
     */
    public static void doObserver6(final Context context) {
        final int desId = R.mipmap.ic_launcher;
        Observable
                .create(new Observable.OnSubscribe<Drawable>() {
                    @Override
                    public void call(Subscriber<? super Drawable> subscriber) {
                        Drawable drawable = context.getResources().getDrawable(desId);
                        subscriber.onNext(drawable);
                    }
                })
                .subscribeOn(Schedulers.io())// 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Subscriber<Drawable>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //TODO 打印错误信息
                    }

                    @Override
                    public void onNext(Drawable drawable) {
                        //TODO 往imageView设置 drawable
                    }
                });
    }

    /**
     * 使用map转换
     * map() 方法将参数中的 int 对象转换成一个 Drawable 对象后返回
     */
    public static void doObserver7(final Context context) {
        final int desId = R.mipmap.ic_launcher;
        Observable
                .just(desId)
                .map(new Func1<Integer, Drawable>() {
                    @Override
                    public Drawable call(Integer integer) {
                        Drawable drawable = context.getResources().getDrawable(desId);
                        return drawable;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Drawable>() {
                    @Override
                    public void call(Drawable drawable) {
                        //TODO 往imageView设置 drawable
                    }
                });
    }

    /**
     * 使用flatMap
     */
    public static void doObserver8() {
        //boobi是个文件夹
        String[] fileNames = new String[]{"kitty.exe", "boobi", "photo.jpg"};
        Observable
                .from(fileNames)
                .map(new Func1<String, File>() {
                    @Override
                    public File call(String s) {
                        return new File(s);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        //TODO 处理一个File的内容
                        for (File x : file.listFiles()) {
                            //TODO 处理每一个file文件夹里面的文件内容
                        }
                    }
                });

        //或者使用flatMap，避免for循环
        //flatMap() 的原理是这样的：
        // 1. 使用传入的事件对象创建一个 Observable 对象；
        // 2. 并不发送这个 Observable, 而是将它激活，于是它开始发送事件；
        // 3. 每一个创建出来的 Observable 发送的事件，都被汇入同一个 Observable ，而这个 Observable 负责将这些事件统一交给 Subscriber 的回调方法。
        Observable
                .from(fileNames)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String fileName) {
                        File file = new File(fileName);
                        return Observable.from(file.list());
                    }
                })
                .map(new Func1<String, File>() {
                    @Override
                    public File call(String s) {
                        return new File(s);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        //TODO 处理一个File的内容，现在不需要再for循环遍历文件夹的子文件
                    }
                });
    }


    /**
     * lift转换方法, 主要适合Retrofit + RxJava的网络请求
     * 在 Observable 执行了 lift(Operator) 方法之后，
     * 会返回一个新的 Observable，这个新的 Observable 会像一个代理一样，
     * 负责接收原始的 Observable 发出的事件，并在处理后发送给 Subscriber。
     */
    public static void doObserver9() {
        Integer[] fileNames = new Integer[]{1, 23, 144};
        Observable
                .from(fileNames)
                .lift(new Observable.Operator<String, Integer>() {
                    @Override
                    public Subscriber<? super Integer> call(final Subscriber<? super String> subscriber) {
                        return new Subscriber<Integer>() {
                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(Integer integer) {
                                subscriber.onNext(String.valueOf(integer));
                            }
                        };
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {

                    }
                });
    }

    /**
     * 一直切换线程
     * 通过 observeOn() 的多次调用，程序实现了线程的多次切换。
     * 不过，不同于 observeOn() ， subscribeOn() 的位置放在哪里都可以，但它是只能调用一次的。
     * 当使用了多个 subscribeOn() 的时候，只有第一个 subscribeOn() 起作用。
     */
    public static void doObserver10() {
        Observable
                // IO 线程，由 subscribeOn() 指定
                .just(1, 2, 3, 4)
                .subscribeOn(Schedulers.io())
                //observeOn() 指定的是它之后的操作所在的线程
                .observeOn(Schedulers.newThread())
                // 新线程，由 observeOn() 指定
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        return null;
                    }
                })
                .observeOn(Schedulers.io())
                // IO 线程，由 observeOn() 指定
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                // Android 主线程，由 observeOn() 指定
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {

                    }
                });

    }

    public static void doObServer11(){
        final ArrayList<Integer> integers = (ArrayList<Integer>) Arrays.asList(1,22, 279, 73, 4, 5454);
        Observable
                .create(new Observable.OnSubscribe<ArrayList<Integer>>() {
                    @Override
                    public void call(Subscriber<? super ArrayList<Integer>> subscriber) {
                        Collections.sort(integers);
                        subscriber.onNext(integers);
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        //这里可以做showLoading
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())//指定doOnSubscribe在主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<Integer>>() {
                    @Override
                    public void call(ArrayList<Integer> integers) {

                    }
                });
    }

}
