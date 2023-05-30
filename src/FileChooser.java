import javax.swing.*;
import java.io.File;

public class FileChooser {

    public static String chooseFile(){

        JFileChooser fileChooser = new JFileChooser();
        JDialog dialog = new JDialog();

        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(dialog);
        if (result == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile().toString();
        else
            return null;

    }
}