package com.pixelificgames.taoexgame.piece;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.pixelificgames.taoexgame.AssetLoader;
import com.pixelificgames.taoexgame.GameObject;
import com.pixelificgames.taoexgame.board.HexagonDirection;

public class HexagonPiece {
	
	private static final String TEXTURE_PATH = "test/models/pieces texture/";

    //direction that this HexagonPiece can move
    public HexagonDirection direction;
    public HexagonDirection capturedDirection;

    //how far this HexagonPiece can move
    public int range;
    public int capturedRange;
    
    String colour;

    public boolean captured;
    boolean dead;
    
    // 3d model
    GameObject model;
    boolean flipped;
    
    public boolean hook;

    /**
     * Constructor
     * @param direction the default direction of this HexagonPiece
     * @param capturedDirection the captured direction of this HexagonPiece
     * @param range the default range of this HexagonPiece
     * @param capturedRange the captured range of this HexagonPiece
     * @param model the 3d model instances associated with this pieceStack
     */
    public HexagonPiece(HexagonDirection direction, HexagonDirection capturedDirection, int range, int capturedRange, String colour, GameObject model) {
        this.direction = direction;
        this.capturedDirection = capturedDirection;
        this.range = range;
        this.capturedRange = capturedRange;
        this.colour = colour;
        this.model = model;
    }
    
    /**
     * Constructor for a hook pieceStack
     * @param direction direction of the hook pieceStack
     * @param model model for the hook
     */
    public HexagonPiece(HexagonDirection direction, GameObject model) {
    	this.direction = direction;
    	capturedDirection = direction;
    	range = 1;
    	capturedRange = 1;
    	colour = "hook";
    	this.model = model;
    	hook = true;
    }

    /**
     * Returns the actual direction of this HexagonPiece.
     * @return the actual direction of this HexagonPiece.
     */
    public HexagonDirection getDirection() {
        if (captured) {
            return capturedDirection;
        }
        return direction;
    }

    /**
     * Returns the actual range of this HexagonPiece.
     * @return the actual range of this HexagonPiece.
     */
    public int getRange() {
        if (captured) {
            return capturedRange;
        }
        return range;
    }
    
    
    // applies the texture to the model face
    public void applyTexture() {
		BlendingAttribute blend = new BlendingAttribute(1f);
		
		String texturePath = TEXTURE_PATH + colour + "-" + direction.text + range + ".png";
		TextureAttribute texture = new TextureAttribute(TextureAttribute.Diffuse, AssetLoader.assets.get(texturePath, Texture.class));
		
		NodePart baseNode = model.nodes.get(0).parts.get(0);
		NodePart textureNode = new NodePart(baseNode.meshPart, new Material());
		
		model.nodes.get(0).parts.add(textureNode);

		textureNode.material.set(texture);
		textureNode.material.set(blend);
		
		// flip texture of pieceStack
		if (!hook) {

			String texturePathF = TEXTURE_PATH + colour + "-" + direction.text + range + "f.png";
			TextureAttribute textureFlip = new TextureAttribute(TextureAttribute.Diffuse, AssetLoader.assets.get(texturePathF, Texture.class));
			NodePart baseNodeF = model.nodes.get(0).parts.get(1);
			NodePart textureNodeF = new NodePart(baseNodeF.meshPart, new Material());
			
			model.nodes.get(0).parts.add(textureNodeF);
	
			textureNodeF.material.set(textureFlip);
			textureNodeF.material.set(blend);
		} else {
			NodePart baseNodeF = model.nodes.get(0).parts.get(1);
			NodePart textureNodeF = new NodePart(baseNodeF.meshPart, new Material());
			
			model.nodes.get(0).parts.add(textureNodeF);
	
			textureNodeF.material.set(texture);
			textureNodeF.material.set(blend);
		}
    }
    
    public void flipTextures(boolean unflip) {
    	
    	if (hook) {
    		return;
    	}
    	
		Texture texture = AssetLoader.assets.get(TEXTURE_PATH + colour + "-" + direction.text + range+ ".png", Texture.class);
		Sprite sprite = new Sprite(texture);

		if (unflip) {
			sprite.flip(false, false);
		} else {
			sprite.flip(true, true);

		}

		TextureAttribute textureAttr = new TextureAttribute(TextureAttribute.Diffuse, sprite);
		
		NodePart node = model.nodes.get(0).parts.get(4);
		node.material.remove(TextureAttribute.Diffuse);
		node.material.set(textureAttr);

		HexagonDirection hd;
		if (getDirection() == direction) {
			hd = capturedDirection;
		} else {
			hd = direction;
		}
		int r;
		if (getRange() == range) {
			r = capturedRange;
		} else {
			r = range;
		}
		Texture texturef = AssetLoader.assets.get(TEXTURE_PATH + colour + "-" + hd.text + r + "f.png", Texture.class);
		Sprite spritef = new Sprite(texturef);

		if (unflip) {
			spritef.flip(false, false);
		} else {
			spritef.flip(true, true);
		}
		
		TextureAttribute textureAttrf = new TextureAttribute(TextureAttribute.Diffuse, spritef);
		
		NodePart nodef = model.nodes.get(0).parts.get(3);
		nodef.material.remove(TextureAttribute.Diffuse);
		nodef.material.set(textureAttrf);

    }

	/**
	 * @return the model
	 */
	public GameObject getModel() {
		return model;
	}

	/**
	 * @return the colour
	 */
	public String getColour() {
		return colour;
	}

	/**
	 * @return the flipped
	 */
	public boolean isFlipped() {
		return flipped;
	}

	/**
	 * @param flipped the flipped to set
	 */
	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}

	/**
	 * @return the capturedDirection
	 */
	public HexagonDirection getCapturedDirection() {
		return capturedDirection;
	}

	/**
	 * @return the capturedRange
	 */
	public int getCapturedRange() {
		return capturedRange;
	}

	/**
	 * @return the dead
	 */
	public boolean isDead() {
		return dead;
	}

	/**
	 * @param dead the dead to set
	 */
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	
	
	
	
    
    
}
