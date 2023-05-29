import javax.swing.JFileChooser;

public class FileChooser {
    private static JFileChooser fileComponent = new JFileChooser();
    public static String chooseFile(){
        if(fileComponent.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            return fileComponent.getSelectedFile().toString();
        else
            return null;
    }
}