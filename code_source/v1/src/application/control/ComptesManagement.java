package application.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ComptesManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.orm.AccessCompteCourant;
import model.orm.LogToDatabase;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

public class ComptesManagement {

	private Stage primaryStage;
	private ComptesManagementController cmc;
	private DailyBankState dbs;
	private Client clientDesComptes;

	public ComptesManagement(Stage _parentStage, DailyBankState _dbstate, Client client) {

		this.clientDesComptes = client;
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(ComptesManagementController.class.getResource("comptesmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+50, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des comptes");
			this.primaryStage.setResizable(false);

			this.cmc = loader.getController();
			this.cmc.initContext(this.primaryStage, this, _dbstate, client);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doComptesManagementDialog() {
		this.cmc.displayDialog();
	}

	public void gererOperations(CompteCourant cpt) {
		OperationsManagement om = new OperationsManagement(this.primaryStage, this.dbs, this.clientDesComptes, cpt);
		om.doOperationsManagementDialog();
	}

	public CompteCourant creerCompte() {
		CompteCourant compte;
		CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dbs);
		compte = cep.doCompteEditorDialog(this.clientDesComptes, null, EditionMode.CREATION);
		if (compte != null) {
			try {
				// TODO : enregistrement du nouveau compte en BDD (la BDD donne de nouvel id
				// dans "compte")
				
				// On établit une connexion avec la base de données
				Connection con = LogToDatabase.getConnexion();
				
				// On "prépare" la requête à executer dans sur la BD
				String query = "INSERT INTO COMPTECOURANT VALUES (" + "seq_id_compte.NEXTVAL" + ", " + "?" + ", " + "?" + ", " + "?" + ", " + "?" + ")";
				
				//On remplace les "?" par des valeurs précises
				PreparedStatement pst;
					try {
						pst = con.prepareStatement(query);
						pst.setInt(1, compte.debitAutorise*(-1));
						pst.setDouble(2, compte.solde);
						pst.setInt(3, compte.idNumCli);
						pst.setString(4, compte.estCloture);
						
						// On récupère le résultat pour vérifier si l'insert à fonctionner
						int result = pst.executeUpdate();
						
						if (result != 1) {
						    // 0 ou plus de 1 insert => anormal
						        con.rollback();
						        throw new RowNotFoundOrTooManyRowsException(Table.CompteCourant, Order.INSERT,
				                        "Insert anormal (insert de moins ou plus d'une ligne)", null, result);
						} else {
						    con.commit();
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				// if JAMAIS vrai
				// existe pour compiler les catchs dessous
				if (Math.random() < -1) {
					throw new ApplicationException(Table.CompteCourant, Order.INSERT, "todo : test exceptions", null);
				}
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
			}
		}
		return compte;
	}
	
	public CompteCourant modifierCompte(CompteCourant compteAmodif) throws RowNotFoundOrTooManyRowsException, DatabaseConnexionException {
        CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dbs);
        CompteCourant result = cep.doCompteEditorDialog(this.clientDesComptes, compteAmodif, EditionMode.MODIFICATION);
        if (result != null) {
            try {
                AccessCompteCourant ac = new AccessCompteCourant();
                ac.updateCompteCourant(result);
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
        return compteAmodif;
    }
	
	public void supprimerCompte(CompteCourant compteASupr) {
		try {
			CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dbs);
	        CompteCourant result = cep.doCompteEditorDialog(this.clientDesComptes, compteASupr, EditionMode.SUPPRESSION);
	        if (result != null) {
	    			// On établit une connexion avec la base de données
	    			Connection con = LogToDatabase.getConnexion();
	    			
	    			// On "prépare" la requête à executer dans sur la BD
	    			String query = "UPDATE COMPTECOURANT SET solde = 0.00 , estCloture = 'O' WHERE idNumCompte = ?";
	    			
	    			//On remplace le "?" par le numéro du compte
	    			PreparedStatement pst;
	    					try {
	    						
	    						// Premier UPDATE 
	    						pst = con.prepareStatement(query);
	    						pst.setInt(1, compteASupr.idNumCompte);
	    						
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
	        		}	
	            } catch (DatabaseConnexionException e) {
	                ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
	                ed.doExceptionDialog();
	                this.primaryStage.close();
	            }
	}

	public ArrayList<CompteCourant> getComptesDunClient() {
		ArrayList<CompteCourant> listeCpt = new ArrayList<>();

		try {
			AccessCompteCourant acc = new AccessCompteCourant();
			listeCpt = acc.getCompteCourants(this.clientDesComptes.idNumCli);
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
}