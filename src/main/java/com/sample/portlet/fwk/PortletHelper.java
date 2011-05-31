package com.sample.portlet.fwk;

import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;

public class PortletHelper {

    private final GenericPortlet portlet;

    public PortletHelper(GenericPortlet portlet) {
        this.portlet = portlet;
    }

    public void setMode(PortletMode mode) {
        try {
            ((ActionResponse) PortletController.currentResponse.get()).setPortletMode(mode);
        } catch (PortletModeException ex) {
            ex.printStackTrace();
        }
    }

    private PortletRequest getRequest() {
        return PortletController.currentRequest.get();
    }
}
