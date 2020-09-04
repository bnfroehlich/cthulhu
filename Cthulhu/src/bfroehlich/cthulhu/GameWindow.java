package bfroehlich.cthulhu;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public abstract class GameWindow extends JFrame {
	
	protected Networker networker;
	
	public GameWindow(String title, Networker networker) {
		super(title);
		
		this.networker = networker;
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		JFrame thiss = this;
		addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                int confirm = JOptionPane.showConfirmDialog(thiss, "Quit the game?", "Confirm", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION) {
                	networker.gameWindowClosed();
                	e.getWindow().dispose();
                }
            }
        });
	}
	
	public abstract void handleMessage(String message);
	
}