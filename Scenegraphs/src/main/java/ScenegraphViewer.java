import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by ashesh on 10/30/2015.
 */
public class ScenegraphViewer {
    public static String loadin;

    public static void main(String[] args) {
        File file = new File(args[0]);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String token = scanner.next();
                if (token.equalsIgnoreCase("ymca")) {
                    loadin = "scenegraphmodels/ymca.xml";
                } else if (token.equalsIgnoreCase("building")) {
                    //loadin = "scenegraphmodels/two-buildings.xml";
                    loadin = "scenegraphmodels/cone.xml";
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(loadin);
            }
        });
    }

    private static void createAndShowGUI(String loadin) {
        JFrame frame = new JOGLFrame("Scene graph Viewer", loadin);
        frame.setVisible(true);
    }
}