/*
 * SonarQube C# Plugin
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sourcemeter.analyzer.csharp.colorizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.CppDocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.LiteralTokenizer;
import org.sonar.colorizer.RegexpTokenizer;
import org.sonar.colorizer.Tokenizer;

import com.google.common.collect.ImmutableSet;
import com.sourcemeter.analyzer.csharp.core.CSharp;

public class CSharpColorizer extends CodeColorizerFormat {

    private static final Set<String> KEYWORDS = ImmutableSet.of(
        "abstract",
        "as",
        "base",
        "bool",
        "break",
        "byte",
        "case",
        "catch",
        "char",
        "checked",
        "class",
        "const",
        "continue",
        "decimal",
        "default",
        "delegate",
        "do",
        "double",
        "else",
        "enum",
        "event",
        "explicit",
        "extern",
        "false",
        "finally",
        "fixed",
        "float",
        "for",
        "foreach",
        "goto",
        "if",
        "implicit",
        "in",
        "int",
        "interface",
        "internal",
        "is",
        "lock",
        "long",
        "namespace",
        "new",
        "null",
        "object",
        "operator",
        "out",
        "override",
        "params",
        "private",
        "protected",
        "public",
        "readonly",
        "ref",
        "return",
        "sbyte",
        "sealed",
        "short",
        "sizeof",
        "stackalloc",
        "static",
        "string",
        "struct",
        "switch",
        "this",
        "throw",
        "true",
        "try",
        "typeof",
        "uint",
        "ulong",
        "unchecked",
        "unsafe",
        "ushort",
        "using",
        "virtual",
        "void",
        "volatile",
        "while");
    private static final String SPAN = "</span>";

    public CSharpColorizer() {
        super(CSharp.KEY);
    }

    @Override
    public List<Tokenizer> getTokenizers() {
        List<Tokenizer> tokenizers = new ArrayList<Tokenizer>();
        tokenizers.add(new CDocTokenizer("<span class=\"cd\">", SPAN));
        tokenizers.add(new CppDocTokenizer("<span class=\"cppd\">", SPAN));
        tokenizers.add(new KeywordsTokenizer("<span class=\"k\">", SPAN, KEYWORDS));
        tokenizers.add(new LiteralTokenizer("<span class=\"s\">", SPAN));
        tokenizers.add(new RegexpTokenizer("<span class=\"j\">", SPAN, "#[^\\n\\r]*+"));
        tokenizers.add(new RegexpTokenizer("<span class=\"c\">", SPAN, "[+-]?[0-9]++(\\.[0-9]*+)?"));
        return tokenizers;
    }
}