package application.tools;

import javafx.stage.Stage;

public class StageManagement {

	/*
	 * Centers primary Stage over its parent Stage
	 */
	public static void manageCenteringStage(Stage parent, Stage primary) {

		// Calculate the center position of the parent Stage
		double centerXPosition = parent.getX() + parent.getWidth() / 2d;
		double centerYPosition = parent.getY() + parent.getHeight() / 2d;

		// Hide the pop-up stage before it is shown and becomes relocated
		primary.setOnShowing(ev -> primary.hide());

		// Relocate the pop-up Stage
		primary.setOnShown(ev -> {
			primary.setX(centerXPosition - primary.getWidth() / 2d);
			primary.setY(centerYPosition - primary.getHeight() / 2d);
			primary.show();
		});
	}
}
