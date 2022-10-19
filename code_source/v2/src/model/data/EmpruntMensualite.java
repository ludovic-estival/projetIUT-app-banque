package model.data;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class EmpruntMensualite {
	private SimpleIntegerProperty mois;
	private SimpleStringProperty interets;
	private SimpleStringProperty principal;
	private SimpleStringProperty capitalRestant;

	public EmpruntMensualite(int mois, String interets, String principal, String capitalRestant) {
        this.setMois(mois);
        this.setInterets(interets);
        this.setPrincipal(principal);
        this.setCapitalRestant(capitalRestant);
	}

	public int getMois() {
		return mois.get();
	}

	public void setMois(int mois) {
		this.mois = new SimpleIntegerProperty(mois);
	}

	public String getInterets() {
		return interets.get();
	}

	public void setInterets(String interets) {
		this.interets = new SimpleStringProperty(interets);
	}

	public String getPrincipal() {
		return principal.get();
	}

	public void setPrincipal(String principal) {
		this.principal = new SimpleStringProperty(principal);
	}

	public String getCapitalRestant() {
		return capitalRestant.get();
	}

	public void setCapitalRestant(String capitalRestant) {
		this.capitalRestant = new SimpleStringProperty(capitalRestant);
	}
}
