CREATE TABLE datasets (
  source VARCHAR(500),
  `key` VARCHAR(500),
  isodatetime VARCHAR(80),
  datatype VARCHAR(15),
  realdata REAL,
  intdata INTEGER,
  stringdata VARCHAR(12000)
);
CREATE TABLE apitokens (
  source VARCHAR(500),
  token VARCHAR(50)
);
