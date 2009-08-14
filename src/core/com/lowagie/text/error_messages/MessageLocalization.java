/*
 * $Id: $
 *
 * Copyright 2009 by Paulo Soares.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.error_messages;

import com.lowagie.text.pdf.BaseFont;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Localizes error messages. The messages are located in the package
 * com.lowagie.text.error_messages in the form language_country.lng.
 * The internal file encoding is UTF-8 without any escape chars, it's not a
 * normal property file. See en.lng for more information on the internal format.
 * @author Paulo Soares (psoares@glintt.com)
 */
public class MessageLocalization {
    private static HashMap defaultLanguage = new HashMap();
    private static HashMap currentLanguage;
    private static final String BASE_PATH = "com/lowagie/text/error_messages/";

    private MessageLocalization() {
    }

    static {
        try {
            defaultLanguage = GetLanguageMessages("en", null);
        } catch (Exception ex) {
            // do nothing
        }
        if (defaultLanguage == null)
            defaultLanguage = new HashMap();
    }

    /**
     * Get a message without parameters.
     * @param key the key to the message
     * @return the message
     */
    public static String GetMessage(String key) {
        HashMap cl = currentLanguage;
        String val;
        if (cl != null) {
            val = (String)cl.get(key);
            if (val != null)
                return val;
        }
        cl = defaultLanguage;
        val = (String)cl.get(key);
        if (val != null)
            return val;
        return "No message found for " + key;
    }

    /**
     * Get a message without parameters.
     * @param key the key to the message
     * @return the message
     */
    public static String GetComposedMessage(String key) {
        return GetComposedMessage(key, null, null);
    }

    /**
     * Get a message with one parameter. The parameter will replace the string
     * "{1}" found in the message.
     * @param key the key to the message
     * @param p1 the parameter
     * @return the message
     */
    public static String GetComposedMessage(String key, Object p1) {
        return GetComposedMessage(key, p1, null);
    }

    /**
     * Get a message with two parameters. The parameters will replace the strings
     * "{1}" and "{2}" found in the message.
     * @param key the key to the message
     * @param p1 the parameter
     * @param p2 the parameter
     * @return the message
     */
    public static String GetComposedMessage(String key, Object p1, Object p2) {
        String msg = GetMessage(key);
        if (p1 != null) {
            msg = msg.replaceAll("\\{1\\}", p1.toString());
        }
        if (p2 != null) {
            msg = msg.replaceAll("\\{2\\}", p2.toString());
        }
        return msg;
    }

    /**
     * Sets the language to be used globally for the error messages. The language
     * is a two letter lowercase country designation like "en" or "pt". The country
     * is an optional two letter uppercase code like "US" or "PT".
     * @param language the language
     * @param country the country
     * @return true if the language was found, false otherwise
     * @throws IOException on error
     */
    public static boolean SetLanguage(String language, String country) throws IOException {
        HashMap lang = GetLanguageMessages(language, country);
        if (lang == null)
            return false;
        currentLanguage = lang;
        return true;
    }

    /**
     * Sets the error messages directly from a Reader.
     * @param r the Reader
     * @throws IOException on error
     */
    public static void SetMessages(Reader r) throws IOException {
        currentLanguage = ReadLanguageStream(r);
    }

    private static HashMap GetLanguageMessages(String language, String country) throws IOException {
        if (language == null)
            throw new IllegalArgumentException("The language cannot be null.");
        InputStream is = null;
        try {
            String file;
            if (country != null)
                file = language + "_" + country + ".lng";
            else
                file = language + ".lng";
            is = BaseFont.getResourceStream(BASE_PATH + file, new MessageLocalization().getClass().getClassLoader());
            if (is != null)
                return ReadLanguageStream(is);
            if (country == null)
                return null;
            file = language + ".lng";
            is = BaseFont.getResourceStream(BASE_PATH + file, new MessageLocalization().getClass().getClassLoader());
            if (is != null)
                return ReadLanguageStream(is);
            else
                return null;
        }
        finally {
            try {
                is.close();
            } catch (Exception exx) {
            }
            // do nothing
        }
    }

    private static HashMap ReadLanguageStream(InputStream is) throws IOException {
        return ReadLanguageStream(new InputStreamReader(is, "UTF-8"));
    }

    private static HashMap ReadLanguageStream(Reader r) throws IOException {
        HashMap lang = new HashMap();
        BufferedReader br = new BufferedReader(r);
        String line;
        while ((line = br.readLine()) != null) {
            int idxeq = line.indexOf('=');
            if (idxeq < 0)
                continue;
            String key = line.substring(0, idxeq).trim();
            if (key.startsWith("#"))
                continue;
            lang.put(key, line.substring(idxeq + 1));
        }
        return lang;
    }
}
