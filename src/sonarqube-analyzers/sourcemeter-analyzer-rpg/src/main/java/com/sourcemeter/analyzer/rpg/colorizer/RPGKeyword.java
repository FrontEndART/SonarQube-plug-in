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

import java.util.Set;
import java.util.TreeSet;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.TokenType;

public enum RPGKeyword implements TokenType {

    ABS("ABS"),
    ACQ("ACQ"), 
    ADD("ADD"), 
    ADDDUR("ADDDUR"), 
    ADDR("ADDR"),
    ALLOC("ALLOC"), 
    AND("AND"), 
    BEGSR("BEGSR"),
    BITAND("BITAND"),
    BITNOT("BITNOT"),
    BITOFF("BITOFF"), 
    BITON("BITON"), 
    BITOR("BITOR"),
    BITXOR("BITXOR"),
    CAB("CAB"), 
    CALL("CALL"), 
    CALLB("CALLB"), 
    CALLP("CALLP"), 
    CAS("CAS"), 
    CAT("CAT"), 
    CHAIN("CHAIN"), 
    CHAR("CHAR"),
    CHECK("CHECK"), 
    CHECKR("CHECKR"), 
    CLEAR("CLEAR"), 
    CLOSE("CLOSE"), 
    COMMIT("COMMIT"), 
    COMP("COMP"), 
    DATE("DATE"),
    DAYS("DAYS"),
    DEALLOC("DEALLOC"), 
    DEC("DEC"),
    DECH("DECH"),
    DECPOS("DECPOS"),
    DEFINE("DEFINE"), 
    DELETE("DELETE"), 
    DIFF("DIFF"),
    DIV("DIV"), 
    DO("DO"),
    DOU("DOU"),
    DOW("DOW"),
    DSPLY("DSPLY"), 
    DUMP("DUMP"), 
    EDITC("EDITC"),
    EDITFLT("EDITFLT"),
    EDITW("EDITW"),
    ELEM("ELEM"),
    ELSE("ELSE"), 
    ELSEIF("ELSEIF"), 
    ENDDO("ENDDO"),
    ENDFOR("ENDFOR"),
    ENDIF("ENDIF"),
    ENDSL("ENDSL"),
    ENDSR("ENDSR"),
    EOF("EOF"),
    EQUAL("EQUAL"),
    ERROR("ERROR"),
    EVAL("EVAL"), 
    EVALR("EVALR"), 
    EXCEPT("EXCEPT"), 
    EXFMT("EXFMT"), 
    EXSR("EXSR"), 
    EXTRCT("EXTRCT"), 
    FEOD("FEOD"), 
    FIELDS("FIELDS"),
    FLOAT("FLOAT"),
    FOR("FOR"),
    FORCE("FORCE"), 
    FOUND("FOUND"),
    GOTO("GOTO"), 
    GRAPH("GRAPH"),
    HOURS("HOURS"),
    IF("IF"),
    IN("IN"), 
    INT("INT"),
    INTH("INTH"),
    ITER("ITER"), 
    KDS("KDS"),
    KFLD("KFLD"), 
    KLIST("KLIST"), 
    LEAVE("LEAVE"), 
    LEAVESR("LEAVESR"), 
    LEN("LEN"),
    LOOKUP("LOOKUP"), 
    MHHZO("MHHZO"), 
    MHLZO("MHLZO"), 
    MINUTES("MINUTES"),
    MLHZO("MLHZO"), 
    MLLZO("MLLZO"), 
    MONITOR("MONITOR"), 
    MONTHS("MONTHS"),
    MOVE("MOVE"), 
    MOVEA("MOVEA"), 
    MOVEL("MOVEL"), 
    MSECONDS("MSECONDS"),
    MULT("MULT"), 
    MVR("MVR"), 
    NEXT("NEXT"), 
    NULLIND("NULLIND"),
    OCCUR("OCCUR"), 
    ON("ON"), 
    OPEN("OPEN"), 
    OR("OR"), 
    OTHER("OTHER"), 
    OUT("OUT"), 
    PADDR("PADDR"),
    PARM("PARM"), 
    PARMS("PARMS"),
    PLIST("PLIST"), 
    POST("POST"), 
    READ("READ"), 
    READC("READC"), 
    READE("READE"), 
    READP("READP"), 
    READPE("READPE"), 
    REALLOC("REALLOC"), 
    REL("REL"), 
    REM("REM"),
    REPLACE("REPLACE"),
    RESET("RESET"), 
    RETURN("RETURN"), 
    ROLBK("ROLBK"), 
    SCAN("SCAN"), 
    SECONDS("SECONDS"),
    SELECT("SELECT"),
    SETGT("SETGT"), 
    SETLL("SETLL"), 
    SETOFF("SETOFF"), 
    SETON("SETON"), 
    SHTDN("SHTDN"), 
    SIZE("SIZE"),
    SORTA("SORTA"), 
    SQRT("SQRT"), 
    STATUS("STATUS"),
    STR("STR"),
    SUB("SUB"), 
    SUBDT("SUBDT"),
    SUBDUR("SUBDUR"), 
    SUBST("SUBST"), 
    TAG("TAG"), 
    TEST("TEST"), 
    TESTB("TESTB"), 
    TESTN("TESTN"), 
    TESTZ("TESTZ"), 
    THIS("THIS"),
    TIME("TIME"), 
    TIMESTAMP("TIMESTAMP"),
    TLOOKUP("TLOOKUP"),
    TRIM("TRIM"),
    TRIML("TRIML"),
    TRIMR("TRIMR"),
    UCS2("UCS2"),
    UNLOCK("UNLOCK"), 
    UNS("UNS"),
    UNSH("UNSH"),
    UPDATE("UPDATE"), 
    WHEN("WHEN"),
    WRITE("WRITE"), 
    XFOOT("XFOOT"), 
    XLATE("XLATE"), 
    YEARS("YEARS"),
    Z_ADD("Z-ADD"), 
    Z_SUB("Z-SUB");

  private final String value;

  private RPGKeyword(String value) {
    this.value = value;
  }

  public String getName() {
    return name();
  }

  public String getValue() {
    return value;
  }

  public boolean hasToBeSkippedFromAst(AstNode node) {
    return false;
  }

  public static String[] keywordValues() {
    RPGKeyword[] keywordsEnum = RPGKeyword.values();
    String[] keywords = new String[keywordsEnum.length];
    for (int i = 0; i < keywords.length; i++) {
      keywords[i] = keywordsEnum[i].getValue();
    }
    return keywords;
  }

    public static Set<String> keywordValuesSet() {
        Set<String> keywords = new TreeSet<String>();
        RPGKeyword[] keywordsEnum = RPGKeyword.values();
        for (int i = 0; i < keywordsEnum.length; i++) {
          keywords.add(keywordsEnum[i].getValue());
        }
        return keywords;
    }

}