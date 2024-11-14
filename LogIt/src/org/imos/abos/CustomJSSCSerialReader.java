package org.imos.abos;

import java.io.IOException;


import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;


import ocss.nmea.api.NMEAReader;
import ocss.nmea.api.NMEAEvent;
import java.util.ArrayList;

import ocss.nmea.api.NMEAListener;

public class CustomJSSCSerialReader extends NMEAReader implements SerialPortEventListener
{
	String comPort = null; // indicates use first port

	SerialPort sp;
	
	public CustomJSSCSerialReader(ArrayList<NMEAListener> al)
	{
		super(al);
	}

	public CustomJSSCSerialReader(ArrayList<NMEAListener> al, String com)
	{
		super(al);
		comPort = com;
	}

	public void read()
	{
		if (System.getProperty("verbose", "false").equals("true"))
			System.out.println("CustomJSSCSerialReader:: From " + this.getClass().getName() + " Reading Serial Port " + comPort);

		super.enableReading();

		// Opening Serial port
		String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++)
        {
            //System.out.println("CustomJSSCSerialReader::read() " + portNames[i]);
            if (comPort == null)
            {
            	comPort = portNames[i];
            }
        }

		//System.out.println("CustomJSSCSerialReader::read() Found " + portNames.length + " port(s)");

		sp = new SerialPort(comPort);

		int mask = SerialPort.MASK_RXCHAR;//Prepare mask
		try
		{
			sp.openPort();//Open port
			sp.setEventsMask(mask);
			sp.setParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);//Set params
			
			sp.addEventListener(this);//Add SerialPortEventListener
			sp.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

			//System.out.println("CustomJSSCSerialReader::read() Reading serial port...");			
			
		}
		catch (SerialPortException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//Set mask
		// Reading on Serial Port
		System.out.println("CustomJSSCSerialReader::read() Port is open..." + sp.getPortName());
	}
	
	public void close()
	{
		try
		{
			sp.closePort();
		}
		catch (SerialPortException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	StringBuffer inputBuffer = new StringBuffer();
	
	public void serialEvent(SerialPortEvent serialPortEvent)
	{
        if (serialPortEvent.isRXCHAR()) //If data is available
        {
            try 
            {
                byte buffer[] = sp.readBytes(serialPortEvent.getEventValue());
                String s = new String(buffer);
                super.fireDataRead(new NMEAEvent(this, s));
            }
            catch (SerialPortException ex) 
            {
                System.out.println(ex);
            }
        }		
	}

}
