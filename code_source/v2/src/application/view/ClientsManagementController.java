package application.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.ClientsManagement;
import application.control.ExceptionDialog;
import application.tools.AlertUtilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.orm.AccessClient;
import model.orm.AccessCompteCourant;
import model.orm.AccessEmploye;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class ClientsManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private ClientsManagement cm;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<Client> olc;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, ClientsManagement _cm, DailyBankState _dbstate) {
		this.cm = _cm;
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olc = FXCollections.observableArrayList();
		this.lvClients.setItems(this.olc);
		this.lvClients.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvClients.getFocusModel().focus(-1);
		this.lvClients.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
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
	private TextField txtNum;
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private ListView<Client> lvClients;
	@FXML
	private Button btnDesactClient;
	@FXML
	private Button btnModifClient;
	@FXML
	private Button btnComptesClient;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	@FXML
	private void doRechercher() {
		int numCompte;
		try {
			String nc = this.txtNum.getText();
			if (nc.equals("")) {
				numCompte = -1;
			} else {
				numCompte = Integer.parseInt(nc);
				if (numCompte < 0) {
					this.txtNum.setText("");
					numCompte = -1;
				}
			}
		} catch (NumberFormatException nfe) {
			this.txtNum.setText("");
			numCompte = -1;
		}

		String debutNom = this.txtNom.getText();
		String debutPrenom = this.txtPrenom.getText();

		if (numCompte != -1) {
			this.txtNom.setText("");
			this.txtPrenom.setText("");
		} else {
			if (debutNom.equals("") && !debutPrenom.equals("")) {
				this.txtPrenom.setText("");
			}
		}

		// Recherche des clients en BD. cf. AccessClient > getClients(.)
		// numCompte != -1 => recherche sur numCompte
		// numCompte != -1 et debutNom non vide => recherche nom/prenom
		// numCompte != -1 et debutNom vide => recherche tous les clients
		ArrayList<Client> listeCli;
		listeCli = this.cm.getlisteComptes(numCompte, debutNom, debutPrenom);

		this.olc.clear();
		for (Client cli : listeCli) {
			this.olc.add(cli);
		}

		this.validateComponentState();
	}

	private void loadList() {
		ArrayList<Client> listeCli;
		listeCli = this.cm.getlisteComptes(-1, "", "");

		this.olc.clear();
		for (Client cli : listeCli) {
			this.olc.add(cli);
		}
	}
	
	@FXML
	private void doComptesClient() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client client = this.olc.get(selectedIndice);
			this.cm.gererComptesClient(client);
		}
	}

	@FXML
	private void doModifierClient() {

		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client cliMod = this.olc.get(selectedIndice);
			Client result = this.cm.modifierClient(cliMod);
			if (result != null) {
				this.olc.set(selectedIndice, result);
			}
		}
	}

	@FXML
	private void doDesactiverClient() {
		//On cherche à savoir si tous les comptes sont cloturés
		ArrayList<CompteCourant> listComptes= getComptesDunClient(this.lvClients.getSelectionModel().getSelectedItem());
		boolean isCloture = true;
		for(CompteCourant compte : listComptes) {
			if (compte.estCloture.equals("N")) {
				isCloture = false;
			}
		}
		
		if (isCloture == false) {
			
			AlertUtilities.showAlert(this.primaryStage, "Erreur de désactivation du client", null, "Les comptes du client ne sont pas cloturés.",
					AlertType.WARNING);
			
			} else {
		
			int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
			if (selectedIndice >= 0) {
				Client cliMod = this.olc.get(selectedIndice);
				Client result = this.cm.supprimerClient(cliMod);
				if (result != null) {
					this.loadList();
			}
		}
	 }
	}

	@FXML
	private void doNouveauClient() {
		Client client;
		client = this.cm.nouveauClient();
		if (client != null) {
			this.olc.add(client);
		}
	}

	public ArrayList<CompteCourant> getComptesDunClient(Client client) {
		ArrayList<CompteCourant> listeCpt = new ArrayList<>();

		try {
			AccessCompteCourant acc = new AccessCompteCourant();
			listeCpt = acc.getCompteCourants(client.idNumCli);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeCpt = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeCpt = new ArrayList<>();
		}
		return listeCpt;
	}
	
	private void validateComponentState() {
		
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			this.btnModifClient.setDisable(false);
			this.btnComptesClient.setDisable(false);
			if(this.dbs.isChefDAgence()) {
				if(this.lvClients.getSelectionModel().getSelectedItem().estInactif.equals("O")) {
					this.btnDesactClient.setDisable(true);
					this.btnModifClient.setDisable(true);
				} else {
					this.btnDesactClient.setDisable(false);
				}
			} else {
				this.btnDesactClient.setDisable(true);
				if(this.lvClients.getSelectionModel().getSelectedItem().estInactif.equals("O")) {
					this.btnModifClient.setDisable(true);
				}
			}
		} else {
			this.btnModifClient.setDisable(true);
			this.btnComptesClient.setDisable(true);
			this.btnDesactClient.setDisable(true);
		}
	}
}