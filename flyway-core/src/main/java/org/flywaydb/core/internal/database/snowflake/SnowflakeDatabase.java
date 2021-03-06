/*
 * Copyright 2010-2020 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import java.sql.Connection;

public class SnowflakeDatabase extends Database<SnowflakeConnection> {
    /**
     * Creates a new instance.
     */
    public SnowflakeDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );
    }

    @Override
    protected SnowflakeConnection doGetConnection(Connection connection) {
        return new SnowflakeConnection(this, connection);
    }









    @Override
    public void ensureSupported() {
        ensureDatabaseIsRecentEnough("3.0");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("3", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessaryForMajorVersion("4.2");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        // CAUTION: Quotes are optional around column names without underscores; but without them, Snowflake will
        // uppercase the column name leading to SELECTs failing.
        return "CREATE TABLE " + table + " (\n" +
                quote("installed_rank") + " NUMBER(38,0) NOT NULL,\n" +
                quote("version") + " VARCHAR(50),\n" +
                quote("description") + " VARCHAR(200),\n" +
                quote("type") + " VARCHAR(20) NOT NULL,\n" +
                quote("script") + " VARCHAR(1000) NOT NULL,\n" +
                quote("checksum") + " NUMBER(38,0),\n" +
                quote("installed_by") + " VARCHAR(100) NOT NULL,\n" +
                quote("installed_on") + " TIMESTAMP_LTZ(9) NOT NULL DEFAULT CURRENT_TIMESTAMP(),\n" +
                quote("execution_time") + " NUMBER(38,0) NOT NULL,\n" +
                quote("success") + " BOOLEAN NOT NULL,\n" +
                "primary key (" + quote("installed_rank") + "));\n" +

                (baseline ? getBaselineStatement(table) + ";\n" : "");
    }

    @Override
    public String getSelectStatement(Table table) {
        // CAUTION: Quotes are optional around column names without underscores; but without them, Snowflake will
        // uppercase the column name. In data readers, the column name is case sensitive.
        return "SELECT " + quote("installed_rank")
                + "," + quote("version")
                + "," + quote("description")
                + "," + quote("type")
                + "," + quote("script")
                + "," + quote("checksum")
                + "," + quote("installed_on")
                + "," + quote("installed_by")
                + "," + quote("execution_time")
                + "," + quote("success")
                + " FROM " + table
                + " WHERE " + quote("installed_rank") + " > ?"
                + " ORDER BY " + quote("installed_rank");
    }

    @Override
    public String getInsertStatement(Table table) {
        // CAUTION: Quotes are optional around column names without underscores; but without them, Snowflake will
        // uppercase the column name.
        return "INSERT INTO " + table
                + " (" + quote("installed_rank")
                + ", " + quote("version")
                + ", " + quote("description")
                + ", " + quote("type")
                + ", " + quote("script")
                + ", " + quote("checksum")
                + ", " + quote("installed_by")
                + ", " + quote("execution_time")
                + ", " + quote("success")
                + ")"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "true";
    }

    @Override
    public String getBooleanFalse() {
        return "false";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}