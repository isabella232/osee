
-- OSEE_ACTIVITY_TYPE
CREATE TABLE OSEE_ACTIVITY_TYPE (
	TYPE_ID ${db.bigint} NOT NULL,
	MODULE VARCHAR(4000),
	MSG_FORMAT VARCHAR(4000),
	LOG_LEVEL ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_ACTIVITY_TYPE__ID_PK PRIMARY KEY(TYPE_ID)
);

-- OSEE_ACTIVITY
CREATE TABLE OSEE_ACTIVITY (
	ENTRY_ID ${db.bigint} NOT NULL,
	ACCOUNT_ID ${db.bigint} NOT NULL,
	CLIENT_ID ${db.bigint} NOT NULL,
	TYPE_ID ${db.bigint} NOT NULL,
	SERVER_ID ${db.bigint} NOT NULL,
	MSG_ARGS VARCHAR(4000),
	START_TIME ${db.bigint} NOT NULL,
	STATUS INTEGER NOT NULL,
	PARENT_ID ${db.bigint} NOT NULL,
	DURATION ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_ACTIVITY__ENTRY_ID_PK PRIMARY KEY(ENTRY_ID),
);

CREATE INDEX OSEE_ACTIVITY__P_E_IDX ON OSEE_ACTIVITY(PARENT_ID,ENTRY_ID);
CREATE INDEX OSEE_ACTIVITY__ACCOUNT_IDX ON OSEE_ACTIVITY(ACCOUNT_ID);
CREATE INDEX OSEE_ACTIVITY__TYPE_IDX ON OSEE_ACTIVITY(TYPE_ID);