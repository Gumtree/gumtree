package org.gumtree.service.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

class HeaderFooter extends PdfPageEventHelper {
    /** Alternating phrase for the header. */
//    Phrase[] header = new Phrase[2];
    /** Current page number (will be reset for every chapter). */
    int pagenumber;
    
    /**
     * Initialize one of the headers.
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
     *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
     */
    public void onOpenDocument(PdfWriter writer, Document document) {
//        header[0] = new Phrase("Movie history");
    }
    
    /**
     * Initialize one of the headers, based on the chapter title;
     * reset the page number.
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onChapter(
     *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, float,
     *      com.itextpdf.text.Paragraph)
     */
    public void onChapter(PdfWriter writer, Document document,
            float paragraphPosition, Paragraph title) {
//        header[1] = new Phrase(title.getContent());
        pagenumber = 1;
    }

    /**
     * Increase the page number.
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onStartPage(
     *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
     */
    public void onStartPage(PdfWriter writer, Document document) {
        pagenumber++;
    }
    
    /**
     * Adds the header and the footer.
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
     *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
     */
    public void onEndPage(PdfWriter writer, Document document) {
        Rectangle rect = writer.getBoxSize("art");
//        switch(writer.getPageNumber() % 2) {
//        case 0:
//            ColumnText.showTextAligned(writer.getDirectContent(),
//                    Element.ALIGN_RIGHT, header[0],
//                    rect.getRight(), rect.getTop(), 0);
//            break;
//        case 1:
//            ColumnText.showTextAligned(writer.getDirectContent(),
//                    Element.ALIGN_LEFT, header[1],
//                    rect.getLeft(), rect.getTop(), 0);
//            break;
//        }
        ColumnText.showTextAligned(writer.getDirectContent(),
                Element.ALIGN_CENTER, new Phrase(String.format("%d", pagenumber)),
                (rect.getLeft() + rect.getRight()) / 2, rect.getBottom() - 28, 0);
    }
}
