package biz.ajoshi.swcheckin;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import biz.ajoshi.t1401654727.checkin.network.HtmlReader;

/**
 * tests for the html parsing methods in HtmlReader
 */
public class HtmlReaderTest extends TestCase {
    private static final String TAG_STRING = "<one>two<three>four<five>";
    private static final String LONG_STRING = "This is a long string\n that one might think would end. ";

    public void testReadTag() throws IOException {
        HtmlReader htmlReader = new HtmlReader();
        InputStream is = new ByteArrayInputStream(TAG_STRING.getBytes());
        String outputTag = htmlReader.readTag(is);
        String outputTag2 = htmlReader.readTag(is);
        assertEquals("<one>", outputTag);
        assertEquals("two<three>", outputTag2);
    }

    public void testReadTagNullInput() throws IOException {
        HtmlReader htmlReader = new HtmlReader();
        assertEquals(null, htmlReader.readTag(null));
    }

    public void testReadTagReadsUntilTagEnd() throws IOException {
        String tagString = LONG_STRING + ">";
        InputStream is = new ByteArrayInputStream(tagString.getBytes());
        HtmlReader htmlReader = new HtmlReader();
        assertEquals(tagString, htmlReader.readTag(is));
    }

    public void testReadTagReadsIfNoTagEnd() throws IOException {
        HtmlReader htmlReader = new HtmlReader();
        InputStream is = new ByteArrayInputStream(LONG_STRING.getBytes());
        assertEquals(LONG_STRING, htmlReader.readTag(is));
    }

    public void testGetDocumentContentForNextTag() throws IOException {
        HtmlReader htmlReader = new HtmlReader();
        InputStream is = new ByteArrayInputStream(TAG_STRING.getBytes());
        String nextTagContent = htmlReader.getDocumentContentForNextTag(is);
        assertEquals("two", nextTagContent);
    }
}
