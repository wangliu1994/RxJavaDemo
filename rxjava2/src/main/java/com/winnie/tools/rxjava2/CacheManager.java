package com.winnie.tools.rxjava2;

/**
 * Created by winnie on 2018/9/17.
 */

public class CacheManager {
    private static final CacheManager ourInstance = new CacheManager();
    private FoodList foodListData;

    public static CacheManager getInstance() {
        return ourInstance;
    }

    private CacheManager() {
    }

    public FoodList getFoodListData() {
        return foodListData;
    }

    public void setFoodListData(FoodList foodListData) {
        this.foodListData = foodListData;
    }
}
