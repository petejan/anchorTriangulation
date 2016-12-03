import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.StringParsers;

public class MulticastGpsLog 
{
	Connection connection = null;
	PreparedStatement preparedStatement = null;
 	
	public MulticastGpsLog() throws SQLException
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		connection = common.getConnection();
		
//        Table "public.track"
//		Column   |            Type             | Modifiers 
//		-----------+-----------------------------+-----------
//		timestamp | timestamp without time zone | 
//		voyage    | character varying           | 
//		latitude  | double precision            | 
//		longitude | double precision            | 
//		altitude  | double precision            | 
//		heading   | double precision            | 
//		the_geom  | geometry                    | 


		String insertTableSQL = "INSERT INTO track (timestamp, voyage, latitude, longitude, altitude, heading, the_geom) "+
									" VALUES (?,?,?,?,?,?,ST_GeomFromText(?, 4326))";
		
		preparedStatement = connection.prepareStatement(insertTableSQL);
	}

	public void run() throws SocketException, SQLException
	{
		MulticastSocket sock = null;
		InetAddress group = null;
			try
			{
				group = InetAddress.getByName(common.getProp("gpsmulticastaddress","224.0.36.0"));
				System.out.println("Group " + group);
				int port = Integer.parseInt(common.getProp("gpsport", "50102"));
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
		String s;
		String packet;
		byte[] buf = new byte[4096];
		DatagramPacket pack = new DatagramPacket(buf, buf.length);
		
		System.out.println("Listening on port " + pack.getPort());

		GeoPos pos = null;
		int head = -1000;
		Date ut = null;
		long lastt = new Date().getTime();

		while (true)
		{
			try
			{
				sock.receive(pack);
				//System.out.println("GpsLog::receive " + pack);

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
								//System.out.println("UTC " + StringParsers.parseGGA(nmea).get(0));
								
								long t = new Date().getTime();
								if (ut != null)
								{
									t = ut.getTime();
								}
								t = (t / 1000) * 1000; // round to nearest second
								//System.out.println("time " + t + " mod 5 " + (t % 5000));								
								
								if (((t % 5000) == 0) && (lastt != t)) // only insert every 5 seconds
								{
									lastt = t;
									Timestamp ts = new Timestamp(t);
									
									preparedStatement.setTimestamp(1, ts);
									preparedStatement.setString(2, "IN-2015-V01");
									preparedStatement.setDouble(3, pos.lat);
									preparedStatement.setDouble(4, pos.lng);
									preparedStatement.setDouble(5, 0);
									preparedStatement.setDouble(6, head);
									preparedStatement.setObject(7, "POINT("+pos.lng+" "+pos.lat+")");
									
									System.out.println("Insert :" + preparedStatement);
		
									// execute insert SQL stetement
									try
									{
										preparedStatement.executeUpdate();
									}
									catch (SQLException ex)
									{
										System.out.println("SQL EXCEPTION " + ex.getMessage());
									}
								}
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
	
	public static void main(String[] args) 
	{
		try 
		{
			common.setPropFile("ddls.properties");
			
			MulticastGpsLog gps = new MulticastGpsLog();
			
			gps.run();
		} 
		catch (SQLException | SocketException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
