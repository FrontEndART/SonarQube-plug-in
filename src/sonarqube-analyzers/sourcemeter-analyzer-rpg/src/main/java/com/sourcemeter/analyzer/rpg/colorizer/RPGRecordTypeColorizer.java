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
package com.sourcemeter.analyzer.rpg.colorizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.channel.CodeReader;
import org.sonar.colorizer.HtmlCodeBuilder;
import org.sonar.colorizer.NotThreadSafeTokenizer;

/**
 * Detect case-sensitive keywords
 */
public class RPGRecordTypeColorizer extends NotThreadSafeTokenizer {

    private final String tagBefore;
    private final String tagAfter;
    private final Matcher matcher;
    private final StringBuilder tmpBuilder = new StringBuilder();
    private static final String DEFAULT_REGEX = "[DFICOEHdficoeh]{1}";
    public static final int COLUMN_INDEX = 5;

    public RPGRecordTypeColorizer(String tagBefore, String tagAfter) {
        this.tagBefore = tagBefore;
        this.tagAfter = tagAfter;
        this.matcher = Pattern.compile(DEFAULT_REGEX).matcher("");
    }

    @Override
    public boolean consume(CodeReader code, HtmlCodeBuilder codeBuilder) {
        if (code.getColumnPosition() == COLUMN_INDEX
                && code.popTo(matcher, tmpBuilder) > 0) {
            codeBuilder.appendWithoutTransforming(tagBefore);
            codeBuilder.append(tmpBuilder);
            codeBuilder.appendWithoutTransforming(tagAfter);
            tmpBuilder.delete(0, tmpBuilder.length());
            return true;
        }
        return false;
    }

    @SuppressWarnings("PMD")
    @Override
    public RPGRecordTypeColorizer clone() {
        // There's no super.clone() method in this case. Parent class is
        // abstract and has abstract clone() method.

        RPGRecordTypeColorizer clone = new RPGRecordTypeColorizer(tagBefore, tagAfter);
        return clone;
    }
}
