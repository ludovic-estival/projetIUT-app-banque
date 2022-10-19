package application.tools;

import model.data.Client;
import model.data.Employe;

public class ConstantesIHM {

	public static final String CLIENT_INACTIF = "O";
	public static final String CLIENT_ACTIF = "N";

	public static final String AGENCE_CHEF = "chefAgence";
	public static final String AGENCE_GUICHETIER = "guichetier";

	public static final String TYPE_OP_1 = "Dépôt Espèces";
	public static final String TYPE_OP_2 = "Retrait Espèces";
	public static final String TYPE_OP_3 = "Dépôt Chèque";
	public static final String TYPE_OP_4 = "Paiement Chèque";
	public static final String TYPE_OP_5 = "Retrait Carte Bleue";
	public static final String TYPE_OP_6 = "Paiement Carte Bleue";
	public static final String TYPE_OP_7 = "Virement Compte à Compte";
	public static final String TYPE_OP_8 = "Prélèvement automatique";
	public static final String TYPE_OP_9 = "Prélèvement agios";

	public static final String[] OPERATIONS_DEBIT_GUICHET = { ConstantesIHM.TYPE_OP_2, ConstantesIHM.TYPE_OP_5 };
	public static final String[] OPERATIONS_CREDIT_GUICHET = { ConstantesIHM.TYPE_OP_1, ConstantesIHM.TYPE_OP_3 };

	public static boolean isAdmin(String droitAccess) {
		return droitAccess.equals(ConstantesIHM.AGENCE_CHEF);
	}

	public static boolean isAdmin(Employe employe) {
		return employe.droitsAccess.equals(ConstantesIHM.AGENCE_CHEF);
	}

	public static boolean estActif(Client c) {
		return c.estInactif.equals(ConstantesIHM.CLIENT_ACTIF);
	}

	public static boolean estInactif(Client c) {
		return c.estInactif.equals(ConstantesIHM.CLIENT_INACTIF);
	}

}
