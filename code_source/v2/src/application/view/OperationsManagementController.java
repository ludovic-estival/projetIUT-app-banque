package application.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfWriter;

import application.DailyBankApp;
import application.DailyBankState;
import application.control.OperationsManagement;
import application.tools.AlertUtilities;
import application.tools.NoSelectionModel;
import application.tools.PairsOfValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;

public class OperationsManagementController implements Initializable {

	// Etat application
	private DailyBankState dbs;
	private OperationsManagement om;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private Client clientDuCompte;
	private CompteCourant compteConcerne;
	private ObservableList<Operation> olOperation;

	// Manipulation de la fenêtre
	public void initContext(Stage _primaryStage, OperationsManagement _om, DailyBankState _dbstate, Client client, CompteCourant compte) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.om = _om;
		this.clientDuCompte = client;
		this.compteConcerne = compte;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.olOperation = FXCollections.observableArrayList();
		this.lvOperations.setItems(this.olOperation);
		this.lvOperations.setSelectionModel(new NoSelectionModel<Operation>());
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
	private ListView<Operation> lvOperations;
	@FXML
	private Button btnDebit;
	@FXML
	private Button btnCredit;
	@FXML
	private Button btnAutre;
	@FXML
	private Button btnGenerer;
	@FXML
	private Button btnPrelevements;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	@FXML
	private void doDebit() {
		Operation op = this.om.enregistrerDebit();
		if (op != null) {
			this.updateInfoCompteClient();
			this.validateComponentState();
		}
	}

	@FXML
	private void doCredit() {
		Operation op = this.om.enregistrerCredit();
		if (op != null) {
			this.updateInfoCompteClient();
			this.validateComponentState();
		}
	}

	@FXML
	private void doAutre() {
		Operation op = this.om.enregistrerVirement();
		if (op != null) {
			this.updateInfoCompteClient();
			this.validateComponentState();
		}
	}
	
	@FXML
	private void doGenerer() {
		
		String nom = this.clientDuCompte.nom + "_" + this.clientDuCompte.prenom + "_cpt" + this.compteConcerne.idNumCompte;
		String fichier = "C:/PDF_DailyBank/releve_" + nom + ".pdf";
		String banque = "Daily Bank \n" + this.dbs.getAgAct().nomAg + "\n" + this.dbs.getAgAct().adressePostaleAg;
		
		Font policeNormale = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
		Font policeTitre = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new CMYKColor(0, 255, 0, 0));
		
		Document document = new Document();
		
		try
	      {
			File D = new File("C:/PDF_DailyBank");    
		    D.mkdir(); 
		      
	         PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fichier));
	         document.open();
	         
	         document.add(new Paragraph(banque));
	         
	         document.add(new Paragraph(" "));
	         
	         document.add(new Paragraph("Relevé de compte", policeTitre));
	         
	         document.add(new Paragraph(" "));
	         
	         document.add(new Paragraph(this.clientDuCompte.nom + " " + this.clientDuCompte.prenom));
	         document.add(new Paragraph(this.clientDuCompte.adressePostale));
	         
	         document.add(new Paragraph(" "));
	         
	         document.add(new Paragraph("Relevé des opérations sur le compte n°" + this.compteConcerne.idNumCompte));
	         
	         document.add(new Paragraph(" "));
	         
	         for(int i=0; i<olOperation.size(); i++) {
	 			document.add(new Paragraph(this.olOperation.get(i).toString()));
	 			
	 		}
	         
	        
	         document.close();
	         writer.close();
	         
	         AlertUtilities.showAlert(this.primaryStage, "Génération d'un pdf", "Le PDF a été créé.", fichier, AlertType.INFORMATION);
	      } catch (DocumentException e)
	      {
	         e.printStackTrace();
	      } catch (FileNotFoundException e)
	      {
	         e.printStackTrace();
	      }
		
	}
	
	@FXML
	private void doPrelevements() {
		this.om.gererPrelevements();
	}

	private void validateComponentState() {
		if(this.compteConcerne.estCloture.equals("N")) {
			this.btnCredit.setDisable(false);
			this.btnDebit.setDisable(false);
			this.btnAutre.setDisable(false);
			if (this.dbs.isChefDAgence()) {
				this.btnPrelevements.setDisable(false);		
			} else {
				this.btnPrelevements.setDisable(true);	
			}
		} else {
			this.btnCredit.setDisable(true);
			this.btnDebit.setDisable(true);
			this.btnAutre.setDisable(true);	
		}
	}

	private void updateInfoCompteClient() {

		PairsOfValue<CompteCourant, ArrayList<Operation>> opesEtCompte;

		opesEtCompte = this.om.operationsEtSoldeDunCompte();

		ArrayList<Operation> listeOP;
		this.compteConcerne = opesEtCompte.getLeft();
		listeOP = opesEtCompte.getRight();

		String info;
		info = this.clientDuCompte.nom + "  " + this.clientDuCompte.prenom + "  (id : " + this.clientDuCompte.idNumCli
				+ ")";
		this.lblInfosClient.setText(info);

		info = "Cpt. : " + this.compteConcerne.idNumCompte + "  "
				+ String.format(Locale.ENGLISH, "%12.02f", this.compteConcerne.solde) + "  /  "
				+ String.format(Locale.ENGLISH, "%8d", this.compteConcerne.debitAutorise);
		this.lblInfosCompte.setText(info);

		this.olOperation.clear();
		for (Operation op : listeOP) {
			this.olOperation.add(op);
		}

		this.validateComponentState();
	}
}