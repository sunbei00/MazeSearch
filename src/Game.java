import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Stack;

public class Game {
    private int energy; // 남은 체력
    private float mana; // 현재 마나
    private boolean breakItem; // breakItem 사용 여부
    public Pos breakPos; // breakItem을 사용한 위치
    private Model model;
    public Pos playerPos = new Pos(); // 플레이어의 위치
    public Pos prevPos = new Pos(); // 플레이어의 이동을 위해 이전에 이동한 위치
    public Pos goal = null; // 목적지의 위치
    public BranchBlockGraph branchBlockGraph; // Branch Block에서 Branch Block으로 이동하기 위해 Branch Block 간의 그래프

    // 스캔을 사용하여 골을 찾았을시 현재 위치를 Branch Block으로 만들어야지 이동할 수 있기 때문에 
    // 현재 위치가 Branch Block이 아니어도 임의로 추가하기 위한 정보 저장
    // 외에도 Break Item을 사용했을 때 Branch Graph의 문제를 해결하는데도 사용
    public ArrayList<Pos> addBranchBlockPos = new ArrayList<>(); 

    // goal을 찾고 체력의 1%를 소요할 때마다 BreakItem 사용 여부 판단
    private int usingItemFrequency = 101;
    
    // 에너지가 80% 이하로 떨어질 경우 갇혔는지 판단하는데 사용
    private int checkEnd = 80;

    // 스캔 우선순위 계산을 위해서
    private Priority.ScanPriority scanPriority;

    public int getEnergy() {
        return energy;
    }
    public boolean isEnergy(){
        if(this.energy > 0)
            return true;
        return false;
    }
    public void checkIsEndEnergy(){
        if(!isEnergy()){
            model.printImageSet(getEnergy(),playerPos,breakPos,goal,false);
            System.out.println("GameOver : !isEnergy");
            System.exit(0);
        }
    }
    public void decreaseEnergy(){
        if(this.energy <= 0)
            return;
        this.energy--;
    }
    public void increaseMana() {
        this.mana += 0.1f;
        if(this.mana >= 3.0f)
            this.mana = 3.0f;
    }
    public boolean isMana(){
        if(Math.abs(this.mana - 3.0f) <= 10e-3) // float error
            return true;
        return false;
    }
    public boolean isBreakItem(){
        return this.breakItem;
    }
    public Game(Model model){
        this.model = model;
        this.energy = model.getCol() * model.getRow() * 2;
        this.mana = 3.0f;
        this.breakItem = true;

        this.playerPos.setValue(1,0);
        this.model.our.get(this.playerPos.y).get(this.playerPos.x).type = Define.PLAYER;
        MapUtil.lookAround(playerPos, model);
        prevPos.setValue(1,0);
        branchBlockGraph = new BranchBlockGraph(model);
        scanPriority = new Priority.ScanPriority(model, playerPos, model.our);
        scanPriority.createScanGrid();
        if(model.our.get(1).get(1).type == Define.WALL)
            useBreak(new Pos(1,1));
    }

    // Branch Block에서 Branch Block으로 이동할 때 우선순위 계산 후 해당 경로로 이동
    private void calculatePriorityAndMove(){

        branchBlockGraph.clear();
        model.our.get(playerPos.y).get(playerPos.x).type = Define.AIR; // for build graph
        branchBlockGraph.checkBranchBlock();
        for(Pos BranchBlockPos : addBranchBlockPos)
            branchBlockGraph.addHashMap(BranchBlockPos);
        BranchBlock head = branchBlockGraph.buildGraph();
        model.our.get(playerPos.y).get(playerPos.x).type = Define.PLAYER;

        // Branch 우선 순위 계산 및 경로로 이동
        Route route = new Route(branchBlockGraph.branchBlockHashMap);
        route.setList();
        ArrayList<DestInfo> destInfos = route.dijkstra(branchBlockGraph.branchBlockHashMap.get(playerPos.hashCode()));
        // 어느 방향으로 이동했는지에 대해서도 저장을 해야한다,

        Priority.BranchPriority priority = new Priority.BranchPriority(model, destInfos, goal);
        Pos dest = priority.HighestPriorityBranch();

        if(priority.maxPriority == Integer.MIN_VALUE){
            // Game Over
            System.out.println("GameOver s: 이동할 수 있는 맵이 미존재");
            model.printImageSet(getEnergy(),playerPos,breakPos,goal,false);
            System.exit(0);
        }

        ArrayList<Define.Direction> Directions = priority.destResult.directions;

        if(goal != null)
            for(DestInfo destInfo : destInfos)
                if(goal.x == destInfo.branchBlock.x && goal.y == destInfo.branchBlock.y)
                    Directions = destInfo.directions;

        for (Define.Direction direction : Directions) {
            int distance = 0;
            if(direction == Define.Direction.UP)
                distance = branchBlockGraph.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).up.distance - 1;
            if(direction == Define.Direction.DOWN)
                distance = branchBlockGraph.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).down.distance - 1;
            if(direction == Define.Direction.LEFT)
                distance = branchBlockGraph.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).left.distance - 1;
            if(direction == Define.Direction.RIGHT)
                distance = branchBlockGraph.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).right.distance - 1;

            checkIsEndEnergy();
            decreaseEnergy();
            increaseMana();
            MapUtil.moveDirection(playerPos, direction, model);
            MapUtil.applyMove(playerPos, prevPos, model);
            MapUtil.checkFinish(playerPos, breakPos, goal,energy ,model);
            MapUtil.lookAround(playerPos, model);
            for(int i=0; i < distance; i++){
                if(useScanWithScanPriority())
                    return;
                checkIsEndEnergy();
                decreaseEnergy();
                increaseMana();
                MapUtil.moveAround(playerPos, prevPos, model);
                MapUtil.applyMove(playerPos, prevPos, model);
                MapUtil.checkFinish(playerPos, breakPos, goal ,energy ,model);
                MapUtil.lookAround(playerPos, model);
            }
        }

        MapUtil.lookAround(playerPos,model);

        Define.Direction direction = MapUtil.getDirection(playerPos, dest);
        if(MapUtil.isAir(playerPos,direction,model)){
            checkIsEndEnergy();
            decreaseEnergy();
            increaseMana();
            // left
            if(direction == Define.Direction.LEFT){
                MapUtil.moveDirection(playerPos,Define.Direction.LEFT, model);
                MapUtil.applyMove(playerPos, prevPos, model);
            }
            // right
            if(direction == Define.Direction.RIGHT){
                MapUtil.moveDirection(playerPos,Define.Direction.RIGHT, model);
                MapUtil.applyMove(playerPos, prevPos, model);
            }
            // down
            if(direction == Define.Direction.DOWN){
                MapUtil.moveDirection(playerPos,Define.Direction.DOWN, model);
                MapUtil.applyMove(playerPos, prevPos, model);
            }
            // up
            if(direction == Define.Direction.UP){
                MapUtil.moveDirection(playerPos,Define.Direction.UP, model);
                MapUtil.applyMove(playerPos, prevPos, model);
            }
            if(direction == Define.Direction.UNKNOWN) // Error Check
                System.out.println("direction Unkwon Error");
            MapUtil.lookAround(playerPos, model);
            MapUtil.checkFinish(playerPos ,breakPos, goal ,energy ,model);
        }
    }

    // Branch Block이 아니라는 가정이 들어간다. (move boundary에 이동할 수 있는 경로가 2개라는 가정)
    // 현재 위치에서 이동할 수 있는 경로 중 이전에 이동한 경로가 아닌 곳으로 이동한다.
    public void Move(){
        MapUtil.checkFinish(playerPos, breakPos, goal ,energy ,model);
        Pos goalPos = MapUtil.CheckFindGoal(goal, model); // goal 변수에 넣어주기위해서 목적지를 찾았는지 확인하는 과정
        if(goalPos != null)
            goal = goalPos;


        checkIsEndEnergy();
        decreaseEnergy();
        increaseMana();

        MapUtil.moveAround(playerPos, prevPos, model);
        MapUtil.applyMove(playerPos, prevPos, model);
        MapUtil.lookAround(playerPos, model);

        if(MapUtil.isBranchBlock(playerPos, model))
            calculatePriorityAndMove();

        MapUtil.lookAround(playerPos, model);
        useScanWithScanPriority();


        if(goal != null && isBreakItem() && (int)Math.floor(100*(double)getEnergy()/(double)(model.getCol()*model.getRow()*2)) != usingItemFrequency){
            if(!BreakItemUtil.isGoodBreak(goal, this, model)){
                usingItemFrequency = (int)Math.floor(100*(double)getEnergy()/(double)(model.getCol()*model.getRow()*2));
                calculatePriorityAndMove();
            }
        }
        if( (int)Math.floor(100*(double)getEnergy()/(double)(model.getCol()*model.getRow()*2)) <= checkEnd  && (int)Math.floor(100*(double)getEnergy()/(double)(model.getCol()*model.getRow()*2)) != checkEnd){
            checkEnd = (int)Math.floor(100*(double)getEnergy()/(double)(model.getCol()*model.getRow()*2));
            if(MapUtil.cantFindOut(model)){
                // Game Over
                System.out.println("GameOver : 이동할 수 있는 맵이 미존재");
                model.printImageSet(getEnergy(),playerPos,breakPos,goal,false);
                System.exit(0);
            }
        }
    }

    // 맵을 그리드로 나누고 그리드 중 우선순위가 높은 지역에 scan을 한다.
    public boolean useScanWithScanPriority(){
        if(isMana()){
            // 스캔 우선 순위 계산
            boolean isEmptyGoal = goal == null? true : false;
            mana = 0.f;
            ScanPoint scanPoint = scanPriority.HighestPriorityScan();
            useScan(scanPoint.x,scanPoint.y);
            if(isEmptyGoal && goal != null){
                addBranchBlockPos.add(playerPos);
                return true;
            }
        }
        return false;
    }

    // 스캔을 진행한다.
    public boolean useScan(int x, int y){
        Pos look = new Pos();
        for(Pos p : Define.sacnBoundary){
            look.setValue(x,y);
            look.x += p.x;
            look.y += p.y;
            Util.calcIndex(look,model);
            if(this.model.our.get(look.y).get(look.x).type != Define.UNKNOWN) // 이미 알고있으면 계산x
                continue;
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.WALL) // 벽 표시
                this.model.our.get(look.y).get(look.x).type = Define.WALL;
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR) // 길 표시
                this.model.our.get(look.y).get(look.x).type = Define.AIR;

            if(look.x == 0 || look.x == this.model.getCol() - 1 || look.y == 0 || look.y == this.model.getRow() -1)
                if(!(look.x == 1 && look.y == 0))  // 출발점을 제외한 벽의 양 끝에 길이 있으면 목적지
                    if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR)
                        if(goal == null)
                            goal = new Pos(look.x, look.y);

        }
        return true;
    }

    // break Item을 사용한다.
    public boolean useBreak(Pos pos){
        if(!isBreakItem())
            return false;
        int magnitude = Math.abs(this.playerPos.x - pos.x) + Math.abs(this.playerPos.y - pos.y);
        if(magnitude != 1) // 주변에 있는 칸이 아닌 경우 리턴 false
            return false;
        if(pos.x <= 0 || pos.x >= this.model.getCol() - 1)  // 양끝의 벽은 부실 수 없음
            return false;
        if(pos.y <= 0 || pos.y >= this.model.getRow() - 1) // 양끝의 벽은 부실 수 없음
            return false;
        if(this.model.groundTruth.get(pos.y).get(pos.x).type != Define.WALL) // 부시려는 것이 벽이 아닐 경우
            return false;

        System.out.println("Using Break Item");
        this.model.groundTruth.get(pos.y).get(pos.x).type = Define.BREAK; // GroundTruth에 벽을 부순 위치 표시
        this.model.our.get(pos.y).get(pos.x).type = Define.AIR; // GroundTruth에 벽을 부순 위치 표시
        MapUtil.lookAround(playerPos, model);
        this.breakItem = false;
        breakPos = pos;
        return true;
    }

}
