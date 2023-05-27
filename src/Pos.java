public class Pos {
    public int x;
    public int y;

    public Pos(int x, int y){
        this.x = x;
        this.y = y;
    }
    public Pos(){
        this.x = 1;
        this.y = 0;
    }
    public void setValue(int x, int y){
        this.x = x;
        this.y = y;
    }

    public boolean isEquals(Pos pos){
        if(x != pos.x || y != pos.y)
            return false;
        return true;
    }
}
