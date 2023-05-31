public class BooleanDirection {
    // BranchBlcokGraph 클래스에서 사용하는 동서남북 방향으로 이미 지나갔는지 확인하기 위한 클래스
    
    public boolean up = false;
    public boolean down = false;
    public boolean left = false;
    public boolean right = false;

    // false로 모두 초기화
    public void setFalse(){
        up = false;
        down = false;
        left = false;
        right = false;
    }

    // Define.Direction Enum 변수를 받아와 해당 방향을 boolean 값 리턴
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


    // true로 모두 초기화
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
