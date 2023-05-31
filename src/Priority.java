import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

public class Priority {
    public static class BranchPriority {
        public Model model;
        public ArrayList<DestInfo> destInfos; //(BranchBlock, distance) List
        public DestInfo destResult; //HighPriorityDestInfo
        public Pos location = new Pos(); //return_HighPriorityBlock
        public Pos goal;
        public LoopUnknownChecker loopUnknownChecker;
        public double maxPriority; //minValue, Game end

        public BranchPriority(Model model, ArrayList<DestInfo> destInfos, Pos goal) {
            this.model = model;
            this.destInfos = destInfos;
            setGoal(goal);
            loopUnknownChecker = new LoopUnknownChecker(model);
        }

        public void setGoal(Pos goal) {this.goal = goal;}

        //가까운 벽 우선순위 계산
        //arctan 함수 사용하여, 우선순위 부여
        private double wallPriorityCalculate(double input){
            double rangeStart = 1;
            double rangeEnd = Math.max(model.getRow(), model.getCol()) - 1;

            double mid = (rangeStart + rangeEnd) / 2;
            double range = rangeEnd - rangeStart;
            double a = 10 / range;

            return 100 * Math.atan(a * input-mid);
        }

        private void updatePriority(DestInfo destInfo, Orientation udlr, Define.Direction direction, double goalDistance, int wall_distance, int dest_distance) {
            Pos branchBlockPos = new Pos(destInfo.branchBlock.x, destInfo.branchBlock.y);

            //브랜치 상하좌우 길이 있는지 & 갔던 길인지 판단
            if(udlr.exist == true && udlr.linkedBranch == null){
                //닫힌 구역이면, 우선순위 최저
                if(loopUnknownChecker.isEndLoop(branchBlockPos, direction))
                    udlr.priority = Integer.MIN_VALUE + 1;
                else {
                    double goalPriority = goalDistance * goalDistance * 100; //BranchBlock과 골까지의 거리
                    double destPriority = dest_distance * dest_distance; //BranchBlock과 현재 위치와의 거리
                    double wallPriority = wallPriorityCalculate(wall_distance); //BranchBlock과 외벽과의 거리

                    udlr.priority = - goalPriority - destPriority - wallPriority; //우선순위 부여
                }

                if (udlr.priority > maxPriority) {
                    maxPriority = udlr.priority;
                    destResult = destInfo;

                    //반환할 HighestPriority 블럭 좌표
                    location.x = destInfo.branchBlock.x;
                    location.y = destInfo.branchBlock.y;
                }
            }
        }

        //우선순위 이동 블럭 선정
        public Pos HighestPriorityBranch() {
            //maxPriority 초기값
            maxPriority = Integer.MIN_VALUE;

            //맵 row, col
            int row = model.getRow();
            int col = model.getCol();

            //지금까지 탐색한 DestInfo(BranchBlock, 거리) 리스트
            for (DestInfo dest : destInfos) {
                int destDistance = dest.distance; //현재 위치에서 BranchBlock까지의 거리
                BranchBlock branchBlock = dest.branchBlock;

                //goal을 찾았을 때 → BranchBlock에서 goal까지의 거리
                double goalDistance = 0;
                if(goal != null) {
                    int xDiff = branchBlock.x - goal.x;
                    int yDiff = branchBlock.y - goal.y;
                    goalDistance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
                }

                //x, y 좌표와 row, col을 비교해 짧은 거리 찾기
                int x = Math.min(branchBlock.x, col - branchBlock.x);
                int y = Math.min(branchBlock.y, row - branchBlock.y);

                //Math.min : 벽까지의 최소거리
                //destDistance : 현재 위치에서 브랜치까지의 최소거리
                //goalDistance : goal에서 브랜치까지의 거리
                updatePriority(dest, branchBlock.up, Define.Direction.UP, goalDistance, Math.min(x, y - 1), destDistance);
                updatePriority(dest, branchBlock.down, Define.Direction.DOWN, goalDistance, Math.min(x, y + 1), destDistance);
                updatePriority(dest, branchBlock.right, Define.Direction.RIGHT, goalDistance, Math.min(x + 1, y), destDistance);
                updatePriority(dest, branchBlock.left, Define.Direction.LEFT, goalDistance, Math.min(x - 1, y), destDistance);
            }

            //HighestPriority 블럭 좌표
            return location;
        }
    }


    public static class ScanPriority {
        public Model model;
        HashSet<ScanPoint> scanCenter = new HashSet<>(); //그리드
        public ArrayList<ArrayList<Block>> our; //쥐가 현재 알고있는 Map 정보
        ArrayList<ScanPoint> goalNearCenter = new ArrayList<>();

        //쥐의 현재 위치, 대칭점 계산
        public Pos location;

        public Pos goal;
        int row;
        int col;
        boolean back;

        public ScanPriority(Model model, Pos location, ArrayList<ArrayList<Block>> our){
            this.model = model;
            this.location = location;
            this.our = our;
            row = model.getRow();
            col = model.getCol();
        }
        public void createScanGrid(){
            //내곽 스캔그리드
            for(int i=2;i<=col-3;i+=5){
                for(int j=2;j<=row-3;j+=5){
                    ScanPoint center = new ScanPoint(i, j, false);
                    scanCenter.add(center);
                }
            }

            int remainX = col%5;
            int remainY = row%5;

            //col과 row가 5로 나누어떨어지지 않을 때 추가 스캔그리드 add
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

        //맵의 정보를 모르는 곳, unknown 비율 체크
        public int checkUnknown(ScanPoint point) {
            int unknown_count = 0;
            for (Pos p : Define.sacnBoundary) {
                Pos look = new Pos(point.x, point.y); //스캔할 지역의 중심좌표, new 생성

                //스캔 범위
                look.x += p.x;
                look.y += p.y;
                Util.calcIndex(look,model);

                //unknown block count
                if(our.get(look.y).get(look.x).type == Define.UNKNOWN){
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

            return 100 * Math.atan(a * (input-mid));
        }

        public void calculatePriority(ScanPoint point, int symX, int symY) {
            point.priority = 0; //이전까지 scanPriority 초기화

            int xDiff = point.x - symX;
            int yDiff = point.y - symY;
            double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);

            int min_x = Math.min(point.x, col-1-point.x); //스캔그리드와 왼쪽, 오른쪽 외벽까지의 최소 거리
            int min_y = Math.min(point.y, row-1-point.y); //스캔그리드와 위, 아래 외벽까지의 최소 거리
            int wallDistance = Math.min(min_x, min_y); //외벽까지의 최소 거리

            double wallPriority = 1000 * wallDistance;
            double unknownPriority = unknownPriorityCalculate(checkUnknown(point));
            double distancePriority = distance;

            point.priority = unknownPriority - distancePriority - wallPriority;
        }

        //우선순위 스캔 구역 선정
        public ScanPoint HighestPriorityScan() {
            double maxPriority = Integer.MIN_VALUE;
            ScanPoint scanPoint = new ScanPoint(-1, -1, false);

            //현재 위치를 기준으로 대칭점 구하기
            int symX = col - location.x;
            int symY = row - location.y;

            for (ScanPoint point : scanCenter) {
                //미스캔 영역
                if (!point.visited) {
                    calculatePriority(point, symX, symY);

                    //스캔 우선순위 계산
                    if (maxPriority <= point.priority) {
                        maxPriority = point.priority;
                        scanPoint = point;
                    }
                }
            }
            //스캔그리드 방문
            scanPoint.visited = true;

            return scanPoint;
        }
    }
}