package application.view;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import application.DailyBankState;
import application.control.ExceptionDialog;
import application.tools.AlertUtilities;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.Employe;
import model.orm.exception.ApplicationException;
import model.orm.exception.Order;
import model.orm.exception.Table;

public class EmployeEditorPaneController implements Initializable {

	// Etat application
	private DailyBankState dbs;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private Employe empEdite;
	private EditionMode em;
	private Employe empResult;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}

	/** Affiche la fenêtre d'édition d'un employé
	 * @param emp l'employé concerné
	 * @param mode le mode: creation ou modification
	 * @return un employé modifié ou crée
	 */
	public Employe displayDialog(Employe emp, EditionMode mode) {

		this.em = mode;
		if (emp == null) {
			this.empEdite = new Employe(0, "", "", "", "", "", this.dbs.getEmpAct().idAg);
		} else {
			this.empEdite = new Employe(emp);
		}
		this.empResult = null;
		switch (mode) {
		
		case CREATION:
			
			this.txtId.setDisable(true);
			this.txtNom.setDisable(false);
			this.txtPrenom.setDisable(false);
			
			this.txtDroit.setDisable(false);
			this.txtLogin.setDisable(false);
			this.txtMdP.setDisable(false);
			this.lblMessage.setText("Informations sur l'employé à créer");
			
			this.txtAgence.setDisable(true);
			this.butOk.setText("Ajouter");
			
			this.butCancel.setText("Annuler");
			break;
			
		case MODIFICATION:
			this.txtId.setDisable(true);
			this.txtNom.setDisable(false);
			this.txtPrenom.setDisable(false);
			this.txtDroit.setDisable(false);
			this.txtLogin.setDisable(false);
			this.txtMdP.setDisable(false);
			
			this.txtAgence.setDisable(true);
			
			this.lblMessage.setText("Informations employé");
			this.butOk.setText("Modifier");
			this.butCancel.setText("Annuler");
			break;
		
		/*	
		case SUPPRESSION:
			
		
			ApplicationException ae = new ApplicationException(Table.NONE, Order.OTHER, "SUPPRESSION CLIENT NON PREVUE",
					null);
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			break;*/
			
		}
		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dbs.getEmpAct())) {
			// rien pour l'instant
		}
		// initialisation du contenu des champs
		
		this.txtId.setText("" + this.empEdite.idEmploye);
		this.txtNom.setText(this.empEdite.nom);
		this.txtPrenom.setText(this.empEdite.prenom);
		this.txtDroit.setText(this.empEdite.droitsAccess);
		this.txtLogin.setText(this.empEdite.login);
		this.txtMdP.setText(this.empEdite.motPasse);
		this.txtAgence.setText(Integer.toString(this.empEdite.idAg));

		this.empResult = null;

		this.primaryStage.showAndWait();
		return this.empResult;
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private TextField txtId;
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private TextField txtDroit;
	@FXML
	private TextField txtLogin;
	@FXML
	private TextField txtMdP;
	@FXML
	private TextField txtAgence;
	@FXML
	private Button butOk;
	@FXML
	private Button butCancel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	/** Annuler l'édition d'un employé
	 * 
	 */
	@FXML
	private void doCancel() {
		this.empResult = null;
		this.primaryStage.close();
	}

	/** Valider l'édition d'un employé
	 * 
	 */
	@FXML
	private void doAjouter() {
		switch (this.em) {
		case CREATION:
			if (this.isSaisieValide()) {
				this.empResult = this.empEdite;
				this.primaryStage.close();
			}
			break;
		case MODIFICATION:
			if (this.isSaisieValide()) {
				this.empResult = this.empEdite;
				this.primaryStage.close();
			}
			break;
			
		case SUPPRESSION:
			this.empResult = this.empEdite;
			this.primaryStage.close();
			break;
		}

	}

	
	private boolean isSaisieValide() {
		this.empEdite.nom = this.txtNom.getText().trim();
		this.empEdite.prenom = this.txtPrenom.getText().trim();
		this.empEdite.droitsAccess = this.txtDroit.getText().trim();
		this.empEdite.login = this.txtLogin.getText().trim();
		this.empEdite.motPasse = this.txtMdP.getText().trim();
		
		if (this.empEdite.nom.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le nom ne doit pas être vide",
					AlertType.WARNING);
			this.txtNom.requestFocus();
			return false;
		}
		if (this.empEdite.prenom.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le prénom ne doit pas être vide",
					AlertType.WARNING);
			this.txtPrenom.requestFocus();
			return false;
		}
		if (!this.empEdite.droitsAccess.equals("guichetier") && !this.empEdite.droitsAccess.equals("chefAgence")) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Droit d'accès : guichetier ou chefAgence",
					AlertType.WARNING);
			this.txtDroit.requestFocus();
			return false;
		}
		
		return true;
	}
}