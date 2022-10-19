package application.view;

import java.net.URL;
import java.util.ResourceBundle;

import application.DailyBankState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.CompteCourant;
import model.data.Prelevement;

public class PrelevementEditorPaneController implements Initializable {

	// Etat application
	private DailyBankState dbs;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private CompteCourant compteEdite;
	private Prelevement prelevementResultat;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}

	public Prelevement displayDialog(CompteCourant cpte) {
		this.compteEdite = cpte;

		this.prelevementResultat = null;

		this.primaryStage.showAndWait();
		return this.prelevementResultat;
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMontant;
	@FXML
	private Label lblBenef;
	@FXML
	private Label lblDate;
	@FXML
	private TextField txtMontant;
	@FXML
	private TextField txtBenef;
	@FXML
	private TextField txtDate;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.prelevementResultat = null;
		this.primaryStage.close();
	}

	@FXML
	private void doAjouter() {
		double montant;
		int dateRec;
		String benef = this.txtBenef.getText().trim();

		this.txtMontant.getStyleClass().remove("borderred");
		this.txtBenef.getStyleClass().remove("borderred");
		this.txtDate.getStyleClass().remove("borderred");
		this.lblMontant.getStyleClass().remove("borderred");
		this.lblBenef.getStyleClass().remove("borderred");
		this.lblDate.getStyleClass().remove("borderred");
		try {
			montant = Double.parseDouble(this.txtMontant.getText().trim());
			if (montant <= 0)
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			this.txtMontant.getStyleClass().add("borderred");
			this.lblMontant.getStyleClass().add("borderred");
			this.txtMontant.requestFocus();
			return;
		}
		try {
			dateRec = Integer.parseInt(this.txtDate.getText().trim());
			if (dateRec < 1 || dateRec > 28)
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			this.txtDate.getStyleClass().add("borderred");
			this.lblDate.getStyleClass().add("borderred");
			this.txtDate.requestFocus();
			return;
		}
		this.prelevementResultat = new Prelevement(-1, montant, dateRec, benef, this.compteEdite.idNumCompte);
		this.primaryStage.close();
	}
}