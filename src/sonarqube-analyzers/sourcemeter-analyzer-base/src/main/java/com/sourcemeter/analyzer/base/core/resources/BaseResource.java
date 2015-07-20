/**
 * Copyright (c) 2014-2015, FrontEndART Software Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by FrontEndART Software Ltd.
 * 4. Neither the name of FrontEndART Software Ltd. nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY FrontEndART Software Ltd. ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL FrontEndART Software Ltd. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sourcemeter.analyzer.base.core.resources;

import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Scopes;
import org.sonar.api.utils.WildcardPattern;

/**
 * General class for storing a resource.
 */
public abstract class BaseResource extends Resource {

    public static final String BASE_CLASS_QUALIFIER = "SM_CLASS";
    public static final String BASE_METHOD_QUALIFIER = "SM_METHOD";
    public static final String BASE_FUNCTION_QUALIFIER = "SM_FUNC";

    private static final long serialVersionUID = 4587870746785174955L;
    private final String name;
    private final String longName;
    private String description = "";
    private Resource parent = null;

    /**
     * Create resource by key
     *
     * @param key
     */
    public BaseResource(String key) {
        this.name = "";
        this.longName = "";
        setKey(key);
    }

    /**
     * Create resource by all necessary attributes
     *
     * @param key
     * @param fileName
     * @param longName
     */
    public BaseResource(String key, String fileName, String longName) {
        this.name = fileName;
        this.longName = longName;
        if (key == null) {
            throw new IllegalArgumentException("Resource name can not be null");
        }

        setKey(key);
    }

    /**
     * Create resource by all necessary attributes
     *
     * @param key
     * @param name
     * @param parent
     */
    public BaseResource(String key, String name, File parent) {
        this.parent = parent;
        this.name = name;
        this.longName = name;
        setKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLongName() {
        return this.longName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Language getLanguage() {
        return SourceMeterInitializer.getPluginLanguage();
    }

    /**
     * @return Scopes.FILE
     */
    @Override
    public String getScope() {
        return Scopes.FILE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getParent() {
        return this.parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchFilePattern(String antPattern) {
        String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
        WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
        return matcher.match(getKey());
    }

    /**
     * Set description
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set parent
     *
     * @param parentResource
     * @return
     */
    public Resource setParent(Resource parentResource) {
        this.parent = parentResource;
        return this;
    }

    public String toString() {
        return "[id=\"" + this.getKey() + "\", qualifier=\""
                + this.getQualifier() + "\", name=\"" + this.name
                + "\", longName=\"" + this.longName + "\", parent=\""
                + this.parent + "]";
    }
}
