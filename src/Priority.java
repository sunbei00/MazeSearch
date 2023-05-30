import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

public class Priority {
    public static class BranchPriority {
        public Model model;
        public Pos location = new Pos();
        public ArrayList<DestInfo> destInfos;
        public DestInfo destResult;
        public double maxPriority; //minValue, 게임 end
        public Pos goal;
        static int branchCount = 0;
        public LoopUnknownChecker loopUnknownChecker;

        public BranchPriority(Model model, ArrayList<DestInfo> destInfos, Pos goal) {
            this.model = model;
            this.destInfos = destInfos;
            setGoal(goal);
            loopUnknownChecker = new LoopUnknownChecker(model);
        }

        public void setGoal(Pos goal) {this.goal = goal;}

        //벽 우선순위 계산
        //arctan 함수 사용하여, 우선순위 부여
        private double wallPriorityCalculate(double input){
            double rangeStart = 1;
            double rangeEnd = Math.max(model.getRow(), model.getCol()) - 1;

            double mid = (rangeStart + rangeEnd) / 2;
            double range = rangeEnd - rangeStart;
            double a = 10 / range;

            return 100 * Math.atan(a * input-mid);
        }

        private void updatePriority(DestInfo destInfo, Orientation udlr, Define.Direction direction, double goalDistance, int wall_distance, int dest_distance, int x, int y) {
            Pos branchBlockPos = new Pos(destInfo.branchBlock.x, destInfo.branchBlock.y);
            if(udlr.exist == true && udlr.linkedBranch == null){
                if(loopUnknownChecker.isEndLoop(branchBlockPos, direction))
                    udlr.priority = Integer.MIN_VALUE + 1;
                else {
                    double wall_Priority = wallPriorityCalculate(wall_distance);
                    double dest_Priority = dest_distance * dest_distance;
                    double goal_Priority = goalDistance * goalDistance * 100;
                    udlr.priority = -wall_Priority - dest_Priority - goal_Priority;
                }

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
                double goalDistance = 0;

                BranchBlock branchBlock = dest.branchBlock;
                int x = Math.min(branchBlock.x, col - branchBlock.x);
                int y = Math.min(branchBlock.y, row - branchBlock.y);

                if(goal != null) {
                    int xDiff = branchBlock.x - goal.x;
                    int yDiff = branchBlock.y - goal.y;
                    goalDistance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
                }

                //Math.min : 벽까지의 최소거리
                //destDistance : 현재 위치에서 브랜치까지의 최소거리
                updatePriority(dest, branchBlock.up, Define.Direction.UP, goalDistance, Math.min(x, y - 1), destDistance, x, y - 1);
                updatePriority(dest, branchBlock.down, Define.Direction.DOWN, goalDistance, Math.min(x, y + 1), destDistance, x, y + 1);
                updatePriority(dest, branchBlock.right, Define.Direction.RIGHT, goalDistance, Math.min(x + 1, y), destDistance, x + 1, y);
                updatePriority(dest, branchBlock.left, Define.Direction.LEFT, goalDistance, Math.min(x - 1, y), destDistance, x - 1, y);
            }

            return location;
        }
    }


    public static class ScanPriority {
        public Model model;
        HashSet<ScanPoint> scanCenter = new HashSet<>();

        //쥐가 현재 알고있는 Map 정보
        public ArrayList<ArrayList<Block>> our;

        ArrayList<ScanPoint> goalNearCenter = new ArrayList<>();

        //쥐의 현재 위치, 대칭점 계산
        public Pos location;

        int row = 0;
        int col = 0;

        public Pos goal;
        public Pos goalGrid;
        boolean back;
        int scanCounter=0;

        //현재 쥐의 위치와, 알고있는 Map 정보
        public ScanPriority(Model model, Pos location, ArrayList<ArrayList<Block>> our, Pos goal, Pos goalGrid, boolean back){
            this.model = model;
            this.location = location;
            this.our = our;
            row = model.getRow();
            col = model.getCol();
            setGoal(goal);
            setGoalGrid(goalGrid);
            setBack(back);
        }
        public void setGoal(Pos goal) {
            this.goal = goal;
        }

        public void setGoalGrid(Pos goalGrid) {
            this.goalGrid = goalGrid;
            if(goalGrid!=null)
                this.goalNearCenter.add(new ScanPoint(goalGrid.x, goalGrid.y, true));
        }

        public void setBack(boolean back) {
            this.back = back;
        }

        public void createScanGrid(){
            //각 스캔그리드 중심 좌표, 5n*5n 구역만 고려 //최외각만 따로 그리드 생성하는 방식으로 바꾸기!
            //외곽 스캔그리드
//            for (int i = 2; i <= row-3; i += 5) {
//                ScanPoint leftcenter = new ScanPoint(2, i, false);
//                scanCenter.add(leftcenter);
//                ScanPoint rightcenter = new ScanPoint(col-3, i, false);
//                scanCenter.add(rightcenter);
//            }
//
//            for (int i = 2; i <= col-3; i += 5) {
//                ScanPoint topcenter = new ScanPoint(i, 2, false);
//                if (!scanCenter.contains(topcenter)) {  // ScanCenter에 center가 없는 경우만
//                    scanCenter.add(topcenter);  // ScanCenter에 추가
//                }
//                ScanPoint bottomcenter = new ScanPoint(i, row-3, false);
//                if (!scanCenter.contains(bottomcenter)) {  // ScanCenter에 center가 없는 경우만
//                    scanCenter.add(bottomcenter);  // ScanCenter에 추가
//                }
//            }
//
//            ScanPoint outSide = new ScanPoint(col-3, row-3, false);
//            if (!scanCenter.contains(outSide)) {  // ScanCenter에 center가 없는 경우만
//                scanCenter.add(outSide);  // ScanCenter에 추가
//            }
//
//            //내곽 스캔 그리드
//            for (int i = 2; i < col; i += 5) {
//                for (int j = 2; j < row; j += 5) {
//                    ScanPoint center = new ScanPoint(i, j, false);
//                    if (!scanCenter.contains(center)) {  // ScanCenter에 center가 없는 경우만
//                        scanCenter.add(center);  // ScanCenter에 추가
//                    }
//                }
//            }

            for(int i=2;i<=col-3;i+=5){
                for(int j=2;j<=row-3;j+=5){
                    ScanPoint center = new ScanPoint(i, j, false);
                    scanCenter.add(center);
                }
            }

            int remainX = col%5;
            int remainY = row%5;

            if(remainX != 0){
                for(int i=2;i<=row-3;i+=5){
                    ScanPoint center = new ScanPoint(col-3, i, false);
                    scanCenter.add(center);
                }
            }

            if(remainY != 0){
                for(int i=2;i<=col-3;i+=5){
                    ScanPoint center = new ScanPoint(i, row-3, false);
                    scanCenter.add(center);
                }
            }

            if(remainX!=0 && remainY!=0){
                ScanPoint center = new ScanPoint(col-3, row-3, false);
                scanCenter.add(center);
            }
        }

        //이미 스캔한 범위일 경우, unknown 비율 체크
        public int checkUnknown(ScanPoint point) {
            int unknown_count = 0;
            for (Pos p : Define.sacnBoundary) {
                Pos look = new Pos(point.x, point.y); //스캔할 지역의 중심좌표, new 생성

                //스캔 범위
                look.x += p.x;
                look.y += p.y;
                Util.calcIndex(look,model);
                if(our.get(look.y).get(look.x).type == Define.UNKNOWN){ //지금까지 스캔한 값
                    unknown_count++;
                }
            }

            return unknown_count;
        }
        private double unknownPriorityCalculate(double input){
            double rangeStart = 0;
            double rangeEnd = 25;

            double mid = (rangeStart + rangeEnd + 1) / 2;
            double range = rangeEnd - rangeStart;
            double a = 10 / range;
            double k = 100 * Math.atan(0.4 * 12);
            double z = 100 * Math.atan(a * (input-mid));
            return z;
        }

        public void calculatePriority(ScanPoint point, int symX, int symY, int pointX, int pointY) {
            int xDiff = point.x - symX;
            int yDiff = point.y - symY;
            double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);

            int min_x = Math.min(pointX, col-1-pointX); //스캔그리드와 왼쪽, 오른쪽 벽까지의 최소 거리
            int min_y = Math.min(pointY, row-1-pointY); //스캔그리드와 위, 아래 벽까지의 최소 거리

            int wall_distance = Math.min(min_x, min_y); //벽까지의 최소 거리

            //골 o
            //골과 현재 경로 사이의 거리에 따라 distance_Priority 계수 k 조정
            double k = 1;

            //골 x → 대칭점과 가까울수록 외곽 스캔그리드 우선순위 ↑
            //골 o
            //1) 지난 경로 back
            // → 골과 가장 가까운 길을 찾고, 해당 길과 가까운 스캔그리드 우선순위 ↑
            //2) keep going
            // → 골 그리드와 인접하면서, 쥐의 현재 위치와 가까운 스캔그리드 우선순위 ↑
            point.priority = 0;

            double unknown_Priority = unknownPriorityCalculate(checkUnknown(point));
            double distance_Priority = k * (distance);
            double wall_Priority = 1000 * wall_distance;

            point.priority = unknown_Priority - distance_Priority - wall_Priority;

        }
        //우선순위 스캔 구역 선정
        public ScanPoint HighestPriorityScan() {
            double maxPriority = Integer.MIN_VALUE;
            ScanPoint scanPoint = new ScanPoint(-1, -1, false);

            scanCounter++;
            //현재 위치를 기준으로 대칭점 구하기
            int symX = col - location.x;
            int symY = row - location.y;

            System.out.println(symX);
            System.out.println(symY);

            for (ScanPoint point : scanCenter) {
                //미스캔 영역 + 외곽 스캔그리드 탐색
                if (!point.visited) {
                    calculatePriority(point, symX, symY, point.x, point.y);

                    //스캔 우선순위 계산
                    if (maxPriority <= point.priority) {
                        maxPriority = point.priority;
                        scanPoint = point;
                    }
                }
            }
            //스캔그리드 방문
            scanPoint.visited = true;

            System.out.println("x" + scanPoint.x);
            System.out.println("y" + scanPoint.y);

            return scanPoint;
        }
    }
}