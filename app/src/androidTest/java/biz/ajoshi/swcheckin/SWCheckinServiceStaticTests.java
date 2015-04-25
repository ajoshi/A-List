package biz.ajoshi.swcheckin;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import biz.ajoshi.t1401654727.checkin.services.SWCheckinService;

/**
 * tests for static methods in SWCheckinService
 */
public class SWCheckinServiceStaticTests extends TestCase {
    private static final String TAG_STRING = "<one>two<three>four<five>";
    private static final String LONG_STRING = "This is a long string\n that one might think would end. ";

    public void testReadTag() throws IOException {
        InputStream is = new ByteArrayInputStream(TAG_STRING.getBytes());
        String outputTag = SWCheckinService.readTag(is);
        String outputTag2 = SWCheckinService.readTag(is);
        assertEquals("<one>", outputTag);
        assertEquals("two<three>", outputTag2);
    }

    public void testReadTagNullInput() throws IOException {
        assertEquals(null, SWCheckinService.readTag(null));
    }

    public void testReadTagReadsUntilTagEnd() throws IOException {
        String tagString = LONG_STRING + ">";
        InputStream is = new ByteArrayInputStream(tagString.getBytes());
        assertEquals(tagString, SWCheckinService.readTag(is));
    }

    public void testReadTagReadsIfNoTagEnd() throws IOException {

        InputStream is = new ByteArrayInputStream(LONG_STRING.getBytes());
        assertEquals(LONG_STRING, SWCheckinService.readTag(is));
    }

    public void testGetDocumentContentForNextTag() throws IOException {
        InputStream is = new ByteArrayInputStream(TAG_STRING.getBytes());
        String nextTagContent = SWCheckinService.getDocumentContentForNextTag(is);
        assertEquals("two", nextTagContent);
    }
}
