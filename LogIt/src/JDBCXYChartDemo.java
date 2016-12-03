
import java.awt.Color;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.jdbc.JDBCXYDataset;
import org.jfree.data.jdbc.JDBCXYDatasetAdd;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class JDBCXYChartDemo extends ApplicationFrame
{
	public JDBCXYChartDemo(String s, String args[])
	{
		super(s);
		
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		XYDataset xydataset = createDataset();
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(null, "Date", "Value", xydataset, false, true, false);
		ChartPanel chartpanel = new ChartPanel(jfreechart);
		XYPlot plot = (XYPlot) jfreechart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setRangeGridlinePaint(Color.black);
		
		setContentPane(chartpanel);
		
		startTime = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(startTime);
		cal.add(Calendar.HOUR, -1);
		startTime = cal.getTime();		
		System.out.println("Start " + startTime);

		String s1 = "SELECT data_timestamp, parameter_value AS ntri FROM raw_instrument_data WHERE parameter_code = 'NTRI' AND data_timestamp > \'" + startTime + "\' ORDER BY data_timestamp";
		try 
		{
			jdbcxydataset.executeQuery(s1);
			System.out.println("Item Count : " + jdbcxydataset.getItemCount());			
		} 
		catch (SQLException e) 
		{
			System.err.println("SQLException: " + e.getMessage());			
		}		

		startTime = new Date();

		TimerTask readDataTask = new ReadDataThread();

		Timer uploadCheckerTimer = new Timer(true);
		uploadCheckerTimer.scheduleAtFixedRate(readDataTask, 5000, 2 * 1000);		
	}

	Date startTime = null;
	JDBCXYDatasetAdd jdbcxydataset = null;
	Connection connection = null;

	private XYDataset createDataset()
	{
		String s = "jdbc:postgresql://10.1.0.5/voyage";
		try
		{
			Class.forName("org.postgresql.Driver");
		}
		catch (ClassNotFoundException classnotfoundexception)
		{
			System.err.print("ClassNotFoundException: ");
			System.err.println(classnotfoundexception.getMessage());
		}
		try
		{
			connection = DriverManager.getConnection(s, "pete", "password");
			jdbcxydataset = new JDBCXYDatasetAdd(connection);
		}
		catch (SQLException sqlexception)
		{
			System.err.println("SQLException: " + sqlexception.getMessage());
		}
		catch (Exception exception)
		{
			System.err.println("Exception: " + exception.getMessage());
		}
		return jdbcxydataset;
	}
	
	public class ReadDataThread extends TimerTask
	{
		private void readData()
		{
			System.out.println("reading database data ");

			String s1 = "SELECT data_timestamp, parameter_value AS ntri FROM raw_instrument_data WHERE parameter_code = 'NTRI' AND data_timestamp > \'" + startTime + "\' ORDER BY data_timestamp";
			try 
			{
				startTime = new Date();
				jdbcxydataset.executeQuery(s1);
				Date endTime = new Date();
				System.out.println("Item Count : " + jdbcxydataset.getItemCount() + " took " + (endTime.getTime() - startTime.getTime()) + " ms");
			} 
			catch (SQLException e) 
			{
				System.err.println("SQLException: " + e.getMessage());			
			}		
		}

		@Override
		public void run()
		{
			readData();

		}
	}
	
	private void close()
	{
		try 
		{
			connection.close();
		} 
		catch (SQLException e) 
		{
			System.err.println("SQLException: " + e.getMessage());
		}		
	}

	public static void main(String args[])
	{
		JDBCXYChartDemo jdbcxychartdemo = new JDBCXYChartDemo("data plot", args);
		jdbcxychartdemo.pack();
		RefineryUtilities.centerFrameOnScreen(jdbcxychartdemo);
		
		jdbcxychartdemo.setVisible(true);
	}
}
