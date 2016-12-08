<?php

namespace Graeuler\Garden\Collect;



class DataFeedDatastore {
    
    private $db;
    private $insertRecord = null;
    
    const INSERT_RECORD_DML =  <<<DML
INSERT INTO datasets 
  (source, key, isodatetime, datatype, realdata, intdata, stringdata)
VALUES
  (:source, :key, :isodatetime, :datatype, :realdata, :intdata, :stringdata)
DML;

    private $selectTokenBySource = null;
    const SELECT_TOKEN_BY_SOURCE = <<<SQL
SELECT token 
  FROM apitokens
 WHERE source = :source
 ORDER BY token asc
SQL;

    private $selectDataByKey = null;
    const SELECT_DATA_BY_KEY = <<<SQL
SELECT isodatetime, datatype, realdata, intdata, stringdata 
  FROM datasets
 WHERE source = :source AND key = :key
 ORDER BY isodatetime
SQL;

    private $selectKeysBySource = null;
    const SELECT_KEYS_BY_SOURCE = <<<SQL
SELECT DISTINCT key 
  FROM datasets
 WHERE source = :source
 ORDER BY key
SQL;
    
    public function __construct(\PDO $db) {
        $this->db = $db;
        $this->db->setAttribute(\PDO::ATTR_ERRMODE, \PDO::ERRMODE_EXCEPTION);
        $this->prepareStatements();
    }
    
    protected function prepareStatements() {
        $this->insertRecord = $this->db->prepare(self::INSERT_RECORD_DML);
        $this->selectTokenBySource = $this->db->prepare(self::SELECT_TOKEN_BY_SOURCE);
        $this->selectKeysBySource = $this->db->prepare(self::SELECT_KEYS_BY_SOURCE);
        $this->selectDataByKey = $this->db->prepare(self::SELECT_DATA_BY_KEY);
   }

    private function fail($message) {
        throw new InvalidDataException('Data Processing Error: ' . $message);
    }
    
    public function storeDataSet(DataSetInterface $dataSet) {
                
        $this->db->beginTransaction();
        // if ($dataSet instanceOf GardenDataSet ) ... or: switch (true) {case $dataSet instanceOf GardenDataSet: ... }
        $source = $key = $isoDateTime = $datatype = $realvalue = $intvalue = $stringvalue = null;
        $this->insertRecord->bindParam(':source',      $source); 
        $this->insertRecord->bindParam(':key',         $key); 
        $this->insertRecord->bindParam(':isodatetime', $isoDateTime); 
        $this->insertRecord->bindParam(':datatype',    $datatype); 
        $this->insertRecord->bindParam(':realdata',    $realvalue); 
        $this->insertRecord->bindParam(':intdata',     $intvalue); 
        $this->insertRecord->bindParam(':stringdata',  $stringvalue); 
        
        foreach($dataSet->asIterableList() as list($source, $key,  $isoDateTime, $value)) {
            $datatype = gettype($value);
            $bindName = $realvalue = $intvalue = $stringvalue = null;                
            switch ($datatype) {
                case 'integer': $intvalue = $value; break;
                case 'double':  $realvalue = $value; break;
                case 'boolean':
                case 'string':  $stringvalue = $value; break;
                default: $this->fail('Unsupported datatype.'); break;
            }
            $this->insertRecord->execute();
        }
        $this->db->commit();
        return true;
    }
    
    public function selectTokenBySource($source) {
        $q = $this->selectTokenBySource;
        $q->bindValue(":source", $source);
        $q->execute();
        $result = $q->fetchAll(\PDO::FETCH_COLUMN, 0);
        return $result;
    }
    
    public function selectDataByKey($source, $key) {
        $q = $this->selectDataByKey;
        $q->bindValue(":source", $source);
        $q->bindValue(":key", $key);
        $q->execute();
        $result = $q->fetchAll(\PDO::FETCH_ASSOC);
        return $result;
    }
    
    public function selectKeysBySource($source) {
        $q = $this->selectKeysBySource;
        $q->bindValue(":source", $source);
        $q->execute();
        $result = $q->fetchAll(\PDO::FETCH_COLUMN, 0);
        return $result;
    }
}