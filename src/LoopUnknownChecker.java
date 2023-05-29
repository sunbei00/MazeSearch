import java.util.ArrayList;
import java.util.Stack;

public class LoopUnknownChecker {

    private Model model;
    private ArrayList<ArrayList<Boolean>> graphMap = new ArrayList<ArrayList<Boolean>>();
    private Stack<Pos> checkUnknown = new Stack<>();
    private Pool.PosPool posPool = new Pool.PosPool();


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

    public boolean isEndLoop(Pos branchBlockPos, Define.Direction direction){
        if(branchBlockPos.x == 0 || branchBlockPos.x == model.getCol() - 1)
            return false;
        if(branchBlockPos.y == 0 || branchBlockPos.y == model.getRow() - 1)
            return false;
        clearGraphMap();
        checkUnknown.clear();
        Pos checkPos = MapUtil.DirectionPosition(branchBlockPos,direction,model);
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
            graphMap.get(pop.y).set(pop.x, true);

            tmp = MapUtil.DirectionPosition(pop, Define.Direction.UP,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.AIR)
                count++;
            if((tmp.x == 0 || tmp.y == 0 || tmp.y == model.getRow() - 1 || tmp.x == model.getCol() -1) && model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN)
                count++;
            tmp = MapUtil.DirectionPosition(pop, Define.Direction.DOWN,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.AIR)
                count++;
            if((tmp.x == 0 || tmp.y == 0 || tmp.y == model.getRow() - 1 || tmp.x == model.getCol() -1) && model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN)
                count++;
            tmp = MapUtil.DirectionPosition(pop, Define.Direction.LEFT,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.AIR)
                count++;
            if((tmp.x == 0 || tmp.y == 0 || tmp.y == model.getRow() - 1 || tmp.x == model.getCol() -1) && model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN)
                count++;
            tmp = MapUtil.DirectionPosition(pop, Define.Direction.RIGHT,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.AIR)
                count++;
            if((tmp.x == 0 || tmp.y == 0 || tmp.y == model.getRow() - 1 || tmp.x == model.getCol() -1) && model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN)
                count++;


            if(count >=2){
                while(checkUnknown.size() != 0)
                    posPool.push(checkUnknown.pop());
                return false;
            }

            tmp = MapUtil.DirectionPosition(pop, Define.Direction.UP,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN){
                Pos pos = posPool.get();
                pos.setValue(tmp.x,tmp.y);
                checkUnknown.push(pos);
            }
            tmp = MapUtil.DirectionPosition(pop, Define.Direction.DOWN,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN){
                Pos pos = posPool.get();
                pos.setValue(tmp.x,tmp.y);
                checkUnknown.push(pos);
            }
            tmp = MapUtil.DirectionPosition(pop, Define.Direction.RIGHT,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN){
                Pos pos = posPool.get();
                pos.setValue(tmp.x,tmp.y);
                checkUnknown.push(pos);
            }
            tmp = MapUtil.DirectionPosition(pop, Define.Direction.LEFT,model);
            if(model.our.get(tmp.y).get(tmp.x).type == Define.UNKNOWN){
                Pos pos = posPool.get();
                pos.setValue(tmp.x,tmp.y);
                checkUnknown.push(pos);
            }
            posPool.push(pop);
        }
        int row = model.getRow();
        int col = model.getCol();
        for(int i=0; i<row; i++)
            for(int j=0; j<col; j++)
                if(graphMap.get(i).get(j) == true)
                    if(!(i ==0 || j == 0 || i == model.getRow() - 1 || j == model.getCol() - 1))
                        model.our.get(i).get(j).type = Define.CLOSE;

        return true;
    }

}
