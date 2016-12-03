import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.StringParsers;

public class common
{
	static Properties prop;
	
	public static void setPropFile(String propFileName) throws IOException
	{
		prop = new Properties();
		
		InputStream inputStream = common.class.getClassLoader().getResourceAsStream(propFileName);
		 
		if (inputStream != null) 
		{
			prop.load(inputStream);
		} 
		else 
		{
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}		 
	}
	public static String getProp(String name, String def)
	{
		String s = prop.getProperty(name);
		
		System.out.println("Property value " + name + " " + s);
		if (s == null)
			return def;
		
		return s;
	}
	
	public static Connection getConnection()
	{
		Connection connection = null;
		try 
		{			
			Class.forName(getProp("driver", "org.postgresql.Driver")); 
			System.out.println("PostgreSQL JDBC Driver Registered!");
			
			connection = DriverManager.getConnection(getProp("database","jdbc:postgresql://150.229.234.84:5432/voyage"), 
														getProp("dbuser", "pete"), 
														getProp("dbpassword", "password")); 
		} 
		catch (ClassNotFoundException e) 
		{
			System.out.println("Where is your PostgreSQL JDBC Driver? Include in your library path!");
			e.printStackTrace();
			
			return connection;
		}
		catch (SQLException e) 
		{
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		
			return connection;
		}

		if (connection != null) 
		{
			System.out.println("You made it, take control your database now!");
		} 
		else 
		{
			System.out.println("Failed to make connection!");
		}	
		
		return connection;
	}
	
	static GeoPos pos = null;
	static int head = -1000;
	static Date ut = null;
	
	public static class GpsThread implements Runnable 
	{
		int port = Integer.parseInt(getProp("gpsport", "3002"));
		
		public void run()
		{
			// Receiving
			DatagramSocket sock = null;
			try 
			{
				sock = new DatagramSocket(port);
				sock.setReuseAddress(true);
			} 
			catch (SocketException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
			String s;
			String packet;
			byte[] buf = new byte[4096];
			DatagramPacket pack = new DatagramPacket(buf, buf.length);
			pack.setPort(port);
			
			System.out.println("Listening on port " + pack.getPort());
	
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
	
							String nmea = ev.getContent();
							if (nmea.length() > 10)
							{
								//System.out.print("Line : " + packet);
								
								String key = nmea.substring(3, 6);
								if (key.equals("GGA"))
								{
									pos = (GeoPos) StringParsers.parseGGA(nmea).get(1);								
								}
								else if (key.equals("HDT"))
								{
									head = StringParsers.parseHDT(nmea);
								}
								else if (key.equals("ZDA"))
								{
									try 
									{
										ut = StringParsers.parseZDA(nmea);
									} 
									catch (ParseException e) 
									{
										e.printStackTrace();
									}
								}
							}
							
						}
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			// sock.close();
		}
	}
	

	
}
