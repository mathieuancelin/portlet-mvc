package com.sample.portlet.fwk;

import java.util.Locale;
import java.util.ResourceBundle;
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

    private final GenericPortlet portlet;

    public PortletHelper(GenericPortlet portlet) {
        this.portlet = portlet;
    }

    public static PortletRequest getRequest() {
        return PortletController.currentRequest.get();
    }

    public static PortletResponse getResponse() {
        return PortletController.currentResponse.get();
    }

    public String getPortletName() {
        return portlet.getPortletName();
    }

    public PortletConfig getPortletConfig() {
        return portlet.getPortletConfig();
    }

    public PortletContext getPortletContext() {
        return portlet.getPortletContext();
    }

    public ResourceBundle getPortletResourceBundle(Locale locale) {
        return portlet.getResourceBundle(locale);
    }

    public static void setMode(PortletMode mode) {
        try {
            ((ActionResponse) getResponse()).setPortletMode(mode);
        } catch (PortletModeException ex) {
            ex.printStackTrace();
        }
    }

    public static <E> E getService(final Class<E> e) {
        return null;
    }

    public static <E> E getService(final String serviceId, final Class<E> e) {
        return null;
    }

    public static String getRemoteUser() {
        return getRequest().getRemoteUser();
    }

    public static <E> E getBean(final String bean, final Class<E> modelClass) {
        return (E) PortletController.currentModel.get().get(bean);
    }

    public static boolean isMaximized() {
        return getRequest().getWindowState().equals(WindowState.MAXIMIZED);
    }

    public static void setWindowState(final WindowState state) {
        try {
            ((ActionResponse) getRequest()).setWindowState(state);
        } catch (WindowStateException ex) {
            ex.printStackTrace();
        }
    }

    public String getCurrentPath() {
        String webRoot = getPortletContext().getRealPath("/");
        return webRoot;
    }

    public String getNamespace() {
        return getPortletConfig().getDefaultNamespace();
    }

    public static void setTitle(final String title) {
        ((RenderResponse) getResponse()).setTitle(title);
    }

    public static TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public static String getRenderURL(
            final PortletMode mode, final WindowState state) {
        String url = null;
        final PortletResponse portletResponse = getResponse();
        final PortletURL renderURL = ((RenderResponse) portletResponse).createRenderURL();
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
        final String url = getRenderURL(null,
                WindowState.MAXIMIZED);
        return url;
    }

    public static String getMinimizeRenderURL() {
        final String url = getRenderURL(null,
                WindowState.MINIMIZED);
        return url;
    }

    public static String getNormalRenderURL() {
        final String url = getRenderURL(null,
                WindowState.NORMAL);
        return url;
    }

    public static String getMaximizeRenderURL(PortletMode mode) {
        final String url = getRenderURL(mode,
                WindowState.MAXIMIZED);
        return url;
    }

    public static String getMinimizeRenderURL(PortletMode mode) {
        final String url = getRenderURL(mode,
                WindowState.MINIMIZED);
        return url;
    }

    public static String getNormalRenderURL(PortletMode mode) {
        final String url = getRenderURL(mode,
                WindowState.NORMAL);
        return url;
    }

    public static void render(String file) {
        PortletController.currentPortletSession.get().setAttribute(PortletController.CUSTOM_VIEW, file);
    }
}
