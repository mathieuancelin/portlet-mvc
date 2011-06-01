package com.sample.portlet.fwk;

import com.sample.portlet.fwk.F.Maybe;
import com.sample.portlet.fwk.F.Option;
import com.sample.portlet.fwk.PortletController.Render;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

public class PortletHelper {

    public static PortletRequest getRequest() {
        return PortletController.currentRequest.get();
    }

    public static PortletResponse getResponse() {
        return PortletController.currentResponse.get();
    }

    public static String getPortletName(final GenericPortlet portlet) {
        return portlet.getPortletName();
    }

    public static PortletConfig getPortletConfig(final GenericPortlet portlet) {
        return portlet.getPortletConfig();
    }

    public static PortletContext getPortletContext(final GenericPortlet portlet) {
        return portlet.getPortletContext();
    }

    public static void setMode(PortletResponse resp, PortletMode mode) {
        try {
            ((ActionResponse) resp).setPortletMode(mode);
        } catch (PortletModeException ex) {
            ex.printStackTrace();
        }
    }

    public static void setMode(PortletMode mode) {
        setMode(getResponse(), mode);
    }

    public static <E> E getService(final Class<E> e) {
        return null;
    }

    public static <E> E getService(final String serviceId, final Class<E> e) {
        return null;
    }

    public static String getRemoteUser() {
        return getRemoteUser(getRequest());
    }

    public static String getRemoteUser(PortletRequest req) {
        return req.getRemoteUser();
    }

    public static <E> E getBean(final String bean, final Class<E> modelClass) {
        return (E) PortletController.currentModel.get().get(bean);
    }

    public static boolean isMaximized() {
        return isMaximized(getRequest());
    }

    public static boolean isMaximized(PortletRequest req) {
        return req.getWindowState().equals(WindowState.MAXIMIZED);
    }

    public static void setWindowState(WindowState state) {
        setWindowState(getRequest(), state);
    }

    public static void setWindowState(PortletRequest req, WindowState state) {
        try {
            ((ActionResponse) req).setWindowState(state);
        } catch (WindowStateException ex) {
            ex.printStackTrace();
        }
    }

    public static String getCurrentPath(GenericPortlet portlet) {
        String webRoot = getPortletContext(portlet).getRealPath("/");
        return webRoot;
    }

    public static String getNamespace(GenericPortlet portlet) {
        return getPortletConfig(portlet).getDefaultNamespace();
    }

    public static void setTitle(String title) {
        setTitle(getResponse(), title);
    }

    public static void setTitle(PortletResponse resp, String title) {
        try {
            ((RenderResponse) resp).setTitle(title);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public static String getRenderURL(
            final PortletMode mode, final WindowState state) {
        return getRenderURL(getResponse(), mode, state);
    }

    public static String getRenderURL(PortletResponse resp,
            PortletMode mode, WindowState state) {
        String url = null;
        final PortletURL renderURL = ((RenderResponse) resp).createRenderURL();
        try {
            if (mode != null) {
                renderURL.setPortletMode(mode);
            }
            if (state != null) {
                renderURL.setWindowState(state);
            }
        } catch (final PortletModeException e) {
            e.printStackTrace();
        } catch (final WindowStateException e) {
            e.printStackTrace();
        }
        url = renderURL.toString();
        return url;
    }

    public static String getMaximizeRenderURL() {
        return getRenderURL(getResponse(), null,
                WindowState.MAXIMIZED);
    }

    public static String getMinimizeRenderURL() {
        return getRenderURL(getResponse(), null,
                WindowState.MINIMIZED);
    }

    public static String getNormalRenderURL() {
        return getRenderURL(getResponse(), null,
                WindowState.NORMAL);
    }

    public static String getMaximizeRenderURL(PortletMode mode) {
        return getRenderURL(getResponse(), mode,
                WindowState.MAXIMIZED);
    }

    public static String getMinimizeRenderURL(PortletMode mode) {
        return getRenderURL(getResponse(), mode,
                WindowState.MINIMIZED);
    }

    public static String getNormalRenderURL(PortletMode mode) {
        return getRenderURL(getResponse(), mode,
                WindowState.NORMAL);
    }

    public static String getMaximizeRenderURL(PortletResponse resp) {
        final String url = getRenderURL(resp, null,
                WindowState.MAXIMIZED);
        return url;
    }

    public static String getMinimizeRenderURL(PortletResponse resp) {
        final String url = getRenderURL(resp, null,
                WindowState.MINIMIZED);
        return url;
    }

    public static String getNormalRenderURL(PortletResponse resp) {
        final String url = getRenderURL(resp, null,
                WindowState.NORMAL);
        return url;
    }

    public static String getMaximizeRenderURL(PortletResponse resp, PortletMode mode) {
        final String url = getRenderURL(resp, mode,
                WindowState.MAXIMIZED);
        return url;
    }

    public static String getMinimizeRenderURL(PortletResponse resp, PortletMode mode) {
        final String url = getRenderURL(resp, mode,
                WindowState.MINIMIZED);
        return url;
    }

    public static String getNormalRenderURL(PortletResponse resp, PortletMode mode) {
        final String url = getRenderURL(resp, mode,
                WindowState.NORMAL);
        return url;
    }

    public static abstract class Form {

        public static final String FORM_KEY_NAME = "form";

        protected boolean valid = false;

        public boolean isValid() {
            return valid;
        }

        public static Model getModel() {
            return PortletController.currentModel.get();
        }

        public Form fillFromRequest() {
            try {
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (!field.getName().equals("valid")) {
                        field.setAccessible(true);
                        Object value = PortletController.currentRequest.get().getParameter(field.getName());
                        field.set(this, value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        public Form fillFromPreferences() {
            try {
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (!field.getName().equals("valid")) {
                        field.setAccessible(true);
                        Object value = PortletController.currentPortletPrefs.get().getValue(field.getName(), null);
                        field.set(this, value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        public Form fillFromSession() {
            try {
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (!field.getName().equals("valid")) {
                        field.setAccessible(true);
                        Object value = PortletController.currentPortletSession.get().getAttribute(field.getName());
                        field.set(this, value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        public void saveInPreferences() {
            try {
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (!field.getName().equals("valid")) {
                        field.setAccessible(true);
                        Object value = field.get(this);
                        if (value != null) {
                            PortletController.currentPortletPrefs.get().setValue(field.getName(), value.toString());
                        }
                    }
                }
                PortletController.currentPortletPrefs.get().store();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void saveInSession() {
            try {
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (!field.getName().equals("valid")) {
                        field.setAccessible(true);
                        Object value = field.get(this);
                        if (value != null) {
                            PortletController.currentPortletSession.get().setAttribute(field.getName(), value);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void validateForm() {
            valid = validate();
        }

        public abstract boolean validate();

        public String toHTML() {
            StringBuilder builder = new StringBuilder();
            builder.append("<table>\n");
            try {
                for (Field field : this.getClass().getDeclaredFields()) {
                    if (!field.getName().equals("valid")) {
                        field.setAccessible(true);
                        Object value = field.get(this);
                        if (field.getType().equals(String.class)) {
                            builder.append("<tr><td>");
                            builder.append(field.getName());
                            builder.append(" : </td><td><input type=\"text\" name=\"");
                            builder.append(field.getName());
                            builder.append("\"");
                            if (value != null) {
                                builder.append(" value=\"");
                                builder.append(value.toString());
                                builder.append("\" ");
                            }
                            builder.append("/></td></tr>\n");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            builder.append("<tr><td><input type=\"submit\" label=\"Submit\"/></td></tr>\n");
            builder.append("</table>\n");
            return builder.toString();
        }

        public void render() {
            Render.attr(FORM_KEY_NAME, toHTML());
        }
    }

    public static class Model extends HashMap<String, Object> implements ParameterizedModel {

        public static final String ERROR = "error";

        public Maybe<Object> forKey(String key) {
            return new Maybe<Object>(super.get(key));
        }

        public <T> Maybe<T> forKey(String key, Class<T> type) {
            return new Maybe<T>((T) super.get(key));
        }

        @Override
        public ParameterizedModel attr(String name, Object value) {
            super.put(name, value);
            return this;
        }

        @Override
        public ParameterizedModel rem(String name) {
            super.remove(name);
            return this;
        }

        @Override
        public ParameterizedModel err(String mess) {
            super.put(ERROR, mess);
            return this;
        }
    }

    public static class RequestParams {

        private PortletRequest getRequest() {
            return PortletController.currentRequest.get();
        }

        public Option<String> parameter(String name) {
            return new Maybe<String>(getRequest().getParameter(name));
        }

        public Option<List<String>> parameterValues(String name) {
            String[] values = getRequest().getParameterValues(name);
            if (values == null) {
                return new Maybe<List<String>>(new ArrayList<String>());
            }
            return new Maybe<List<String>>(Arrays.asList(values));
        }

        public Map<String, String[]> parameters() {
            return getRequest().getParameterMap();
        }

        public List<String> parametersNames() {
            return Collections.list(getRequest().getParameterNames());
        }
    }
    
    public static interface ParameterizedModel {
        ParameterizedModel err(String mess);
        ParameterizedModel attr(String name, Object value);
        ParameterizedModel rem(String name);
    }
}
