package model.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.data.Client;
import model.data.Employe;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

public class AccessEmploye {

	public AccessEmploye() {
	}

	/**
	 * Recherche d'un employe par son login / mot de passe.
	 *
	 * @param login    login de l'employé recherché
	 * @param password mot de passe donné
	 * @return un Employe ou null si non trouvé
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public Employe getEmploye(String login, String password)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		Employe employeTrouve;

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM Employe WHERE" + " login = ?" + " AND motPasse = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setString(1, login);
			pst.setString(2, password);

			ResultSet rs = pst.executeQuery();

			System.err.println(query);

			if (rs.next()) {
				int idEmployeTrouve = rs.getInt("idEmploye");
				String nom = rs.getString("nom");
				String prenom = rs.getString("prenom");
				String droitsAccess = rs.getString("droitsAccess");
				String loginTROUVE = rs.getString("login");
				String motPasseTROUVE = rs.getString("motPasse");
				int idAgEmploye = rs.getInt("idAg");

				employeTrouve = new Employe(idEmployeTrouve, nom, prenom, droitsAccess, loginTROUVE, motPasseTROUVE,
						idAgEmploye);
			} else {
				rs.close();
				pst.close();
				// Non trouvé
				return null;
			}

			if (rs.next()) {
				// Trouvé plus de 1 ... bizarre ...
				rs.close();
				pst.close();
				throw new RowNotFoundOrTooManyRowsException(Table.Employe, Order.SELECT,
						"Recherche anormale (en trouve au moins 2)", null, 2);
			}
			rs.close();
			pst.close();
			return employeTrouve;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Employe, Order.SELECT, "Erreur accès", e);
		}
		
	}
	
	
	/**
	 * Recherche des employés paramétrée (tous/un seul par id/par nom-prénom).
	 *
	 * @param idAg        : id de l'agence dont on cherche les employés
	 * @param idEmp    : vaut -1 si il n'est pas spécifié sinon numéro recherché
	 * @param nomD    : vaut "" si il n'est pas spécifié sinon sera le nom recherché
	 * @param prenomD : vaut "" si il n'est pas spécifié sinon sera prénom recherché
	 * @return Le ou les clients recherchés, liste vide si non trouvé
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public ArrayList<Employe> getEmployes(int idAg, int idEmp, String nomD, String prenomD)
			throws DataAccessException, DatabaseConnexionException {
		ArrayList<Employe> alResult = new ArrayList<>();

		try {
			Connection con = LogToDatabase.getConnexion();

			PreparedStatement pst;

			String query;
			if (idEmp != -1) {
				query = "SELECT * FROM Employe where idAg = ?";
				query += " AND idEmploye = ?";
				query += " ORDER BY nom";
				pst = con.prepareStatement(query);
				pst.setInt(1, idAg);
				pst.setInt(2, idEmp);

			} else if (!nomD.equals("")) {
				nomD = nomD.toUpperCase() + "%";
				prenomD = prenomD.toUpperCase() + "%";
				query = "SELECT * FROM Employe where idAg = ?";
				query += " AND UPPER(nom) like ?" + " AND UPPER(prenom) like ?";
				query += " ORDER BY nom";
				pst = con.prepareStatement(query);
				pst.setInt(1, idAg);
				pst.setString(2, nomD);
				pst.setString(3, prenomD);
			} else {
				query = "SELECT * FROM Employe where idAg = ?";
				query += " ORDER BY nom";
				pst = con.prepareStatement(query);
				pst.setInt(1, idAg);
			}
			System.err.println(query + " nom : " + nomD + " prenom : " + prenomD + "#");

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idNumEmp = rs.getInt("idEmploye");
				String nom = rs.getString("nom");
				String prenom = rs.getString("prenom");
				
				String droitsAccess = rs.getString("droitsAccess");
				String login = rs.getString("login");
				String motPasse = rs.getString("motPasse");
				
				int idAgEmp = rs.getInt("idAg");

				alResult.add(
						new Employe(idNumEmp, nom, prenom, droitsAccess, login, motPasse, idAgEmp));
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.SELECT, "Erreur accès", e);
		}

		return alResult;
	}
	
	
	
	
	/**
	 * Insertion d'un employé dans la base de données
	 *
	 * @param emp IN/OUT Tous les attributs IN sauf idEmploye en OUT
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public void insertEmploye(Employe emp)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {

			Connection con = LogToDatabase.getConnexion();

			String query = "INSERT INTO EMPLOYE VALUES (" + "seq_id_client.NEXTVAL" + ", " + "?" + ", " + "?" + ", "
					+ "?" + ", " + "?" + ", " + "?" + ", " + "?" + ")";
			PreparedStatement pst = con.prepareStatement(query);
			
			pst.setString(1, emp.nom);
			pst.setString(2, emp.prenom);
			pst.setString(3, emp.droitsAccess);
			pst.setString(4, emp.login);
			pst.setString(5, emp.motPasse);
			pst.setInt(6, emp.idAg);

			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();

			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Employe, Order.INSERT,
						"Insert anormal (insert de moins ou plus d'une ligne)", null, result);
			}

			query = "SELECT seq_id_client.CURRVAL from DUAL";

			System.err.println(query);
			PreparedStatement pst2 = con.prepareStatement(query);

			ResultSet rs = pst2.executeQuery();
			rs.next();
			int numCliBase = rs.getInt(1);

			con.commit();
			rs.close();
			pst2.close();

			emp.idEmploye = numCliBase;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.INSERT, "Erreur accès", e);
		}
	}
	
	
	/**
	 * Mise à jour d'un employé dans la base de données
	 * @param emp IN emp.idEmp (clé primaire) doit exister
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public void updateEmploye(Employe emp)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {
			Connection con = LogToDatabase.getConnexion();

			String query = "UPDATE EMPLOYE SET " + "nom = " + "? , " + "prenom = " + "? , " + "droitsAccess = "
					+ "? , " + "login = " + "? , " + "motPasse = " + "? , " + "idAg = " + "? " + " "
					+ "WHERE idEmploye = ? ";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setString(1, emp.nom);
			pst.setString(2, emp.prenom);
			pst.setString(3, emp.droitsAccess);
			pst.setString(4, emp.login);
			pst.setString(5, emp.motPasse);
			pst.setInt(6, emp.idAg);
			pst.setInt(7, emp.idEmploye);

			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Employe, Order.UPDATE,
						"Update anormal (update de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Employe, Order.UPDATE, "Erreur accès", e);
		}
	}
}
