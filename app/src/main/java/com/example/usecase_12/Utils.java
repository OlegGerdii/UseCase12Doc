package com.example.usecase_12;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final int INDEX_NOT_FOUND = -1;
    private static final String EMPTY = "";

    /**
     * For a string takes its substring and appends string appendToEnd to it.
     * Substring's end is either whitespace character ' ', or character with index equal to upper
     * parameter (whatever happens first).
     *
     * @param  str  a string
     * @param  lower index starting from which whitespace should be looked for
     * @param  upper end index of substring, if whitespace wasn't found before it
     * @param  appendToEnd a string appended to the end of the result
     * @return the string obtained from str by taking its substring and appending string appendToEnd
     */
    public static String abbreviate(final String str, int lower, int upper, final String appendToEnd) {
        isTrue(upper >= -1, "upper value cannot be less than -1");
        isTrue(upper >= lower || upper == -1, "upper value is less than lower value");
        if (isEmpty(str)) {
            return str;
        }

        if (lower > str.length()) {
            lower = str.length();
        }

        if (upper == -1 || upper > str.length()) {
            upper = str.length();
        }

        final StringBuilder result = new StringBuilder();
        final int index = indexOf(str, " ", lower);
        if (index == -1) {
            result.append(str, 0, upper);
            if (upper != str.length()) {
                result.append(defaultString(appendToEnd));
            }
        } else {
            result.append(str, 0, Math.min(index, upper));
            result.append(defaultString(appendToEnd));
        }

        return result.toString();
    }

    /**
     * Divides a string into words by given delimiters.
     * Then for each word its first character is added to the result.
     * For example, for input "Banana cat window" and delimeter ' ' the result is "Bcw".
     *
     * @param  str  a string
     * @param  delimiters  characters that indicate word end
     * @return the string that contains the first characters of the words in the input str
     */
    public static String initials(final String str, final char... delimiters) {
        if (isEmpty(str)) {
            return str;
        }
        if (delimiters != null && delimiters.length == 0) {
            return EMPTY;
        }
        final Set<Integer> delimiterSet = generateDelimiterSet(delimiters);
        final int strLen = str.length();
        final int[] newCodePoints = new int[strLen / 2 + 1];
        int count = 0;
        boolean lastWasGap = true;
        for (int i = 0; i < strLen; ) {
            final int codePoint = str.codePointAt(i);

            if (delimiterSet.contains(codePoint) || delimiters == null && Character.isWhitespace(codePoint)) {
                lastWasGap = true;
            } else if (lastWasGap) {
                newCodePoints[count++] = codePoint;
                lastWasGap = false;
            }

            i += Character.charCount(codePoint);
        }
        return new String(newCodePoints, 0, count);
    }

    /**
     * For each character of the given string adds this character in other case to the result.
     * For example, for input "BaNaNa" the result is "bAnAnA"/
     *
     * @param  str  a string
     * @return the string with same characters as the input str, but each in opposite case
     */
    public static String swapCase(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        final int strLen = str.length();
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        boolean whitespace = true;
        for (int index = 0; index < strLen; ) {
            final int oldCodepoint = str.codePointAt(index);
            final int newCodePoint;
            if (Character.isUpperCase(oldCodepoint) || Character.isTitleCase(oldCodepoint)) {
                newCodePoint = Character.toLowerCase(oldCodepoint);
                whitespace = false;
            } else if (Character.isLowerCase(oldCodepoint)) {
                if (whitespace) {
                    newCodePoint = Character.toTitleCase(oldCodepoint);
                    whitespace = false;
                } else {
                    newCodePoint = Character.toUpperCase(oldCodepoint);
                }
            } else {
                whitespace = Character.isWhitespace(oldCodepoint);
                newCodePoint = oldCodepoint;
            }
            newCodePoints[outOffset++] = newCodePoint;
            index += Character.charCount(newCodePoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }


    /**
     * Breaks a string into lines, inserting new line string delimeter each time given substring
     * found or current substring has certain length
     *
     * @param  str  a string
     * @param  wrapLength  substring length, after which new line delimeter should be added
     * @param  newLineStr  new line delimeter, a string
     * @param  wrapLongWords  flag indicating if break words longer wrapLength
     * @param  wrapOn  a substring instead of which newLineStr should be inserted
     * @return string obtained from the original string with inserted new-line substrings
     */
    public static String wrap(final String str,
                              int wrapLength,
                              String newLineStr,
                              final boolean wrapLongWords,
                              String wrapOn) {
        if (str == null) {
            return null;
        }
        if (newLineStr == null) {
            newLineStr = System.lineSeparator();
        }
        if (wrapLength < 1) {
            wrapLength = 1;
        }
        if (isBlank(wrapOn)) {
            wrapOn = " ";
        }
        final Pattern patternToWrapOn = Pattern.compile(wrapOn);
        final int inputLineLength = str.length();
        int offset = 0;
        final StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);
        int matcherSize = -1;

        while (offset < inputLineLength) {
            int spaceToWrapAt = -1;
            Matcher matcher = patternToWrapOn.matcher(str.substring(offset,
                    Math.min((int) Math.min(Integer.MAX_VALUE, offset + wrapLength + 1L), inputLineLength)));
            if (matcher.find()) {
                if (matcher.start() == 0) {
                    matcherSize = matcher.end();
                    if (matcherSize != 0) {
                        offset += matcher.end();
                        continue;
                    }
                    offset += 1;
                }
                spaceToWrapAt = matcher.start() + offset;
            }

            if (inputLineLength - offset <= wrapLength) {
                break;
            }

            while (matcher.find()) {
                spaceToWrapAt = matcher.start() + offset;
            }

            if (spaceToWrapAt >= offset) {
                wrappedLine.append(str, offset, spaceToWrapAt);
                wrappedLine.append(newLineStr);
                offset = spaceToWrapAt + 1;
            } else if (wrapLongWords) {
                if (matcherSize == 0) {
                    offset--;
                }
                wrappedLine.append(str, offset, wrapLength + offset);
                wrappedLine.append(newLineStr);
                offset += wrapLength;
                matcherSize = -1;
            } else {
                matcher = patternToWrapOn.matcher(str.substring(offset + wrapLength));
                if (matcher.find()) {
                    matcherSize = matcher.end() - matcher.start();
                    spaceToWrapAt = matcher.start() + offset + wrapLength;
                }

                if (spaceToWrapAt >= 0) {
                    if (matcherSize == 0 && offset != 0) {
                        offset--;
                    }
                    wrappedLine.append(str, offset, spaceToWrapAt);
                    wrappedLine.append(newLineStr);
                    offset = spaceToWrapAt + 1;
                } else {
                    if (matcherSize == 0 && offset != 0) {
                        offset--;
                    }
                    wrappedLine.append(str, offset, str.length());
                    offset = inputLineLength;
                    matcherSize = -1;
                }
            }
        }

        if (matcherSize == 0 && offset < inputLineLength) {
            offset--;
        }

        wrappedLine.append(str, offset, str.length());

        return wrappedLine.toString();
    }

    private static Set<Integer> generateDelimiterSet(final char[] delimiters) {
        final Set<Integer> delimiterHashSet = new HashSet<>();
        if (delimiters == null || delimiters.length == 0) {
            if (delimiters == null) {
                delimiterHashSet.add(Character.codePointAt(new char[]{' '}, 0));
            }

            return delimiterHashSet;
        }

        for (int index = 0; index < delimiters.length; index++) {
            delimiterHashSet.add(Character.codePointAt(delimiters, index));
        }
        return delimiterHashSet;
    }

    private static void isTrue(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    private static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    private static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String defaultString(final String str) {
        return Objects.toString(str, EMPTY);
    }

    private static int indexOf(final CharSequence seq, final CharSequence searchSeq, final int startPos) {
        if (seq == null || searchSeq == null) {
            return INDEX_NOT_FOUND;
        }
        if (seq instanceof String) {
            return ((String) seq).indexOf(searchSeq.toString(), startPos);
        }
        if (seq instanceof StringBuilder) {
            return ((StringBuilder) seq).indexOf(searchSeq.toString(), startPos);
        }
        if (seq instanceof StringBuffer) {
            return ((StringBuffer) seq).indexOf(searchSeq.toString(), startPos);
        }
        return seq.toString().indexOf(searchSeq.toString(), startPos);
    }
}