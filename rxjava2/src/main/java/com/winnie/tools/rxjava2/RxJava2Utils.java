package com.winnie.tools.rxjava2;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by winnie on 2018/9/15.
 * 参考博客：https://www.jianshu.com/p/36e0f7f43a51
 */

public class RxJava2Utils {
    public static void doFun1() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            private int i;
            private Disposable mDisposable;

            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
                Log.d(TAG, "subscribe");
            }

            @Override
            public void onNext(Integer integer) {
                i++;
                if (i == 2) {
                    //mDisposable可以切断操作，不再接收上游事件
                    mDisposable.dispose();
                }
                Log.d(TAG, "" + integer);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "error");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "complete");
            }
        });
    }

    private static void doFun2() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {

            }
        });
    }

    private final static String TAG = "RxJava2";
    private static boolean isFromNet = false;

    /**
     * 使用 concat 操作符
     * 在操作符 concat 中，只有调用 onComplete 之后才会执行下一个Observable
     * 使用场景：获取缓存数据，没有缓存数据的时候才做网络请求
     */
    private static void doFun3() {
        Observable<FoodList> cache = Observable.create(new ObservableOnSubscribe<FoodList>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<FoodList> e) throws Exception {
                FoodList data = CacheManager.getInstance().getFoodListData();
                if (data != null) {
                    // 如果缓存数据不为空，则直接读取缓存数据，而不读取网络数据
                    isFromNet = false;
                    Log.d(TAG, "subscribe: 读取缓存数据:");
                    e.onNext(data);
                } else {
                    isFromNet = true;
                    Log.d(TAG, "subscribe: 读取网络数据:");
                    e.onComplete();
                }
            }
        });

        Observable<FoodList> netWork = Rx2AndroidNetworking.get("baidu.com")
                .addQueryParameter("row", "10")
                .build()
                .getObjectObservable(FoodList.class);

        Observable
                .concat(cache, netWork)
                .subscribe(new Consumer<FoodList>() {
                    @Override
                    public void accept(FoodList foodList) throws Exception {
                        if (isFromNet) {
                            Log.d(TAG, "subscribe: 设置网络数据的缓存:");
                            CacheManager.getInstance().setFoodListData(foodList);
                        }
                        Log.d(TAG, "subscribe: 读取数据成功:");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "subscribe: 读取数据失败:" + throwable.getMessage());
                    }
                });
    }

    /**
     * 使用zip方法，合并几个Observable
     * 使用场景：获取两个网络接口的数据，都拿到之后才做页面展示的情况
     */
    private static void doFun4() {
        Observable<FoodList> netWork1 = Rx2AndroidNetworking.get("baidu.com")
                .addQueryParameter("row", "10")
                .build()
                .getObjectObservable(FoodList.class);

        Observable<CateList> netWork2 = Rx2AndroidNetworking.get("baidu.com")
                .addQueryParameter("row", "11")
                .build()
                .getObjectObservable(CateList.class);

        Observable
                .zip(netWork1, netWork2, new BiFunction<FoodList, CateList, String>() {
                    @Override
                    public String apply(FoodList foodList, CateList cateList) throws Exception {
                        return "合并后的数据为：" + foodList.foodName + cateList.name;
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "subscribe: 读取数据失败:" + throwable.getMessage());
                    }
                });

    }

    /**
     * 使用interval做轮训操作
     */
    private static void doFun5() {
        Flowable.interval(1, TimeUnit.SECONDS)
                .onBackpressureLatest()
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d(TAG, "accept: doOnNext" + aLong);
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d(TAG, "accept: 设置文本" + aLong);
                    }
                });
    }

    /**
     * 使用 Flowable，Subscriber
     */
    private static void doFun6() {
        Flowable
                .create(new FlowableOnSubscribe<Long>() {
                    @Override
                    public void subscribe(FlowableEmitter<Long> emitter) throws Exception {
                        long size = emitter.requested();
                        for(int i= 0; i< size; i++) {
                            emitter.onNext((long) i);
                        }
                        emitter.onComplete();
                    }
                }, BackpressureStrategy.BUFFER)//背压参数
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);//下游告诉上游，我能接收多少个事件
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private static void doFun7() {
        Flowable
                .create(new FlowableOnSubscribe<String>() {
                    @Override
                    public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                       while (emitter.requested() != 0){
                           emitter.onNext("发送一个事件");
                       }
                        emitter.onComplete();
                    }
                }, BackpressureStrategy.BUFFER)//背压参数
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(String s) {
                        try {
                            Thread.sleep(2000);
                            subscription.request(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, s);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @SuppressLint("CheckResult")
    public static void doFun10() {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "Observable thread is : " + Thread.currentThread().getName());
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
            }
        });

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d(TAG, "Observer thread is :" + Thread.currentThread().getName());
                Log.d(TAG, "onNext: " + integer);
            }
        };

        observable
                .subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "After observeOn(io), current thread is : " + Thread.currentThread().getName());
                    }
                })
                .subscribe(consumer);
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
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.length() > 3;
                    }
                })
                .subscribe(new Observer<String>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e.toString());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("complete");
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
                .fromArray(datas)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println(s);
                    }


                    @Override
                    public void onComplete() {
                        System.out.println("complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e.toString());
                    }

                });

    }

    /**
     * lift转换方法, 主要适合Retrofit + RxJava的网络请求
     * 在 Observable 执行了 lift(Operator) 方法之后，
     * 会返回一个新的 Observable，这个新的 Observable 会像一个代理一样，
     * 负责接收原始的 Observable 发出的事件，并在处理后发送给 Subscriber。
     */
    @SuppressLint("CheckResult")
    public static void doObserver9() {
        Integer[] fileNames = new Integer[]{1, 23, 144};
        Observable
                .fromArray(fileNames)
                .lift(new ObservableOperator<String, Integer>() {
                    @Override
                    public Observer<? super Integer> apply(final Observer<? super String> observer) throws Exception {
                        return new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                observer.onSubscribe(d);
                            }

                            @Override
                            public void onNext(Integer integer) {
                                observer.onNext(String.valueOf(integer));
                            }

                            @Override
                            public void onError(Throwable e) {
                                observer.onError(e);
                            }

                            @Override
                            public void onComplete() {
                                observer.onComplete();
                            }
                        };
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }

}
