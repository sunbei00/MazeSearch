import javax.security.auth.login.AccountExpiredException;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.text.html.HTMLDocument;
import java.lang.reflect.Array;
import java.util.*;

/*
Setlist : 브랜치가 저장되어있는 해쉬맵을 받아, 해쉬맵에 저장되어 있는 브랜치 블록들을 List Arraylist에 저장
Dijkstra :  현재위치의 브랜치 블록을 받아, 거리를 0으로 설정하고 나머지 브랜치 블록들은 MAX_int값으로 저장
            거리에 대한 우선순위 큐를 선언해 현재위치 브랜치블록을 넣고 각 상하좌우에 연결된 브랜치 블록이 있는지 탐색
            만약 어느 한 방향에 연결된 브랜치 블록이 있고 거리 배열에 저장된 거리보다 거리가 짧다면,
            해당 브랜치블록의 거리배열을 업데이트하고 방향에 맞는 경로 추가와 우선순위 큐에 브랜치 블록 추가


*/
public class Route {

    public ArrayList<ArrayList<Define.Direction>> routeTable = null;
    public ArrayList<DestInfo> BBList = null;
    public ArrayList<BranchBlock> List = null;
    public Pos pos = null;
    public BranchBlock firstBranch = null;
    public HashMap<Integer, BranchBlock> branchBlockHashMap;

    // 생성자 : 해쉬맵과 현제 pos값을 인자로 받음
    public Route(BranchBlock branchBlock, Pos pos, HashMap<Integer, BranchBlock> branchBlockHashMap ) {
        firstBranch = branchBlock;
        this.pos = new Pos(pos.x, pos.y);

        this.List = new ArrayList<BranchBlock>();
        this.BBList = new ArrayList<DestInfo>();
        this.branchBlockHashMap = branchBlockHashMap;
    }
    // 브랜치 블록 리스트 생성
    public void SetList() {
        List.clear();
        for (BranchBlock b : branchBlockHashMap.values()) List.add(b);
    }
    /*
     * 다익스트라 알고리즘 핵심 키워드 : 방문여부, 거리
     * 가보지 않은 노드들중 가장 가까운 거리의 노드 거리 계산
     */

    // 최소거리 계산
    // 현재 브랜치 기준으로 각 브랜치까지의 거리를 계산해 DestInfo 타입의 리스트로 저장
    public ArrayList<DestInfo> Dijkstra (BranchBlock start) {
        int size = List.size();
        boolean[] check = new boolean[size];
        int[] dis = new int[size];
        this.routeTable = new ArrayList<ArrayList<Define.Direction>>();
        ArrayList<Define.Direction> route = new ArrayList<Define.Direction>();
        int INF = Integer.MAX_VALUE;

        Arrays.fill(dis, INF);
        dis[List.indexOf(start)] = 0;

        PriorityQueue<DestInfo> pq = new PriorityQueue<>();
        pq.offer(new DestInfo(start, 0));

        for(int i = 0; i < List.size(); i++)
            routeTable.add(new ArrayList<>());

        while(!pq.isEmpty()) {
            DestInfo now = pq.poll();
            BranchBlock nowBranch = now.branchBlock;
            now.directions = new ArrayList<Define.Direction>();
            now.directions.addAll(routeTable.get(List.indexOf(nowBranch)));

            //방문 여부 확인
            if (check[List.indexOf(nowBranch)]) continue;
            check[List.indexOf(nowBranch)] = true;

            //각각의 4방향의 linked branch 여부 확인 후, 경로의 길이를 비교한 뒤 더 짧은 경로가 있다면 업데이트
            if (nowBranch.up.linkedBranch != null && dis[List.indexOf(nowBranch.up.linkedBranch)] > dis[List.indexOf(nowBranch)] + nowBranch.up.distance) {
                dis[List.indexOf(nowBranch.up.linkedBranch)] = dis[List.indexOf(nowBranch)] + nowBranch.up.distance;
                route.clear();
                if (!now.directions.isEmpty()) route.addAll(now.directions);
                route.add(Define.Direction.UP);
                routeTable.get(List.indexOf(nowBranch.up.linkedBranch)).addAll(route);

                pq.offer(new DestInfo(nowBranch.up.linkedBranch, dis[List.indexOf(nowBranch.up.linkedBranch)]));
            }

            if (nowBranch.down.linkedBranch != null && dis[List.indexOf(nowBranch.down.linkedBranch)] > dis[List.indexOf(nowBranch)] + nowBranch.down.distance) {
                dis[List.indexOf(nowBranch.down.linkedBranch)] = dis[List.indexOf(nowBranch)] + nowBranch.down.distance;
                route.clear();
                if (!now.directions.isEmpty()) route.addAll(now.directions);
                route.add(Define.Direction.DOWN);
                routeTable.get(List.indexOf(nowBranch.down.linkedBranch)).addAll(route);

                pq.offer(new DestInfo(nowBranch.down.linkedBranch, dis[List.indexOf(nowBranch.down.linkedBranch)]));
            }
            if (nowBranch.left.linkedBranch != null && dis[List.indexOf(nowBranch.left.linkedBranch)] > dis[List.indexOf(nowBranch)] + nowBranch.left.distance) {
                dis[List.indexOf(nowBranch.left.linkedBranch)] = dis[List.indexOf(nowBranch)] + nowBranch.left.distance;
                route.clear();
                if (!now.directions.isEmpty()) route.addAll(now.directions);
                route.add(Define.Direction.LEFT);
                routeTable.get(List.indexOf(nowBranch.left.linkedBranch)).addAll(route);

                pq.offer(new DestInfo(nowBranch.left.linkedBranch, dis[List.indexOf(nowBranch.left.linkedBranch)]));
            }
            if (nowBranch.right.linkedBranch != null && dis[List.indexOf(nowBranch.right.linkedBranch)] > dis[List.indexOf(nowBranch)] + nowBranch.right.distance) {
                dis[List.indexOf(nowBranch.right.linkedBranch)] = dis[List.indexOf(nowBranch)] + nowBranch.right.distance;
                route.clear();
                if (!now.directions.isEmpty()) route.addAll(now.directions);
                route.add(Define.Direction.RIGHT);
                routeTable.get(List.indexOf(nowBranch.right.linkedBranch)).addAll(route);

                pq.offer(new DestInfo(nowBranch.right.linkedBranch, dis[List.indexOf(nowBranch.right.linkedBranch)]));
            }
        }
        //리스트에 각 브랜치와 최소길이를 add
        for(int i = 0; i < dis.length; i++) {
            boolean isNew = true;
            if(List.get(i).up.exist && List.get(i).up.linkedBranch == null && isNew) {
                BBList.add(new DestInfo(List.get(i), dis[i],routeTable.get(i)));
                isNew = false;
            }
            if(List.get(i).down.exist && List.get(i).down.linkedBranch == null && isNew) {
                BBList.add(new DestInfo(List.get(i), dis[i],routeTable.get(i)));
                isNew = false;
            }
            if(List.get(i).left.exist && List.get(i).left.linkedBranch == null && isNew) {
                BBList.add(new DestInfo(List.get(i), dis[i],routeTable.get(i)));
                isNew = false;
            }
            if(List.get(i).right.exist && List.get(i).right.linkedBranch == null && isNew) {
                BBList.add(new DestInfo(List.get(i), dis[i],routeTable.get(i)));
                isNew = false;
            }
        }
        System.out.println(BBList.size());
        return BBList;
    }
}