package application.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ClientsManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.orm.AccessClient;
import model.orm.LogToDatabase;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

public class ClientsManagement {

	private Stage primaryStage;
	private DailyBankState dbs;
	private ClientsManagementController cmc;

	public ClientsManagement(Stage _parentStage, DailyBankState _dbstate) {
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(ClientsManagementController.class.getResource("clientsmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+50, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des clients");
			this.primaryStage.setResizable(false);

			this.cmc = loader.getController();
			this.cmc.initContext(this.primaryStage, this, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doClientManagementDialog() {
		this.cmc.displayDialog();
	}

	public Client modifierClient(Client c) {
		ClientEditorPane cep = new ClientEditorPane(this.primaryStage, this.dbs);
		Client result = cep.doClientEditorDialog(c, EditionMode.MODIFICATION);
		if (result != null) {
			try {
				AccessClient ac = new AccessClient();
				ac.updateClient(result);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				result = null;
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				result = null;
			}
		}
		return result;
	}
	
	public Client supprimerClient(Client c) {
		ClientEditorPane cep = new ClientEditorPane(this.primaryStage, this.dbs);
		Client result = cep.doClientEditorDialog(c, EditionMode.SUPPRESSION);
		if (result != null) {
			// On établit une connexion avec la base de données
			Connection con;
			try {
				con = LogToDatabase.getConnexion();
			
			// On "prépare" la requête à executer dans sur la BD
			String query = "UPDATE CLIENT SET estInactif = 'O' WHERE idNumCli = ?";
			
			//On remplace le "?" par le numéro du compte
			PreparedStatement pst;
					try {
						
						// Premier UPDATE 
						pst = con.prepareStatement(query);
						pst.setInt(1, result.idNumCli);
						
						// On récupère le résultat pour vérifier si l'insert à fonctionner
						int resultat = pst.executeUpdate();
						
						// Validation des updates
						if (resultat != 1) {
						    // si pas 1 update => anormal
						        con.rollback();
						        throw new RowNotFoundOrTooManyRowsException(Table.CompteCourant, Order.DELETE,
				                        "Insert anormal (insert de moins ou plus d'une ligne)", null, resultat);
						} else {
						    con.commit();
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RowNotFoundOrTooManyRowsException e) {
						e.printStackTrace();
					}
			} catch (DatabaseConnexionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		}	
		return result;
	}

	public Client nouveauClient() {
		Client client;
		ClientEditorPane cep = new ClientEditorPane(this.primaryStage, this.dbs);
		client = cep.doClientEditorDialog(null, EditionMode.CREATION);
		if (client != null) {
			try {
				AccessClient ac = new AccessClient();

				ac.insertClient(client);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				client = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				client = null;
			}
		}
		return client;
	}

	public void gererComptesClient(Client c) {
		ComptesManagement cm = new ComptesManagement(this.primaryStage, this.dbs, c);
		cm.doComptesManagementDialog();
	}

	public ArrayList<Client> getlisteComptes(int _numCompte, String _debutNom, String _debutPrenom) {
		ArrayList<Client> listeCli = new ArrayList<>();
		try {
			// Recherche des clients en BD. cf. AccessClient > getClients(.)
			// numCompte != -1 => recherche sur numCompte
			// numCompte != -1 et debutNom non vide => recherche nom/prenom
			// numCompte != -1 et debutNom vide => recherche tous les clients

			AccessClient ac = new AccessClient();
			listeCli = ac.getClients(this.dbs.getEmpAct().idAg, _numCompte, _debutNom, _debutPrenom);

		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeCli = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeCli = new ArrayList<>();
		}
		return listeCli;
	}
}