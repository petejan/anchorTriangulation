import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.imos.abos.Common;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.StringParsers;

public class GpsLog 
{
	Connection connection = null;
	PreparedStatement preparedStatement = null;
 	
	public GpsLog() throws SQLException
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		connection = Common.getConnection();
		
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

	int port = 3002;
	
	public void run() throws SocketException, SQLException
	{
		// Receiving
		final DatagramSocket sock = new DatagramSocket(port);
		sock.setReuseAddress(true);

		String s;
		String packet;
		byte[] buf = new byte[4096];
		DatagramPacket pack = new DatagramPacket(buf, buf.length);
		pack.setPort(port);
		
		System.out.println("Listening on port " + pack.getPort());

		GeoPos pos = null;
		int head = -1000;
		Date ut = null;
		String voyage = Common.getProp("voyage", "IN-2015-V01");
		
		while (true)
		{
			try
			{
				sock.receive(pack);
				// System.out.println("GpsLog::receive " + pack);

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
								
								long t = ut.getTime();
								t = (t / 1000) * 1000; // round to nearest second
								//System.out.println("time " + t + " mod 5 " + (t % 5000));								
								
								if ((t % 5000) == 0) // only insert every 5 seconds
								{
									
									Timestamp ts = new Timestamp(t);
									
									preparedStatement.setTimestamp(1, ts);
									preparedStatement.setString(2, voyage);
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
			Common.setPropFile("ddls.properties");
			
			GpsLog gps = new GpsLog();
			
			gps.run();
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SocketException e) 
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
