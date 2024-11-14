package org.imos.abos;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.swing.JTextArea;

import jssc.SerialPortException;
import jssc.SerialPortList;
import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.StringParsers;
import ocss.nmea.parser.UTC;

public class SerialJSSCEventGenerator extends NMEAListener implements SerialPortEventListener
{
	String comPort = null;
	OutputStream out;
	EventWriter send;
	SerialPort sp;

	public SerialJSSCEventGenerator(FileOutputStream out, EventWriter send)
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
			System.out.println("SerialJSSCEventGenerator:: From " + this.getClass().getName() + " Reading Serial Port " + comPort);

		// Opening Serial port
		String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++)
        {
            //System.out.println("SerialJSSCEventGenerator::read() " + portNames[i]);
            if (comPort == null)
            {
            	comPort = portNames[i];
            }
        }

		//System.out.println("SerialJSSCEventGenerator::read() Found " + portNames.length + " port(s)");

		sp = new SerialPort(comPort);

		int mask = SerialPort.MASK_RXCHAR;//Prepare mask
		try
		{
			sp.openPort();//Open port
			sp.setEventsMask(mask);
			sp.setParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);//Set params
			
			sp.addEventListener(this);//Add SerialPortEventListener
			sp.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

			//System.out.println("SerialJSSCEventGenerator::read() Reading serial port...");			
			
		}
		catch (SerialPortException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//Set mask
		// Reading on Serial Port
		System.out.println("SerialJSSCEventGenerator::read() Port is open..." + sp.getPortName());
	}

	StringBuffer inputBuffer = new StringBuffer();

	public void serialEvent(SerialPortEvent serialPortEvent)
	{
        if (serialPortEvent.isRXCHAR()) // If data is available
        {
            try 
            {
                byte buffer[] = sp.readBytes(serialPortEvent.getEventValue());
                try
				{
					inputBuffer.append(new String(buffer, "UTF-8"));
				}
				catch (UnsupportedEncodingException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                int i = inputBuffer.indexOf("\n");
                if (i > 0)
                {
					String s = inputBuffer.subSequence(0, i-1).toString(); // i-1 removes the \n
					System.out.println("serial Event " + s);

					Event v = new Event(new Date(), pos, s);
					
					send.write(v);

					inputBuffer.delete(0, i+1); // delete all of the data including the \n
                	
                }
            }
            catch (SerialPortException ex) 
            {
                System.out.println(ex);
            }
        }				
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
