import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class BranchBlockGraph {
    // Our Map에서 Branch Block의 조건에 만족하는 위치에 Branch Blcok을 만들고 Branch Block을 연결하여 graph 형식으로 만든다.

    // dfs를 진행하는데 이전 위치와 현재 위치를 기반으로 이동하기 때문에 이전 위치와 현재 위치 저장
    // key : prev, value : current
    private Stack<AbstractMap.SimpleEntry<Pos, Pos>> posPairList = new Stack<>(); 
    
    // Branch Block Graph를 연결하기 위해서 이전 Branch Block과 어느 방향으로 이동했는지 저장
    private Stack<Pool.LinkPool.Link> linkList = new Stack<>();
    
    // 메모리 최적화를 위한 Pool Class 사용
    private Pool.PosPairPool posPairPool = new Pool.PosPairPool();
    private Pool.LinkPool linkPool = new Pool.LinkPool();
    Model model = null;

    private ArrayList<ArrayList<BooleanDirection>> graphMap = new ArrayList<ArrayList<BooleanDirection>>();
    public HashMap<Integer, BranchBlock> branchBlockHashMap = new HashMap<Integer, BranchBlock>();

    public BranchBlockGraph(Model model){
        this.model = model;
        int row = model.getRow();
        int col = model.getCol();
        for(int i=0; i<row; i++)
            graphMap.add(new ArrayList<>());

        for(int y=0; y<row; y++)
            for(int x=0; x<col; x++)
                graphMap.get(y).add(new BooleanDirection());
    }

    private void clearGraphMap(){
        int row = model.getRow();
        int col = model.getCol();
        for(int y=0; y<row; y++)
            for(int x=0; x<col; x++)
                graphMap.get(y).get(x).setFalse();
    }

    // 재사용성을 위해 초기화
    public void clear(){
        clearGraphMap();
        branchBlockHashMap.clear();
        linkList.clear();
        posPairList.clear();
    }

    // 중복 제거
    public void addHashMap(Pos pos){
        if(!branchBlockHashMap.containsKey(pos.hashCode()))
            branchBlockHashMap.put(pos.hashCode(), new BranchBlock(pos.x, pos.y));
    }

    // Branch Block의 조건에 맞는 Branch Block의 위치 branchBlockHashMap에 저장
    public void checkBranchBlock(){
        clear();
        AbstractMap.SimpleEntry<Pos, Pos> pos = posPairPool.get(); // key : prev, value : current
        pos.getKey().setValue(1,0);
        pos.getValue().setValue(1,0);
        posPairList.clear();
        posPairList.add(pos);

        while(posPairList.size() != 0){
            AbstractMap.SimpleEntry<Pos,Pos> pop = posPairList.pop();
            if(graphMap.get(pop.getValue().y).get(pop.getValue().x).up){ // up 방향 boolean으로 체크
                // 순환구조로 인해.
                posPairPool.push(pop);
                continue;
            }
            graphMap.get(pop.getValue().y).get(pop.getValue().x).up = true;

            if(MapUtil.isBranchBlock(pop.getValue(),model)){
                addHashMap(pop.getValue());

                if(MapUtil.isAir(pop.getValue(), Define.Direction.LEFT, model)){
                    AbstractMap.SimpleEntry<Pos,Pos> push = posPairPool.get();
                    push.getKey().setValue(pop.getValue().x,pop.getValue().y);
                    push.getValue().setValue(pop.getValue().x-1,pop.getValue().y);
                    posPairList.add(push);
                }
                if(MapUtil.isAir(pop.getValue(), Define.Direction.RIGHT, model)){
                    AbstractMap.SimpleEntry<Pos,Pos> push = posPairPool.get();
                    push.getKey().setValue(pop.getValue().x,pop.getValue().y);
                    push.getValue().setValue(pop.getValue().x+1,pop.getValue().y);
                    posPairList.add(push);
                }
                if(MapUtil.isAir(pop.getValue(), Define.Direction.UP, model)){
                    AbstractMap.SimpleEntry<Pos,Pos> push = posPairPool.get();
                    push.getKey().setValue(pop.getValue().x,pop.getValue().y);
                    push.getValue().setValue(pop.getValue().x,pop.getValue().y-1);
                    posPairList.add(push);
                }
                if(MapUtil.isAir(pop.getValue(), Define.Direction.DOWN, model)){
                    AbstractMap.SimpleEntry<Pos,Pos> push = posPairPool.get();
                    push.getKey().setValue(pop.getValue().x,pop.getValue().y);
                    push.getValue().setValue(pop.getValue().x,pop.getValue().y+1);
                    posPairList.add(push);
                }
                posPairPool.push(pop);
            } else {
                Pos move = MapUtil.moveAround(pop.getValue(), pop.getKey(), model);
                pop.getKey().setValue(pop.getValue().x,pop.getValue().y);
                pop.getValue().setValue(move.x,move.y);
                posPairList.push(pop);
            }
        }
    }

    // branchBlockHashmap에 저장된 Brnach Block 간의 연결관계를 파악 후 Graph 형식으로 저장.
    // 후에 다익스트라 연산에 사용
    public BranchBlock buildGraph(){
        clearGraphMap();
        Pool.LinkPool.Link link = linkPool.get();
        AbstractMap.SimpleEntry<Pos, Pos> pos = posPairPool.get(); // key : prev, value : current
        linkList.clear();
        link.prevBranchBlock = branchBlockHashMap.get(Util.HashCode(1,0));
        link.prevBranchDirection = Define.Direction.DOWN;
        link.prevBranchBlock.up.exist = false; // 시작 지점 위는 미존재 false
        link.prevBranchBlock.right.exist = false; // 시작 지점 오른쪽은 미존재 false
        link.prevBranchBlock.left.exist = false; // 시작 지점 왼쪽은 미존재 false
        linkList.add(link);

        while(linkList.size() != 0){
            Pool.LinkPool.Link pop = linkList.pop();
            if(graphMap.get(pop.prevBranchBlock.y).get(pop.prevBranchBlock.x).check(pop.prevBranchDirection)){
                // 순환구조로 인해.
                linkPool.push(pop);
                continue;
            }
            graphMap.get(pop.prevBranchBlock.y).get(pop.prevBranchBlock.x).setTrue(pop.prevBranchDirection);
            pos.getKey().setValue(pop.prevBranchBlock.x, pop.prevBranchBlock.y);
            int count = 0;
            count++;
            Pos move = MapUtil.moveDirection(pos.getKey(), pop.prevBranchDirection, model);
            pos.getValue().setValue(move.x, move.y);
            while(!branchBlockHashMap.containsKey(pos.getValue().hashCode())){
                count++;
                move = MapUtil.moveAround(pos.getValue(), pos.getKey(), model);
                pos.getKey().setValue(pos.getValue().x, pos.getValue().y);
                pos.getValue().setValue(move.x, move.y);
            }
            BranchBlock current = branchBlockHashMap.get(pos.getValue().hashCode());
            BranchBlockUtil.linkBranchBlock(current, pop.prevBranchBlock, count, pos.getValue(), pos.getKey(), pop.prevBranchDirection, model);
            graphMap.get(current.y).get(current.x).setTrue(MapUtil.getDirection(pos.getValue(), pos.getKey()));

            if(MapUtil.isAir(pos.getValue(), Define.Direction.UP, model) ){
                Pool.LinkPool.Link newLink = linkPool.get();
                newLink.prevBranchBlock = current;
                newLink.prevBranchDirection = Define.Direction.UP;
                linkList.push(newLink);
            }
            if(MapUtil.isAir(pos.getValue(), Define.Direction.DOWN, model) ){
                Pool.LinkPool.Link newLink = linkPool.get();
                newLink.prevBranchBlock = current;
                newLink.prevBranchDirection = Define.Direction.DOWN;
                linkList.push(newLink);
            }
            if(MapUtil.isAir(pos.getValue(), Define.Direction.RIGHT, model) ){
                Pool.LinkPool.Link newLink = linkPool.get();
                newLink.prevBranchBlock = current;
                newLink.prevBranchDirection = Define.Direction.RIGHT;
                linkList.push(newLink);
            }
            if(MapUtil.isAir(pos.getValue(), Define.Direction.LEFT, model) ){
                Pool.LinkPool.Link newLink = linkPool.get();
                newLink.prevBranchBlock = current;
                newLink.prevBranchDirection = Define.Direction.LEFT;
                linkList.push(newLink);
            }
            linkPool.push(pop);
        }
        posPairPool.push(pos);
        return branchBlockHashMap.get(Util.HashCode(1,0));
    }
}
