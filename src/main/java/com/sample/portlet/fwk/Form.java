package com.sample.portlet.fwk;

import java.lang.reflect.Field;

public abstract class Form {

    public Form fillFromRequest() {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = PortletController.currentRequest.get().getParameter(field.getName());
                field.set(this, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public Form fillFromPreferences() {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = PortletController.currentPortletPrefs.get().getValue(field.getName(), null);
                field.set(this, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public Form fillFromSession() {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = PortletController.currentPortletSession.get().getAttribute(field.getName());
                field.set(this, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public void saveInPreferences() {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value != null) {
                    PortletController.currentPortletPrefs.get().setValue(field.getName(), value.toString());
                }
            }
            PortletController.currentPortletPrefs.get().store();
            System.out.println("stored");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveInSession() {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value != null) {
                    PortletController.currentPortletSession.get().setAttribute(field.getName(), value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract boolean validate();
}
