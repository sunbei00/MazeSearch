public class Main {
    public static void main(String[] args) {
        Model model = new Model("./Maze/Maze1.txt", "./result.bmp");
        model.fileRead();
        model.buildOur();

        model.setWritePath("GroundTruth.bmp");
        model.ImgWrite(Define.ImgOutput.GroundTruth);
        model.setWritePath("Our.bmp");
        model.ImgWrite(Define.ImgOutput.Our);
    }
}