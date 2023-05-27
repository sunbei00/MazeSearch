import java.util.ArrayList;

public class DestInfo implements Comparable<DestInfo> {
    public BranchBlock branchBlock = null;
    public int distance;
    public ArrayList<Define.Direction> directions = null;

    public DestInfo(BranchBlock branchBlock, int distance, ArrayList<Define.Direction> directions) {
        this.branchBlock = branchBlock;
        this.distance = distance;
        this.directions = directions;
    }

    public DestInfo(BranchBlock branchBlock, int distance) {
        this.branchBlock = branchBlock;
        this.distance = distance;
    }

    @Override
    public int compareTo(DestInfo o) {

        if (this.distance > o.distance)
            return 1;
        else
            return -1;
    }
}
