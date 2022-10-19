package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.PairsOfValue;
import application.tools.StageManagement;
import application.view.PrelevementsManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Prelevement;
import model.orm.AccessCompteCourant;
import model.orm.AccessPrelevement;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class PrelevementsManagement {

	private Stage primaryStage;
	private DailyBankState dbs;
	private PrelevementsManagementController pmc;
	private Client clientDuCompte;
	private CompteCourant compteConcerne;

	public PrelevementsManagement(Stage _parentStage, DailyBankState _dbstate, Client client, CompteCourant compte) {

		this.clientDuCompte = client;
		this.compteConcerne = compte;
		this.dbs = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(
					PrelevementsManagementController.class.getResource("prelevementsmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, 900 + 20, 350 + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des prélèvements");
			this.primaryStage.setResizable(false);

			this.pmc = loader.getController();
			this.pmc.initContext(this.primaryStage, this, _dbstate, client, this.compteConcerne);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPrelevementsManagementDialog() {
		this.pmc.displayDialog();
	}

	public void delPrev(int idPrev) {
		try {
			AccessPrelevement ap = new AccessPrelevement();
			ap.delPrev(idPrev);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
		}

	}

	public Prelevement addPrev() {
		PrelevementEditorPane pep = new PrelevementEditorPane(this.primaryStage, this.dbs);
		Prelevement p = pep.doPrelevementEditorDialog(this.compteConcerne);
		if (p != null) {
			try {
				AccessPrelevement ap = new AccessPrelevement();
				ap.addPrev(p.montant, p.dateRec, p.benef, this.compteConcerne.idNumCompte);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				p = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
				ed.doExceptionDialog();
				p = null;
			}
		}
		return p;
	}

	public PairsOfValue<CompteCourant, ArrayList<Prelevement>> prelevementsEtCompte() {
		ArrayList<Prelevement> listeP = new ArrayList<>();

		try { // Relecture BD du solde du compte
			AccessCompteCourant acc = new AccessCompteCourant();
			this.compteConcerne = acc.getCompteCourant(this.compteConcerne.idNumCompte);

			// lecture BD de la liste des opérations du compte de l'utilisateur
			AccessPrelevement ap = new AccessPrelevement();
			listeP = ap.getPrelevements(this.compteConcerne.idNumCompte);

		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeP = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dbs, ae);
			ed.doExceptionDialog();
			listeP = new ArrayList<>();
		} //
		System.out.println(this.compteConcerne.solde);
		return new PairsOfValue<>(this.compteConcerne, listeP);
	}

	public void gererPrelevements() {
		PrelevementsManagement pm = new PrelevementsManagement(this.primaryStage, this.dbs, clientDuCompte,
				compteConcerne);
		pm.doPrelevementsManagementDialog();
	}
}