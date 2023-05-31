public class Pos {
    // 이미지 좌표계에서 특정 위치를 저장하기 위한 클래스
    
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

    public int hashCode(){
        return Util.HashCode(x,y);
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
