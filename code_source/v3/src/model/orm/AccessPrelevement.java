package model.orm;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import model.data.Prelevement;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.ManagementRuleViolation;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

public class AccessPrelevement {

	public AccessPrelevement() {
	}

	/**
	 * Recherche de toutes les prelevements d'un compte.
	 *
	 * @param idNumCompte id du compte dont on cherche toutes les prelevements
	 * @return Toutes les prelevements du compte, liste vide si pas de prelevement
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public ArrayList<Prelevement> getPrelevements(int idNumCompte) throws DataAccessException, DatabaseConnexionException {
		ArrayList<Prelevement> alResult = new ArrayList<>();

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM PrelevementAutomatique where idNumCompte = ?";
			query += " ORDER BY dateRecurrente";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idNumCompte);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idPrelevement = rs.getInt("idPrelev");
				double montant = rs.getDouble("montant");
				int dateRecurrente = rs.getInt("dateRecurrente");
				String beneficiaire = rs.getString("beneficiaire");
				int idNumCompteTrouve = rs.getInt("idNumCompte");

				alResult.add(new Prelevement(idPrelevement, montant, dateRecurrente, beneficiaire, idNumCompteTrouve));
			}
			rs.close();
			pst.close();
			return alResult;
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.SELECT, "Erreur accès", e);
		}
	}

	/**
	 * Recherche d'un prelevement par son id.
	 *
	 * @param idPrelevement id de l'opération recherchée (clé primaire)
	 * @return une Prelevement ou null si non trouvé
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public Prelevement getPrelevement(int idPrelevement)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		Prelevement prelevementTrouvee;

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM PrelevementAutomatique  where" + " idPrelev = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idPrelevement);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				int idPrelevementTrouve = rs.getInt("idPrelev");
				double montant = rs.getDouble("montant");
				int dateRecurrente = rs.getInt("dateRecurrente");
				String beneficiaire = rs.getString("beneficiaire");
				int idNumCompte = rs.getInt("idNumCompte");

				prelevementTrouvee = new Prelevement(idPrelevementTrouve, montant, dateRecurrente, beneficiaire, idNumCompte);
			} else {
				rs.close();
				pst.close();
				return null;
			}

			if (rs.next()) {
				rs.close();
				pst.close();
				throw new RowNotFoundOrTooManyRowsException(Table.PrelevementAutomatique, Order.SELECT,
						"Recherche anormale (en trouve au moins 2)", null, 2);
			}
			rs.close();
			pst.close();
			return prelevementTrouvee;
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.SELECT, "Erreur accès", e);
		}
	}


	/**
	 * Fonction utilitaire qui retourne un ordre sql "to_date" pour mettre une date
	 * dans une requête sql
	 *
	 * @param d Date (java.sql) à transformer
	 * @return Une chaine : TO_DATE ('j/m/a', 'DD/MM/YYYY') 'j/m/a' : jour mois an
	 *         de d ex : TO_DATE ('25/01/2019', 'DD/MM/YYYY')
	 */
	private String dateToString(Date d) {
		String sd;
		Calendar cal;
		cal = Calendar.getInstance();
		cal.setTime(d);
		sd = "" + cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
		sd = "TO_DATE( '" + sd + "' , 'DD/MM/YYYY')";
		return sd;
	}
	
	/**
	 * Enregistrement d'un virement.
	 *
	 * Se fait par procédure stockée : - Enregistre l'opération - Met à jour le solde du compte.
	 *
	 * Préconditions : compte de destination du virement existant est non clôturé
	 * 
	 * @param idNumCompte		compte dédité
	 * @param idNumCompteCible	compte crédité
	 * @param montant			montant du virement
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 * @throws ManagementRuleViolation
	 */
	public void addPrev(double montant, int dateRec, String benef, int idNumCompte)
			throws DatabaseConnexionException, ManagementRuleViolation, DataAccessException {
		try {
			Connection con = LogToDatabase.getConnexion();
			CallableStatement call;

			String q = "{call AjoutPrelevement (?, ?, ?, ?, ?)}";
			call = con.prepareCall(q);
			call.setDouble(1, montant);
			call.setInt(2, dateRec);
			call.setString(3, benef);
			call.setInt(4, idNumCompte);
			call.registerOutParameter(5, java.sql.Types.INTEGER);

			call.execute();

			int res = call.getInt(5);

			if (res != 0) { // Erreur applicative
				throw new ManagementRuleViolation(Table.PrelevementAutomatique, Order.INSERT,
						"Erreur de règle de gestion : Date de prélèvement incorrecte", null);
			}
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.INSERT, "Erreur accès", e);
		}
	}
	

	/**
	 * Enregistrement d'un crédit.
	 *
	 * Se fait par procédure stockée : - Vérifie que le montant crédité est bien positif
	 * - Enregistre l'opération - Met à jour le solde du compte.
	 *
	 * @param idNumCompte compte crédité
	 * @param montant     montant crédité
	 * @param typeOp      libellé de l'opération effectuée (cf TypeOperation)
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 * @throws ManagementRuleViolation
	 */
	public void delPrev(int idNumPrelev)
			throws DatabaseConnexionException, ManagementRuleViolation, DataAccessException, RowNotFoundOrTooManyRowsException {
        try {
            Connection con = LogToDatabase.getConnexion();

            String query = "DELETE FROM PrelevementAutomatique WHERE idPrelev = ?";

            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, idNumPrelev);

            System.err.println(query);

            int result = pst.executeUpdate();
            pst.close();
            if (result != 1) {
                con.rollback();
                throw new RowNotFoundOrTooManyRowsException(Table.PrelevementAutomatique, Order.DELETE,
                        "Delete anormale (Delete > ou < à une ligne)", null, result);
            }
            con.commit();
        } catch (SQLException e) {
            throw new DataAccessException(Table.PrelevementAutomatique, Order.DELETE, "Erreur accès", e);
        }
	}
}
