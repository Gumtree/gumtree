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

package de.huxhorn.lilith.logback.encoder;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.spi.LoggingEvent;
import de.huxhorn.lilith.api.FileConstants;
import de.huxhorn.sulky.codec.filebuffer.DefaultFileHeaderStrategy;
import de.huxhorn.sulky.codec.filebuffer.MetaData;
import de.huxhorn.sulky.codec.filebuffer.MetaDataCodec;

public class ClassicLilithEncoder
	extends LilithEncoderBase<LoggingEvent>
{
	private boolean includeCallerData;
	private WrappingClassicEncoder wrappingEncoder;

	public ClassicLilithEncoder()
	{
		wrappingEncoder = new WrappingClassicEncoder();
		encoder=wrappingEncoder;
	}

	public boolean isIncludeCallerData()
	{
		return includeCallerData;
	}

	public void setIncludeCallerData(boolean includeCallerData)
	{
		this.includeCallerData = includeCallerData;
	}

	@Override
	public byte[] headerBytes() {
		Map<String, String> metaDataMap = new HashMap<String, String>();
		metaDataMap.put(FileConstants.CONTENT_TYPE_KEY, FileConstants.CONTENT_TYPE_VALUE_LOGGING);
		metaDataMap.put(FileConstants.CONTENT_FORMAT_KEY, FileConstants.CONTENT_FORMAT_VALUE_PROTOBUF);
		metaDataMap.put(FileConstants.COMPRESSION_KEY, FileConstants.COMPRESSION_VALUE_GZIP);
		MetaDataCodec metaCodec = new MetaDataCodec();
		MetaData metaData = new MetaData(metaDataMap, false);
		byte[] buffer = metaCodec.encode(metaData);
		ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.length + 12);
		byteBuffer.putInt(DefaultFileHeaderStrategy.CODEC_FILE_HEADER_MAGIC_VALUE);
		byteBuffer.putInt(FileConstants.MAGIC_VALUE);
		byteBuffer.putInt(buffer.length);
		byteBuffer.put(buffer);
		return byteBuffer.array();
	}

	@Override
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

	@Override
	public byte[] encode(LoggingEvent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] footerBytes() {
		// TODO Auto-generated method stub
		return null;
	}

}
