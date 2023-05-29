import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Stack;

public class Game {
    public ArrayList<Pos> addBranchBlockPos = new ArrayList<>();
    private int energy;
    private float mana;
    private boolean breakItem;
    public Pos breakPos;
    private Model model;
    public Pos playerPos = new Pos();
    public Pos prevPos = new Pos(); // Temp for move
    public Pos goal = null;
    public BranchBlockGraph branchBlockGraph;

    private boolean isFindGoal=false;
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
            model.printImageSet(getEnergy(),playerPos,breakPos);
            System.out.println("GameOver : !isEnergy");
            System.exit(0);
        }
    }
    public void decreaseEnergy(){
        if(this.energy <= 0)
            return;
        this.energy--;
    }
    public void increaseMana(){
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
        scanPriority = new Priority.ScanPriority(model, playerPos, model.our, goal, null, false);
        scanPriority.createScanGrid();
    }

    private void calculatePriorityAndMove(){

        branchBlockGraph.clear();
        model.our.get(playerPos.y).get(playerPos.x).type = Define.AIR; // for build graph
        branchBlockGraph.checkBranchBlock();
        for(Pos BranchBlockPos : addBranchBlockPos)
            branchBlockGraph.addHashMap(BranchBlockPos);
        BranchBlock head = branchBlockGraph.buildGraph();
        model.our.get(playerPos.y).get(playerPos.x).type = Define.PLAYER;

        // Branch 우선 순위 계산 및 경로로 이동
        Route route = new Route(head, playerPos, branchBlockGraph.branchBlockHashMap);
        route.SetList();
        ArrayList<DestInfo> destInfos = route.Dijkstra(branchBlockGraph.branchBlockHashMap.get(playerPos.hashCode()));
        // 어느 방향으로 이동했는지에 대해서도 저장을 해야한다,

        Priority.BranchPriority priority = new Priority.BranchPriority(model, destInfos, goal);
        Pos dest = priority.HighestPriorityBranch();

        if(priority.maxPriority == Integer.MIN_VALUE){
            // Game Over
            System.out.println("GameOver : 이동할 수 있는 맵이 미존재");
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
            MapUtil.checkFinish(playerPos, breakPos,energy ,model);
            MapUtil.lookAround(playerPos, model);
            for(int i=0; i < distance; i++){
                if(useScanWithScanPriority())
                    return;
                checkIsEndEnergy();
                decreaseEnergy();
                increaseMana();
                MapUtil.moveAround(playerPos, prevPos, model);
                MapUtil.applyMove(playerPos, prevPos, model);
                MapUtil.checkFinish(playerPos, breakPos ,energy ,model);
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
            MapUtil.checkFinish(playerPos ,breakPos ,energy ,model);
        }
    }
    public void Move(){
        // GAME OVER : 우선순위 계산 할 Branch가 미존재 할 시 (우선순위에서 계산해야 할 듯)
        MapUtil.checkFinish(playerPos, breakPos ,energy ,model);
        Pos goalPos = MapUtil.CheckFindGoal(goal, model); // goal 변수에 넣어주기위해서 목적지를 찾았는지 확인하는 과정
        if(goalPos != null){
            goal = goalPos;
            scanPriority.setGoal(goal);
        }

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


        /*
        if(goal != null && isBreakItem() && MapUtil.isBranchBlock(playerPos, model)){
            if(!BreakItemUtil.isGoodBreak(goal, this, model))
                calculatePriorityAndMove();
        }
         */

        if(isFindGoal && isBreakItem()){
            isFindGoal = false;
            if(!BreakItemUtil.isGoodBreak(goal, this, model))
                calculatePriorityAndMove();
        }

    }

    public boolean useScanWithScanPriority(){
        if(isMana()){
            // 스캔 우선 순위 계산
            boolean isEmptyGoal = goal == null? true : false;
            mana = 0.f;
            ScanPoint scanPoint = scanPriority.HighestPriorityScan();
            useScan(scanPoint.x,scanPoint.y);
            if(isEmptyGoal && goal != null){
                addBranchBlockPos.add(playerPos);
                isFindGoal = true;
                return true;
            }
        }
        return false;
    }
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
                    if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR){
                        if(goal == null)
                            goal = new Pos(look.x, look.y);
                        scanPriority.setGoal(goal);
                        scanPriority.setGoalGrid(new Pos(x,y));
                        scanPriority.setBack(false);
                    }
        }
        return true;
    }
    public boolean useBreak(Pos pos){
        if(!isBreakItem())
            return false;
        int magnitude = Math.abs(this.playerPos.x - pos.x) + Math.abs(this.playerPos.y - pos.y);
        if(magnitude != 1) // 주변에 있는 칸이 아닌 경우 리턴 false
            return false;
        if(pos.x <= 0 || pos.x >= this.model.getCol() - 1)  // 양끝의 벽은 부실 수 없음
            return false;
        if(pos.y <= 0 || pos.x >= this.model.getRow() - 1) // 양끝의 벽은 부실 수 없음
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
