package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MjpegRunner implements Runnable {
    private static final String CONTENT_LENGTH = "Content-Length: ";
    private static final String CONTENT_TYPE = "Content-type: image/jpeg";
    private final URL url;
    private IMjpegPanel viewer;
    private InputStream urlStream;
    private List<IRunnerListener> listeners;
    private boolean isRunning = true; //TODO should be false by default
    private boolean isPaused = false;
	private static Logger logger = LoggerFactory.getLogger(MjpegRunner.class);

    public MjpegRunner(IMjpegPanel viewer, URL url) throws IOException {
        this.viewer = viewer;
        this.url = url;
        listeners = new ArrayList<IRunnerListener>();
//        start(); //TODO remove from here
    }

    private void start() throws IOException {
//        URLConnection urlConn = url.openConnection();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        // change the timeout to taste, I like 1 second
        urlConn.setReadTimeout(1000);
        try {
            urlConn.connect();
		} catch (Exception e) {
			logger.error("failed to connect to video camera: " + url.getHost());
			throw new IOException("failed to connect to camera server: " + url.getHost());
		}
        try {
            urlStream = urlConn.getInputStream();
		} catch (SocketTimeoutException e) {
			logger.error("connection timeout. Server down or proxy misconfiguration?");
		} catch (Exception e) {
			logger.error("connection to MJpeg server failed", e);
		}
        if (urlStream == null) {
        	logger.error("failed to create video stream from video camera: " + url.getHost());
        	throw new IOException("failed to create video stream from video camera: " + url.getHost());
        }
    }

    /**
     * Stop the loop, and allow it to clean up
     */
    public synchronized void stop() {
        isRunning = false;
    }

    /**
     * Keeps running while process() returns true
     * <p>
     * Each loop asks for the next JPEG image and then sends it to our JPanel to draw
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
    	try {
			start();
		} catch (Exception e1) {
//			triggerError(e1);
		}
    	viewer.start();
    	int count = 0;
//    	long start = System.currentTimeMillis();
//    	long other = 0;
        while (isRunning) {
        	while (isPaused) {
				try {
					Thread.sleep(100);
					if (!isRunning) {
						viewer.setImageBytes(null);
			        	viewer.stop();
			        	try {
			                urlStream.close();
			            } catch (IOException ioe) {
			                System.err.println("Failed to close the stream: " + ioe);
			            }
			        	return;
					}
				} catch (InterruptedException e) {
					triggerError(e);
				}
			}
            try {
//                long begin = System.currentTimeMillis();
                byte[] imageBytes = retrieveNextImage();
//                long fetch = System.currentTimeMillis();   
//                System.out.println("set image bytes");
                viewer.setImageBytes(imageBytes);
//                long paint = System.currentTimeMillis();
//                System.out.println(String.format("Fetch = %d, Paint = %d, other = %d", 
//                		fetch - begin, paint - fetch, begin - other));
//                other = paint;
                count += 1;
                if (count % 10 == 0) {
//                	long now = System.currentTimeMillis();
//                	System.out.println(String.format(url + " FPS = %f", (count / ((now - start) / 1000.))));
                	count = 0;
//                	start = now;
                }
            } catch (SocketTimeoutException ste) {
                logger.error("failed stream read: SocketTimeout " + ste);
                viewer.setFailedString("Lost Camera connection: " + ste);
                viewer.repaint();
                ste.printStackTrace();
                triggerError(ste);
                stop();
            } catch (EOFException e) {
            	logger.error("failed stream read: EOF " + e.getLocalizedMessage());
            	triggerError(e);
                stop();
            	handleException(e);
			} catch (IOException e) {
				logger.error("failed stream read: IO " + e.getMessage());
                triggerError(e);
                stop();
            } catch (Exception e) {
            	if (e.getMessage() != null) {
            		logger.error("failed to load stream on camera server: " + url.getHost() + "; " + e.getMessage());
                	triggerError(new Exception("failed load stream on camera server: " + url.getHost() + "; " + e.getMessage()));
            	} else {
            		logger.error("failed to load stream on camera server: " + url.getHost());
            		triggerError(new Exception("failed load stream on camera server: " + url.getHost()));
            	}
            	stop();
			}
        }

        if (!isRunning) {
        	viewer.setImageBytes(null);
        	viewer.stop();
        }
        // close streams
        try {
        	if (urlStream != null) {
        		urlStream.close();
        	}
        } catch (IOException ioe) {
            logger.error("Failed to close the stream: " + ioe.getMessage());
        }
    }

    private void handleException(Exception e) {
    	while(true) {
    		try {
				start();
				break;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					break;
				}
			}
    	}
    }
    
//    private void addTimestampToFrame(BufferedImage frame) {
//    	if (frame != null) {
//    		Graphics2D g2d = (Graphics2D) frame.getGraphics().create();
//    		try {
//    			g2d.setColor(Color.WHITE);
//    			g2d.drawString(new Date().toString(), 10, frame.getHeight()-50);
//    		} finally {
//    			g2d.dispose();
//    		}
//    	}
//    }

    /**
     * Using the urlStream get the next JPEG image as a byte[]
     *
     * @return byte[] of the JPEG
     * @throws IOException
     */
    private byte[] retrieveNextImage() throws IOException {
        int currByte = -1;

//        String header = null;
        // build headers
        // the DCS-930L stops it's headers

        boolean captureContentLength = false;
        StringWriter contentLengthStringWriter = new StringWriter(128);
        StringWriter headerWriter = new StringWriter(128);

        int contentLength = 0;

//        System.out.println("begin next image");
        while (urlStream != null && (currByte = urlStream.read()) > -1) {
            if (captureContentLength) {
                if (currByte == 10 || currByte == 13) {
                    contentLength = Integer.parseInt(contentLengthStringWriter.toString());
//                    System.out.println("content length is " + contentLength);
                    break;
                }
                contentLengthStringWriter.write(currByte);

            } else {
                headerWriter.write(currByte);
                String tempString = headerWriter.toString();
                int indexOf = tempString.indexOf(CONTENT_LENGTH);
                if (indexOf > 0) {
                    captureContentLength = true;
//                    System.out.println("try to get content length");
                }
            }
        }

//        System.out.println("skip to image start");
        // 255 indicates the start of the jpeg image
        int count = 0;
        int n = 0;
        while (n != 255) {
        	n = urlStream.read();
        	if (n == -1) {
        		throw new EOFException("end of stream exception");
        	}
            // just skip extras
        	count ++;
        	if (count > 100) {
        		System.out.println("skipped more than 100 read");
        		throw new IOException("too many junk read");
        	}
        }
//        System.out.println(String.format("skipped totally %d count", count));

//        System.out.println("image started");
        // rest is the buffer
        byte[] imageBytes = new byte[contentLength + 1];
        // since we ate the original 255 , shove it back in
        imageBytes[0] = (byte) 255;
        int offset = 1;
        int numRead = 0;
        while (offset < imageBytes.length
                && (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
            offset += numRead;
        }
//        System.out.println("image finished");
        return imageBytes;
    }

    public void addRunnerListener(IRunnerListener listener) {
    	listeners.add(listener);
    }
    
    public void removeRunnerListener(IRunnerListener listener) {
    	listeners.remove(listener);
    }
    
    private void triggerError(Exception e) {
    	for (IRunnerListener listener : listeners) {
    		listener.onError(e);
    	}
    }
    
    public void setPaused(boolean isPaused) {
    	this.isPaused = isPaused;
    }
    
    public void unpause() {
    	
    }
//    // dirty but it works content-length parsing
//    private static int contentLength(String header) {
//        int indexOfContentLength = header.indexOf(CONTENT_LENGTH);
//        int valueStartPos = indexOfContentLength + CONTENT_LENGTH.length();
//        int indexOfEOL = header.indexOf('\n', indexOfContentLength);
//
//        String lengthValStr = header.substring(valueStartPos, indexOfEOL).trim();
//
//        int retValue = Integer.parseInt(lengthValStr);
//
//        return retValue;
//    }
}