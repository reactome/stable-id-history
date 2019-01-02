CREATE TABLE StableIdentifier (
    entryId INT(32) PRIMARY KEY,
    stableId VARCHAR(255) NOT NULL,
    stableIdVersion INT(32) NOT NULL,
    oldStableId VARCHAR(255),
    instanceId INT(32) NOT NULL,
    instanceName VARCHAR(255) NOT NULL,
    instanceClass VARCHAR(255) NOT NULL,
    instanceType VARCHAR(255) NOT NULL,
    reactomeVersion INT(32) NOT NULL
) ENGINE=InnoDB;
