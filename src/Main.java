
public class Main {
    public static void main(String[] args) {
        Model model = new Model("./Maze/Maze12.txt", "./result.bmp");
        model.fileRead();
        model.buildOur();
    }
}