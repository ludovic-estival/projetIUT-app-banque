package application;

import application.tools.ConstantesIHM;
import model.data.AgenceBancaire;
import model.data.Employe;

public class DailyBankState {
	
	private Employe empAct;
	private AgenceBancaire agAct;
	private boolean isChefDAgence;

	public Employe getEmpAct() {
		return this.empAct;
	}

	public void setEmpAct(Employe employeActif) {
		this.empAct = employeActif;
	}

	public AgenceBancaire getAgAct() {
		return this.agAct;
	}

	public void setAgAct(AgenceBancaire agenceActive) {
		this.agAct = agenceActive;
	}

	public boolean isChefDAgence() {
		return this.isChefDAgence;
	}

	public void setChefDAgence(boolean isChefDAgence) {
		this.isChefDAgence = isChefDAgence;
	}

	public void setChefDAgence(String droitsAccess) {
		this.isChefDAgence = false;

		if (droitsAccess.equals(ConstantesIHM.AGENCE_CHEF)) {
			this.isChefDAgence = true;
		}
	}
}
