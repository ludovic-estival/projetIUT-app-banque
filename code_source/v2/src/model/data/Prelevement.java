package model.data;

public class Prelevement {

	public int idPrelevement;
	public double montant;
	public int dateRec;
	public String benef;
	public int idNumCompte;

	public Prelevement(int idPrelevement, double montant, int dateRec, String benef, int idNumCompte) {
		super();
		this.idPrelevement = idPrelevement;
		this.montant = montant;
		this.dateRec = dateRec;
		this.benef = benef;
		this.idNumCompte = idNumCompte;
	}

	public Prelevement(Prelevement p) {
		this(p.idPrelevement, p.montant, p.dateRec, p.benef, p.idNumCompte);
	}

	public Prelevement() {
		this(-1000, 0, 0, "", -1000);
	}

	@Override
	public String toString() {
		return "Montant : " + String.format("%10.02f", this.montant) + "€ - Bénéficiaire : " + this.benef + " - le "
				+ this.dateRec + " de chaque mois";

//		return "Operation [idOperation=" + idOperation + ", montant=" + montant + ", dateOp=" + dateOp + ", dateValeur="
//				+ dateValeur + ", idNumCompte=" + idNumCompte + ", idTypeOp=" + idTypeOp + "]";
	}

}
