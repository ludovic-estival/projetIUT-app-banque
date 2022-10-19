package application.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.ClientsManagement;
import application.control.EmployeManagement;
import application.control.EmpruntManagement;
import application.control.ExceptionDialog;
import application.tools.AlertUtilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Employe;
import model.data.EmpruntMensualite;
import model.orm.AccessClient;
import model.orm.AccessCompteCourant;
import model.orm.AccessEmploye;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class EmpruntManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private EmpruntManagement em;
	private EmployeManagement emt;
	private boolean etatEmprunt;
	private double sommeD;
	private int duree;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<EmpruntMensualite> ole;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, EmpruntManagement _em, DailyBankState _dbstate) {
		this.em = _em;
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.etatEmprunt = false;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.ole = FXCollections.observableArrayList();
		this.tv.setItems(this.ole);
		this.tv.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.tv.getFocusModel().focus(-1);

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
	private TextField txtSomme;
	@FXML
	private TextField txtDuree;
	@FXML
	private TextField txtTaux;
	@FXML
	private TextField txtTauxAssurance;
	@FXML
	private Label txtMontantRemboursement;
	@FXML
	private TableView<EmpruntMensualite> tv;
	@FXML
	private TableColumn<EmpruntMensualite, Integer> colMois;
	@FXML
	private TableColumn<EmpruntMensualite, String> colInterets;
	@FXML
	private TableColumn<EmpruntMensualite, String> colPrincipal;
	@FXML
	private TableColumn<EmpruntMensualite, String> colCapitalRestant;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colMois.setCellValueFactory(new PropertyValueFactory<>("Mois"));
		colInterets.setCellValueFactory(new PropertyValueFactory<>("Interets"));
		colPrincipal.setCellValueFactory(new PropertyValueFactory<>("Principal"));
		colCapitalRestant.setCellValueFactory(new PropertyValueFactory<>("CapitalRestant"));
	}

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	@FXML
	private void simulerEmprunt() {
		this.ole.clear();

		if (validationChampsARemplir()) {

			double sommeD = Double.parseDouble(txtSomme.getText());
			this.sommeD = sommeD;
			int duree = Integer.parseInt(txtDuree.getText()) * 12;
			this.duree = duree;
			double tauxApplicable = Double.parseDouble(txtTaux.getText()) / 100 / 12;
			double montantMensualite = sommeD * (tauxApplicable / (1 - Math.pow((1 + tauxApplicable), duree * (-1))));
			double interets;
			double principal;
			double somme;
			this.txtMontantRemboursement.setText((double) Math.round(montantMensualite * 100) / 100 + "€/mois");

			for (int i = 0; i < duree; i++) {
				interets = sommeD * tauxApplicable;
				principal = montantMensualite - interets;
				somme = sommeD - principal;
				this.ole.add(new EmpruntMensualite((i + 1), (double) Math.round(interets * 100) / 100 + "€",
						(double) Math.round(principal * 100) / 100 + "€",
						(double) Math.round(somme * 100) / 100 + "€"));
				sommeD = somme;
			}

			this.etatEmprunt = true;
		}
	}

	@FXML
	private void simulerAssurance() {

		if (this.etatEmprunt) {
			if (!isParsableToDouble(txtTauxAssurance.getText())) {
				AlertUtilities.showAlert(this.primaryStage, "Erreur lors de la simulation", "Erreur de saisie",
						"Taux assurance -> Chiffre entre 0 et 1 (exclus).", AlertType.WARNING);
			} else if (Double.parseDouble(this.txtTauxAssurance.getText()) <= 0
					|| Double.parseDouble(this.txtTauxAssurance.getText()) >= 1) {
				AlertUtilities.showAlert(this.primaryStage, "Erreur lors de la simulation", "Erreur de saisie",
						"Taux assurance -> Chiffre entre 0 et 1 (exclus).", AlertType.WARNING);
			} else {
				double mensualite = Double.parseDouble(txtTauxAssurance.getText()) / 100 * this.sommeD / 12;
				AlertUtilities.showAlert(this.primaryStage, "Informations assurance d'emprunt",
						"Total de l'assurance : " + (double) Math.round((mensualite * this.duree) * 100) / 100 + "€",
						"Mensualité : " + (double) Math.round((mensualite) * 100) / 100 + "€/mois",
						AlertType.INFORMATION);
			}

		} else {
			AlertUtilities.showAlert(this.primaryStage, "Erreur lors de la simulation",
					"Erreur lors de la simulation d'assurance d'emprunt", "Vous n'avez pas encore simuler d'emprunt.",
					AlertType.WARNING);
		}
	}

	private boolean validationChampsARemplir() {

		if (!isParsableToDouble(this.txtSomme.getText())) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null,
					"Capital emprunté -> Nombre positif > 0.", AlertType.WARNING);
			this.txtSomme.clear();
			return false;
		} else if (Double.parseDouble(this.txtSomme.getText()) <= 0) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null,
					"Capital emprunté -> Nombre positif > 0.", AlertType.WARNING);
			this.txtSomme.clear();
			return false;
		}

		if (!isParsableToInt(this.txtDuree.getText())) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null,
					"Durée d'emprunt -> Nombre entier positif > 0.", AlertType.WARNING);
			this.txtDuree.clear();
			return false;
		} else if (Integer.parseInt(this.txtDuree.getText()) <= 0) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null,
					"Durée d'emprunt -> Nombre entier positif > 0.", AlertType.WARNING);
			this.txtDuree.clear();
			return false;
		}

		if (!isParsableToDouble(this.txtTaux.getText())) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null,
					"Taux annuel -> Chiffre entre 0 et 1 (exclus).", AlertType.WARNING);
			this.txtTaux.clear();
			return false;
		} else if (Double.parseDouble(this.txtTaux.getText()) <= 0 || Double.parseDouble(this.txtTaux.getText()) >= 1) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null,
					"Taux annuel -> Chiffre entre 0 et 1 (exclus).", AlertType.WARNING);
			this.txtTaux.clear();
			return false;
		}

		return true;
	}

	public static boolean isParsableToInt(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

	public static boolean isParsableToDouble(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
}