package application.control;

import java.util.ArrayList;


import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ClientsManagementController;
import application.view.EmployeManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.Employe;
import model.orm.AccessClient;
import model.orm.AccessEmploye;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class EmployeManagement {

	private Stage primaryStage;
	private DailyBankState dbs;
	private EmployeManagementController emc;

	public EmployeManagement(Stage _parentStage, DailyBankState _dbstate) {
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(EmployeManagementController.class.getResource("employemanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth()+50, root.getPrefHeight()+10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des employés");
			this.primaryStage.setResizable(false);

			this.emc = loader.getController();
			this.emc.initContext(this.primaryStage, this, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Ouvre la fenêtre pour gérer les employés
	 */
	public void doEmployeManagementDialog() {
		this.emc.displayDialog();
	}

	
	/** Recherche les employés dans la base de données
	 * @param numEmp numéro de l'employé
	 * @param nomD nom de l'employé
	 * @param prenomD prénom de l'employé
	 * @return ArrayList contenant les employés trouvés
	 */
	public ArrayList<Employe> getlisteEmployes(int numEmp, String nomD, String prenomD) {
		ArrayList<Employe> listeEmp = new ArrayList<>();
		
		try {
			AccessEmploye ae = new AccessEmploye();
			listeEmp = ae.getEmployes(this.dbs.getEmpAct().idAg, numEmp, nomD, prenomD);

		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeEmp = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeEmp = new ArrayList<>();
		}
		return listeEmp;
	}
	
	
	
	/** Permet de créer un nouvel employé
	 * @return le nouvel employé
	 */
	public Employe nouveauEmploye() {
		Employe emp;
		
		EmployeEditorPane ep = new EmployeEditorPane(this.primaryStage, this.dbs);
		emp = ep.doEmployeEditorDialog(null, EditionMode.CREATION);
		if (emp != null) {
			try {
				AccessEmploye ac = new AccessEmploye();

				ac.insertEmploye(emp);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				emp = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				emp = null;
			}
		}
		return emp;
	}
	
	
	/** Permet de modifier un employé
	 * 
	 * @param emp l'employé à modifier
	 * @return l'employé modifié
	 */
	public Employe modifierEmploye(Employe emp) {
		EmployeEditorPane eep = new EmployeEditorPane(this.primaryStage, this.dbs);
		Employe result = eep.doEmployeEditorDialog(emp, EditionMode.MODIFICATION);
		if (result != null) {
			try {
				AccessEmploye ac = new AccessEmploye();
				ac.updateEmploye(result);
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
}