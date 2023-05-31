import javax.swing.*;
import java.io.File;

public class FileChooser {

    // 프로그램 실행 시 파일을 선택하기 위한 GUI
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