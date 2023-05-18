import java.util.ArrayList;

public class Game {

    private int energy;
    private float mana;
    private boolean breakItem;
    private Define.Pos breakPos;
    private Model model;
    private Define.Pos playerPos = new Define.Pos();

    private static Define.Pos look = new Define.Pos(); // optimize for memory (Temp)

    public boolean isEnergy(){
        if(this.energy > 0)
            return true;
        return false;
    }
    private void decreaseEnergy(){
        if(this.energy <= 0)
            return;
        this.energy--;
    }
    private void increaseMana(){
        this.mana += 0.1f;
        if(this.mana >= 3.0f)
            this.mana = 3.0f;
    }
    public boolean isMana(){
        if(this.mana == 3.0f)
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

        this.playerPos.x = 1;
        this.playerPos.y = 0;
        this.model.our.get(this.playerPos.y).set(this.playerPos.x, new Define.Block(Define.PLAYER));
        lookAround();
        Define.branchBlockHashMap.put(Define.hashCode(1,0), new Define.BranchBlock(1,0));
    }

    private void calcIndex(Define.Pos pos){ // index error 방지
        if(pos.x < 0)
            pos.x = 0;
        if(pos.x >= this.model.getCol())
            pos.x = this.model.getCol() - 1;
        if(pos.y < 0)
            pos.y = 0;
        if(pos.y >= this.model.getRow())
            pos.y = this.model.getRow() - 1;
    }

    private void lookAround(){
        for(Define.Pos p : Define.boundary){
            look.setValue(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if(this.model.our.get(look.y).get(look.x).type != Define.UNKNOWN) // 이미 알고있으면 계산x
                continue;
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.WALL) // 벽 표시
                this.model.our.get(look.y).set(look.x, new Define.Block(Define.WALL));
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR || this.model.groundTruth.get(look.y).get(look.x).type == Define.BREAK){
                this.model.our.get(look.y).set(look.x, new Define.Block(Define.AIR));
            }
        }
    }

    public void Move(){
        if(!isEnergy()){
            // GAME OVER
            // FILE WRITE
            // EXIT
            System.exit(0);
        }

        for(Define.Pos p : Define.boundary){
            look.setValue(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if(this.model.our.get(look.y).get(look.x).type == Define.GOING)
                posArrayList.add(look);
        }

        if(posArrayList.size() == 0){
        // 주변에 GOING이 없으면 priority 계산 후 GOING 길 만들기
        } else {
            decreaseEnergy();
            increaseMana();
            // GOING 위치로 으로 이동
            this.model.our.get(this.playerPos.y).set(this.playerPos.x,new Define.Block(Define.AIR));
            this.playerPos = posArrayList.get(0);
            this.model.our.get(this.playerPos.y).set(this.playerPos.x,new Define.Block(Define.PLAYER));
            lookAround();
        }
    }

    public boolean useScan(Define.Pos pos){
        if(!isMana())
            return false;
        mana = 0.0f;

        for(Define.Pos p : Define.sacnBoundary){
            Define.Pos look = new Define.Pos(pos.x, pos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if(this.model.our.get(look.y).get(look.x).type != Define.UNKNOWN) // 이미 알고있으면 계산x
                continue;
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.WALL) // 벽 표시
                this.model.our.get(look.y).set(look.x, new Define.Block(Define.WALL));
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR) // 길 표시
                this.model.our.get(look.y).set(look.x, new Define.Block(Define.AIR));

            if(look.x == 0 || look.x == this.model.getCol() - 1 || look.y == 0 || look.y == this.model.getRow() -1)
                if(!(look.x == 1 && look.y == 0))  // 출발점을 제외한 벽의 양 끝에 길이 있으면 목적지
                    if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR)
                        this.model.our.get(look.y).set(look.x, new Define.Block(Define.GOAL));
        }
        return true;
    }
    public boolean useBreak(Define.Pos pos){
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

        this.model.groundTruth.get(pos.y).set(pos.x, new Define.Block(Define.BREAK)); // GroundTruth에 벽을 부순 위치 표시
        for(Define.Pos p : Define.boundary) { // lookAround 재계산을 위해서 주변 AIR를 제외한 것들을 UNKNOWN으로 변경
            Define.Pos look = new Define.Pos(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if(this.model.our.get(look.y).get(look.x).type != Define.AIR)
                this.model.our.get(look.y).set(look.x, new Define.Block(Define.UNKNOWN));
        }
        lookAround();
        this.breakItem = false;
        breakPos = pos;
        return true;
    }

}
