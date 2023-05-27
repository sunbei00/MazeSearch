import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Priority {

    public static class BranchPriority {
        public Model model;
        public Pos location = new Pos();
        public ArrayList<DestInfo> destInfos;
        public DestInfo destResult;
        public int maxPriority; //minValue, 게임 end

        public BranchPriority(Model model, ArrayList<DestInfo> destInfos) {
            this.model = model;
            this.destInfos = destInfos;
        }

        private void updatePriority(BranchBlock branchBlock, Orientation udlr, int priority, int distance, int x, int y, DestInfo destInfo) {
            if (udlr.exist == true && udlr.linkedBranch == null) {
                udlr.priority = -10 * priority - distance;
                if (udlr.priority > maxPriority) {
                    maxPriority = udlr.priority;
                    destResult = destInfo;
                    location.x = x;
                    location.y = y;
                }
            }
        }

        public Pos HighestPriorityBranch() {
            maxPriority = Integer.MIN_VALUE;

            int row = model.getRow();
            int col = model.getCol();

            for (DestInfo dest : destInfos) {
                int destDistance = dest.distance;

                BranchBlock branchBlock = dest.branchBlock;
                int x = Math.min(branchBlock.x, col - branchBlock.x);
                int y = Math.min(branchBlock.y, row - branchBlock.y);

                //Math.min : 벽까지의 최소거리
                //destDistance : 현재 위치에서 브랜치까지의 최소거리
                updatePriority(branchBlock, branchBlock.up, Math.min(x, y - 1), destDistance, x, y - 1,dest);
                updatePriority(branchBlock, branchBlock.down, Math.min(x, y + 1), destDistance, x, y + 1,dest);
                updatePriority(branchBlock, branchBlock.right, Math.min(x + 1, y), destDistance, x + 1, y,dest);
                updatePriority(branchBlock, branchBlock.left, Math.min(x - 1, y), destDistance, x - 1, y,dest);
            }

            return location;
        }
    }


    public static class ScanPriority {
        public Model model;
        HashSet<ScanPoint> scanCenter = new HashSet<>();

        //쥐가 현재 알고있는 Map 정보
        public ArrayList<ArrayList<Block>> our;

        //쥐의 현재 위치, 대칭점 계산
        public Pos location;

        int row = model.getRow();
        int col = model.getCol();


        //현재 쥐의 위치와, 알고있는 Map 정보
        public ScanPriority(Model model, Pos location, ArrayList<ArrayList<Block>> our){
            this.model = model;
            this.location = location;
            this.our = our;
        }


        //이미 스캔한 범위일 경우, unknown 비율 체크
        public int checkUnknown(ScanPoint point) {
            int unknown_count = 0;
            for (Pos p : Define.sacnBoundary) {
                Pos look = new Pos(point.x, point.y); //스캔할 지역의 중심좌표, new 생성

                //스캔 범위
                look.x += p.x;
                look.y += p.y;


                if(our.get(look.y).get(look.x).type != Define.UNKNOWN){ //지금까지 스캔한 값
                    unknown_count++;
                }
            }

            return unknown_count;
        }

        //우선순위 스캔 구역 선정
        public ScanPoint HighestPriorityScan() {
            //각 스캔그리드 중심 좌표, 5n*5n 구역만 고려 //최외각만 따로 그리드 생성하는 방식으로 바꾸기!
            for(int i=2;i<row;i+=5){
                for(int j=2;j<col;j+=5){
                    ScanPoint center = new ScanPoint(i,j, false);
                    scanCenter.add(center);
                }
            }

            /*
            int right = 0;
            for(int i=2;i<col;i+=5){
                right = i;
            }
            int bottom = 0;
            for(int i=2;i<row;i+=5) {
                bottom = i;
            }
             */
            int right = ((col - 2) / 5) * 5 + 2;
            int bottom = ((row - 2) / 5) * 5 +2;


            //왼쪽 행 외곽
            for(int i=2;i<row;i+=5){
                ScanPoint leftcenter = new ScanPoint(2, i, false);
                scanCenter.add(leftcenter);
                ScanPoint rightcenter = new ScanPoint(right, i, false);
                scanCenter.add(rightcenter);
            }

            for(int i=2;i<col;i+=5){
                ScanPoint topcenter = new ScanPoint(i, 2, false);
                scanCenter.add(topcenter);
                ScanPoint bottomcenter = new ScanPoint(i, bottom, false);
                scanCenter.add(bottomcenter);
            }



            //현재 위치를 기준으로 대칭점 구하기
            int symX = row - location.x;
            int symY = col - location.y;

            double maxPriority = Double.MIN_VALUE;
            ScanPoint scanPoint = new ScanPoint(1, 1, false);

            for(ScanPoint point : scanCenter){
                //미스캔 영역
                if(point.visited==false) {
                    int xDiff = point.x - symX;
                    int yDiff = point.y - symY;

                    double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);

                    //대칭점과 가까울수록 우선순위 ↑
                    point.priority -= distance;


                    //직접 가보지 않은 구역으로, unknown 비율이 높은 경우 우선순위 ↑
                    point.priority += checkUnknown(point);
                }

                //스캔 우선순위 계산
                if(maxPriority < point.priority){
                    maxPriority = point.priority;
                    scanPoint = point;
                }
            }

            return scanPoint;
        }
    }
}