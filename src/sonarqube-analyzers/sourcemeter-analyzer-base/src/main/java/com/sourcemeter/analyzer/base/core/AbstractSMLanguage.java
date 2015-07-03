package com.sourcemeter.analyzer.base.core;

import org.sonar.api.resources.AbstractLanguage;

public abstract class AbstractSMLanguage extends AbstractLanguage {

    public AbstractSMLanguage(String key, String name) {
        super(key, name);
    }

    /**
     * Determines whether a file's language is the current language.
     *
     * @param file
     * @return true if the file is detected to be in the current language. 
     * False otherwise.
     */
    public abstract boolean isFileForCurrentLanguage(java.io.File file);
}
