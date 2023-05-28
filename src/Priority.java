import java.util.ArrayList;
import java.util.HashSet;

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

        private void updatePriority(DestInfo destInfo, Orientation udlr, int priority, int distance, int x, int y) {
            if (udlr.exist == true && udlr.linkedBranch == null) {
                udlr.priority = -10 * priority - (10*distance);
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
                updatePriority(dest, branchBlock.up, Math.min(x, y - 1), destDistance, x, y - 1);
                updatePriority(dest, branchBlock.down, Math.min(x, y + 1), destDistance, x, y + 1);
                updatePriority(dest, branchBlock.right, Math.min(x + 1, y), destDistance, x + 1, y);
                updatePriority(dest, branchBlock.left, Math.min(x - 1, y), destDistance, x - 1, y);
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

        int row;
        int col;

        public Pos goal;
        public Pos goalGrid;
        boolean back;

        //현재 쥐의 위치와, 알고있는 Map 정보
        public ScanPriority(Model model, Pos location, ArrayList<ArrayList<Block>> our){
            this.model = model;
            this.location = location;
            this.our = our;
            int row = model.getRow();
            int col = model.getCol();
        }
        public ScanPriority(Model model, Pos location, ArrayList<ArrayList<Block>> our, Pos goal, Pos goalGrid, boolean back){
            this(model, location, our);
            this.goal = goal;
            this.goalGrid = goalGrid;
            this.back = back;
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
            point.priority -= distance;

            // 직접 가보지 않은 구역으로, unknown 비율이 높은 경우 우선순위 ↑
            point.priority += checkUnknown(point);
        }

        public void createScanGrid(){
            //각 스캔그리드 중심 좌표, 5n*5n 구역만 고려 //최외각만 따로 그리드 생성하는 방식으로 바꾸기!

            //최외각 스캔그리드 중심 좌표 조절
            int balanceX = 0;
            int balanceY = 0;

            if(row%5!=0) {
                balanceY = row % 5;
            }

            if(col%5!=0) {
                balanceX = col % 5;
            }


            //외곽 스캔그리드
            for (int i = 2; i < row; i += 5) {
                // 마지막 i에서 +plusY를 하는 로직
                if(i+5>=row)
                    i += balanceY;

                ScanPoint leftcenter = new ScanPoint(2, i, false, true);
                scanCenter.add(leftcenter);
                ScanPoint rightcenter = new ScanPoint(col-balanceX, i, false, true);
                scanCenter.add(rightcenter);
            }

            for (int i = 2; i < col; i += 5) {
                // 마지막 i에서 +plusX를 하는 로직
                if(i+5>=col)
                    i+=balanceX;

                ScanPoint topcenter = new ScanPoint(i, 2, false, true);
                scanCenter.add(topcenter);
                ScanPoint bottomcenter = new ScanPoint(i, row-balanceY, false, true);
                scanCenter.add(bottomcenter);
            }

            //내곽 스캔 그리드
            for (int i = 2; i < row; i += 5) {
                for (int j = 2; j < col; j += 5) {
                    ScanPoint center = new ScanPoint(i, j, false, false);
                    if (!scanCenter.contains(center)) {  // ScanCenter에 center가 없는 경우만
                        scanCenter.add(center);  // ScanCenter에 추가
                    }
                }
            }
        }

        //우선순위 스캔 구역 선정
        public ScanPoint HighestPriorityScan() {
            double maxPriority = Double.MIN_VALUE;
            ScanPoint scanPoint = new ScanPoint(-1, -1, false, false);

            //골 위치를 모를 경우
            if (goal == null) {
                //현재 위치를 기준으로 대칭점 구하기
                int symX = row - location.x;
                int symY = col - location.y;

                for (ScanPoint point : scanCenter) {
                    //미스캔 영역 + 외곽 스캔그리드 탐색
                    if (!point.visited && scanPoint.side) {
                        calculatePriority(point, symX, symY);

                        //스캔 우선순위 계산
                        if (maxPriority < point.priority) {
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
                    ArrayList<ScanPoint> goalNearCenter = new ArrayList<>();

                    //골 스캔그리드와 인접한 그리드 찾기
                    for (ScanPoint point : scanCenter) {
                        double minGoalDistance = Double.MAX_VALUE;

                        if (point.visited == false) {
                            int goalDiffX = point.x - goalGrid.x;
                            int goalDiffY = point.y - goalGrid.y;

                            double goalDistance = Math.sqrt(goalDiffX * goalDiffX + goalDiffY * goalDiffY);

                            // 현재 점의 거리가 최소 거리와 같다면 리스트에 추가
                            if (minGoalDistance == goalDistance) {
                                goalNearCenter.add(point);
                            }
                            // 현재 점의 거리가 최소 거리보다 작다면 최소 거리를 갱신하고 리스트를 비우고 현재 점을 추가
                            else if (minGoalDistance > goalDistance) {
                                minGoalDistance = goalDistance;
                                goalNearCenter.clear();
                                goalNearCenter.add(point);
                            }
                        }
                    }

                    //골 스캔그리드와 인접한 그리드 중 (현재위치, unknown) 정보에 따라 우선순위 부여
                    for (ScanPoint point : goalNearCenter) {
                        if(point.visited==false) {
                            calculatePriority(point, location.x, location.y);

                            //스캔 우선순위 계산
                            if (maxPriority < point.priority) {
                                maxPriority = point.priority;
                                scanPoint = point;
                            }
                        }
                    }

                    //골 그리드를 바꿔주기 => 다음 스캔할 때 해당 그리드의 인접 그리드를 탐색하기 위해
                    goalGrid.x = scanPoint.x;
                    goalGrid.y = scanPoint.y;
                }
            }
            //스캔그리드 방문
            scanPoint.visited = true;

            return scanPoint;
        }
    }
}