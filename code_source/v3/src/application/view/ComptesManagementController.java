package application.view;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.ComptesManagement;
import application.tools.AlertUtilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.orm.LogToDatabase;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

public class ComptesManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private ComptesManagement cm;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private Client clientDesComptes;
	private ObservableList<CompteCourant> olCompteCourant;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, ComptesManagement _cm, DailyBankState _dbstate, Client client) {
		this.cm = _cm;
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.clientDesComptes = client;
		this.configure();
	}

	private void configure() {
		String info;

		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olCompteCourant = FXCollections.observableArrayList();
		this.lvComptes.setItems(this.olCompteCourant);
		this.lvComptes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvComptes.getFocusModel().focus(-1);
		this.lvComptes.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());

		info = this.clientDesComptes.nom + "  " + this.clientDesComptes.prenom + "  (id : "
				+ this.clientDesComptes.idNumCli + ")";
		this.lblInfosClient.setText(info);

		this.loadList();
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
	private ListView<CompteCourant> lvComptes;
	@FXML
	private Button btnVoirOpes;
	@FXML
	private Button btnModifierCompte;
	@FXML
	private Button btnSupprCompte;
	@FXML
	private Button btnNouveauCompte;
	@FXML
	private Button btnEmprunt;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	@FXML
	private void doVoirOperations() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			CompteCourant cpt = this.olCompteCourant.get(selectedIndice);
			this.cm.gererOperations(cpt);
		}
		this.loadList();
		this.validateComponentState();
	}

	@FXML
	private void doModifierCompte() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
        if (selectedIndice >= 0) {
            CompteCourant compteAmodif = this.olCompteCourant.get(selectedIndice);
            CompteCourant result;
            try {
                result = this.cm.modifierCompte(compteAmodif);
                if (result != null) {
                    this.olCompteCourant.set(selectedIndice, result);
                    this.loadList();
                }
            } catch (RowNotFoundOrTooManyRowsException e) {
                e.printStackTrace();
            } catch (DatabaseConnexionException e) {
                e.printStackTrace();
            }
        }
	}

	@FXML
	private void doSupprimerCompte() {
		if (this.lvComptes.getSelectionModel().getSelectedItem().solde != 0) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de clôture du compte", null, "Le solde du compte n'est pas à 0.",
					AlertType.WARNING);
		} else {
			int indexCompte = this.lvComptes.getSelectionModel().getSelectedIndex();
			if (indexCompte >= 0) {
	            CompteCourant compteASupr = this.olCompteCourant.get(indexCompte);
	                this.cm.supprimerCompte(compteASupr);
	                this.loadList();
	        }
		}
	}

	@FXML
	private void doNouveauCompte() {
		CompteCourant compte;
		compte = this.cm.creerCompte();
		if (compte != null) {
			this.loadList();
		}
	}

	private void loadList () {
		ArrayList<CompteCourant> listeCpt;
		listeCpt = this.cm.getComptesDunClient();
		this.olCompteCourant.clear();
		for (CompteCourant co : listeCpt) {
			this.olCompteCourant.add(co);
		}
	}

	@FXML
	private void doSimulation() {
		this.cm.faireSimulation();
	}
	
	private void validateComponentState() {

		if(this.clientDesComptes.estInactif.equals("O")) {
			this.btnNouveauCompte.setDisable(true);
		}
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			this.btnVoirOpes.setDisable(false);
			if (this.lvComptes.getSelectionModel().getSelectedItem().estCloture.equals("N")) {
				this.btnSupprCompte.setDisable(false);
				this.btnModifierCompte.setDisable(false);
			} else {
				this.btnSupprCompte.setDisable(true);
				this.btnModifierCompte.setDisable(true);
			}
		} else {
			this.btnVoirOpes.setDisable(true);
			this.btnSupprCompte.setDisable(true);
			this.btnModifierCompte.setDisable(true);
		}
	}
}