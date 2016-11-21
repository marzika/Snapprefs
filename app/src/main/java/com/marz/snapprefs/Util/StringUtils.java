package com.marz.snapprefs.Util;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * <p>Operations on {@link java.lang.String} that are
 * {@code null} safe.</p>
 * <p/>
 * <ul>
 * <li><b>IsEmpty/IsBlank</b>
 * - checks if a String contains text</li>
 * <li><b>Trim/Strip</b>
 * - removes leading and trailing whitespace</li>
 * <li><b>Equals/Compare</b>
 * - compares two strings null-safe</li>
 * <li><b>startsWith</b>
 * - check if a String starts with a prefix null-safe</li>
 * <li><b>endsWith</b>
 * - check if a String ends with a suffix null-safe</li>
 * <li><b>IndexOf/LastIndexOf/Contains</b>
 * - null-safe index-of checks
 * <li><b>IndexOfAny/LastIndexOfAny/IndexOfAnyBut/LastIndexOfAnyBut</b>
 * - index-of any of a set of Strings</li>
 * <li><b>ContainsOnly/ContainsNone/ContainsAny</b>
 * - does String contains only/none/any of these characters</li>
 * <li><b>Substring/Left/Right/Mid</b>
 * - null-safe substring extractions</li>
 * <li><b>SubstringBefore/SubstringAfter/SubstringBetween</b>
 * - substring extraction relative to other strings</li>
 * <li><b>Split/Join</b>
 * - splits a String into an array of substrings and vice versa</li>
 * <li><b>Remove/Delete</b>
 * - removes part of a String</li>
 * <li><b>Replace/Overlay</b>
 * - Searches a String and replaces one String with another</li>
 * <li><b>Chomp/Chop</b>
 * - removes the last part of a String</li>
 * <li><b>AppendIfMissing</b>
 * - appends a suffix to the end of the String if not present</li>
 * <li><b>PrependIfMissing</b>
 * - prepends a prefix to the start of the String if not present</li>
 * <li><b>LeftPad/RightPad/Center/Repeat</b>
 * - pads a String</li>
 * <li><b>UpperCase/LowerCase/SwapCase/Capitalize/Uncapitalize</b>
 * - changes the case of a String</li>
 * <li><b>CountMatches</b>
 * - counts the number of occurrences of one String in another</li>
 * <li><b>IsAlpha/IsNumeric/IsWhitespace/IsAsciiPrintable</b>
 * - checks the characters in a String</li>
 * <li><b>DefaultString</b>
 * - protects against a null input String</li>
 * <li><b>Rotate</b>
 * - rotate (circular shift) a String</li>
 * <li><b>Reverse/ReverseDelimited</b>
 * - reverses a String</li>
 * <li><b>Abbreviate</b>
 * - abbreviates a string using ellipsis</li>
 * <li><b>Difference</b>
 * - compares Strings and reports on their differences</li>
 * <li><b>LevenshteinDistance</b>
 * - the number of changes needed to change one String into another</li>
 * </ul>
 * <p/>
 * <p>The {@code StringUtils} class defines certain words related to
 * String handling.</p>
 * <p/>
 * <ul>
 * <li>null - {@code null}</li>
 * <li>empty - a zero-length string ({@code ""})</li>
 * <li>space - the space character ({@code ' '}, char 32)</li>
 * <li>whitespace - the characters defined by {@link Character#isWhitespace(char)}</li>
 * <li>trim - the characters &lt;= 32 as in {@link String#trim()}</li>
 * </ul>
 * <p/>
 * <p>{@code StringUtils} handles {@code null} input Strings quietly.
 * That is to say that a {@code null} input will return {@code null}.
 * Where a {@code boolean} or {@code int} is being returned
 * details vary by method.</p>
 * <p/>
 * <p>A side effect of the {@code null} handling is that a
 * {@code NullPointerException} should be considered a bug in
 * {@code StringUtils}.</p>
 * <p/>
 * <p>Methods in this class give sample code to explain their operation.
 * The symbol {@code *} is used to indicate any input including {@code null}.</p>
 * <p/>
 * <p>#ThreadSafe#</p>
 *
 * @see java.lang.String
 * @since 1.0
 */
//@Immutable
public class StringUtils {

    /**
     * A String for a space character.
     *
     * @since 3.2
     */
    public static final String SPACE = " ";

    /**
     * The empty String {@code ""}.
     *
     * @since 2.0
     */
    public static final String EMPTY = "";

    /**
     * A String for linefeed LF ("\n").
     *
     * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
     * for Character and String Literals</a>
     * @since 3.2
     */
    public static final String LF = "\n";

    /**
     * A String for carriage return CR ("\r").
     *
     * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
     * for Character and String Literals</a>
     * @since 3.2
     */
    public static final String CR = "\r";

    /**
     * Represents a failed index search.
     *
     * @since 2.1
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;



    // Conversion
    //-----------------------------------------------------------------------

    // Padding
    //-----------------------------------------------------------------------

    /**
     * <p>Repeat a String {@code repeat} times to form a
     * new String.</p>
     * <p/>
     * <pre>
     * StringUtils.repeat(null, 2) = null
     * StringUtils.repeat("", 0)   = ""
     * StringUtils.repeat("", 2)   = ""
     * StringUtils.repeat("a", 3)  = "aaa"
     * StringUtils.repeat("ab", 2) = "abab"
     * StringUtils.repeat("a", -2) = ""
     * </pre>
     *
     * @param str    the String to repeat, may be null
     * @param repeat number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     * {@code null} if null String input
     */
    public static String repeat( final String str, final int repeat ) {
        // Performance tuned for 2.0 (JDK1.4)

        if ( str == null ) {
            return null;
        }
        if ( repeat <= 0 ) {
            return EMPTY;
        }
        final int inputLength = str.length();
        if ( repeat == 1 || inputLength == 0 ) {
            return str;
        }
        if ( inputLength == 1 && repeat <= PAD_LIMIT ) {
            return repeat( str.charAt( 0 ), repeat );
        }

        final int outputLength = inputLength * repeat;
        switch ( inputLength ) {
            case 1:
                return repeat( str.charAt( 0 ), repeat );
            case 2:
                final char ch0 = str.charAt( 0 );
                final char ch1 = str.charAt( 1 );
                final char[] output2 = new char[ outputLength ];
                for ( int i = repeat * 2 - 2; i >= 0; i--, i-- ) {
                    output2[ i ] = ch0;
                    output2[ i + 1 ] = ch1;
                }
                return new String( output2 );
            default:
                final StringBuilder buf = new StringBuilder( outputLength );
                for ( int i = 0; i < repeat; i++ ) {
                    buf.append( str );
                }
                return buf.toString();
        }
    }

    /**
     * <p>Repeat a String {@code repeat} times to form a
     * new String, with a String separator injected each time. </p>
     * <p/>
     * <pre>
     * StringUtils.repeat(null, null, 2) = null
     * StringUtils.repeat(null, "x", 2)  = null
     * StringUtils.repeat("", null, 0)   = ""
     * StringUtils.repeat("", "", 2)     = ""
     * StringUtils.repeat("", "x", 3)    = "xxx"
     * StringUtils.repeat("?", ", ", 3)  = "?, ?, ?"
     * </pre>
     *
     * @param str       the String to repeat, may be null
     * @param separator the String to inject, may be null
     * @param repeat    number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     * {@code null} if null String input
     * @since 2.5
     */
    public static String repeat( final String str, final String separator, final int repeat ) {
        if ( str == null || separator == null ) {
            return repeat( str, repeat );
        }
        // given that repeat(String, int) is quite optimized, better to rely on it than try and splice this into it
        final String result = repeat( str + separator, repeat );
        return removeEnd( result, separator );
    }

    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     * <p/>
     * <pre>
     * StringUtils.repeat('e', 0)  = ""
     * StringUtils.repeat('e', 3)  = "eee"
     * StringUtils.repeat('e', -2) = ""
     * </pre>
     * <p/>
     * <p>Note: this method doesn't not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using {@link #repeat(String, int)} instead.
     * </p>
     *
     * @param ch     character to repeat
     * @param repeat number of times to repeat char, negative treated as zero
     * @return String with repeated character
     * @see #repeat(String, int)
     */
    public static String repeat( final char ch, final int repeat ) {
        if ( repeat <= 0 ) {
            return EMPTY;
        }
        final char[] buf = new char[ repeat ];
        for ( int i = repeat - 1; i >= 0; i-- ) {
            buf[ i ] = ch;
        }
        return new String( buf );
    }

    /**
     * <p>Right pad a String with spaces (' ').</p>
     * <p/>
     * <p>The String is padded to the size of {@code size}.</p>
     * <p/>
     * <pre>
     * StringUtils.rightPad(null, *)   = null
     * StringUtils.rightPad("", 3)     = "   "
     * StringUtils.rightPad("bat", 3)  = "bat"
     * StringUtils.rightPad("bat", 5)  = "bat  "
     * StringUtils.rightPad("bat", 1)  = "bat"
     * StringUtils.rightPad("bat", -1) = "bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size the size to pad to
     * @return right padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String rightPad( final String str, final int size ) {
        return rightPad( str, size, ' ' );
    }

    /**
     * <p>Right pad a String with a specified character.</p>
     * <p/>
     * <p>The String is padded to the size of {@code size}.</p>
     * <p/>
     * <pre>
     * StringUtils.rightPad(null, *, *)     = null
     * StringUtils.rightPad("", 3, 'z')     = "zzz"
     * StringUtils.rightPad("bat", 3, 'z')  = "bat"
     * StringUtils.rightPad("bat", 5, 'z')  = "batzz"
     * StringUtils.rightPad("bat", 1, 'z')  = "bat"
     * StringUtils.rightPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str     the String to pad out, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return right padded String or original String if no padding is necessary,
     * {@code null} if null String input
     * @since 2.0
     */
    public static String rightPad( final String str, final int size, final char padChar ) {
        if ( str == null ) {
            return null;
        }
        final int pads = size - str.length();
        if ( pads <= 0 ) {
            return str; // returns original String when possible
        }
        if ( pads > PAD_LIMIT ) {
            return rightPad( str, size, String.valueOf( padChar ) );
        }
        return str.concat( repeat( padChar, pads ) );
    }

    /**
     * <p>Right pad a String with a specified String.</p>
     * <p/>
     * <p>The String is padded to the size of {@code size}.</p>
     * <p/>
     * <pre>
     * StringUtils.rightPad(null, *, *)      = null
     * StringUtils.rightPad("", 3, "z")      = "zzz"
     * StringUtils.rightPad("bat", 3, "yz")  = "bat"
     * StringUtils.rightPad("bat", 5, "yz")  = "batyz"
     * StringUtils.rightPad("bat", 8, "yz")  = "batyzyzy"
     * StringUtils.rightPad("bat", 1, "yz")  = "bat"
     * StringUtils.rightPad("bat", -1, "yz") = "bat"
     * StringUtils.rightPad("bat", 5, null)  = "bat  "
     * StringUtils.rightPad("bat", 5, "")    = "bat  "
     * </pre>
     *
     * @param str    the String to pad out, may be null
     * @param size   the size to pad to
     * @param padStr the String to pad with, null or empty treated as single space
     * @return right padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String rightPad( final String str, final int size, String padStr ) {
        if ( str == null ) {
            return null;
        }
        if ( padStr.isEmpty() ) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if ( pads <= 0 ) {
            return str; // returns original String when possible
        }
        if ( padLen == 1 && pads <= PAD_LIMIT ) {
            return rightPad( str, size, padStr.charAt( 0 ) );
        }

        if ( pads == padLen ) {
            return str.concat( padStr );
        } else if ( pads < padLen ) {
            return str.concat( padStr.substring( 0, pads ) );
        } else {
            final char[] padding = new char[ pads ];
            final char[] padChars = padStr.toCharArray();
            for ( int i = 0; i < pads; i++ ) {
                padding[ i ] = padChars[ i % padLen ];
            }
            return str.concat( new String( padding ) );
        }
    }

    /**
     * <p>Left pad a String with spaces (' ').</p>
     * <p/>
     * <p>The String is padded to the size of {@code size}.</p>
     * <p/>
     * <pre>
     * StringUtils.leftPad(null, *)   = null
     * StringUtils.leftPad("", 3)     = "   "
     * StringUtils.leftPad("bat", 3)  = "bat"
     * StringUtils.leftPad("bat", 5)  = "  bat"
     * StringUtils.leftPad("bat", 1)  = "bat"
     * StringUtils.leftPad("bat", -1) = "bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size the size to pad to
     * @return left padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String leftPad( final String str, final int size ) {
        return leftPad( str, size, ' ' );
    }

    /**
     * <p>Left pad a String with a specified character.</p>
     * <p/>
     * <p>Pad to a size of {@code size}.</p>
     * <p/>
     * <pre>
     * StringUtils.leftPad(null, *, *)     = null
     * StringUtils.leftPad("", 3, 'z')     = "zzz"
     * StringUtils.leftPad("bat", 3, 'z')  = "bat"
     * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
     * StringUtils.leftPad("bat", 1, 'z')  = "bat"
     * StringUtils.leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str     the String to pad out, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return left padded String or original String if no padding is necessary,
     * {@code null} if null String input
     * @since 2.0
     */
    public static String leftPad( final String str, final int size, final char padChar ) {
        if ( str == null ) {
            return null;
        }
        final int pads = size - str.length();
        if ( pads <= 0 ) {
            return str; // returns original String when possible
        }
        if ( pads > PAD_LIMIT ) {
            return leftPad( str, size, String.valueOf( padChar ) );
        }
        return repeat( padChar, pads ).concat( str );
    }

    /**
     * <p>Left pad a String with a specified String.</p>
     * <p/>
     * <p>Pad to a size of {@code size}.</p>
     * <p/>
     * <pre>
     * StringUtils.leftPad(null, *, *)      = null
     * StringUtils.leftPad("", 3, "z")      = "zzz"
     * StringUtils.leftPad("bat", 3, "yz")  = "bat"
     * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
     * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
     * StringUtils.leftPad("bat", 1, "yz")  = "bat"
     * StringUtils.leftPad("bat", -1, "yz") = "bat"
     * StringUtils.leftPad("bat", 5, null)  = "  bat"
     * StringUtils.leftPad("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param str    the String to pad out, may be null
     * @param size   the size to pad to
     * @param padStr the String to pad with, null or empty treated as single space
     * @return left padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String leftPad( final String str, final int size, String padStr ) {
        if ( str == null ) {
            return null;
        }
        if ( padStr.isEmpty() ) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if ( pads <= 0 ) {
            return str; // returns original String when possible
        }
        if ( padLen == 1 && pads <= PAD_LIMIT ) {
            return leftPad( str, size, padStr.charAt( 0 ) );
        }

        if ( pads == padLen ) {
            return padStr.concat( str );
        } else if ( pads < padLen ) {
            return padStr.substring( 0, pads ).concat( str );
        } else {
            final char[] padding = new char[ pads ];
            final char[] padChars = padStr.toCharArray();
            for ( int i = 0; i < pads; i++ ) {
                padding[ i ] = padChars[ i % padLen ];
            }
            return new String( padding ).concat( str );
        }
    }

    /**
     * Gets a CharSequence length or {@code 0} if the CharSequence is
     * {@code null}.
     *
     * @param cs a CharSequence or {@code null}
     * @return CharSequence length or {@code 0} if the CharSequence is
     * {@code null}.
     * @since 3.0 Changed signature from length(String) to length(CharSequence)
     */
    public static int length( final CharSequence cs ) {
        return cs == null ? 0 : cs.length();
    }

    // Centering
    //-----------------------------------------------------------------------

    /**
     * <p>Centers a String in a larger String of size {@code size}
     * using the space character (' ').</p>
     * <p/>
     * <p>If the size is less than the String length, the String is returned.
     * A {@code null} String returns {@code null}.
     * A negative size is treated as zero.</p>
     * <p/>
     * <p>Equivalent to {@code center(str, size, " ")}.</p>
     * <p/>
     * <pre>
     * StringUtils.center(null, *)   = null
     * StringUtils.center("", 4)     = "    "
     * StringUtils.center("ab", -1)  = "ab"
     * StringUtils.center("ab", 4)   = " ab "
     * StringUtils.center("abcd", 2) = "abcd"
     * StringUtils.center("a", 4)    = " a  "
     * </pre>
     *
     * @param str  the String to center, may be null
     * @param size the int size of new String, negative treated as zero
     * @return centered String, {@code null} if null String input
     */
    public static String center( final String str, final int size ) {
        return center( str, size, ' ' );
    }

    /**
     * <p>Centers a String in a larger String of size {@code size}.
     * Uses a supplied character as the value to pad the String with.</p>
     * <p/>
     * <p>If the size is less than the String length, the String is returned.
     * A {@code null} String returns {@code null}.
     * A negative size is treated as zero.</p>
     * <p/>
     * <pre>
     * StringUtils.center(null, *, *)     = null
     * StringUtils.center("", 4, ' ')     = "    "
     * StringUtils.center("ab", -1, ' ')  = "ab"
     * StringUtils.center("ab", 4, ' ')   = " ab "
     * StringUtils.center("abcd", 2, ' ') = "abcd"
     * StringUtils.center("a", 4, ' ')    = " a  "
     * StringUtils.center("a", 4, 'y')    = "yayy"
     * </pre>
     *
     * @param str     the String to center, may be null
     * @param size    the int size of new String, negative treated as zero
     * @param padChar the character to pad the new String with
     * @return centered String, {@code null} if null String input
     * @since 2.0
     */
    public static String center( String str, final int size, final char padChar ) {
        if ( str == null || size <= 0 ) {
            return str;
        }
        final int strLen = str.length();
        final int pads = size - strLen;
        if ( pads <= 0 ) {
            return str;
        }
        str = leftPad( str, strLen + pads / 2, padChar );
        str = rightPad( str, size, padChar );
        return str;
    }

    /**
     * <p>Centers a String in a larger String of size {@code size}.
     * Uses a supplied String as the value to pad the String with.</p>
     * <p/>
     * <p>If the size is less than the String length, the String is returned.
     * A {@code null} String returns {@code null}.
     * A negative size is treated as zero.</p>
     * <p/>
     * <pre>
     * StringUtils.center(null, *, *)     = null
     * StringUtils.center("", 4, " ")     = "    "
     * StringUtils.center("ab", -1, " ")  = "ab"
     * StringUtils.center("ab", 4, " ")   = " ab "
     * StringUtils.center("abcd", 2, " ") = "abcd"
     * StringUtils.center("a", 4, " ")    = " a  "
     * StringUtils.center("a", 4, "yz")   = "yayz"
     * StringUtils.center("abc", 7, null) = "  abc  "
     * StringUtils.center("abc", 7, "")   = "  abc  "
     * </pre>
     *
     * @param str    the String to center, may be null
     * @param size   the int size of new String, negative treated as zero
     * @param padStr the String to pad the new String with, must not be null or empty
     * @return centered String, {@code null} if null String input
     * @throws IllegalArgumentException if padStr is {@code null} or empty
     */
    public static String center( String str, final int size, String padStr ) {
        if ( str == null || size <= 0 ) {
            return str;
        }
        if ( padStr.isEmpty() ) {
            padStr = SPACE;
        }
        final int strLen = str.length();
        final int pads = size - strLen;
        if ( pads <= 0 ) {
            return str;
        }
        str = leftPad( str, strLen + pads / 2, padStr );
        str = rightPad( str, size, padStr );
        return str;
    }

    /**
     * <p>Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.</p>
     * <p/>
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     * <p/>
     * <pre>
     * StringUtils.removeEnd(null, *)      = null
     * StringUtils.removeEnd("", *)        = ""
     * StringUtils.removeEnd(*, null)      = *
     * StringUtils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
     * StringUtils.removeEnd("www.domain.com", ".com")   = "www.domain"
     * StringUtils.removeEnd("www.domain.com", "domain") = "www.domain.com"
     * StringUtils.removeEnd("abc", "")    = "abc"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     * {@code null} if null String input
     * @since 2.1
     */
    public static String removeEnd( final String str, final String remove ) {
        if (  str.isEmpty() || remove.isEmpty() ) {
            return str;
        }
        if ( str.endsWith( remove ) ) {
            return str.substring( 0, str.length() - remove.length() );
        }
        return str;
    }

    public static String stripKey( String input )
    {
        String finalOutput = input;

        if( finalOutput.contains("https://app.snapchat.com/bq/auth_story_blobs") )
            finalOutput = finalOutput.replace("https://app.snapchat.com/bq/auth_story_blobs", "");
        else if( finalOutput.contains("encoding=compressed") )
        {
            String[] split = input.split("encoding=compressed");

            if( split.length > 0 )
                finalOutput = split[split.length - 1];
        } else if( finalOutput.contains("https://app.snapchat.com/bq/story_blob") ) {
            String[] split = finalOutput.split("&mt=");

            if( split.length > 0 ) {
                finalOutput = split[split.length - 1];
                finalOutput = finalOutput.substring(1);
            }
        }

        if( finalOutput.contains("#") )
        {
            String[] split = finalOutput.split("#");

            if( split.length > 0 )
                finalOutput = split[0];
        }

        return finalOutput;
    }

    public static String obfus(String input) {
        StringBuilder builder = new StringBuilder();
        char[] charArray = input.toCharArray();
        boolean shouldSkip = false;

        for(char character : charArray) {
            if(shouldSkip) {
                builder.append('*');
                shouldSkip = false;
                continue;
            }

            builder.append(character);
            shouldSkip = true;
        }

        return builder.toString();
    }
}