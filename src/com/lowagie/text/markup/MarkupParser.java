/*
 * $Id$
 * $Name$
 *
 * Copyright 2001, 2002 by Bruno Lowagie.
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

package com.lowagie.text.markup;

import java.awt.Color;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class contains several static methods that can be used to parse markup.
 *
 * @author  blowagie
 */
public class MarkupParser {

/** Creates new MarkupParser */
    private MarkupParser() {
    }
    
/**
 * This method parses a String with attributes and returns a Properties object.
 *
 * @param   string   a String of this form: 'key1="value1"; key2="value2";... keyN="valueN" '
 * @return  a Properties object
 */

    public static Properties parseAttributes(String string) {
        Properties result = new Properties();
        if (string == null) return result;
        StringTokenizer keyValuePairs = new StringTokenizer(string, ";");
        StringTokenizer keyValuePair;
        String key;
        String value;
        while (keyValuePairs.hasMoreTokens()) {
            keyValuePair = new StringTokenizer(keyValuePairs.nextToken(), "=");
            if (keyValuePair.hasMoreTokens()) key = keyValuePair.nextToken();
            else continue;
            if (keyValuePair.hasMoreTokens()) value = keyValuePair.nextToken();
            else continue;
            if (value.startsWith("\"")) value = value.substring(1);
            if (value.endsWith("\"")) value = value.substring(0, value.length() - 1);
            result.setProperty(key, value);
        }
        return result;
    }
    
/**
 * Converts a <CODE>Color</CODE> into a HTML representation of this <CODE>Color</CODE>.
 *
 * @param	color	the <CODE>Color</CODE> that has to be converted.
 * @return	the HTML representation of this <COLOR>Color</COLOR>
 */
    
    public static Color decodeColor(String string) {
        int red = 0;
        int green = 0;
        int blue = 0;
        try {
            red = Integer.parseInt(string.substring(1, 3), 16);
            green = Integer.parseInt(string.substring(3, 5), 16);
            blue = Integer.parseInt(string.substring(5), 16);
        }
        catch(Exception sioobe) {
            // empty on purpose
        }
        return new Color(red, green, blue);
    }
}