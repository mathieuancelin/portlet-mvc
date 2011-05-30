package com.sample.portlet.fwk;

import com.sample.portlet.fwk.annotation.ModelAttribute;
import com.sample.portlet.fwk.annotation.OnAction;
import com.sample.portlet.fwk.annotation.OnRender;
import com.sample.portlet.fwk.annotation.OnRender.Phase;
import com.sample.portlet.fwk.annotation.OnSave;
import com.sample.portlet.fwk.annotation.PreferenceAttribute;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

public abstract class PortletController extends GenericPortlet {

    public static final String MODEL_KEY = "fwk____Model";

    public static ThreadLocal<PortletRequest> currentRequest = new ThreadLocal<PortletRequest>();
    public static ThreadLocal<PortletResponse> currentResponse = new ThreadLocal<PortletResponse>();

    public static ThreadLocal<PortletSession> currentPortletSession = new ThreadLocal<PortletSession>();
    public static ThreadLocal<PortletPreferences> currentPortletPrefs = new ThreadLocal<PortletPreferences>();

    public static ThreadLocal<Model> currentModel = new ThreadLocal<Model>();

    private static final String VIEW_PREFIX = "/view/";
    private static final String VIEW = "/view.jsp";
    private static final String NORMAL_VIEW = "/view-normal.jsp";
    private static final String MAXIMIZED_VIEW = "/view-maximized.jsp";
    private static final String HELP_VIEW = "/help.jsp";
    private static final String EDIT_VIEW = "/edit.jsp";
    
    private PortletRequestDispatcher normalView;
    private PortletRequestDispatcher maximizedView;
    private PortletRequestDispatcher helpView;
    private PortletRequestDispatcher editView;
    private Class<? extends PortletController> actualControllerType;
    private String controllerName;

    @Override
    public final void init(PortletConfig config) throws PortletException {
        super.init(config);
        actualControllerType = this.getClass();
        controllerName = actualControllerType.getSimpleName().toLowerCase();
        normalView = config.getPortletContext()
                .getRequestDispatcher(VIEW_PREFIX + controllerName + VIEW);
        if (normalView == null) {
            normalView = config.getPortletContext()
                    .getRequestDispatcher(VIEW_PREFIX + controllerName + NORMAL_VIEW);
            maximizedView = config.getPortletContext()
                    .getRequestDispatcher(VIEW_PREFIX + controllerName + MAXIMIZED_VIEW);
        } else {
            maximizedView = config.getPortletContext()
                    .getRequestDispatcher(VIEW_PREFIX + controllerName + VIEW);
        }
        helpView = config.getPortletContext()
                .getRequestDispatcher(VIEW_PREFIX + controllerName + HELP_VIEW);
        editView = config.getPortletContext()
                .getRequestDispatcher(VIEW_PREFIX + controllerName + EDIT_VIEW);
    }

    @Override
    public final void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        editView = null;
        super.destroy();
    }

    @Override
    public final void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if (WindowState.MINIMIZED.equals(request.getWindowState())) {
            return;
        }
        if (WindowState.NORMAL.equals(request.getWindowState())) {
            normalView.include(request, response);
        } else {
            maximizedView.include(request, response);
        }
    }

    @Override
    public final void doHelp(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        helpView.include(request, response);
    }

    @Override
    protected void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        editView.include(request, response);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) 
            throws PortletException, IOException {
        initRequest(request, response);
        action();
        storeAttributes();
        endRequest(false);
    }

    @Override
    public final void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        initRequest(request, response);
        render();
        storeAttributes();
        super.render(request, response);
        endRequest(true);
    }

    private void initRequest(PortletRequest request, PortletResponse response) {
        currentRequest.set(request);
        currentResponse.set(response);
        currentPortletSession.set(request.getPortletSession());
        currentPortletPrefs.set(request.getPreferences());
        Object model = currentPortletSession.get().getAttribute(MODEL_KEY);
        if (model == null) {
            model = new Model();
            currentPortletSession.get().setAttribute(MODEL_KEY, model);
        }
        currentModel.set((Model) model);
        setControllersFields();
    }

    private void setControllersFields() {
        try {
            for (Field field : actualControllerType.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PreferenceAttribute.class)) {
                    field.set(this, currentPortletPrefs.get().getValue(field.getName(), null));
                }
                if (field.isAnnotationPresent(ModelAttribute.class)) {
                    Object value = currentModel.get().get(field.getName());
                    if (field.getAnnotation(ModelAttribute.class).value()) {
                        value = currentPortletSession.get().getAttribute(field.getName());
                    }
                    field.set(this, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endRequest(boolean render) {
        currentRequest.remove();
        currentResponse.remove();
        if (render) {
            currentPortletSession.get().removeAttribute(MODEL_KEY);
            currentModel.remove();
        }
        currentPortletSession.remove();
        currentPortletPrefs.remove();
    }

    private void storeAttributes() {
        try {
            for (Field field : actualControllerType.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PreferenceAttribute.class)) {
                    Object value = field.get(this);
                    if (value != null) {
                        currentPortletPrefs.get().setValue(field.getName(), value.toString());
                    }
                }
                if (field.isAnnotationPresent(ModelAttribute.class)) {
                    Object value = field.get(this);
                    if (value != null) {
                        currentModel.get().put(field.getName(), value);
                        if (field.getAnnotation(ModelAttribute.class).value()) {
                            currentPortletSession.get().setAttribute(field.getName(), value);
                        }
                    }
                }
            }
            currentPortletPrefs.get().store();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String name : currentModel.get().keySet()) {
            currentRequest.get().setAttribute(name, currentModel.get().get(name));
        }
    }

    private void render() {
        for (Method m : this.getClass().getDeclaredMethods()) {
            m.setAccessible(true);
            if (m.isAnnotationPresent(OnRender.class)) {
                if (m.getAnnotation(OnRender.class).value().equals(Phase.ALL) ||
                    m.getAnnotation(OnRender.class).value().toString().toLowerCase()
                        .equals(currentRequest.get().getPortletMode()
                            .toString().toLowerCase())) {
                    try {
                        m.invoke(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void action() {
        if (currentRequest.get().getParameter("javax.portlet.action")
                .equals("savePreferences")) {
            save();
        } else {
            for (Method m : this.getClass().getDeclaredMethods()) {
                m.setAccessible(true);
                if (m.isAnnotationPresent(OnAction.class)) {
                    try {
                        if (m.getAnnotation(OnAction.class).value().equals("*")) {
                            m.invoke(this);
                        } else {
                            if (currentRequest.get()
                                .getParameter("javax.portlet.action")
                                    .equals(m.getAnnotation(OnAction.class).value())) {
                               m.invoke(this);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void save() {
        for (Method m : this.getClass().getDeclaredMethods()) {
            m.setAccessible(true);
            if (m.isAnnotationPresent(OnSave.class)) {
                try {
                    m.invoke(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
