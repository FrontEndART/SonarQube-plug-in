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

import com.sourcemeter.analyzer.rpg.core.RPG;

import java.util.List;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.RegexpTokenizer;
import org.sonar.colorizer.Tokenizer;

import com.google.common.collect.Lists;

public final class RPGColorizer extends CodeColorizerFormat {

    private List<Tokenizer> tokenizers;
    private static final String END_TAG = "</span>";
    private static final String K_CLASS_START_TAG = "<span class=\"k\">";
    private static final String S_CLASS_START_TAG = "<span class=\"s\">";

    public RPGColorizer() {
        super(RPG.KEY);
    }

    @Override
    public List<Tokenizer> getTokenizers() {
        if (tokenizers == null) {
            tokenizers = Lists.newArrayList();

            tokenizers.add(new RPGRecordTypeColorizer(K_CLASS_START_TAG, END_TAG));
            tokenizers.add(new RPGCommentTokenizer("<span class=\"cd\">", END_TAG));
            tokenizers.add(new RPGKeywordsTokenizer(K_CLASS_START_TAG, END_TAG));
            tokenizers.add(new RPGDocStringTokenizer(S_CLASS_START_TAG,END_TAG));

            KeywordsTokenizer defaultKeywordsTokenizer = new KeywordsTokenizer(
                    K_CLASS_START_TAG, END_TAG, RPGKeyword.keywordValuesSet());
            defaultKeywordsTokenizer.setCaseInsensitive(true);

            tokenizers.add(defaultKeywordsTokenizer);
            tokenizers.add(new RegexpTokenizer(K_CLASS_START_TAG, END_TAG,
                    "[*]{1}[a-zA-Z0-9_\\-]*+"));
            tokenizers.add(new RegexpTokenizer(S_CLASS_START_TAG, END_TAG,
                    " \\d+[a-zA-Z_\\-]*"));
        }
        return tokenizers;
    }

}