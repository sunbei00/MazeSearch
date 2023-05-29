
public class Main {
    public static void main(String[] args) {
        Model model = new Model("./Maze/Maze1.txt", "./result.bmp");
        model.fileRead();
        model.setWritePath("./GroundTruth.bmp");
        model.ImgWrite(Define.ImgOutput.GroundTruth);

        model.buildOur();
    }
}