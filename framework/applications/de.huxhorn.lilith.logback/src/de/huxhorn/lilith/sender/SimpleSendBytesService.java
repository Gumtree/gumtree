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

package de.huxhorn.lilith.sender;

import de.huxhorn.sulky.io.IOUtilities;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleSendBytesService
	implements SendBytesService
{
	/**
	 * The default reconnection delay (30000 milliseconds or 30 seconds).
	 */
	public static final int DEFAULT_RECONNECTION_DELAY = 30000;

	public static final int DEFAULT_QUEUE_SIZE = 1000;

	public static final int DEFAULT_POLL_INTERVALL = 100;

	private final Object lock = new Object();
	private final BlockingQueue<byte[]> localEventBytes;

	private WriteByteStrategy writeByteStrategy;
	private DataOutputStreamFactory dataOutputStreamFactory;
	//private boolean shutdown;
	private final int reconnectionDelay;
	private final int queueSize;
	private final int pollIntervall;

	private final AtomicReference<ConnectionState> connectionState=new AtomicReference<ConnectionState>(ConnectionState.Offline);
	private final AtomicBoolean shutdown=new AtomicBoolean(false);
	private SendBytesThread sendBytesThread;
	private boolean debug;

	public SimpleSendBytesService(DataOutputStreamFactory dataOutputStreamFactory, WriteByteStrategy writeByteStrategy)
	{
		this(dataOutputStreamFactory, writeByteStrategy, DEFAULT_QUEUE_SIZE, DEFAULT_RECONNECTION_DELAY, DEFAULT_POLL_INTERVALL);
	}

	public SimpleSendBytesService(DataOutputStreamFactory dataOutputStreamFactory, WriteByteStrategy writeByteStrategy, int queueSize, int reconnectionDelay, int pollIntervall)
	{
		if(dataOutputStreamFactory == null)
		{
			throw new IllegalArgumentException("dataOutputStreamFactory must not be null!");
		}
		if(writeByteStrategy == null)
		{
			throw new IllegalArgumentException("writeByteStrategy must not be null!");
		}
		if(queueSize <= 0)
		{
			throw new IllegalArgumentException("queueSize must be greater than zero!");
		}
		if(reconnectionDelay <= 0)
		{
			throw new IllegalArgumentException("reconnectionDelay must be greater than zero!");
		}
		if(pollIntervall <= 0)
		{
			throw new IllegalArgumentException("pollIntervall must be greater than zero!");
		}
		this.localEventBytes = new ArrayBlockingQueue<byte[]>(queueSize, true);
		this.dataOutputStreamFactory = dataOutputStreamFactory;
		this.writeByteStrategy = writeByteStrategy;
		this.queueSize = queueSize;
		this.reconnectionDelay = reconnectionDelay;
		this.pollIntervall = pollIntervall;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public ConnectionState getConnectionState()
	{
		return connectionState.get();
	}

	public void sendBytes(byte[] bytes)
	{
		if(connectionState.get() == ConnectionState.Connected && sendBytesThread != null && bytes != null)
		{
			try
			{
				localEventBytes.put(bytes);
			}
			catch(InterruptedException e)
			{
				// ignore
			}
		}
	}

	public void startUp()
	{
		synchronized(lock)
		{
			if(sendBytesThread == null)
			{
				shutdown.set(false);
				sendBytesThread = new SendBytesThread();
				sendBytesThread.start();
			}
		}
	}

	public void shutDown()
	{
		shutdown.set(true);
		synchronized(lock)
		{
			connectionState.set(ConnectionState.Canceled);
		}
		if(sendBytesThread != null)
		{
			sendBytesThread.interrupt();
			try
			{
				sendBytesThread.join();
			}
			catch(InterruptedException e)
			{
				// this is ok
			}
			sendBytesThread = null;
		}
		localEventBytes.clear();
	}

	private class SendBytesThread
		extends Thread
	{
		private DataOutputStream dataOutputStream;

		public SendBytesThread()
		{
			super("SendBytes@" + dataOutputStreamFactory);
			setDaemon(true);
		}

		public void closeConnection()
		{
			synchronized(lock)
			{
				if(dataOutputStream != null)
				{
					//IOUtilities.closeQuietly(dataOutputStream);
					// the above call can result in a ClassNotFoundException if a
					// webapp is already unloaded!!!
					try
					{
						dataOutputStream.close();
					}
					catch(IOException e)
					{
						// ignore
					}
					dataOutputStream = null;
					if(connectionState.get() != ConnectionState.Canceled)
					{
						connectionState.set(ConnectionState.Offline);
					}
					if(debug)
					{
						System.err.println("Closed dataOutputStream.");
					}
				}
				lock.notifyAll();
			}
		}

		public void run()
		{
			Thread reconnectionThread = new ReconnectionThread();
			reconnectionThread.start();

			List<byte[]> copy = new ArrayList<byte[]>(queueSize);
			for(; ;)
			{
				try
				{
					localEventBytes.drainTo(copy);
					if(copy.size() > 0)
					{
						DataOutputStream outputStream;
						synchronized(lock)
						{
							outputStream = dataOutputStream;
						}
						if(outputStream != null)
						{
//								System.out.println(this+" - about to write "+copy.size()+" events...");
							try
							{
								for(byte[] current : copy)
								{
									writeByteStrategy.writeBytes(outputStream, current);
								}
								outputStream.flush();
//									System.out.println(this+" wrote "+copy.size()+" events.");
							}
							catch(Throwable e)
							{
								IOUtilities.interruptIfNecessary(e);
								closeConnection();
							}
						}
						copy.clear();
					}
//						else
//						{
//							System.out.println(this+" ignored "+copy.size()+" events because of missing connection.");
//						}
					if(shutdown.get())
					{
						break;
					}
					Thread.sleep(pollIntervall);
				}
				catch(InterruptedException e)
				{
					break;
					//e.printStackTrace();
				}
			}
			reconnectionThread.interrupt();
			try
			{
				reconnectionThread.join();
			}
			catch(InterruptedException e1)
			{
				// this is ok.
			}
			closeConnection();
		}

		private class ReconnectionThread
			extends Thread
		{
			public ReconnectionThread()
			{
				super("Reconnection@" + dataOutputStreamFactory);
				setDaemon(true);
			}

			public void run()
			{
				for(; ;)
				{
					boolean connect = false;
					synchronized(lock)
					{
						if(dataOutputStream == null && connectionState.get() != ConnectionState.Canceled)
						{
							connect = true;
							connectionState.set(ConnectionState.Connecting);
						}
					}
					DataOutputStream newStream = null;
					if(connect)
					{
						try
						{
							newStream = dataOutputStreamFactory.createDataOutputStream();
						}
						catch(IOException e)
						{
							// ignore
						}
					}

					synchronized(lock)
					{
						if(connect)
						{
							if(newStream != null)
							{
								if(connectionState.get() == ConnectionState.Canceled)
								{
									// cleanup
									try
									{
										newStream.close();
									}
									catch(IOException e)
									{
										// ignore
									}
								}
								else
								{
									dataOutputStream = newStream;
									connectionState.set(ConnectionState.Connected);
								}
							}
							else if(connectionState.get() != ConnectionState.Canceled)
							{
								connectionState.set(ConnectionState.Offline);
							}
						}
						try
						{
							lock.wait(reconnectionDelay);
						}
						catch(InterruptedException e)
						{
							return;
						}
					}
				}
			}
		}
	}
}
