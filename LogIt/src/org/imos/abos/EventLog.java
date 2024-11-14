package org.imos.abos;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import java.awt.GridLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.awt.Dialog;
import java.awt.Frame;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAReader;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.StringParsers;
import ocss.nmea.parser.UTC;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import org.imos.abos.RequestFocusListener;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class EventLog extends JFrame implements EventWriter
{
	private boolean database = false;
	private JPanel contentPane;
	private JTextField txtLon;
	private JTextField txtLat;
	private JTextField txtTime;
	private JTextField txtNewEvent;
	private JTextField txtFileName;
	private GeoPos pos;

	SimpleDateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
	protected boolean txtEventChanged = false;
	DecimalFormat df = new DecimalFormat("#0.000000");

	static EventLog frame;

	Connection connection = null;
	PreparedStatement preparedStatement = null;
 	
	public class PositionClient extends NMEAClient
	{
		public PositionClient(String s, String[] sa)
		{
			super(s, sa);
			
		}
		
		public void setPosReader(NMEAReader c)
		{
			reader = c;
			System.out.println("PositionClient::setReader " + reader);
		}

		NMEAReader reader = null;
		
		public void dataDetectedEvent(NMEAEvent e)
		{
			String s = e.getContent();

			// System.out.println("PositionClient::Received:" + s);
			String key = s.substring(3, 6);
			if (key.equals("GGA"))
			{
				pos = (GeoPos) StringParsers.parseGGA(s).get(1);
				if (pos != null)
				{
					txtLat.setText(pos.getLatInDegMinDec());
					txtLon.setText(pos.getLngInDegMinDec());
				}
				UTC ut = (UTC) StringParsers.parseGGA(s).get(0);
				if (ut != null)
				{
					// System.out.println("UTC " + ut);
				}
			}
		}

		public void start()
		{
			if (reader == null)
			{
				reader = new CustomUDPReader(getListeners(), 50102);
			}
			
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				public void run()
				{
					System.out.println("EventLog::PositionClient::start() Shutting down nicely.");
					ArrayList<NMEAListener> al = getListeners();

					for (NMEAListener l : al)
					{
						l.stopReading(new NMEAEvent(this));
					}
				}
			});
			initClient();
			System.out.println("PositionClient::start " + reader);
			
			setReader(reader);
			startWorking();
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		boolean serial = false;
		boolean database = false;
		String serialPort = null;
		String nmeaPort = null;
		
		System.setProperty("java.net.preferIPv4Stack" , "true");
		
		try
		{
			Common.setPropFile("ddls.properties");
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int i = 0;
		while (i < args.length)
		{
			//System.out.println("arg " + i + " " + args[i]);
			
			if (args[i].startsWith("-serial"))
			{
				serial = true;
				System.out.println("EventLog::main serial " + serial);
			}
			else if (args[i].startsWith("-database"))
			{
				database = true;
				System.out.println("EventLog::main database " + database);
			}
			else if (serialPort == null)
			{
				serialPort = args[i];
				System.out.println("EventLog::main serialPort " + serialPort);
			}
			else if (nmeaPort == null)
			{
				nmeaPort = args[i];
				System.out.println("EventLog::main nmeaPort " + nmeaPort);
			}
			
			i++;
		}

		frame = new EventLog();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setSerial(serial);
		frame.database = database;
		frame.setSerialPort(serialPort);
		frame.setNMEAport(nmeaPort);
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					frame.run();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	static File file;
	static FileOutputStream out;
	static JTextArea txtEvents;
	static EventDialog ev;
	JScrollPane scroll;

	public void newFile(File f)
	{
		try
		{
			if (f.exists())
			{
				System.out.println("EventLog::newFile File Exists");
				FileInputStream fstream = new FileInputStream(f);
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				// Read File Line By Line
				while ((strLine = br.readLine()) != null)
				{
					txtEvents.append(strLine + "\n");
				}
				br.close();
				in.close();
                SwingUtilities.invokeLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                    	try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	txtEvents.setCaretPosition(txtEvents.getDocument().getLength());
                    	scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
                    }

                });				
				out = new FileOutputStream(f, true);
			}
			else
			{
				out = new FileOutputStream(f);
			}
		}
		catch (FileNotFoundException e)
		{
			txtEvents.append(e.toString() + "\n");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			txtEvents.append(e.toString() + "\n");
			e.printStackTrace();
		}
	}

	public class EventButton implements ActionListener
	{
		public EventButton()
		{
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			boolean oldEvent = false;
			for (String i : events)
			{
				if (i.startsWith(txtNewEvent.getText().trim()))
				{
					oldEvent = true;
				}
			}
			if (!oldEvent)
			{
				System.out.println("btnEvent::newText " + txtNewEvent.getText().trim());
				events.insertElementAt(txtNewEvent.getText(), 0);
			}

			ev = new EventDialog(null, events);
			ev.setTime(txtTime.getText());
			ev.setPos(pos);

			ev.addWindowListener(new WindowListener()
			{
				@Override
				public void windowClosed(WindowEvent arg0)
				{
					// System.out.println("EventDialog window Closed" + arg0);
					// System.out.println("Event " + ev.getAction());
					if (ev.getAction().getActionCommand().startsWith("OK"))
					{
						txtNewEvent.setText(ev.getEvent().getTxt());
						write(ev.getEvent());
					}
				}

				@Override
				public void windowActivated(WindowEvent arg0)
				{
				}

				@Override
				public void windowClosing(WindowEvent arg0)
				{
				}

				@Override
				public void windowDeactivated(WindowEvent arg0)
				{
				}

				@Override
				public void windowDeiconified(WindowEvent arg0)
				{
				}

				@Override
				public void windowIconified(WindowEvent arg0)
				{
				}

				@Override
				public void windowOpened(WindowEvent arg0)
				{
				}
			});
			ev.setVisible(true);
		}
	}

	final Vector<String> events = new Vector<String>();
	String voyage = "IN-2015-V01";

	public EventLog()
	{
		setTitle("Event Log");
		voyage = Common.getProp("voyage", "IN-2015-V01");

		sdfFile.setTimeZone(TimeZone.getTimeZone("UTC"));
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		file = new File(sdfFile.format(new Date()) + "-EventLog.txt");

		// Create a file chooser
		final JFileChooser fc = new JFileChooser();

		events.add(new String("New Event"));
		events.add(new String("Start"));
		events.add(new String("End"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel buttonPanel = new JPanel();
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton btnEvent = new JButton("Event");
		btnEvent.setDefaultCapable(true);
		EventButton btnEventListner = new EventButton();
		btnEvent.addActionListener(btnEventListner);

		buttonPanel.add(btnEvent);

		JPanel infoPanel = new JPanel();
		contentPane.add(infoPanel, BorderLayout.NORTH);
		infoPanel.setLayout(new GridLayout(0, 2, 0, 0));

		JLabel lblTime = new JLabel("Time:");
		infoPanel.add(lblTime);

		txtTime = new JTextField();
		infoPanel.add(txtTime);
		txtTime.setColumns(10);

		JLabel ldlLat = new JLabel("Lat:");
		infoPanel.add(ldlLat);

		txtLat = new JTextField();
		txtLat.setColumns(10);
		infoPanel.add(txtLat);

		JLabel lblLong = new JLabel("Long:");
		infoPanel.add(lblLong);

		txtLon = new JTextField();
		infoPanel.add(txtLon);
		txtLon.setColumns(10);

		JPanel centrePanel = new JPanel();
		contentPane.add(centrePanel, BorderLayout.CENTER);
		centrePanel.setLayout(new BorderLayout(0, 0));

		txtEvents = new JTextArea();
		txtEvents.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		txtEvents.setText("Previous Events:\n");
		txtEvents.setEditable(false);

		scroll = new JScrollPane(txtEvents);
		centrePanel.add(scroll);
		//scroll.setAutoscrolls(true);
		scroll.createVerticalScrollBar();
		scroll.setWheelScrollingEnabled(true);
		//scroll.getVerticalScrollBar().setAutoscrolls(true);

		txtNewEvent = new JTextField();
		txtNewEvent.setText("New Event");
		txtNewEvent.addActionListener(btnEventListner);
		txtNewEvent.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent arg0)
			{
				System.out.println("txtNewEvent::changed " + arg0);
				txtEventChanged = true;
			}

			@Override
			public void insertUpdate(DocumentEvent arg0)
			{
			}

			@Override
			public void removeUpdate(DocumentEvent arg0)
			{
			}
		});

		centrePanel.add(txtNewEvent, BorderLayout.SOUTH);
		txtNewEvent.setColumns(10);
		txtNewEvent.requestFocusInWindow();
		txtNewEvent.addAncestorListener(new RequestFocusListener());

		JPanel filePanel = new JPanel();
		centrePanel.add(filePanel, BorderLayout.NORTH);
		filePanel.setLayout(new BorderLayout(0, 0));

		txtFileName = new JTextField();
		filePanel.add(txtFileName);
		txtFileName.setColumns(10);
		txtFileName.setText(file.getAbsolutePath());
		newFile(file);

		JButton btnFile = new JButton("File");
		btnFile.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				int returnVal = fc.showOpenDialog(EventLog.this);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					file = fc.getSelectedFile();

					System.out.println("Opening: " + file.getName());
					txtFileName.setText(file.getAbsolutePath());
					newFile(file);
				}
				else
				{
					System.out.println("Open command cancelled by user.");
				}
			}
		});
		filePanel.add(btnFile, BorderLayout.WEST);
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { btnEvent, buttonPanel, infoPanel, ldlLat, txtLat, lblLong, txtLon, lblTime, txtTime, centrePanel, txtEvents, contentPane }));
		
		if (database)
		{
			try 
			{			
				Class.forName("org.postgresql.Driver"); 
			} 
			catch (ClassNotFoundException e) 
			{
				System.out.println("Where is your PostgreSQL JDBC Driver? Include in your library path!");
				e.printStackTrace();
				return;
			}
	 
			System.out.println("PostgreSQL JDBC Driver Registered!");
	 
	 
			try 
			{
				connection = DriverManager.getConnection("jdbc:postgresql://10.1.0.5:5432/voyage", "pete", "password");
	 
			} 
			catch (SQLException e) 
			{
				System.out.println("Connection Failed! Check output console");
				e.printStackTrace();
			
				return;
			}
	 
			if (connection != null) 
			{
				System.out.println("You made it, take control your database now!");
			} 
			else 
			{
				System.out.println("Failed to make connection!");
			}	
			
//			            Table "public.event_log"
//			Column    |            Type             |                      Modifiers                       
//			-------------+-----------------------------+------------------------------------------------------
//			pk          | integer                     | not null default nextval('event_sequence'::regclass)
//			timestamp   | timestamp without time zone | 
//			voyage      | character varying           | 
//			group       | character varying           | 
//			name        | character varying           | 
//			description | character varying           | 
//			user        | character varying           | 
//			latitude    | double precision            | 
//			longitude   | double precision            | 
//			altitude    | double precision            | 
//			heading     | double precision            | 
//			the_geom    | geometry                    | 
	
	
			String insertTableSQL = "INSERT INTO event (timestamp, voyage, group, name, description, user, latitude, longitude, altitude, heading, the_geom) "+
										" VALUES (?,?,?,?,?,?,?,?,?,?,ST_GeomFromText(?, 4326))";
			
			try
			{
				preparedStatement = connection.prepareStatement(insertTableSQL);
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	boolean serialMode = false;

	public void setSerial(boolean serial)
	{
		serialMode = serial;
	}
	
	String serialPort = null;
	public void setSerialPort(String s)
	{
		serialPort = s;
	}
	
	String nmeaPort = null;
	public void setNMEAport(String s)
	{
		nmeaPort = s;
	}

	public void run()
	{
		Action updateCursorAction = new AbstractAction()
		{
			boolean shouldDraw = false;

			public void actionPerformed(ActionEvent e)
			{
				txtTime.setText(sdf.format(new Date()));
			}
		};

		new Timer(1000, updateCursorAction).start();

		String[] array = { "GGA", "RMC" };

		PositionClient pc = new PositionClient("GP", array);
		if (nmeaPort != null)
		{
			pc.setPosReader(new CustomJSSCSerialReader(pc.getListeners(), nmeaPort));
		}
		pc.start();

		if (serialMode)
		{
			SerialJSSCEventGenerator se = new SerialJSSCEventGenerator(this.out, this);
			if (serialPort != null)
			{
				se.setComPort(serialPort);
			}
			se.read();
			pc.addNMEAListener(se); // send the position updated to update the serial event generator
		}
	}

	@Override
	public void write(Event ev)
	{
		String event = sdf.format(ev.getTime()) + ",";
		if (ev.pos != null)
		{
			event += ev.getPos().getLatInDegMinDec() + "," + ev.getPos().getLngInDegMinDec() + "," + df.format(ev.getPos().lat) + "," + df.format(ev.getPos().lng);
		}
		else
		{
			event += ",,,,";
		}

		event += "," + ev.getTxt();
		
		txtEvents.append(event + "\n");
		System.out.println("EventLog::write " + event);
		txtEventChanged = false;
		scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
		
		ev.toFile(out);
		
		if (database)
		{
			try
			{
				preparedStatement.setTimestamp(1, new Timestamp(ev.getTime().getTime()));
				preparedStatement.setString(2, "IN-2015-V01"); // voyage
				preparedStatement.setString(3, "EventLog"); // group
				preparedStatement.setString(4, "Event"); // name
				preparedStatement.setString(5, ev.getTxt()); // description
				preparedStatement.setString(6, System.getProperty("user.name")); // user
				if (ev.getPos() != null)
				{
					preparedStatement.setDouble(7, ev.getPos().lat);
					preparedStatement.setDouble(8, ev.getPos().lng);
					preparedStatement.setDouble(9, Double.NaN);
					preparedStatement.setDouble(10, Double.NaN);
				}
				else
				{
					preparedStatement.setDouble(7, Double.NaN);
					preparedStatement.setDouble(8, Double.NaN);
					preparedStatement.setDouble(9, Double.NaN);
					preparedStatement.setDouble(10, Double.NaN);
				}
					
				preparedStatement.setObject(11, "POINT("+pos.lng+" "+pos.lat+")");
				
				System.out.println("Insert :" + preparedStatement);

				preparedStatement.executeUpdate();
			}
			catch (SQLException ex)
			{
				System.out.println("SQL EXCEPTION " + ex.getMessage());
			}			
		}
	}

}
