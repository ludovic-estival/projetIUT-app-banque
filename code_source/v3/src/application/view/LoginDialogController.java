package application.view;

import java.net.URL;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.LoginDialog;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Employe;

public class LoginDialogController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private LoginDialog ld;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, LoginDialog _ld, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.ld = _ld;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}

	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private TextField txtLogin;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private Label lblMessage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.dbs.setEmpAct(null);
		this.primaryStage.close();
	}

	@FXML
	private void doOK() {
		String login = this.txtLogin.getText().trim();
		String password = new String(this.txtPassword.getText().trim());
		if (login.length() == 0 || password.length() == 0) {
			this.afficheErreur("Identifiants incorrects :");
		} else {
			Employe e;
			e = this.ld.chercherParLogin(login, password);
			if (e == null) {
				this.afficheErreur("Identifiants incorrects :");
			} else {
				this.dbs.setEmpAct(e);
				this.primaryStage.close();
			}
		}
	}

	private void afficheErreur(String texte) {
		this.lblMessage.setText(texte);
		this.lblMessage.setStyle("-fx-text-fill:red; -fx-font-weight: bold;");
	}
}
