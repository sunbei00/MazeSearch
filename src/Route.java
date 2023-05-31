import javax.security.auth.login.AccountExpiredException;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.text.html.HTMLDocument;
import java.lang.reflect.Array;
import java.util.*;

public class Route {
    public ArrayList<ArrayList<Define.Direction>> routeTable = null;
    public ArrayList<DestInfo> destInfoList = null;
    public ArrayList<BranchBlock> branchList = null;
    public HashMap<Integer, BranchBlock> branchBlockHashMap;

    public Route(HashMap<Integer, BranchBlock> branchBlockHashMap ) {

        this.branchList = new ArrayList<BranchBlock>();
        this.destInfoList = new ArrayList<DestInfo>();
        this.branchBlockHashMap = branchBlockHashMap;
    }

    //해쉬맵의 브랜치 블록을 ArrayList로 저장
    public void setList() {
        branchList.clear();
        for (BranchBlock b : branchBlockHashMap.values()) branchList.add(b);
    }

    //PriorityQueue를 사용한 다익스트라 연산
    //PriorityQueue는 DestInfo.distance를 우선순위로 계산
    public ArrayList<DestInfo> dijkstra (BranchBlock start) {
        int INT = Integer.MAX_VALUE;
        int size = branchList.size();
        boolean[] isChecked = new boolean[size];
        int[] distance = new int[size];
        this.routeTable = new ArrayList<ArrayList<Define.Direction>>();
        ArrayList<Define.Direction> route = new ArrayList<Define.Direction>();

        //시작위치를 제외한 브랜치별 거리는 INT_MAX값
        Arrays.fill(distance, INT);
        distance[branchList.indexOf(start)] = 0;

        //우선순위 큐를 생성하고 start브랜치 블록을 distance = 0으로 설정하여 DestInfo 타입으로 큐에 넣음
        PriorityQueue<DestInfo> priorityQueue = new PriorityQueue<>();
        priorityQueue.offer(new DestInfo(start, 0));

        for(int i = 0; i < branchList.size(); i++)
            routeTable.add(new ArrayList<>());

        //우선순위 큐가 빌때까지 반복
        while(!priorityQueue.isEmpty()) {
            //큐에서 가장 우선순위가 높은 맴버를 now로 poll
            DestInfo now = priorityQueue.poll();
            BranchBlock nowBranch = now.branchBlock;
            now.directions = new ArrayList<Define.Direction>();
            now.directions.addAll(routeTable.get(branchList.indexOf(nowBranch)));

            //이미 확인한 브랜치 블록이라면 넘어감
            if (isChecked[branchList.indexOf(nowBranch)]) continue;
            isChecked[branchList.indexOf(nowBranch)] = true;

            //브랜치 블록에 연결된 브랜치 블록이(LinkedBranch) 있고, distance에 저장된 거리보다 현재 브랜치에 저장된 거리 + 연결됨 블록 가지의 거리 가 더 짧다면
            //거리 업데이트와 경로를 추가하고 큐에 offer
            //거리와 경로에 저장되는 리스트의 인덱스 값은 branchList와 동일하게 설정
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

        //경로는 존재하지만 연결된 브랜치 블록이 없는경우(=안가본 경로인 경우)만 destInfoList에 추가
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