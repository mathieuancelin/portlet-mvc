package com.sample.portlet.fwk;

import com.sample.portlet.fwk.PortletHelper.Form;
import com.sample.portlet.fwk.PortletHelper.Model;
import com.sample.portlet.fwk.PortletHelper.ParameterizedModel;
import com.sample.portlet.fwk.PortletHelper.Preferences;
import com.sample.portlet.fwk.PortletHelper.RequestParams;
import com.sample.portlet.fwk.PortletHelper.Session;
import com.sample.portlet.fwk.Template.FileUtils.FileGrabber;
import com.sample.portlet.fwk.annotation.ModelAttribute;
import com.sample.portlet.fwk.annotation.OnAction;
import com.sample.portlet.fwk.annotation.OnEvent;
import com.sample.portlet.fwk.annotation.OnRender;
import com.sample.portlet.fwk.annotation.OnRender.Phase;
import com.sample.portlet.fwk.annotation.OnSave;
import com.sample.portlet.fwk.annotation.PreferenceAttribute;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
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

public abstract class AbstractPortletController extends GenericPortlet {

    public static final String MODEL_KEY = "fwk____Model";
    public static final String SAVE_PREFS_KEY = "savePreferences";
    public static final String HANDLED = "handled";
    public static final String WILDCARD = "*";
    public static final String CONTROLLER_KEY = "controller";
    public static final String ACTION = "javax.portlet.action";
    public static final String CUSTOM_VIEW = "fwk___view";
    public static final String CUSTOM_TEMPLATE = "fwk___template";

    public static ThreadLocal<PortletRequest> currentRequest = new ThreadLocal<PortletRequest>();
    public static ThreadLocal<PortletResponse> currentResponse = new ThreadLocal<PortletResponse>();

    public static ThreadLocal<PortletSession> currentPortletSession = new ThreadLocal<PortletSession>();
    public static ThreadLocal<PortletPreferences> currentPortletPrefs = new ThreadLocal<PortletPreferences>();

    public static ThreadLocal<Model> currentModel = new ThreadLocal<Model>();

    public static ThreadLocal<Object> currentController = new ThreadLocal<Object>();

    protected static final String VIEW_PREFIX = "/view/";
    protected static final String VIEW = "/view.jsp";
    protected static final String NORMAL_VIEW = "/view-normal.jsp";
    protected static final String MAXIMIZED_VIEW = "/view-maximized.jsp";
    protected static final String HELP_VIEW = "/help.jsp";
    protected static final String EDIT_VIEW = "/edit.jsp";
    
    protected PortletRequestDispatcher normalView;
    protected PortletRequestDispatcher maximizedView;
    protected PortletRequestDispatcher helpView;
    protected PortletRequestDispatcher editView;
    protected Class<?> actualControllerType;
    protected String controllerName;
    protected String elName;

    protected Template template;

    protected final Map<String, File> files = new HashMap<String, File>();

    @Override
    public final void init(PortletConfig config) throws PortletException {
        super.init(config);
        String ctrl = config.getInitParameter(CONTROLLER_KEY);
        if (ctrl == null) {
            ctrl = "controller." + config.getPortletName();
        }
        try {
            actualControllerType = loadClass(ctrl);
        } catch (Exception e) {
            actualControllerType = EmptyController.class;
        }
        controllerName = config.getPortletName().toLowerCase();
		elName = config.getPortletName().substring(0, 1).toLowerCase() + config.getPortletName().substring(1);
        if (getFile("view.jsp").exists()) {
        normalView = config.getPortletContext()
                .getRequestDispatcher(VIEW_PREFIX + controllerName + VIEW);
        }
        if (normalView == null) {
            if (getFile("view-normal.jsp").exists()) {
            normalView = config.getPortletContext()
                    .getRequestDispatcher(VIEW_PREFIX + controllerName + NORMAL_VIEW);
            }
            if (getFile("view-maximized.jsp").exists()) {
            maximizedView = config.getPortletContext()
                    .getRequestDispatcher(VIEW_PREFIX + controllerName + MAXIMIZED_VIEW);
            }
        } else {
            maximizedView = normalView;
        }
        if (getFile("help.jsp").exists()) {
            helpView = config.getPortletContext()
                .getRequestDispatcher(VIEW_PREFIX + controllerName + HELP_VIEW);
        }
        if (getFile("edit.jsp").exists()) {
            editView = config.getPortletContext()
                .getRequestDispatcher(VIEW_PREFIX + controllerName + EDIT_VIEW);
        }

        template = new Template(this, new FileGrabber() {

            @Override
            public File getFile(String file) {
                return new File(getPortletContext().getRealPath("/view/" + controllerName + "/" + file));
            }

        });
        start(config);
    }

    private File getFile(String path) {
        if (!files.containsKey(path)) {
            files.put(path, new File(getPortletContext()
                    .getRealPath("/view/" + controllerName + "/" + path)));
        }
        return files.get(path);
    }

    public abstract void start(PortletConfig config);

    private Class<?> loadClass(String name) {
        try {
            return getClass().getClassLoader().loadClass(name);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        editView = null;
        stop();
        super.destroy();
    }

    public abstract void stop();

    @Override
    public final void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if (request.getPortletSession().getAttribute(CUSTOM_TEMPLATE) != null) {
            renderTemplate(request, response);
        } else if (request.getPortletSession().getAttribute(CUSTOM_VIEW) != null) {
            renderCustom(request, response);
        } else {
            if (WindowState.MINIMIZED.equals(request.getWindowState())) {
                return;
            }
            if (WindowState.NORMAL.equals(request.getWindowState())) {
                if (normalView == null && getFile("view.html").exists()) {
                    Template.render("view.html");
                    renderTemplate(request, response);
                } else {
                    normalView.include(request, response);
                }
            } else {
                if (maximizedView == null && getFile("view-maximized.html").exists()) {
                    Template.render("view-maximized.html");
                    renderTemplate(request, response);
                } else if (maximizedView == null && getFile("view.html").exists()) {
                    Template.render("view.html");
                    renderTemplate(request, response);
                } else {
                    maximizedView.include(request, response);
                }
            }
        }
    }

    @Override
    public final void doHelp(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if (request.getPortletSession().getAttribute(CUSTOM_TEMPLATE) != null) {
            renderTemplate(request, response);
        } else if (request.getPortletSession().getAttribute(CUSTOM_VIEW) != null) {
            renderCustom(request, response);
        } else {
            if (helpView == null && getFile("help.html").exists()) {
                Template.render("help.html");
                renderTemplate(request, response);
            } else {
                helpView.include(request, response);
            }
        }
    }

    @Override
    public final void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if (request.getPortletSession().getAttribute(CUSTOM_TEMPLATE) != null) {
            renderTemplate(request, response);
        } else if (request.getPortletSession().getAttribute(CUSTOM_VIEW) != null) {
            renderCustom(request, response);
        } else {
            if (editView == null && getFile("edit.html").exists()) {
                Template.render("edit.html");
                renderTemplate(request, response);
            } else {
                editView.include(request, response);
            }
        }
    }

    private void renderCustom(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        String file = (String) request.getPortletSession().getAttribute(CUSTOM_VIEW);
        PortletRequestDispatcher custom =
                getPortletConfig().getPortletContext().getRequestDispatcher(file);
        custom.include(request, response);
    }

    private void renderTemplate(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        String file = (String) request.getPortletSession().getAttribute(CUSTOM_TEMPLATE);
        request.getPortletSession().removeAttribute(CUSTOM_TEMPLATE);
        response.setContentType("text/html");
        try {
            template.render(file, currentModel.get(), response.getPortletOutputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public final void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        initRequest(request, response);
        action();
        storeAttributes();
        endRequest(false);
    }

    @Override
    public final void processEvent(EventRequest request, EventResponse response)
            throws PortletException, IOException {
        initRequest(request, response);
        event();
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
        if (request.getAttribute(HANDLED) == null) {
            request.setAttribute(HANDLED, "true");
            if (currentController.get() == null) {
                currentController.set(getInstance(actualControllerType));
            }
            currentPortletSession.set(request.getPortletSession());
            currentPortletPrefs.set(request.getPreferences());
            Object model = currentPortletSession.get().getAttribute(MODEL_KEY);
            if (model == null) {
                model = new Model();
                currentPortletSession.get().setAttribute(MODEL_KEY, model);
            }
            currentModel.set((Model) model);
            currentModel.get().put(elName, currentController.get());
            currentModel.get().put(Model.ERROR, "");
            currentModel.get().putAll(getManagedBeans(currentModel.get()));
            setControllersFields();
            startRequest(request, response);
        }
    }

    public abstract void startRequest(PortletRequest request, PortletResponse response);

    private void setControllersFields() {
        try {
            for (Field field : actualControllerType.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PreferenceAttribute.class)) {
                    field.set(currentController.get(),
                            currentPortletPrefs.get().getValue(field.getName(), null));
                }
                if (field.isAnnotationPresent(ModelAttribute.class)) {
                    Object value = currentModel.get().get(field.getName());
                    if (field.getAnnotation(ModelAttribute.class).value()) {
                        value = currentPortletSession.get().getAttribute(field.getName());
                    }
                    field.set(currentController.get(), value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeAttributes() {
        try {
            for (Field field : actualControllerType.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(PreferenceAttribute.class)) {
                    Object value = field.get(currentController.get());
                    if (value != null) {
                        currentPortletPrefs.get().setValue(field.getName(), value.toString());
                    }
                }
                if (field.isAnnotationPresent(ModelAttribute.class)) {
                    Object value = field.get(currentController.get());
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

    private void endRequest(boolean render) {
        if (render) {
            stopRequest(currentRequest.get(), currentResponse.get());
            currentPortletSession.get().removeAttribute(CUSTOM_VIEW);
            currentRequest.get().removeAttribute(HANDLED);
            currentRequest.remove();
            currentResponse.remove();
            currentPortletSession.get().removeAttribute(MODEL_KEY);
            currentModel.remove();
            currentController.remove();
            currentPortletSession.remove();
            currentPortletPrefs.remove();
        }
    }

    public abstract void stopRequest(PortletRequest request, PortletResponse response);

    private void render() {
        for (Method m : actualControllerType.getDeclaredMethods()) {
            m.setAccessible(true);
            if (m.isAnnotationPresent(OnRender.class)) {
                if (m.getAnnotation(OnRender.class).value().equals(Phase.ALL) ||
                    m.getAnnotation(OnRender.class).value().toString().toLowerCase()
                        .equals(currentRequest.get().getPortletMode()
                            .toString().toLowerCase())) {
                    try {
                        m.invoke(currentController.get(), getParams(m));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void action() {
        if (currentRequest.get().getParameter(ACTION)
                .equals(SAVE_PREFS_KEY)) {
            save();
        } else {
            for (Method m : actualControllerType.getDeclaredMethods()) {
                m.setAccessible(true);
                if (m.isAnnotationPresent(OnAction.class)) {
                    try {
                        if (m.getAnnotation(OnAction.class).value().equals(WILDCARD)) {
                            m.invoke(currentController.get(), getParams(m));
                        } else {
                            if (currentRequest.get()
                                .getParameter(ACTION)
                                    .equals(m.getAnnotation(OnAction.class).value())) {
                               m.invoke(currentController.get(), getParams(m));
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
        for (Method m : actualControllerType.getDeclaredMethods()) {
            m.setAccessible(true);
            if (m.isAnnotationPresent(OnSave.class)) {
                try {
                    m.invoke(currentController.get(), getParams(m));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void event() {
        for (Method m : actualControllerType.getDeclaredMethods()) {
            m.setAccessible(true);
            if (m.isAnnotationPresent(OnEvent.class)) {
                try {
                    if (m.getAnnotation(OnEvent.class).value().equals(WILDCARD)) {
                        m.invoke(currentController.get(), getParams(m));
                    } else {
                        EventRequest req = (EventRequest) currentRequest.get();
                        if (req.getEvent().getName()
                                .equals(m.getAnnotation(OnEvent.class).value())) {
                           m.invoke(currentController.get(), getParams(m));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object[] getParams(Method m) {
        Object[] params = new Object[m.getParameterTypes().length];
        int i = 0;
        for (Class<?> type : m.getParameterTypes()) {
            params[i] = getParamForType(type, m.getParameterAnnotations()[i]);
            i++;
        }
        return params;
    }

    protected <T> T getParamForType(Class<T> type, Annotation... qualifiers) {
        if(type.equals(PortletRequest.class)) {
            return (T) currentRequest.get();
        }
        if(type.equals(PortletResponse.class)) {
            return (T) currentResponse.get();
        }
        if(type.equals(ActionRequest.class)) {
            return (T) currentRequest.get();
        }
        if(type.equals(ActionResponse.class)) {
            return (T) currentResponse.get();
        }
        if(type.equals(EventRequest.class)) {
            return (T) currentRequest.get();
        }
        if(type.equals(EventResponse.class)) {
            return (T) currentResponse.get();
        }
        if(type.equals(RenderRequest.class)) {
            return (T) currentRequest.get();
        }
        if(type.equals(RenderResponse.class)) {
            return (T) currentResponse.get();
        }
        if(type.equals(PortletSession.class)) {
            return (T) currentPortletSession.get();
        }
        if(type.equals(PortletPreferences.class)) {
            return (T) currentPortletPrefs.get();
        }
        if(type.equals(Model.class)) {
            return (T) currentModel.get();
        }
        if(type.equals(RequestParams.class)) {
            return (T) new RequestParams();
        }
        if(type.equals(Render.class)) {
            return (T) new Render();
        }
        if(type.equals(Session.class)) {
            return (T) new Session();
        }
        if(type.equals(Preferences.class)) {
            return (T) new Preferences();
        }
        Object value = null;
        try {
            value = getInstance(type, qualifiers);
            if (value != null) {
                return (T) value;
            }
        } catch (Exception e) {}
        throw new IllegalStateException("Can't find instance for type " + type);
    }

    /**
     * Override this method for integration with third party DI frameworks.
     */
    public abstract <T> T getInstance(Class<T> clazz, Annotation... qualifiers);

    /**
     * Override this method for integration with third party DI frameworks.
     * The DI managed returned will be accessible from EL.
     */
    public abstract Map<String, Object> getManagedBeans(Model model);

    public static class Render {

        private Render() {}

        public static void form(Form form) {
            form.render();
        }

        public static ParameterizedModel err(String mess) {
            currentModel.get().put(Model.ERROR, mess);
            return new ModelRender();
        }

        public static ParameterizedModel attr(String name, Object value) {
            currentModel.get().put(name, value);
            return new ModelRender();
        }

        public static ParameterizedModel view(String file) {
            AbstractPortletController.currentPortletSession.get()
                .setAttribute(AbstractPortletController.CUSTOM_VIEW, file);
            return new ModelRender();
        }
    }

    public static class ModelRender implements ParameterizedModel {

        private ModelRender() {}

        @Override
        public ParameterizedModel attr(String name, Object value) {
            currentModel.get().put(name, value);
            return this;
        }

        public ParameterizedModel view(String file) {
            AbstractPortletController.currentPortletSession.get()
                .setAttribute(AbstractPortletController.CUSTOM_VIEW, file);
            return this;
        }

        @Override
        public ParameterizedModel rem(String name) {
            currentModel.get().remove(name);
            return this;
        }

        @Override
        public ParameterizedModel err(String mess) {
            currentModel.get().put(Model.ERROR, mess);
            return this;
        }
    }

    protected static class EmptyController {}
}
