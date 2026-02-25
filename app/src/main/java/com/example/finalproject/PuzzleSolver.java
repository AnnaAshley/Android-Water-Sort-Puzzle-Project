package com.example.finalproject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PuzzleSolver {
    private static final int MAX_DEPTH = 1000;

    //move class to organize moves
    public static class Move {
        public int from;
        public int to;
        public Move (int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
    public static List<Move> solve(ArrayList<ArrayList<Integer>> layersCopy, int maxLayer) {
        List<Move> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        dfs(layersCopy, path, visited, maxLayer, 0);
        return path;
    }

    private static boolean dfs(ArrayList<ArrayList<Integer>> state, List<Move> path, Set<String> visited, int maxLayer, int depth) {
        if (depth > MAX_DEPTH) return false;
        if (isSolved(state, maxLayer)) return true;

        String stateKey = encodeState(state);
        if(visited.contains(stateKey)) return false;
        visited.add(stateKey);

        int n = state.size();
        for (int from = 0; from < n; from++){
            ArrayList<Integer> fromTube = state.get(from);
            if (fromTube.isEmpty()) continue;

            int colorToPour = fromTube.get(fromTube.size() - 1);

            int sameColorCount = 1;
            for (int i = fromTube.size() - 2; i >=0; i--) {
                if (fromTube.get(i).equals(colorToPour)) sameColorCount++;
                else break;
            }

            for (int to = 0; to < n; to++) {
                if (from == to) continue;

                ArrayList<Integer> toTube = state.get(to);
                if (toTube.size() >= maxLayer) continue;
                if (!toTube.isEmpty() && !toTube.get(toTube.size() - 1).equals(colorToPour)) continue;

                //prevent cycles back
                if (!path.isEmpty()) {
                    Move lastMove = path.get(path.size() - 1);
                    if (lastMove.from == to && lastMove.to == from) continue;
                }

                //skip pouring same color onto same color if it doesn't progress
               // if (!toTube.isEmpty() && fromTube.get(fromTube.size() -1).equals(toTube.get(toTube.size()-1))&& sameColorCount == 1) continue;


                int space = maxLayer - toTube.size();
                int pourAmount = Math.min(sameColorCount, space);

                if (pourAmount == 0) continue;// help ensure only meaningful pours are preformed

                // simulate move
                ArrayList<ArrayList<Integer>> newState = deepCopy(state);
                for (int i = 0; i < pourAmount; i++) {
                    newState.get(to).add(colorToPour);
                    newState.get(from).remove(newState.get(from).size() - 1);
                }

                path.add(new Move(from, to));
                if (dfs(newState, path, visited, maxLayer, depth + 1)) return true;
                path.remove(path.size() - 1); // backtrack
            }
        }





        return false;

    }

    //a modified copy of checkWinCondition from MainActivity. only names changed
    private static boolean isSolved(ArrayList<ArrayList<Integer>> layers, int MaxLayer){
        for (ArrayList<Integer> tube : layers) {
            if(tube.isEmpty()) continue;
            if (tube.size() != MaxLayer) return false;
            int color = tube.get(0);
            for (int c : tube) {
                if (c != color) return false;
            }
        }
        return true;
    }

    private static ArrayList<ArrayList<Integer>> deepCopy(ArrayList<ArrayList<Integer>> original) {
        ArrayList<ArrayList<Integer>> copy = new ArrayList<>();
        for (List<Integer> tube : original) {
            copy.add(new ArrayList<>(tube));
        }
        return copy;
    }

    private static String encodeState(ArrayList<ArrayList<Integer>> state) {
        StringBuilder sb = new StringBuilder();
        for (ArrayList<Integer> tube : state) {
            sb.append('|');
            for (int c : tube) {
                sb.append(c).append(',');
            }
        }
        return sb.toString();
    }


}
