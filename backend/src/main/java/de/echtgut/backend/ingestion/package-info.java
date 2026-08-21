/**
 * Ingestion module responsible for scheduled fetching and parsing of raw deal sources.
 *
 * <p>Contains feed adapters implementing the RawDealSource interface to convert external data into
 * pending raw_deals records in the staging database table.
 */
package de.echtgut.backend.ingestion;
