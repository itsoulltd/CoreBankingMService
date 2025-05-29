--Create New User for Oracle-Database Server:
ALTER SESSION SET "_ORACLE_SCRIPT"=true;
CREATE USER testdb IDENTIFIED BY testdb123 ACCOUNT UNLOCK;
GRANT CONNECT, RESOURCE, DBA TO testdb;
GRANT CREATE SESSION TO testdb;
GRANT ALL PRIVILEGES TO testdb;
GRANT CREATE TABLE TO testdb;
GRANT CREATE VIEW TO testdb;
GRANT CREATE ANY TRIGGER TO testdb;
GRANT CREATE ANY PROCEDURE TO testdb;
GRANT CREATE SEQUENCE TO testdb;
GRANT CREATE SYNONYM TO testdb;
GRANT UNLIMITED TABLESPACE TO testdb;
GRANT CREATE ROLE TO testdb;
GRANT DROP ANY TABLE TO testdb;
ALTER SESSION SET "_ORACLE_SCRIPT"=false;
--
CREATE TABLE testdb.client (
   ref VARCHAR2(250) NOT NULL,
   tenant_ref VARCHAR2(250) NOT NULL,
   creation_date TIMESTAMP(6) NOT NULL,
   PRIMARY KEY(ref, tenant_ref)
);

CREATE TABLE testdb.account (
  id NUMBER GENERATED ALWAYS AS IDENTITY,
  client_ref VARCHAR2(250) NOT NULL,
  tenant_ref VARCHAR2(250) NOT NULL,
  account_ref VARCHAR2(20) NOT NULL,
  amount NUMBER(20,2) NOT NULL,
  currency VARCHAR2(3) NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE testdb.transaction_history (
  client_ref VARCHAR2(250) NOT NULL,
  tenant_ref VARCHAR2(250) NOT NULL,
  transaction_ref VARCHAR2(20) NOT NULL,
  transaction_type VARCHAR2(20) NOT NULL,
  transaction_date TIMESTAMP(6) NOT NULL,
  PRIMARY KEY(client_ref, tenant_ref, transaction_ref)
);

CREATE TABLE testdb.transaction_leg (
  client_ref VARCHAR2(250) NOT NULL,
  tenant_ref VARCHAR2(250) NOT NULL,
  transaction_ref VARCHAR2(20) NOT NULL,
  account_ref VARCHAR2(20) NOT NULL,
  amount NUMBER(20,2) NOT NULL,
  currency VARCHAR2(3) NOT NULL,
  eventTimestamp VARCHAR2(512) NULL,
  signature VARCHAR2(512) NULL
);