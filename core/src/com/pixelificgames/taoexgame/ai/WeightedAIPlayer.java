package com.pixelificgames.taoexgame.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.pixelificgames.taoexgame.board.HexagonTile3D;
import com.pixelificgames.taoexgame.communication.AIWayMessage;
import com.pixelificgames.taoexgame.communication.Message;
import com.pixelificgames.taoexgame.communication.MessageType;
import com.pixelificgames.taoexgame.piece.HexagonPiece;
import com.pixelificgames.taoexgame.piece.PieceStack;
import com.pixelificgames.taoexgame.piece.PieceStackState;

public class WeightedAIPlayer extends AIPlayer {

    public ArrayList<WeightedMove> badMoves;
    public ArrayList<WeightedMove> normalMoves;
    public ArrayList<WeightedMove> wayMoves;
    public ArrayList<WeightedMove> safeMoves;
    public ArrayList<WeightedMove> captureMoves;

    public WeightedAIPlayer(String colour) {
        super(colour);
        badMoves = new ArrayList<WeightedMove>();
        normalMoves = new ArrayList<WeightedMove>();
        wayMoves = new ArrayList<WeightedMove>();
        safeMoves = new ArrayList<WeightedMove>();
        captureMoves = new ArrayList<WeightedMove>();
    }

    @Override
    public void makeTurn() {
        moves = getAllAvailableMoves();

        //No available moves
        if (moves.size() == 0) {
            System.out.println(colour + " force skipped");
            return;
        }

        weightMoves(moves);
        WeightedMove pickedMove = null;

        //Maximum number of attempts before giving up
        int numAttempts = 10;
        while (numAttempts > 0) {
            Integer roll = new Random().nextInt(1000);

            //0 - 2
            if (roll > 249 && captureMoves.size() > 0) {
                pickedMove = captureMoves.get(new Random().nextInt(captureMoves.size()));

            } else if (roll > 100 && safeMoves.size() > 0) { //2 - 5
                pickedMove = safeMoves.get(new Random().nextInt(safeMoves.size()));

            } else if (roll > 10 && wayMoves.size() > 0) { // 6 - 15
                pickedMove = wayMoves.get(new Random().nextInt(wayMoves.size()));

            } else if (roll > 5 && normalMoves.size() > 0) {
                pickedMove = normalMoves.get(new Random().nextInt(normalMoves.size()));

            } else if (badMoves.size() > 0) {
                pickedMove = badMoves.get(new Random().nextInt(badMoves.size()));
            }

            if (pickedMove != null && pickedMove.piece.pieces.size() > 0) {
                break;
            }
            numAttempts--;
        }

        //Failed to pick a move
        if (numAttempts == 0) {
            System.out.println(this.colour + " failed to decide on a move. Forcing a random move");
            randomMove();
        } else {
            move(pickedMove.piece, pickedMove.destination);
        }

        //Clear the moves
        badMoves.clear();
        normalMoves.clear();
        wayMoves.clear();
        safeMoves.clear();
        captureMoves.clear();
    }

    public void move(PieceStack piece, HexagonTile3D destination) {
        if (destination.pieceStack != null && destination.pieceStack.player != this) {
            if (destination.pieceStack.pieces.size() == 0) {
                piece.move(destination);
            } else {
                piece.attackPiece(destination.pieceStack);
            }
        } else {
            piece.move(destination);
        }

        if (piece.state == PieceStackState.WAY) {
            AIWayMessage extra = new AIWayMessage(piece);
            Message.MsgHandler.getMsgManager().dispatchMessage(0.6f, this, this, MessageType.AI_WAY_MOVE, extra);
        }
    }

    /**
     * Weight map:
     * 0-5 = Will make you stuck or vulnerable to capture
     * 6-10 = Default weighting
     * 11-100 = Put a piece on the way,
     * 101-249 = Avoid capture by another piece
     * 250-999 = Capture another piece
     */
    public void weightMoves(HashMap<PieceStack, HashSet<HexagonTile3D>> moves) {

        for (PieceStack pieceStack : moves.keySet()) {
            for (HexagonTile3D tile : moves.get(pieceStack)) {

                //Capture a piece
                if (tile.pieceStack != null && tile.pieceStack.pieces.size() != 0 && tile.pieceStack.player != pieceStack.player) {
                    WeightedMove wm = new WeightedMove(pieceStack, tile, 64);
                    captureMoves.add(wm);
                    continue;
                }

                //Moving to safety
                if (checkTileThreat(pieceStack.getCurrentTile()) == true && checkTileThreat(tile) == false) {
                    WeightedMove wm = new WeightedMove(pieceStack, tile, 20);
                    safeMoves.add(wm);
                    continue;
                }

                //If it's a way tile
                if (tile.type == 2 && (tile.pieceStack == null || tile.pieceStack.pieces.size() == 0)) {
                    WeightedMove wm = new WeightedMove(pieceStack, tile, 10);
                    wayMoves.add(wm);
                    continue;
                }

                //Will make your piece stuck
                boolean stuck = false;
                for (HexagonPiece hp : pieceStack.pieces) {

                    if (tile.neighbours[hp.getDirection().toInt()] == null) {
                        stuck = true;
                    } else {
                        stuck = false;
                        break;
                    }

                }
                if (stuck) {
                    WeightedMove wm = new WeightedMove(pieceStack, tile, 2);
                    badMoves.add(wm);
                    continue;
                }

                //Tile can be attacked by another player
                if (checkTileThreat(tile)) {
                    WeightedMove wm = new WeightedMove(pieceStack, tile, 2);
                    badMoves.add(wm);
                    continue;
                }

                //Default case
                WeightedMove wm = new WeightedMove(pieceStack, tile, 4);
                normalMoves.add(wm);

            }
        }
    }

    /**
     * Possibly heavy
     * Only checks up to 2 nearby tiles
     * @param tile
     * @return
     */
    public boolean checkTileThreat(HexagonTile3D tile) {

        for (HexagonTile3D nearOne : tile.neighbours) {
            if (nearOne != null) {

                for (HexagonTile3D nearTwo : nearOne.neighbours) {

                    if (nearTwo != null) {
                        if (nearTwo.pieceStack != null && nearTwo.pieceStack.pieces.size() != 0 && !nearTwo.pieceStack.getOwnerPiece().getColour().equals(colour)) {
                            //System.out.println("Player is " + colour);
                            if (nearTwo.pieceStack.checkReach(tile)) {
                                return true;
                            }
                        }
                    }
                }

                if (nearOne.pieceStack != null && nearOne.pieceStack.pieces.size() != 0 && !nearOne.pieceStack.getOwnerPiece().getColour().equals(colour)) {

                    //System.out.println("Player is " + colour);
                    if (nearOne.pieceStack.checkReach(tile)) {
                        return true;
                    }
                }
            }
        }

        return false;

    }


}
