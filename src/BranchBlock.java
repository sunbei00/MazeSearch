public class BranchBlock {

    public Orientation up = new Orientation(false,-1, null);
    public Orientation down = new Orientation(false,-1, null);
    public Orientation left = new Orientation(false,-1, null);
    public Orientation right = new Orientation(false,-1, null);
    public int x;
    public int y;
    public BranchBlock(int x,int y){
        this.x = x;
        this.y = y;
    }
    @Override
    public int hashCode() {
        return x*10000 + y;
    }
}
