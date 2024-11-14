package astro.runtime;
import astro.data.ephemerides.CelestialComputer;
import astro.data.Data;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DataPublisher 
{
  private final static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
  
  public DataPublisher() throws Exception
  {
    String year  = userInput("Year  > ");
    String month = userInput("Month > ");
    String day   = userInput("Day   > ");
    
    int horizontalAngleOption = Data.HA;
    int displayOption         = Data.DMD;
    
    String hOption = userInput("Hour Angle (1) or Right Ascension (2) ? > [1] ");
    try
    {
      if (hOption.trim().length() != 0)
      {
        int opt = Integer.parseInt(hOption);
        if (opt == 2)
          horizontalAngleOption = Data.RA;
      }
    }
    catch (NumberFormatException nfe)
    { }
    String dOption = userInput("Angles in Degrees Minutes 100th of minutes (1) or Degrees Minutes Seconds (2) ? > [1] ");
    try
    {
      if (dOption.trim().length() != 0)
      {
        int opt = Integer.parseInt(dOption);
        if (opt == 2)
          displayOption = Data.DMS;
      }
    }
    catch (NumberFormatException nfe)
    { }
    
    for (int i=0; i<24; i++)
    {
      String hour = Integer.toString(i);
      if (hour.length() < 2)
        hour = "0" + hour;
      CelestialComputer cc = new CelestialComputer(year,
                                                   month,
                                                   day,
                                                   hour,
                                                   "0",
                                                   "0",
                                                   "");
      Data data = cc.getData();
      data.setMinuteDisplay(displayOption);
      data.setHorizontalDisplay(horizontalAngleOption);
      //                 "| 00 | 012°44.93' | 182°47.42' - S 04°17.32' |"
      if (i == 0)
      {
        System.out.println("\r\nOliv Soft Strikes agains");
        System.out.println("+------" + (horizontalAngleOption==Data.RA?"-":"") + "-" + (horizontalAngleOption==Data.RA?"-":"") + "-" + (horizontalAngleOption==Data.RA?"-":"") + "----------------------------------------------------------------------------------------------+");
        System.out.println("| Ephemerides for " + data.getDoW().toLowerCase() + " " + (day.length()==1?" ":"") + day + "-" + getMonthName(Integer.parseInt(month)) + "-" + year + 
                                                        (horizontalAngleOption==Data.RA?"    ":"") + "                                                                      |");
        System.out.println("+----+---" + (horizontalAngleOption==Data.RA?"-":"") + "---------+----" + (horizontalAngleOption==Data.RA?"-":"") + "-------------------------------------+---" + (horizontalAngleOption==Data.RA?"-":"") + "---------------------------------------+");
        System.out.println("|    |   " + (horizontalAngleOption==Data.RA?" ":"") + " Aries   |    " + (horizontalAngleOption==Data.RA?" ":"") + "               Sun                   |   " + (horizontalAngleOption==Data.RA?" ":"") + "                 Moon                  |");
        System.out.println("| UT |------" + (horizontalAngleOption==Data.RA?"-":"") + "------+----" + (horizontalAngleOption==Data.RA?"-":"") + "-------------------------------------+---" + (horizontalAngleOption==Data.RA?"-":"") + "---------------------------------------+");
        System.out.println("|    |   " + (horizontalAngleOption==Data.RA?" ":"") + "  " + (horizontalAngleOption==Data.RA?"RA ":"GHA") + "    |    " + (horizontalAngleOption==Data.RA?" ":"") + " " + (horizontalAngleOption==Data.RA?"RA ":"GHA") + "    |      Dec    |   sd  |  hp  |   " + (horizontalAngleOption==Data.RA?" ":"") + "  " + (horizontalAngleOption==Data.RA?"RA ":"GHA") + "    |      Dec    |   sd  |  hp   |");
        System.out.println("+----+------" + (horizontalAngleOption==Data.RA?"-":"") + "------+----" + (horizontalAngleOption==Data.RA?"-":"") + "--------+-------------+-------+------+---" + (horizontalAngleOption==Data.RA?"-":"") + "---------+-------------+-------+-------+");
        //                 "| 00 | 012°44.93' | 182°47.42' - S 04°17.32' | 16.3' | 0.1' | 182°47.42' - S 04°17.32' | 15.1' | 55.5' |"
      }
      System.out.println("| " + hour + " | " + 
                                Data.formatHA(data.getAR()) + " | " + 
                                Data.formatHA(data.getGHAsun()) + " - " +  Data.formatDec(data.getDECsun()) + " | " +
                                Data.formatSDHP(data.getSDsun()) + " | " + Data.formatSDHP(data.getHPsun()) + " | " +
                                Data.formatHA(data.getGHAmoon()) + " - " +  Data.formatDec(data.getDECmoon()) + " | " +
                                Data.formatSDHP(data.getSDmoon()) + " | " + Data.formatSDHP(data.getHPmoon()) + " |");
    }
    System.out.println("+----+------" + (horizontalAngleOption==Data.RA?"-":"") + "------+----" + (horizontalAngleOption==Data.RA?"-":"") + "--------+-------------+-------+------+---" + (horizontalAngleOption==Data.RA?"-":"") + "---------+-------------+-------+-------+");
  }

  public static String userInput(String prompt)
  {
    String retString = "";
    System.err.print(prompt);
    try { retString = stdin.readLine(); }
    catch (Exception e)
    {
      System.out.println(e);
      try { String s = userInput("<Oooch/>"); } catch (Exception ex) {}
    }
    return retString;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    DataPublisher dataPublisher = new DataPublisher();
  }
  
  private String getMonthName(int mNum)
  {
    String[] month = {"Jan", "Feb", "Mar",
                      "Apr", "May", "Jun",
                      "Jul", "Aug", "Sep",
                      "Oct", "Nov", "Dec"}; 
    return month[mNum-1];
  }
}