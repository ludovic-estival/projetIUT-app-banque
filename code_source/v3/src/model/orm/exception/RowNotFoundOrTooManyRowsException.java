package model.orm.exception;

/**
 * Erreur applicative sur un ordre select/insert/update/delete "monoligne".
 * L'ordre exécuté devait "traiter" une seule ligne et le résultat aboutit à 0
 * ou plusieurs lignes. Exemple de use case de survenue : - demande d'un update
 * d'un client à partir de son numéro - le numéro inexistant en base - =&gt; 0
 * lignes updatées
 */

@SuppressWarnings("serial")
public class RowNotFoundOrTooManyRowsException extends ApplicationException {

	protected int rowsConcerned;

	/**
	 * @param tablename     nom de la table concernée
	 * @param order         opération ayant échoué sur tablename
	 * @param message       message associé à l'erreur
	 * @param cause         éventuelle autre Exception associée (en cas de
	 *                      SQLException principalement)
	 * @param rowsConcerned nombre de lignes concernées (0 ou &gt;1)
	 */
	public RowNotFoundOrTooManyRowsException(Table tablename, Order order, String message, Throwable cause,
			int rowsConcerned) {
		super(tablename, order, message, cause);
		this.rowsConcerned = rowsConcerned;
	}

	@Override
	public String getMessage() {
		return super.getMessageAlone() + " (" + this.tablename + " - " + this.order + " - " + "(" + this.rowsConcerned
				+ " rows)" + " - " + this.getCause() + ")";
	}

	public int getRowsConcerned() {
		return this.rowsConcerned;
	}
}
