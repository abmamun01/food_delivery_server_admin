package com.example.food_delivery_server.EventBus;

import com.example.food_delivery_server.Model.AddonModel;

import java.util.List;

public class UpdateAdonModel {
    private List<AddonModel> addonModel;


    public UpdateAdonModel() {
    }


    public UpdateAdonModel(List<AddonModel> addonModel) {
        this.addonModel = addonModel;
    }

    public List<AddonModel> getAddonModel() {
        return addonModel;
    }

    public void setAddonModel(List<AddonModel> addonModel) {
        this.addonModel = addonModel;
    }
}
