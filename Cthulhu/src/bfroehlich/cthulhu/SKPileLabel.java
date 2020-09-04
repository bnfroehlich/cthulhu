package bfroehlich.cthulhu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class SKPileLabel extends JLabel {
	
	private Direction direction;
	
	public SKPileLabel(ArrayList<? extends SKGamePiece> pieces, Direction direction, int maxLength, double minOverlapRatio) {
		this.direction = direction;
		init(pieces, maxLength, minOverlapRatio);
	}

	private void init(ArrayList<? extends SKGamePiece> pieces, int maxLength, double minOverlapRatio) {
		
		if(pieces.isEmpty()) {
			setIcon(null);
			return;
		}
		
		if(maxLength == -1) {
			maxLength = Integer.MAX_VALUE;
		}
		
		int totalPieceLength = 0;
		int maxPieceW = 0;
		int maxPieceH = 0;
		for(int i = 0; i < pieces.size(); i++) {
			SKGamePiece piece = pieces.get(i);
			int pieceW = Main.toBufferedImage(piece.getImage()).getWidth(this);
			if(pieceW > maxPieceW) {
				maxPieceW = pieceW;
			}
			int pieceH = Main.toBufferedImage(piece.getImage()).getHeight(this);
			if(pieceH > maxPieceH) {
				maxPieceH = pieceH;
			}
			
		    if(direction == Direction.East || direction == Direction.West) {
		    	int increment = pieceW;
		    	if(i < pieces.size()-1) {
		    		increment -= ((double) pieceW)*minOverlapRatio;
		    	}
		    	totalPieceLength += increment;
		    }
		    else if(direction == Direction.North || direction == Direction.South) {
		    	int increment = pieceH;
		    	if(i < pieces.size()-1) {
		    		increment -= ((double) pieceH)*minOverlapRatio;
		    	}
		    	totalPieceLength += increment;
		    }
		}
		
		int overlap = 0;
    	if(totalPieceLength > maxLength) {
    		overlap = (totalPieceLength - maxLength)/pieces.size() + 4;
    		//System.out.println("totalpiecelength: " + totalPieceLength + ", length: " + maxLength + ", overlap: " + overlap + ", pieces: " + pieces.size());
    	}
		
	    int stackW = maxPieceW;
	    int stackH = maxPieceH;

	    if(direction == Direction.East || direction == Direction.West) {
	    	stackW = Math.min(maxLength, totalPieceLength);
	    }
	    else if(direction == Direction.North || direction == Direction.South) {
	    	stackH = Math.min(maxLength, totalPieceLength);
	    }
	
	    //System.out.println("Pilesize: " + stackW + ", " + stackH);
	    final BufferedImage finalImage = new BufferedImage(stackW, stackH,
	        BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = finalImage.createGraphics();
	    
	    //backtrack to the starting point
	    if(direction == Direction.North) {
	    	g.translate(0, -stackH);
	    }
	    else if(direction == Direction.West) {
	    	g.translate(-stackW, 0);
	    }
	    
	    for(int i = 0; i < pieces.size(); i++) {
	    	SKGamePiece piece = pieces.get(i);
		    g.drawImage(piece.getImage(), 0, 0, null);
		    
		    if(i < pieces.size() - 1) {
		    	SKGamePiece nextPiece = pieces.get(i+1);

		    	int imgWidth = piece.getImage().getWidth(this);
		    	int imgHeight = piece.getImage().getHeight(this);
		    	//move forward
			    g.translate(direction.getPointer().x*imgWidth, direction.getPointer().y*imgHeight);
			    
		    	//move back slightly to overlap for stacking
			    g.translate(Math.max(overlap, ((double) imgWidth)*minOverlapRatio)*direction.getPointer().x*(-1), Math.max(overlap, ((double) imgHeight)*minOverlapRatio)*direction.getPointer().y*(-1));
		    }
	    }
	    //g.drawImage(newPiece.getImage(), 0, 0, null);
	    g.dispose();
	    
	    setIcon(new ImageIcon(finalImage));
	}
}