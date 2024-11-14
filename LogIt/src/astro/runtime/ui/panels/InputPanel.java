package astro.runtime.ui.panels;
import astro.runtime.ui.AppFrame;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import user.util.GeomUtil;

public class InputPanel extends JPanel 
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JTextField LDeg = new JTextField();
  private JLabel jLabel4 = new JLabel();
  private JTextField LMin = new JTextField();
  private JComboBox LSign = new JComboBox();
  private JButton StartStop = new JButton();
  private JComboBox GSign = new JComboBox();
  private JTextField GMin = new JTextField();
  private JTextField GDeg = new JTextField();
  private JLabel jLabel5 = new JLabel();

  AppFrame parent; 
  
  public InputPanel(AppFrame parent)
  {
    this.parent = parent;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

  }

  private void jbInit() throws Exception
  {
    this.setLayout(gridBagLayout1);
    jLabel1.setText("Estimated Position");
    jLabel2.setText("L:");
    jLabel3.setText("G:");
    LDeg.setText("00");
    LDeg.setHorizontalAlignment(JTextField.RIGHT);
    LDeg.setPreferredSize(new Dimension(40, 20));
    jLabel4.setText("°");
    LMin.setText("00.00");
    LMin.setHorizontalAlignment(JTextField.RIGHT);
    LMin.setPreferredSize(new Dimension(50, 20));
    LSign.setPreferredSize(new Dimension(35, 20));
    LSign.addItem("N");
    LSign.addItem("S");
    StartStop.setText("Start");
    StartStop.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          SS_actionPerformed(e);
        }
      });
    GSign.setPreferredSize(new Dimension(35, 20));
    GSign.addItem("E");
    GSign.addItem("W");
    GMin.setText("00.00");
    GMin.setHorizontalAlignment(JTextField.RIGHT);
    GMin.setPreferredSize(new Dimension(50, 20));
    GDeg.setText("000");
    GDeg.setHorizontalAlignment(JTextField.RIGHT);
    GDeg.setPreferredSize(new Dimension(40, 20));
    jLabel5.setText("°");
    jLabel6.setText("Eye height above sea level:");
    jLabel7.setText(" meter(s)");
    eyeHeight.setText("2.0");
    eyeHeight.setPreferredSize(new Dimension(40, 20));
    eyeHeight.setHorizontalAlignment(JTextField.RIGHT);
    this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    this.add(jLabel2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(LDeg, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel4, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(LMin, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(LSign, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(StartStop, new GridBagConstraints(0, 3, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
    this.add(GSign, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(GMin, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(GDeg, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel5, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel6, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel7, new GridBagConstraints(3, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(eyeHeight, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  private boolean running = false;
  private JLabel jLabel6 = new JLabel();
  private JLabel jLabel7 = new JLabel();
  private JTextField eyeHeight = new JTextField();
  
  private void SS_actionPerformed(ActionEvent e)
  {
    running = !running;
    if (running)
    {
      StartStop.setText("Stop");
      parent.go();
    }
    else
    {
      StartStop.setText("Start");
      parent.stop();
    }
  }
  
  public double getL()
  {
    double l = 0.0;
    String d = LDeg.getText();
    String m = LMin.getText();
    l = GeomUtil.SexToDec(d, m);
    String ns = (String)LSign.getSelectedItem();
    if (ns.toUpperCase().equals("S"))
      l *= -1;
    return l;
  }
  
  public double getG()
  {
    double g = 0.0;
    String d = GDeg.getText();
    String m = GMin.getText();
    g = GeomUtil.SexToDec(d, m);
    String ew = (String)GSign.getSelectedItem();
    if (ew.toUpperCase().equals("W"))
      g *= -1;
    return g;
  }
  
  public double getEyeHeight()
  {
    double h = 0.0;
    try
    {
      h = Double.parseDouble(eyeHeight.getText());
    }
    catch (NumberFormatException nfe)
    {
      nfe.printStackTrace();
    }
    return h;
  }
}