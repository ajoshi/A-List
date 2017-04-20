package biz.ajoshi.t1401654727.checkin.network;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ajoshi on 4/20/2017.
 */
public class HtmlReader {
    /**
     * Get the content for the next HTML tag in the stream
     * eg, if stream is at "&lt;a&gt;hi&lt;b&gt;>Bob&lt;/b&gt;&lt;/a&gt;", returns hi
     *
     * @param is InputStream to get data from
     * @return Document content for the next tag
     * @throws IOException
     */
    public String getDocumentContentForNextTag(InputStream is) throws IOException {
        // discard next tag
        readTag(is);
        // we are at the content + next tag now
        String content = readTag(is);

        //discard the closing tag
        if (content == null) {
            return content;
        }
        int indexOfNextTag = content.indexOf('<');
        if (indexOfNextTag != -1) {
            content = content.substring(0, indexOfNextTag);
        }
        return content;
    }

    public String readTag(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        boolean isInTag = true;
        StringBuilder sb = new StringBuilder();
        do {
            int thisChar = stream.read();
            if (thisChar == -1) {
                return null;
            }
            if (thisChar == '<') {
                isInTag = true;
            } else if (thisChar == '>') {
                isInTag = false;
            }
            sb.append((char) thisChar);
        } while (isInTag);
        return sb.toString();
    }
}
