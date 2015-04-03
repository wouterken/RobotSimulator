package simulator;
import java.awt.Graphics;

/**
 * A wrapper for all undo actions for use in the world class.
 * @author wouterken
 *
 */
public abstract class UndoAction {
	protected World world;

	public UndoAction(World world) {
		this.world = world;
	}

	public abstract void execute(int robotID, Graphics g);
}
