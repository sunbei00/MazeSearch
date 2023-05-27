public class ScanPoint {

    public int x;
    public int y;
    public boolean visited;
    public boolean side;
    public double priority = 0;

    public ScanPoint(int x, int y, boolean visited, boolean side) {
        this.x = x;
        this.y = y;
        this.visited = false;
        this.side = side;
    }
}