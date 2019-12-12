package com.pixelificgames.taoexgame.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.pixelificgames.taoexgame.board.HexagonTile3D;
import com.pixelificgames.taoexgame.communication.AIWayMessage;
import com.pixelificgames.taoexgame.communication.Message;
import com.pixelificgames.taoexgame.communication.MessageType;
import com.pixelificgames.taoexgame.piece.PieceStack;
import com.pixelificgames.taoexgame.piece.PieceStackState;
import com.pixelificgames.taoexgame.piece.Player;
import com.pixelificgames.taoexgame.ui.GameMain;

public class AIPlayer extends Player implements Telegraph {

    public HashMap<PieceStack, HashSet<HexagonTile3D>> moves;

    public AIPlayer(String colour) {
        super(colour);
    }


    public HashMap<PieceStack, HashSet<HexagonTile3D>> getAllAvailableMoves() {
        HashMap<PieceStack, HashSet<HexagonTile3D>> allMoves = new HashMap();

        for (PieceStack piece : ownedPieceStacks) {
            HashSet<HexagonTile3D> pieceMoves = piece.getAllAvailableMoves();

            //Only add if there is possible moves
            if (pieceMoves.size() > 0) {
                allMoves.put(piece, pieceMoves);
            }
        }

        //No moves
        if (allMoves.size() == 0) {
            GameMain.gameSession.nextTurn();
        }
        return allMoves;
    }

    public void makeTurn() {
        moves = getAllAvailableMoves();

        //Only move if there are available moves
        if (moves.size() != 0) {
            randomMove();
        } else {
            System.out.println(colour + " force skipped");
//            GameMain.gameSession.nextTurn();
        }

    }

    public void randomMove() {
        //Pick a random piece
        ArrayList<PieceStack> availablePieces = new ArrayList<PieceStack>(moves.keySet());
        PieceStack randomPiece = availablePieces.get(new Random().nextInt(availablePieces.size()));

        ArrayList<HexagonTile3D> randomPieceMoves = new ArrayList(moves.get(randomPiece));

        //Get a random tile from the possible tiles
        HexagonTile3D randomTile = randomPieceMoves.get(new Random().nextInt(randomPieceMoves.size()));

        //Attack if there is a piece there
        if (randomTile.pieceStack != null) {
            if (randomTile.pieceStack.pieces.size() == 0) {
                randomPiece.move(randomTile);
            } else {
                randomPiece.attackPiece(randomTile.pieceStack);
            }
        } else {
            randomPiece.move(randomTile);
        }

        if (randomPiece.state == PieceStackState.WAY) {
            AIWayMessage extra = new AIWayMessage(randomPiece);
            Message.MsgHandler.getMsgManager().dispatchMessage(0.2f, this, this, MessageType.AI_WAY_MOVE, extra);
        } else {
//            GameMain.gameSession.nextTurn();
        }
    }

    public void wayMove(PieceStack pieceStack) {
        List<HexagonTile3D> randomPieceMoves = new ArrayList<HexagonTile3D>(pieceStack.getAllWayMoves());

        if (randomPieceMoves.size() == 0) {
            return;
        }

        //Pick a random way tile
        HexagonTile3D randomWayTile = randomPieceMoves.get(new Random().nextInt(randomPieceMoves.size()));

        //Check if the other stack actually has pieces
        if (randomWayTile.pieceStack != null && randomWayTile.pieceStack.pieces.size() > 0 && randomWayTile.pieceStack.player != this && randomWayTile.pieceStack.getOwnerPiece() != null) {
            pieceStack.attackPiece(randomWayTile.pieceStack);
        } else {
            pieceStack.move(randomWayTile);
        }
    }

    public boolean handleMessage(Telegram msg) {

        switch(msg.message) {
            case MessageType.AI_TURN:
                makeTurn();
                break;
            case MessageType.AI_WAY_MOVE:
                AIWayMessage extraInfo = (AIWayMessage)msg.extraInfo;
                wayMove(extraInfo.pieceStack);
                break;
        }

        return true;
    }

}
