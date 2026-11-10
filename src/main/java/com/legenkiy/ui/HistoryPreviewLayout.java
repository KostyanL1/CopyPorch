package com.legenkiy.ui;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class HistoryPreviewLayout {
    static final int MAX_LINES = 3;
    private static final String ELLIPSIS = "…";

    private final Font font;
    private final FontRenderContext context;
    private final int width;

    HistoryPreviewLayout(Font font, FontRenderContext context, int width) {
        this.font = font;
        this.context = context;
        this.width = width;
    }

    List<TextLayout> wrap(String text) {
        List<TextLayout> lines = new ArrayList<>(MAX_LINES);
        Iterator<String> paragraphs = text.lines().iterator();
        while (lines.size() < MAX_LINES && paragraphs.hasNext()) {
            String paragraph = paragraphs.next();
            appendParagraph(lines, paragraph, paragraphs.hasNext());
        }
        return lines;
    }

    private void appendParagraph(List<TextLayout> lines, String paragraph, boolean hasMoreParagraphs) {
        if (paragraph.isEmpty()) {
            boolean truncate = lines.size() == MAX_LINES - 1 && hasMoreParagraphs;
            lines.add(new TextLayout(truncate ? ELLIPSIS : " ", font, context));
            return;
        }

        AttributedString attributed = new AttributedString(paragraph);
        attributed.addAttribute(TextAttribute.FONT, font);
        LineBreakMeasurer measurer = new LineBreakMeasurer(attributed.getIterator(), context);
        while (lines.size() < MAX_LINES && measurer.getPosition() < paragraph.length()) {
            int start = measurer.getPosition();
            TextLayout line = measurer.nextLayout(width);
            boolean hasMoreText = measurer.getPosition() < paragraph.length() || hasMoreParagraphs;
            if (lines.size() == MAX_LINES - 1 && hasMoreText) {
                line = withEllipsis(paragraph, measurer, start);
            }
            lines.add(line);
        }
    }

    private TextLayout withEllipsis(String paragraph, LineBreakMeasurer measurer, int start) {
        TextLayout ellipsis = new TextLayout(ELLIPSIS, font, context);
        float available = width - ellipsis.getAdvance();
        if (available <= 0) {
            return ellipsis;
        }

        measurer.setPosition(start);
        int end = measurer.nextOffset(available, paragraph.length(), false);
        if (end <= start) {
            return ellipsis;
        }

        String lastLine = paragraph.substring(start, end).stripTrailing() + ELLIPSIS;
        TextLayout layout = new TextLayout(lastLine, font, context);
        return layout.getAdvance() <= width ? layout : ellipsis;
    }
}
