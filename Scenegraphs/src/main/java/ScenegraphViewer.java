import javax.swing.*;

/**
 * Created by ashesh on 10/30/2015.
 */
public class ScenegraphViewer
{
    public static void main(String []args)
    {
        String loadin;
        if (args[0].equalsIgnoreCase("ymca")) {
            loadin = "scenegraphmodels/ymca.xml";
        }
        else if (args[0].equalsIgnoreCase("building")) {
            loadin = "scenegraphmodels/building.xml";
        }
        else {
            loadin = "";
        }
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(loadin);
            }
        });
    }

    private static void createAndShowGUI(String loadin)
    {
        JFrame frame = new JOGLFrame("Scene graph Viewer", loadin);
        frame.setVisible(true);
    }
}
