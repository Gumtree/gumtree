/*******************************************************************************
 * Copyright (c) 2004  Australian Nuclear Science and Technology Organisation.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * GumTree Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.internal.core.io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gumtree.gumnix.sics.core.io.ISicsData;
import org.gumtree.gumnix.sics.core.io.ISycamoreResponse;

/**
 * 
 */
/**
 * @author tla
 *
 */
public class SycamoreResponse implements ISycamoreResponse {

	private static Pattern tagPattern = Pattern.compile("\\[.+\\]");
	private static Pattern messageSeparatorPattern = Pattern.compile(",");
	private static Pattern tagSeparatorPattern = Pattern.compile(":");
	private static final String TAG_END_LIST = "ENDLIST";
	
	private String response;
	private String connId;
	private int transactionId;
	private String deviceId;
	private String tag;
	private String message;
	private List<ISicsData> values;
	private String key;
	
	/**
	 * Constructs a sycamore response from the output by sics.
	 * 
	 * @param response a single line output from sics
	 */
	public SycamoreResponse(String response) {
//		long startTime = System.nanoTime();
		if(response == null || response.length() == 0)
			throw new IllegalArgumentException("No response text");
		this.response = response.trim();
		if(!validateTag())
			throw new IllegalArgumentException("Illegal tag in response text");

		if(getMessage().length() != 0) {
			try{
				parseMessage();
			} catch (Exception e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		}
//		long endTime = System.nanoTime();
//		System.out.println("Parse time: " + ((endTime - startTime) / 1000000) + "ms");
	}
	
	private boolean validateTag() {
		// find sycamore tag [xx:xx:xx:xx] xxxx
		Matcher tagMatcher = tagPattern.matcher(getResponse());
		if(!tagMatcher.find())
			return false;
		
		// find message
		String messageTag = tagMatcher.group(0);
		message = getResponse().substring(messageTag.length()).trim();
		
		// process tag data
		messageTag = messageTag.substring(1, messageTag.length() - 1);
		String[] messageTagData = tagSeparatorPattern.split(messageTag, 0);
		if(messageTagData.length != 4)
			return false;
		connId = messageTagData[0];
		if(connId == null)
			return false;
		try {
			transactionId = Integer.parseInt(messageTagData[1].substring(1));
		} catch (NumberFormatException ex) {
			return false;
		}
		deviceId = messageTagData[2];
		if(deviceId == null)
			return false;
		tag = messageTagData[3];
		if(tag == null)
			return false;
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see sycamoretest.io.ISycamoreResponse#getResponse()
	 */
	public String getResponse() {
		return response;
	}
	
	/* (non-Javadoc)
	 * @see sycamoretest.io.ISycamoreResponse#getMessage()
	 */
	public String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISycamoreResponse#getConnId()
	 */
	public String getConnId() {
		return connId;
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISycamoreResponse#getDeviceId()
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISycamoreResponse#getTag()
	 */
	public String getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISycamoreResponse#getTransactionId()
	 */
	public int getTransactionId() {
		return transactionId;
	}
	
	private void parseMessage() throws Exception {
		// initialise
		values = new ArrayList<ISicsData>(2);
		int index = getMessage().indexOf("=");
		// invalid expression
		if (index == -1) {
			throw new Exception("invalid expression");
		}
		// get reply key
		key = getMessage().substring(0, index).trim();
		// get reply data
		String dataExp = getMessage().substring(index + 1, getMessage().length()).trim();
		if (dataExp.length() >= 2) {
			// group expressions
			if (dataExp.charAt(0) == '{'
					&& dataExp.charAt(dataExp.length() - 1) == '}') {
				String groupExp = dataExp.substring(1, dataExp.length() - 1);
				String[] singleExps = messageSeparatorPattern.split(groupExp, 0);
				for (String singleExp : singleExps) {
					index = singleExp.indexOf("=");
					// found key value pair
					if (index != -1) {
						KeyValueData keyValueData = new KeyValueData(singleExp.substring(0,
								index).trim(), singleExp.substring(index + 1, singleExp
										.length()).trim());
						if(keyValueData.getValue().length() <= 0)
							throw new Exception("invalid value");
						values.add(keyValueData);
					} else {
						String singleData = singleExp.trim();
						if(singleData.length() <= 0)
							throw new Exception("invalid value");
						if(!singleData.equals(TAG_END_LIST))
							values.add(new SingleData(singleData));
					}
				}
			} else {
				// single value with lenght greater than or equals to 2
				if(!dataExp.equals(TAG_END_LIST))
					values.add(new SingleData(dataExp));
			}
		} else {
			if(dataExp.trim().length() <= 0)
				throw new Exception("invalid value");
			// single value with lenght less than 2
			values.add(new SingleData(dataExp));
		}
    }
    
    /* (non-Javadoc)
     * @see sycamoretest.io.ISycamoreResponse#getMessageKey()
     */
    public String getMessageKey() {
    	return key;
    }
    
    /* (non-Javadoc)
     * @see sycamoretest.io.ISycamoreResponse#getMessageValues()
     */
    public ISicsData[] getMessageValues() {
    	return values.toArray(new ISicsData[values.size()]);
    }
	
}
