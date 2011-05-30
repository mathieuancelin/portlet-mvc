package com.sample.portlet;

import com.sample.portlet.fwk.Form;
import com.sample.portlet.fwk.annotation.ModelAttribute;
import com.sample.portlet.fwk.annotation.OnAction;
import com.sample.portlet.fwk.annotation.OnRender;
import com.sample.portlet.fwk.annotation.OnSave;
import com.sample.portlet.fwk.annotation.PreferenceAttribute;

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
        UsernameForm form = new UsernameForm();
        form.fillFromRequest();
        form.validate();
        this.username = form.username;
    }

    @OnSave
    public void savePreferences() {
        PrefsForm form = new PrefsForm();
        form.fillFromRequest();
        form.validate();
        upper = form.upper;
    }

    public class UsernameForm extends Form {

        private String username;

        @Override
        public boolean validate() {
            return true;
        }
    }

    public class PrefsForm extends Form {

        private String upper;

        @Override
        public boolean validate() {
            return true;
        }
    }
}


//<portlet:actionURL name="select" var="selectUrl">
//    <portlet:param name="time" value="${dates[8].time}"/>
//</portlet:actionURL>