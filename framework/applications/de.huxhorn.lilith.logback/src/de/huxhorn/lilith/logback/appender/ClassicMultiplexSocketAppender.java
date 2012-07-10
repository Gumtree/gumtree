/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2011 Joern Huxhorn
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2011 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.lilith.logback.appender;

import de.huxhorn.lilith.data.logging.logback.TransformingEncoder;
import de.huxhorn.lilith.data.logging.protobuf.LoggingEventProtobufEncoder;

import ch.qos.logback.classic.spi.LoggingEvent;

public class ClassicMultiplexSocketAppender
	extends MultiplexSocketAppenderBase<LoggingEvent>
{
	/**
	 * The default port number of compressed new-style remote logging server (10000).
	 */
	public static final int COMPRESSED_DEFAULT_PORT = 10000;

	/**
	 * The default port number of uncompressed new-style remote logging server (10001).
	 */
	public static final int UNCOMPRESSED_DEFAULT_PORT = 10001;

	private boolean includeCallerData;
	private boolean compressing;
	private boolean usingDefaultPort;
	private TransformingEncoder transformingEncoder;

	public ClassicMultiplexSocketAppender()
	{
		this(true);
	}

	public ClassicMultiplexSocketAppender(boolean compressing)
	{
		super();
		usingDefaultPort = true;
		transformingEncoder = new TransformingEncoder(true);
		setEncoder(transformingEncoder);
		setCompressing(compressing);
		includeCallerData = false;
	}

	protected void applicationIdentifierChanged()
	{
		transformingEncoder.setApplicationIdentifier(getApplicationIdentifier());
	}

	@Override
	public void setPort(int port)
	{
		super.setPort(port);
		usingDefaultPort = false;
	}

	/**
	 * GZIPs the event if set to true.
	 * <p/>
	 * Automatically chooses the correct default port if it was not previously set manually.
	 *
	 * @param compressing if events will be gzipped or not.
	 */
	public void setCompressing(boolean compressing)
	{
		this.compressing = compressing;
		if(usingDefaultPort)
		{
			if(compressing)
			{
				setPort(COMPRESSED_DEFAULT_PORT);
			}
			else
			{
				setPort(UNCOMPRESSED_DEFAULT_PORT);
			}
			usingDefaultPort = true;
		}
		transformingEncoder.setLilithEncoder(new LoggingEventProtobufEncoder(compressing));
	}

	public boolean isCompressing()
	{
		return compressing;
	}

	public boolean isIncludeCallerData()
	{
		return includeCallerData;
	}

	public void setIncludeCallerData(boolean includeCallerData)
	{
		this.includeCallerData = includeCallerData;
	}

	protected void preProcess(LoggingEvent event)
	{
		if(event != null)
		{
			if(includeCallerData)
			{
				event.getCallerData();
			}
		}
	}
}
