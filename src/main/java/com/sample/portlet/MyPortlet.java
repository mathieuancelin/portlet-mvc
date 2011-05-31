package com.sample.portlet;

import com.sample.portlet.fwk.Form;
import com.sample.portlet.fwk.PortletHelper;
import com.sample.portlet.fwk.annotation.ModelAttribute;
import com.sample.portlet.fwk.annotation.OnAction;
import com.sample.portlet.fwk.annotation.OnRender;
import com.sample.portlet.fwk.annotation.OnSave;
import com.sample.portlet.fwk.annotation.PreferenceAttribute;
import javax.portlet.PortletMode;

public class MyPortlet {

    @ModelAttribute(true)
    private String username;

    @ModelAttribute
    private String uppercase;

    @PreferenceAttribute
    public String upper;

    @OnRender
    public void render() {
        if (username == null) {
            username = "Unknown";
        }
        if (upper != null && upper.equals("on")) {
            username = username.toUpperCase();
        }
    }

    @OnRender(OnRender.Phase.EDIT)
    public void renderEdit() {
        if (upper != null) {
            uppercase = upper;
        } else {
            uppercase = "off";
        }
    }

    @OnAction("saveUsername")
    public void submitUsername() {
        UsernameForm form = UsernameForm.validateFromRequest();
        this.username = form.username;
    }

    @OnSave
    public void savePreferences(PortletHelper helper) {
        PrefsForm form = PrefsForm.validateFromRequest();
        upper = form.upper;
        helper.setMode(PortletMode.VIEW);
    }

    public static class UsernameForm extends Form {

        private String username;

        public static UsernameForm validateFromRequest() {
            UsernameForm form = new UsernameForm();
            form.fillFromRequest();
            form.validate();
            return form;
        }

        @Override
        public boolean validate() {
            return true;
        }
    }

    public static class PrefsForm extends Form {

        private String upper;

        public static PrefsForm validateFromRequest() {
            PrefsForm form = new PrefsForm();
            form.fillFromRequest();
            form.validate();
            return form;
        }

        @Override
        public boolean validate() {
            return true;
        }
    }
}