package com.pixelificgames.taoexgame.ai;

import com.pixelificgames.taoexgame.board.HexagonTile3D;
import com.pixelificgames.taoexgame.piece.PieceStack;

public class WeightedMove {

    public PieceStack piece;
    public HexagonTile3D destination;
    public int weight; //not used

    public WeightedMove(PieceStack piece, HexagonTile3D destination, int weight) {
        this.piece = piece;
        this.destination = destination;
        this.weight = weight;
    }
}
