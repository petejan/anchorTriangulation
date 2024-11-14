package astro.runtime.ui.panels;
import astro.runtime.ui.AppFrame;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;

public class DatePanel extends JPanel 
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel dateLabel = new JLabel();

  public DatePanel(AppFrame parent)
  {
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
    dateLabel.setText("xx-Mon-yyyy hh:mi:ss UT");
    dateLabel.setToolTipText("Current UT Date & Time");
    dateLabel.setFont(new Font("Tahoma", 1, 14));
    this.add(dateLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  public void setText(String str)
  {
    dateLabel.setText(str);
  }
}