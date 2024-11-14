package org.imos.abos;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.CommPortOwnershipListener;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.swing.JTextArea;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.StringParsers;
import ocss.nmea.parser.UTC;

public class SerialEventGenerator extends NMEAListener implements CommPortOwnershipListener, SerialPortEventListener
{
	String comPort = null;
	InputStream theInput;
	OutputStream out;
	EventWriter send;

	public SerialEventGenerator(FileOutputStream out, EventWriter send)
	{
		this.out = out;
		this.send = send;
	}

	public void setComPort(String c)
	{
		comPort = c;
	}
	
	public void read()
	{
		if (System.getProperty("verbose", "false").equals("true"))
			System.out.println("From " + this.getClass().getName() + " Reading Serial Port " + comPort);

		// Opening Serial port
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		int nbp = 0;
		while (enumeration.hasMoreElements())
		{
			CommPortIdentifier cpi = (CommPortIdentifier) enumeration.nextElement();
			System.out.println("Port:" + cpi.getName());
			if (comPort == null)
			{
				if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL)
				{
					comPort = cpi.getName(); // use the first one we find
				}
			}
			nbp++;
		}
		System.out.println("Found " + nbp + " port(s)");

		CommPortIdentifier com = null;
		try
		{
			com = CommPortIdentifier.getPortIdentifier(comPort);
		}
		catch (NoSuchPortException nspe)
		{
			System.err.println("No Such Port");
			nspe.printStackTrace();
			return;
		}
		CommPort thePort = null;
		try
		{
			com.addPortOwnershipListener(this);
			thePort = com.open("SerialEventPort", 10000);
		}
		catch (PortInUseException piue)
		{
			System.err.println("Port In Use");
			return;
		}
		int portType = com.getPortType();
		if (portType == CommPortIdentifier.PORT_PARALLEL)
			System.out.println("This is a parallel port");
		else if (portType == CommPortIdentifier.PORT_SERIAL)
			System.out.println("This is a serial port");
		else
			System.out.println("This is an unknown port:" + portType);
		if (portType == CommPortIdentifier.PORT_SERIAL)
		{
			SerialPort sp = (SerialPort) thePort;
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
				System.err.println("Unsupported Comm Operation");
				return;
			}
			try
			{
				sp.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				theInput = sp.getInputStream();
				System.out.println("Reading serial port...");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// Reading on Serial Port
		System.out.println("Port is open...");
	}

	StringBuffer inputBuffer = new StringBuffer();

	public void serialEvent(SerialPortEvent serialPortEvent)
	{
		// System.out.println("serialEvent " + serialPortEvent);
		switch (serialPortEvent.getEventType())
		{
		case SerialPortEvent.DATA_AVAILABLE:
			try
			{
				int newData = 0;
				while (newData != -1)
				{
					try
					{
						newData = theInput.read();
						if (newData == -1)
							break;
						if (newData == '\n')
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
				{
					System.out.println(":: [" + s + "] ::");
					inputBuffer.setLength(0);
				}
				else
				{
					if (newData == '\n')
					{
						System.out.println("String Event " + inputBuffer);

						Event v = new Event(new Date(), pos, s.substring(0, s.length() - 1));
						
						send.write(v);

						inputBuffer.setLength(0);
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		default:
			break;
		}
	}

	@Override
	public void ownershipChange(int type)
	{
		if (type == CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED)
		{
			System.out.println("PORT_OWNERSHIP_REQUESTED");
		}
		else
			System.out.println("ownership changed:" + type);
	}

	GeoPos pos;
	UTC ut;

	public void dataDetected(NMEAEvent e)
	{
		String s = e.getContent();

		// System.out.println("SerialEventGenerator::Received:" + s);
		String key = s.substring(3, 6);
		if (key.equals("GGA"))
		{
			pos = (GeoPos) StringParsers.parseGGA(s).get(1);
			ut = (UTC) StringParsers.parseGGA(s).get(0);
		}
	}

}
