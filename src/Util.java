public class Util {

    // Pos의 x, y로 2개의 변수가 존재하기 때문에 HashMap을 이용하기 위한 HashCode 선언
    public static int HashCode(int x,int y) {
        return x*10000 + y;
    }

    // Index Error 방지를 위한 함수.
    public static void calcIndex(Pos pos,Model model){
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
