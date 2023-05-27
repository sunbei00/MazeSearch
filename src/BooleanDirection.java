public class BooleanDirection {
    public boolean up = false;
    public boolean down = false;
    public boolean left = false;
    public boolean right = false;

    public void setFalse(){
        up = false;
        down = false;
        left = false;
        right = false;
    }
    public boolean check(Define.Direction direction){
        if(direction == Define.Direction.UP)
            return up;
        if(direction == Define.Direction.DOWN)
            return down;
        if(direction == Define.Direction.RIGHT)
            return right;
        if(direction == Define.Direction.LEFT)
            return left;
        return false;
    }
    public void setTrue(Define.Direction direction){
        if(direction == Define.Direction.UP)
            up = true;
        if(direction == Define.Direction.DOWN)
            down = true;
        if(direction == Define.Direction.RIGHT)
            right = true;
        if(direction == Define.Direction.LEFT)
            left = true;
    }
}
