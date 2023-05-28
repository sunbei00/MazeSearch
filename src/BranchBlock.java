public class BranchBlock {

    public Orientation up = new Orientation(true,-1, null);
    public Orientation down = new Orientation(true,-1, null);
    public Orientation left = new Orientation(true,-1, null);
    public Orientation right = new Orientation(true,-1, null);
    public int x;
    public int y;
    public BranchBlock(int x,int y){
        this.x = x;
        this.y = y;
    }
    @Override
    public int hashCode() {
        return Util.HashCode(x,y);
    }
}
