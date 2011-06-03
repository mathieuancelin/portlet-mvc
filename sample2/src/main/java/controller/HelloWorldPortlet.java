package controller;

import com.sample.portlet.fwk.F.Option;
import com.sample.portlet.fwk.AbstractPortletController.Render;
import com.sample.portlet.fwk.PortletHelper;
import com.sample.portlet.fwk.PortletHelper.Form;
import com.sample.portlet.fwk.PortletHelper.Model;
import com.sample.portlet.fwk.PortletHelper.Preferences;
import com.sample.portlet.fwk.Template;
import com.sample.portlet.fwk.annotation.OnAction;
import com.sample.portlet.fwk.annotation.OnRender;
import com.sample.portlet.fwk.annotation.OnSave;
import javax.portlet.PortletMode;

public class HelloWorldPortlet {

    /**
     * Called on render view phase.
     */
    @OnRender(OnRender.Phase.VIEW)
    public void render(Model model, Preferences prefs) {
        // get username from model
        Option<String> username = model.forKey("username", String.class);
        // get username transformation based on preferences
        UsernameAction action = new UsernameAction(
                prefs.forKey("upper").getOrElse("off").equals("on"));

        if (!username.isDefined()) {
            // if no username in model
            model.attr("username", action.apply("Unknown"));
        } else {
            // if username in model (after submit action)
            model.attr("username", action.apply(username.get()));
        }
        // render a new username form
        new UsernameForm().render();
    }

    /**
     * Called on render edit phase.
     */
    @OnRender(OnRender.Phase.EDIT)
    public void renderEdit() {
        // render a form filled with prefs
        PrefsForm.loadFromPreferences().render();
    }

    /**
     * Called on action named submitUsername.
     */
    @OnAction("submitUsername")
    public void submitUsername() {
        // get form inputs
        UsernameForm form = UsernameForm.loadAndValidateFromRequest();
        // render the username
        Render.attr("username", form.username);
    }

    /**
     * Called on action named savePreferences.
     */
    @OnSave
    public void savePreferences() {
        // get prefs form inputs
        PrefsForm form = PrefsForm.loadAndValidateFromRequest();
        if (form.isValid()) {
            // if it's valid, save it
            form.saveInPreferences();
            // and go back to view
            PortletHelper.setMode(PortletMode.VIEW);
        }
    }

    /**
     * Representation of the username submission form.
     */
    public static class UsernameForm extends Form {

        private String username;

        public static UsernameForm loadAndValidateFromRequest() {
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

    /**
     * Representation of the preference form.
     * This form validates the input values.
     */
    public static class PrefsForm extends Form {

        private String upper;

        public static PrefsForm loadFromPreferences() {
            PrefsForm form = new PrefsForm();
            form.fillFromPreferences();
            return form;
        }

        public static PrefsForm loadAndValidateFromRequest() {
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

    /**
     * Transformation class for the username.
     * Returns an uppercased username based on preferences.
     */
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