package com.sample.portlet.fwk;

import com.sample.portlet.fwk.Template.FileUtils.FileGrabber;
import groovy.text.SimpleTemplateEngine;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.portlet.GenericPortlet;
import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletURL;

public class Template {

    private static final Pattern listPattern = Pattern.compile("\\#\\{list items:[^/]+, as:'[^']+'\\}");
    private static final Pattern extendsPatter = Pattern.compile("\\#\\{extends '[^']+' /\\}");
    private static final Pattern setPattern = Pattern.compile("\\#\\{set [^/]+:'[^']+' /\\}");
    private static final Pattern getPattern = Pattern.compile("\\#\\{get [^/]+ /\\}");
    private static final Pattern linkPattern = Pattern.compile("\\@\\{'[^']+'\\}");
    private static final Pattern linkRender = Pattern.compile("\\@render\\{'[^']+'\\}");
    private static final Pattern linkAction = Pattern.compile("\\@action\\{'[^']+'\\}");
    private final SimpleTemplateEngine engine;
    private final ConcurrentHashMap<String, groovy.text.Template> templates =
            new ConcurrentHashMap<String, groovy.text.Template>();

    private final GenericPortlet portlet;

    private final FileGrabber grabber;

    public Template(GenericPortlet portlet, FileGrabber grabber) {
        this.engine = new SimpleTemplateEngine();
        this.portlet = portlet;
        this.grabber = grabber;
    }

    public static void render(String path) {
        AbstractPortletController.currentPortletSession.get()
                .setAttribute(AbstractPortletController.CUSTOM_TEMPLATE, path);
    }

    public Writer render(File file, Map<String, Object> context, OutputStream os) throws Exception {
        return renderWithGroovy(file, context, os);
    }

    public Writer render(String file, Map<String, Object> context, OutputStream os) throws Exception {
        return renderWithGroovy(grabber.getFile(file), context, os);
    }
    
    private Writer renderWithGroovy(File file, Map<String, Object> context, OutputStream os) throws Exception {
        // TODO : if file not exists, return 404
        OutputStreamWriter osw = new OutputStreamWriter(os);
        if (!templates.containsKey(file.getAbsolutePath())) {
            String code = FileUtils.readFileAsString(file);
            templates.putIfAbsent(file.getAbsolutePath(), 
                    engine.createTemplate(enhanceCode(code, portlet, grabber)));
        }
        return templates.get(file.getAbsolutePath()).make(context).writeTo(osw);
    }

    private static String enhanceCode(String code, GenericPortlet portlet, FileGrabber grabber) {
        // TODO : custom tags, links, optimize :)
        List<String> before = new ArrayList<String>();
        String custom = code.replace("%{", "<%").replace("}%", "%>").replace("$.", "\\$.").replace("$(", "\\$(").replace("#{/list}", "<% } %>").replace("#{/list }", "<% } %>");
        Matcher matcher = listPattern.matcher(custom);
        while (matcher.find()) {
            String group = matcher.group();
            String list = group;
            list = list.replace("#{list items:", "<% ").replace(", as:'", ".each { ").replace(",as:'", ".each { ").replace("'}", " -> %>").replace("' }", " -> %>");
            custom = custom.replace(group, list);
        }
        Matcher setMatcher = setPattern.matcher(custom);
        while (setMatcher.find()) {
            String group = setMatcher.group();
            String name = group;
            name = name.replace("#{set ", "").replaceAll(":'[^']+' /\\}", "");
            String value = group;
            value = group.replaceAll("\\#\\{set [^/]+:'", "").replace("' /}", "");
            custom = custom.replace(group, "");
            before.add("<% " + name + " = '" + value + "' %>\n");
        }
        Matcher getMatcher = getPattern.matcher(custom);
        while (getMatcher.find()) {
            String group = getMatcher.group();
            String name = group;
            name = name.replace("#{get ", "").replace(" /}", "");
            custom = custom.replace(group, "${" + name + "}");
        }
        Matcher linkMatcher = linkPattern.matcher(custom);
        while (linkMatcher.find()) {
            String group = linkMatcher.group();
            String link = group;
            link = link.replace("@{'", "").replace("'}", "");
            if (!"/".equals(PortletHelper.getCurrentPath(portlet))) {
                link = PortletHelper.getCurrentPath(portlet) + link;
            }
            custom = custom.replace(group, link);
        }
        //////
        Matcher renderMatcher = linkRender.matcher(custom);
        while (renderMatcher.find()) {
            String group = renderMatcher.group();
            String link = group;
            link = link.replace("@render{'", "").replace("'}", "");
            PortletURL url = ((MimeResponse) AbstractPortletController.currentResponse.get()).createRenderURL();
            try {
            if (link.toLowerCase().equals("view")) {
                url.setWindowState(AbstractPortletController.currentRequest.get().getWindowState());
                url.setPortletMode(PortletMode.VIEW);
                link = url.toString();
            } else if (link.toLowerCase().equals("edit")) {
                url.setWindowState(AbstractPortletController.currentRequest.get().getWindowState());
                url.setPortletMode(PortletMode.EDIT);
                link = url.toString();
            } else if (link.toLowerCase().equals("help")) {
                url.setWindowState(AbstractPortletController.currentRequest.get().getWindowState());
                url.setPortletMode(PortletMode.HELP);
                link = url.toString();
            } else {
                link = "#";
            }
            } catch (Exception e) { e.printStackTrace(); }
            custom = custom.replace(group, link);
        }
        Matcher actionMatcher = linkAction.matcher(custom);
        while (actionMatcher.find()) {
            String group = actionMatcher.group();
            String link = group;
            link = link.replace("@action{'", "").replace("'}", "");
            PortletURL url = ((MimeResponse) AbstractPortletController.currentResponse.get()).createActionURL();
            url.setParameter("javax.portlet.action", link);
            link = url.toString();
            custom = custom.replace(group, link);
        }
        /////
        Matcher extendsMatcher = extendsPatter.matcher(custom);
        custom = custom.replaceAll("\\#\\{extends '[^']+' /\\}", "");
        while (extendsMatcher.find()) {
            String group = extendsMatcher.group();
            String fileName = group;
            fileName = fileName.replace("#{extends '", "").replace("' /}", "");
            File file = grabber.getFile(fileName);
            String parentCode = FileUtils.readFileAsString(file);
            String parentCustomCode = enhanceCode(parentCode, portlet, grabber);
            String[] parts = parentCustomCode.split("\\#\\{doLayout /\\}");
            if (parts.length > 2) {
                throw new RuntimeException("Can't have #{doLayout /} more than one time in a template.");
            }
            String finalCode = parts[0] + custom + parts[1];
            for (String bef : before) {
                finalCode = bef + finalCode;
            }
            return finalCode;
        }
        for (String bef : before) {
            custom = bef + custom;
        }
        return custom;
    }

    public static class FileUtils {

        private static final int BUFFER_SIZE = 1024;

        public static String readFileAsString(File file) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder data = readFromBufferedReader(reader);
                reader.close();
                return new String(data.toString().getBytes(), "utf-8");
            } catch (IOException ex) {
                throw new RuntimeException("File " + file + " not found.");
            }
        }

        private static StringBuilder readFromBufferedReader(BufferedReader reader) throws IOException {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[BUFFER_SIZE];
            int numRead = 0;
            while ((numRead = reader.read(buffer)) != -1) {
                builder.append(String.valueOf(buffer, 0, numRead));
                buffer = new char[BUFFER_SIZE];
            }
            return builder;
        }

        public interface FileGrabber {

            File getFile(String file);
        }
    }
}
