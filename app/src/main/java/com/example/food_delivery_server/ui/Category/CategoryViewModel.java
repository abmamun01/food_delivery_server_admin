package com.example.food_delivery_server.ui.Category;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.food_delivery_server.CallBack.ICategoryCallBackListener;
import com.example.food_delivery_server.Commons.Commons;
import com.example.food_delivery_server.Model.CategoryModel;
import com.google.android.gms.common.internal.service.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel implements ICategoryCallBackListener {

    private MutableLiveData<List<CategoryModel>> categoryListMutable;
    private MutableLiveData<String> messageError=new MutableLiveData<>();
    private ICategoryCallBackListener categoryCallBackListener;

    public CategoryViewModel() {

        categoryCallBackListener=this;
    }



    public MutableLiveData<List<CategoryModel>> getCategoryListMutable() {

        if (categoryListMutable==null){
            categoryListMutable=new MutableLiveData<>();
            messageError=new MutableLiveData<>();
            loadCategories();
        }

        return categoryListMutable;
    }

    public void loadCategories() {

        List<CategoryModel> tempList=new ArrayList<>();
        DatabaseReference categoryRef= FirebaseDatabase.getInstance().getReference(Commons.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot itemSnapshot:snapshot.getChildren()){
                    CategoryModel categoryModel=itemSnapshot.getValue(CategoryModel.class);
                    categoryModel.setMenu_id(itemSnapshot.getKey());
                    tempList.add(categoryModel);
                }
                categoryCallBackListener.onCategoryLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                categoryCallBackListener.onCategoryLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }





    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModelList) {

        categoryListMutable.setValue(categoryModelList);
    }

    @Override
    public void onCategoryLoadFailed(String message) {

        messageError.setValue(message);
    }
}