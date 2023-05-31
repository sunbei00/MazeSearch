
public class Main {

    public static void main(String[] args) {
        String fileName=  FileChooser.chooseFile();
        if(fileName == null){
            System.out.println("파일을 선택해주세요.");
            System.exit(0);
        }
        Model model = new Model(fileName, "./result.bmp");
        model.fileRead();
        model.buildOur();
    }
}