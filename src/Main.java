
public class Main {
    public static void main(String[] args) {
        Model model = new Model("./Maze/Maze4.txt", "./result.bmp");
        model.fileRead();
        model.buildOur();
    }
}