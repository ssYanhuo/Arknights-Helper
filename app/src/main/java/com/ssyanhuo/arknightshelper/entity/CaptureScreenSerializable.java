package com.ssyanhuo.arknightshelper.entity;

import com.ssyanhuo.arknightshelper.module.Hr;
import com.ssyanhuo.arknightshelper.service.OverlayService;

import java.io.Serializable;

public class CaptureScreenSerializable implements Serializable {
    private Hr module;
    private OverlayService service;

    public void setModule(Hr module) {
        this.module = module;
    }

    public Hr getModule() {
        return module;
    }

    public void setService(OverlayService service) {
        this.service = service;
    }

    public OverlayService getService() {
        return service;
    }
}

