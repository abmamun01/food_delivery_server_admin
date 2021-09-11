package com.example.food_delivery_server.EventBus;

import com.example.food_delivery_server.Model.SizeModel;

public class SelecSizeModel {

    private SizeModel sizeModel;

    public SelecSizeModel(SizeModel sizeModel) {
        this.sizeModel = sizeModel;
    }

    public SizeModel getSizeModel() {
        return sizeModel;
    }

    public void setSizeModel(SizeModel sizeModel) {
        this.sizeModel = sizeModel;
    }
}
