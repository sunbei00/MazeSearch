public class ScanPoint {

    public int x;
    public int y;
    public boolean visited;
    public double priority = 0;

    public ScanPoint(int x, int y, boolean visited) {
        this.x = x;
        this.y = y;
        this.visited = false;
    }


}
