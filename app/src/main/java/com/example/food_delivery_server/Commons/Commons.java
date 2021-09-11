package com.example.food_delivery_server.Commons;

import com.example.food_delivery_server.Model.CategoryModel;
import com.example.food_delivery_server.Model.FoodModel;
import com.example.food_delivery_server.Model.ServerUserModel;

public class Commons {
    public static final String SERVIER_REF = "Server";
    public static final String CATEGORY_REF = "Category";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static ServerUserModel currentSerVerUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
}
