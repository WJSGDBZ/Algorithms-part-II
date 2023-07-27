/* *****************************************************************************
 *  Name: Jun
 *  Date: 2023.7.14
 *  Description: Max-Flow Problem
 **************************************************************************** */



import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FordFulkerson;

import java.util.Arrays;
import java.util.HashMap;

public class BaseballElimination {
    private class Team {
        String name;
        int win;
        int losses;
        int toPlay;
        boolean isEliminated;
        Bag<String> certificate;

        public Team(String name, int win, int losses, int toPlay) {
            this.name = name;
            this.win = win;
            this.losses = losses;
            this.toPlay = toPlay;
            this.isEliminated = false;
            this.certificate = new Bag<>();
        }

        @Override
        public String toString() {
            return "Team{" +
                    "name='" + name + '\'' +
                    ", win=" + win +
                    ", losses=" + losses +
                    ", toPlay=" + toPlay +
                    '}';
        }
    }

    private final int number;
    private final Team[] teams;
    private final HashMap<String, Integer> teamsIndex;
    private final int[][] games;

    public BaseballElimination(String filename) { // create a baseball division from given filename in format specified below
        In in = new In(filename);

        number = in.readInt();
        games = new int[number][number];
        teams = new Team[number];
        teamsIndex = new HashMap<>();

        int index = 0;
        for(int i = 0; i < number; i++){
            String tName = in.readString();
            Team t = new Team(tName, in.readInt(), in.readInt(), in.readInt());
            teams[index] = t;
            teamsIndex.put(tName, index);

            for (int j = 0; j < number; j++) {
                games[index][j] = in.readInt();
            }

            index++;
        }

        MathematicalElimination();
    }

    private void MathematicalElimination() {
        int n = numberOfTeams();
        for (String team : teams()) {
            int teamIndex = teamsIndex.get(team);
            int vertices = 2 + (n - 1) * (n - 2) / 2 + n;
            FlowNetwork net = new FlowNetwork(vertices);

            int round = 2 + n;
            int teamBase = 2;
            int s = 0, t = 1;
            int value = 0;
            boolean impossibleToWin = false;
            for (int i = 0; i < n; i++) {
                if (i == teamIndex) continue;
                for (int j = i + 1; j < n; j++) {
                    if (j == teamIndex) continue;
                    String play1 = teams[i].name;
                    String play2 = teams[j].name;

                    int toPlay = against(play1, play2);
                    net.addEdge(new FlowEdge(s, round, toPlay));
                    value += toPlay;

                    net.addEdge(new FlowEdge(round, teamBase + i, Integer.MAX_VALUE));
                    net.addEdge(new FlowEdge(round, teamBase + j, Integer.MAX_VALUE));

                    round++;
                }
            }

            for (int i = 0; i < n; i++) {
                if (i == teamIndex) continue;
                String rival = teams[i].name;
                int stillWin = wins(team) + remaining(team) - wins(rival);
                if (stillWin < 0) {
                    impossibleToWin = true;
                    teams[teamIndex].certificate.add(rival);
                } else {
                    net.addEdge(new FlowEdge(teamBase + i, t, stillWin));
                }
            }

            if (impossibleToWin) {
                teams[teamIndex].isEliminated = true;
            } else {
                FordFulkerson ff = new FordFulkerson(net, s, t);

                if (ff.value() < value) { // team[teamIndex] is mathematically elimination
                    teams[teamIndex].isEliminated = true;

                    for (int v = 0; v < n; v++) {
                        if (v == teamIndex) continue;
                        if (ff.inCut(v + teamBase)) {
                            String name = teams[v].name;
                            teams[teamIndex].certificate.add(name);
                        }
                    }

                }
            }

        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int[] arr : games) {
            str.append(Arrays.toString(arr)).append(" ");
        }
        return "BaseballElimination{" +
                "number=" + number +
                ", teams=" + Arrays.toString(teams) +
                ", teamsIndex=" + teamsIndex +
                ", games=" + str +
                '}';
    }

    public int numberOfTeams() {  // number of teams
        return number;
    }

    public Iterable<String> teams() { // all teams
        return teamsIndex.keySet();
    }

    public int wins(String team) { // number of wins for given team
        if (!teamsIndex.containsKey(team)) {
            throw new IllegalArgumentException("wins: " + "team" + team + "doesn't exist");
        }
        return teams[teamsIndex.get(team)].win;
    }

    public int losses(String team) { // number of losses for given team
        if (!teamsIndex.containsKey(team)) {
            throw new IllegalArgumentException("losses: " + "team" + team + "doesn't exist");
        }
        return teams[teamsIndex.get(team)].losses;
    }

    public int remaining(String team) { // number of remaining games for given team
        if (!teamsIndex.containsKey(team)) {
            throw new IllegalArgumentException("remaining: " + "team" + team + "doesn't exist");
        }
        return teams[teamsIndex.get(team)].toPlay;
    }

    public int against(String team1,
                       String team2) { // number of remaining games between team1 and team2
        if (!teamsIndex.containsKey(team1)) {
            throw new IllegalArgumentException("remaining: " + "team" + team1 + "doesn't exist");
        }
        if (!teamsIndex.containsKey(team2)) {
            throw new IllegalArgumentException("remaining: " + "team" + team2 + "doesn't exist");
        }
        int index1 = teamsIndex.get(team1);
        int index2 = teamsIndex.get(team2);

        return games[index1][index2];
    }

    public boolean isEliminated(String team) { // is given team eliminated?
        if (!teamsIndex.containsKey(team)) {
            throw new IllegalArgumentException("isEliminated: " + "team" + team + "doesn't exist");
        }
        return teams[teamsIndex.get(team)].isEliminated;
    }

    public Iterable<String> certificateOfElimination(String team) {  // subset R of teams that eliminates given team; null if not eliminated
        if (!teamsIndex.containsKey(team)) {
            throw new IllegalArgumentException("certificateOfElimination: " + "team" + team + "doesn't exist");
        }
        Bag<String> certificate = teams[teamsIndex.get(team)].certificate;
        if(certificate.isEmpty()){
            return null;
        }

        return certificate;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
//        BaseballElimination division = new BaseballElimination("/Users/liangjun/Library/CloudStorage/OneDrive-std.uestc.edu.cn/Algorithms-part2/lab3/src/teams4.txt");
//        StdOut.println(division);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }

    }
}
