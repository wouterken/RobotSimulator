package simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Setup {
	public static void setup(){
		   // Create the world.
        final World world = new World();
        
        // Create main window.
        final JFrame frame = new JFrame("Java Robot Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the drawing area (double bufferred).
        final JPanel panel = new JPanel(true) {
                public Dimension getPreferredSize() {
                    return new Dimension(world.getWidth(), world.getHeight());
                }
                public void paint(Graphics g) {
                    world.drawWorld(g);
                }
            };

        // Create robot control panel for playing with the simulator.
        JPanel buttonPanel = new JPanel();
	//  buttonPanel.setLayout(new GridLayout(1, 0));

        JButton bLoadWorld = new JButton("Load World");
        buttonPanel.add(bLoadWorld);

        final JButton bChooseRobot = new JButton("Choose Robot");
        buttonPanel.add(bChooseRobot);

        JButton bRobotMove = new JButton("MOVE");
        buttonPanel.add(bRobotMove);
	final JTextField moveArg = new JTextField("10", 5);
        buttonPanel.add(moveArg);

        JButton bRobotTurn = new JButton("TURN");
        buttonPanel.add(bRobotTurn);
	final JTextField turnArg = new JTextField("45", 5);
        buttonPanel.add(turnArg);

        JButton bRobotPickUp = new JButton("PICKUP");
        buttonPanel.add(bRobotPickUp);

        JButton bRobotDrop = new JButton("DROP");
        buttonPanel.add(bRobotDrop);

        JButton bRobotTurnTowardsFirstThing = new JButton("Turn to Thing");
        buttonPanel.add(bRobotTurnTowardsFirstThing);
                    
        JButton bRobotTurnTowardsFirstBox = new JButton("Turn to Box");
        buttonPanel.add(bRobotTurnTowardsFirstBox);

        // Second panel for programs
        JPanel programButtonPanel = new JPanel();

        JButton bLoadProgram = new JButton("Load Program");
        programButtonPanel.add(bLoadProgram);
        
        JButton bExecuteProgram = new JButton("Execute Program");
	programButtonPanel.add(bExecuteProgram);


	final int[] currentRobot = new int[]{-1};  // container for the index of the current robot.

	// Button listeners
        bLoadWorld.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                    chooser.setFileFilter(new FileNameExtensionFilter("Text Files with Worlds", "world"));
                    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        if (world.loadWorld(chooser.getSelectedFile().getPath())) {
                            bChooseRobot.setText("Robot: NONE");
			    currentRobot[0]=-1;
                            frame.pack();
                            panel.repaint();
                        }
                    }
                }
            });
        
        bChooseRobot.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!world.validWorldLoaded()) {
                        JOptionPane.showMessageDialog(frame, "Cannot control robots, since no world is loaded!",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    ArrayList<Integer> robotIDs = world.getRobotIDs();
		    if (robotIDs.size()==1) {
			currentRobot[0] = robotIDs.get(0);
		    }
		    else {
			Object[] robots = new String[robotIDs.size()];
			for (int i = 0; i < robots.length; i++) {
			    robots[i] = "Robot: " + robotIDs.get(i);
			}
			String selectedRobot =
			    (String) JOptionPane.showInputDialog(frame,
								 "Select which robot you want to control:",
								 "Pick Robot ID",
								 JOptionPane.INFORMATION_MESSAGE,
								 null, robots, robots[0]);
			if (selectedRobot != null) {
			    currentRobot[0] = Integer.valueOf(selectedRobot.substring(7));
			}
		    }
		    bChooseRobot.setText("Robot: " + currentRobot[0]);
                }
            });

        bRobotMove.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    if (!checkWorld(world) || !checkRobot(currentRobot[0])) return;
                    String input = moveArg.getText();
                    try {
                        int move = Integer.valueOf(input);
			world.moveRobot(currentRobot[0], move, panel.getGraphics());
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(frame, "Cannot understand " + input + " as integer!",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

        bRobotTurn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!checkWorld(world) || !checkRobot(currentRobot[0])) return;
		    String input = turnArg.getText();
                    try {
                        int turn = Integer.valueOf(input);
			world.turnRobot(currentRobot[0], turn, panel.getGraphics());
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(frame, "Cannot understand " + input + " as integer!",
                                                      "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

        bRobotPickUp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!checkWorld(world) || !checkRobot(currentRobot[0])) return;
                    world.pickUp(currentRobot[0], panel.getGraphics());
                }
            });

        bRobotDrop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!checkWorld(world) || !checkRobot(currentRobot[0])) return;
                    world.drop(currentRobot[0], panel.getGraphics());
                }
            });


        bRobotTurnTowardsFirstThing.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!checkWorld(world) || !checkRobot(currentRobot[0])) return;
                    world.turnTowardsFirstThing(currentRobot[0], panel.getGraphics());
                }
            });

        bRobotTurnTowardsFirstBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!checkWorld(world) || !checkRobot(currentRobot[0])) return;
                    world.turnTowardsFirstBox(currentRobot[0], panel.getGraphics());
                }
            });
                    

        bLoadProgram.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!checkWorld(world)) return;

		    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		    chooser.setFileFilter(new FileNameExtensionFilter("Text Files with Programs",
								      "program"));
		    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
			int id = getRobotID(world, "load", world.getRobotIDs(), frame);
			world.loadProgram(chooser.getSelectedFile().getPath(), id,
					  panel.getGraphics());
		    }
		}
            });
	bExecuteProgram.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (!checkWorld(world)) return;
		int id = getRobotID(world, "execute", world.getRobotIDsWithValidProgramsLoaded(), frame);
		world.executeProgram(id, panel.getGraphics());
	    }
	});
        
    // Create a standard kind of menu.
    JMenuBar menuBar = new JMenuBar();
        
    JMenu menu = new JMenu("Menu");
    menu.setMnemonic(KeyEvent.VK_M);
    menu.getAccessibleContext().setAccessibleDescription("The only menu in this program");
    menuBar.add(menu);

    JMenuItem miExit = new JMenuItem("Exit", KeyEvent.VK_X);
    miExit.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		frame.dispose();
	    }
	});
    menu.add(miExit);

    // Put all the parts onto the form and pack it together.
    frame.setJMenuBar(menuBar);
    frame.getContentPane().add(buttonPanel, BorderLayout.NORTH);
    frame.getContentPane().add(programButtonPanel, BorderLayout.CENTER);
    frame.getContentPane().add(panel, BorderLayout.SOUTH);
    frame.pack();
    frame.setVisible(true);
	}
	 static boolean checkWorld(World world){
			if (!world.validWorldLoaded()) {
			    JOptionPane.showMessageDialog(null, "Cannot control robots, since no world is loaded!",
							  "Error", JOptionPane.ERROR_MESSAGE);
			    return false;
			}
			return true;
		    }

		    static boolean checkRobot(int id){
			if (id<0) {
			    JOptionPane.showMessageDialog(null, "Please select ROBOT using ROBOT button first!",
							  "Error", JOptionPane.ERROR_MESSAGE);
			    return false;
			}
			return true;
		    }


		    static int getRobotID(World world, String action, ArrayList<Integer> candIDs, JFrame frame){
			Object[] robots = new String[candIDs.size()];
			for (int i = 0; i < robots.length; i++) {
			    robots[i] = "Robot: " + candIDs.get(i);
			}
			String selectedRobot =
			    (String) JOptionPane.showInputDialog(frame,
								 "Select which robot to "+action+" a program for:",
								 "Pick Robot ID",
								 JOptionPane.INFORMATION_MESSAGE,
								 null, robots, robots[0]);
			if (selectedRobot != null) {
			    return Integer.valueOf(selectedRobot.substring(7));
			}
			return candIDs.get(0);
		    }

}
