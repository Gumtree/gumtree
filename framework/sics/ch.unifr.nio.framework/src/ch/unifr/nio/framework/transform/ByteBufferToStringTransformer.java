/*
 * ByteBufferToStringTransformer.java
 *
 * Created on 14.04.2008, 16:21:44
 *
 * This file is part of the NIO Framework.
 * 
 * The NIO Framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The NIO Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.unifr.nio.framework.transform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A transformer that transforms ByteBuffers into Strings.
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class ByteBufferToStringTransformer
        extends AbstractTransformer<ByteBuffer, String> {

    private final static Logger logger =
            Logger.getLogger(ByteBufferToStringTransformer.class.getName());
    private CharsetDecoder charsetDecoder;

    /**
     * creates a new ByteBufferToStringTransformer that uses the default charset
     * for transforming
     */
    public ByteBufferToStringTransformer() {
        this(Charset.defaultCharset());
    }

    /**
     * creates a new ByteBufferToStringTransformer with a given charset to use
     * @param charset
     */
    public ByteBufferToStringTransformer(Charset charset) {
        setCharsetPrivate(charset);
    }

    @Override
    public synchronized void forward(ByteBuffer input) throws IOException {
        if (nextForwarder == null) {
            logger.log(Level.SEVERE, "no nextTransformer => data lost!");
        } else {
            try {
                String string = transform(input);
                nextForwarder.forward(string);
            } catch (TransformationException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public String transform(ByteBuffer input) throws TransformationException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "transforming " + input);
        }
        String string;
        try {
            CharBuffer charBuffer = charsetDecoder.decode(input);
            string = charBuffer.toString();
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST,
                        "transformed string: \"" + string + "\"");
            }
        } catch (CharacterCodingException ex) {
        	string = "";
            ex.printStackTrace();
        }
        return string;
    }

    /**
     * sets the charset to use for transforming
     * @param charset
     */
    public synchronized void setCharset(Charset charset) {
        setCharsetPrivate(charset);
    }

    private void setCharsetPrivate(Charset charset) {
        charsetDecoder = charset.newDecoder();
    }
}
