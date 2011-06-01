package controller;

import com.sample.portlet.fwk.PortletHelper;
import com.sample.portlet.fwk.PortletHelper.Form;
import com.sample.portlet.fwk.PortletHelper.Model;
import com.sample.portlet.fwk.annotation.ModelAttribute;
import com.sample.portlet.fwk.annotation.OnAction;
import com.sample.portlet.fwk.annotation.OnRender;
import com.sample.portlet.fwk.annotation.OnSave;
import com.sample.portlet.fwk.annotation.PreferenceAttribute;
import javax.portlet.PortletMode;

public class HelloWorldPortletV2 {

    @ModelAttribute(true)
    private String username;

    @ModelAttribute
    private String uppercase;

    @PreferenceAttribute
    public String upper;

    @OnRender
    public void render(Model model) {
        if (username == null) {
            username = "Unknown";
        }
        if (upper != null && upper.equals("on")) {
            username = username.toUpperCase();
        }
        model.put("max", PortletHelper.getMaximizeRenderURL(PortletMode.EDIT));
        model.put("min", PortletHelper.getMinimizeRenderURL());
        model.put("normal", PortletHelper.getNormalRenderURL());
    }

    @OnRender(OnRender.Phase.EDIT)
    public void renderEdit() {
        if (upper != null) {
            uppercase = upper;
        } else {
            uppercase = "off";
        }
    }

    @OnAction("submitUsername")
    public void submitUsername() {
        UsernameForm form = UsernameForm.validateFromRequest();
        this.username = form.username;
    }

    @OnSave
    public void savePreferences() {
        PrefsForm form = PrefsForm.validateFromRequest();
        if (form.isValid()) {
            upper = form.upper;
            PortletHelper.setMode(PortletMode.VIEW);
        }
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
            form.validateForm();
            return form;
        }

        @Override
        public boolean validate() {
            if (!upper.equals("on") && !upper.equals("off")) {
                getModel().put(Model.ERROR, "Value should be on or off");
                return false;
            }
            return true;
        }
    }
}