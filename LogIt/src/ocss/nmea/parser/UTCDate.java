package ocss.nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.Date;

public class UTCDate implements Serializable
{
  private Date date = null;
  private static SimpleDateFormat FMT = new SimpleDateFormat("EEE, yyyy MMM dd HH:mm:ss 'UTC'");

  public UTCDate()
  {
  }

  public UTCDate(Date date)
  {
    this.date = date;
  }

  public Date getValue()
  {
    return this.date;
  }

  public String toString()
  {
    return FMT.format(this.date);
  }
}
