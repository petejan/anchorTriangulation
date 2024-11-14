import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

import org.imos.abos.Common;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.StringParsers;


public class TcpLog 
{
	Connection connection = null;
	PreparedStatement preparedStatement = null;
	SimpleDateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd");
 		
	public TcpLog() throws SQLException
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));		

		//		Table "public.raw_instrument_data"
		//	Column      |           Type           |           Modifiers            
		//	-----------------+--------------------------+--------------------------------
		//	source_file_id  | integer                  | not null
		//	instrument_id   | integer                  | not null
		//	mooring_id      | character(20)            | not null
		//	data_timestamp  | timestamp with time zone | not null
		//	latitude        | numeric(16,4)            | not null
		//	longitude       | numeric(16,4)            | not null
		//	depth           | numeric(16,4)            | not null default 0
		//	parameter_code  | character(20)            | not null
		//	parameter_value | numeric(16,4)            | not null
		//	quality_code    | character(20)            | not null default 'N/A'::bpchar

		connection = Common.getConnection();

		String insertTableSQL = "INSERT INTO raw_instrument_data (source_file, instrument, voyage, data_timestamp, latitude, longitude, depth, parameter_code, parameter_value, quality_code) "+
									" VALUES (?,?,?,?,?,?,?,?,?,?)";
		
		preparedStatement = connection.prepareStatement(insertTableSQL);
		
	}

	
	public void run() throws UnknownHostException, IOException, SQLException
	{
		String sentence;
		File file = new File(sdfFile.format(new Date()) + "-TcpLog.txt");
		FileOutputStream out = new FileOutputStream(file);
		
		while(true)
		{
			try
			{
				Socket clientSocket = new Socket("localhost", 6789);
				
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				boolean run = true;
				while(run)
				{
					sentence = inFromServer.readLine();
					if (sentence != null)
					{			
						System.out.println("FROM SERVER: " + sentence);
						out.write((sentence + "\r\n").getBytes());
						
						if (sentence.matches("SATNLF.*"))
						{
							String[] data = sentence.split(",");
							
							preparedStatement.setString(1, "TriAXYS");
							preparedStatement.setString(2, "ISUS");
							preparedStatement.setString(3, "IN-2015-V01");
							preparedStatement.setTimestamp(4, new Timestamp(new Date().getTime()));
							if (Common.pos != null)
							{
								preparedStatement.setDouble(5, Common.pos.lat);
								preparedStatement.setDouble(6, Common.pos.lng);
								preparedStatement.setDouble(7, Common.head);
							}
							else
							{
								preparedStatement.setDouble(5, Double.NaN);
								preparedStatement.setDouble(6, Double.NaN);
								preparedStatement.setDouble(7, Double.NaN);
							}
								
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
					else
					{
						run = false;
					}
				}
				clientSocket.close();
			}
			catch (ConnectException cex)
			{
				System.out.println("Re-connecting " + cex);				
			}
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) 
	{
		try 
		{
			TcpLog tl = new TcpLog();
			(new Thread(new Common.GpsThread())).start();
			
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
