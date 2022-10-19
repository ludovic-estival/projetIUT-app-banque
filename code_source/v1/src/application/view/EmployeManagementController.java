package application.view;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import application.DailyBankState;
import application.control.ClientsManagement;
import application.control.EmployeManagement;
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
import model.data.Employe;
import model.orm.LogToDatabase;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

public class EmployeManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private EmployeManagement em;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<Employe> ole;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, EmployeManagement _em, DailyBankState _dbstate) {
		this.em = _em;
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
		this.ole = FXCollections.observableArrayList();
		this.lvEmploye.setItems(this.ole);
		this.lvEmploye.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvEmploye.getFocusModel().focus(-1);
		this.lvEmploye.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
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
	private TextField txtID;
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private ListView<Employe> lvEmploye;
	@FXML
	private Button btnCreerEmploye;
	@FXML
	private Button btnModifierEmploye;
	@FXML
	private Button btnSupprimerEmploye;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	
	/**	Permet de rechercher un employé.
	 * On peut rechercher tous les employés ou filtrer avec le nom, prénom et l'identifiant
	 */
	@FXML
	private void doRechercher() {
		int numCompte;
		try {
			String nc = this.txtID.getText();
			if (nc.equals("")) {
				numCompte = -1;
			} else {
				numCompte = Integer.parseInt(nc);
				if (numCompte < 0) {
					this.txtID.setText("");
					numCompte = -1;
				}
			}
		} catch (NumberFormatException nfe) {
			this.txtID.setText("");
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

		// Recherche des employés en BD. cf. AccessEmploye > getEmployes
		// numCompte != -1 => recherche sur numCompte
		// numCompte != -1 et debutNom non vide => recherche nom/prenom
		// numCompte != -1 et debutNom vide => recherche tous les employés
		ArrayList<Employe> listeCli;
		listeCli = this.em.getlisteEmployes(numCompte, debutNom, debutPrenom);

		this.ole.clear();
		for (Employe cli : listeCli) {
			this.ole.add(cli);
		}

		this.validateComponentState();
	}
	
	/** Permet de créer un employé
	 * 
	 */
	@FXML
	private void doCreer() {
		Employe emp;
		
		emp = this.em.nouveauEmploye();
		if (emp != null) {
			this.ole.add(emp);
		}
	}
	
	/** Permet de modifier un employé
	 * 	
	 */
	@FXML
	private void doModifier() {
		int selectedIndice = this.lvEmploye.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Employe cliMod = this.ole.get(selectedIndice);
			Employe result = this.em.modifierEmploye(cliMod);
			if (result != null) {
				this.ole.set(selectedIndice, result);
			}
	}
	}
	
	/** Permet de supprimer un employé après confirmation
	 * 
	 */
	@FXML
	private void doSupprimer() {
		if (AlertUtilities.confirmYesCancel(this.primaryStage, "Supprimer un employé", "Etes-vous sûr de vouloir supprimer cet employé ?", null, AlertType.CONFIRMATION)) {
			this.validerSupprimerEmp();
		}
	}
	
	
	/** Supprime l'employé sélectionné de la base de données
	 * 
	 */
	private void validerSupprimerEmp() {
		
		// On récupère le numéro du compte
		int numeroCompte = this.lvEmploye.getSelectionModel().getSelectedItem().idEmploye;
		
		try {
			// On établit une connexion avec la base de données
			Connection con = LogToDatabase.getConnexion();
			
			// On "prépare" la requête à executer dans sur la BD
			String query = "DELETE FROM EMPLOYE WHERE idEmploye = ?";
			
			//On remplace le "?" par le numéro du compte
			PreparedStatement pst;
					try {
						pst = con.prepareStatement(query);
						pst.setInt(1, numeroCompte);
						
						// On récupère le résultat pour vérifier si l'insert à fonctionner
						int result = pst.executeUpdate();
						
						if (result != 1) {
						    // 0 ou plus de 1 delete => anormal
						        con.rollback();
						        throw new RowNotFoundOrTooManyRowsException(Table.Employe, Order.DELETE,
				                        "Delete anormal", null, result);
						} else {
						    con.commit();
						    this.loadList();
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RowNotFoundOrTooManyRowsException e) {
						e.printStackTrace();
					}
			
		} catch (DatabaseConnexionException e) {
			e.printStackTrace();
		}
	}
	
	private void loadList () {
		ArrayList<Employe> listeEmp;
		listeEmp = this.em.getlisteEmployes(-1, "","");
		this.ole.clear();
		for (Employe element : listeEmp) {
			this.ole.add(element);
		}
	}
	
	
	private void validateComponentState() {
		this.btnModifierEmploye.setDisable(true);
		this.btnSupprimerEmploye.setDisable(true);
		
		int selectedIndice = this.lvEmploye.getSelectionModel().getSelectedIndex();
		
		if (selectedIndice >= 0) {
			this.btnModifierEmploye.setDisable(false);
			this.btnSupprimerEmploye.setDisable(false);
		} else {
			this.btnModifierEmploye.setDisable(true);
			this.btnSupprimerEmploye.setDisable(true);
		}
	}

	
}