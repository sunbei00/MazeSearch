import javax.xml.stream.Location;
import java.util.ArrayList;

public class Priority {
    // 각 브랜치 위치 & 각 브랜치에서 현재 위치까지의 거리
    public ArrayList<Define.DestInfo> destInfos;

    public Define.Location location = null;
    public Priority(ArrayList<Define.DestInfo> destInfos){
        this.destInfos = destInfos;
    }

    public Define.Location HighestPriorityBranch() {
        for (Define.DestInfo dest : destInfos) {
            // 현재 위치를 기준으로 각 브랜치까지의 거리
            int destDistance = dest.distance;

            // 브랜치 좌표 값 => 상하좌우 우선순위 계산 위해
            Define.BranchBlock branchBlock = dest.branchBlock;
            int x = branchBlock.x;
            int y = branchBlock.y;

            int maxPriority = -1000000000;


            // branchBlock 을 기준으로 상하좌우 경로가 있을 때 계산
            // branchBlock 의 상하좌우 우선순위 (해당 위치 & 현재 위치부터 branchBlock 까지의 거리)
            if (branchBlock.up.exist == true) {
                branchBlock.up.priority = -10 * Math.min(x, y - 1) - destDistance;
                if (maxPriority < branchBlock.up.priority) {
                    location.x = x;
                    location.y = y - 1;
                }
            } else if (branchBlock.down.exist == true) {
                branchBlock.down.priority = -10 * Math.min(x, y + 1) - destDistance;
                if (maxPriority < branchBlock.down.priority) {
                    location.x = x;
                    location.y = y + 1;
                }
            } else if (branchBlock.right.exist == true) {
                branchBlock.right.priority = -10 * Math.min(x + 1, y) - destDistance;
                if (maxPriority < branchBlock.right.priority) {
                    location.x = x + 1;
                    location.y = y;
                }
            } else if (branchBlock.left.exist == true) {
                branchBlock.left.priority = -10 * Math.min(x - 1, y) - destDistance;
                if (maxPriority < branchBlock.left.priority) {
                    location.x = x - 1;
                    location.y = y;
                }
            }
        }

        // 이전 브랜치로 이동하는 경우
        return location;
    }
}
