package com.spinyowl.legui.system.renderer.nvg.component;

import static com.spinyowl.legui.style.color.ColorUtil.oppositeBlackOrWhite;
import static com.spinyowl.legui.style.util.StyleUtilities.getInnerContentRectangle;
import static com.spinyowl.legui.style.util.StyleUtilities.getPadding;
import static com.spinyowl.legui.style.util.StyleUtilities.getStyle;
import static com.spinyowl.legui.system.renderer.nvg.util.NvgRenderUtils.alignTextInBox;
import static com.spinyowl.legui.system.renderer.nvg.util.NvgRenderUtils.calculateTextBoundsRect;
import static com.spinyowl.legui.system.renderer.nvg.util.NvgRenderUtils.intersectScissor;
import static com.spinyowl.legui.system.renderer.nvg.util.NvgRenderUtils.runWithScissor;
import static org.lwjgl.nanovg.NanoVG.nnvgTextGlyphPositions;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFindFont;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.TextAreaField;
import com.spinyowl.legui.component.event.textarea.TextAreaFieldHeightChangeEvent;
import com.spinyowl.legui.component.event.textarea.TextAreaFieldWidthChangeEvent;
import com.spinyowl.legui.component.optional.TextState;
import com.spinyowl.legui.component.optional.align.HorizontalAlign;
import com.spinyowl.legui.component.optional.align.VerticalAlign;
import com.spinyowl.legui.input.Mouse;
import com.spinyowl.legui.listener.processor.EventProcessorProvider;
import com.spinyowl.legui.style.Style;
import com.spinyowl.legui.style.font.FontRegistry;
import com.spinyowl.legui.system.context.Context;
import com.spinyowl.legui.system.renderer.nvg.util.NvgColorUtil;
import com.spinyowl.legui.system.renderer.nvg.util.NvgShapes;
import com.spinyowl.legui.system.renderer.nvg.util.NvgText;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGGlyphPosition;

/**
 * NanoVG Text area renderer.
 */
public class NvgTextAreaFieldRenderer extends NvgDefaultComponentRenderer<TextAreaField> {

  public static final String NEWLINE = "\n";
  private static final String TABS = "\t";
  private static final String SPACES = " ";
  private static final char SPACEC = ' ';
  private static final int MAX_GLYPH_COUNT = 2048;
  private final Vector4f caretColor = new Vector4f(0, 0, 0, 0.5f);

  @Override
  public void renderSelf(TextAreaField component, Context context, long nanovg) {
    runWithScissor(nanovg, component, () -> {
      Vector2f pos = component.getAbsolutePosition();
      Vector2f size = component.getSize();
      Style style = component.getStyle();
      Vector4f backgroundColor = new Vector4f(style.getBackground().getColor());

      renderBackground(component, context, nanovg);

      Vector4f padding = getPadding(component, style);
      Vector4f textRect = getInnerContentRectangle(pos, size, padding);

      Component parent = component.getParent();
      Vector4f viewportRect = null;
      if (parent != null) {
        Vector2f pSize = parent.getSize();
        viewportRect = new Vector4f(parent.getAbsolutePosition(), pSize.x, pSize.y);
      }

      intersectScissor(nanovg, new Vector4f(textRect));

      renderText(context, nanovg, component, textRect, viewportRect, backgroundColor);
    });
  }

  private void renderText(Context leguiContext, long context, TextAreaField gui, Vector4f rect,
                          Vector4f viewportRect, Vector4f bc) {

    String font = getStyle(gui, Style::getFont, FontRegistry.getDefaultFont());
    // switch to default font if font not found in nanovg.
    if (nvgFindFont(context, font) == -1) {
      font = FontRegistry.getDefaultFont();
    }
    try (NVGGlyphPosition.Buffer glyphs = NVGGlyphPosition.calloc(MAX_GLYPH_COUNT)) {

      TextState textState = gui.getTextState();
      float fontSize = getStyle(gui, Style::getFontSize, 16F);
      HorizontalAlign halign = getStyle(gui, Style::getHorizontalAlign, HorizontalAlign.LEFT);
      VerticalAlign valign = getStyle(gui, Style::getVerticalAlign, VerticalAlign.MIDDLE);
      Vector4f textColor = getStyle(gui, Style::getTextColor);
      int caretPosition = gui.getCaretPosition();
      boolean focused = gui.isFocused();

      int caretLine = 0;

      preinitializeTextRendering(context, font, fontSize, halign, valign, textColor);
      float spaceWidth = getSpaceWidth(context);

      String[] lines = textState.getText().split(NEWLINE, -1);
      int lineCount = lines.length;
      int[] lineStartIndeces = new int[lineCount];
      int caretOffset = 0;

      // calculate caret offset for every line
      for (int i = 0; i < lineCount - 1; i++) {
        lineStartIndeces[i + 1] = lineStartIndeces[i] + lines[i].length() + 1;
        if (caretPosition >= lineStartIndeces[i + 1]) {
          caretOffset = lineStartIndeces[i + 1];
          caretLine = i + 1;
        }
      }

      // calculate line caret position
      int lineCaretPosition = caretPosition - caretOffset;

      // if not focused set caret line and caret position in line to default
      if (!focused && gui.isStickToAlignment()) {
        switch (valign) {
          case TOP:
            caretLine = 0;
            break;
          case BOTTOM:
            caretLine = lineCount - 1;
            break;
          default:
            caretLine = lineCount / 2;
            break;
        }
        switch (halign) {
          case LEFT:
            lineCaretPosition = (0);
            break;
          case RIGHT:
            lineCaretPosition = lines[caretLine].length();
            break;
          default:
            lineCaretPosition = lines[caretLine].length() / 2;
            break;
        }
      }

      int vp;
      switch (valign) {
        case TOP:
          vp = 0;
          break;
        case MIDDLE:
          vp = 1;
          break;
        default:
          vp = valign == VerticalAlign.BOTTOM ? 2 : 1;
          break;
      }

      float voffset =
          (lineCount - 1) * fontSize * vp * -0.5f + (valign == VerticalAlign.BASELINE ? fontSize
              / 4f : 0);
      float caretx;
      float mouseCaretX = 0;
      int mouseLineIndex = 0;

      int mouseCaretPositionInLine = 0;
      Vector2f cursorPosition = Mouse.getCursorPosition();
      float mouseX = cursorPosition.x;
      float mouseY = cursorPosition.y;

      // we need to calculate x and y offsets
      String caretLineText = lines[caretLine];
      float[] caretLineBounds = calculateTextBoundsRect(context, rect, caretLineText, halign,
          valign, fontSize);

      // also we need to calculate offset x // caretLine
      caretx = getCaretx(context, lineCaretPosition, caretLineText, caretLineBounds, glyphs,
          spaceWidth, gui.getTabSize());

      preinitializeTextRendering(context, font, fontSize, halign, valign, textColor);

      float[][] bounds = new float[lineCount][8];
      float maxWid = 0f;

      // binary search line in view rect
      int first = 0;
      int last = lineCount - 1;

      if (viewportRect != null) {
        int mid = (first + last) / 2;

        // search for any line in viewport rect
        while (first <= last) {
          String line = lines[mid];
          float[] lineBounds = calculateTextBoundsRect(context, rect, line, halign, valign,
              fontSize);
          bounds[mid] = lineBounds;

          float lineY = lineBounds[5] + voffset + fontSize * mid;
          float lineHeight = lineBounds[7];

          if (lineY > viewportRect.y + viewportRect.w) {
            last = mid - 1;
          } else if (lineY <= viewportRect.y + viewportRect.w
              && lineY + lineHeight >= viewportRect.y) {
            break;
          } else {
            first = mid + 1;
          }

          mid = (first + last) / 2;
        }

        // search start and end lines in viewport rect and calculate bounds
        float lineY;
        float lineHeight;

        for (first = mid - 1; first >= 0; first--) {
          String line = lines[first];
          float[] lineBounds = calculateTextBoundsRect(context, rect, line, halign, valign,
              fontSize);
          bounds[first] = lineBounds;

          lineY = lineBounds[5] + voffset + fontSize * first;
          lineHeight = lineBounds[7];
          if (lineY + lineHeight <= viewportRect.y) {
            break;
          }
        }
        first++;

        for (last = mid + 1; last < lineCount; last++) {
          String line = lines[last];
          float[] lineBounds = calculateTextBoundsRect(context, rect, line, halign, valign,
              fontSize);
          bounds[last] = lineBounds;

          lineY = lineBounds[5] + voffset + fontSize * last;
          if (lineY > viewportRect.y + viewportRect.w) {
            break;
          }
        }
        last--;

        // calculate max width
        int maxLength = 0;
        int longestStringIndex = 0;
        for (int i = 0; i < lines.length; i++) {
          String s = lines[i];
          if (s.length() > maxLength) {
            maxLength = s.length();
            longestStringIndex = i;
          }
        }
        if (longestStringIndex < first || longestStringIndex > last) {
          float[] lineBounds = calculateTextBoundsRect(context, rect, lines[longestStringIndex],
              halign, valign, fontSize);
          maxWid = lineBounds[2];
        } else {
          maxWid = bounds[longestStringIndex][2];
        }
      } else {
        maxWid = calculateLineBoundsAndMaxWidth(context, gui, rect, fontSize, halign, valign,
            spaceWidth, lines, lineCount, bounds, maxWid);
      }

      float textWidth = textState.getTextWidth();
      float textHeight = textState.getTextHeight();

      textState.setTextWidth(maxWid);
      float newTextHeight = (lines.length) * fontSize;
      textState.setTextHeight(newTextHeight);
      textState.setCaretX(caretx);
      textState.setCaretY(caretLineBounds[5] + voffset + fontSize * caretLine);

      if (Math.abs(textWidth - maxWid) > 0.001) {
        EventProcessorProvider.getInstance().pushEvent(
            new TextAreaFieldWidthChangeEvent(gui, leguiContext, gui.getFrame(), maxWid));
      }

      if (Math.abs(textHeight - newTextHeight) > 0.001) {
        EventProcessorProvider.getInstance().pushEvent(
            new TextAreaFieldHeightChangeEvent(gui, leguiContext, gui.getFrame(), newTextHeight));
      }

      // calculate default mouse line index
      float llineY = bounds[lineCount - 1][5] - voffset + fontSize * (lineCount - 1);
      if (mouseY > llineY + fontSize) {
        mouseLineIndex = lineCount - 1;
      }

      // calculate caret color based on time
      if (focused) {
        oppositeBlackOrWhite(bc, caretColor);
        caretColor.w = (float) Math.abs(GLFW.glfwGetTime() % 1 * 2 - 1);
      }

      int startSelectionIndex = gui.getStartSelectionIndex();
      int endSelectionIndex = gui.getEndSelectionIndex();
      // swap
      if (startSelectionIndex > endSelectionIndex) {
        startSelectionIndex += endSelectionIndex;
        endSelectionIndex = startSelectionIndex - endSelectionIndex;
        startSelectionIndex -= endSelectionIndex;
      }

      int startSelectionLine = 0;
      int startSelectionIndexInLine;
      int endSelectionLine = 0;
      int endSelectionIndexInLine;
      for (int i = 0; i < lineCount; i++) {
        if (startSelectionIndex >= lineStartIndeces[i]) {
          startSelectionLine = i;
        }
        if (endSelectionIndex >= lineStartIndeces[i]) {
          endSelectionLine = i;
        }
      }
      startSelectionIndexInLine = startSelectionIndex - lineStartIndeces[startSelectionLine];
      endSelectionIndexInLine = endSelectionIndex - lineStartIndeces[endSelectionLine];

      float startSelectionCaretX =
          getCaretx(context, startSelectionIndexInLine, lines[startSelectionLine],
              bounds[startSelectionLine], glyphs, spaceWidth, gui.getTabSize());
      float endSelectionCaretX =
          getCaretx(context, endSelectionIndexInLine, lines[endSelectionLine],
              bounds[endSelectionLine], glyphs, spaceWidth, gui.getTabSize());

      // render every line of text
      for (int i = first; i <= last; i++) {
        ByteBuffer lineBytes = null;
        try {
          String line = lines[i];
          lineBytes = memUTF8(line);

          alignTextInBox(context, HorizontalAlign.LEFT, VerticalAlign.MIDDLE);
          int ng = nnvgTextGlyphPositions(context, bounds[i][4], 0, memAddress(lineBytes), 0,
              memAddress(glyphs), MAX_GLYPH_COUNT);

          float lineX = bounds[i][4];
          float lineWidth = bounds[i][6];
          float lineY = bounds[i][5] + voffset + fontSize * i;
          float lineHeight = bounds[i][7];
          if (inRect(viewportRect, lineX, lineWidth, lineY, lineHeight)) {

            List<Integer> tabIndices = getTabIndices(line);
            // calculate mouse caret position
            if (lineY <= mouseY && lineY + fontSize > mouseY) {
              if (line.length() == 0) {
                mouseCaretX = caretx;
              } else {
                if (mouseX <= glyphs.get(0).x()) {
                  mouseCaretPositionInLine = 0;
                  mouseCaretX = glyphs.get(0).x();
                } else if (mouseX >= glyphs.get(ng - 1).maxx() + spaceWidth * (gui.getTabSize() - 1)
                    * tabIndices.size()) {
                  mouseCaretPositionInLine = ng;
                  mouseCaretX = glyphs.get(ng - 1).maxx() + spaceWidth * (gui.getTabSize() - 1)
                      * tabIndices.size();
                  // if window not minimized
                } else if (!leguiContext.isIconified()) {
                  // binary search mouse caret position
                  int upper = ng;
                  int lower = 0;
                  boolean found = false;
                  do {
                    int index = (upper + lower) / 2;
                    float tabAddition = 0;
                    for (Integer tabIndex : tabIndices) {
                      if (index > tabIndex) {
                        tabAddition += spaceWidth * (gui.getTabSize() - 1);
                      }
                    }
                    float left = glyphs.get(index).x();
                    float right =
                        index >= ng - 1 ? glyphs.get(ng - 1).maxx() : glyphs.get(index + 1).x();
                    left += tabAddition;
                    right += tabAddition;
                    if (tabIndices.contains(index)) {
                      right += spaceWidth * (gui.getTabSize() - 1);
                    }

                    float mid = (left + right) / 2f;
                    if (mouseX >= left && mouseX < right) {
                      found = true;
                      if (mouseX > mid) {
                        mouseCaretPositionInLine = index + 1;
                        mouseCaretX = right;
                      } else {
                        mouseCaretPositionInLine = index;
                        mouseCaretX = left;
                      }
                    } else if (mouseX >= right) {
                      if (index != ng) {
                        lower = index + 1;
                      } else {
                        found = true;
                        mouseCaretPositionInLine = ng;
                        mouseCaretX = right;
                      }
                    } else if (mouseX < left) {
                      if (index != 0) {
                        upper = index;
                      } else {
                        found = true;
                        mouseCaretPositionInLine = 0;
                        mouseCaretX = left;
                      }
                    }
                  } while (!found);
                }
              }

              mouseLineIndex = i;
              // render mouse caret
              if (leguiContext.isDebugEnabled()) {
                NvgShapes.drawRectStroke(context,
                    new Vector4f(mouseCaretX - 1, lineY, 1, lineHeight),
                    new Vector4f(caretColor).div(2), 1);
              }
            }
            if (mouseY >= bounds[lineCount - 1][5] + voffset + fontSize * (lineCount - 1)
                + fontSize) {
              mouseLineIndex = lineCount - 1;
              mouseCaretPositionInLine = lines[mouseLineIndex].length();
            }
            // render selection background
            if (startSelectionIndex != endSelectionIndex && i >= startSelectionLine
                && i <= endSelectionLine) {
              float x1 = bounds[i][4];
              float w = bounds[i][6];
              float x2 = x1 + w;
              if (i == startSelectionLine) {
                x1 = startSelectionCaretX;
              }
              if (i == endSelectionLine) {
                x2 = endSelectionCaretX;
              }
              w = x2 - x1;
              NvgShapes
                  .drawRect(context,
                      new Vector4f(x1, bounds[i][5] + voffset + fontSize * i, w, bounds[i][7]),
                      getStyle(gui, Style::getHighlightColor));
            }

            // render current line background
            renderCurrentLineBackground(context, rect, bc, fontSize, focused, caretLine, i, lineY);

            char[] spaces = new char[gui.getTabSize()];
            Arrays.fill(spaces, SPACEC);
            NvgText.drawTextLineToRect(context, new Vector4f(lineX, lineY, lineWidth, lineHeight),
                false, HorizontalAlign.LEFT, VerticalAlign.MIDDLE,
                fontSize, font, line.replace(TABS, new String(spaces)), textColor);
            if (i == caretLine && focused) {
              // render caret
              NvgShapes.drawRectStroke(context, new Vector4f(caretx - 1, lineY, 1, lineHeight),
                  caretColor, 1);
            }
          }
        } finally {
          // free allocated memory
          if (lineBytes != null) {
            memFree(lineBytes);
          }
        }
      }

      gui.setMouseCaretPosition(lineStartIndeces[mouseLineIndex] + mouseCaretPositionInLine);
    }
  }

  private float calculateLineBoundsAndMaxWidth(long context, TextAreaField gui, Vector4f rect,
      float fontSize, HorizontalAlign halign, VerticalAlign valign, float spaceWidth,
      String[] lines, int lineCount, float[][] bounds, float maxWid) {
    for (int i = 0; i < lineCount; i++) {
      String line = lines[i];
      float[] lineBounds = calculateTextBoundsRect(context, rect, line, halign, valign, fontSize);

      if (lineBounds[2] > maxWid) {
        maxWid = lineBounds[2];
      }

      bounds[i] = lineBounds;
      if (line.contains(TABS)) {
        bounds[i][6] +=
            spaceWidth * (line.length() - line.replace(TABS, "").length()) * (gui.getTabSize() - 1);
      }
    }
    return maxWid;
  }

  /**
   * Used to get space width.
   *
   * @param context nanovg context.
   * @return space width.
   */
  private float getSpaceWidth(long context) {
    String s = SPACES + SPACES;
    ByteBuffer spaceBytes = null;

    try (NVGGlyphPosition.Buffer glyphs = NVGGlyphPosition.calloc(MAX_GLYPH_COUNT)) {
      spaceBytes = memUTF8(s);

      alignTextInBox(context, HorizontalAlign.LEFT, VerticalAlign.MIDDLE);
      nnvgTextGlyphPositions(context, 10, 0, memAddress(spaceBytes), 0, memAddress(glyphs),
          MAX_GLYPH_COUNT);

      float x1 = glyphs.get(1).x();
      float x0 = glyphs.get(0).x();
      return x1 - x0;
    } finally {
      if (spaceBytes != null) {
        memFree(spaceBytes);
      }
    }
  }


  /**
   * Used to obtain caret (x) position (on screen) by text line and caret position in text(index).
   *
   * @param context         context
   * @param caretPosInText  position of caret in text.
   * @param text            text.
   * @param caretLineBounds text bounds on screen.
   * @param glyphs          glyphs.
   * @param spaceWidth      space width.
   * @return caret x position on screen.
   */
  private float getCaretx(long context, int caretPosInText, String text, float[] caretLineBounds,
      NVGGlyphPosition.Buffer glyphs, float spaceWidth,
      int tabSize) {
    float caretx;
    ByteBuffer caretLineBytes = null;
    try {
      // allocate ofheap memory and fill it with text
      caretLineBytes = memUTF8(text);
      // align text for calculations
      alignTextInBox(context, HorizontalAlign.LEFT, VerticalAlign.MIDDLE);
      int ng = nnvgTextGlyphPositions(context, caretLineBounds[4], 0, memAddress(caretLineBytes), 0,
          memAddress(glyphs), MAX_GLYPH_COUNT);
      caretx = calculateCaretPos(caretPosInText, caretLineBounds, ng, glyphs);

      String substring = text.substring(0, caretPosInText);
      int tabCountBeforeCaret;
      if (substring.contains(TABS)) {
        tabCountBeforeCaret = substring.length() - substring.replace(TABS, "").length();
        caretx += spaceWidth * tabCountBeforeCaret * (tabSize - 1);
      }
    } finally {
      memFree(caretLineBytes);
    }
    return caretx;
  }

  private void preinitializeTextRendering(long context, String font, float fontSize,
      HorizontalAlign halign, VerticalAlign valign, Vector4f textColor) {
    try (NVGColor colorA = NvgColorUtil.create(textColor)) {
      alignTextInBox(context, halign, valign);
      nvgFontSize(context, fontSize);
      nvgFontFace(context, font);
      nvgFillColor(context, colorA);
    }
  }

  private float calculateCaretPos(int caretPosition, float[] textBounds, int ng,
      NVGGlyphPosition.Buffer glyphs) {
    float caretx = 0;
    if (caretPosition < ng) {
      try {
        caretx = glyphs.get(caretPosition).x();
      } catch (IndexOutOfBoundsException e) {
        e.printStackTrace();
      }
    } else {
      if (ng > 0) {
        caretx = glyphs.get(ng - 1).maxx();
      } else {
        caretx = textBounds[4];
      }
    }
    return caretx;
  }

  private boolean inRect(Vector4f rect, float lineX, float lineWidth, float lineY,
      float lineHeight) {
    return rect == null
        || lineY <= rect.y + rect.w && lineY + lineHeight >= rect.y
        && lineX <= rect.x + rect.z && lineX + lineWidth >= rect.x;
  }

  private List<Integer> getTabIndices(String line) {
    List<Integer> tabIndices = new ArrayList<>();
    if (line.contains(TABS)) {
      int index = line.indexOf(TABS);
      while (index != -1) {
        tabIndices.add(index);
        index = line.indexOf(TABS, index + 1);
      }
    }
    return tabIndices;
  }

  private void renderCurrentLineBackground(long context, Vector4f rect, Vector4f backgroundColor,
      float fontSize, boolean focused,
      int caretLine, int currentLineIndex, float lineY) {
    if (currentLineIndex == caretLine && focused) {
      Vector4f currentLineBgColor = oppositeBlackOrWhite(backgroundColor);
      currentLineBgColor.w = 0.1f;
      NvgShapes.drawRect(context, new Vector4f(rect.x, lineY, rect.z, fontSize),
          currentLineBgColor);
    }
  }

}
