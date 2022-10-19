package model.data;

public class Employe {

	public int idEmploye;
	public String nom, prenom, droitsAccess;
	public String login, motPasse;

	public int idAg;

	public Employe(int idEmploye, String nom, String prenom, String droitsAccess, String login, String motPasse,
			int idAg) {
		super();
		this.idEmploye = idEmploye;
		this.nom = nom;
		this.prenom = prenom;
		this.droitsAccess = droitsAccess;
		this.login = login;
		this.motPasse = motPasse;
		this.idAg = idAg;
	}

	public Employe(Employe e) {
		this(e.idEmploye, e.nom, e.prenom, e.droitsAccess, e.login, e.motPasse, e.idAg);
	}

	public Employe() {
		this(-1000, null, null, null, null, null, -1000);
	}

	@Override
	public String toString() {
		return "[" + this.idEmploye + "]  " + this.nom + " " + this.prenom
				+ " (" + this.droitsAccess + ")  {" + this.login + " - " + this.motPasse
				+ "}  [Agence n° " + this.idAg + "]";
	}

}