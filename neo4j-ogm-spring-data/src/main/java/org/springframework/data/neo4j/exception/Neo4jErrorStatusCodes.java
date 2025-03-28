/*
 * Copyright 2011-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.neo4j.exception;

import java.util.HashMap;

import org.springframework.dao.*;

/**
 * Conversion table for Neo4j Errors to Spring Data Exception hierarchy.
 *
 * @author Mark Angrish
 */
public class Neo4jErrorStatusCodes {

	private static HashMap<String, Class<? extends DataAccessException>> errors;

	static {
		errors = new HashMap<>();
		errors.put("Neo.ClientError.General.ForbiddenOnReadOnlyDatabase", PermissionDeniedDataAccessException.class);
		errors.put("Neo.ClientError.LegacyIndex.LegacyIndexNotFound", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Procedure.ProcedureCallFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Procedure.ProcedureNotFound", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Procedure.ProcedureRegistrationFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Procedure.TypeError", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Request.Invalid", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Request.InvalidFormat", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Request.TransactionRequired", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Schema.ConstraintAlreadyExists", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Schema.ConstraintNotFound", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Schema.ConstraintValidationFailed", DataIntegrityViolationException.class);
		errors.put("Neo.ClientError.Schema.ConstraintVerificationFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Schema.ForbiddenOnConstraintIndex", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Schema.IndexAlreadyExists", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Schema.IndexNotFound", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Schema.TokenNameError", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Security.AuthenticationRateLimit", PermissionDeniedDataAccessException.class);
		errors.put("Neo.ClientError.Security.CredentialsExpired", PermissionDeniedDataAccessException.class);
		errors.put("Neo.ClientError.Security.EncryptionRequired", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Security.Forbidden", PermissionDeniedDataAccessException.class);
		errors.put("Neo.ClientError.Security.Unauthorized", PermissionDeniedDataAccessException.class);
		errors.put("Neo.ClientError.Statement.ArgumentError", InvalidDataAccessApiUsageException.class);
		errors.put("Neo.ClientError.Statement.ArithmeticError", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Statement.ConstraintVerificationFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Statement.EntityNotFound", EmptyResultDataAccessException.class);
		errors.put("Neo.ClientError.Statement.ExternalResourceFailed", NonTransientDataAccessResourceException.class);
		errors.put("Neo.ClientError.Statement.LabelNotFound", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Statement.ParameterMissing", InvalidDataAccessApiUsageException.class);
		errors.put("Neo.ClientError.Statement.PropertyNotFound", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Statement.SemanticError", NonTransientDataAccessException.class);
		errors.put("Neo.ClientError.Statement.SyntaxError", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Statement.TypeError", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Transaction.ForbiddenDueToTransactionType",
				InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Transaction.TransactionAccessedConcurrently",
				InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Transaction.TransactionEventHandlerFailed",
				InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Transaction.TransactionHookFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Transaction.TransactionMarkedAsFailed", ConcurrencyFailureException.class);
		errors.put("Neo.ClientError.Transaction.TransactionNotFound", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Transaction.TransactionTerminated", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.ClientError.Transaction.TransactionValidationFailed",
				InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.General.IndexCorruptionDetected", DataAccessResourceFailureException.class);
		errors.put("Neo.DatabaseError.General.SchemaCorruptionDetected", DataAccessResourceFailureException.class);
		errors.put("Neo.DatabaseError.General.UnknownError", UncategorizedDataAccessException.class);
		errors.put("Neo.DatabaseError.Schema.ConstraintCreationFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.ConstraintDropFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.IndexCreationFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.IndexDropFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.LabelAccessFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.LabelLimitReached", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.PropertyKeyAccessFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.RelationshipTypeAccessFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.SchemaRuleAccessFailed", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Schema.SchemaRuleDuplicateFound", InvalidDataAccessResourceUsageException.class);
		errors.put("Neo.DatabaseError.Statement.ExecutionFailed", TransientDataAccessResourceException.class);
		errors.put("Neo.DatabaseError.Transaction.TransactionCommitFailed", TransientDataAccessResourceException.class);
		errors.put("Neo.DatabaseError.Transaction.TransactionLogError", TransientDataAccessResourceException.class);
		errors.put("Neo.DatabaseError.Transaction.TransactionRollbackFailed", TransientDataAccessResourceException.class);
		errors.put("Neo.DatabaseError.Transaction.TransactionStartFailed", TransientDataAccessResourceException.class);
		errors.put("Neo.TransientError.General.DatabaseUnavailable", TransientDataAccessResourceException.class);
		errors.put("Neo.TransientError.General.OutOfMemoryError", NonTransientDataAccessResourceException.class);
		errors.put("Neo.TransientError.General.StackOverFlowError", NonTransientDataAccessResourceException.class);
		errors.put("Neo.TransientError.Network.CommunicationError", DataAccessResourceFailureException.class);
		errors.put("Neo.TransientError.Schema.SchemaModifiedConcurrently", ConcurrencyFailureException.class);
		errors.put("Neo.TransientError.Security.ModifiedConcurrently", ConcurrencyFailureException.class);
		errors.put("Neo.TransientError.Transaction.ConstraintsChanged", ConcurrencyFailureException.class);
		errors.put("Neo.TransientError.Transaction.DeadlockDetected", ConcurrencyFailureException.class);
		errors.put("Neo.TransientError.Transaction.InstanceStateChanged", ConcurrencyFailureException.class);
		errors.put("Neo.TransientError.Transaction.LockClientStopped", NonTransientDataAccessResourceException.class);
		errors.put("Neo.TransientError.Transaction.LockSessionExpired", DataAccessResourceFailureException.class);
		errors.put("Neo.TransientError.Transaction.Outdated", TransientDataAccessResourceException.class);
		errors.put("Neo.TransientError.Transaction.Terminated", TransientDataAccessResourceException.class);
	}

	public static Class<? extends DataAccessException> translate(String errorCode) {
		return errors.get(errorCode);
	}
}
