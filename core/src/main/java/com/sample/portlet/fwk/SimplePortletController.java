package com.sample.portlet.fwk;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

public class SimplePortletController extends AbstractPortletController {

    @Override
    public void start(PortletConfig config) {
        // nothing to do here
    }

    @Override
    public void stop() {
        // nothing to do here
    }

    @Override
    public void startRequest(PortletRequest request, PortletResponse response) {
        // nothing to do here
    }

    @Override
    public void stopRequest(PortletRequest request, PortletResponse response) {
        // nothing to do here
    }

    @Override
    public <T> T getInstance(Class<T> clazz, Annotation... qualifiers) {
        try {
            return (T) clazz.newInstance();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getManagedBeans() {
        return Collections.emptyMap();
    }
}
