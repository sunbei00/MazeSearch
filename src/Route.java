import javax.security.auth.login.AccountExpiredException;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.text.html.HTMLDocument;
import java.lang.reflect.Array;
import java.util.*;

public class Route {
    public ArrayList<ArrayList<Define.Direction>> routeTable = null;
    public ArrayList<DestInfo> destInfoList = null;
    public ArrayList<BranchBlock> branchList = null;
    public Pos playerPos = null;
    public BranchBlock firstBranch = null;
    public HashMap<Integer, BranchBlock> branchBlockHashMap;

    public Route(BranchBlock branchBlock, Pos playerPos, HashMap<Integer, BranchBlock> branchBlockHashMap ) {
        firstBranch = branchBlock;
        this.playerPos = new Pos(playerPos.x, playerPos.y);

        this.branchList = new ArrayList<BranchBlock>();
        this.destInfoList = new ArrayList<DestInfo>();
        this.branchBlockHashMap = branchBlockHashMap;
    }

    public void SetList() {
        branchList.clear();
        for (BranchBlock b : branchBlockHashMap.values()) branchList.add(b);
    }
    
    public ArrayList<DestInfo> Dijkstra (BranchBlock start) {
        int INT = Integer.MAX_VALUE;
        int size = branchList.size();
        boolean[] isChecked = new boolean[size];
        int[] distance = new int[size];
        this.routeTable = new ArrayList<ArrayList<Define.Direction>>();
        ArrayList<Define.Direction> route = new ArrayList<Define.Direction>();

        Arrays.fill(distance, INT);
        distance[branchList.indexOf(start)] = 0;

        PriorityQueue<DestInfo> priorityQueue = new PriorityQueue<>();
        priorityQueue.offer(new DestInfo(start, 0));

        for(int i = 0; i < branchList.size(); i++)
            routeTable.add(new ArrayList<>());

        while(!priorityQueue.isEmpty()) {
            DestInfo now = priorityQueue.poll();
            BranchBlock nowBranch = now.branchBlock;
            now.directions = new ArrayList<Define.Direction>();
            now.directions.addAll(routeTable.get(branchList.indexOf(nowBranch)));

            if (isChecked[branchList.indexOf(nowBranch)]) continue;
            isChecked[branchList.indexOf(nowBranch)] = true;

            if (nowBranch.up.linkedBranch != null && distance[branchList.indexOf(nowBranch.up.linkedBranch)] > distance[branchList.indexOf(nowBranch)] + nowBranch.up.distance) {
                distance[branchList.indexOf(nowBranch.up.linkedBranch)] = distance[branchList.indexOf(nowBranch)] + nowBranch.up.distance;
                route.clear();
                if (!now.directions.isEmpty()) route.addAll(now.directions);
                route.add(Define.Direction.UP);
                routeTable.get(branchList.indexOf(nowBranch.up.linkedBranch)).addAll(route);

                priorityQueue.offer(new DestInfo(nowBranch.up.linkedBranch, distance[branchList.indexOf(nowBranch.up.linkedBranch)]));
            }
            if (nowBranch.down.linkedBranch != null && distance[branchList.indexOf(nowBranch.down.linkedBranch)] > distance[branchList.indexOf(nowBranch)] + nowBranch.down.distance) {
                distance[branchList.indexOf(nowBranch.down.linkedBranch)] = distance[branchList.indexOf(nowBranch)] + nowBranch.down.distance;
                route.clear();
                if (!now.directions.isEmpty()) route.addAll(now.directions);
                route.add(Define.Direction.DOWN);
                routeTable.get(branchList.indexOf(nowBranch.down.linkedBranch)).addAll(route);

                priorityQueue.offer(new DestInfo(nowBranch.down.linkedBranch, distance[branchList.indexOf(nowBranch.down.linkedBranch)]));
            }
            if (nowBranch.left.linkedBranch != null && distance[branchList.indexOf(nowBranch.left.linkedBranch)] > distance[branchList.indexOf(nowBranch)] + nowBranch.left.distance) {
                distance[branchList.indexOf(nowBranch.left.linkedBranch)] = distance[branchList.indexOf(nowBranch)] + nowBranch.left.distance;
                route.clear();
                if (!now.directions.isEmpty()) route.addAll(now.directions);
                route.add(Define.Direction.LEFT);
                routeTable.get(branchList.indexOf(nowBranch.left.linkedBranch)).addAll(route);

                priorityQueue.offer(new DestInfo(nowBranch.left.linkedBranch, distance[branchList.indexOf(nowBranch.left.linkedBranch)]));
            }
            if (nowBranch.right.linkedBranch != null && distance[branchList.indexOf(nowBranch.right.linkedBranch)] > distance[branchList.indexOf(nowBranch)] + nowBranch.right.distance) {
                distance[branchList.indexOf(nowBranch.right.linkedBranch)] = distance[branchList.indexOf(nowBranch)] + nowBranch.right.distance;
                route.clear();
                if (!now.directions.isEmpty()) route.addAll(now.directions);
                route.add(Define.Direction.RIGHT);
                routeTable.get(branchList.indexOf(nowBranch.right.linkedBranch)).addAll(route);

                priorityQueue.offer(new DestInfo(nowBranch.right.linkedBranch, distance[branchList.indexOf(nowBranch.right.linkedBranch)]));
            }
        }

        for(int i = 0; i < distance.length; i++) {
            boolean isNew = true;
            if(branchList.get(i).up.exist && branchList.get(i).up.linkedBranch == null && isNew) {
                destInfoList.add(new DestInfo(branchList.get(i), distance[i],routeTable.get(i)));
                isNew = false;
            }
            if(branchList.get(i).down.exist && branchList.get(i).down.linkedBranch == null && isNew) {
                destInfoList.add(new DestInfo(branchList.get(i), distance[i],routeTable.get(i)));
                isNew = false;
            }
            if(branchList.get(i).left.exist && branchList.get(i).left.linkedBranch == null && isNew) {
                destInfoList.add(new DestInfo(branchList.get(i), distance[i],routeTable.get(i)));
                isNew = false;
            }
            if(branchList.get(i).right.exist && branchList.get(i).right.linkedBranch == null && isNew) {
                destInfoList.add(new DestInfo(branchList.get(i), distance[i],routeTable.get(i)));
                isNew = false;
            }
        }
        return destInfoList;
    }
}