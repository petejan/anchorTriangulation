package org.imos.abos;

import java.io.IOException;

import gnu.io.SerialPortEvent;
import ocss.nmea.api.NMEAReader;
import ocss.nmea.api.NMEAEvent;

import java.util.ArrayList;
import java.util.StringTokenizer;

import gnu.io.PortInUseException;
import gnu.io.NoSuchPortException;
import gnu.io.CommPort;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortOwnershipListener;
import gnu.io.SerialPortEventListener;
import ocss.nmea.api.NMEAListener;

public class CustomUDPReader extends NMEAReader
{
	int port = 50100;

	public CustomUDPReader(ArrayList<NMEAListener> al)
	{
		super(al);
	}

	public CustomUDPReader(ArrayList<NMEAListener> al, int port)
	{
		super(al);
		this.port = port;
	}

	InputStream theInput = null;

	public void read() throws SocketException
	{
		if (System.getProperty("verbose", "false").equals("true"))
			System.out.println("From " + this.getClass().getName() + " UDP port " + port);

		super.enableReading();
		MulticastSocket sock = null;
		InetAddress group = null;
			try
			{
				group = InetAddress.getByName(Common.getProp("gpsmulticastaddress","224.0.36.0"));
				System.out.println("Group " + group);
				int port = Integer.parseInt(Common.getProp("gpsmulticastport", "50102"));
				System.out.println("port " + port);
				
				sock = new MulticastSocket(port);

				System.out.println("Socket " + sock.getPort());
				
				sock.joinGroup(group);
	
				sock.setReuseAddress(true);
			}
			catch (UnknownHostException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		// Receiving
//		final DatagramSocket sock = new DatagramSocket(port);
		sock.setReuseAddress(true);

		String s;
		String packet;
		byte[] buf = new byte[4096];
		DatagramPacket pack = new DatagramPacket(buf, buf.length);
		pack.setPort(port);

		while (true)
		{
			try
			{
				sock.receive(pack);
				// System.out.println("CustomUDPReader::receive " + pack);

				s = new String(pack.getData());

				StringTokenizer st = new StringTokenizer(s, "\n\r");
				while (st.hasMoreElements())
				{
					packet = st.nextToken().trim() + "\r\n";
					if (packet.length() > 0)
					{
						NMEAEvent ev = new NMEAEvent(this, packet);

						// System.out.println("Line : " + packet + ":" + ev);

						super.fireDataRead(ev);
					}
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
