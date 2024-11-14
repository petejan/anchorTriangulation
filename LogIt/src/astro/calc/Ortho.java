package astro.calc;

import java.util.Vector;
import astro.calc.OrthoWayPoint;

/**
 * A Bean to calculate an orthodromic route
 *
 * @author Olivier LE DIOURIS
 * @see Point
 * @see OrthoWayPoint
 */
public class Ortho
{
  public final static int TO_NORTH = 0;
  public final static int TO_SOUTH = 1;
  public final static int TO_EAST  = 2;
  public final static int TO_WEST  = 3;

  private static int ewDir;
  private static int nsDir;

  private static Point start   = null;
  private static Point arrival = null;

  private static Vector route;

  private double rv = 0.0;
  private double dLoxo = 0.0;

  /**
   * Constructor
   */
  public Ortho()
  {
  }

  public void setStart(Point p)
  {
    start = p;
  }

  public void setArrival(Point p)
  {
    arrival = p;
  }

  public Point getStart()
  {
    return start;
  }

  public Point getArrival()
  {
    return arrival;
  }

  public int getNS()
  {
    return nsDir;
  }

  public int getEW()
  {
    return ewDir;
  }

  public static void calculateRoute(int nbPoints)
  {
    if (arrival.getL() > start.getL())
      nsDir = TO_NORTH;
    else
      nsDir = TO_SOUTH;

    if (arrival.getG() > start.getG())
      ewDir = TO_EAST;
    else
      ewDir = TO_WEST;

    if  (Math.abs(arrival.getG() - start.getG()) > Math.PI) // Swap
    {
      if (ewDir == TO_EAST)
      {
        ewDir = TO_WEST;
        arrival.setG(arrival.getG() - (Math.PI * 2.0));
      }
      else
      {
        ewDir = TO_EAST;
        arrival.setG((Math.PI * 2.0) + arrival.getG());
      }
    }

//  System.out.println("From " + (start.getG() * (180.0/Math.PI)) + " to " + (arrival.getG() * (180.0/Math.PI)) + " -> " +
//                     (ewDir==TO_EAST?"East":"West"));
                       
    double deltaG = arrival.getG() - start.getG();
//  System.out.println("Delta G:" + deltaG);
    
    route = new Vector();
    route.addElement(new OrthoWayPoint(start, null)); // Origin.
    double interval = deltaG / nbPoints;
    // Attention au sens...
    Point smallStart = start;
    boolean go = true;
//  for (double g=start.getG(); g<arrival.getG(); g+=interval)
    double g = start.getG();
    while (go)
    {
      if (interval > 0.0)
      {
        if (g > (arrival.getG() * 1.01))
          go = false;
      }
      else
      {
        if (g < (arrival.getG() * 1.01))
          go = false;
      }
      if (go)
      {
        double deltag = arrival.getG() - g; // for each point
        double tanStartAngle = Math.sin(deltag) /
                               ((Math.cos(smallStart.getL()) * Math.tan(arrival.getL())) -
                                (Math.sin(smallStart.getL()) * Math.cos(deltag))
                               );
        double smallL = Math.atan((Math.tan(smallStart.getL()) * Math.cos(interval)) + ( Math.sin(interval) / (tanStartAngle * Math.cos(smallStart.getL())) ));

        double rpG = g + interval;
        if (rpG > Math.PI)
          rpG = rpG - (Math.PI * 2.0);
        Point routePoint = new Point(smallL, rpG);
        double ari = Math.atan(tanStartAngle) * 180.0 / Math.PI; // in degrees
        // going from smallStart to routePoint
        // Calculating on 360.
        int _nsDir, _ewDir;
        if (routePoint.getL() > smallStart.getL())
          _nsDir = TO_NORTH;
        else
          _nsDir = TO_SOUTH;

        if (routePoint.getG() > smallStart.getG())
          _ewDir = TO_EAST;
        else
          _ewDir = TO_WEST;
          
        double _start = 0.0;
        if (_nsDir == astro.calc.Ortho.TO_SOUTH)
        {
          _start = 180.0;
          if (_ewDir == astro.calc.Ortho.TO_EAST)
            ari = _start - ari;
          else
            ari = _start + ari;
        }
        else // To North
        {
          if (_ewDir == astro.calc.Ortho.TO_EAST)
            ari = _start + ari;
          else
            ari = _start - ari;
        }
        while (ari < 0.0)
          ari += 360;
//      System.out.println("ari->" + ari);
        
        route.addElement(new OrthoWayPoint(routePoint, new Double(ari)));
//      System.out.println("L=" + (smallL * 180.0 / Math.PI) + ", G=" + ((g + interval) * 180.0 / Math.PI) + " Z=" + (Math.atan(tanStartAngle) * 180.0 / Math.PI));
        smallStart = routePoint;

        g += interval;
      }
    }
//  route.addElement(arrival); // Destination
  }

  public double getDistance()
  {
    double dist = Math.acos((Math.sin(start.getL()) * Math.sin(arrival.getL())) + (Math.cos(start.getL()) * Math.cos(arrival.getL()) * Math.cos(arrival.getG() - start.getG())));
    return dist;
  }

/*private double log10(double a)
  {
    return Math.log(a) / Math.log(10);
  }*/

  public void calcLoxo()
  {
//  double lc45 = Math.log(Math.tan((Math.PI/4.0) + (Math.PI/8.0)));
//  System.out.println("LC 45 : " + (lc45 * (180.0 / Math.PI) ));
    // Calculating Loxo
    double deltaL = (arrival.getL() - start.getL()) * 60.0 * (180.0 / Math.PI);
    double deltaG = (arrival.getG() - start.getG()) * 60.0 * (180.0 / Math.PI);
    if (deltaG < 0.0)
      deltaG = -deltaG;
    // Latitudes croissantes
    double startLC = Math.log(Math.tan((Math.PI/4.0) + (start.getL()/2.0)));
    double arrLC   = Math.log(Math.tan((Math.PI/4.0) + (arrival.getL()/2.0)));
    double deltaLC = (21600.0/(2 * Math.PI)) * (arrLC - startLC);
    //               ^
    //               Earth Radius

    rv    = Math.atan(deltaG/deltaLC);
    dLoxo = (deltaL / Math.cos(rv));
    if (dLoxo < 0.0)
      dLoxo = -dLoxo;
    if (rv < 0.0)
      rv = -rv;
  }

  public double getLoxoDistance()
  {
    return dLoxo;
  }

  public double getLoxoRoute()
  {
    return rv;
  }

  public Vector getRoute()
  {
    return route;
  }
}

