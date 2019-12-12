/**
 * 
 */
package com.pixelificgames.taoexgame.board;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.pixelificgames.taoexgame.GameObject;
import com.pixelificgames.taoexgame.piece.PieceStack;
import com.pixelificgames.taoexgame.piece.PieceStackState;
import com.pixelificgames.taoexgame.ui.GameMain;

/**
 * @author Davis
 *
 */
public class HexagonTile3D {
	
    // cord center
    static int centerCordX = 14, centerCordY = 14;
    
    public int type;
    public int extraType;
    ModelInstance tileInstance;
    
    static float faceRadius = new Double(1f * Math.cos(30.0 * (Math.PI/180.0))).floatValue(); // for y
    static float edgeRadius = 1f; // for x
    
    public float x, z;
    public HexagonTile3D[] neighbours = new HexagonTile3D[6];
    
    int cordX, cordY;

    public PieceStack pieceStack;
    public GameObject instanceRef;
    public boolean highlighted;

    public boolean hookHighlight;

    public static PieceStack selectedPieceStack;
	
	public HexagonTile3D(float x, float y, int type) {
    	// cords w/ 0,0 at middle
    	cordX = Math.round(x) - centerCordX;
    	cordY = Math.round(y) - centerCordY;
        
        // tile type
        this.type = type;
        
        // for x position
        this.x = cordX * edgeRadius * 1.5f;
        
        // for y position
        this.z = cordY * faceRadius * 2.0f;
        this.z -= cordX * faceRadius;
    }
	
	/**
	 * Does not dispose the old instance
	 * @param tileInstance
	 */
	public void setModelInstance(ModelInstance tileInstance) {
		this.tileInstance = tileInstance;
		tileInstance.transform.translate(x, 0, z);
	}

	public void updateColour(Attribute newColour) {
	    instanceRef.materials.get(0).set(newColour);
    }

    public HexagonTile3D[] getAdjacentWayTiles() {
        HexagonTile3D[] adjacentWays = new HexagonTile3D[6];
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i] == null || neighbours[i].neighbours[i] == null) {
                continue;
            }
            if (neighbours[i].type == 2 && neighbours[i].neighbours[i].type != 0) {
                adjacentWays[i] = neighbours[i];
            }
        }
        return adjacentWays;
    }

    public boolean checkIfWayNeighbour(HexagonTile3D tileToCheck) {
        for (HexagonTile3D h : neighbours) {
            if (tileToCheck == h && h.type == 2) {
                return true;
            }
        }
        return false;
    }

    public void resetStates() {
        //wayMove = false;
        selectedPieceStack = null;
        //Message.MsgHandler.getMsgManager().dispatchMessage(this, null, MessageType.UNSELECT);
        //selected = false;
    }

    /**
     * handles cases for when the user clicks on this
     */
    public void selected() {

    	// non move related click so reset.
    	if (!highlighted && pieceStack == null) {
    		if (selectedPieceStack != null && selectedPieceStack.state != PieceStackState.WAY) {
    			selectedPieceStack.unhighlightAvailableMoves(selectedPieceStack.getAllAvailableMoves());
    			selectedPieceStack = null;
    		}
    		return;
    	}
    	
    	// no current selection
    	if (selectedPieceStack == null) {
    		// select this piece if ur turn
    		if (pieceStack != null && pieceStack.player == GameMain.gameSession.getCurrentPlayer()) {
    			selectedPieceStack = pieceStack;
    			selectedPieceStack.showMoves();
    		}
    		return;
    	}
    	
    	if (highlighted) {
    		
    		// way rolled c and stay case
    		if (pieceStack == selectedPieceStack) {
    		    if (pieceStack.state == PieceStackState.WAY) {
                    selectedPieceStack.unhighlightAvailableMoves(selectedPieceStack.getAllWayMoves());
                    selectedPieceStack = null;
                    pieceStack.move(this);
                    return;
                } else if (pieceStack.state == PieceStackState.NORMAL) {
    		        selectedPieceStack.move(this);
                }
    			
    		} else if (pieceStack != null) { // attack move
    			if (selectedPieceStack.state == PieceStackState.WAY && selectedPieceStack.player != pieceStack.player) {
    			    //Shouldn't be possible but just in case
    			    if (pieceStack.pieces.size() == 0) {
    			        selectedPieceStack.move(this);
    			        return;
                    } else {
                        selectedPieceStack.attackPiece(pieceStack);
                        selectedPieceStack = null;
//                        GameMain.gameSession.nextTurn();
                    }
    			} else {
                    selectedPieceStack.attackPiece(pieceStack);
                    if (selectedPieceStack.state != PieceStackState.WAY) {
                        selectedPieceStack = null;
//                        GameMain.gameSession.nextTurn();
                    }
                }
    			
    			//selectedPieceStack.state = PieceStackState.NORMAL;
    			
    		} else { // normal move
    			selectedPieceStack.move(this);
    			if (selectedPieceStack.state == PieceStackState.NORMAL) {
                    selectedPieceStack = null;
//                    GameMain.gameSession.nextTurn();
                }
    		}

    	}
    }

}
