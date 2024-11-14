package org.imos.abos;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ocss.nmea.parser.GeoPos;

public class Event
{
	Date time;
	GeoPos pos;
	String txt;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
	DecimalFormat df = new DecimalFormat("#0.000000");

	public Event(Date d, GeoPos p, String t)
	{
		time = d;
		pos = p;
		txt = t;

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public String toFileString()
	{
		String event = sdf.format(getTime()) + ",";
		if (pos != null)
		{
			event += getPos().getLatInDegMinDec() + "," + getPos().getLngInDegMinDec() + "," + df.format(getPos().lat) + "," + df.format(getPos().lng);
		}
		else
		{
			event += ",,,,";
		}

		event += "," + getTxt();

		return event;
	}

	public String toString()
	{
		String event = sdf.format(time) + "," + pos.getLatInDegMinDec() + "," + pos.getLngInDegMinDec() + "," + txt;

		return event;
	}

	public Date getTime()
	{
		return time;
	}

	public GeoPos getPos()
	{
		return pos;
	}

	public String getTxt()
	{
		return txt;
	}

	public void toFile(OutputStream f)
	{
		try
		{
			f.write((toFileString() + "\r\n").getBytes());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
