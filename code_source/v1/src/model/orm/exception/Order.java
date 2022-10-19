package model.orm.exception;

/**
 * Ordres (opérations) possibles sur la base de données
 */
public enum Order {

	SELECT, UPDATE, DELETE, INSERT,

	OTHER // En général, accès à la BD (type connexion ...)
}
