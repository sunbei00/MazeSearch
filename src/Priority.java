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
            double range = rangeEnd - mid;
            double a = 0.001 * range;

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
                this.goalNearCenter.add(new ScanPoint(goalGrid.x, goalGrid.y, true, true));
        }

        public void setBack(boolean back) {
            this.back = back;
        }

        public void createScanGrid(){
            //각 스캔그리드 중심 좌표, 5n*5n 구역만 고려 //최외각만 따로 그리드 생성하는 방식으로 바꾸기!
            //외곽 스캔그리드
            for (int i = 2; i <= row-3; i += 5) {
                ScanPoint leftcenter = new ScanPoint(2, i, false, true);
                scanCenter.add(leftcenter);
                ScanPoint rightcenter = new ScanPoint(col-3, i, false, true);
                scanCenter.add(rightcenter);
            }

            for (int i = 2; i <= col-3; i += 5) {
                ScanPoint topcenter = new ScanPoint(i, 2, false, true);
                if (!scanCenter.contains(topcenter)) {  // ScanCenter에 center가 없는 경우만
                    scanCenter.add(topcenter);  // ScanCenter에 추가
                }
                ScanPoint bottomcenter = new ScanPoint(i, row-3, false, true);
                if (!scanCenter.contains(bottomcenter)) {  // ScanCenter에 center가 없는 경우만
                    scanCenter.add(bottomcenter);  // ScanCenter에 추가
                }
            }

            ScanPoint outSide = new ScanPoint(col-3, row-3, false, true);
            if (!scanCenter.contains(outSide)) {  // ScanCenter에 center가 없는 경우만
                scanCenter.add(outSide);  // ScanCenter에 추가
            }

            //내곽 스캔 그리드
            for (int i = 2; i < col; i += 5) {
                for (int j = 2; j < row; j += 5) {
                    ScanPoint center = new ScanPoint(i, j, false, false);
                    if (!scanCenter.contains(center)) {  // ScanCenter에 center가 없는 경우만
                        scanCenter.add(center);  // ScanCenter에 추가
                    }
                }
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
            double range = rangeEnd - mid;
            double a = 0.001 * range;

            return 100 * Math.atan(a * input-mid);
        }

        public void calculatePriority(ScanPoint point, int symX, int symY) {
            int xDiff = point.x - symX;
            int yDiff = point.y - symY;

            double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);

            //골 x → 대칭점과 가까울수록 외곽 스캔그리드 우선순위 ↑
            //골 o
            //1) 지난 경로 back
            // → 골과 가장 가까운 길을 찾고, 해당 길과 가까운 스캔그리드 우선순위 ↑
            //2) keep going
            // → 골 그리드와 인접하면서, 쥐의 현재 위치와 가까운 스캔그리드 우선순위 ↑
            point.priority = 0;

            double unknown_Priority = unknownPriorityCalculate(checkUnknown(point));
            double distance_Priority = distance*distance;
//            point.priority -= distance;
//
//            // 직접 가보지 않은 구역으로, unknown 비율이 높은 경우 우선순위 ↑
//            point.priority += 20*Math.sqrt(checkUnknown(point));

            point.priority = unknown_Priority - distance_Priority;

        }
        //우선순위 스캔 구역 선정
        public ScanPoint HighestPriorityScan() {
            double maxPriority = Integer.MIN_VALUE;
            ScanPoint scanPoint = new ScanPoint(-1, -1, false, false);

            //골 위치를 모를 경우
            if (goal == null) {
                scanCounter++;
                //현재 위치를 기준으로 대칭점 구하기
                int symX = col - location.x;
                int symY = row - location.y;

                for (ScanPoint point : scanCenter) {
                    //미스캔 영역 + 외곽 스캔그리드 탐색
                    if (!point.visited && point.side) {
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

            //골 위치 파악
            else {
                //지난 경로로 돌아가는 경우
                //→ 골과 가장 가까운 길을 찾고, 해당 길과 가까운 스캔그리드를 높은 우선순위 부여
                if (back) {
                    //골과 가까운 길 찾기
                    int minMapX = 0;
                    int minMapY = 0;
                    double minDistance = Double.MAX_VALUE;

                    //시간 오래 소요
                    for (int i = 0; i < row; ++i) {
                        for (int j = 0; j < col; ++j) {
                            //길인 경우
                            if (our.get(i).get(j).type == Define.AIR) {
                                double distance = Math.sqrt(Math.pow(goal.x - i, 2) + Math.pow(goal.y - j, 2));
                                if (minDistance > distance) {
                                    minMapX = i;
                                    minMapY = j;
                                }
                            }
                        }
                    }

                    //골과 가까운 길과 스캔그리드와의 우선순위 계산을 통해 스캔 구역 선정
                    for (ScanPoint point : scanCenter) {
                        //미스캔 영역
                        if (!point.visited) {
                            calculatePriority(point, minMapX, minMapY);

                            //스캔 우선순위 계산
                            if (maxPriority < point.priority) {
                                maxPriority = point.priority;
                                scanPoint = point;
                            }
                        }
                    }
                }

                //계속 앞으로 탐색
                //→ 골 스캔그리드와 인접한 그리드를 찾고, 현재 쥐의 위치를 고려하여 스캔그리드 선정
                else {
                    ArrayList<ScanPoint> tempPoints = new ArrayList<>();
                    //골 스캔그리드와 인접한 그리드 찾기
                    for (ScanPoint point : scanCenter) {
                        if (point.visited == false) {
                            //goalNearCenter의 그리드와 인접한 스캔그리드 add

                            for(ScanPoint near : goalNearCenter){
                                int goalDiffX = point.x - near.x;
                                int goalDiffY = point.y - near.y;

                                double goalDistance = Math.sqrt(goalDiffX * goalDiffX + goalDiffY * goalDiffY);

                                // 현재 점의 거리가 최소 거리와 같다면 리스트에 추가
                                if (goalDistance <= 5.0) {
                                    if (!tempPoints.contains(point)) {  // ScanCenter에 center가 없는 경우만
                                        tempPoints.add(point);  // ScanCenter에 추가
                                    }
                                }
                            }
                        }
                    }
                    goalNearCenter.addAll(tempPoints);

                    //골 스캔그리드와 인접한 그리드 중 (현재위치, unknown) 정보에 따라 우선순위 부여
                    for (ScanPoint point : goalNearCenter) {
                        if (!point.visited) {
                            calculatePriority(point, location.x, location.y);

                            //스캔 우선순위 계산
                            if (maxPriority < point.priority) {
                                maxPriority = point.priority;
                                scanPoint = point;
                            }
                        }
                    }

                }
            }
            //스캔그리드 방문
            scanPoint.visited = true;
            goalNearCenter.removeIf(p->!p.visited);

            return scanPoint;
        }
    }
}