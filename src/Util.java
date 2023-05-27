public class Util {
    public static int HashCode(int x,int y) {
        return x*10000 + y;
    }

    public static void calcIndex(Pos pos,Model model){ // index error 방지
        if(pos.x < 0)
            pos.x = 0;
        if(pos.x >= model.getCol())
            pos.x = model.getCol() - 1;
        if(pos.y < 0)
            pos.y = 0;
        if(pos.y >= model.getRow())
            pos.y = model.getRow() - 1;
    }
}
