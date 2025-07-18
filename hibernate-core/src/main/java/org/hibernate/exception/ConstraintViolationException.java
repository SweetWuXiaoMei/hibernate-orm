/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.exception;
import java.sql.SQLException;

import org.hibernate.JDBCException;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link JDBCException} indicating that the requested DML operation
 * resulted in violation of a defined integrity constraint.
 *
 * @author Steve Ebersole
 */
public class ConstraintViolationException extends JDBCException {

	private final ConstraintKind kind;
	private final @Nullable String constraintName;

	public ConstraintViolationException(String message, SQLException root, @Nullable String constraintName) {
		this( message, root, ConstraintKind.OTHER, constraintName );
	}

	public ConstraintViolationException(String message, SQLException root, String sql, @Nullable String constraintName) {
		this( message, root, sql, ConstraintKind.OTHER, constraintName );
	}

	public ConstraintViolationException(String message, SQLException root, ConstraintKind kind, @Nullable String constraintName) {
		super( message, root );
		this.kind = kind;
		this.constraintName = constraintName;
	}

	public ConstraintViolationException(String message, SQLException root, String sql, ConstraintKind kind, @Nullable String constraintName) {
		super( message, root, sql );
		this.kind = kind;
		this.constraintName = constraintName;
	}

	/**
	 * Returns the name of the violated constraint, if known.
	 *
	 * @return The name of the violated constraint, or null if not known.
	 */
	public @Nullable String getConstraintName() {
		return constraintName;
	}

	/**
	 * Returns the kind of constraint that was violated.
	 */
	public ConstraintKind getKind() {
		return kind;
	}

	/**
	 * Enumerates the kinds of integrity constraint violation recognized
	 * by Hibernate.
	 */
	public enum ConstraintKind {
		/**
		 * A {@code not null} constraint violation.
		 *
		 * @apiNote The {@linkplain #getConstraintName constraint name}
		 *          in this case is usually just the column name.
		 *
		 * @see jakarta.persistence.Column#nullable
		 */
		NOT_NULL,
		/**
		 * A {@code unique} or {@code primary key} constraint violation.
		 *
		 * @see jakarta.persistence.Column#unique
		 * @see jakarta.persistence.Table#uniqueConstraints
		 */
		UNIQUE,
		/**
		 * A {@code foreign key} constraint violation.
		 *
		 * @see jakarta.persistence.JoinColumn#foreignKey
		 * @see jakarta.persistence.JoinTable#foreignKey
		 */
		FOREIGN_KEY,
		/**
		 * A {@code check} constraint violation.
		 *
		 * @see jakarta.persistence.Column#check
		 * @see jakarta.persistence.Table#check
		 */
		CHECK,
		/**
		 * A constraint violation whose kind was unknown or unrecognized.
		 */
		OTHER
	}
}
