package Oaker;

import org.osbot.rs07.api.Bank.BankMode;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import utils.Sleep;
import utils.SystemAlert;

import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@ScriptManifest(name = "Zzz Woodcutter", author = "Zor", version = 1.0, info = "Chops trees until oaks semi auto muling", logo = "")  //Helps the client understand and find the script we compile

public class Main extends Script {
	
	long timegui;
    private boolean isRunning = true;
    private Thread thread;
    private JFrame gui;
	int muleAt;
	int logsmuled;
	long startTime = -1;
	String getStatus = "Initializing";
	boolean axeEq = false;
	Area tree = new Area(3036, 3272, 3054, 3261);
	Area depoArea = new Area(3043, 3236, 3047, 3235);
	Area bankArea = new Area(3092, 3246,3093,3240);
	public State state;
	int startLevel = -1;
	int currentXP = -1;
	int currentLevel = -1;
	int oaklogscut;
    private boolean muling = false;
    private boolean notified = false;
    private String muleName;
	
	   private static enum State {
	        //LOGOUT,
	        IDLE,
	        WALKT,
	        CHOPL,
	        DROP,
	        MULE,
	        CHOPO,
	        BANK;
	        
	        private State() {
	        }
	    }
	   private State getState() {
	    	int wclev = skills.getStatic(Skill.WOODCUTTING);
	    	   if(oaklogscut >muleAt) {
	    		   muling = true;
	    		   return State.MULE;
	    	   }
			   if(!tree.contains(myPlayer()) && !inventory.isFull() && !myPlayer().isAnimating()) { 
				   return State.WALKT;
			   }
			   if(tree.contains(myPlayer()) && !inventory.isFull() && !myPlayer().isAnimating() && wclev < 15) {
				  return State.CHOPL; 
			   }
			   if(!myPlayer().isAnimating() && inventory.isFull() && inventory.contains("Logs")) {
				 return State.DROP;
			   }
			   if(tree.contains(myPlayer()) && !myPlayer().isAnimating() && wclev >= 15 && !inventory.isFull()) {
				   return State.CHOPO;
			   }
			   if(inventory.isFull() && !myPlayer().isAnimating() && wclev >= 15) {
				   return State.BANK;
			   }
			return State.IDLE;
		   }
	   
	   
	    public void bank() throws InterruptedException, IOException {
	        if (muling && !bankArea.contains(myPlayer())) {
	            getWalking().webWalk(bankArea);
	        }
	        bank.open();
	        if (!myPlayer().isMoving() && !bank.isOpen()) {
	            new ConditionalSleep((int)(Math.random() * 1000.0 + 3000.0)){

	                public boolean condition() throws InterruptedException {
	                    return Main.this.bank.isOpen();
	                }
	            }.sleep();
	        } else if (muling) {
	            bank.depositAll();
	            bank.enableMode(BankMode.WITHDRAW_NOTE);
	            bank.withdrawAll("Oak logs");
	        } else if (skills.getStatic(Skill.WOODCUTTING) > 14) {
	            bank.enableMode(BankMode.WITHDRAW_ITEM);
	            if (bank.contains(new String[]{"Bronze axe"})) {
	            	bank.withdraw("Bronze axe", 1);
	                bank.close();
	            } else {
	                mule();
	            }
	        }
	    }
	   
	    public void waitForTrade(String mule) throws InterruptedException, IOException {
	        getStatus = "Waiting for Player";
	        Player closest = (Player)getPlayers().closest(new String[]{mule});
	        if (!this.isTrading() && closest != null && closest.interact(new String[]{"Trade with"})) {
	            new ConditionalSleep((int)(Math.random() * 4000.0 + 6000.0)){

	                public boolean condition() throws InterruptedException {
	                    return Main.this.isTrading();
	                }
	            }.sleep();
	        }
	        Main.sleep((long)Main.random((int)5000, (int)8000));
	        if (isTrading() && inventory.contains(new String[]{"Oak logs"}) && trade.isFirstInterfaceOpen() && inventory.interact("Offer-All", new String[]{"Oak logs"})) {
	            new ConditionalSleep((int)(Math.random() * 4000.0 + 9000.0)){

	                public boolean condition() throws InterruptedException {
	                    return Main.this.isTrading();
	                }
	            }.sleep();
	            Main.sleep((long)Main.random((int)1000, (int)2500));
	        }
	        if (this.isTrading() && this.trade.acceptTrade()) {
	            new ConditionalSleep((int)(Math.random() * 4000.0 + 9000.0)){

	                public boolean condition() throws InterruptedException {
	                    return Main.this.trade.isSecondInterfaceOpen();
	                }
	            }.sleep();
	            Main.sleep((long)Main.random((int)1000, (int)2500));
	        }
	        if (isTrading() && this.trade.acceptTrade()) {
	            new ConditionalSleep((int)(Math.random() * 4000.0 + 9000.0)){

	                public boolean condition() throws InterruptedException {
	                    return !Main.this.isTrading();
	                }
	            }.sleep();
	            Main.sleep((long)Main.random((int)1000, (int)2500));
	            if (!inventory.contains(new String[]{"Oak logs"})) {
	                muling = false;
	                logsmuled = oaklogscut + logsmuled;
	                oaklogscut = 0;
	            }
	        }

	    }
	   
	   
	    public SystemAlert systemAlert(String string) {
	        SystemAlert sa = null;
	        try {
	            BufferedImage img = ImageIO.read(new URL("https://gyazo.com/ba33fdf73f6e2224eac4621276f2da4d.png"));
	            sa = new SystemAlert(400, 100, img, string, "World " + worlds.getCurrentWorld(), new Date());
	            this.makeSysAlertVisisble(sa);
	        }
	        catch (Exception e) {
	            this.logger.error((Object)e);
	        }
	        return sa;
	    }
	    
	    public void makeSysAlertVisisble(SystemAlert sa) {
	        sa.setVisible(true);
	    }
	   
	    public boolean isTrading() {
	        return trade.isCurrentlyTrading() || trade.isFirstInterfaceOpen() || trade.isSecondInterfaceOpen();
	    }
	   
	    public void mule() throws InterruptedException, IOException {
	        getStatus = "Muling";
	        muling = true;
	        bank();
			if (skills.getStatic(Skill.WOODCUTTING) > 14 && bankArea.contains(myPlayer()) && inventory.contains(new int[]{1522}) && !isTrading()) {
	            if (!notified) {
	                systemAlert("Oak Logs");
	                notified = true;
	            }
	            bank.close();
	            waitForTrade(muleName);
	        }
	    }
	   
	    private void gui() throws InterruptedException {
	        final JTextField textField = new JTextField(10);
	        SpinnerNumberModel numbers = new SpinnerNumberModel(250, 25, 1000, 25);
	        final JSpinner spinner = new JSpinner(numbers);
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        int gX = (int)(screenSize.getWidth() / 2.0) - 150;
	        int gY = (int)(screenSize.getHeight() / 2.0) - 40;
	        gui = new JFrame("Zzz Woodcutter");
	        gui.setBounds(gX, gY, 300, 80);
	        gui.setResizable(false);
	        JPanel panel = new JPanel();
	        JLabel label = new JLabel("Mule name: ");
	        JLabel label2 = new JLabel("Logs Cut Before Muling: ");
	        gui.add(panel);
	        textField.addActionListener(new ActionListener(){

	            @Override
	            public void actionPerformed(ActionEvent e) {
	                Main.this.muleName = textField.getText();
	            }
	        });
	        spinner.addChangeListener(new ChangeListener(){

	            @Override
	            public void stateChanged(ChangeEvent e) {
	                Main.this.muleAt = ((Integer)spinner.getValue()).intValue();
	            }
	        });
	        panel.add(label);
	        panel.add(textField);
	        panel.add(label2);
	        panel.add(spinner);
	        JButton startButton = new JButton("Start");
	        startButton.addActionListener(e -> {
	            muleName = textField.getText();
	            muleAt = ((Integer)spinner.getValue()).intValue();
	            gui.setVisible(false);
	            startTime = System.currentTimeMillis();
	            thread = new Thread(){

	                @Override
	                public synchronized void run() {
	                    while (Main.this.isRunning) {
	                        try {
	                            timegui = System.currentTimeMillis() - Main.this.startTime;
	                            this.wait(50);
	                        }
	                        catch (InterruptedException ex) {
	                            Main.this.log((Object)ex);
	                        }
	                    }
	                }
	            };
	            thread.start();
	        }
	        );
	        panel.add(startButton);
	        label.setForeground(Color.black);
	        gui.setVisible(true);
	        sleep(20000);
	    }
	   
	   
	   
	   
	@Override
    public void onMessage(Message m) {
		if(m.getMessage().contains("You get some oak logs")) oaklogscut++;
}
	
    
    
    @Override

    public void onStart() throws InterruptedException {
    	gui();
        /*try {
			BufferedImage paintZ = ImageIO.read(new URL("https://gyazo.com/df850cff9ac410cc3799059834044dd4.png"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        paintZ;*/

    }

    

    @Override

    public void onExit() {

        //Code here will execute after the script ends

    }

    @Override

    public int onLoop() throws InterruptedException{
    	state = this.getState();
        switch (state) {
		case BANK: {
			getStatus = "Banking";
			getWalking().webWalk(depoArea.getRandomPosition());
    		afk(); mouse.move(random(500), random(500));
    		if(!depoArea.contains(myPlayer().getPosition()))
    			walking.webWalk(depoArea.getRandomPosition());
    			

    			
    			while(!depositBox.isOpen()) {
    				
    				RS2Object depoBox = getObjects().closest("Bank deposit box");
    				if(depoBox != null) {
    					
    					depoBox.interact("Deposit");
    					mouse.move(random(500), random(500));
    					Sleep.sleepUntil(() -> depositBox.isOpen(), random(1000,2000));
    				}
    				
    			}
    			if(depositBox.isOpen() && !inventory.isEmptyExcept("Bronze axe")) {
    				depositBox.depositAllExcept("Bronze axe");
    			}
    			if(!inventory.isFull() && depositBox.isOpen()) {
    				
    				depositBox.close();
    				
    			}
		}
			break;
		case CHOPL: {
			getStatus = "Chopping Logs";
			RS2Object treetochop = objects.closest("Tree");
			if(treetochop != null) {
				treetochop.interact("Chop down");
				Sleep.sleepUntil(() -> myPlayer().isAnimating(), random(3000, 5000));
				afk();
			}
		}
			break;
		case CHOPO: {
			getStatus = "Chopping Oak Logs";
			RS2Object treetochop = objects.closest("Oak");
			if(treetochop != null) {
				treetochop.interact("Chop down");
				Sleep.sleepUntil(() -> myPlayer().isAnimating(), random(3000, 5000));
				afk();
			}
		}
			break;
		case DROP:{
			getStatus = "Dropping Logs";
			inventory.dropAllExcept("Bronze axe", "Oak logs");
			sleep(random(5000,10000));
		}
			break;
		case IDLE: {
			getStatus = "Afk";
			sleep(random(5000, 10000));
		}
			break;
		case WALKT: {
			getStatus = "Walking to Trees";
			walking.webWalk(tree);
			afk();
		}
			break;
		case MULE: {
			getStatus = "Muling";
			try {
				mule();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }


		}
        return 700; //The amount of time in milliseconds before the loop starts over
        }

    private void afk() throws InterruptedException {
    	sleep(random(2000,5000));
    }
    
    @Override

    public void onPaint(Graphics2D g) {
    	long timeElapsed = System.currentTimeMillis() - startTime;
		long second = (timeElapsed / 1000) % 60;
		long minute = (timeElapsed / (1000 * 60)) % 60;
		long hour = (timeElapsed / (1000 * 60 * 60)) % 24;
int levelsGained = currentLevel - startLevel;
		
		if(myPlayer().isOnScreen()) {
			
			if(startLevel == -1) {
				startLevel = skills.getStatic(Skill.WOODCUTTING);
			}
			
			currentXP = skills.getExperience(Skill.WOODCUTTING);
			currentLevel = skills.getStatic(Skill.WOODCUTTING);
			
		}
		

		g.setColor(new Color(61,213,255));
		g.drawString("Levels gained: " + levelsGained, 25, 30);
		g.drawString("Current level:" + skills.getStatic(Skill.WOODCUTTING), 25, 45);
		g.drawString("Logs Chopped:" + (oaklogscut), 25,60);
		g.drawString("Runtime: " + (hour + ":" + minute + ":" + second), 25, 75);
		g.drawString("Muling: "+ muling, 25, 90);
		g.drawString("Status: " + getStatus, 25, 105);
		if(logsmuled > 0) {
		g.drawString("Logs Muled" + logsmuled, 25, 120);
		}
    }

}
