import java.util.ArrayList;
import java.util.Stack;

public class LoopUnknownChecker {

    private Model model;
    private ArrayList<ArrayList<Boolean>> graphMap = new ArrayList<ArrayList<Boolean>>();
    private Stack<Pos> checkUnknown = new Stack<>();
    private Pool.PosPool posPool = new Pool.PosPool();
    private static Pos look = new Pos(); // optimize for memory (Temp)
    private static Pos movePos = new Pos(); // optimize for memory (Temp)


    public LoopUnknownChecker(Model model){
        this.model = model;
        int row = model.getRow();
        int col = model.getCol();
        for(int i=0;i<row;i++)
            graphMap.add(new ArrayList<>());
        for(int i=0; i<row; i++)
            for(int j=0; j<col; j++)
                graphMap.get(i).add(false);
    }

    private void clearGraphMap(){
        int row = model.getRow();
        int col = model.getCol();
        for(int i=0; i<row; i++)
            for(int j=0; j<col; j++)
                graphMap.get(i).set(j, false);
    }

    public static Pos DirectionPosition(Pos playerPos, Define.Direction direction, Model model) {
        look.setValue(playerPos.x, playerPos.y);
        if(direction == Define.Direction.UP)
            look.y += -1;
        if(direction == Define.Direction.DOWN)
            look.y += 1;
        if(direction == Define.Direction.LEFT)
            look.x += -1;
        if(direction == Define.Direction.RIGHT)
            look.x += 1;
        Util.calcIndex(look,model);
        movePos.setValue(look.x, look.y);
        return movePos;
    }

    public boolean isEndLoop(Pos branchBlockPos, Define.Direction direction){
        clearGraphMap();
        checkUnknown.clear();
        Pos checkPos = MapUtil.moveDirection(branchBlockPos,direction,model);
        Pos head = posPool.get();
        head.setValue(checkPos.x,checkPos.y);
        int count = 0;
        checkUnknown.push(head);

        while(checkUnknown.size() != 0){
            Pos pop = checkUnknown.pop();
            Pos tmp;
            if(graphMap.get(pop.y).get(pop.x)){
                posPool.push(pop);
                continue;
            }
            graphMap.get(checkPos.y).set(checkPos.x, true);

            tmp = DirectionPosition(pop, Define.Direction.UP,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.AIR)
                count++;
            if((tmp.x == 0 || tmp.y == 0 || tmp.y == model.getRow() - 1|| tmp.x == model.getCol() -1) && model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN)
                count++;
            if(count >=2){
                while(checkUnknown.size() != 0)
                    posPool.push(checkUnknown.pop());
                return false;
            }

            tmp = DirectionPosition(pop, Define.Direction.UP,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN){
                Pos pos = posPool.get();
                pos.setValue(tmp.x,tmp.y);
                checkUnknown.push(pos);
            }
            tmp = DirectionPosition(pop, Define.Direction.DOWN,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN){
                Pos pos = posPool.get();
                pos.setValue(tmp.x,tmp.y);
                checkUnknown.push(pos);
            }
            tmp = DirectionPosition(pop, Define.Direction.RIGHT,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN){
                Pos pos = posPool.get();
                pos.setValue(tmp.x,tmp.y);
                checkUnknown.push(pos);
            }
            tmp = DirectionPosition(pop, Define.Direction.LEFT,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN){
                Pos pos = posPool.get();
                pos.setValue(tmp.x,tmp.y);
                checkUnknown.push(pos);
            }
            posPool.push(pop);
        }
        return true;
    }

}
