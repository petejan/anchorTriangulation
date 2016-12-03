import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.StringParsers;


public class MulticastLog 
{
	Connection connection = null;
	PreparedStatement preparedStatement = null;
	SimpleDateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd");
 		
	public MulticastLog() throws SQLException
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));	
		
		connection = common.getConnection();		
		
//			Table "public.raw_instrument_data"
//		Column      |           Type           |           Modifiers            
//		-----------------+--------------------------+--------------------------------
//		source_file_id  | integer                  | not null
//		instrument_id   | integer                  | not null
//		mooring_id      | character(20)            | not null
//		data_timestamp  | timestamp with time zone | not null
//		latitude        | numeric(16,4)            | not null
//		longitude       | numeric(16,4)            | not null
//		depth           | numeric(16,4)            | not null default 0
//		parameter_code  | character(20)            | not null
//		parameter_value | numeric(16,4)            | not null
//		quality_code    | character(20)            | not null default 'N/A'::bpchar


		String insertTableSQL = "INSERT INTO raw_instrument_data (source_file, instrument, voyage, data_timestamp, latitude, longitude, depth, parameter_code, parameter_value, quality_code) "+
									" VALUES (?,?,?,?,?,?,?,?,?,?)";
		
		preparedStatement = connection.prepareStatement(insertTableSQL);
	}

	int port = 2000;
	
	public void run() throws UnknownHostException, IOException, SQLException
	{
		File file = new File(sdfFile.format(new Date()) + "-MultiCast.txt");
		FileOutputStream out = new FileOutputStream(file);
		
		// Receiving
		MulticastSocket sock = null;
		InetAddress group = null;
		try 
		{
			group = InetAddress.getByName(common.getProp("multicast.isus.group","224.0.36.0"));
			int port = Integer.parseInt(common.getProp("multicast.isus.port", "51412"));
			
			sock = new MulticastSocket(port);
			sock.joinGroup(group);

			sock.setReuseAddress(true);
		} 
		catch (SocketException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String s;
		String packet;
		byte[] buf = new byte[4096];
		String match = common.getProp("multicast.isus.match", "SATSDF.*");
		String source = common.getProp("source", "TriAXYS");
		String instrument = common.getProp("multicast.isus.instrument", "ISUS");
		String voyage = common.getProp("voyage", "IN-2015-V01");
		while(true)
		{
			DatagramPacket pack = new DatagramPacket(buf, buf.length, group, port);
			
			sock.receive(pack);
			// System.out.println("MulticastLog::receive " + pack);

			s += new String(pack.getData());

			StringTokenizer st = new StringTokenizer(s, "\n\r");
			while (st.hasMoreElements())
			{
				packet = st.nextToken().trim() + "\r\n";
				if (packet.length() > 0)
				{
					System.out.println("FROM SERVER: " + packet);
					out.write((packet + "\r\n").getBytes());
					
					if (packet.matches(match))
					{
						String[] data = packet.split(",");
						
						preparedStatement.setString(1, source);
						preparedStatement.setString(2, instrument);
						preparedStatement.setString(3, voyage);
						preparedStatement.setTimestamp(4, new Timestamp(new Date().getTime()));
						if (common.pos != null)
						{
							preparedStatement.setDouble(5, common.pos.lat);
							preparedStatement.setDouble(6, common.pos.lng);
							preparedStatement.setDouble(7, common.head);
						}
						else
						{
							preparedStatement.setDouble(5, Double.NaN);
							preparedStatement.setDouble(6, Double.NaN);
							preparedStatement.setDouble(7, Double.NaN);
						}
							
						if (data.length > 4)
						{
							preparedStatement.setString(8, "NTRI");
							preparedStatement.setDouble(9, Double.parseDouble(data[4]));
							preparedStatement.setString(10, "RAW");
							
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
					
				}
			}
			s = "";
		}
	}
	
	public static void main(String[] args) 
	{
		try 
		{
			common.setPropFile("ddls.properties");

			MulticastLog tl = new MulticastLog();
			(new Thread(new common.GpsThread())).start();
			
			tl.run();
		} 
		catch (UnknownHostException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
