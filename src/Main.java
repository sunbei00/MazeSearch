
public class Main {
    public static void main(String[] args) {
        Model model = new Model("./Maze/Maze19.txt", "./result.bmp");
        model.fileRead();
        model.buildOur();
    }
}