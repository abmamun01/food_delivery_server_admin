package com.example.food_delivery_server.ui.FoodList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.food_delivery_server.Commons.Commons;
import com.example.food_delivery_server.Model.FoodModel;
import com.google.android.gms.common.internal.service.Common;

import java.util.List;

public class FoodList_ViewModel extends ViewModel {

    private MutableLiveData<List<FoodModel>> mutableLiveDataFoodList;

    public FoodList_ViewModel() {

    }

    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList() {
        if (mutableLiveDataFoodList == null) {
            mutableLiveDataFoodList = new MutableLiveData<>();
        }
        mutableLiveDataFoodList.setValue(Commons.categorySelected.getFoods());
        return mutableLiveDataFoodList;
    }
}