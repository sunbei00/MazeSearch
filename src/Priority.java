import java.util.ArrayList;

public class Priority {
    public ArrayList<Define.DestInfo> destInfos;
    public Define.Location location = null;
    public Model model;

    public Priority(ArrayList<Define.DestInfo> destInfos){
        this.destInfos = destInfos;
    }

    private void updatePriority(Define.orientation udlr, int priority, int distance, int x, int y) {
        int maxPriority = Integer.MIN_VALUE;

        if (udlr.exist == true && udlr.linkedBranch==null) {
            udlr.priority = -10 * priority - distance;
            if (udlr.priority > maxPriority) {
                location.x = x;
                location.y = y;
            }
        }
    }

    public Define.Location HighestPriorityBranch() {
        int row = model.getRow();
        int col = model.getCol();

        for (Define.DestInfo dest : destInfos) {
            int destDistance = dest.distance;

            Define.BranchBlock branchBlock = dest.branchBlock;
            int x = Math.min(branchBlock.x, col - branchBlock.x);
            int y = Math.min(branchBlock.y, row - branchBlock.y);

            //Math.min : 벽까지의 최소거리
            //destDistance : 현재 위치에서 브랜치까지의 최소거리
            updatePriority(branchBlock.up, Math.min(x, y - 1), destDistance, x, y - 1);
            updatePriority(branchBlock.down, Math.min(x, y + 1), destDistance, x, y + 1);
            updatePriority(branchBlock.right, Math.min(x + 1, y), destDistance,x + 1, y);
            updatePriority(branchBlock.left, Math.min(x - 1, y), destDistance,x - 1, y);
        }

        return location;
    }
}
