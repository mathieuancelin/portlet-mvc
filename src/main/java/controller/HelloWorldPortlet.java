package controller;

import com.sample.portlet.fwk.F.Maybe;
import com.sample.portlet.fwk.PortletController.Render;
import com.sample.portlet.fwk.PortletHelper;
import com.sample.portlet.fwk.PortletHelper.Form;
import com.sample.portlet.fwk.PortletHelper.Model;
import com.sample.portlet.fwk.annotation.OnAction;
import com.sample.portlet.fwk.annotation.OnRender;
import com.sample.portlet.fwk.annotation.OnSave;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

public class HelloWorldPortlet {

    @OnRender(OnRender.Phase.VIEW)
    public void render(Model model, PortletPreferences prefs) {
        Maybe<String> username = model.forKey("username", String.class);
        UsernameAction action = new UsernameAction(
                prefs.getValue("upper", "off").equals("on"));
        if (!username.isDefined()) {
            model.attr("username", action.apply("Unknown"));
        } else {
            model.attr("username", action.apply(username.get()));
        }
        new UsernameForm().render();
    }

    @OnRender(OnRender.Phase.EDIT)
    public void renderEdit(PortletPreferences prefs) {
        PrefsForm.fromPreferences().render();
    }

    @OnAction("submitUsername")
    public void submitUsername() {
        UsernameForm form = UsernameForm.validateFromRequest();
        Render.attr("username", form.username);
    }

    @OnSave
    public void savePreferences(PortletPreferences prefs) {
        PrefsForm form = PrefsForm.validateFromRequest();
        if (form.isValid()) {
            form.saveInPreferences();
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

        public static PrefsForm fromPreferences() {
            PrefsForm form = new PrefsForm();
            form.fillFromPreferences();
            return form;
        }

        public static PrefsForm validateFromRequest() {
            PrefsForm form = new PrefsForm();
            form.fillFromRequest();
            form.validateForm();
            return form;
        }

        @Override
        public boolean validate() {
            if (!upper.equals("on") && !upper.equals("off")) {
                Render.err("Value should be on or off");
                return false;
            }
            return true;
        }
    }

    public static class UsernameAction {
        
        private final boolean uppercase;

        public UsernameAction(boolean uppercase) {
            this.uppercase = uppercase;
        }

        public String apply(String value) {
            if (uppercase) {
                return value.toUpperCase();
            } else {
                return value;
            }
        }
    }
}