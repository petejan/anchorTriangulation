package astro.data.ephemerides;
import astro.data.Data;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class has been written after the work of:
 * <ul>
 *   <li type="disc">Jean Meuus - Astronomic Algorithms</li>
 *   <li type="disc">Henning Umland - http://www.celnav.de/index.htm</li>
 * </ul>
 * This class actually doesn't do anything else than
 * what Henning Umland very appropriatly provided in JavaScript.
 * 
 * The constructor is actually triggering the calculation process,
 * finishing with the setData() method, that populates a JavaBean
 * containing the result of the calculation, ready to be displayed 
 * or reused.
 * 
 * We provide as examples two ways to call this class.
 * @see main.Main
 * @see astro.runtime.DataPublisher
 * @see astro.data.Data
 */
public class CelestialComputer 
{
  Data data = new Data();
  
  final static double dtr = Math.PI/180.0;

  double JD0h = 0.0, JD = 0.0, JDE = 0.0;
  double T = 0.0, T2 = 0.0, T3 = 0.0, T4 = 0.0, T5 = 0.0;
  double TE = 0.0, TE2 = 0.0, TE3 = 0.0, TE4 = 0.0, TE5 = 0.0;
  double Tau = 0.0, Tau2 = 0.0, Tau3 = 0.0, Tau4 = 0.0, Tau5 = 0.0;
  
  int year = 0, month = 0, day = 0, 
      hour = 0, minute = 0, second = 0,
      deltaT = 0;

  double dayfraction = 0.0f;

  double D = 0.0, M = 0.0, Mm = 0.0, F = 0.0, omega = 0.0,
         dp = 0.0, de = 0.0, delta_psi = 0.0, delta_eps = 0.0,
         eps0 = 0.0, eps = 0.0;
  
  double GHAAmean = 0.0, GHAAtrue =0.0;
  String SidTm = "", SidTa = "";
  double EoE = 0.0;
  String EoEout = "";
  String DoW = "";
 
  // Sun
  double GHAsun = 0.0;
  double SHAsun = 0.0;
  double DECsun = 0.0;
  double SDsun = 0.0;
  double HPsun = 0.0;
  
  double Dsun = 0.0;
  double RAsun = 0.0;
  double Lsun_true = 0.0;
  double lambda = 0.0;

  // Moon
  double GHAmoon = 0.0;
  double SHAmoon = 0.0;
  double DECmoon = 0.0;
  double SDmoon = 0.0;
  double HPmoon = 0.0;
  
  double lambdaMapp = 0.0; 

  // aries
  double AR = 0.0;
  
  // Polaris
  double GHApol = 0.0, 
         SHApol = 0.0, 
         DECpol = 0.0;

  // Obliquity of Ecliptic
  double OoE = 0.0;
  double tOoE = 0.0;

  // Equation of time
  double EOT = 0.0;
  
  // Illumination
  double k = 0.0;
  
  // Moon-Sun distance
  double LDist = 0.0;
  
  /**
   * Takes a Date as a parameter. Will compute after this.
   * Delta will be considered as null (0).
   * @param date The date you want the data for.
   */
  public CelestialComputer(Date date)
  {
   this((new SimpleDateFormat("yyyy")).format(date), 
        (new SimpleDateFormat("MM")).format(date), 
        (new SimpleDateFormat("d")).format(date), 
        (new SimpleDateFormat("HH")).format(date), 
        (new SimpleDateFormat("mm")).format(date), 
        (new SimpleDateFormat("ss")).format(date), 
        "");
  }
  /**
   * Class constructor
   * 
   * Processes all the position for the given time.
   * All data will be stored in a astro.data.Data,
   * returned by the getData() method.
   * 
   * @param y year
   * @param m month
   * @param d day
   * @param h hour
   * @param mn minute
   * @param s second
   * @param deltaT delta
   * 
   * @author olivier@lediouris.net
   * @version 0.9
   * @see astro.data.Data
   */
  public CelestialComputer(String y, String m, String d, String h, String mn, String s, String deltaT)
  {
    readData(y, m, d, h, mn, s, "");
    processTimeMeasures();
    processNutation();
    processAries();
    processSun();
    processMoon();
    processPolaris();
    processMoonPhase();
    processWeekDay();
    setData();
  }

  //Sine of angles in degrees
  private double sind(double x)
  {
    return Math.sin(dtr*x);
  }

  //Cosine of angles in degrees
  private double cosd(double x)
  {
    return Math.cos(dtr*x);
  }

  //Tangent of angles in degrees
  private double tand(double x)
  {
    return Math.tan(dtr*x);
  }

  // Truncate large angles
  private double trunc(double x)
  {
    return 360*(x/360-Math.floor(x/360));
  }

  // Input data conversion
  private void readData(String _year,
                        String _month,
                        String _day,
                        String _hour,
                        String _minute,
                        String _second,
                        String _delta)
    throws RuntimeException,
           NumberFormatException
  {
    if(_year.trim().equals("")) 
      throw new RuntimeException("Missing year! Restart calculation.");
    else 
      year = Integer.parseInt(_year);

    if(_month.trim().equals(""))  
      throw new RuntimeException("Missing month! Restart calculation.");
    else 
      month = Integer.parseInt(_month);
    if(month < 1 || month > 12) 
      throw new RuntimeException("Month out of range! Restart calculation.");
    if(_day.trim().equals("")) 
      throw new RuntimeException("Missing day! Restart calculation.");
    else 
      day = Integer.parseInt(_day);
    if(day < 1 || day > 31) 
      throw new RuntimeException("Day out of range! Restart calculation.");	
    int schj=0;
    if(year/4-Math.floor(year/4) == 0) 
      schj=1;
    if(year/100-Math.floor(year/100) == 0) 
      schj=0;
    if(year/400-Math.floor(year/400) == 0) 
      schj=1;
    if(month == 2 && day > 28 && schj == 0) 
      throw new RuntimeException("February has only 28 days! Restart calculation.");
    if(month == 2 && day > 29 && schj == 1) 
      throw new RuntimeException("February has only 29 days in a leap year! Restart calculation."); 
    if(month == 4 && day > 30) 
      throw new RuntimeException("April has only 30 days! Restart calculation.");
    if(month == 6 && day > 30) 
      throw new RuntimeException("June has only 30 days! Restart calculation.");
    if(month == 9 && day > 30) 
      throw new RuntimeException("September has only 30 days! Restart calculation.");
    if(month == 11 && day > 30) 
      throw new RuntimeException("November has only 30 days! Restart calculation.");
    if(_hour.trim().equals("")) 
      hour = 0;
    else 
      hour = Integer.parseInt(_hour);
    if(_minute.trim().equals("")) 
      minute = 0;
    else 
      minute = Integer.parseInt(_minute);
    if(_second.trim().equals("")) 
      second = 0;
    else 
      second = Integer.parseInt(_second);
    dayfraction = (hour + minute/60.0 + second/3600.0)/24.0;
    if(dayfraction < 0 || dayfraction > 1) 
      throw new RuntimeException("Time out of range! Restart calculation.");
    if(_delta.trim().equals("")) 
      deltaT = 0;
    else 
      deltaT = Integer.parseInt(_delta);
  }
  
  // Calculating Julian date, century, and millennium
  private void processTimeMeasures()
  {
    //Julian day (UT1)
    if(month <= 2) 
      {year -=1; month += 12;}
    double A = Math.floor(year/100);
    double B = 2-A+Math.floor(A/4);
    JD0h = Math.floor(365.25*(year+4716))+Math.floor(30.6001*(month+1))+day+B-1524.5;
    JD = JD0h+dayfraction;
    
    //Julian centuries (GMT) since 2000 January 0.5
    T = (JD-2451545)/36525.0;
    T2 = T*T;
    T3 = T*T2;
    T4 = T*T3;
    T5 = T*T4;
  
    //Julian ephemeris day (TDT)
    JDE = JD+deltaT/86400.0;
  
    //Julian centuries (TDT) from 2000 January 0.5
    TE = (JDE-2451545)/36525.0;
    TE2 = TE*TE;
    TE3 = TE*TE2;
    TE4 = TE*TE3;
    TE5 = TE*TE4;
  
    //Julian millenniums (TDT) from 2000 January 0.5
    Tau = 0.1*TE;
    Tau2 = Tau*Tau;
    Tau3 = Tau*Tau2;
    Tau4 = Tau*Tau3;
    Tau5 = Tau*Tau4;
  }

  // nutation, obliquity of the ecliptic
  private void processNutation()
  {
    //Mean elongation of the moon 
    D = 297.85036+445267.111480*TE-0.0019142*TE2+TE3/189474;
    D = trunc(D);
    
    //Mean anomaly of the sun
    M = 357.52772+35999.050340*TE-0.0001603*TE2-TE3/300000;
    M = trunc(M);
    
    //Mean anomaly of the moon
    Mm = 134.96298+477198.867398*TE+0.0086972*TE2+TE3/56250;
    Mm = trunc(Mm);
    
    //Mean distance of the moon from ascending node
    F = 93.27191+483202.017538*TE-0.0036825*TE2+TE3/327270;
    F = trunc(F);
    
    //Longitude of the ascending node of the moon
    omega = 125.04452-1934.136261*TE+0.0020708*TE2+TE3/450000;
    omega = trunc(omega);
    
    //Periodic terms for nutation
    dp = (-171996-174.2*TE)*sind(omega);
    de = (92025+8.9*TE)*cosd(omega);
    
    dp+=(-13187-1.6*TE)*sind(-2*D+2*F+2*omega);
    de+=(5736-3.1*TE)*cosd(-2*D+2*F+2*omega);
    
    dp+=(-2274-0.2*TE)*sind(2*F+2*omega);
    de+=(977-0.5*TE)*cosd(2*F+2*omega);
    
    dp+=(2062+0.2*TE)*sind(2*omega);
    de+=(-895+0.5*TE)*cosd(2*omega);
    
    dp+=(1426-3.4*TE)*sind(M);
    de+=(54-0.1*TE)*cosd(M);
    
    dp+=(712+0.1*TE)*sind(Mm);
    de+=-7*cosd(Mm);	
    
    dp+=(-517+1.2*TE)*sind(-2*D+M+2*F+2*omega);
    de+=(224-0.6*TE)*cosd(-2*D+M+2*F+2*omega);
    
    dp+=(-386-0.4*TE)*sind(2*F+omega);
    de+=200*cosd(2*F+omega);	
    
    dp+=-301*sind(Mm+2*F+2*omega);
    de+=(129-0.1*TE)*cosd(Mm+2*F+2*omega);
    
    dp+=(217-0.5*TE)*sind(-2*D-M+2*F+2*omega);
    de+=(-95+0.3*TE)*cosd(-2*D-M+2*F+2*omega);	
    
    dp+=-158*sind(-2*D+Mm);
      
    dp+=(129+0.1*TE)*sind(-2*D+2*F+omega);
    de+=-70*cosd(-2*D+2*F+omega);	
    
    dp+=123*sind(-Mm+2*F+2*omega);
    de+=-53*cosd(-Mm+2*F+2*omega);
    
    dp+=63*sind(2*D);
      
    dp+=(63+0.1*TE)*sind(Mm+omega);
    de+=-33*cosd(Mm+omega);
    
    dp+=-59*sind(2*D-Mm+2*F+2*omega);
    de+=26*cosd(2*D-Mm+2*F+2*omega);	
    
    dp+=(-58-0.1*TE)*sind(-Mm+omega);
    de+=32*cosd(-Mm+omega);
    
    dp+=-51*sind(Mm+2*F+omega);
    de+=27*cosd(Mm+2*F+omega);	
    
    dp+=48*sind(-2*D+2*Mm);
      
    dp+=46*sind(-2*Mm+2*F+omega);
    de+=-24*cosd(-2*Mm+2*F+omega);
    
    dp+=-38*sind(2*D+2*F+2*omega);
    de+=16*cosd(2*D+2*F+2*omega);
    
    dp+=-31*sind(2*Mm+2*F+2*omega);
    de+=13*cosd(2*Mm+2*F+2*omega);	
    
    dp+=29*sind(2*Mm);
      
    dp+=29*sind(-2*D+Mm+2*F+2*omega);
    de+=-12*cosd(-2*D+Mm+2*F+2*omega);
    
    dp+=26*sind(2*F);
    
    dp+=-22*sind(-2*D+2*F);
      
    dp+=21*sind(-Mm+2*F+omega);
    de+=-10*cosd(-Mm+2*F+omega);
    
    dp+=(17-0.1*TE)*sind(2*M);
      
    dp+=16*sind(2*D-Mm+omega);
    de+=-8*cosd(2*D-Mm+omega);
    
    dp+=(-16+0.1*TE)*sind(-2*D+2*M+2*F+2*omega);
    de+=7*cosd(-2*D+2*M+2*F+2*omega);
    
    dp+=-15*sind(M+omega);
    de+=9*cosd(M+omega);
    
    dp+=-13*sind(-2*D+Mm+omega);
    de+=7*cosd(-2*D+Mm+omega);
    
    dp+=-12*sind(-M+omega);
    de+=6*cosd(-M+omega);
    
    dp+=11*sind(2*Mm-2*F);
      
    dp+=-10*sind(2*D-Mm+2*F+omega);
    de+=5*cosd(2*D-Mm+2*F+omega);
    
    dp+=-8*sind(2*D+Mm+2*F+2*omega);
    de+=3*cosd(2*D+Mm+2*F+2*omega);
    
    dp+=7*sind(M+2*F+2*omega);
    de+=-3*cosd(M+2*F+2*omega);
    
    dp+=-7*sind(-2*D+M+Mm);
      
    dp+=-7*sind(-M+2*F+2*omega);
    de+=3*cosd(-M+2*F+2*omega);
    
    dp+=-7*sind(2*D+2*F+omega);
    de+=3*cosd(2*D+2*F+omega);
    
    dp+=6*sind(2*D+Mm);
      
    dp+=6*sind(-2*D+2*Mm+2*F+2*omega);
    de+=-3*cosd(-2*D+2*Mm+2*F+2*omega);
    
    dp+=6*sind(-2*D+Mm+2*F+omega);
    de+=-3*cosd(-2*D+Mm+2*F+omega);
    
    dp+=-6*sind(2*D-2*Mm+omega);
    de+=3*cosd(2*D-2*Mm+omega);
    
    dp+=-6*sind(2*D+omega);
    de+=3*cosd(2*D+omega);
    
    dp+=5*sind(-M+Mm);
      
    dp+=-5*sind(-2*D-M+2*F+omega);
    de+=3*cosd(-2*D-M+2*F+omega);
    
    dp+=-5*sind(-2*D+omega);
    de+=3*cosd(-2*D+omega);
    
    dp+=-5*sind(2*Mm+2*F+omega);
    de+=3*cosd(2*Mm+2*F+omega);
    
    dp+=4*sind(-2*D+2*Mm+omega);
      
    dp+=4*sind(-2*D+M+2*F+omega);
      
    dp+=4*sind(Mm-2*F);	
    
    dp+=-4*sind(-D+Mm);
      
    dp+=-4*sind(-2*D+M);
      
    dp+=-4*sind(D);
      
    dp+=3*sind(Mm+2*F);
      
    dp+=-3*sind(-2*Mm+2*F+2*omega);
      
    dp+=-3*sind(-D-M+Mm);
      
    dp+=-3*sind(M+Mm);
      
    dp+=-3*sind(-M+Mm+2*F+2*omega);
      
    dp+=-3*sind(2*D-M-Mm+2*F+2*omega);
      
    dp+=-3*sind(3*Mm+2*F+2*omega);
      
    dp+=-3*sind(2*D-M+2*F+2*omega);
    
    //nutation in longitude
    delta_psi = dp/36000000.0;
    
    //nutation in obliquity
    delta_eps = de/36000000.0;
      
    //Mean obliquity of the ecliptic
    eps0 = (84381.448-46.815*TE-0.00059*TE2+0.001813*TE3)/3600.0;
    
    //True obliquity of the ecliptic
    eps = eps0+delta_eps;
  }

  //GHA aries, GAST, GMST, equation of the equinoxes
  private void processAries()
  {
    //Mean GHA aries
    GHAAmean = 280.46061837+ 360.98564736629*(JD-2451545)+0.000387933*T2-T3/38710000;
    GHAAmean = trunc(GHAAmean);
    
    //True GHA aries
    GHAAtrue = GHAAmean+delta_psi*cosd(eps);
    GHAAtrue = trunc(GHAAtrue);
    
    //Equation of the equinoxes
    EoE = 240*delta_psi*cosd(eps);
  }

  //Calculations for the sun
  private void processSun()
  {
    double L0 = 0.0, L1 = 0.0, L2 = 0.0, L3 = 0.0, L4 = 0.0, L5 = 0.0;
    double Lhelioc = 0.0;
    double B = 0.0, B0 = 0.0, B1 = 0.0, B2 = 0.0, B3 = 0.0, B4 = 0.0, B5 = 0.0;
    double beta = 0.0;
    double Lsun_prime= 0.0;
    double R = 0.0, R0 = 0.0, R1 = 0.0, R2 = 0.0, R3 = 0.0, R4 = 0.0;
           
    double Lsun_mean = 0.0;
  
    //Periodic terms for the sun
  
    //Longitude
    L0=175347046;
    L0+=3341656*Math.cos(4.6692568+6283.0758500*Tau);
    L0+=34894*Math.cos(4.62610+12566.15170*Tau);
    L0+=3497*Math.cos(2.7441+5753.3849*Tau);
    L0+=3418*Math.cos(2.8289+3.5231*Tau);
    L0+=3136*Math.cos(3.6277+77713.7715*Tau);
    L0+=2676*Math.cos(4.4181+7860.4194*Tau);
    L0+=2343*Math.cos(6.1352+3930.2097*Tau);
    L0+=1324*Math.cos(0.7425+11506.7698*Tau);
    L0+=1273*Math.cos(2.0371+529.6910*Tau);
  
    L0+=1199*Math.cos(1.1096+1577.3435*Tau);
    L0+=990*Math.cos(5.233+5884.927*Tau);
    L0+=902*Math.cos(2.045+26.298*Tau);
    L0+=857*Math.cos(3.508+398.149*Tau);
    L0+=780*Math.cos(1.179+5223.694*Tau);
    L0+=753*Math.cos(2.533+5507.553*Tau);
    L0+=505*Math.cos(4.583+18849.228*Tau);
    L0+=492*Math.cos(4.205+775.523*Tau);
    L0+=357*Math.cos(2.920+0.067*Tau);
    L0+=317*Math.cos(5.849+11790.629*Tau);
  
    L0+=284*Math.cos(1.899+796.298*Tau);
    L0+=271*Math.cos(0.315+10977.079*Tau);
    L0+=243*Math.cos(0.345+5486.778*Tau);
    L0+=206*Math.cos(4.806+2544.314*Tau);
    L0+=205*Math.cos(1.869+5573.143*Tau);
    L0+=202*Math.cos(2.458+6069.777*Tau);
    L0+=156*Math.cos(0.833+213.299*Tau);
    L0+=132*Math.cos(3.411+2942.463*Tau);
    L0+=126*Math.cos(1.083+20.775*Tau);
    L0+=115*Math.cos(0.645+0.980*Tau);
  
    L0+=103*Math.cos(0.636+4694.003*Tau);
    L0+=102*Math.cos(0.976+15720.839*Tau);
    L0+=102*Math.cos(4.267+7.114*Tau);
    L0+=99*Math.cos(6.21+2146.17*Tau);
    L0+=98*Math.cos(0.68+155.42*Tau);
    L0+=86*Math.cos(5.98+161000.69*Tau);
    L0+=85*Math.cos(1.30+6275.96*Tau);
    L0+=85*Math.cos(3.67+71430.70*Tau);
    L0+=80*Math.cos(1.81+17260.15*Tau);
    L0+=79*Math.cos(3.04+12036.46*Tau);
  
    L0+=75*Math.cos(1.76+5088.63*Tau);
    L0+=74*Math.cos(3.50+3154.69*Tau);
    L0+=74*Math.cos(4.68+801.82*Tau);
    L0+=70*Math.cos(0.83+9437.76*Tau);
    L0+=62*Math.cos(3.98+8827.39*Tau);
    L0+=61*Math.cos(1.82+7084.90*Tau);
    L0+=57*Math.cos(2.78+6286.60*Tau);
    L0+=56*Math.cos(4.39+14143.50*Tau);
    L0+=56*Math.cos(3.47+6279.55*Tau);
    L0+=52*Math.cos(0.19+12139.55*Tau);
  
    L0+=52*Math.cos(1.33+1748.02*Tau);
    L0+=51*Math.cos(0.28+5856.48*Tau);
    L0+=49*Math.cos(0.49+1194.45*Tau);
    L0+=41*Math.cos(5.37+8429.24*Tau);
    L0+=41*Math.cos(2.40+19651.05*Tau);
    L0+=39*Math.cos(6.17+10447.39*Tau);
    L0+=37*Math.cos(6.04+10213.29*Tau);
    L0+=37*Math.cos(2.57+1059.38*Tau);
    L0+=36*Math.cos(1.71+2352.87*Tau);
    L0+=36*Math.cos(1.78+6812.77*Tau);
  
    L0+=33*Math.cos(0.59+17789.85*Tau);
    L0+=30*Math.cos(0.44+83996.85*Tau);
    L0+=30*Math.cos(2.74+1349.87*Tau);
    L0+=25*Math.cos(3.16+4690.48*Tau);
  
    L1=628331966747.0;
    L1+=206059*Math.cos(2.678235+6283.075850*Tau);
    L1+=4303*Math.cos(2.6351+12566.1517*Tau);
    L1+=425*Math.cos(1.590+3.523*Tau);
    L1+=119*Math.cos(5.796+26.298*Tau);
    L1+=109*Math.cos(2.966+1577.344*Tau);
    L1+=93*Math.cos(2.59+18849.23*Tau);
    L1+=72*Math.cos(1.14+529.69*Tau);
    L1+=68*Math.cos(1.87+398.15*Tau);
    L1+=67*Math.cos(4.41+5507.55*Tau);
  
    L1+=59*Math.cos(2.89+5223.69*Tau);
    L1+=56*Math.cos(2.17+155.42*Tau);
    L1+=45*Math.cos(0.40+796.30*Tau);
    L1+=36*Math.cos(0.47+775.52*Tau);
    L1+=29*Math.cos(2.65+7.11*Tau);
    L1+=21*Math.cos(5.34+0.98*Tau);
    L1+=19*Math.cos(1.85+5486.78*Tau);
    L1+=19*Math.cos(4.97+213.30*Tau);
    L1+=17*Math.cos(2.99+6275.96*Tau);
    L1+=16*Math.cos(0.03+2544.31*Tau);
  
    L1+=16*Math.cos(1.43+2146.17*Tau);
    L1+=15*Math.cos(1.21+10977.08*Tau);
    L1+=12*Math.cos(2.83+1748.02*Tau);
    L1+=12*Math.cos(3.26+5088.63*Tau);
    L1+=12*Math.cos(5.27+1194.45*Tau);
    L1+=12*Math.cos(2.08+4694.00*Tau);
    L1+=11*Math.cos(0.77+553.57*Tau);
    L1+=10*Math.cos(1.30+6286.60*Tau);
    L1+=10*Math.cos(4.24+1349.87*Tau);
    L1+=9*Math.cos(2.70+242.73*Tau);
  
    L1+=9*Math.cos(5.64+951.72*Tau);
    L1+=8*Math.cos(5.30+2352.87*Tau);
    L1+=6*Math.cos(2.65+9437.76*Tau);
    L1+=6*Math.cos(4.67+4690.48*Tau);
  
    
    L2=52919;
    L2+=8720*Math.cos(1.0721+6283.0758*Tau);
    L2+=309*Math.cos(0.867+12566.152*Tau);
    L2+=27*Math.cos(0.05+3.52*Tau);
    L2+=16*Math.cos(5.19+26.30*Tau);
    L2+=16*Math.cos(3.68+155.42*Tau);
    L2+=10*Math.cos(0.76+18849.23*Tau);
    L2+=9*Math.cos(2.06+77713.77*Tau);
    L2+=7*Math.cos(0.83+775.52*Tau);
    L2+=5*Math.cos(4.66+1577.34*Tau);
  
    L2+=4*Math.cos(1.03+7.11*Tau);
    L2+=4*Math.cos(3.44+5573.14*Tau);
    L2+=3*Math.cos(5.14+796.30*Tau);
    L2+=3*Math.cos(6.05+5507.55*Tau);
    L2+=3*Math.cos(1.19+242.73*Tau);
    L2+=3*Math.cos(6.12+529.69*Tau);
    L2+=3*Math.cos(0.31+398.15*Tau);
    L2+=3*Math.cos(2.28+553.57*Tau);
    L2+=2*Math.cos(4.38+5223.69*Tau);
    L2+=2*Math.cos(3.75+0.98*Tau);
  
    
    L3=289*Math.cos(5.844+6283.076*Tau);
    L3+=35;
    L3+=17*Math.cos(5.49+12566.15*Tau);
    L3+=3*Math.cos(5.20+155.42*Tau);
    L3+=1*Math.cos(4.72+3.52*Tau);
    L3+=1*Math.cos(5.30+18849.23*Tau);
    L3+=1*Math.cos(5.97+242.73*Tau);
    
    L4=114*Math.cos(3.142);
    L4+=8*Math.cos(4.13+6283.08*Tau);
    L4+=1*Math.cos(3.84+12566.15*Tau);
  
    L5 = 1*Math.cos(3.14);
  
    //Heliocentric longitude
    Lhelioc = (L0+L1*Tau+L2*Tau2+L3*Tau3+L4*Tau4+L5*Tau5)/1e8/dtr;
    Lhelioc = trunc(Lhelioc);
  
    //Geocentric longitude
    Lsun_true = Lhelioc+180-0.000025;
    Lsun_true = trunc(Lsun_true);
  
    //Latitude
    B0=280*Math.cos(3.199+84334.662*Tau);
    B0+=102*Math.cos(5.422+5507.553*Tau);
    B0+=80*Math.cos(3.88+5223.69*Tau);
    B0+=44*Math.cos(3.70+2352.87*Tau);
    B0+=32*Math.cos(4.00+1577.34*Tau);
    
    B1=9*Math.cos(3.90+5507.55*Tau);
    B1+=6*Math.cos(1.73+5223.69*Tau);
  
    //Heliocentric latitude
    B = (B0+B1*Tau)/1e8/dtr;
    
    //Geocentric latitude
    beta = -B;
    beta = trunc(beta);
  
    //Corrections
    Lsun_prime = Lhelioc+180-1.397*TE-0.00031*TE2;
    Lsun_prime = trunc(Lsun_prime);
  
    beta = beta+0.000011*(cosd(Lsun_prime)-sind(Lsun_prime));
  
    //Distance earth-sun
    R0=100013989;
    R0+=1670700*Math.cos(3.0984635+6283.0758500*Tau);
    R0+=13956*Math.cos(3.05525+12566.15170*Tau);
    R0+=3084*Math.cos(5.1985+77713.7715*Tau);
    R0+=1628*Math.cos(1.1739+5753.3849*Tau);
    R0+=1576*Math.cos(2.8469+7860.4194*Tau);
    R0+=925*Math.cos(5.453+11506.770*Tau);
    R0+=542*Math.cos(4.564+3930.210*Tau);
    R0+=472*Math.cos(3.661+5884.927*Tau);
    R0+=346*Math.cos(0.964+5507.553*Tau);
  
    R0+=329*Math.cos(5.900+5223.694*Tau);
    R0+=307*Math.cos(0.299+5573.143*Tau);
    R0+=243*Math.cos(4.273+11790.629*Tau);
    R0+=212*Math.cos(5.847+1577.344*Tau);
    R0+=186*Math.cos(5.022+10977.079*Tau);
    R0+=175*Math.cos(3.012+18849.228*Tau);
    R0+=110*Math.cos(5.055+5486.778*Tau);
    R0+=98*Math.cos(0.89+6069.78*Tau);
    R0+=86*Math.cos(5.69+15720.84*Tau);
    R0+=86*Math.cos(1.27+161000.69*Tau);
  
    R0+=65*Math.cos(0.27+17260.15*Tau);
    R0+=63*Math.cos(0.92+529.69*Tau);
    R0+=57*Math.cos(2.01+83996.85*Tau);
    R0+=56*Math.cos(5.24+71430.70*Tau);
    R0+=49*Math.cos(3.25+2544.31*Tau);
    R0+=47*Math.cos(2.58+775.52*Tau);
    R0+=45*Math.cos(5.54+9437.76*Tau);
    R0+=43*Math.cos(6.01+6275.96*Tau);
    R0+=39*Math.cos(5.36+4694.00*Tau);
    R0+=38*Math.cos(2.39+8827.39*Tau);
  
    R0+=37*Math.cos(0.83+19651.05*Tau);
    R0+=37*Math.cos(4.90+12139.55*Tau);
    R0+=36*Math.cos(1.67+12036.46*Tau);
    R0+=35*Math.cos(1.84+2942.46*Tau);
    R0+=33*Math.cos(0.24+7084.90*Tau);
    R0+=32*Math.cos(0.18+5088.63*Tau);
    R0+=32*Math.cos(1.78+398.15*Tau);
    R0+=28*Math.cos(1.21+6286.60*Tau);
    R0+=28*Math.cos(1.90+6279.55*Tau);
    R0+=26*Math.cos(4.59+10447.39*Tau);
  
    
    R1=103019*Math.cos(1.107490+6283.075850*Tau);
    R1+=1721*Math.cos(1.0644+12566.1517*Tau);
    R1+=702*Math.cos(3.142);
    R1+=32*Math.cos(1.02+18849.23*Tau);
    R1+=31*Math.cos(2.84+5507.55*Tau);
    R1+=25*Math.cos(1.32+5223.69*Tau);
    R1+=18*Math.cos(1.42+1577.34*Tau);
    R1+=10*Math.cos(5.91+10977.08*Tau);
    R1+=9*Math.cos(1.42+6275.96*Tau);
    R1+=9*Math.cos(0.27+5486.78*Tau);
  
    
    R2=4359*Math.cos(5.7846+6283.0758*Tau);
    R2+=124*Math.cos(5.579+12566.152*Tau);
    R2+=12*Math.cos(3.14);
    R2+=9*Math.cos(3.63+77713.77*Tau);
    R2+=6*Math.cos(1.87+5573.14*Tau);
    R2+=3*Math.cos(5.47+18849.23*Tau);
    
    R3=145*Math.cos(4.273+6283.076*Tau);
    R3+=7*Math.cos(3.92+12566.15*Tau);
    
    R4 = 4*Math.cos(2.56+6283.08*Tau);
    
    R = (R0+R1*Tau+R2*Tau2+R3*Tau3+R4*Tau4)/1e8;
  
    //Apparent longitude of the sun
    lambda = Lsun_true+delta_psi-0.005691611/R;
    lambda = trunc(lambda);
  
    //Right ascension of the sun, apparent
    RAsun = Math.atan2((sind(lambda)*cosd(eps)-tand(beta)*sind(eps)),cosd(lambda))/dtr;
    RAsun = trunc(RAsun);
  
    //Siderial hour angle of the sun, apparent
    SHAsun = 360-RAsun;
  
    //Declination of the sun, apparent 
    DECsun = Math.asin(sind(beta)*cosd(eps)+cosd(beta)*sind(eps)*sind(lambda))/dtr;
    Dsun = DECsun;
  
    //GHA of the sun
    GHAsun = GHAAtrue-RAsun;
    GHAsun = trunc(GHAsun);
    
    //Semidiameter of the sun
    SDsun = 959.63/R;
  
    //Horizontal parallax of the sun
    HPsun = 8.794/R;
  
    //Mean longitude of the sun
    Lsun_mean = 280.4664567+360007.6982779*Tau+0.03032028*Tau2+Tau3/49931-Tau4/15299-Tau5/1988000;
    Lsun_mean = trunc(Lsun_mean);
  
    //Equation of time
    EOT = 4*(Lsun_mean-0.0057183-0.0008-RAsun+delta_psi*cosd(eps));
    EOT = 4*GHAsun+720-1440*dayfraction;
    if (EOT>20) EOT-=1440;
    if (EOT<-20) EOT+=1440;
  }
  
  //Calculation of ephemerides for the moon
  private void processMoon()
  {
    double Lmoon_mean = 0.0, Msun_mean = 0.0, Mmoon_mean = 0.0,
           lambdaMm = 0.0, betaM = 0.0, dEM = 0.0,
           RAmoon = 0.0,
           Dmoon = 0.0;

    // Mean longitude of the moon
    Lmoon_mean = 218.3164591+481267.88134236*TE-0.0013268*TE2+TE3/538841-TE4/65194000;
    Lmoon_mean = trunc(Lmoon_mean);
  
    //Mean elongation of the moon
    D = 297.8502042+445267.1115168*TE-0.00163*TE2+TE3/545868-TE4/113065000;
    D = trunc(D);
  
    // Mean anomaly of the sun
    Msun_mean = 357.5291092+35999.0502909*TE-0.0001536*TE2+TE3/24490000;
    Msun_mean = trunc(Msun_mean);
  
    //Mean anomaly of the moon
    Mmoon_mean = 134.9634114+477198.8676313*TE+0.008997*TE2+TE3/69699-TE4/14712000;
    Mmoon_mean = trunc(Mmoon_mean);
  
    //Mean distance of the moon from her ascending node
    F = 93.2720993+483202.0175273*TE-0.0034029*TE2-TE3/3526000+TE4/863310000;
    F = trunc(F);
  
    //Corrections
    double A1 =119.75+131.849*TE;
    A1 = 360*(A1/360 - Math.floor(A1/360));
    double A2 = 53.09+479264.29*TE;
    A2 = 360*(A2/360 - Math.floor(A2/360));
    double A3 = 313.45+481266.484*TE;
    A3 = 360*(A3/360 - Math.floor(A3/360));
  
    double fE = 1-0.002516*TE-0.0000074*TE2;
    double fE2 = fE*fE;
    
    //Periodic terms for the moon
  
    //Longitude and distance
    int[] fD = new int[60];
    int[] fMms = new int[60];
    int[] fMmm = new int[60];
    int[] fF = new int[60];
    int[] coeffs = new int[60];
    int[] coeffc = new int[60];
  
    fD[0]=0; fMms[0]=0; fMmm[0]=1; fF[0]=0; coeffs[0]=6288774; coeffc[0]=-20905355;
    fD[1]=2; fMms[1]=0; fMmm[1]=-1; fF[1]=0; coeffs[1]=1274027; coeffc[1]=-3699111;
    fD[2]=2; fMms[2]=0; fMmm[2]=0; fF[2]=0; coeffs[2]=658314; coeffc[2]=-2955968;
    fD[3]=0; fMms[3]=0; fMmm[3]=2; fF[3]=0; coeffs[3]=213618; coeffc[3]=-569925;
    fD[4]=0; fMms[4]=1; fMmm[4]=0; fF[4]=0; coeffs[4]=-185116; coeffc[4]=48888;
    fD[5]=0; fMms[5]=0; fMmm[5]=0; fF[5]=2; coeffs[5]=-114332; coeffc[5]=-3149;
    fD[6]=2; fMms[6]=0; fMmm[6]=-2; fF[6]=0; coeffs[6]=58793; coeffc[6]=246158;
    fD[7]=2; fMms[7]=-1; fMmm[7]=-1; fF[7]=0; coeffs[7]=57066; coeffc[7]=-152138;
    fD[8]=2; fMms[8]=0; fMmm[8]=1; fF[8]=0; coeffs[8]=53322; coeffc[8]=-170733;
    fD[9]=2; fMms[9]=-1; fMmm[9]=0; fF[9]=0; coeffs[9]=45758; coeffc[9]=-204586;
  
    fD[10]=0; fMms[10]=1; fMmm[10]=-1; fF[10]=0; coeffs[10]=-40923; coeffc[10]=-129620;
    fD[11]=1; fMms[11]=0; fMmm[11]=0; fF[11]=0; coeffs[11]=-34720; coeffc[11]=108743;
    fD[12]=0; fMms[12]=1; fMmm[12]=1; fF[12]=0; coeffs[12]=-30383; coeffc[12]=104755;
    fD[13]=2; fMms[13]=0; fMmm[13]=0; fF[13]=-2; coeffs[13]=15327; coeffc[13]=10321;
    fD[14]=0; fMms[14]=0; fMmm[14]=1; fF[14]=2; coeffs[14]=-12528; coeffc[14]=0;
    fD[15]=0; fMms[15]=0; fMmm[15]=1; fF[15]=-2; coeffs[15]=10980; coeffc[15]=79661;
    fD[16]=4; fMms[16]=0; fMmm[16]=-1; fF[16]=0; coeffs[16]=10675; coeffc[16]=-34782;
    fD[17]=0; fMms[17]=0; fMmm[17]=3; fF[17]=0; coeffs[17]=10034; coeffc[17]=-23210;
    fD[18]=4; fMms[18]=0; fMmm[18]=-2; fF[18]=0; coeffs[18]=8548; coeffc[18]=-21636;
    fD[19]=2; fMms[19]=1; fMmm[19]=-1; fF[19]=0; coeffs[19]=-7888; coeffc[19]=24208;
  
    fD[20]=2; fMms[20]=1; fMmm[20]=0; fF[20]=0; coeffs[20]=-6766; coeffc[20]=30824;
    fD[21]=1; fMms[21]=0; fMmm[21]=-1; fF[21]=0; coeffs[21]=-5163; coeffc[21]=-8379;
    fD[22]=1; fMms[22]=1; fMmm[22]=0; fF[22]=0; coeffs[22]=4987; coeffc[22]=-16675;
    fD[23]=2; fMms[23]=-1; fMmm[23]=1; fF[23]=0; coeffs[23]=4036; coeffc[23]=-12831;
    fD[24]=2; fMms[24]=0; fMmm[24]=2; fF[24]=0; coeffs[24]=3994; coeffc[24]=-10445;
    fD[25]=4; fMms[25]=0; fMmm[25]=0; fF[25]=0; coeffs[25]=3861; coeffc[25]=-11650;
    fD[26]=2; fMms[26]=0; fMmm[26]=-3; fF[26]=0; coeffs[26]=3665; coeffc[26]=14403;
    fD[27]=0; fMms[27]=1; fMmm[27]=-2; fF[27]=0; coeffs[27]=-2689; coeffc[27]=-7003;
    fD[28]=2; fMms[28]=0; fMmm[28]=-1; fF[28]=2; coeffs[28]=-2602; coeffc[28]=0;
    fD[29]=2; fMms[29]=-1; fMmm[29]=-2; fF[29]=0; coeffs[29]=2390; coeffc[29]=10056;
  
    fD[30]=1; fMms[30]=0; fMmm[30]=1; fF[30]=0; coeffs[30]=-2348; coeffc[30]=6322;
    fD[31]=2; fMms[31]=-2; fMmm[31]=0; fF[31]=0; coeffs[31]=2236; coeffc[31]=-9884;
    fD[32]=0; fMms[32]=1; fMmm[32]=2; fF[32]=0; coeffs[32]=-2120; coeffc[32]=5751;
    fD[33]=0; fMms[33]=2; fMmm[33]=0; fF[33]=0; coeffs[33]=-2069; coeffc[33]=0;
    fD[34]=2; fMms[34]=-2; fMmm[34]=-1; fF[34]=0; coeffs[34]=2048; coeffc[34]=-4950;
    fD[35]=2; fMms[35]=0; fMmm[35]=1; fF[35]=-2; coeffs[35]=-1773; coeffc[35]=4130;
    fD[36]=2; fMms[36]=0; fMmm[36]=0; fF[36]=2; coeffs[36]=-1595; coeffc[36]=0;
    fD[37]=4; fMms[37]=-1; fMmm[37]=-1; fF[37]=0; coeffs[37]=1215; coeffc[37]=-3958;
    fD[38]=0; fMms[38]=0; fMmm[38]=2; fF[38]=2; coeffs[38]=-1110; coeffc[38]=0;
    fD[39]=3; fMms[39]=0; fMmm[39]=-1; fF[39]=0; coeffs[39]=-892; coeffc[39]=3258;
  
    fD[40]=2; fMms[40]=1; fMmm[40]=1; fF[40]=0; coeffs[40]=-810; coeffc[40]=2616;
    fD[41]=4; fMms[41]=-1; fMmm[41]=-2; fF[41]=0; coeffs[41]=759; coeffc[41]=-1897;
    fD[42]=0; fMms[42]=2; fMmm[42]=-1; fF[42]=0; coeffs[42]=-713; coeffc[42]=-2117;
    fD[43]=2; fMms[43]=2; fMmm[43]=-1; fF[43]=0; coeffs[43]=-700; coeffc[43]=2354;
    fD[44]=2; fMms[44]=1; fMmm[44]=-2; fF[44]=0; coeffs[44]=691; coeffc[44]=0;
    fD[45]=2; fMms[45]=-1; fMmm[45]=0; fF[45]=-2; coeffs[45]=596; coeffc[45]=0;
    fD[46]=4; fMms[46]=0; fMmm[46]=1; fF[46]=0; coeffs[46]=549; coeffc[46]=-1423;
    fD[47]=0; fMms[47]=0; fMmm[47]=4; fF[47]=0; coeffs[47]=537; coeffc[47]=-1117;
    fD[48]=4; fMms[48]=-1; fMmm[48]=0; fF[48]=0; coeffs[48]=520; coeffc[48]=-1571;
    fD[49]=1; fMms[49]=0; fMmm[49]=-2; fF[49]=0; coeffs[49]=-487; coeffc[49]=-1739;
  
    fD[50]=2; fMms[50]=1; fMmm[50]=0; fF[50]=-2; coeffs[50]=-399; coeffc[50]=0;
    fD[51]=0; fMms[51]=0; fMmm[51]=2; fF[51]=-2; coeffs[51]=-381; coeffc[51]=-4421;
    fD[52]=1; fMms[52]=1; fMmm[52]=1; fF[52]=0; coeffs[52]=351; coeffc[52]=0;
    fD[53]=3; fMms[53]=0; fMmm[53]=-2; fF[53]=0; coeffs[53]=-340; coeffc[53]=0;
    fD[54]=4; fMms[54]=0; fMmm[54]=-3; fF[54]=0; coeffs[54]=330; coeffc[54]=0;
    fD[55]=2; fMms[55]=-1; fMmm[55]=2; fF[55]=0; coeffs[55]=327; coeffc[55]=0;
    fD[56]=0; fMms[56]=2; fMmm[56]=1; fF[56]=0; coeffs[56]=-323; coeffc[56]=1165;
    fD[57]=1; fMms[57]=1; fMmm[57]=-1; fF[57]=0; coeffs[57]=299; coeffc[57]=0;
    fD[58]=2; fMms[58]=0; fMmm[58]=3; fF[58]=0; coeffs[58]=294; coeffc[58]=0;
    fD[59]=2; fMms[59]=0; fMmm[59]=-1; fF[59]=-2; coeffs[59]=0; coeffc[59]=8752;
  
    //Latitude
    int[] fD2 = new int[60];
    int[] fMms2= new int[60];
    int[] fMmm2 = new int[60];
    int[] fF2 = new int[60];
    int[] coeffs2 = new int[60];
    
    fD2[0]=0; fMms2[0]=0; fMmm2[0]=0; fF2[0]=1; coeffs2[0]=5128122;
    fD2[1]=0; fMms2[1]=0; fMmm2[1]=1; fF2[1]=1; coeffs2[1]=280602;
    fD2[2]=0; fMms2[2]=0; fMmm2[2]=1; fF2[2]=-1; coeffs2[2]=277693;
    fD2[3]=2; fMms2[3]=0; fMmm2[3]=0; fF2[3]=-1; coeffs2[3]=173237;
    fD2[4]=2; fMms2[4]=0; fMmm2[4]=-1; fF2[4]=1; coeffs2[4]=55413;
    fD2[5]=2; fMms2[5]=0; fMmm2[5]=-1; fF2[5]=-1; coeffs2[5]=46271;
    fD2[6]=2; fMms2[6]=0; fMmm2[6]=0; fF2[6]=1; coeffs2[6]=32573;
    fD2[7]=0; fMms2[7]=0; fMmm2[7]=2; fF2[7]=1; coeffs2[7]=17198;
    fD2[8]=2; fMms2[8]=0; fMmm2[8]=1; fF2[8]=-1; coeffs2[8]=9266;
    fD2[9]=0; fMms2[9]=0; fMmm2[9]=2; fF2[9]=-1; coeffs2[9]=8822;
  
    fD2[10]=2; fMms2[10]=-1; fMmm2[10]=0; fF2[10]=-1; coeffs2[10]=8216;
    fD2[11]=2; fMms2[11]=0; fMmm2[11]=-2; fF2[11]=-1; coeffs2[11]=4324;
    fD2[12]=2; fMms2[12]=0; fMmm2[12]=1; fF2[12]=1; coeffs2[12]=4200;
    fD2[13]=2; fMms2[13]=1; fMmm2[13]=0; fF2[13]=-1; coeffs2[13]=-3359;
    fD2[14]=2; fMms2[14]=-1; fMmm2[14]=-1; fF2[14]=1; coeffs2[14]=2463;
    fD2[15]=2; fMms2[15]=-1; fMmm2[15]=0; fF2[15]=1; coeffs2[15]=2211;
    fD2[16]=2; fMms2[16]=-1; fMmm2[16]=-1; fF2[16]=-1; coeffs2[16]=2065;
    fD2[17]=0; fMms2[17]=1; fMmm2[17]=-1; fF2[17]=-1; coeffs2[17]=-1870;
    fD2[18]=4; fMms2[18]=0; fMmm2[18]=-1; fF2[18]=-1; coeffs2[18]=1828;
    fD2[19]=0; fMms2[19]=1; fMmm2[19]=0; fF2[19]=1; coeffs2[19]=-1794;
    
    fD2[20]=0; fMms2[20]=0; fMmm2[20]=0; fF2[20]=3; coeffs2[20]=-1749;
    fD2[21]=0; fMms2[21]=1; fMmm2[21]=-1; fF2[21]=1; coeffs2[21]=-1565;
    fD2[22]=1; fMms2[22]=0; fMmm2[22]=0; fF2[22]=1; coeffs2[22]=-1491;
    fD2[23]=0; fMms2[23]=1; fMmm2[23]=1; fF2[23]=1; coeffs2[23]=-1475;
    fD2[24]=0; fMms2[24]=1; fMmm2[24]=1; fF2[24]=-1; coeffs2[24]=-1410;
    fD2[25]=0; fMms2[25]=1; fMmm2[25]=0; fF2[25]=-1; coeffs2[25]=-1344;
    fD2[26]=1; fMms2[26]=0; fMmm2[26]=0; fF2[26]=-1; coeffs2[26]=-1335;
    fD2[27]=0; fMms2[27]=0; fMmm2[27]=3; fF2[27]=1; coeffs2[27]=1107;
    fD2[28]=4; fMms2[28]=0; fMmm2[28]=0; fF2[28]=-1; coeffs2[28]=1021;
    fD2[29]=4; fMms2[29]=0; fMmm2[29]=-1; fF2[29]=1; coeffs2[29]=833;
    
    fD2[30]=0; fMms2[30]=0; fMmm2[30]=1; fF2[30]=-3; coeffs2[30]=777;
    fD2[31]=4; fMms2[31]=0; fMmm2[31]=-2; fF2[31]=1; coeffs2[31]=671;
    fD2[32]=2; fMms2[32]=0; fMmm2[32]=0; fF2[32]=-3; coeffs2[32]=607;
    fD2[33]=2; fMms2[33]=0; fMmm2[33]=2; fF2[33]=-1; coeffs2[33]=596;
    fD2[34]=2; fMms2[34]=-1; fMmm2[34]=1; fF2[34]=-1; coeffs2[34]=491;
    fD2[35]=2; fMms2[35]=0; fMmm2[35]=-2; fF2[35]=1; coeffs2[35]=-451;
    fD2[36]=0; fMms2[36]=0; fMmm2[36]=3; fF2[36]=-1; coeffs2[36]=439;
    fD2[37]=2; fMms2[37]=0; fMmm2[37]=2; fF2[37]=1; coeffs2[37]=422;
    fD2[38]=2; fMms2[38]=0; fMmm2[38]=-3; fF2[38]=-1; coeffs2[38]=421;
    fD2[39]=2; fMms2[39]=1; fMmm2[39]=-1; fF2[39]=1; coeffs2[39]=-366;
  
    fD2[40]=2; fMms2[40]=1; fMmm2[40]=0; fF2[40]=1; coeffs2[40]=-351;
    fD2[41]=4; fMms2[41]=0; fMmm2[41]=0; fF2[41]=1; coeffs2[41]=331;
    fD2[42]=2; fMms2[42]=-1; fMmm2[42]=1; fF2[42]=1; coeffs2[42]=315;
    fD2[43]=2; fMms2[43]=-2; fMmm2[43]=0; fF2[43]=-1; coeffs2[43]=302;
    fD2[44]=0; fMms2[44]=0; fMmm2[44]=1; fF2[44]=3; coeffs2[44]=-283;
    fD2[45]=2; fMms2[45]=1; fMmm2[45]=1; fF2[45]=-1; coeffs2[45]=-229;
    fD2[46]=1; fMms2[46]=1; fMmm2[46]=0; fF2[46]=-1; coeffs2[46]=223;
    fD2[47]=1; fMms2[47]=1; fMmm2[47]=0; fF2[47]=1; coeffs2[47]=223;
    fD2[48]=0; fMms2[48]=1; fMmm2[48]=-2; fF2[48]=-1; coeffs2[48]=-220;
    fD2[49]=2; fMms2[49]=1; fMmm2[49]=-1; fF2[49]=-1; coeffs2[49]=-220;
  
    fD2[50]=1; fMms2[50]=0; fMmm2[50]=1; fF2[50]=1; coeffs2[50]=-185;
    fD2[51]=2; fMms2[51]=-1; fMmm2[51]=-2; fF2[51]=-1; coeffs2[51]=181;
    fD2[52]=0; fMms2[52]=1; fMmm2[52]=2; fF2[52]=1; coeffs2[52]=-177;
    fD2[53]=4; fMms2[53]=0; fMmm2[53]=-2; fF2[53]=-1; coeffs2[53]=176;
    fD2[54]=4; fMms2[54]=-1; fMmm2[54]=-1; fF2[54]=-1; coeffs2[54]=166;
    fD2[55]=1; fMms2[55]=0; fMmm2[55]=1; fF2[55]=-1; coeffs2[55]=-164;
    fD2[56]=4; fMms2[56]=0; fMmm2[56]=1; fF2[56]=-1; coeffs2[56]=132;
    fD2[57]=1; fMms2[57]=0; fMmm2[57]=-1; fF2[57]=-1; coeffs2[57]=-119;
    fD2[58]=4; fMms2[58]=-1; fMmm2[58]=0; fF2[58]=-1; coeffs2[58]=115;
    fD2[59]=2; fMms2[59]=-2; fMmm2[59]=0; fF2[59]=1; coeffs2[59]=107;
    
    double sumL = 0.0, sumr = 0.0, sumB = 0.0;
  
    for (int x=0; x<60; x++)
    {
      double f = 1;
      if(Math.abs(fMms[x])==1) 
        f=fE;
      if(Math.abs(fMms[x])==2) 
        f=fE2;
      sumL += f*(coeffs[x]*sind(fD[x]*D+fMms[x]*Msun_mean+fMmm[x]*Mmoon_mean+fF[x]*F));
      sumr += f*(coeffc[x]*cosd(fD[x]*D+fMms[x]*Msun_mean+fMmm[x]*Mmoon_mean+fF[x]*F));
      f = 1;
      if(Math.abs(fMms2[x])==1) 
        f=fE;
      if(Math.abs(fMms2[x])==2) 
        f=fE2;
      sumB += f*(coeffs2[x]*sind(fD2[x]*D+fMms2[x]*Msun_mean+fMmm2[x]*Mmoon_mean+fF2[x]*F));
    }
  
    //Corrections
    sumL = sumL+3958*sind(A1)+1962*sind(Lmoon_mean-F)+318*sind(A2);
    sumB = sumB-2235*sind(Lmoon_mean)+382*sind(A3)+175*sind(A1-F)+175*sind(A1+F)+127*sind(Lmoon_mean-Mmoon_mean)-115*sind(Lmoon_mean+Mmoon_mean);
    
    //Longitude of the moon
    lambdaMm = Lmoon_mean+sumL/1000000;
    lambdaMm = trunc(lambdaMm);
  
    //Latitude of the moon
    betaM = sumB/1000000;
  
    //Distance earth-moon
    dEM = 385000.56+sumr/1000;
  
    //Apparent longitude of the moon
    lambdaMapp = lambdaMm+delta_psi;
  
    //Right ascension of the moon, apparent
    RAmoon = Math.atan2((sind(lambdaMapp)*cosd(eps)-tand(betaM)*sind(eps)),cosd(lambdaMapp))/dtr;
    RAmoon = trunc(RAmoon);
  
    //Siderial hour angle of the moon, apparent
    SHAmoon = 360-RAmoon;
  
    //Declination of the moon 
    DECmoon = Math.asin(sind(betaM)*cosd(eps)+cosd(betaM)*sind(eps)*sind(lambdaMapp))/dtr;
    Dmoon = DECmoon;
  
    //GHA of the moon
    GHAmoon = GHAAtrue-RAmoon;
    GHAmoon = trunc(GHAmoon);
  
    //Horizontal parallax of the moon
    HPmoon = 3600*Math.asin(6378.14/dEM)/dtr;
  
    //Semidiameter of the moon
    SDmoon = 3600*Math.asin(1738/dEM)/dtr;
  
    //Lunar distance of the sun
    LDist = Math.acos(sind(Dmoon)*sind(Dsun)+cosd(Dmoon)*cosd(Dsun)*cosd(RAmoon-RAsun))/dtr;
  
    //Illumination of the moon's disk
    double i = 180-D-6.289*sind(Mmoon_mean)+2.1*sind(Msun_mean)-1.274*sind(2*D-Mmoon_mean)-0.658*sind(2*D)-0.214*sind(2*Mmoon_mean)-0.112*sind(D);
    k = (1+cosd(i))/2.0;
    k = 100*k;
  }

  //Ephemerides of Polaris
  private void processPolaris()
  {
    double RApol0 = 0.0, DECpol0 = 0.0,
           dRApol = 0.0, dDECpol = 0.0,
           RApol1 = 0.0, DECpol1 = 0.0,
           RApol2 = 0.0, DECpol2 = 0.0,
           eps0_2000 = 0.0;
      
    //Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
    RApol0 = 37.95293333;
    DECpol0 = 89.26408889;
    
    //Proper motion per year
    dRApol = 2.98155/3600;
    dDECpol = -0.0152/3600;
    
    //Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
    RApol1 = RApol0+100*TE*dRApol;
    DECpol1 = DECpol0+100*TE*dDECpol;
    
    //Mean obliquity of ecliptic at 2000.0 in degrees
    eps0_2000 = 23.439291111;
    
    //Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
    double lambdapol1 = Math.atan2((sind(RApol1)*cosd(eps0_2000)+tand(DECpol1)*sind(eps0_2000)),cosd(RApol1));
    double betapol1 = Math.asin(sind(DECpol1)*cosd(eps0_2000)-cosd(DECpol1)*sind(eps0_2000)*sind(RApol1));
    
    //Precession
    double eta = (47.0029*TE-0.03302*TE2+0.00006*TE3)*dtr/3600;
    double PI0 = (174.876384-(869.8089*TE+0.03536*TE2)/3600)*dtr;
    double p0 = (5029.0966*TE+1.11113*TE2-0.0000006*TE3)*dtr/3600;

    double A1 = Math.cos(eta)*Math.cos(betapol1)*Math.sin(PI0-lambdapol1)-Math.sin(eta)*Math.sin(betapol1);
    double B1 = Math.cos(betapol1)*Math.cos(PI0-lambdapol1);
    double C1 = Math.cos(eta)*Math.sin(betapol1)+Math.sin(eta)*Math.cos(betapol1)*Math.sin(PI0-lambdapol1); 
    double lambdapol2 = p0+PI0-Math.atan2(A1,B1);
    double betapol2 = Math.asin(C1);  
    
    //nutation in longitude
    lambdapol2 += dtr*delta_psi;
    
    //Aberration
    double kappa = dtr*20.49552/3600;
    double pi0 = dtr*(102.93735+1.71953*TE+0.00046*TE2);
    double e = 0.016708617-0.000042037*TE-0.0000001236*TE2;

    double dlambdapol = (e*kappa*Math.cos(pi0-lambdapol2)-kappa*Math.cos(dtr*Lsun_true-lambdapol2))/Math.cos(betapol2);
    double dbetapol = -kappa*Math.sin(betapol2)*(Math.sin(dtr*Lsun_true-lambdapol2)-e*Math.sin(pi0-lambdapol2));

    lambdapol2 += dlambdapol;
    betapol2 += dbetapol;
      
    //Transformation back to equatorial coordinates in radians
    RApol2 =  Math.atan2((Math.sin(lambdapol2)*cosd(eps)-Math.tan(betapol2)*sind(eps)),Math.cos(lambdapol2));
    DECpol2 = Math.asin(Math.sin(betapol2)*cosd(eps)+Math.cos(betapol2)*sind(eps)*Math.sin(lambdapol2));
    
    //Finals
    GHApol = GHAAtrue-RApol2/dtr;
    GHApol = trunc(GHApol);
    SHApol = 360-RApol2/dtr;
    SHApol = trunc(SHApol);
    DECpol = DECpol2/dtr;    
  }
  
  String quarter = "";
  
  //Calculation of the phase of the moon
  private void processMoonPhase()
  {
    double x = lambdaMapp-lambda;
    x = trunc(x);
    if(x<180) 
      quarter = "(+)";
    if(x>180) 
      quarter = "(-)";
    if(k==100) 
      quarter = "(full)";
    if(k==0) 
      quarter = "(new)";
  }

  //Day of the week
  void processWeekDay()
  {
     JD0h += 1.5;
     int res = (int)(JD0h-7*Math.floor(JD0h/7));
     if (res == 0) DoW = "SUN";
     if (res == 1) DoW = "MON";
     if (res == 2) DoW = "TUE";
     if (res == 3) DoW = "WEN";
     if (res == 4) DoW = "THU";
     if (res == 5) DoW = "FRI";
     if (res == 6) DoW = "SAT";  
  }

  // Data output
  private void setData() // setData
  {	
    // aries
    data.setAR(GHAAtrue);   
    data.setGHAAmean(GHAAmean);
    data.setGHAAtrue(GHAAtrue);
    
    //Polaris
    data.setGHApolaris(GHApol);
    data.setSHApolaris(SHApol);
    data.setDECpolaris(DECpol);
    //Obliquity of Ecliptic
    data.setOoE(eps0);
    data.setTOoE(eps);
    
    //Illumination
    data.setIllum(k);
    data.setQuarter(quarter);
    
    //Lunar Distance of Sun
    data.setLDist(LDist);

    // Sun
    data.setGHAsun(GHAsun);
    data.setSHAsun(SHAsun);
    data.setDECsun(DECsun);
    data.setSDsun(SDsun);
    data.setHPsun(HPsun);
    
    data.setEOT(EOT);
    data.setEoE(EoE);

    data.setDelta_psi(delta_psi);
    data.setDelta_eps(delta_eps);
        
    data.setGHAmoon(GHAmoon);
    data.setSHAmoon(SHAmoon);
    data.setDECmoon(DECmoon);
    data.setSDmoon(SDmoon);
    data.setHPmoon(HPmoon);
    
    data.setJD(JD);
    data.setJDE(JDE);
    
    data.setDoW(DoW);
  }
  
  /**
   * To call to get the result of the computations
   * done by the constructor.
   * 
   * @return astro.data.Data
   * @see astro.data.Data
   */
  public Data getData()
  {
    return data;
  }
}