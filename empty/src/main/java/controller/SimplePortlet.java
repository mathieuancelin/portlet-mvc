package controller;

import com.sample.portlet.fwk.PortletController.Render;
import com.sample.portlet.fwk.annotation.OnRender;

public class SimplePortlet {

    /**
     * Called on render view phase.
     */
    @OnRender
    public void render() {
        Render.attr("message", "Hello World !!!");
    }
}