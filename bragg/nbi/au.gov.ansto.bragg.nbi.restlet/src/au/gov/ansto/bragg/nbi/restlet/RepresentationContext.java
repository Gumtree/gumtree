/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package au.gov.ansto.bragg.nbi.restlet;

import java.io.IOException;
import java.io.InputStream;

import org.restlet.representation.Representation;

import au.gov.ansto.bragg.nbi.restlet.fileupload.RequestContext;

/**
 * Provides access to the representation information needed by the FileUpload
 * processor.
 * 
 * @author Jerome Louvel
 */
public class RepresentationContext implements RequestContext {
    /** The representation to adapt. */
    private volatile Representation multipartForm;

    /**
     * Constructor.
     * 
     * @param multipartForm
     *            The multipart form to parse.
     */
    public RepresentationContext(Representation multipartForm) {
        this.multipartForm = multipartForm;
    }

    /**
     * Returns the character encoding for the form.
     * 
     * @return The character encoding for the form.
     */
    public String getCharacterEncoding() {
        if (this.multipartForm.getCharacterSet() != null) {
            return this.multipartForm.getCharacterSet().getName();
        }

        return null;
    }

    /**
     * Returns the content length of the form.
     * 
     * @return The content length of the form.
     */
    public int getContentLength() {
        return (int) this.multipartForm.getSize();
    }

    /**
     * Returns the content type of the form.
     * 
     * @return The content type of the form.
     */
    public String getContentType() {
        if (this.multipartForm.getMediaType() != null) {
            return this.multipartForm.getMediaType().toString();
        }

        return null;
    }

    /**
     * Returns the input stream.
     * 
     * @return The input stream.
     */
    public InputStream getInputStream() throws IOException {
        return this.multipartForm.getStream();
    }

}
