<?php

namespace Graeuler\Garden\Collect;

use Graeuler\Garden\Collect\AbstractDataFeedDatastoreTestBase;
use PHPUnit\Framework\TestCase;

class DataFeedDatastoreTestBase extends TestCase {
    
    protected static $db;

    public function tearDown() {
		if (null == self::$db) return;
        self::$db->exec("delete from datasets");
        self::$db->exec("delete from apitokens");
    }

    private function getDataStoreInstance() {
        return new DataFeedDatastore(self::$db);
    }
    
    private function storeSimpleDataSet(array $data = array()) {
        $defaults = array("source"=>"source", "key"=>"key", "time"=>"2016-12-02T01:42:27.283+01:00[Europe\/Berlin]", "value"=>1.0);
        foreach($defaults as $k => &$v) {
            if(isset($data[$k])) {
                $v = $data[$k];
            }
        }
        $dataSet = new GardenDataSet();
        $dataSet->addValue($defaults["source"], $defaults["key"], $defaults["time"], $defaults["value"]);
        return $dataSet;
    }
    
    public function testStoreDataSetDoubleType() {
        $dataStore = $this->getDataStoreInstance();
        $dataSet = $this->storeSimpleDataSet();
        $this->assertTrue($dataStore->storeDataSet($dataSet));
    }
    
    public function testStoreDataSetIntegerType() {
        $dataStore = $this->getDataStoreInstance();
        $dataSet = $this->storeSimpleDataSet(["value"=>5]);
        $this->assertTrue($dataStore->storeDataSet($dataSet));
    }
    
    public function testStoreDataSetBooleanType() {
        $dataStore = $this->getDataStoreInstance();
        $dataSet = $this->storeSimpleDataSet(["value"=>true]);
        $this->assertTrue($dataStore->storeDataSet($dataSet));
    }
    
    public function testStoreDataSetStringType() {
        $dataStore = $this->getDataStoreInstance();
        $dataSet = $this->storeSimpleDataSet(["value"=>"data"]);
        $this->assertTrue($dataStore->storeDataSet($dataSet));
    }
    
    /**
     * @expectedException Graeuler\Garden\Collect\InvalidDataException
     **/
    public function testStoreDataSetInvalidType() {
        $dataStore = $this->getDataStoreInstance();
        $dataSet = $this->storeSimpleDataSet(["value"=>new \stdClass()]);
        $this->assertTrue($dataStore->storeDataSet($dataSet));
    }

    
    public function testSelectTokenBySource() {
        self::$db->exec ('insert into apitokens values ("test-source", "test-token")');
        $dataStore = $this->getDataStoreInstance();
        $tokenList = $dataStore->selectTokenBySource("test-source");
        $this->assertCount(1, $tokenList);
        $this->assertEquals("test-token", $tokenList[0]);
    }
    
    public function testSelectTokenByInvalidSource() {
        self::$db->exec ('insert into apitokens values ("invalid-source", "test-token")');
        $dataStore = $this->getDataStoreInstance();
        $tokenList = $dataStore->selectTokenBySource("test-source");
        $this->assertCount(0, $tokenList);
    }
    
    public function testSelectTokenBySourceMulti() {
        self::$db->exec ('insert into apitokens values ("test-source", "test-token1")');
        self::$db->exec ('insert into apitokens values ("test-source", "test-token2")');
        self::$db->exec ('insert into apitokens values ("test-source", "test-token3")');
        $dataStore = $this->getDataStoreInstance();
        $tokenList = $dataStore->selectTokenBySource("test-source");
        $this->assertCount(3, $tokenList);
        $this->assertEquals("test-token3", $tokenList[2]);
    }
    
    public function testSelectDataByKeyNothing() {
        $dataStore = $this->getDataStoreInstance();
        $data = $dataStore->selectDataByKey("source", "key1");
        $this->assertEmpty($data);

        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key1", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key2", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        
        $data = $dataStore->selectDataByKey("source", "key3");
        $this->assertEmpty($data);
    }
    
    public function testSelectDataByKeySingle() {
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key1", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key2", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        
        $dataStore = $this->getDataStoreInstance();
        $data = $dataStore->selectDataByKey("source", "key1");
        $this->assertCount(1, $data);
        $this->assertEquals("v1", $data[0]['stringdata']);
    }
    
    public function testSelectDataByKeyMulti() {
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v2")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v3")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v4")');
        
        $dataStore = $this->getDataStoreInstance();
        $data = $dataStore->selectDataByKey("source", "key");
        $this->assertCount(4, $data);
        $this->assertEquals("v3", $data[2]['stringdata']);
    }
        
    public function testSelectKeysBySourceNothing() {
        $dataStore = $this->getDataStoreInstance();
        $keys = $dataStore->selectKeysBySource("source");
        $this->assertEmpty($keys);
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key1", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key2", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        $keys = $dataStore->selectKeysBySource("not-source");
        $this->assertEmpty($keys);
    }
    
    public function testSelectKeysBySourceSingle() {
        $dataStore = $this->getDataStoreInstance();
        $keys = $dataStore->selectKeysBySource("source");
        $this->assertEmpty($keys);
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key1", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        $keys = $dataStore->selectKeysBySource("source");
        $this->assertCount(1, $keys);
        $this->assertEquals("key1", $keys[0], print_r($keys, true));
    }
    
    public function testSelectKeysBySourceMulti() {
        $dataStore = $this->getDataStoreInstance();
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key1", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v1")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key1", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v2")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key2", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v3")');
        self::$db->exec ('insert into datasets(source, `key`, isodatetime, datatype, stringdata) values ("source", "key2", "2016-12-02T01:42:27.283+01:00[Europe/Berlin]", "string", "v4")');
        $keys = $dataStore->selectKeysBySource("source");
        $this->assertCount(2, $keys);
        $this->assertEquals("key2", $keys[1], print_r($keys, true));
    }
    
}