package com.example.food_delivery_server.EventBus;

import com.example.food_delivery_server.Model.AddonModel;

public class SelectedAddonModel {
    private AddonModel addonModel;

    public SelectedAddonModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }

    public SelectedAddonModel() {
    }

    public AddonModel getAddonModel() {
        return addonModel;
    }

    public void setAddonModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }
}
