
-- OSEE_ARTIFACT
CREATE TABLE OSEE_ARTIFACT ( 
	GUID varchar (22) NOT NULL,
	ART_ID ${db.bigint} NOT NULL,
	ART_TYPE_ID ${db.bigint} NOT NULL,
	GAMMA_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_ART__G_A_PK PRIMARY KEY (ART_ID,GAMMA_ID)
);

CREATE INDEX OSEE_ART__ART_ID_IDX ON OSEE_ARTIFACT (ART_ID) ${db.tablespace.osee_index};
CREATE INDEX OSEE_ART__GUID_IDX ON OSEE_ARTIFACT (GUID) ${db.tablespace.osee_index};
CREATE INDEX OSEE_ART__ART_TYPE_ID_IDX ON OSEE_ARTIFACT (ART_TYPE_ID) ${db.tablespace.osee_index};

-- OSEE_ATTRIBUTE
CREATE TABLE OSEE_ATTRIBUTE ( 
	ATTR_TYPE_ID ${db.bigint} NOT NULL,
	ART_ID int NOT NULL,
	VALUE varchar (4000),
	ATTR_ID int NOT NULL,
	GAMMA_ID ${db.bigint} NOT NULL,
	URI varchar (200),
	CONSTRAINT OSEE_ATTRIBUTE_AT_G_PK PRIMARY KEY (ATTR_ID,GAMMA_ID) 
);

CREATE INDEX OSEE_ATTRIBUTE_AR_G_IDX ON OSEE_ATTRIBUTE (ART_ID, GAMMA_ID) ${db.tablespace.osee_index};
CREATE INDEX OSEE_ATTRIBUTE_G_AT_IDX ON OSEE_ATTRIBUTE (GAMMA_ID, ATTR_ID) ${db.tablespace.osee_index};

-- OSEE_RELATION_LINK
CREATE TABLE OSEE_RELATION_LINK ( 
	REL_LINK_ID int NOT NULL,
	REL_LINK_TYPE_ID ${db.bigint} NOT NULL,
	A_ART_ID int NOT NULL,
	B_ART_ID int NOT NULL,
	RATIONALE varchar (4000),
	GAMMA_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_RELATION__G_PK PRIMARY KEY (GAMMA_ID) 
);

CREATE INDEX OSEE_RELATION__R_G_IDX ON OSEE_RELATION_LINK (REL_LINK_ID, GAMMA_ID) ${db.tablespace.osee_index};
CREATE INDEX OSEE_RELATION__A_IDX ON OSEE_RELATION_LINK (A_ART_ID) ${db.tablespace.osee_index};
CREATE INDEX OSEE_RELATION__B_IDX ON OSEE_RELATION_LINK (B_ART_ID) ${db.tablespace.osee_index};

-- OSEE_BRANCH
CREATE TABLE OSEE_BRANCH ( 
	BRANCH_NAME varchar (200) NOT NULL,
	BRANCH_TYPE smallint NOT NULL,
	BASELINE_TRANSACTION_ID int NOT NULL,
	ASSOCIATED_ART_ID int NOT NULL,
	ARCHIVED smallint NOT NULL,
	BRANCH_ID ${db.bigint} NOT NULL,
	BRANCH_STATE smallint NOT NULL,
	PARENT_BRANCH_ID ${db.bigint} NOT NULL,
	PARENT_TRANSACTION_ID int NOT NULL,
	INHERIT_ACCESS_CONTROL smallint NOT NULL,
	CONSTRAINT OSEE_BRANCH_B_PK PRIMARY KEY (BRANCH_ID) 
);

CREATE INDEX OSEE_BRANCH_A_IDX ON OSEE_BRANCH (ARCHIVED) ${db.tablespace.osee_index};

INSERT INTO OSEE_BRANCH (BRANCH_NAME, BRANCH_TYPE, BASELINE_TRANSACTION_ID, ASSOCIATED_ART_ID, ARCHIVED, BRANCH_ID, BRANCH_STATE, PARENT_BRANCH_ID, PARENT_TRANSACTION_ID, INHERIT_ACCESS_CONTROL) VALUES ('${osee.sys_root_name}',${osee.sys_root_type},1,-1,0,${osee.sys_root_id},${osee.sys_root_state},-1,1,0);

-- OSEE_TXS
CREATE TABLE OSEE_TXS ( 
	BRANCH_ID ${db.bigint} NOT NULL,
	GAMMA_ID ${db.bigint} NOT NULL,
	TRANSACTION_ID int NOT NULL,
	TX_CURRENT smallint NOT NULL,
	MOD_TYPE smallint NOT NULL,
	APP_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_TXS_PK PRIMARY KEY (BRANCH_ID, GAMMA_ID, TRANSACTION_ID))
	${db.organization_index_1};

-- OSEE_TXS_ARCHIVED
CREATE TABLE OSEE_TXS_ARCHIVED ( 
	BRANCH_ID ${db.bigint} NOT NULL,
	GAMMA_ID ${db.bigint} NOT NULL,
	TRANSACTION_ID int NOT NULL,
	TX_CURRENT smallint NOT NULL,
	MOD_TYPE smallint NOT NULL,
	APP_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_TXS_ARCHIVED_PK PRIMARY KEY (BRANCH_ID, GAMMA_ID, TRANSACTION_ID))
	${db.organization_index_1}
	${db.tablespace.osee_archived};

-- OSEE_TX_DETAILS
CREATE TABLE OSEE_TX_DETAILS ( 
	AUTHOR int NOT NULL,
	TIME timestamp NOT NULL,
	OSEE_COMMENT varchar (1000),
	TX_TYPE smallint NOT NULL,
	COMMIT_ART_ID int,
	BRANCH_ID ${db.bigint} NOT NULL,
	TRANSACTION_ID int NOT NULL,
	CONSTRAINT OSEE_TX_DETAILS_TXID_PK PRIMARY KEY (TRANSACTION_ID),
	CONSTRAINT BRANCH_ID_FK1 FOREIGN KEY (BRANCH_ID) REFERENCES OSEE_BRANCH (BRANCH_ID) 
);

CREATE INDEX OSEE_TX_DETAILS_B_TX_IDX ON OSEE_TX_DETAILS (BRANCH_ID, TRANSACTION_ID) ${db.tablespace.osee_index};

INSERT INTO OSEE_TX_DETAILS (AUTHOR, TIME, OSEE_COMMENT, TX_TYPE, COMMIT_ART_ID, BRANCH_ID, TRANSACTION_ID) VALUES (-1,CURRENT_TIMESTAMP,'${osee.sys_root_name} Creation',1,NULL,1,1);

ALTER TABLE OSEE_BRANCH ADD CONSTRAINT PARENT_TX_ID_FK1 FOREIGN KEY(PARENT_TRANSACTION_ID) REFERENCES OSEE_TX_DETAILS (TRANSACTION_ID);
ALTER TABLE OSEE_BRANCH ADD CONSTRAINT BASELINE_TX_ID_FK1 FOREIGN KEY(BASELINE_TRANSACTION_ID) REFERENCES OSEE_TX_DETAILS (TRANSACTION_ID) ${db.deferrable};

-- OSEE_PERMISSION
CREATE TABLE OSEE_PERMISSION ( 
	PERMISSION_NAME varchar (50) NOT NULL,
	PERMISSION_ID int NOT NULL,
	CONSTRAINT PERMISSION_PK PRIMARY KEY (PERMISSION_ID) 
);

-- OSEE_ARTIFACT_ACL
CREATE TABLE OSEE_ARTIFACT_ACL (
	PRIVILEGE_ENTITY_ID INTEGER NOT NULL,
	ART_ID int NOT NULL,
	BRANCH_ID ${db.bigint} NOT NULL,
	PERMISSION_ID int NOT NULL,
	CONSTRAINT OSEE_ARTIFACT_ACL_A_P_B_PK PRIMARY KEY (ART_ID,PRIVILEGE_ENTITY_ID,BRANCH_ID),
	CONSTRAINT ARTIFACT_ACL_PERM_FK FOREIGN KEY (PERMISSION_ID) REFERENCES OSEE_PERMISSION (PERMISSION_ID)
);

-- OSEE_BRANCH_ACL
CREATE TABLE OSEE_BRANCH_ACL ( 
	PRIVILEGE_ENTITY_ID int NOT NULL,
	BRANCH_ID ${db.bigint} NOT NULL,
	PERMISSION_ID int NOT NULL,
	CONSTRAINT OSEE_BRANCH_ACL_B_P_PK PRIMARY KEY (BRANCH_ID,PRIVILEGE_ENTITY_ID),
	CONSTRAINT BRANCH_ACL_FK FOREIGN KEY (BRANCH_ID) REFERENCES OSEE_BRANCH (BRANCH_ID) ON DELETE CASCADE,
	CONSTRAINT BRANCH_ACL_PERM_FK FOREIGN KEY (PERMISSION_ID) REFERENCES OSEE_PERMISSION (PERMISSION_ID) 
);

-- OSEE_SEARCH_TAGS
CREATE TABLE OSEE_SEARCH_TAGS ( 
	CODED_TAG_ID ${db.bigint} NOT NULL,
	GAMMA_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_SEARCH_TAGS__PK PRIMARY KEY (CODED_TAG_ID,GAMMA_ID) 
);

CREATE INDEX OSEE_SEARCH_TAGS_C_IDX ON OSEE_SEARCH_TAGS (CODED_TAG_ID) ${db.tablespace.osee_index};
CREATE INDEX OSEE_SEARCH_TAGS_G_IDX ON OSEE_SEARCH_TAGS (GAMMA_ID) ${db.tablespace.osee_index};

-- OSEE_TAG_GAMMA_QUEUE
CREATE TABLE OSEE_TAG_GAMMA_QUEUE ( 
	QUERY_ID int NOT NULL,
	GAMMA_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_TAG_GAMMA_Q_G_PK PRIMARY KEY (QUERY_ID,GAMMA_ID) 
);

-- OSEE_SEQUENCE
CREATE TABLE OSEE_SEQUENCE ( 
	SEQUENCE_NAME varchar (128) NOT NULL,
	LAST_SEQUENCE ${db.bigint} NOT NULL,
	CONSTRAINT SEQUENCE_ID_UN UNIQUE (SEQUENCE_NAME) 
);

INSERT INTO OSEE_SEQUENCE (SEQUENCE_NAME, LAST_SEQUENCE) VALUES ('${osee.tx_seq}',1);

-- OSEE_INFO
CREATE TABLE OSEE_INFO ( 
	OSEE_VALUE varchar (1000) NOT NULL,
	OSEE_KEY varchar (50) NOT NULL,
	CONSTRAINT OSEE_INFO_KEY_UN_IDX UNIQUE (OSEE_KEY) 
);

-- OSEE_MERGE
CREATE TABLE OSEE_MERGE ( 
	SOURCE_BRANCH_ID ${db.bigint} NOT NULL,
	MERGE_BRANCH_ID ${db.bigint} NOT NULL,
	COMMIT_TRANSACTION_ID int NOT NULL,
	DEST_BRANCH_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_MERGE__PK PRIMARY KEY (MERGE_BRANCH_ID),
	CONSTRAINT OSEE_MERGE__MBI_FK FOREIGN KEY (MERGE_BRANCH_ID) REFERENCES OSEE_BRANCH (BRANCH_ID),
	CONSTRAINT OSEE_MERGE__DBI_FK FOREIGN KEY (DEST_BRANCH_ID) REFERENCES OSEE_BRANCH (BRANCH_ID) 
);

-- OSEE_CONFLICT
CREATE TABLE OSEE_CONFLICT ( 
	SOURCE_GAMMA_ID ${db.bigint} NOT NULL,
	MERGE_BRANCH_ID ${db.bigint} NOT NULL,
	CONFLICT_ID int NOT NULL,
	DEST_GAMMA_ID ${db.bigint} NOT NULL,
	CONFLICT_TYPE smallint NOT NULL,
	STATUS smallint NOT NULL,
	CONSTRAINT OSEE_CONFLICT__PK PRIMARY KEY (MERGE_BRANCH_ID,SOURCE_GAMMA_ID),
	CONSTRAINT OSEE_CONFLICT__MBI_FK FOREIGN KEY (MERGE_BRANCH_ID) REFERENCES OSEE_MERGE (MERGE_BRANCH_ID) 
);

-- OSEE_JOIN_EXPORT_IMPORT
CREATE TABLE OSEE_JOIN_EXPORT_IMPORT ( 
	ID2 ${db.bigint} NOT NULL,
	ID1 ${db.bigint} NOT NULL,
	QUERY_ID int NOT NULL)
	${db.tablespace.osee_join};

-- OSEE_IMPORT_SOURCE
CREATE TABLE OSEE_IMPORT_SOURCE ( 
	IMPORT_ID int NOT NULL,
	SOURCE_EXPORT_DATE timestamp NOT NULL,
	DB_SOURCE_GUID varchar (28) NOT NULL,
	DATE_IMPORTED timestamp NOT NULL,
	CONSTRAINT OSEE_IMPORT_MAP_PK PRIMARY KEY (IMPORT_ID) 
);

-- OSEE_IMPORT_SAVE_POINT
CREATE TABLE OSEE_IMPORT_SAVE_POINT ( 
	IMPORT_ID int NOT NULL,
	STATE_ERROR varchar (4000),
	STATUS int NOT NULL,
	SAVE_POINT_NAME varchar (128) NOT NULL,
	CONSTRAINT OSEE_IMP_SAVE_POINT_II_PK PRIMARY KEY (IMPORT_ID,SAVE_POINT_NAME),
	CONSTRAINT OSEE_IMP_SAVE_POINT_II_FK FOREIGN KEY (IMPORT_ID) REFERENCES OSEE_IMPORT_SOURCE (IMPORT_ID) 
);

-- OSEE_IMPORT_MAP
CREATE TABLE OSEE_IMPORT_MAP ( 
	IMPORT_ID int NOT NULL,
	SEQUENCE_NAME varchar (128) NOT NULL,
	SEQUENCE_ID int NOT NULL,
	CONSTRAINT OSEE_IMPORT_MAP_II_PK PRIMARY KEY (SEQUENCE_ID),
	CONSTRAINT OSEE_IMPORT_MAP_II_FK FOREIGN KEY (IMPORT_ID) REFERENCES OSEE_IMPORT_SOURCE (IMPORT_ID) 
);

-- OSEE_IMPORT_INDEX_MAP
CREATE TABLE OSEE_IMPORT_INDEX_MAP ( 
	MAPPED_ID ${db.bigint} NOT NULL,
	SEQUENCE_ID int NOT NULL,
	ORIGINAL_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_IMPORT_INDEX_MAP_IOM_PK PRIMARY KEY (SEQUENCE_ID,ORIGINAL_ID,MAPPED_ID),
	CONSTRAINT OSEE_IMPORT_INDEX_MAP_II_FK FOREIGN KEY (SEQUENCE_ID) REFERENCES OSEE_IMPORT_MAP (SEQUENCE_ID) 
);

CREATE INDEX OSEE_IMPORT_INDEX_MAP_IO_IDX ON OSEE_IMPORT_INDEX_MAP (SEQUENCE_ID, ORIGINAL_ID) ${db.tablespace.osee_index};
CREATE INDEX OSEE_IMPORT_INDEX_MAP_IM_IDX ON OSEE_IMPORT_INDEX_MAP (SEQUENCE_ID, MAPPED_ID) ${db.tablespace.osee_index};

-- OSEE_JOIN_ARTIFACT
CREATE TABLE OSEE_JOIN_ARTIFACT ( 
	ART_ID int NOT NULL,
	BRANCH_ID ${db.bigint} NOT NULL,
	TRANSACTION_ID int,
	QUERY_ID int NOT NULL)
	${db.tablespace.osee_join};

CREATE INDEX OSEE_JOIN_ART__Q_A_IDX ON OSEE_JOIN_ARTIFACT (QUERY_ID, ART_ID) ${db.tablespace.osee_index};

-- OSEE_JOIN_ID
CREATE TABLE OSEE_JOIN_ID ( 
	ID ${db.bigint},
	QUERY_ID int NOT NULL)
	${db.tablespace.osee_join};

CREATE INDEX OSEE_JOIN_ID__Q_I_IDX ON OSEE_JOIN_ID (QUERY_ID, ID) ${db.tablespace.osee_index};

-- OSEE_JOIN_CLEANUP
CREATE TABLE OSEE_JOIN_CLEANUP ( 
	TABLE_NAME varchar (28) NOT NULL,
	EXPIRES_IN ${db.bigint} NOT NULL,
	ISSUED_AT ${db.bigint} NOT NULL,
	QUERY_ID int NOT NULL,
	CONSTRAINT OSEE_JOIN_CLEANUP__PK PRIMARY KEY (QUERY_ID))
	${db.tablespace.osee_join};

-- OSEE_JOIN_CHAR_ID
CREATE TABLE OSEE_JOIN_CHAR_ID ( 
	ID varchar (4000) NOT NULL,
	QUERY_ID int NOT NULL)
	${db.tablespace.osee_join};

CREATE INDEX OSEE_JOIN_CHAR__Q_IDX ON OSEE_JOIN_CHAR_ID (QUERY_ID) ${db.tablespace.osee_index};

-- OSEE_JOIN_TRANSACTION
CREATE TABLE OSEE_JOIN_TRANSACTION ( 
	BRANCH_ID ${db.bigint},
	TRANSACTION_ID int NOT NULL,
	QUERY_ID int NOT NULL,
	GAMMA_ID ${db.bigint} NOT NULL)
	${db.tablespace.osee_join};

CREATE INDEX OSEE_JOIN_TRANSACTION_Q_IDX ON OSEE_JOIN_TRANSACTION (QUERY_ID) ${db.tablespace.osee_index};

-- OSEE_BRANCH_GROUP
CREATE TABLE OSEE_BRANCH_GROUP (
	GROUP_TYPE ${db.bigint} NOT NULL,
	GROUP_ID ${db.bigint} NOT NULL,
	BRANCH_ID ${db.bigint} NOT NULL,
	GAMMA_ID ${db.bigint} NOT NULL,
	CONSTRAINT OSEE_BRANCH_GROUP__T_GROUP_ID_BRANCH_ID_PK PRIMARY KEY (GROUP_TYPE, GROUP_ID, BRANCH_ID))
	${db.organization_index_2};
 
CREATE INDEX OSEE_BRANCH_GROUP__G_IDX ON OSEE_BRANCH_GROUP (GAMMA_ID);