package application.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.PrelevementsManagement;
import application.tools.NoSelectionModel;
import application.tools.PairsOfValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Prelevement;

public class PrelevementsManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private PrelevementsManagement pm;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private Client clientDuCompte;
	private CompteCourant compteConcerne;
	private ObservableList<Prelevement> olPrelevement;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, PrelevementsManagement _pm, DailyBankState _dbstate, Client client,
			CompteCourant compte) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.pm = _pm;
		this.clientDuCompte = client;
		this.compteConcerne = compte;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olPrelevement = FXCollections.observableArrayList();
		this.lvPrelevements.setItems(this.olPrelevement);
		this.lvPrelevements.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvPrelevements.getFocusModel().focus(-1);
		this.lvPrelevements.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
		this.updateInfoCompteClient();
		this.validateComponentState();
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
	private Label lblInfosClient;
	@FXML
	private Label lblInfosCompte;
	@FXML
	private ListView<Prelevement> lvPrelevements;
	@FXML
	private Button btnAddPrev;
	@FXML
	private Button btnDelPrev;
	@FXML
	private Button btnExecute;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	@FXML
	private void doAddPrev() {
		Prelevement p = this.pm.addPrev();
		if (p != null) {
			this.updateInfoCompteClient();
			this.validateComponentState();
		}
	}

	@FXML
	private void doDelPrev() {
		int selectedIndice = this.lvPrelevements.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Prelevement p = this.olPrelevement.get(selectedIndice);
			this.pm.delPrev(p.idPrelevement);
			// this.olPrelevement.remove(selectedIndice);
			this.updateInfoCompteClient();
			this.validateComponentState();
		}
	}

	@FXML
	private void doPrelevements() {
		this.pm.gererPrelevements();
	}

	private void validateComponentState() {
		int selectedIndice = this.lvPrelevements.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			this.btnDelPrev.setDisable(false);

		} else {
			this.btnDelPrev.setDisable(true);
		}
	}

	private void updateInfoCompteClient() {
		PairsOfValue<CompteCourant, ArrayList<Prelevement>> opesEtCompte;

		opesEtCompte = this.pm.prelevementsEtCompte();

		ArrayList<Prelevement> listeP;
		this.compteConcerne = opesEtCompte.getLeft();
		listeP = opesEtCompte.getRight();

		String info;
		info = this.clientDuCompte.nom + "  " + this.clientDuCompte.prenom + "  (id : " + this.clientDuCompte.idNumCli
				+ ")";
		this.lblInfosClient.setText(info);

		info = "Cpt. : " + this.compteConcerne.idNumCompte + "  "
				+ String.format(Locale.ENGLISH, "%12.02f", this.compteConcerne.solde) + "  /  "
				+ String.format(Locale.ENGLISH, "%8d", this.compteConcerne.debitAutorise);
		this.lblInfosCompte.setText(info);

		this.olPrelevement.clear();
		for (Prelevement p : listeP) {
			this.olPrelevement.add(p);
		}

		this.validateComponentState();
	}
}