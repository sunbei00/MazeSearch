import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ScanPoint scanPoint = (ScanPoint) o;
        return x == scanPoint.x && y == scanPoint.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}