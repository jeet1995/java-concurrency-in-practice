package ch8_applying_thread_pools.parallelizing_recursive_algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PuzzleFramework {}

interface Puzzle {
    Position initialPosition();
    Set<Move> legalMoves();
    boolean isGoal(Position p);
    Position move(Position p, Move m);
}

class Node {
    // move to reach this node
    final Move m;
    // position of this node
    final Position p;
    // previous position and move to reach this node
    final Node prev;

    Node(Move m, Position p, Node prev) {
        this.m = m;
        this.p = p;
        this.prev = prev;
    }

    public List<Move> computePartialSolToReachThisNode() {
        List<Move> partialSolution = new ArrayList<>();
        Node current = this;
        while (current != null) {
            partialSolution.add(current.m);
            current = current.prev;
        }
        return partialSolution;
    }
}

class Position {}
class Move {}