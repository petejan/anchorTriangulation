package user.util;

/**
 * Static Utilities
 */
public class StaticUtil
{
  /**
   * Utility. Replaces a String within a String.
   */
  public static String replaceString(String orig,
                                     String oldStr,
                                     String newStr)
  {
    String ret = orig;
    int indx = 0;
    boolean go = true;
    while (go)
    {
      indx = ret.indexOf(oldStr, indx);
      if (indx < 0)
        go = false;
      else
      {
        ret = ret.substring(0, indx) + newStr + ret.substring(indx + oldStr.length());
        indx += (1 + oldStr.length());
      }
    }
    return ret;
  }
}

 