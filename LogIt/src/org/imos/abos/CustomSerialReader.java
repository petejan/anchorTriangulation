package org.imos.abos;

import java.io.IOException;

import gnu.io.SerialPortEvent;

import ocss.nmea.api.NMEAReader;
import ocss.nmea.api.NMEAEvent;
import java.util.ArrayList;

import gnu.io.PortInUseException;
import gnu.io.NoSuchPortException;
import gnu.io.CommPort;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.util.Enumeration;

import java.util.TooManyListenersException;

import gnu.io.CommPortOwnershipListener;
import gnu.io.SerialPortEventListener;

import ocss.nmea.api.NMEAListener;

public class CustomSerialReader extends NMEAReader implements SerialPortEventListener, CommPortOwnershipListener
{
	String comPort = null; // indicates use first port

	SerialPort sp;
	
	public CustomSerialReader(ArrayList<NMEAListener> al)
	{
		super(al);
	}

	public CustomSerialReader(ArrayList<NMEAListener> al, String com)
	{
		super(al);
		comPort = com;
	}

	InputStream theInput = null;

	public void read()
	{
		if (System.getProperty("verbose", "false").equals("true"))
			System.out.println("From " + this.getClass().getName() + " Reading Serial Port " + comPort);

		super.enableReading();

		// Opening Serial port
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		int nbp = 0;
		while (enumeration.hasMoreElements())
		{
			CommPortIdentifier cpi = (CommPortIdentifier) enumeration.nextElement();
			// System.out.println("Port:" + cpi.getName());
			if (comPort == null)
			{
				if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL)
				{
					comPort = cpi.getName();
				}
			}
			nbp++;
		}
		System.out.println("CustomSerialReader::read() Found " + nbp + " port(s)");

		CommPortIdentifier com = null;
		try
		{
			com = CommPortIdentifier.getPortIdentifier(comPort);
		}
		catch (NoSuchPortException nspe)
		{
			System.err.println("CustomSerialReader::read() No Such Port");
			nspe.printStackTrace();
			return;
		}
		CommPort thePort = null;
		try
		{
			com.addPortOwnershipListener(this);
			thePort = com.open("NMEAPort", 10000);
		}
		catch (PortInUseException piue)
		{
			System.err.println("CustomSerialReader::read() Port In Use");
			return;
		}
		int portType = com.getPortType();
		if (portType == CommPortIdentifier.PORT_PARALLEL)
			System.out.println("CustomSerialReader::read() This is a parallel port");
		else if (portType == CommPortIdentifier.PORT_SERIAL)
			System.out.println("CustomSerialReader::read() This is a serial port");
		else
			System.out.println("CustomSerialReader::read() This is an unknown port:" + portType);

		if (portType == CommPortIdentifier.PORT_SERIAL)
		{
			sp = (SerialPort) thePort;
			try
			{
				sp.addEventListener(this);
			}
			catch (TooManyListenersException tmle)
			{
				sp.close();
				System.err.println(tmle.getMessage());
				return;
			}
			sp.notifyOnDataAvailable(true);
			try
			{
				sp.enableReceiveTimeout(30);
			}
			catch (UnsupportedCommOperationException ucoe)
			{
				sp.close();
				System.err.println(ucoe.getMessage());
				return;
			}
			try
			{
				sp.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			}
			catch (UnsupportedCommOperationException ucoe)
			{
				System.err.println("CustomSerialReader::read() Unsupported Comm Operation");
				return;
			}

			try
			{
				sp.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				theInput = sp.getInputStream();
				System.out.println("CustomSerialReader::read() Reading serial port...");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// Reading on Serial Port
		System.out.println("CustomSerialReader::read() Port is open...");
	}
	
	public void close()
	{
		sp.close();
	}

	public void serialEvent(SerialPortEvent serialPortEvent)
	{
		switch (serialPortEvent.getEventType())
		{
			case SerialPortEvent.DATA_AVAILABLE:
				if (canRead())
				{
					try
					{
						StringBuffer inputBuffer = new StringBuffer();
						int newData = 0;
						while (newData != -1)
						{
							try
							{
								newData = theInput.read();
								if (newData == -1)
									break;
								inputBuffer.append((char) newData);
							}
							catch (IOException ex)
							{
								System.err.println(ex);
								return;
							}
						}
						String s = new String(inputBuffer);
						// Display the read string
						boolean justDump = false;
						if (justDump)
							System.out.println(":: [" + s + "] ::");
						else
							super.fireDataRead(new NMEAEvent(this, s));
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				else
					System.out.println("CustomSerialReader::serialEvent() Stop Reading serial port.");
			default:
				break;
		}
	}

	public void ownershipChange(int type)
	{
		if (type == CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED)
		{
			System.out.println("PORT_OWNERSHIP_REQUESTED");
		}
		else
			System.out.println("CustomSerialReader::ownershipChange() ownership changed:" + type);
	}
}
