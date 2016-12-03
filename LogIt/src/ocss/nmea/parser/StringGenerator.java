package ocss.nmea.parser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

public class StringGenerator
{
  private static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HHmmss");
  private static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("ddMMyy");
  private final static NumberFormat LAT_DEG_FMT = new DecimalFormat("00");
  private final static NumberFormat LONG_DEG_FMT = new DecimalFormat("000");
  private final static NumberFormat MIN_FMT = new DecimalFormat("00.000");
  private final static NumberFormat OG_FMT = new DecimalFormat("000.0");
  
  public static String generateRMC(String devicePrefix, Date date, double lat, double lng, double sog, double cog, double d)
  {
    String rmc = devicePrefix + "RMC,";
    rmc += (SDF_TIME.format(date) + ",");
    rmc += "A,";
    int deg = (int)Math.abs(lat);
    double min = 0.6 * ((Math.abs(lat) - deg) * 100d);
    rmc += (LAT_DEG_FMT.format(deg) + MIN_FMT.format(min));
    if (lat < 0) rmc += ",S,";
    else rmc += ",N,";

    deg = (int)Math.abs(lng);
    min = 0.6 * ((Math.abs(lng) - deg) * 100d);
    rmc += (LONG_DEG_FMT.format(deg) + MIN_FMT.format(min));
    if (lng < 0) rmc += ",W,";
    else rmc += ",E,";
    
    rmc += (OG_FMT.format(sog) + ",");
    rmc += (OG_FMT.format(cog) + ",");

    rmc += (SDF_DATE.format(date) + ",");

    rmc += (OG_FMT.format(Math.abs(d)) + ",");
    if (d < 0) rmc += "W";
    else rmc += "E";    
    // Checksum
    int cs = StringParsers.calculateCheckSum(rmc);
    rmc += ("*" + Integer.toString(cs, 16).toUpperCase());
    
    return "$" + rmc;
  }
  
  public static String generateMWV(String devicePrefix, double aws, int awa)
  {
    String mwv = devicePrefix + "MWV,";
    mwv += (OG_FMT.format(awa) + ",R,");
    mwv += (OG_FMT.format(aws) + ",N,A");
    // Checksum
    int cs = StringParsers.calculateCheckSum(mwv);
    mwv += ("*" + Integer.toString(cs, 16).toUpperCase());
    
    return "$" + mwv;
  }
  
  public static String generateVHW(String devicePrefix, double bsp, int cc)
  {
    String vhw = devicePrefix + "VHW,,,";
    vhw += (LONG_DEG_FMT.format(cc) + ",M,");
    vhw += (MIN_FMT.format(bsp) + ",N,,");
    // Checksum
    int cs = StringParsers.calculateCheckSum(vhw);
    vhw += ("*" + Integer.toString(cs, 16).toUpperCase());
    
    return "$" + vhw;
  }
  
  public static String generateHDM(String devicePrefix, int cc)
  {
    String hdm = devicePrefix + "HDM,";
    hdm += (LONG_DEG_FMT.format(cc) + ",M");
    // Checksum
    int cs = StringParsers.calculateCheckSum(hdm);
    hdm += ("*" + Integer.toString(cs, 16).toUpperCase());
    
    return "$" + hdm;
  }
  
  public static void main(String[] args)
  {
    String rmc = generateRMC("II", new Date(), 38.2500, -122.5, 6.7, 210, 3d);
    System.out.println("Generated RMC:" + rmc);
    
    if (StringParsers.validCheckSum(rmc))
      System.out.println("Valid!");
    else
      System.out.println("Invalid...");
    
    String mwv = generateMWV("II", 23.45, 110);
    System.out.println("Generated MWV:" + mwv);
    
    if (StringParsers.validCheckSum(mwv))
      System.out.println("Valid!");
    else
      System.out.println("Invalid...");

    String vhw = generateVHW("II", 8.5, 110);
    System.out.println("Generated VHW:" + vhw);
    
    if (StringParsers.validCheckSum(vhw))
      System.out.println("Valid!");
    else
      System.out.println("Invalid...");
  }
}
