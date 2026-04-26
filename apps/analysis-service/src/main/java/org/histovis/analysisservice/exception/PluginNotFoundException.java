package org.histovis.analysisservice.exception;

public class PluginNotFoundException extends RuntimeException {

    public PluginNotFoundException(String code) {
        super("Plugin not found with code: " + code);
    }
}
