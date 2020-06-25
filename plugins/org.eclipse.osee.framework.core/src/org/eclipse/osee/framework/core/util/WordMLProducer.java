/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.core.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.xml.Xml;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Ryan D. Brooks
 */
public class WordMLProducer {
   public static final String RGB_RED = "FF0000";
   public static final String RGB_GREEN = "00FF00";
   public static final String RGB_BLUE = "0000FF";

   private static final String FILE_NAME = "fileName";

   public static final String LISTNUM_FIELD_HEAD = "<w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr>";
   public static final String LISTNUM_FIELD_TAIL =
      "<w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:instrText>LISTNUM\"listreset\"\\l1\\s0</w:instrText></w:r><w:r><w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"end\"/><wx:t wx:val=\"1.\"/></w:r>";

   //This regular expression pulls out all of the stuff after the inserted listnum reordering stuff.  This needs to be
   //here so that we remove unwanted template information from single editing
   public static final String LISTNUM_FIELD_TAIL_REG_EXP =
      "<w:r(>| .*?>)<w:rPr><w:vanish/></w:rPr><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r(>| .*?>)<w:rPr><w:vanish/></w:rPr><w:instrText> LISTNUM \"listreset\"";
   public static final String LISTNUM_FIELD = LISTNUM_FIELD_HEAD + LISTNUM_FIELD_TAIL;
   private static final String SUB_DOC =
      "<wx:sect><w:p><w:pPr><w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/><w:pgMar w:top=\"1440\" w:right=\"1800\" w:bottom=\"1440\" w:left=\"1800\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/><w:cols w:space=\"720\"/><w:docGrid w:line-pitch=\"360\"/></w:sectPr></w:pPr></w:p><w:subDoc w:link=\"" + FILE_NAME + "\"/></wx:sect><wx:sect><wx:sub-section><w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr></w:p><w:sectPr><w:type w:val=\"continuous\"/><w:pgSz w:w=\"12240\" w:h=\"15840\"/><w:pgMar w:top=\"1440\" w:right=\"1800\" w:bottom=\"1440\" w:left=\"1800\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/><w:cols w:space=\"720\"/><w:docGrid w:line-pitch=\"360\"/></w:sectPr></wx:sub-section></wx:sect>";
   private static final String HYPER_LINK_DOC =
      "<w:p><w:hlink w:dest=\"fileName\"><w:r wsp:rsidRPr=\"00CE6681\"><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>fileName</w:t></w:r></w:hlink></w:p>";
   private final Appendable strB;
   private final int[] outlineNumber;
   private int outlineLevel;
   private int maxOutlineLevel = 9;
   private int flattenedLevelCount;
   private final Map<String, Integer> alphabetMap;

   private static final String DEFAULT_FONT = "Times New Roman";

   public WordMLProducer(Appendable str) {
      strB = str;
      outlineNumber = new int[10]; // word supports 9 levels of outlining; index this array from 1 to 9
      outlineLevel = 0;
      flattenedLevelCount = 0;

      alphabetMap = new HashMap<>();

      alphabetMap.put("A.0", 1);
      alphabetMap.put("B.0", 2);
      alphabetMap.put("C.0", 3);
   }

   public CharSequence startOutlineSubSection() {
      CharSequence paragraphNumber = startOutlineSubSection(DEFAULT_FONT, null, null);
      return paragraphNumber;
   }

   public CharSequence startOutlineSubSection(CharSequence font, CharSequence headingText, String outlineType) {
      if (okToStartSubsection()) {
         outlineNumber[++outlineLevel]++;
         CharSequence paragraphNumber = getOutlineNumber();
         startOutlineSubSection((outlineType != null ? outlineType : "Heading") + outlineLevel, paragraphNumber, font,
            headingText);
         return paragraphNumber;
      } else {
         flattenedLevelCount++;
         endOutlineSubSection(true);
         OseeLog.log(this.getClass(), Level.WARNING,
            "Outline level flattened, max outline level is currently set to " + maxOutlineLevel + ", ms word only goes 9 levels deep");
         return startOutlineSubSection(font, headingText, outlineType);
      }
   }

   protected void append(CharSequence value) {
      try {
         strB.append(value);
      } catch (IOException ex) {
         OseeCoreException.wrapAndThrow(ex);
      }
   }

   public void startOutlineSubSection(CharSequence style, CharSequence outlineNumber, CharSequence font, CharSequence headingText) {
      append("<wx:sub-section>");
      if (Strings.isValid(headingText)) {
         startParagraph();
         append("<w:pPr>");
         writeParagraphStyle(style);
         append("<w:listPr><wx:t wx:val=\"");
         append(outlineNumber);
         append("\" wx:wTabBefore=\"540\" wx:wTabAfter=\"90\"/><wx:font wx:val=\"");
         append(font);
         append("\"/></w:listPr></w:pPr>");
         writeHeadingText(headingText);
         endParagraph();
      }
   }

   public void startAppendixSubSection(CharSequence style, CharSequence headingText) {
      append("<wx:sub-section>");
      if (Strings.isValid(headingText)) {
         startParagraph();
         append("<w:pPr>");
         writeParagraphStyle(style);
         append("</w:pPr>");
         writeHeadingText(headingText);
         endParagraph();
      }
   }

   public void endAppendixSubSection() {
      append("</wx:sub-section>");
   }

   /**
    * @param chapterNumbering - Whether or not chapter number (1-1) will be applied
    * @param chapterStyle = Which style to use (1-1, 1.1-1, 1.2.3-1 etc)
    * @param restartNumbering - Restart the numbering from the previous section
    * @param pageLayout - Set to landscape if needed
    */
   public void setPageBreak(boolean chapterNumbering, int chapterStyle, boolean restartNumbering, String pageLayout) {
      boolean landscape = pageLayout != null && pageLayout.equals("Landscape");

      append("<w:p>");
      append("<w:pPr>");
      append("<w:sectPr>");
      if (landscape) {
         append("<w:pgSz w:w=\"15840\" w:h=\"12240\" w:orient=\"landscape\" w:code=\"1\" />");
      } else {
         append("<w:pgSz w:w=\"12240\" w:h=\"15840\" w:code=\"1\" />");
      }
      append(
         "<w:pgMar w:top=\"1440\" w:right=\"1296\" w:bottom=\"1440\" w:left=\"1296\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>");
      if (chapterNumbering) {
         append("<w:pgNumType ");
         if (restartNumbering) {
            append("w:start=\"1\" ");
         }
         append(String.format("w:chap-style=\"%s\"/>", chapterStyle));
      }
      append("</w:sectPr>");
      append("</w:pPr>");
      append("</w:p>");
   }

   public void setPageBreak(boolean chapterNumbering, int chapterStyle, boolean restartNumbering) {
      // Default to no page layout style which will stay with portrait
      setPageBreak(chapterNumbering, chapterStyle, restartNumbering, null);
   }

   private void writeParagraphStyle(CharSequence style) {
      append("<w:pStyle w:val=\"");
      append(style);
      append("\"/>");
   }

   private void writeHeadingText(CharSequence headingText) {
      append("<w:r><w:t>");
      append(Xml.escape(headingText));
      append("</w:t></w:r>");
   }

   public String setHeadingNumbers(String outlineNumber, String template, String outlineType) {
      boolean appendixOutlineType = outlineType != null && outlineType.equalsIgnoreCase("APPENDIX");
      if (outlineNumber == null) {
         return template;
      }

      if (appendixOutlineType) {
         // Example of appendix number: A.0
         char[] chars = outlineNumber.toCharArray();
         template = setAppendixStartLetter(chars[0], template);
      } else {
         int index = 1;
         String[] numbers = outlineNumber.split("\\.");

         for (String number : numbers) {
            Matcher matcher = Pattern.compile(
               String.format("<w:start w:val=\"(\\d*?)\"/><w:pStyle w:val=\"Heading%d\"/>", index)).matcher("");
            matcher.reset(template);
            template = matcher.replaceAll(
               String.format("<w:start w:val=\"%s\"/><w:pStyle w:val=\"Heading%d\"/>", number, index));
            index++;
         }
      }
      if (!appendixOutlineType) {
         setNextParagraphNumberTo(outlineNumber);
      }
      return template;
   }

   public String setAppendixStartLetter(char chr, String template) {
      template = template.replace("<w:start w:val=\"1\"/><w:nfc w:val=\"3\"/><w:pStyle w:val=\"APPENDIX1\"/>",
         "<w:start w:val=\"" + (Character.toLowerCase(
            chr) - 'a' + 1) + "\"/><w:nfc w:val=\"3\"/><w:pStyle w:val=\"APPENDIX1\"/>");
      return template;
   }

   public void endOutlineSubSection() {
      endOutlineSubSection(false);
   }

   private void endOutlineSubSection(boolean force) {
      if (!force && flattenedLevelCount > 0) {
         flattenedLevelCount--;
      } else {
         append("</wx:sub-section>");
         if (outlineLevel + 1 < outlineNumber.length) {
            outlineNumber[outlineLevel + 1] = 0;
         }
         outlineLevel--;
      }
   }

   public void addWordMl(CharSequence wordMl) {
      append(wordMl);
   }

   public void startParagraph() {
      append("<w:p>");
   }

   public void startSubSection() {
      append("<wx:sect>");
   }

   public void endSubSection() {
      append("</wx:sect>");
   }

   public void createSubDoc(String fileName) {
      if (Strings.isValid(fileName)) {
         throw new IllegalArgumentException("The file name can not be null or empty.");
      }

      append(SUB_DOC.replace(FILE_NAME, fileName));
   }

   public void createHyperLinkDoc(String fileName) {
      if (!Strings.isValid(fileName)) {
         throw new IllegalArgumentException("The file name can not be null or empty.");
      }

      append(HYPER_LINK_DOC.replace(FILE_NAME, fileName));
   }

   public void resetListValue() {
      // extra paragraph needed to support WORD's bug to add in a trailing zero when using field codes
      startParagraph();
      addWordMl(LISTNUM_FIELD_HEAD);
      endParagraph();

      startParagraph();
      //The listnum also acts a template delimiter to know when to remove unwanted content.
      addWordMl(LISTNUM_FIELD);
      endParagraph();
   }

   public void endParagraph() {
      append("</w:p>");
   }

   public void startTable() {
      append("<wx:sub-section><w:tbl>");
   }

   public void endTable() {
      append("</w:tbl></wx:sub-section>");
   }

   public void startTableRow() {
      append("<w:tr>");
   }

   public void endTableRow() {
      append("</w:tr>");
   }

   public void startTableColumn() {
      append("<w:tc>");
   }

   public void endTableColumn() {
      append("</w:tc>");
   }

   public void addTableCaption(String captionText) {

      append(
         "<w:p wsp:rsidR=\"003571A9\" wsp:rsidRDefault=\"00AE7B3F\" wsp:rsidP=\"00AE7B3F\"><w:pPr><w:pStyle w:val=\"Caption\"/></w:pPr>");
      append("<w:r><w:t>Table </w:t></w:r>");
      append("<w:fldSimple w:instr=\" SEQ Table \\* ARABIC \"><w:r><w:rPr><w:noProof/></w:rPr><w:t>");
      append("#");
      append("</w:t></w:r></w:fldSimple><w:r><w:t>: ");
      append(captionText);
      append("</w:t></w:r></w:p>");

   }

   public void addTableColumnHeader(String text) {
      startTableColumn();
      addParagraphBold(text);
      endTableColumn();
   }

   public void addTableColumns(String... datas) {
      for (String data : datas) {
         startTableColumn();
         addParagraph(data);
         endTableColumn();
      }
   }

   public void addTableRow(String... datas) {
      startTableRow();
      addTableColumns(datas);
      endTableRow();
   }

   public void addParagraphNoEscape(CharSequence text) {
      append("<w:p><w:r><w:t>");
      append(text);
      append("</w:t></w:r></w:p>");
   }

   public void addEditParagraphNoEscape(CharSequence text) {
      startParagraph();
      append(text);
      endParagraph();
   }

   public void addParagraph(CharSequence text) {
      startParagraph();
      addTextInsideParagraph(text);
      endParagraph();
   }

   public void addParagraphBold(CharSequence text) {
      append("<w:p><w:r><w:rPr><w:b/></w:rPr><w:t>");
      append(Xml.escape(text));
      append("</w:t><w:rPr><w:b/></w:rPr></w:r></w:p>");
   }

   /**
    * This method will escape the provided text.
    */
   public void addTextInsideParagraph(CharSequence text) {
      append("<w:r><w:t>");
      append(Xml.escape(text));
      append("</w:t></w:r>");
   }

   public void addTextInsideParagraph(CharSequence text, String rgbHexColor) {
      if (rgbHexColor == null) {
         throw new IllegalArgumentException("rgbHexColor can not be null");
      }
      if (rgbHexColor.length() != 6) {
         throw new IllegalArgumentException("rgbHexColor should be a hex string 6 characters long");
      }

      append("<w:r><w:rPr><w:color w:val=\"");
      append(rgbHexColor);
      append("\"/></w:rPr>");
      append("<w:t>");
      append(Xml.escape(text));
      append("</w:t></w:r>");
   }

   public void addOleData(CharSequence oleData) {
      append("<w:docOleData>");
      append(oleData);
      append("</w:docOleData>");
   }

   private CharSequence getOutlineNumber() {
      StringBuilder strB = new StringBuilder();
      for (int i = 1; i < outlineLevel; i++) {
         strB.append(String.valueOf(outlineNumber[i]));
         strB.append(".");
      }
      strB.append(String.valueOf(outlineNumber[outlineLevel]));
      return strB;
   }

   public boolean okToStartSubsection() {
      return outlineLevel < maxOutlineLevel;
   }

   public void setNextParagraphNumberTo(String nextOutlineNumber) {
      String[] nextOutlineNumbers = nextOutlineNumber.split("\\.");
      Arrays.fill(outlineNumber, 0);

      try {
         for (int i = 0; i < nextOutlineNumbers.length; i++) {

            outlineNumber[i + 1] = Integer.parseInt(nextOutlineNumbers[i]);
         }
         outlineNumber[nextOutlineNumbers.length]--;
         outlineLevel = nextOutlineNumbers.length - 1;
      } catch (NumberFormatException ex) {
         //Do nothing
      }
   }

   /**
    * Sets the page layout to either portrait/landscape depending on the artifacts pageType attribute value. Note: This
    * call should be done after processing each artifact so if a previous artifact was landscaped the following artifact
    * would be set back to portrait.
    */
   public void setPageLayout(String pageType) {
      boolean landscape = pageType != null && pageType.equals("Landscape");

      if (landscape) {
         append("<w:p>");
         append("<w:pPr>");
         append("<w:sectPr>");
         append("<w:pgSz w:w=\"15840\" w:h=\"12240\" w:orient=\"landscape\" w:code=\"1\" />");
         append("</w:sectPr>");
         append("</w:pPr>");
         append("</w:p>");
      }
   }

   public void setMaxOutlineLevel(int maxOutlineLevel) {
      this.maxOutlineLevel = maxOutlineLevel;
   }

   public void startErrorLog() {
      startAppendixSubSection("Heading1", "Error Log");
      startTable();
      addWordMl(
         "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders><w:top w:val=\"single\" w:sz=\"4\" wx:bdrwidth=\"10\" w:space=\"0\" w:color=\"auto\"/><w:left w:val=\"single\" w:sz=\"4\" wx:bdrwidth=\"10\" w:space=\"0\" w:color=\"auto\"/><w:bottom w:val=\"single\" w:sz=\"4\" wx:bdrwidth=\"10\" w:space=\"0\" w:color=\"auto\"/><w:right w:val=\"single\" w:sz=\"4\" wx:bdrwidth=\"10\" w:space=\"0\" w:color=\"auto\"/><w:insideH w:val=\"single\" w:sz=\"4\" wx:bdrwidth=\"10\" w:space=\"0\" w:color=\"auto\"/><w:insideV w:val=\"single\" w:sz=\"4\" wx:bdrwidth=\"10\" w:space=\"0\" w:color=\"auto\"/></w:tblBorders></w:tblPr>");
      startTableRow();
      addTableColumnHeader("Artifact Id");
      addTableColumnHeader("Artifact Name");
      addTableColumnHeader("Artifact Type");
      addTableColumnHeader("Description");
      endTableRow();
   }

   public void addErrorRow(String id, String name, String type, String description) {
      startTableRow();
      addTableColumns(id, name, type, description);
      endTableRow();
   }

   public void endErrorLog() {
      endTable();
      addTableCaption("Error Log");
      endAppendixSubSection();
      setPageBreak(true, 1, true);
   }

}
