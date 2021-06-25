package com.zrlog.plugin.message;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class Plugin {

    private String id;
    private String version;
    private String name;
    private Set<String> paths = new LinkedHashSet<>();
    private Set<String> actions = new LinkedHashSet<>();
    private String desc;
    private String author;
    private String shortName;
    private String indexPage;
    private String previewImageBase64;
    private Set<String> services = new LinkedHashSet<>();
    private Set<String> dependentService = new LinkedHashSet<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getIndexPage() {
        return indexPage;
    }

    public void setIndexPage(String indexPage) {
        this.indexPage = indexPage;
    }

    public Set<String> getPaths() {
        return paths;
    }

    public void setPaths(Set<String> paths) {
        this.paths = paths;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    public Set<String> getServices() {
        return services;
    }

    public void setServices(Set<String> services) {
        this.services = services;
    }

    public Set<String> getDependentService() {
        return dependentService;
    }

    public void setDependentService(Set<String> dependentService) {
        this.dependentService = dependentService;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPreviewImageBase64() {
        return previewImageBase64;
    }

    public void setPreviewImageBase64(String previewImageBase64) {
        this.previewImageBase64 = previewImageBase64;
    }
}
