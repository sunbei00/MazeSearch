import javax.security.auth.login.AccountExpiredException;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.text.html.HTMLDocument;
import java.lang.reflect.Array;
import java.util.*;

/*
다익스트라 알고리즘을 이용하여 최적의 경로를 구하는 알고리즘

입력값 : 브랜치 블록 해쉬맵 키세트
출력값 : 브랜치 블록 타입 어레이 리스트

1. 해쉬맴 키값 세트를 받아 첫번째 브랜치블록(시작점)을 브랜치 리스트에 추가
2. 이후 브랜치블록의 상,하,좌,우 값에 링크드 브랜치가 있는지 확인
3. 있을경우 브랜치 리스트에 추가
4. 링크드 브랜치가 없을때까지(상하좌우 모두 null인 경우) 반복
5. 브랜치 리스트를 기반으로 2차원 배열 그래프 시트 생성
6. 그래프 시트에 각 브랜치별 거리 입력
7. 완성된 그래프 시트를 기반으로 다익스트라 알고리즘 계산
8. 시작 브랜치에서 각 브랜치까지의 거리 계산
9. DestInfo 클래스로 값 전달하여 우선순위 연산
10. 가장 우선순위가 높은 브랜치 인덱스 리턴
11. 현재 브랜치에서 리턴받은 브랜치까지 경로를 리스트에 저장 -> 열거형으로 상,하,좌,우 이동방향을 리스트에 저장
12. 경로 리스트 리턴
*/
public class Route {

    public ArrayList<ArrayList<Define.Direction>> routeTable = null;
    public ArrayList<DestInfo> BBList = null;
    public ArrayList<BranchBlock> List = null;
    public ArrayList<BranchBlock> After = null;
    public ArrayList<ArrayList<Integer>> Graph = null;
    public Pos pos = null;
    public BranchBlock firstBranch = null;

    public Model model;

    // 생성자 : 해쉬맵과 현제 pos값을 인자로 받음
    public Route(BranchBlock branchBlock, Pos pos, Model model) {
        firstBranch = branchBlock;
        this.pos = new Pos(pos.x, pos.y);

        this.List = new ArrayList<BranchBlock>();
        this.BBList = new ArrayList<DestInfo>();
        this.model = model;
    }

    public Route(Pos pos) {
        this.pos = pos;
    }

    // 브랜치 블록 리스트 생성
    public void SetList() {
        List.clear();
        for (BranchBlock b : model.branchBlockHashMap.values())
            List.add(b);
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

            for(int i = 0; i< now.directions.size(); i++) {
                route.set(i, now.directions.get(i));
            }
            //방문 여부 확인
            if(check[List.indexOf(nowBranch)]) continue;
            check[List.indexOf(nowBranch)] = true;


            //각각의 4방향의 linked branch 여부 확인 후, 경로의 길이를 비교한 뒤 더 짧은 경로가 있다면 업데이트
            if(nowBranch.up.linkedBranch != null && dis[List.indexOf(nowBranch.up.linkedBranch)] > dis[List.indexOf(nowBranch)] + nowBranch.up.distance) {
                dis[List.indexOf(nowBranch.up.linkedBranch)] = dis[List.indexOf(nowBranch)] + nowBranch.up.distance;
                route.add(Define.Direction.UP);
                routeTable.set(List.indexOf(nowBranch.up.linkedBranch),route);

                pq.offer(new DestInfo(nowBranch.up.linkedBranch, dis[List.indexOf(nowBranch.up.linkedBranch)]));
            }
            if(nowBranch.down.linkedBranch != null && dis[List.indexOf(nowBranch.down.linkedBranch)] > dis[List.indexOf(nowBranch)] + nowBranch.down.distance) {
                dis[List.indexOf(nowBranch.down.linkedBranch)] = dis[List.indexOf(nowBranch)] + nowBranch.down.distance;
                route.add(Define.Direction.DOWN);
                routeTable.set(List.indexOf(nowBranch.down.linkedBranch),route);

                pq.offer(new DestInfo(nowBranch.down.linkedBranch, dis[List.indexOf(nowBranch.down.linkedBranch)]));
            }
            if(nowBranch.left.linkedBranch != null && dis[List.indexOf(nowBranch.left.linkedBranch)] > dis[List.indexOf(nowBranch)] + nowBranch.left.distance) {
                dis[List.indexOf(nowBranch.left.linkedBranch)] = dis[List.indexOf(nowBranch)] + nowBranch.left.distance;
                route.add(Define.Direction.LEFT);
                routeTable.set(List.indexOf(nowBranch.left.linkedBranch),route);

                pq.offer(new DestInfo(nowBranch.left.linkedBranch, dis[List.indexOf(nowBranch.left.linkedBranch)]));
            }
            if(nowBranch.right.linkedBranch != null && dis[List.indexOf(nowBranch.right.linkedBranch)] > dis[List.indexOf(nowBranch)] + nowBranch.right.distance) {
                dis[List.indexOf(nowBranch.right.linkedBranch)] = dis[List.indexOf(nowBranch)] + nowBranch.right.distance;
                route.add(Define.Direction.RIGHT);
                routeTable.set(List.indexOf(nowBranch.right.linkedBranch),route);

                pq.offer(new DestInfo(nowBranch.right.linkedBranch, dis[List.indexOf(nowBranch.right.linkedBranch)]));
            }
        }
        //리스트에 각 브랜치와 최소길이를 add
        for(int i = 0; i < dis.length; i++) {
            BBList.add(new DestInfo(List.get(i), dis[i],routeTable.get(i)));
        }

        return BBList;
    }

}